package com.metamx.common;

/**
 */
public class RE extends RuntimeException
{
  public RE(String formatText, Object... arguments)
  {
    super(String.format(formatText, arguments));
  }

  public RE(Throwable cause, String formatText, Object... arguments)
  {
    super(String.format(formatText, arguments), cause);
  }
}
