package com.metamx.common.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metamx.common.lifecycle.Lifecycle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServices
{
  public static ExecutorService create(Lifecycle lifecycle, ExecutorServiceConfig config)
  {
    return manageLifecycle(
        lifecycle,
        Executors.newFixedThreadPool(
            config.getNumThreads(),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat(config.getFormatString()).build()
        )
    );
  }

  public static <T extends ExecutorService> T manageLifecycle(Lifecycle lifecycle, final T service)
  {
    lifecycle.addHandler(
        new Lifecycle.Handler()
        {
          @Override
          public void start() throws Exception
          {
          }

          @Override
          public void stop()
          {
            service.shutdownNow();
          }
        }
    );

    return service;
  }
}
