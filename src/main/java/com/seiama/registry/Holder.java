/*
 * This file is part of registry, licensed under the MIT License.
 *
 * Copyright (c) 2021-2023 Seiama
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.seiama.registry;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A holder of a value.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @since 1.0.0
 */
@NullMarked
public sealed interface Holder<K, V> permits Holders.Immediate, Holders.Lazy {
  /**
   * Gets the key.
   *
   * @return the key
   * @since 1.0.0
   */
  K key();

  /**
   * Checks if this holder has a value associated.
   *
   * @return {@code true} if this holder has a value associated, {@code false} otherwise
   * @since 1.0.0
   */
  boolean bound();

  /**
   * Gets the value.
   *
   * @return the value, or {@code null}
   * @since 1.0.0
   */
  @Nullable V value();

  /**
   * Gets the value wrapped in an {@link Optional}.
   *
   * @return the value wrapped in an {@link Optional}
   * @since 1.0.0
   */
  default Optional<V> valueOptionally() {
    return Optional.ofNullable(this.value());
  }

  /**
   * Gets the value, or throws {@link NoSuchElementException}.
   *
   * @return the value
   * @throws NoSuchElementException if no value is bound
   * @since 1.0.0
   */
  default V valueOrThrow() throws NoSuchElementException {
    final @Nullable V value = this.value();
    if (value == null) {
      throw new NoSuchElementException("A value has not been defined for " + this);
    }
    return value;
  }

  /**
   * Gets the type.
   *
   * @return the type
   * @since 1.0.0
   */
  Type type();

  /**
   * The type of holder.
   *
   * @since 1.0.0
   */
  enum Type {
    /**
     * An immediate holder has a value available at time of creation.
     *
     * @since 1.0.0
     */
    IMMEDIATE,
    /**
     * A lazy holder has a value of {@code null} at time of creation, with the actual value being assigned at a later time.
     *
     * <p>This type of holder is useful where a forward-reference of a value may be required.</p>
     *
     * @since 1.0.0
     */
    LAZY;
  }
}
