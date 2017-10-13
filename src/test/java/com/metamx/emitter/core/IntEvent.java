package com.metamx.emitter.core;

import org.joda.time.DateTime;

import java.util.Map;

class IntEvent implements Event
{
  int index;

  IntEvent()
  {
  }

  @Override
  public Map<String, Object> toMap()
  {
    return null;
  }

  @Override
  public String getFeed()
  {
    return null;
  }

  @Override
  public DateTime getCreatedTime()
  {
    return null;
  }

  @Override
  public boolean isSafeToBuffer()
  {
    return false;
  }
}
