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

package com.metamx.metrics;

import com.google.common.collect.ImmutableMap;
import com.metamx.common.StringUtils;
import com.metamx.emitter.service.ServiceEmitter;
import com.metamx.emitter.service.ServiceMetricEvent;
import com.metamx.metrics.cgroups.CgroupDiscoverer;
import com.metamx.metrics.cgroups.Memory;

import java.util.Map;

public class CgroupMemoryMonitor extends FeedDefiningMonitor
{
  final CgroupDiscoverer cgroupDiscoverer;

  public CgroupMemoryMonitor(CgroupDiscoverer cgroupDiscoverer, final Map<String, String[]> dimensions, String feed)
  {
    super(feed);
    this.cgroupDiscoverer = cgroupDiscoverer;
  }

  public CgroupMemoryMonitor(final Map<String, String[]> dimensions, String feed)
  {
    this(null, dimensions, feed);
  }

  public CgroupMemoryMonitor(final Map<String, String[]> dimensions)
  {
    this(dimensions, DEFAULT_METRICS_FEED);
  }

  public CgroupMemoryMonitor()
  {
    this(ImmutableMap.of());
  }

  @Override
  public boolean doMonitor(ServiceEmitter emitter)
  {
    final Memory memory = new Memory(cgroupDiscoverer);
    final Memory.MemoryStat stat = memory.snapshot();
    stat.getMemoryStats().forEach((key, value) -> {
      final ServiceMetricEvent.Builder builder = builder();
      // See https://www.kernel.org/doc/Documentation/cgroup-v1/memory.txt
      // There are inconsistent units for these. Most are bytes.
      emitter.emit(builder.build(StringUtils.safeFormat("cgroup/memory/%s", key), value));
    });
    stat.getNumaMemoryStats().forEach((key, value) -> {
      final ServiceMetricEvent.Builder builder = builder().setDimension("numaZone", Long.toString(key));
      value.forEach((k, v) -> {
        emitter.emit(builder.build(StringUtils.safeFormat("cgroup/memory_numa/%s/pages", k), v));
      });
    });
    return true;
  }
}
