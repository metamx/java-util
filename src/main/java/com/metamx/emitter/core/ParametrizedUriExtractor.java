package com.metamx.emitter.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametrizedUriExtractor implements UriExtractor
{
  private String uriPattern;
  private Set<String> params;

  public ParametrizedUriExtractor(String uriPattern)
  {
    this.uriPattern = uriPattern;
    Matcher keyMatcher = Pattern.compile("\\{([^\\}]+)\\}").matcher(uriPattern);
    params = new HashSet<>();
    while (keyMatcher.find()) {
      params.add(keyMatcher.group(1));
    }
  }

  public Set<String> getParams()
  {
    return params;
  }

  @Override
  public URI apply(Event event) throws URISyntaxException
  {
    Map<String, Object> eventMap = event.toMap();
    String processedUri = uriPattern;
    for (String key : params) {
      Object paramValue = eventMap.get(key);
      if (paramValue == null) {
        throw new IllegalArgumentException(String.format(
            "ParametrizedUriExtractor with pattern %s requires %s to be set in event, but found %s",
            uriPattern,
            key,
            eventMap
        ));
      }
      processedUri = processedUri.replace(String.format("{%s}", key), paramValue.toString());
    }
    return new URI(processedUri);
  }

  @Override
  public String toString()
  {
    return "ParametrizedUriExtractor{" +
           "uriPattern='" + uriPattern + '\'' +
           ", params=" + params +
           '}';
  }
}
