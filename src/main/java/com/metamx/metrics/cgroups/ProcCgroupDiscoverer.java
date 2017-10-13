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

package com.metamx.metrics.cgroups;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.metamx.common.RE;
import com.metamx.metrics.CgroupUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ProcCgroupDiscoverer implements CgroupDiscoverer
{
  private static final String CGROUP_TYPE = "cgroup";
  private static final String PROC_TYPE = "getProc";

  @Override
  public Path discover(final String cgroup, long pid)
  {
    Preconditions.checkNotNull(cgroup, "cgroup required");
    // TODO: find a way to cache these
    final File proc = getProc();
    final File procMounts = new File(proc, "mounts");
    final File procCgroups = new File(proc, "cgroups");
    final File pidCgroups = new File(new File(proc, Long.toString(pid)), "cgroup");
    final ProcCgroupsEntry procCgroupsEntry = getCgroupEntry(procCgroups, cgroup);
    final ProcMountsEntry procMountsEntry = getMountEntry(procMounts, cgroup);
    final ProcPidCgroupEntry procPidCgroupEntry = getPidCgroupEntry(pidCgroups, procCgroupsEntry.hierarchy);
    final File cgroupDir = new File(
        procMountsEntry.path.toFile(),
        procPidCgroupEntry.path
    );
    if (cgroupDir.exists() && cgroupDir.isDirectory()) {
      return cgroupDir.toPath();
    }
    throw new RE("Invalid cgroup directory [%s]", cgroupDir);
  }

  @VisibleForTesting
  public File getProc()
  {
    // TODO: discover `/proc` in a more reliable way
    final File proc = new File("/proc");
    Path foundProc = null;
    if (proc.exists() && proc.isDirectory()) {
      // Sanity check
      try {
        for (final String line : Files.readLines(new File(proc, "mounts"), Charsets.UTF_8)) {
          final ProcMountsEntry entry = ProcMountsEntry.parse(line);
          if (PROC_TYPE.equals(entry.type)) {
            if (proc.toPath().equals(entry.path)) {
              return proc;
            } else {
              foundProc = entry.path;
            }
          }
        }
      }
      catch (IOException e) {
        // Unlikely
        throw new RuntimeException(e);
      }
      if (foundProc != null) {
        throw new RE("Expected proc to be mounted on /proc, but was on [%s]", foundProc);
      } else {
        throw new RE("No proc entry found in /proc/mounts");
      }
    } else {
      throw new RE("/proc is not a valid directory");
    }
  }

  private ProcPidCgroupEntry getPidCgroupEntry(final File pidCgroups, final int hierarchy)
  {
    final List<String> lines;
    try {
      lines = Files.readLines(pidCgroups, Charsets.UTF_8);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (final String line : lines) {
      final ProcPidCgroupEntry entry = ProcPidCgroupEntry.parse(line);
      if (hierarchy == entry.hierarchy) {
        return entry;
      }
    }
    throw new RE("No hierarchy found for [%d]", hierarchy);
  }

  private ProcCgroupsEntry getCgroupEntry(final File procCgroups, final String cgroup)
  {
    final List<String> lines;
    try {
      lines = Files.readLines(procCgroups, Charsets.UTF_8);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (final String line : lines) {
      if (line.startsWith("#")) {
        continue;
      }
      final ProcCgroupsEntry entry = ProcCgroupsEntry.parse(line);
      if (entry.enabled && cgroup.equals(entry.subsystem_name)) {
        return entry;
      }
    }
    throw new RE("Hierarchy for [%s] not found", cgroup);
  }

  private ProcMountsEntry getMountEntry(final File procMounts, final String cgroup)
  {
    final List<String> lines;
    try {
      lines = Files.readLines(procMounts, Charsets.UTF_8);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    for (final String line : lines) {
      final ProcMountsEntry entry = ProcMountsEntry.parse(line);
      if (CGROUP_TYPE.equals(entry.type) && entry.options.contains(cgroup)) {
        return entry;
      }
    }
    throw new RE("Cgroup [%s] not found", cgroup);
  }

  /**
   * Doesn't use the last two mount entries for priority/boot stuff
   */
  static class ProcMountsEntry
  {
    // Example: cgroup /sys/fs/cgroup/cpu,cpuacct cgroup rw,nosuid,nodev,noexec,relatime,cpu,cpuacct 0 0
    static ProcMountsEntry parse(String entry)
    {
      final String[] splits = entry.split(CgroupUtil.SPACE_MATCH, 6);
      Preconditions.checkArgument(splits.length == 6, "Invalid entry: [%s]", entry);
      return new ProcMountsEntry(
          splits[0],
          Paths.get(splits[1]),
          splits[2],
          ImmutableSet.copyOf(splits[3].split(CgroupUtil.COMMA_MATCH))
      );
    }

    final String dev;
    final Path path;
    final String type;
    final Set<String> options;

    ProcMountsEntry(String dev, Path path, String type, Collection<String> options)
    {
      this.dev = dev;
      this.path = path;
      this.type = type;
      this.options = ImmutableSet.copyOf(options);
    }
  }

  static class ProcCgroupsEntry
  {
    // Example Header: #subsys_name	hierarchy	num_cgroups	enabled
    static ProcCgroupsEntry parse(String entry)
    {
      final String[] splits = entry.split(Pattern.quote("\t"));
      return new ProcCgroupsEntry(
          splits[0],
          Integer.parseInt(splits[1]),
          Integer.parseInt(splits[2]),
          Integer.parseInt(splits[3]) == 1
      );
    }

    final String subsystem_name;
    final int hierarchy;
    final int num_cgroups;
    final boolean enabled;

    ProcCgroupsEntry(String subsystem_name, int hierarchy, int num_cgroups, boolean enabled)
    {
      this.subsystem_name = subsystem_name;
      this.hierarchy = hierarchy;
      this.num_cgroups = num_cgroups;
      this.enabled = enabled;
    }
  }

  static class ProcPidCgroupEntry
  {
    // example: 3:cpu,cpuacct:/system.slice/mesos-agent-spark.service/673550f3-69b9-4ef0-910d-762c8aaeda1c
    static ProcPidCgroupEntry parse(String entry)
    {
      final String[] splits = entry.split(CgroupUtil.COLON_MATCH, 3);
      Preconditions.checkArgument(splits.length == 3, "Invalid entry [%s]", entry);
      return new ProcPidCgroupEntry(Integer.parseInt(splits[0]), splits[1], splits[2]);
    }

    private final int hierarchy;
    private final String entrypoint;
    private final String path;

    ProcPidCgroupEntry(int hierarchy, String entrypoint, String path)
    {
      this.hierarchy = hierarchy;
      this.entrypoint = entrypoint;
      this.path = path;
    }
  }
}
