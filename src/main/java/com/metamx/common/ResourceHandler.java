/*
 * Copyright 2016 Metamarkets Group Inc.
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
 *
 */

package com.metamx.common;

/**
 * Facilitates using try-with-resources with resources that don't implement {@link AutoCloseable}, e. g.
 * {@link java.nio.ByteBuffer}s.
 *
 * <p>This interface replaces {@code io.druid.collections.ResourceHandler}.
 *
 * @param <T> the type of wrapped resource
 */
public interface ResourceHandler<T> extends AutoCloseable
{
  /**
   * Returns the wrapped resource.
   */
  T get();

  /**
   * Closes the wrapped resource.
   */
  @Override
  void close();
}
