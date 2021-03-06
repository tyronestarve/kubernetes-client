/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes.server.mock;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import io.fabric8.kubernetes.api.model.RootPathsBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.ContextBuilder;
import io.fabric8.mockwebserver.DefaultMockServer;
import io.fabric8.mockwebserver.ServerRequest;
import io.fabric8.mockwebserver.ServerResponse;
import io.fabric8.mockwebserver.internal.DefaultWebSocketWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class KubernetesMockServer extends DefaultMockServer {

    private static final Context context = new ContextBuilder()
            .withReader(new KubernetesWebSocketReader())
            .withWriter(new DefaultWebSocketWriter())
            .build();

    public KubernetesMockServer() {
        this(true);
    }

    public KubernetesMockServer(boolean useHttps) {
        this(new MockWebServer(), new HashMap(), useHttps);
    }

    public KubernetesMockServer(MockWebServer server, Map<ServerRequest, Queue<ServerResponse>> responses, boolean useHttps) {
        this(context, server, responses, useHttps);
    }

    public KubernetesMockServer(Context context, MockWebServer server, Map<ServerRequest, Queue<ServerResponse>> responses, boolean useHttps) {
        super(context, server, responses, useHttps);
    }


    public void init() {
        start();
    }

    public void destroy() {
        shutdown();
    }

    public void onStart() {
        expect().get().withPath("/").andReturn(200, new RootPathsBuilder().addToPaths(getRootPaths()).build()).always();
    }

    public String[] getRootPaths() {
        return new String[]{"/api", "/apis/extensions"};
    }

    public NamespacedKubernetesClient createClient() {
        Config config = new ConfigBuilder()
                .withMasterUrl(url("/"))
                .withTrustCerts(true)
                .withNamespace("test")
                .build();
        return new DefaultKubernetesClient(config);
    }

}
