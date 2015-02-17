package com.metamx.common;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MapUtilsTest
{
  @Test
  public void testURI() throws MalformedURLException
  {
    final String uri = "file:/tmp/foo";
    final Map<String, Object> map = new HashMap<>(
        ImmutableMap.<String, Object>of(
            "k1", URI.create(uri),
            "k2", uri,
            "k3", new URL(uri)
        )
    );
    map.put("k4", null);

    final URI differentURI = URI.create("file:/tmp2/foo2");

    Assert.assertEquals(uri, MapUtils.getURI(map, "k1").toString());
    Assert.assertEquals(uri, MapUtils.getURI(map, "k2").toString());
    Assert.assertEquals(uri, MapUtils.getURI(map, "k3").toString());

    Assert.assertEquals(uri, MapUtils.getURI(map, "k1", differentURI).toString());
    Assert.assertEquals(uri, MapUtils.getURI(map, "k2", differentURI).toString());
    Assert.assertEquals(uri, MapUtils.getURI(map, "k3", differentURI).toString());

    Assert.assertEquals(differentURI, MapUtils.getURI(map, "k4", differentURI));

    Assert.assertEquals(uri, MapUtils.getURI(map, "k5", URI.create(uri)).toString());
  }
  @Test(expected = IAE.class)
  public void testBadURI(){
    MapUtils.getURI(ImmutableMap.<String, Object>of(), "k5");
  }
}
