/*
 * Copyright 2011,2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.exception;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 */
public class FormattedException extends RuntimeException
{
  public static enum ErrorCode
  {
    FILE_NOT_FOUND,
    BAD_HEADER,
    UNPARSABLE_ROW,
    MISSING_TIMESTAMP,
    UNPARSABLE_TIMESTAMP,
    MISSING_METRIC,
    UNPARSABLE_METRIC,
    SERVER_ERROR,
    AUTHENTICATION_ERROR
  }

  public static class Builder
  {
    private ErrorCode errorCode;
    private Map<String, Object> details;
    private String message;

    public Builder() {}

    public Builder withErrorCode(ErrorCode errorCode)
    {
      this.errorCode = errorCode;
      return this;
    }

    public Builder withDetails(Map<String, Object> details)
    {
      this.details = details;
      return this;
    }

    public Builder withMessage(String message)
    {
      this.message = message;
      return this;
    }

    public FormattedException build()
    {
      return new FormattedException(errorCode, details, message);
    }
  }

  private final ErrorCode errorCode;
  private final Map<String, Object> details;
  private final String message;

  private FormattedException(ErrorCode errorCode, Map<String, Object> details, String message)
  {
    this.errorCode = errorCode;
    this.details = details;
    this.message = message;
  }

  @JsonProperty
  public ErrorCode getErrorCode()
  {
    return errorCode;
  }

  @JsonProperty
  public Map<String, Object> getDetails()
  {
    return details;
  }

  @JsonProperty
  public String getMessage()
  {
    return message;
  }
}
