package com.metamx.emitter.core.filter;

import com.metamx.emitter.core.Event;

/**
 * Filters out events based on white and black lists of event field values.
 */
public interface BWListEventFilter
{
  /**
   * Checks if an event is not whitelisted.
   * There are three possible cases: whitelisted, not whitelisted, the white list is not defined.
   * "Is not whitelisted" is opposite to the other two options so it's used as a predicate to decide
   * if the event should be filtered out.
   */
  boolean isNotWhiteListed(Event event);

  /**
   * Checks if an event is blacklisted.
   * There are three possible cases: blacklisted, not blacklisted, the black list is not defined.
   * "Is blacklisted" is opposite to the other two options so it's used as a predicate to decide
   * if the event should be filtered out.
   */
  boolean isBlackListed(Event event);

  /**
   * Checks if an event is not whitelisted or is blacklisted.
   */
  default boolean isNotListed(Event event)
  {
    return isNotWhiteListed(event) || isBlackListed(event);
  }
}
