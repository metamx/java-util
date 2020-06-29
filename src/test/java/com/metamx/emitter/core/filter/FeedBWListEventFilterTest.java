package com.metamx.emitter.core.filter;

import com.google.common.collect.ImmutableMap;
import com.metamx.emitter.core.MapEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class FeedBWListEventFilterTest
{
  @Test
  public void emptyFeedWhiteListTest()
  {
    final FeedBWListEventFilter feedEventSkipFilter = new FeedBWListEventFilter(null, null);

    Assert.assertFalse(feedEventSkipFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("feed", "F1"))));
    Assert.assertFalse(feedEventSkipFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("feed", "F2"))));

    Assert.assertFalse(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F1"))));
    Assert.assertFalse(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F2"))));
  }

  @Test
  public void emptyFeedBlackListTest()
  {
    final FeedBWListEventFilter feedEventSkipFilter = new FeedBWListEventFilter(null, null);

    Assert.assertFalse(feedEventSkipFilter.isBlackListed(new MapEvent(ImmutableMap.of("feed", "F1"))));
    Assert.assertFalse(feedEventSkipFilter.isBlackListed(new MapEvent(ImmutableMap.of("feed", "F2"))));

    Assert.assertFalse(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F1"))));
    Assert.assertFalse(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F2"))));
  }

  @Test
  public void feedWhiteListTest()
  {
    final FeedBWListEventFilter feedEventSkipFilter = new FeedBWListEventFilter(Arrays.asList("F1", "F2"), null);

    Assert.assertFalse(feedEventSkipFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("feed", "F1"))));
    Assert.assertFalse(feedEventSkipFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("feed", "F2"))));
    Assert.assertTrue(feedEventSkipFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("feed", "F3"))));

    Assert.assertFalse(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F1"))));
    Assert.assertFalse(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F2"))));
    Assert.assertTrue(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F3"))));
  }

  @Test
  public void feedBlackListTest()
  {
    final FeedBWListEventFilter feedEventSkipFilter = new FeedBWListEventFilter(null, Arrays.asList("F1", "F2"));

    Assert.assertTrue(feedEventSkipFilter.isBlackListed(new MapEvent(ImmutableMap.of("feed", "F1"))));
    Assert.assertTrue(feedEventSkipFilter.isBlackListed(new MapEvent(ImmutableMap.of("feed", "F2"))));
    Assert.assertFalse(feedEventSkipFilter.isBlackListed(new MapEvent(ImmutableMap.of("feed", "F3"))));

    Assert.assertTrue(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F1"))));
    Assert.assertTrue(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F2"))));
    Assert.assertFalse(feedEventSkipFilter.isNotListed(new MapEvent(ImmutableMap.of("feed", "F3"))));
  }
}
