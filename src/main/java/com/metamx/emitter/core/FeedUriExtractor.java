package com.metamx.emitter.core;

import java.net.URI;
import java.net.URISyntaxException;

public class FeedUriExtractor implements UriExtractor
{
  private String uriPattern;

  public FeedUriExtractor(String uriPattern)
  {
    this.uriPattern = uriPattern;
  }

  @Override
  public URI apply(Event event) throws URISyntaxException
  {
    return new URI(String.format(uriPattern, event.getFeed()));
  }

  @Override
  public String toString()
  {
    return "FeedUriExtractor{" +
           "uriPattern='" + uriPattern + '\'' +
           '}';
  }
}
