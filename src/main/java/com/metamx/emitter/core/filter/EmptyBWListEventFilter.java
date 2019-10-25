package com.metamx.emitter.core.filter;

import com.metamx.emitter.core.Event;

/**
 * The {@link BWListEventFilter} with no white and black lists.
 */
public class EmptyBWListEventFilter implements BWListEventFilter
{
  @Override
  public boolean isNotWhiteListed(Event event)
  {
    return false;
  }

  @Override
  public boolean isBlackListed(Event event)
  {
    return false;
  }
}
