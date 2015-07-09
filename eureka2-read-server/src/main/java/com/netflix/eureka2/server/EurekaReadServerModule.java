/*
 * Copyright 2014 Netflix, Inc.
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

package com.netflix.eureka2.server;

import com.google.inject.name.Names;
import com.netflix.eureka2.client.EurekaInterestClient;
import com.netflix.eureka2.client.EurekaRegistrationClient;
import com.netflix.eureka2.metric.EurekaRegistryMetricFactory;
import com.netflix.eureka2.metric.SpectatorEurekaRegistryMetricFactory;
import com.netflix.eureka2.metric.client.EurekaClientMetricFactory;
import com.netflix.eureka2.metric.client.SpectatorEurekaClientMetricFactory;
import com.netflix.eureka2.metric.server.EurekaServerMetricFactory;
import com.netflix.eureka2.metric.server.SpectatorEurekaServerMetricFactory;
import com.netflix.eureka2.registry.EurekaRegistryView;
import com.netflix.eureka2.server.registry.EurekaReadServerRegistryView;
import com.netflix.eureka2.server.service.EurekaReadServerSelfInfoResolver;
import com.netflix.eureka2.server.service.EurekaReadServerSelfRegistrationService;
import com.netflix.eureka2.server.service.selfinfo.SelfInfoResolver;
import com.netflix.eureka2.server.service.SelfRegistrationService;
import com.netflix.eureka2.server.spi.ExtAbstractModule.ServerType;
import com.netflix.eureka2.server.transport.tcp.interest.TcpInterestServer;
import io.reactivex.netty.metrics.MetricEventsListenerFactory;
import io.reactivex.netty.spectator.SpectatorEventsListenerFactory;

/**
 * @author Tomasz Bak
 */
public class EurekaReadServerModule extends AbstractEurekaServerModule {

    private final EurekaRegistrationClient registrationClient;
    private final EurekaInterestClient interestClient;

    public EurekaReadServerModule() {
        this(null, null);
    }

    public EurekaReadServerModule(EurekaRegistrationClient registrationClient,
                                  EurekaInterestClient interestClient) {
        this.registrationClient = registrationClient;
        this.interestClient = interestClient;
    }

    @Override
    public void configureEureka() {
        bind(ServerType.class).toInstance(ServerType.Read);

        if (registrationClient == null) {
            bind(EurekaRegistrationClient.class).toProvider(EurekaRegistrationClientProvider.class);
        } else {
            bind(EurekaRegistrationClient.class).toInstance(registrationClient);
        }
        if (interestClient == null) {
            bind(EurekaInterestClient.class).toProvider(FullFetchInterestClientProvider.class);
        } else {
            bind(EurekaInterestClient.class).toInstance(interestClient);
        }

        bind(MetricEventsListenerFactory.class).annotatedWith(Names.named(com.netflix.eureka2.Names.INTEREST)).toInstance(new SpectatorEventsListenerFactory("discovery-rx-client-", "discovery-rx-server-"));
        bind(TcpInterestServer.class).asEagerSingleton();

        bind(EurekaRegistryView.class).to(EurekaReadServerRegistryView.class);

        // Metrics
        bind(EurekaClientMetricFactory.class).to(SpectatorEurekaClientMetricFactory.class).asEagerSingleton();
        bind(EurekaServerMetricFactory.class).to(SpectatorEurekaServerMetricFactory.class).asEagerSingleton();
        bind(EurekaRegistryMetricFactory.class).to(SpectatorEurekaRegistryMetricFactory.class).asEagerSingleton();

        bind(SelfInfoResolver.class).to(EurekaReadServerSelfInfoResolver.class).asEagerSingleton();
        bind(SelfRegistrationService.class).to(EurekaReadServerSelfRegistrationService.class).asEagerSingleton();

        bind(AbstractEurekaServer.class).to(EurekaReadServer.class);
    }
}