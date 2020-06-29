package com.metamx.emitter.core.filter;

import com.google.common.collect.ImmutableMap;
import com.metamx.emitter.core.MapEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MultiBWListEventFilterTest
{
  @Test
  public void whiteListTest()
  {
    final MultiBWListEventFilter baseEventFilter = new MultiBWListEventFilter(
        ImmutableMap.of(
            "A", Collections.singletonList("A1"),
            "B", Arrays.asList("B1", "B2"),
            "C", new ArrayList<>()
        ),
        ImmutableMap.of()
    );
    Assert.assertFalse(baseEventFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("A", "A1"))));
    Assert.assertTrue(baseEventFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("A", "A2"))));

    Assert.assertFalse(baseEventFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("B", "B1"))));
    Assert.assertFalse(baseEventFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("B", "B2"))));
    Assert.assertTrue(baseEventFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("B", "B3"))));

    Assert.assertTrue(baseEventFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("C", "C1"))));
    Assert.assertTrue(baseEventFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("C", "C2"))));

    Assert.assertFalse(baseEventFilter.isNotWhiteListed(new MapEvent(ImmutableMap.of("D", "D1"))));
  }

  @Test
  public void blackListTest()
  {
    final MultiBWListEventFilter baseEventFilter = new MultiBWListEventFilter(
        ImmutableMap.of(),
        ImmutableMap.of(
            "A", Collections.singletonList("A1"),
            "B", Arrays.asList("B1", "B2"),
            "C", new ArrayList<>()
        )
    );
    Assert.assertTrue(baseEventFilter.isBlackListed(new MapEvent(ImmutableMap.of("A", "A1"))));
    Assert.assertFalse(baseEventFilter.isBlackListed(new MapEvent(ImmutableMap.of("A", "A2"))));

    Assert.assertTrue(baseEventFilter.isBlackListed(new MapEvent(ImmutableMap.of("B", "B1"))));
    Assert.assertTrue(baseEventFilter.isBlackListed(new MapEvent(ImmutableMap.of("B", "B2"))));
    Assert.assertFalse(baseEventFilter.isBlackListed(new MapEvent(ImmutableMap.of("B", "B3"))));

    Assert.assertFalse(baseEventFilter.isBlackListed(new MapEvent(ImmutableMap.of("C", "C1"))));
    Assert.assertFalse(baseEventFilter.isBlackListed(new MapEvent(ImmutableMap.of("C", "C2"))));

    Assert.assertFalse(baseEventFilter.isBlackListed(new MapEvent(ImmutableMap.of("D", "D1"))));
  }

  @Test
  public void shouldBeSkippedTest()
  {
    final MultiBWListEventFilter baseEventFilter = new MultiBWListEventFilter(
        ImmutableMap.of(
            "A", Collections.singletonList("A1"),
            "B", Arrays.asList("B1", "B2"),
            "C", new ArrayList<>()
        ),
        ImmutableMap.of(
            "A", Collections.singletonList("A2"),
            "B", Arrays.asList("B3", "B4"),
            "C", new ArrayList<>()
        )
    );
    Assert.assertFalse(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("A", "A1"))));
    Assert.assertTrue(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("A", "A2"))));
    Assert.assertTrue(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("A", "A3"))));

    Assert.assertFalse(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("B", "B1"))));
    Assert.assertFalse(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("B", "B2"))));
    Assert.assertTrue(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("B", "B3"))));
    Assert.assertTrue(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("B", "B4"))));
    Assert.assertTrue(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("B", "B5"))));

    Assert.assertTrue(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("C", "C1"))));
    Assert.assertTrue(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("C", "C2"))));

    Assert.assertFalse(baseEventFilter.isNotListed(new MapEvent(ImmutableMap.of("D", "D1"))));
  }

}
