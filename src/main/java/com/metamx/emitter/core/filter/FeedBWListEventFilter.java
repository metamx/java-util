package com.metamx.emitter.core.filter;

import com.metamx.emitter.core.Event;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The {@link BWListEventFilter} that has only BW lists for a {@link Event}'s feed field.
 */
public class FeedBWListEventFilter implements BWListEventFilter
{
  private final Set<String> whiteSet;
  private final Set<String> blackSet;

  public FeedBWListEventFilter(@Nullable List<String> whiteList, @Nullable List<String> blackList)
  {
    this.whiteSet = whiteList == null ? null : new HashSet<>(whiteList);
    this.blackSet = blackList == null ? null : new HashSet<>(blackList);
  }

  @Override
  public boolean isNotWhiteListed(Event event)
  {
    return !(whiteSet == null || whiteSet.contains(event.getFeed()));
  }

  @Override
  public boolean isBlackListed(Event event)
  {
    return blackSet != null && blackSet.contains(event.getFeed());
  }
}
