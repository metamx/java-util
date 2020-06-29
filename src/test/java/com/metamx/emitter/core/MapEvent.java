package com.metamx.emitter.core;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.joda.time.DateTime;

import java.util.Map;

@JsonSerialize(converter = MapEvent.MapEventConverter.class)
public final class MapEvent implements Event
{
  private final Map<String, Object> map;

  public MapEvent(Map<String, Object> map)
  {
    this.map = map;
  }

  @Override
  public Map<String, Object> toMap()
  {
    return map;
  }

  @Override
  public String getFeed()
  {
    return (String) map.get("feed");
  }

  @Override
  public DateTime getCreatedTime()
  {
    return (DateTime) map.get("createdTime");
  }

  @Override
  public boolean isSafeToBuffer()
  {
    return map.containsKey("isSafeToBuffer");
  }

  public static class MapEventConverter extends StdConverter<MapEvent, Map<String, Object>>
  {
    @Override
    public Map<String, Object> convert(MapEvent value)
    {
      return value.toMap();
    }
  }
}
