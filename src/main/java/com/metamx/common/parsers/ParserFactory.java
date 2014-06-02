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

package com.metamx.common.parsers;

import java.util.List;

/**
 */
public interface ParserFactory
{
  /**
   * @param delimiter - delimits different columns
   * @param list_delimiter - delimits multiple values of a single data entry
   * @param header - header of column names
   * @param columns - list of columns
   * @return - an Object to parse data
   */

  public Parser makeParser(String delimiter, String list_delimiter, String header, List<String> columns);
}
