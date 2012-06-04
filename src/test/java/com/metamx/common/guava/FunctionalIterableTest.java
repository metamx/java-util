package com.metamx.common.guava;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 */
public class FunctionalIterableTest
{
  @Test
  public void testTransform() throws Exception
  {
    Assert.assertEquals(
        Lists.<Integer>newArrayList(
            FunctionalIterable.create(Arrays.asList("1", "2", "3"))
                              .transform(
                                  new Function<String, Integer>()
                                  {
                                    @Override
                                    public Integer apply(String input)
                                    {
                                      return Integer.parseInt(input);
                                    }
                                  }
                              )
        ),
        Arrays.asList(1, 2, 3)
    );
  }

  @Test
  public void testTransformCat() throws Exception
  {
    Assert.assertEquals(
        Lists.<String>newArrayList(
            FunctionalIterable.create(Arrays.asList("1,2", "3,4", "5,6"))
                              .transformCat(
                                  new Function<String, Iterable<String>>()
                                  {
                                    @Override
                                    public Iterable<String> apply(String input)
                                    {
                                      return Splitter.on(",").split(input);
                                    }
                                  }
                              )
        ),
        Arrays.asList("1", "2", "3", "4", "5", "6")
    );
  }

  @Test
  public void testKeep() throws Exception
  {
    Assert.assertEquals(
        Lists.<Integer>newArrayList(
            FunctionalIterable.create(Arrays.asList("1", "2", "3"))
                              .keep(
                                  new Function<String, Integer>()
                                  {
                                    @Override
                                    public Integer apply(String input)
                                    {
                                      if ("2".equals(input)) {
                                        return null;
                                      }
                                      return Integer.parseInt(input);
                                    }
                                  }
                              )
        ),
        Arrays.asList(1, 3)
    );
  }

  @Test
  public void testFilter() throws Exception
  {
    Assert.assertEquals(
        Lists.<String>newArrayList(
            FunctionalIterable.create(Arrays.asList("1", "2", "3"))
                              .filter(
                                  new Predicate<String>()
                                  {
                                    @Override
                                    public boolean apply(String input)
                                    {
                                      return !"2".equals(input);
                                    }
                                  }
                              )
        ),
        Arrays.asList("1", "3")
    );
  }

  @Test
  public void testDrop() throws Exception
  {
    Assert.assertEquals(
        Lists.<String>newArrayList(
            FunctionalIterable.create(Arrays.asList("1", "2", "3"))
                              .drop(2)
        ),
        Arrays.asList("3")
    );
  }
}
