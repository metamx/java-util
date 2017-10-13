package com.metamx.metrics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.metamx.emitter.service.ServiceEmitter;
import com.metamx.emitter.service.ServiceMetricEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import org.gridkit.lab.jvm.perfdata.JStatData;
import org.gridkit.lab.jvm.perfdata.JStatData.LongCounter;

public class JvmThreadsMonitor extends FeedDefiningMonitor
{
  private final Map<String, String[]> dimensions;

  private int lastLiveThreads = 0;
  private long lastStartedThreads = 0;

  public JvmThreadsMonitor()
  {
    this(ImmutableMap.<String, String[]>of());
  }

  public JvmThreadsMonitor(Map<String, String[]> dimensions){
    this(dimensions, DEFAULT_METRICS_FEED);
  }

  public JvmThreadsMonitor(Map<String, String[]> dimensions, String feed)
  {
    super(feed);
    Preconditions.checkNotNull(dimensions);
    this.dimensions = ImmutableMap.copyOf(dimensions);
  }

  @Override
  public boolean doMonitor(ServiceEmitter emitter)
  {
    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    final ServiceMetricEvent.Builder builder = builder();
    MonitorUtils.addDimensionsToBuilder(builder, dimensions);

    // Because between next two calls on ThreadMXBean new threads can be started we can observe some inconsistency
    // in counters values and finished counter could be even negative
    int newLiveThreads = threadBean.getThreadCount();
    long newStartedThreads = threadBean.getTotalStartedThreadCount();

    long startedThreadsDiff = newStartedThreads - lastStartedThreads;

    emitter.emit(builder.build("jvm/threads/started", startedThreadsDiff));
    emitter.emit(builder.build("jvm/threads/finished", lastLiveThreads + startedThreadsDiff - newLiveThreads));
    emitter.emit(builder.build("jvm/threads/live", newLiveThreads));
    emitter.emit(builder.build("jvm/threads/liveDaemon", threadBean.getDaemonThreadCount()));

    emitter.emit(builder.build("jvm/threads/livePeak", threadBean.getPeakThreadCount()));
    threadBean.resetPeakThreadCount();

    lastStartedThreads = newStartedThreads;
    lastLiveThreads = newLiveThreads;

    return true;
  }
}
