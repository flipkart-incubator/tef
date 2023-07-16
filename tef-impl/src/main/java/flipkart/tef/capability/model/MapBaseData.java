/*
 * Copyright [2023] [The Original Author]
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

/**
 * 
 * Date: 06/06/20
 * Time: 3:07 PM
 */
package flipkart.tef.capability.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class MapBaseData<K, V> implements Map<K, V> {

    private final Map<K, V> underlying;

    public MapBaseData() {
        this.underlying = new HashMap<>();
    }

    @Override
    public int size() {
        return underlying.size();
    }

    @Override
    public boolean isEmpty() {
        return underlying.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return underlying.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return underlying.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return underlying.get(key);
    }

    @Override
    public V put(K key, V value) {
        return underlying.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return underlying.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        underlying.putAll(m);
    }

    @Override
    public void clear() {
        underlying.clear();
    }

    @Override
    public Set<K> keySet() {
        return underlying.keySet();
    }

    @Override
    public Collection<V> values() {
        return underlying.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return underlying.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return underlying.equals(o);
    }

    @Override
    public int hashCode() {
        return underlying.hashCode();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return underlying.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        underlying.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        underlying.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return underlying.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return underlying.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return underlying.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return underlying.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return underlying.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return underlying.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return underlying.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return underlying.merge(key, value, remappingFunction);
    }
}
