package com.metamx.common.config;

import org.skife.config.ConfigurationObjectFactory;

import java.util.Properties;

/**
*/
public class Config
{
  public static ConfigurationObjectFactory createFactory(Properties props)
  {
    ConfigurationObjectFactory configFactory = new ConfigurationObjectFactory(props);
    configFactory.addCoercible(new DurationCoercible());
    return configFactory;
  }
}
