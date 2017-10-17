/*
 * Copyright 2016 Metamarkets Group Inc.
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
 *
 */

package com.metamx.metrics;

import com.google.common.collect.ImmutableMap;
import com.metamx.emitter.core.HttpPostEmitter;
import com.metamx.emitter.service.ServiceEmitter;
import com.metamx.emitter.service.ServiceMetricEvent;

public class HttpPostEmitterMonitor extends FeedDefiningMonitor
{
  private final HttpPostEmitter httpPostEmitter;
  private final ImmutableMap<String, String> extraDimensions;
  private final ServiceMetricEvent.Builder builder;
  private long lastTotalEmittedEvents = 0;
  private int lastTotalAllocatedBuffers = 0;

  public HttpPostEmitterMonitor(
      String feed,
      HttpPostEmitter httpPostEmitter,
      ImmutableMap<String, String> extraDimensions
  )
  {
    super(feed);
    this.httpPostEmitter = httpPostEmitter;
    this.extraDimensions = extraDimensions;
    this.builder = builder();
  }

  @Override
  public boolean doMonitor(ServiceEmitter emitter)
  {
    long newTotalEmittedEvents = httpPostEmitter.getTotalEmittedEvents();
    long totalEmittedEventsDiff = newTotalEmittedEvents - lastTotalEmittedEvents;
    emitter.emit(builder.build("emitter/events/emitted", totalEmittedEventsDiff));
    lastTotalEmittedEvents = newTotalEmittedEvents;

    int newTotalAllocatedBuffers = httpPostEmitter.getTotalAllocatedBuffers();
    int totalAllocatedBuffersDiff = newTotalAllocatedBuffers - lastTotalAllocatedBuffers;
    emitter.emit(builder.build("emitter/buffers/allocated", totalAllocatedBuffersDiff));
    lastTotalAllocatedBuffers = newTotalAllocatedBuffers;

    emitter.emit(builder.build("emitter/events/emitQueue", httpPostEmitter.getEventsToEmit()));
    emitter.emit(builder.build("emitter/events/large/emitQueue", httpPostEmitter.getLargeEventsToEmit()));
    emitter.emit(builder.build("emitter/buffers/emitQueue", httpPostEmitter.getBuffersToEmit()));
    emitter.emit(builder.build("emitter/buffers/failed", httpPostEmitter.getFailedBuffers()));
    emitter.emit(builder.build("emitter/buffers/reuseQueue", httpPostEmitter.getBuffersToReuse()));

    return true;
  }

  @Override
  protected ServiceMetricEvent.Builder builder()
  {
    ServiceMetricEvent.Builder builder = super.builder();
    extraDimensions.forEach(builder::setDimension);
    return builder;
  }
}
