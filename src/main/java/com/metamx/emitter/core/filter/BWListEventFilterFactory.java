package com.metamx.emitter.core.filter;

import java.util.HashSet;

/**
 * The factory creates {@link BWListEventFilter} out of the {@link BWListEventFilterConfig}.
 */
public class BWListEventFilterFactory
{
  private static final String FEED_FIELD_NAME = "feed";

  public static BWListEventFilter makeEventFilter(BWListEventFilterConfig config)
  {
    final HashSet<String> commonKeys = new HashSet<>(config.getWhiteList().keySet());
    commonKeys.addAll(config.getBlackList().keySet());
    if (commonKeys.size() == 0) {
      return new EmptyBWListEventFilter();
    }
    if (commonKeys.size() == 1 && commonKeys.contains(FEED_FIELD_NAME)) {
      return new FeedBWListEventFilter(
          config.getWhiteList().get(FEED_FIELD_NAME),
          config.getBlackList().get(FEED_FIELD_NAME)
      );
    } else {
      return new MultiBWListEventFilter(config.getWhiteList(), config.getBlackList());
    }
  }

}
