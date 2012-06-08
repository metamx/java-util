package com.metamx.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Props {

  public static Properties fromFilename(String filename) throws IOException {
    final Properties props = new Properties();
    props.load(new FileInputStream(filename));
    return props;
  }

}
