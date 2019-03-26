/**
 * Copyright (c) 2018-2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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
package org.eclipse.microprofile.reactive.messaging.tck.scope;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Dependent
public class DependantBeans {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private final int id;
    private List<Integer> list = new ArrayList<>();
    private static final List<Integer> STATIC_LIST = new ArrayList<>();

    public DependantBeans() {
        id = COUNTER.getAndIncrement();
    }

    static List<Integer> getStaticList() {
        return STATIC_LIST;
    }

    @Outgoing("source")
    public Publisher<Integer> source() {
        return ReactiveStreams.of(id).buildRs();
    }

    @Incoming("source")
    @Outgoing("output")
    public int process(int i) {
        return i + 1;
    }

    @Incoming("output")
    public void sink(int v) {
        STATIC_LIST.add(v);
        list.add(v);
    }

    public List<Integer> getList() {
        return list;
    }
}
