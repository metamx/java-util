package com.metamx.emitter.core.filter;

import com.metamx.emitter.core.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link BWListEventFilter} that supports BW lists for any field.
 * Before checking an event the filter converts the event to a map and iterates over the map keys.
 */
public class MultiBWListEventFilter implements BWListEventFilter
{
  private final Map<String, Set<String>> fieldToValueWhiteSet;
  private final Map<String, Set<String>> fieldToValueBlackSet;

  public MultiBWListEventFilter(
      Map<String, List<String>> fieldToValueWhiteList,
      Map<String, List<String>> fieldToValueBlackList
  )
  {
    this.fieldToValueWhiteSet = fieldToValueWhiteList.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> new HashSet<>(e.getValue())
    ));
    this.fieldToValueBlackSet = fieldToValueBlackList.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> new HashSet<>(e.getValue())
    ));
  }

  @Override
  public boolean isNotWhiteListed(Event event)
  {
    final Map<String, Object> eventMap = event.toMap();
    return fieldToValueWhiteSet.entrySet().stream().anyMatch(
        e -> eventMap.containsKey(e.getKey()) && !e.getValue().contains(eventMap.get(e.getKey()))
    );
  }

  @Override
  public boolean isBlackListed(Event event)
  {
    final Map<String, Object> eventMap = event.toMap();
    return fieldToValueBlackSet.entrySet().stream().anyMatch(
        e -> eventMap.containsKey(e.getKey()) && e.getValue().contains(eventMap.get(e.getKey()))
    );
  }
}
