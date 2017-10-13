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

import com.google.common.collect.ImmutableSet;
import com.metamx.common.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class ProcCgroupDiscovererTest
{
  private static final int PID = 384;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private File procDir;
  private File cgroupDir;
  private CgroupDiscoverer discoverer;

  @Before
  public void setUp() throws Exception
  {
    cgroupDir = temporaryFolder.newFolder();
    procDir = temporaryFolder.newFolder();
    discoverer = new ProcCgroupDiscoverer()
    {
      @Override
      public File getProc()
      {
        return procDir;
      }
    };
    TestUtils.setUpCgroups(procDir, cgroupDir, PID);
  }

  @Test
  public void testSimpleProc() throws Exception
  {
    Assert.assertEquals(
        new File(
            cgroupDir,
            "cpu,cpuacct/system.slice/mesos-agent-druid.service/f12ba7e0-fa16-462e-bb9d-652ccc27f0ee"
        ).toPath(),
        discoverer.discover("cpu", PID)
    );
  }

  @Test
  public void testParse() throws Exception
  {
    final ProcCgroupDiscoverer.ProcMountsEntry entry = ProcCgroupDiscoverer.ProcMountsEntry.parse(
        "/dev/md126 /ebs xfs rw,seclabel,noatime,attr2,inode64,sunit=1024,swidth=16384,noquota 0 0"
    );
    Assert.assertEquals("/dev/md126", entry.dev);
    Assert.assertEquals(Paths.get("/ebs"), entry.path);
    Assert.assertEquals("xfs", entry.type);
    Assert.assertEquals(ImmutableSet.of(
        "rw",
        "seclabel",
        "noatime",
        "attr2",
        "inode64",
        "sunit=1024",
        "swidth=16384",
        "noquota"
    ), entry.options);
  }

  @Test
  public void testNullCgroup()
  {
    expectedException.expect(NullPointerException.class);
    Assert.assertNull(new ProcCgroupDiscoverer().discover(null, 0));
  }
}
