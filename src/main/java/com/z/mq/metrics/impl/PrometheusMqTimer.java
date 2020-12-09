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

import com.z.mq.metrics.MqTimer;
import io.prometheus.client.Summary;

import java.util.concurrent.TimeUnit;

public class PrometheusMqTimer implements MqTimer {
    private final Summary.Child summary;

    public PrometheusMqTimer(final Summary summary, final String[] labels) {
        this.summary = summary.labels(labels);
    }

    @Override
    public void update(final long duration, final TimeUnit unit) {
        summary.observe(unit.toMillis(duration));
    }
}
