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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.metamx.common.logger.Logger;
import com.metamx.emitter.service.ServiceEmitter;
import com.metamx.emitter.service.ServiceMetricEvent;
import com.metamx.metrics.cgroups.CgroupDiscoverer;
import com.metamx.metrics.cgroups.CpuAcct;
import com.metamx.metrics.cgroups.ProcCgroupDiscoverer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.joda.time.DateTime;

public class CpuAcctDeltaMonitor extends FeedDefiningMonitor
{
  private static final Logger log = new Logger(CpuAcctDeltaMonitor.class);
  private final AtomicReference<SnapshotHolder> priorSnapshot = new AtomicReference<>(null);
  private final Map<String, String[]> dimensions;

  private final PidDiscoverer pidDiscoverer;
  private final CgroupDiscoverer cgroupDiscoverer;

  public CpuAcctDeltaMonitor()
  {
    this(ImmutableMap.of());
  }

  public CpuAcctDeltaMonitor(final Map<String, String[]> dimensions)
  {
    this(dimensions, DEFAULT_METRICS_FEED);
  }

  public CpuAcctDeltaMonitor(final Map<String, String[]> dimensions, final String feed)
  {
    this(feed, dimensions, JvmPidDiscoverer.instance(), new ProcCgroupDiscoverer());
  }

  public CpuAcctDeltaMonitor(
      String feed,
      Map<String, String[]> dimensions,
      PidDiscoverer pidDiscoverer,
      CgroupDiscoverer cgroupDiscoverer
  )
  {
    super(feed);
    Preconditions.checkNotNull(dimensions);
    this.dimensions = ImmutableMap.copyOf(dimensions);
    this.pidDiscoverer = Preconditions.checkNotNull(pidDiscoverer, "pidDiscoverer required");
    this.cgroupDiscoverer = Preconditions.checkNotNull(cgroupDiscoverer, "cgroupDiscoverer required");
  }

  @Override
  public boolean doMonitor(ServiceEmitter emitter)
  {
    final CpuAcct cpuAcct = new CpuAcct(cgroupDiscoverer, pidDiscoverer);
    final CpuAcct.CpuAcctMetric snapshot = cpuAcct.snapshot();
    final long nanoTime = System.nanoTime(); // Approx time... may be influenced by an unlucky GC
    final DateTime dateTime = new DateTime();
    final SnapshotHolder priorSnapshotHolder = this.priorSnapshot.get();
    if (!priorSnapshot.compareAndSet(priorSnapshotHolder, new SnapshotHolder(snapshot, nanoTime))) {
      log.debug("Pre-empted by another monitor run");
      return false;
    }
    if (priorSnapshotHolder == null) {
      log.info("Detected first run, storing result for next run");
      return false;
    }
    final long elapsedNs = nanoTime - priorSnapshotHolder.timestamp;
    if (snapshot.cpuCount() != priorSnapshotHolder.metric.cpuCount()) {
      log.warn(
          "Prior CPU count [%d] does not match current cpu count [%d]. Skipping metrics emission",
          priorSnapshotHolder.metric.cpuCount(),
          snapshot.cpuCount()
      );
      return false;
    }
    for (int i = 0; i < snapshot.cpuCount(); ++i) {
      final ServiceMetricEvent.Builder builderUsr = builder()
          .setDimension("cpuName", Integer.toString(i))
          .setDimension("cpuTime", "usr");
      final ServiceMetricEvent.Builder builderSys = builder()
          .setDimension("cpuName", Integer.toString(i))
          .setDimension("cpuTime", "sys");
      MonitorUtils.addDimensionsToBuilder(builderUsr, dimensions);
      MonitorUtils.addDimensionsToBuilder(builderSys, dimensions);
      emitter.emit(builderUsr.build(
          dateTime,
          "cgroup/cpu_time_delta_ns",
          snapshot.usrTime(i) - priorSnapshotHolder.metric.usrTime(i)
      ));
      emitter.emit(builderSys.build(
          dateTime,
          "cgroup/cpu_time_delta_ns",
          snapshot.sysTime(i) - priorSnapshotHolder.metric.sysTime(i)
      ));
    }
    if (snapshot.cpuCount() > 0) {
      // Don't bother emitting metrics if there aren't actually any cpus (usually from error)
      emitter.emit(builder().build(dateTime, "cgroup/cpu_time_delta_ns_elapsed", elapsedNs));
    }
    return true;
  }

  static class SnapshotHolder
  {
    private final CpuAcct.CpuAcctMetric metric;
    private final long timestamp;

    SnapshotHolder(CpuAcct.CpuAcctMetric metric, long timestamp)
    {
      this.metric = metric;
      this.timestamp = timestamp;
    }
  }
}
