/*
 * Copyright 2018 Qunar, Inc.
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

package com.z.mq.metrics.impl;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.z.mq.metrics.MqCounter;
import com.z.mq.metrics.MqMeter;
import com.z.mq.metrics.MqMetricRegistry;
import com.z.mq.metrics.MqTimer;
import io.prometheus.client.*;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author keli.wang
 * @since 2018/11/22
 */
public class PrometheusMqMetricRegistry implements MqMetricRegistry {

    private static final LoadingCache<Key, Collector> CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<Key, Collector>() {
                @Override
                public Collector load(Key key) {
                    return key.create();
                }
            });

    public PrometheusMqMetricRegistry() {
        try {
            new HTTPServer(3333);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static <M extends Collector> M cacheFor(Key<M> key) {
        return (M) CACHE.getUnchecked(key);
    }

    @Override
    public void newGauge(final String name, final String[] tags, final String[] values, final Supplier<Double> supplier) {
        final PrometheusQmqGauge gauge = cacheFor(new GuageKey(name, tags));
        gauge.labels(values).setSupplier(supplier);
    }

    @Override
    public MqCounter newCounter(final String name, final String[] tags, final String[] values) {
        final Gauge gauge = cacheFor(new CounterKey(name, tags));
        return new PrometheusMqCounter(gauge, values);
    }

    @Override
    public MqMeter newMeter(final String name, final String[] tags, final String[] values) {
        final Summary summary = cacheFor(new MeterKey(name, tags));
        return new PrometheusMqMeter(summary, values);
    }

    @Override
    public MqTimer newTimer(final String name, final String[] tags, final String[] values) {
        final Summary summary = cacheFor(new TimerKey(name, tags));
        return new PrometheusMqTimer(summary, values);
    }

    @Override
    public void remove(final String name, final String[] tags, final String[] values) {
        final Collector collector = CACHE.getIfPresent(new SimpleCollectorKey(name, tags));
        if (collector == null) return;
        CollectorRegistry.defaultRegistry.unregister(collector);
    }

    private static abstract class Key<M extends Collector> {
        final String name;
        final String[] tags;

        Key(String name, String[] tags) {
            this.name = name;
            this.tags = tags;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Key key = (Key) o;

            if (name != null ? !name.equals(key.name) : key.name != null)
                return false;
            return Arrays.equals(tags, key.tags);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (tags != null ? Arrays.hashCode(tags) : 0);
            return result;
        }

        public abstract M create();
    }

    private static class SimpleCollectorKey extends Key<SimpleCollector> {

        SimpleCollectorKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public SimpleCollector create() {
            return null;
        }
    }

    private static class GuageKey extends Key<PrometheusQmqGauge> {
        GuageKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public PrometheusQmqGauge create() {
            return PrometheusQmqGauge.build().name(name).help(name).labelNames(tags).create().register();
        }
    }

    private static class CounterKey extends Key<Gauge> {
        CounterKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public Gauge create() {
            return Gauge.build().name(name).help(name).labelNames(tags).create().register();
        }
    }

    private static class MeterKey extends Key<Summary> {

        MeterKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public Summary create() {
            return Summary.build().name(name).help(name).labelNames(tags).create().register();
        }
    }

    private static class TimerKey extends Key<Summary> {

        TimerKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public Summary create() {
            return Summary.build()
                    .name(name)
                    .help(name)
                    .labelNames(tags)
                    .quantile(0.5, 0.05)
                    .quantile(0.75, 0.05)
                    .quantile(0.99, 0.05)
                    .create()
                    .register();
        }
    }
}
