/**
 * The MIT License
 *
 * Copyright for portions of unirest-java are held by Kong Inc (c) 2013.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package kong.unirest.apache;

import kong.unirest.Config;
import kong.unirest.UnirestConfigException;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.Closeable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static kong.unirest.apache.BaseApacheClient.toApacheCreds;

class ApacheAsyncConfig {
    final HttpAsyncClient client;
    final Config config;
    private AsyncIdleConnectionMonitorThread syncMonitor;
    private PoolingNHttpClientConnectionManager manager;
    private boolean hookset;

    public ApacheAsyncConfig(Config config) {
        this.config = config;
        try {
            manager = createConnectionManager();
            manager.setMaxTotal(config.getMaxConnections());
            manager.setDefaultMaxPerRoute(config.getMaxPerRoutes());

            HttpAsyncClientBuilder ab = HttpAsyncClientBuilder.create()
                    .setDefaultRequestConfig(RequestOptions.toRequestConfig(config))
                    .setConnectionManager(manager)
                    .setDefaultCredentialsProvider(toApacheCreds(config.getProxy()))
                    .useSystemProperties();

            setOptions(ab);

            CloseableHttpAsyncClient build = ab.build();
            build.start();
            syncMonitor = new AsyncIdleConnectionMonitorThread(manager);
            syncMonitor.tryStart();
            client = build;
            if (config.shouldAddShutdownHook()) {
                registerShutdownHook();
            }
        } catch (Exception e) {
            throw new UnirestConfigException(e);
        }
    }

    private void setOptions(HttpAsyncClientBuilder ab) {
        if (!config.isVerifySsl()) {
            disableSsl(ab);
        }
        if (config.useSystemProperties()) {
            ab.useSystemProperties();
        }
        if (!config.getFollowRedirects()) {
            ab.setRedirectStrategy(new ApacheNoRedirectStrategy());
        }
        if (!config.getEnabledCookieManagement()) {
            ab.disableCookieManagement();
        }
        config.getInterceptor().forEach(ab::addInterceptorFirst);
    }

    private PoolingNHttpClientConnectionManager createConnectionManager() throws Exception {
        return new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(),
                null,
                getRegistry(),
                null,
                null,
                config.getTTL(), TimeUnit.MILLISECONDS);
    }

    private Registry<SchemeIOSessionStrategy> getRegistry() throws Exception {
        if (config.isVerifySsl()) {
            return RegistryBuilder.<SchemeIOSessionStrategy>create()
                    .register("http", NoopIOSessionStrategy.INSTANCE)
                    .register("https", SSLIOSessionStrategy.getDefaultStrategy())
                    .build();
        } else {
            return RegistryBuilder.<SchemeIOSessionStrategy>create()
                    .register("http", NoopIOSessionStrategy.INSTANCE)
                    .register("https", new SSLIOSessionStrategy(new SSLContextBuilder()
                            .loadTrustMaterial(null, (x509Certificates, s) -> true)
                            .build(), NoopHostnameVerifier.INSTANCE))
                    .build();
        }
    }


    private void disableSsl(HttpAsyncClientBuilder ab) {
        try {
            ab.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            ab.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (arg0, arg1) -> true).build());
        } catch (Exception e) {
            throw new UnirestConfigException(e);
        }
    }

    public ApacheAsyncConfig(HttpAsyncClient client, Config config) {
        this.client = client;
        this.config = config;
    }

    @Deprecated
    public ApacheAsyncConfig(HttpAsyncClient client,
                             Config config,
                             PoolingNHttpClientConnectionManager manager,
                             AsyncIdleConnectionMonitorThread monitor) {
        Objects.requireNonNull(client, "Client may not be null");

        this.config = config;
        this.client = client;
        this.syncMonitor = monitor;
        this.manager = manager;
    }

    public void registerShutdownHook() {
        if (!hookset) {
            hookset = true;
            Runtime.getRuntime().addShutdownHook(new Thread(this::close, "Unirest Apache Async Client Shutdown Hook"));
        }
    }

    public boolean isRunning() {
        return Util.tryCast(client, CloseableHttpAsyncClient.class)
                .map(CloseableHttpAsyncClient::isRunning)
                .orElse(true);
    }

    public HttpAsyncClient getClient() {
        return client;
    }

    public Stream<Exception> close() {
        return Util.collectExceptions(Util.tryCast(client, CloseableHttpAsyncClient.class)
                        .filter(CloseableHttpAsyncClient::isRunning)
                        .map(c -> Util.tryDo(c, Closeable::close))
                        .filter(Optional::isPresent)
                        .map(Optional::get),
                Util.tryDo(manager, PoolingNHttpClientConnectionManager::shutdown),
                Util.tryDo(syncMonitor, Thread::interrupt));
    }
}
