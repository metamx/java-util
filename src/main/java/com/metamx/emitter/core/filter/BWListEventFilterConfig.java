package com.metamx.emitter.core.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import com.metamx.emitter.core.JacksonUtil;

import java.util.List;
import java.util.Map;

/**
 * A config for {@link BWListEventFilter}.
 */
public class BWListEventFilterConfig
{
  @JsonProperty
  @JsonDeserialize(contentUsing = JacksonUtil.CommaDelimitedListDeserializer.class)
  private Map<String, List<String>> whiteList = ImmutableMap.of();

  @JsonProperty
  @JsonDeserialize(contentUsing = JacksonUtil.CommaDelimitedListDeserializer.class)
  private Map<String, List<String>> blackList = ImmutableMap.of();

  public Map<String, List<String>> getWhiteList()
  {
    return whiteList;
  }

  public Map<String, List<String>> getBlackList()
  {
    return blackList;
  }

  @Override
  public String toString()
  {
    return "BWListEventFilterConfig{" +
           "whiteList='" + whiteList + '\'' +
           ", blackList='" + blackList + '\'' +
           '}';
  }

}
