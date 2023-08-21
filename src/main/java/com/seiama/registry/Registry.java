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

import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A registry.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @since 1.0.0
 */
@NullMarked
public interface Registry<K, V> {
  /**
   * Creates a new registry.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @return a registry
   * @since 1.0.0
   */
  static <K, V> Registry<K, V> create() {
    return new RegistryImpl<>();
  }

  /**
   * Gets a holder by its key.
   *
   * <p>{@code null} will be returned if no value has been {@link #register(Object, Object) registered} for {@code key}.</p>
   *
   * @param key the key
   * @return a holder, or {@code null}
   * @throws NullPointerException if the provided key is null
   * @since 1.0.0
   */
  @SuppressWarnings("checkstyle:MethodName")
  @Nullable Holder<V> getHolder(final K key);

  /**
   * Gets a holder by its key.
   *
   * <p>{@link Optional#empty()} will be returned if no value has been {@link #register(Object, Object) registered} for {@code key}.</p>
   *
   * @param key the key
   * @return a holder, or {@link Optional#empty()}
   * @throws NullPointerException if the provided key is null
   * @since 1.0.0
   */
  @SuppressWarnings("checkstyle:MethodName")
  default Optional<Holder<V>> getHolderOptionally(final K key) {
    return Optional.ofNullable(this.getHolder(key));
  }

  /**
   * Gets a holder by its key, or creates a new holder if one does not already exist and registers it against the provided key.
   *
   * <p>The returned holder may contain a value if one has previously been registered, or it may be empty, pending future value registration.</p>
   *
   * @param key the key
   * @return a holder
   * @throws NullPointerException if the provided key is null
   * @since 1.0.0
   */
  @SuppressWarnings("checkstyle:MethodName")
  Holder<V> getOrCreateHolder(final K key);

  /**
   * Registers {@code value} to {@code key}, returning a {@link Holder}.
   *
   * @param key the key
   * @param value the value
   * @return a holder
   * @throws NullPointerException if the provided key or value are null
   * @since 1.0.0
   */
  Holder<V> register(final K key, final V value);

  /**
   * Gets the keys.
   *
   * @return the keys
   * @since 1.0.0
   */
  @UnmodifiableView
  Set<K> keys();
}
