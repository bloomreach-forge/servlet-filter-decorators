/*
 * Copyright 2024 Bloomreach B.V. (http://www.bloomreach.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.servlet.decorators;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onehippo.forge.servlet.decorators.common.DecoratorConfiguration;
import org.onehippo.forge.servlet.decorators.common.DecoratorConfigurationLoader;
import org.onehippo.forge.servlet.decorators.common.DecoratorConst;
import org.onehippo.forge.servlet.decorators.common.HippoDecoratedServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public abstract class ConfigurableDecoratorFilter implements Filter {


    private static final Logger log = LoggerFactory.getLogger(ConfigurableDecoratorFilter.class);
    protected DecoratorConfigurationLoader configLoader;
    private LoadingCache<String, DecoratorConfiguration> cache;
    protected boolean initialized;

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        final DecoratorConfiguration configuration = config(request, response);
        if (configuration.disabled() || configuration.invalid()) {
            log.debug("Invalid or disabled configuration, skipping decorating: {}", configuration);
            chain.doFilter(request, response);
            return;
        }
        final HippoDecoratedServletRequest decorated = new HippoDecoratedServletRequest((HttpServletRequest) request, configuration);
        chain.doFilter(decorated, response);
    }


    @Override
    public void init(final jakarta.servlet.FilterConfig filterConfig) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(DecoratorConst.CACHE_MAX_SIZE)
                .expireAfterWrite(DecoratorConst.CACHE_EXPIRES_IN_DAYS, TimeUnit.DAYS)
                .build(new CacheLoader<String, DecoratorConfiguration>() {
                    private final ExecutorService executor = Executors.newFixedThreadPool(1);

                    public DecoratorConfiguration load(String key) {
                        return loadConfig(key);
                    }

                    @Override
                    public ListenableFuture<DecoratorConfiguration> reload(final String key, final DecoratorConfiguration oldValue) throws Exception {
                        final ListenableFutureTask<DecoratorConfiguration> task = ListenableFutureTask.create(() -> load(key));
                        executor.execute(task);
                        return task;
                    }
                });
    }


    public DecoratorConfiguration config(final ServletRequest request, final ServletResponse response) {
        if (!initialized) {
            requestData();
        }

        if (!initialized) {
            log.debug("{}: not initialized yet", this.getClass().getSimpleName());
            return DecoratorConfiguration.INVALID;
        }

        if (configLoader.needReloading()) {
            invalidateCaches();
        }
        final String host = getHost((HttpServletRequest) request);

        try {
            return cache.get(host);
        } catch (ExecutionException e) {
            log.error("Error loading object:", e);
        }
        return DecoratorConfiguration.INVALID;
    }

    private void requestData() {
        if (!initialized) {
            initializeConfigManager();
        }
        if (initialized && configLoader.needReloading()) {
            configLoader.load();
            invalidateCaches();
            log.info("{}: data reloaded", this.getClass().getSimpleName());
        }
    }


    protected abstract void initializeConfigManager();


    protected DecoratorConfiguration loadConfig(final String host) {
        final Set<DecoratorConfiguration> rawData = configLoader.load();
        for (DecoratorConfiguration value : rawData) {
            final Pattern pattern = value.getHostPattern();
            final Matcher matcher = pattern.matcher(host);
            if (matcher.matches()) {
                log.debug("Found host: {} in pattern: {}", host, pattern);
                return value;
            }

        }

        log.debug("Couldn't match host {} within current patterns", host);
        // just return invalid object
        return DecoratorConfiguration.INVALID;
    }

    public String getHost(final HttpServletRequest request) {
        final String hostHeader = request.getHeader(DecoratorConst.HEADER_X_FORWARDED_HOST);
        if (Strings.isNullOrEmpty(hostHeader)) {
            final String remoteHost = request.getRemoteHost();
            log.debug("Missing header {}, using: {}", DecoratorConst.HEADER_X_FORWARDED_HOST, remoteHost);
            return remoteHost;
        }
        log.debug("Found header: {} -> {}", DecoratorConst.HEADER_X_FORWARDED_HOST, hostHeader);
        return hostHeader;

    }

    private void invalidateCaches() {
        log.debug("Invalidating servlet decorator cache");
        cache.invalidateAll();
    }

    @Override
    public void destroy() {
        invalidateCaches();
    }
}
