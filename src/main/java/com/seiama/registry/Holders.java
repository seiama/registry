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

import java.util.StringJoiner;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class Holders {
  private Holders() {
  }

  record Immediate<K, V>(
    K key,
    V value
  ) implements Holder<V> {
    @Override
    public boolean bound() {
      return true; // An immediate holder always has a value associated with it.
    }

    @Override
    public V valueOrThrow() {
      return this.value;
    }

    @Override
    public Type type() {
      return Type.IMMEDIATE;
    }
  }

  static final class Lazy<K, V> implements Holder<V> {
    private final K key;
    private @Nullable V value;

    Lazy(final K key) {
      this.key = key;
    }

    @Nullable V bind(final V value) {
      if (this.value == null) {
        this.value = value;
        return null;
      } else {
        return this.value;
      }
    }

    @Override
    public boolean bound() {
      return this.value != null;
    }

    @Override
    public @Nullable V value() {
      return this.value;
    }

    @Override
    public Type type() {
      return Type.LAZY;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("key=" + this.key)
        .add("value=" + this.value)
        .toString();
    }
  }
}
