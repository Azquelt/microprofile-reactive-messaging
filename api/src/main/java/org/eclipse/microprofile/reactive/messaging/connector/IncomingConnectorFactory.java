/**
 * Copyright (c) 2018-2019 Contributors to the Eclipse Foundation
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.reactive.messaging.connector;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.MessagingProvider;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;

import javax.enterprise.inject.spi.DeploymentException;
import java.util.NoSuchElementException;

/**
 * SPI used to implement a connector managing a source of messages for a specific <em>transport</em>. For example, to
 * handle the consumption of records from Kafka, the reactive messaging extension would need to implement a {@code bean}
 * implementing this interface. This bean is called for every {@code channel} that needs to be created for this specific
 * <em>transport</em> (so Kafka in this case). These channels are connected to methods annotated with
 * {@link org.eclipse.microprofile.reactive.messaging.Incoming}.
 * <p>
 * The factory is called to create a {@code channel} for each configured <em>transport</em>. The configuration is done using
 * MicroProfile Config. The following snippet gives an example for a hypothetical Kafka connector:
 *
 * <pre>
 * mp.messaging.incoming.my-channel.type=i.e.m.reactive.messaging.impl.kafka.Kafka
 * mp.messaging.incoming.my-channel.bootstrap-servers=localhost:9092
 * mp.messaging.incoming.my-channel.topic=my-topic
 * ...
 * </pre>
 * <p>
 * The configuration keys are structured as follows: {@code mp.messaging.[incoming|outgoing].channel-name.attribute}.
 * <p>
 * <p>
 * The portion of the key that precedes the {@code attribute} is acts as a property prefix that has a common structure
 * across all {@code channels}.
 * </p>
 * <p>
 * The {@code channel-name} segment in the configuration key corresponds to the name of the channel used in the
 * {@code Incoming} annotation:
 *
 * <pre>
 * &#64;Incoming("my-channel")
 * public void consume(String s) {
 *      // ...
 * }
 * </pre>
 * <p>
 * The set of attributes depend on the connector and transport layer (for example, bootstrap-servers is Kafka specific).
 * The {@code type} attribute is mandatory and indicates the fully qualified name of the {@link MessagingProvider}
 * implementation. It must match the class returned by the {@link #type()} method. This is how a reactive messaging
 * implementation looks for the specific {@link IncomingConnectorFactory} required for a channel. In the previous
 * configuration, the reactive messaging implementation would need to find the {@link IncomingConnectorFactory} returning
 * the {@code i.e.m.reactive.messaging.impl.kafka.Kafka} class as result to its {@link #type()} method to create the
 * {@code my-channel} channel. Note that if the connector cannot be found, the deployment must be failed with a
 * {@link DeploymentException}.
 * <p>
 * The {@link #getPublisherBuilder(Config)} is called for every channel that needs to be created. The {@link Config} object
 * passed to the method contains a subset of the global configuration, and with the prefixes removed. So for the previous
 * configuration, it would be:
 * <pre>
 * bootstrap-services = localhost:9092
 * topic = my-topic
 * </pre>
 * <p>
 * In this example, if 'topic' was missing as a configuration property, the Kafka connector would be at liberty to
 * default to the stream name indicated in the annotation as the Kafka topic. Such connector specific behaviours are
 * outside the scope of this specification.
 * <p>
 * So the connector implementation can retrieve the value with {@link Config#getValue(String, Class)} and
 * {@link Config#getOptionalValue(String, Class)}.
 * <p>
 * If the configuration is invalid, the {@link #getPublisherBuilder(Config)} method must throw an
 * {@link IllegalArgumentException}, caught by the reactive messaging implementation and failing the deployment by
 * throwing a {@link DeploymentException} wrapping the exception.
 * <p>
 * Note that a Reactive Messaging implementation must support the configuration format described here. Implementations
 * are free to provide additional support for other approaches.
 */
public interface IncomingConnectorFactory {

    /**
     * Gets the {@link MessagingProvider} class associated with this {@link IncomingConnectorFactory}.
     * Note that the {@link MessagingProvider} is a user-facing interface used in the configuration.
     *
     * @return the {@link MessagingProvider} associated with this {@link IncomingConnectorFactory}. Must not be
     * {@code null}. Returning {@code null} will cause a deployment failure.
     */
    Class<? extends MessagingProvider> type();

    /**
     * Creates a <em>channel</em> for the given configuration. The given configuration {@code type} attribute matches the
     * {@link #type()}.
     * <p>
     * Note that the connection to the <em>transport</em> or <em>broker</em> is generally postponed until the
     * subscription occurs.
     *
     * @param config the configuration, must not be {@code null}
     * @return the created {@link PublisherBuilder}, will not be {@code null}.
     * @throws IllegalArgumentException if the configuration is invalid.
     * @throws NoSuchElementException   if the configuration does not contain an expected attribute.
     */
    PublisherBuilder<? extends Message> getPublisherBuilder(Config config);

}
