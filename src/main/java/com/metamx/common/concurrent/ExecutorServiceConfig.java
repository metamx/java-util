package com.metamx.common.concurrent;

import org.skife.config.Config;

/**
 */
public abstract class ExecutorServiceConfig
{
  @Config(value="${base_path}.formatString")
  public abstract String getFormatString();

  @Config(value="${base_path}.numThreads")
  public abstract int getNumThreads();
}
