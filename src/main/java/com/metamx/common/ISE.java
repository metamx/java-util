package com.metamx.common;

/**
 */
public class ISE extends IllegalStateException
{
  public ISE(String formatText, Object... arguments)
  {
    super(String.format(formatText, arguments));
  }

  public ISE(Throwable cause, String formatText, Object... arguments)
  {
    super(String.format(formatText, arguments), cause);
  }
}
