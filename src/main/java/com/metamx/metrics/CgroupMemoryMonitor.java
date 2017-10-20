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
      emitter.emit(builder.build(StringUtils.safeFormat("cgroup/memory/%s", key), value));
    });
    stat.getNumaMemoryStats().forEach((key, value) -> {
      final ServiceMetricEvent.Builder builder = builder().setDimension("numaZone", Long.toString(key));
      value.forEach((k, v) -> {
        emitter.emit(builder.build(StringUtils.safeFormat("cgroup/memory_numa/%s_pages", k), v));
      });
    });
    return true;
  }
}
