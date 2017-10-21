/*
 * Copyright 2017 Metamarkets Group Inc.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.metamx.http.client.GoHandler;
import com.metamx.http.client.MockHttpClient;
import com.metamx.http.client.Request;
import com.metamx.http.client.response.HttpResponseHandler;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class HttpEmitterTest
{
  private static final Future OK_FUTURE = Futures.immediateFuture(EmitterTest.OK_RESPONSE);
  private final MockHttpClient httpClient = new MockHttpClient();
  private static final ObjectMapper objectMapper = new ObjectMapper()
  {
    @Override
    public byte[] writeValueAsBytes(Object value) throws JsonProcessingException
    {
      return Ints.toByteArray(((IntEvent) value).index);
    }
  };

  private final AtomicLong timeoutUsed = new AtomicLong();

  @Before
  public void setup()
  {
    timeoutUsed.set(-1L);

    httpClient.setGoHandler(new GoHandler()
    {
      @Override
      protected <Intermediate, Final> ListenableFuture<Final> go(
          Request request,
          HttpResponseHandler<Intermediate, Final> httpResponseHandler,
          Duration timeout
      ) throws Exception
      {
        if (timeout != null) {
          timeoutUsed.set(timeout.getMillis());
        } else {
          timeoutUsed.set(Long.MAX_VALUE);
        }
        return (ListenableFuture<Final>) OK_FUTURE;
      }
    });
  }

  @Test
  public void timeoutEmptyQueue() throws IOException, InterruptedException
  {
    final HttpEmitterConfig config = new HttpEmitterConfig.Builder("http://foo.bar")
        .setBatchingStrategy(BatchingStrategy.ONLY_EVENTS)
        .setHttpTimeoutAllowanceFactor(2.0f)
        .build();
    final HttpPostEmitter emitter = new HttpPostEmitter(config, httpClient, objectMapper);

    emitter.start();
    emitter.emitAndReturnBatch(new IntEvent());
    emitter.flush();
    Assert.assertEquals(Long.MAX_VALUE, timeoutUsed.get());

    final Batch batch = emitter.emitAndReturnBatch(new IntEvent());
    Thread.sleep(1000);
    batch.seal();
    emitter.flush();
    Assert.assertTrue(timeoutUsed.get() >= 2000 && timeoutUsed.get() < 3000);
  }

  @Test
  public void timeoutMaxQueue() throws IOException, InterruptedException
  {
    final HttpEmitterConfig config = new HttpEmitterConfig.Builder("http://foo.bar")
        .setBatchingStrategy(BatchingStrategy.ONLY_EVENTS)
        .setHttpTimeoutAllowanceFactor(2.0f)
        .setBatchQueueThreshold(0)
        .build();
    final HttpPostEmitter emitter = new HttpPostEmitter(config, httpClient, objectMapper);

    emitter.start();
    emitter.emitAndReturnBatch(new IntEvent());
    emitter.flush();
    Assert.assertEquals(Long.MAX_VALUE, timeoutUsed.get());

    final Batch batch = emitter.emitAndReturnBatch(new IntEvent());
    Thread.sleep(1000);
    batch.seal();
    emitter.flush();
    Assert.assertTrue(timeoutUsed.get() >= 1000 && timeoutUsed.get() < 2000);
  }
}
