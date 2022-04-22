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
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryTest {
  private static final String EMPTY = "empty";

  private final Registry<String, Item> registry = Registry.create();

  @Test
  void testGetBeforeGetOrCreate() {
    assertNull(this.registry.getHolder(EMPTY));
    final Holder<Item> holder = this.registry.getOrCreateHolder(EMPTY);
    assertSame(holder, this.registry.getHolder(EMPTY)); // get should now return the (unbound) holder we created
  }

  @Test
  void testImmediate() {
    final Item item = new Item();

    final Holder<Item> holder = this.registry.register(EMPTY, item);

    assertThat(this.registry.keys()).containsExactly(EMPTY);

    assertThat(holder).isInstanceOf(Holders.Immediate.class);
    assertSame(Holder.Type.IMMEDIATE, holder.type());

    assertTrue(holder.bound());
    assertSame(item, holder.value());
    assertSame(item, assertDoesNotThrow(holder::valueOrThrow));

    final Holder<Item> holderAfterRegistration = this.registry.getHolder(EMPTY);
    assertSame(holder, holderAfterRegistration);

    this.testRegistrationOfAlreadyRegistered(holder, item);
  }

  @Test
  void testLazy() {
    final Holder<Item> holderBeforeRegistration = this.registry.getOrCreateHolder(EMPTY);

    // The registry should contain the key even when a value is not bound.
    assertThat(this.registry.keys()).containsExactly(EMPTY);

    // The returned holder should be lazy, since we have not yet registered a value.
    assertThat(holderBeforeRegistration).isInstanceOf(Holders.Lazy.class);
    assertSame(Holder.Type.LAZY, holderBeforeRegistration.type());

    // Lazy holder have no value by default.
    assertFalse(holderBeforeRegistration.bound());
    assertNull(holderBeforeRegistration.value());
    assertThrows(NoSuchElementException.class, holderBeforeRegistration::valueOrThrow);

    final Item value = new Item();

    final Holder<Item> holderAfterRegistration = this.registry.register(EMPTY, value);

    // The returned holder should still be of the lazy type, as the holder itself has not been replaced.
    assertThat(holderAfterRegistration).isInstanceOf(Holders.Lazy.class);
    assertSame(Holder.Type.LAZY, holderAfterRegistration.type());

    // The holder returned from registering should be the holder we already have, due to us
    // requesting a holder prior to registering the value.
    assertSame(holderBeforeRegistration, holderAfterRegistration);

    assertTrue(holderBeforeRegistration.bound());

    // The value has been set - both of these are now possible.
    assertSame(value, holderBeforeRegistration.value());
    assertSame(value, assertDoesNotThrow(holderBeforeRegistration::valueOrThrow));

    this.testRegistrationOfAlreadyRegistered(holderBeforeRegistration, value);
  }

  private void testRegistrationOfAlreadyRegistered(final Holder<Item> holder, final Item item) {
    // Attempting to re-register with the same value is perfectly fine...
    assertSame(holder, this.registry.register(EMPTY, item));
    // ...but with a different value is not allowed.
    assertThrows(IllegalStateException.class, () -> this.registry.register(EMPTY, new Item()));
  }

  @Test
  void testRegisterSameValueDifferentKeys() {
    final Item item = new Item();
    final Holder<Item> ha = this.registry.register("a", item);
    assertSame(item, assertDoesNotThrow(ha::valueOrThrow));
    final Holder<Item> hb = this.registry.register("b", item);
    assertSame(item, assertDoesNotThrow(hb::valueOrThrow));
    assertNotSame(ha, hb);
  }

  @Test
  void testAlreadyBoundSameValue() {
    final Object a = new Object();
    assertNull(RegistryImpl.alreadyBound("aa", a, a));
  }

  @Test
  void testAlreadyBoundDifferentValue() {
    final Object a = new Object();
    final Object b = new Object();
    assertNotNull(RegistryImpl.alreadyBound("ab", a, b));
    assertNotNull(RegistryImpl.alreadyBound("ba", b, a));
  }

  static final class Item {
    @Override
    public boolean equals(final Object that) {
      return this == that;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(this);
    }
  }
}
