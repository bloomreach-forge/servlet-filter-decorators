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
package org.onehippo.forge.servlet.decorators.common;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DecoratorConfigurationLoader {

    private static final Logger log = LoggerFactory.getLogger(DecoratorConfigurationLoader.class);

    private String configurationLocation;
    private Repository repository;
    private Credentials credentials;
    private Date lastLoadDate = new Date();
    protected volatile boolean needRefresh = true;
    private final Set<DecoratorConfiguration> data = ConcurrentHashMap.newKeySet();




    public boolean needReloading() {
        return needRefresh;
    }

    public synchronized Set<DecoratorConfiguration> load() {
        if (!needReloading()) {
            return data;
        }

        log.debug("Previously loaded: {}", lastLoadDate);
        Session session = null;
        try {
            session = getSession();
            if (session == null) {
                log.warn("Session was null, cannot load decorator config data");
                return data;
            }
            final Node node = session.getNode(configurationLocation);
            parseConfig(node);
        } catch (Exception e) {
            log.error("Error loading decorator configuration", e);
        } finally {
            closeSession(session);
        }
        needRefresh = false;
        lastLoadDate = new Date();
        return data;
    }

    public synchronized void invalidate(final Event event) {
        // we invalidate on any event:
        needRefresh = true;
    }

    public Date getLastLoadDate() {
        return lastLoadDate;
    }

    protected Session getSession() {
        Session session = null;
        try {
            session = repository.login(credentials);
        } catch (RepositoryException e) {
            log.error("Error obtaining session", e);
        }
        return session;
    }

    protected void closeSession(final Session session) {
        if (session != null) {
            session.logout();
        }
    }

    private void parseConfig(final Node node) throws RepositoryException {
        data.clear();
        final NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            final Node configNode = nodes.nextNode();
            final Set<DecoratorConfiguration> configurations = parse(configNode);
            if (configurations != null) {
                data.addAll(configurations);
            }
        }
    }

    private Set<DecoratorConfiguration> parse(final Node node) throws RepositoryException {
        final boolean enabled = JcrUtils.getBooleanProperty(node, DecoratorConst.CONFIG_ENABLED, true);
        if (!enabled) {
            log.info("Configuration disabled for configuration at {}", node.getPath());
        }

        final String[] hosts = JcrUtils.getMultipleStringProperty(node, DecoratorConst.CONFIG_HOSTNAME, null);
        if (hosts == null) {
            log.error("Host names property ({}) is missing for configuration at {}", DecoratorConst.CONFIG_HOSTNAME, node.getPath());
            return null;
        }
        final String[] contextPaths = JcrUtils.getMultipleStringProperty(node, DecoratorConst.CONFIG_CONTEXT_PATHS, null);
        if (contextPaths == null) {
            log.error("Context paths  property ({}) is missing for configuration at {}", DecoratorConst.CONFIG_CONTEXT_PATHS, node.getPath());
            return null;
        }
        final int hostLength = hosts.length;
        final int contextPathLength = contextPaths.length;
        if (hostLength != contextPathLength) {
            log.error("Invalid configuration: number of host names doesn't match number of context paths:{} vs{} ", hostLength, contextPathLength);
            return null;
        }
        final Map<String, String> mappings = IntStream.range(0, hostLength)
                .boxed()
                .collect(Collectors.toMap(i -> hosts[i], i -> contextPaths[i]));

        final String header = JcrUtils.getStringProperty(node, DecoratorConst.CONFIG_HEADER_HOST, DecoratorConst.HEADER_X_FORWARDED_HOST);
        final Set<DecoratorConfiguration> configs = DecoratorConfiguration.
                Builder
                .start()
                .enabled(enabled)
                .hostHeader(header)
                .hosts(mappings).build();
        log.info("Loaded configurations: {}", configs);
        return configs;
    }


    public String getConfigurationLocation() {
        return configurationLocation;
    }

    public void setConfigurationLocation(final String configurationLocation) {
        this.configurationLocation = configurationLocation;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(final Repository repository) {
        this.repository = repository;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(final Credentials credentials) {
        this.credentials = credentials;
    }


}
