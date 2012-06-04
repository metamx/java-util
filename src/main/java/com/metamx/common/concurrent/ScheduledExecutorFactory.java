package com.metamx.common.concurrent;

import java.util.concurrent.ScheduledExecutorService;

public interface ScheduledExecutorFactory
{
  public ScheduledExecutorService create(int corePoolSize, String nameFormat);
}
