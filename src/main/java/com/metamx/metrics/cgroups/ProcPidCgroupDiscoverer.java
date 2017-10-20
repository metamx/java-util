package com.metamx.metrics.cgroups;

import com.metamx.metrics.PidDiscoverer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcPidCgroupDiscoverer implements CgroupDiscoverer
{
  private final ProcCgroupDiscoverer delegate;

  //    final File pidCgroups = new File(new File(proc, Long.toString(pid)), "cgroup")
  public ProcPidCgroupDiscoverer(PidDiscoverer pidDiscoverer)
  {
    delegate = new ProcCgroupDiscoverer(Paths.get("/proc", Long.toString(pidDiscoverer.getPid())));
  }

  @Override
  public Path discover(String cgroup)
  {
    return delegate.discover(cgroup);
  }
}
