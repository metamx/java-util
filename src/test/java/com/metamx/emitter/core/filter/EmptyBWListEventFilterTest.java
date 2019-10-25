package com.metamx.emitter.core.filter;

import com.google.common.collect.ImmutableMap;
import com.metamx.emitter.core.MapEvent;
import org.junit.Assert;
import org.junit.Test;

public class EmptyBWListEventFilterTest
{
  @Test
  public void isListed()
  {
    final EmptyBWListEventFilter eventFilter = new EmptyBWListEventFilter();
    Assert.assertFalse(eventFilter.isNotListed(new MapEvent(ImmutableMap.of())));
    Assert.assertFalse(eventFilter.isNotListed(new MapEvent(ImmutableMap.of("A", "A1"))));
  }
}