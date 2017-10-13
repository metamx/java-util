/*
 * Copyright 2012 - 2017 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.emitter.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.metamx.common.ISE;
import com.metamx.common.RetryUtils;
import com.metamx.common.StringUtils;
import com.metamx.common.lifecycle.LifecycleStart;
import com.metamx.common.lifecycle.LifecycleStop;
import com.metamx.common.logger.Logger;
import com.metamx.http.client.HttpClient;
import com.metamx.http.client.Request;
import com.metamx.http.client.response.StatusResponseHandler;
import com.metamx.http.client.response.StatusResponseHolder;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.GZIPOutputStream;

public class HttpPostEmitter implements Flushable, Closeable, Emitter
{
  private static final int MAX_EVENT_SIZE = 1023 * 1024; // Set max size slightly less than 1M to allow for metadata

  private static final int MAX_SEND_RETRIES = 3;
  /**
   * Used in {@link EmittingThread#emitLargeEvents()} to ensure fair emitting of both large events and batched events.
   */
  private static final byte[] LARGE_EVENTS_STOP = new byte[] {};

  private static final Logger log = new Logger(HttpPostEmitter.class);
  private static final AtomicInteger instanceCounter = new AtomicInteger();

  final BatchingStrategy batchingStrategy;
  final HttpEmitterConfig config;
  private final int bufferSize;
  final int maxBufferWatermark;
  private final int largeEventThreshold;
  private final HttpClient client;
  private final ObjectMapper jsonMapper;
  private final URL url;

  private final ConcurrentLinkedQueue<byte[]> buffersToReuse = new ConcurrentLinkedQueue<>();

  /**
   * concurrentBatch.get() == null means the service is closed.
   */
  private final AtomicReference<Batch> concurrentBatch = new AtomicReference<>();

  private final ConcurrentLinkedQueue<Batch> buffersToEmit = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<byte[]> largeEventsToEmit = new ConcurrentLinkedQueue<>();
  private final EmittedBatchCounter emittedBatchCounter = new EmittedBatchCounter();
  private final EmittingThread emittingThread = new EmittingThread();
  private final AtomicLong totalEmittedEvents = new AtomicLong();
  private final AtomicInteger allocatedBuffers = new AtomicInteger();

  private final Object startLock = new Object();
  private final CountDownLatch startLatch = new CountDownLatch(1);
  private boolean running = false;

  public HttpPostEmitter(HttpEmitterConfig config, HttpClient client)
  {
    this(config, client, new ObjectMapper());
  }

  public HttpPostEmitter(HttpEmitterConfig config, HttpClient client, ObjectMapper jsonMapper)
  {
    batchingStrategy = config.getBatchingStrategy();
    final int batchOverhead = batchingStrategy.batchStartLength() + batchingStrategy.batchEndLength();
    Preconditions.checkArgument(
        config.getMaxBatchSize() >= MAX_EVENT_SIZE + batchOverhead,
        String.format(
            "maxBatchSize must be greater than MAX_EVENT_SIZE[%,d] + overhead[%,d].",
            MAX_EVENT_SIZE,
            batchOverhead
        )
    );
    Preconditions.checkArgument(
        config.getMaxBufferSize() >= MAX_EVENT_SIZE,
        String.format(
            "maxBufferSize must be greater than MAX_EVENT_SIZE[%,d].",
            MAX_EVENT_SIZE
        )
    );
    this.config = config;
    this.bufferSize = config.getMaxBatchSize();
    this.maxBufferWatermark = bufferSize - batchingStrategy.batchEndLength();
    // Chosen so that if event size < largeEventThreshold, at least 2 events could fit the standard buffer.
    this.largeEventThreshold = (bufferSize - batchOverhead - batchingStrategy.separatorLength()) / 2;
    this.client = client;
    this.jsonMapper = jsonMapper;
    try {
      this.url = new URL(config.getRecipientBaseUrl());
    }
    catch (MalformedURLException e) {
      throw new ISE(e, "Bad URL: %s", config.getRecipientBaseUrl());
    }
    concurrentBatch.set(new Batch(this, acquireBuffer(), 0));
  }

  @Override
  @LifecycleStart
  public void start()
  {
    synchronized (startLock) {
      if (!running) {
        if (startLatch.getCount() == 0) {
          throw new IllegalStateException("Already started.");
        }
        running = true;
        startLatch.countDown();
        emittingThread.start();
      }
    }
  }

  private void awaitStarted()
  {
    try {
      if (!startLatch.await(1, TimeUnit.SECONDS)) {
        throw new RejectedExecutionException("Service is not started.");
      }
      if (isTerminated()) {
        throw new RejectedExecutionException("Service is closed.");
      }
    }
    catch (InterruptedException e) {
      log.debug("Interrupted waiting for start");
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  private boolean isTerminated()
  {
    return concurrentBatch.get() == null;
  }

  @Override
  public void emit(Event event)
  {
    emitAndReturnBatch(event);
  }

  @VisibleForTesting
  Batch emitAndReturnBatch(Event event)
  {
    awaitStarted();

    final byte[] eventBytes = eventToBytes(event);

    if (eventBytes.length > MAX_EVENT_SIZE) {
      log.error(
          "Event too large to emit (%,d > %,d): %s ...",
          eventBytes.length,
          MAX_EVENT_SIZE,
          StringUtils.fromUtf8(ByteBuffer.wrap(eventBytes), 1024)
      );
      return null;
    }

    if (eventBytes.length > largeEventThreshold) {
      writeLargeEvent(eventBytes);
      return null;
    }

    while (true) {
      Batch batch = concurrentBatch.get();
      if (batch == null) {
        throw new RejectedExecutionException("Service is closed.");
      }
      if (batch.tryAddEvent(eventBytes)) {
        return batch;
      }
      // Spin loop, until the thread calling onSealExclusive() updates the concurrentBatch. This update becomes visible
      // eventually, because concurrentBatch.get() is a volatile read.
    }
  }

  private byte[] eventToBytes(Event event)
  {
    try {
      return jsonMapper.writeValueAsBytes(event);
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private void writeLargeEvent(byte[] eventBytes)
  {
    largeEventsToEmit.add(eventBytes);
    wakeUpEmittingThread();
  }

  /**
   * Called from {@link Batch} only once for each Batch in existence.
   */
  void onSealExclusive(Batch batch)
  {
    buffersToEmit.add(batch);
    wakeUpEmittingThread();
    if (!isTerminated()) {
      int nextBatchNumber = EmittedBatchCounter.nextBatchNumber(batch.batchNumber);
      if (!concurrentBatch.compareAndSet(batch, new Batch(this, acquireBuffer(), nextBatchNumber))) {
        // If compareAndSet failed, the service is closed concurrently.
        Preconditions.checkState(isTerminated());
      }
    }
  }

  private void wakeUpEmittingThread()
  {
    LockSupport.unpark(emittingThread);
  }

  @Override
  public void flush() throws IOException
  {
    awaitStarted();
    flush(concurrentBatch.get());
  }

  private void flush(Batch batch) throws IOException
  {
    if (batch == null) {
      return;
    }
    batch.seal();
    try {
      emittedBatchCounter.awaitBatchEmitted(batch.batchNumber, config.getFlushTimeOut(), TimeUnit.MILLISECONDS);
    }
    catch (TimeoutException e) {
      String message = String.format("Timed out after [%d] millis during flushing", config.getFlushTimeOut());
      throw new IOException(message, e);
    }
    catch (InterruptedException e) {
      log.debug("Thread Interrupted");
      Thread.currentThread().interrupt();
      throw new IOException("Thread Interrupted while flushing", e);
    }
  }

  @Override
  @LifecycleStop
  public void close() throws IOException
  {
    synchronized (startLock) {
      if (running) {
        running = false;
        Batch lastBatch = concurrentBatch.getAndSet(null);
        flush(lastBatch);
        emittingThread.shuttingDown = true;
        // EmittingThread is interrupted after the last batch is flushed.
        emittingThread.interrupt();
      }
    }
  }

  @Override
  public String toString()
  {
    return "HttpPostEmitter{" +
           "config=" + config +
           '}';
  }

  private class EmittingThread extends Thread
  {
    private final ArrayDeque<FailedBuffer> failedBuffers = new ArrayDeque<>();
    private boolean shuttingDown = false;
    private ZeroCopyByteArrayOutputStream gzipBaos;

    EmittingThread()
    {
      super("HttpPostEmitter-" + instanceCounter.incrementAndGet());
      setDaemon(true);
    }

    @Override
    public void run()
    {
      while (true) {
        boolean needsToShutdown = needsToShutdown();
        try {
          emitLargeEvents();
          emitBatches();
          tryEmitOneFailedBuffer();

          if (needsToShutdown) {
            tryEmitAndDrainAllFailedBuffers();
            // Make GC life easier
            drainBuffersToReuse();
            return;
          }
        }
        catch (Throwable t) {
          log.error(t, "Uncaught exception in EmittingThread.run()");
        }
        if (failedBuffers.isEmpty()) {
          // Waiting for 1/2 of config.getFlushMillis() in order to flush events not more than 50% later than specified.
          // If nanos=0 parkNanos() doesn't wait at all, then we don't want.
          long waitNanos = Math.max(TimeUnit.MILLISECONDS.toNanos(config.getFlushMillis()) / 2, 1);
          LockSupport.parkNanos(HttpPostEmitter.this, waitNanos);
        }
      }
    }

    private boolean needsToShutdown()
    {
      boolean needsToShutdown = Thread.interrupted() || shuttingDown;
      if (needsToShutdown) {
        Batch lastBatch = concurrentBatch.getAndSet(null);
        if (lastBatch != null) {
          lastBatch.seal();
        }
      } else {
        Batch batch = concurrentBatch.get();
        if (batch != null) {
          batch.sealIfFlushNeeded();
        } else {
          needsToShutdown = true;
        }
      }
      return needsToShutdown;
    }

    private void emitBatches()
    {
      for (Batch batch; (batch = buffersToEmit.poll()) != null; ) {
        emit(batch);
      }
    }

    private void emit(final Batch batch)
    {
      // Awaits until all concurrent event writers finish copy their event bytes to the buffer. This call provides
      // memory visibility guarantees.
      batch.awaitEmittingAllowed();
      try {
        final int bufferWatermark = batch.getSealedBufferWatermark();
        if (bufferWatermark == 0) { // sealed while empty
          return;
        }
        int eventCount = batch.eventCount.get();
        log.debug(
            "Sending batch #%d to url[%s], event count[%d], bytes[%d]",
            batch.batchNumber,
            url,
            eventCount,
            bufferWatermark
        );
        int bufferEndOffset = batchingStrategy.writeBatchEnd(batch.buffer, bufferWatermark);

        if (sendWithRetries(batch.buffer, bufferEndOffset, eventCount)) {
          buffersToReuse.add(batch.buffer);
        } else {
          failedBuffers.add(new FailedBuffer(batch.buffer, bufferEndOffset, eventCount));
        }
      }
      finally {
        // Notify HttpPostEmitter.flush(), that the batch is emitted (or failed).
        emittedBatchCounter.batchEmitted(batch.batchNumber);
      }
    }

    private void emitLargeEvents()
    {
      if (largeEventsToEmit.isEmpty()) {
        return;
      }
      // Don't try to emit large events until exhaustion, to avoid starvation of "normal" batches, if large event
      // posting rate is too high, though it should never happen in practice.
      largeEventsToEmit.add(LARGE_EVENTS_STOP);
      for (byte[] largeEvent; (largeEvent = largeEventsToEmit.poll()) != LARGE_EVENTS_STOP; ) {
        emitLargeEvent(largeEvent);
      }
    }

    private void emitLargeEvent(byte[] eventBytes)
    {
      byte[] buffer = acquireBuffer();
      int bufferOffset = batchingStrategy.writeBatchStart(buffer);
      System.arraycopy(eventBytes, 0, buffer, bufferOffset, eventBytes.length);
      bufferOffset += eventBytes.length;
      bufferOffset = batchingStrategy.writeBatchEnd(buffer, bufferOffset);
      if (sendWithRetries(buffer, bufferOffset, 1)) {
        buffersToReuse.add(buffer);
      } else {
        failedBuffers.add(new FailedBuffer(buffer, bufferOffset, 1));
      }
    }

    private void tryEmitOneFailedBuffer()
    {
      FailedBuffer failedBuffer = failedBuffers.peek();
      if (failedBuffer != null) {
        if (sendWithRetries(failedBuffer.buffer, failedBuffer.length, failedBuffer.eventCount)) {
          // Remove from the queue of failed buffer.
          failedBuffers.poll();
          // Don't add the failed buffer back to the buffersToReuse queue here, because in a situation when we were not
          // able to emit events for a while we don't have a way to discard buffers that were used to accumulate events
          // during that period, if they are added back to buffersToReuse. For instance it may result in having 100
          // buffers in rotation even if we need just 2.
        }
      }
    }

    private void tryEmitAndDrainAllFailedBuffers()
    {
      for (FailedBuffer failedBuffer; (failedBuffer = failedBuffers.poll()) != null; ) {
        sendWithRetries(failedBuffer.buffer, failedBuffer.length, failedBuffer.eventCount);
      }
    }

    /**
     * Returns true if sent successfully.
     */
    private boolean sendWithRetries(final byte[] buffer, final int length, final int eventCount)
    {
      try {
        RetryUtils.retry(
            new Callable<Void>()
            {
              @Override
              public Void call() throws Exception
              {
                send(buffer, length);
                return null;
              }
            },
            new Predicate<Throwable>()
            {
              @Override
              public boolean apply(Throwable input)
              {
                return !(input instanceof InterruptedException);
              }
            },
            MAX_SEND_RETRIES
        );
        totalEmittedEvents.addAndGet(eventCount);
        return true;
      }
      catch (InterruptedException e) {
        return false;
      }
      catch (Exception e) {
        log.error(e, "Failed to send events to url[%s]", config.getRecipientBaseUrl());
        return false;
      }
    }

    private void send(byte[] buffer, int length) throws Exception
    {
      final Request request = new Request(HttpMethod.POST, url);
      byte[] payload;
      int payloadLength;
      ContentEncoding contentEncoding = config.getContentEncoding();
      if (contentEncoding != null) {
        switch (contentEncoding) {
          case GZIP:
            try (GZIPOutputStream gzipOutputStream = acquireGzipOutputStream(length)) {
              gzipOutputStream.write(buffer, 0, length);
            }
            payload = gzipBaos.getBuffer();
            payloadLength = gzipBaos.size();
            request.setHeader(HttpHeaders.Names.CONTENT_ENCODING, HttpHeaders.Values.GZIP);
            break;
          default:
            throw new ISE("Unsupported content encoding [%s]", contentEncoding.name());
        }
      } else {
        payload = buffer;
        payloadLength = length;
      }


      request.setContent("application/json", payload, 0, payloadLength);

      if (config.getBasicAuthentication() != null) {
        final String[] parts = config.getBasicAuthentication().split(":", 2);
        final String user = parts[0];
        final String password = parts.length > 1 ? parts[1] : "";
        request.setBasicAuthentication(user, password);
      }

      final StatusResponseHolder response = client.go(request, new StatusResponseHandler(Charsets.UTF_8)).get();

      if (response.getStatus().getCode() == 413) {
        throw new ISE(
            "Received HTTP status 413 from [%s]. Batch size of [%d] may be too large, "
            + "try adjusting maxBatchSizeBatch property",
            config.getRecipientBaseUrl(),
            config.getMaxBatchSize()
        );
      }

      if (response.getStatus().getCode() / 100 != 2) {
        throw new ISE(
            "Emissions of events not successful[%s], with message[%s].",
            response.getStatus(),
            response.getContent().trim()
        );
      }
    }

    GZIPOutputStream acquireGzipOutputStream(int length) throws IOException
    {
      if (gzipBaos == null) {
        gzipBaos = new ZeroCopyByteArrayOutputStream(length);
      } else {
        gzipBaos.reset();
      }
      return new GZIPOutputStream(gzipBaos, true);
    }
  }

  private static class FailedBuffer
  {
    final byte[] buffer;
    final int length;
    final int eventCount;

    private FailedBuffer(byte[] buffer, int length, int eventCount)
    {
      this.buffer = buffer;
      this.length = length;
      this.eventCount = eventCount;
    }
  }

  private byte[] acquireBuffer()
  {
    byte[] buffer = buffersToReuse.poll();
    if (buffer == null) {
      buffer = new byte[bufferSize];
      allocatedBuffers.incrementAndGet();
    }
    return buffer;
  }

  private void drainBuffersToReuse()
  {
    while (buffersToReuse.poll() != null) {
      // loop
    }
  }

  @VisibleForTesting
  int getAllocatedBuffers()
  {
    return allocatedBuffers.get();
  }

  @VisibleForTesting
  long getTotalEmittedEvents()
  {
    return totalEmittedEvents.get();
  }

  @VisibleForTesting
  void waitForEmission(int batchNumber) throws Exception
  {
    emittedBatchCounter.awaitBatchEmitted(batchNumber, 10, TimeUnit.SECONDS);
  }
}
