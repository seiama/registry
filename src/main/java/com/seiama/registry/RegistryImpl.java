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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.VisibleForTesting;

import static java.util.Objects.requireNonNull;

final class RegistryImpl<K, V> implements Registry<K, V> {
  private final Map<K, Holder<V>> keyToHolder = new HashMap<>();
  private @Nullable Set<K> keys;

  @Override
  public @Nullable Holder<V> getHolder(final @NotNull K key) {
    return this.getHolder0(key, false);
  }

  @Override
  public @NotNull Holder<V> getOrCreateHolder(final @NotNull K key) {
    return this.getHolder0(key, true);
  }

  @SuppressWarnings("checkstyle:MethodName")
  private @UnknownNullability Holder<V> getHolder0(final @NotNull K key, final boolean create) {
    requireNonNull(key, "key");

    @Nullable Holder<V> holder = this.keyToHolder.get(key);

    if (holder == null) {
      // Only create a new holder when requested
      if (create) {
        // No value has been registered for the given key yet - creating a lazy holder here
        // allows us to provide a way to access the value once it has been registered later on.
        holder = new Holders.Lazy<>(key);
        this.keyToHolder.put(key, holder);
      }
    }

    return holder;
  }

  @Override
  public @NotNull Holder<V> register(final @NotNull K key, final @NotNull V value) {
    requireNonNull(key, "key");
    requireNonNull(value, "value");

    @Nullable Holder<V> holder = this.keyToHolder.get(key);

    if (holder == null) {
      // No holder was previously requested prior to registration.
      holder = new Holders.Immediate<>(key, value);
      this.keyToHolder.put(key, holder);
    } else {
      @Nullable V oldValue = null;

      // We can't pass "K" to these casts, they are incompatible
      if (holder instanceof final Holders.Immediate<?, V> immediate) {
        oldValue = immediate.value();
      } else if (holder instanceof final Holders.Lazy<?, V> lazy) {
        // A holder was requested for this key prior to the actual
        // registration of a value - let's attempt to bind the value to the holder
        oldValue = lazy.bind(value);
      }

      if (oldValue != null) {
        final @Nullable IllegalStateException alreadyBound = alreadyBound(key, oldValue, value);
        if (alreadyBound != null) {
          // A holder already exists with a different value.
          throw alreadyBound;
        }
      }
    }

    return holder;
  }

  @Override
  public @NotNull Set<K> keys() {
    if (this.keys == null) {
      this.keys = Collections.unmodifiableSet(this.keyToHolder.keySet());
    }
    return this.keys;
  }

  @VisibleForTesting
  static <K, V> @Nullable IllegalStateException alreadyBound(final K key, final V oldValue, final V newValue) {
    if (oldValue != newValue) {
      return new IllegalStateException(key + " is already bound to " + oldValue + ", cannot bind to " + newValue);
    }
    return null;
  }
}
