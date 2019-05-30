/*
 * Copyright 2011 - 2015 Metamarkets Group Inc.
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

package com.metamx.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class Props {

  public static Properties fromFilename(String filename) throws IOException {
    final Properties props = new Properties();
    props.load(new FileInputStream(filename));
    return props;
  }

  public static Properties fromEnv(String prefix) {
    final Properties props = new Properties();
    for (Map.Entry<String, String> env: System.getenv().entrySet()) {
      if (env.getKey().startsWith(prefix)) {
        props.put(env.getKey().toLowerCase().replaceAll("_", "."), env.getValue());
      }
    }
    return props;
  }

}
