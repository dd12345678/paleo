/*
 * Copyright 2016 Rahel Lüthy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.netzwerg.paleo;

import ch.netzwerg.paleo.impl.MetaDataBuilder;
import javaslang.collection.Map;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import static ch.netzwerg.paleo.ColumnIds.DoubleColumnId;

public final class DoubleColumn implements Column<DoubleColumnId> {

    private final DoubleColumnId id;
    private final double[] values;
    private final Map<String, String> metaData;

    private DoubleColumn(DoubleColumnId id, DoubleStream values, Map<String, String> metaData) {
        this.id = id;
        this.values = values.toArray();
        this.metaData = metaData;
    }

    public static DoubleColumn of(DoubleColumnId id, double value) {
        return builder(id).add(value).build();
    }

    public static DoubleColumn ofAll(DoubleColumnId id, double... values) {
        return builder(id).addAll(values).build();
    }

    public static DoubleColumn ofAll(DoubleColumnId id, DoubleStream values) {
        return builder(id).addAll(values).build();
    }

    public static Builder builder(DoubleColumnId id) {
        return new Builder(id);
    }

    @Override
    public DoubleColumnId getId() {
        return id;
    }

    @Override
    public int getRowCount() {
        return values.length;
    }

    @Override
    public Map<String, String> getMetaData() {
        return metaData;
    }

    public double getValueAt(int index) {
        return values[index];
    }

    public DoubleStream valueStream() {
        return Arrays.stream(values);
    }

    public static final class Builder implements Column.Builder<Double, DoubleColumn> {

        private final DoubleColumnId id;
        private final DoubleStream.Builder valueBuilder;
        private final MetaDataBuilder metaDataBuilder;

        private Builder(DoubleColumnId id) {
            this.id = id;
            this.valueBuilder = DoubleStream.builder();
            this.metaDataBuilder = new MetaDataBuilder();
        }

        @Override
        public Builder add(Double value) {
            valueBuilder.add(value);
            return this;
        }

        public Builder addAll(double... values) {
            return addAll(Arrays.stream(values));
        }

        public Builder addAll(DoubleStream values) {
            values.forEachOrdered(this::add);
            return this;
        }

        @Override
        public Builder putMetaData(String key, String value) {
            metaDataBuilder.putMetaData(key, value);
            return this;
        }

        @Override
        public Builder putAllMetaData(Map<String, String> metaData) {
            metaDataBuilder.putAllMetaData(metaData);
            return this;
        }

        @Override
        public DoubleColumn build() {
            return new DoubleColumn(id, valueBuilder.build(), metaDataBuilder.build());
        }

    }

}