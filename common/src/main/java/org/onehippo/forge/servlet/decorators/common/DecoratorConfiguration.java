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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public final class DecoratorConfiguration {

    public static final DecoratorConfiguration INVALID = new DecoratorConfiguration();
    private boolean valid;
    private boolean enabled;
    private String hostHeader;
    private String contextPath;
    private Pattern hostPattern;


    private DecoratorConfiguration() {
    }


    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    public boolean isValid() {
        return valid;
    }

    public String getHostHeader() {
        return hostHeader;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Pattern getHostPattern() {
        return hostPattern;
    }
    

    public boolean disabled() {
        return !enabled;
    }

    public boolean invalid() {
        return !valid;
    }


    public static final class Builder {
        private static final Logger log = LoggerFactory.getLogger(Builder.class);

        private final Map<String, String> mappings = new HashMap<>();
        private String hostHeader;
        private boolean enabled;

        private Builder() {

        }

        public static Builder start() {
            return new Builder();
        }

        public Builder hostHeader(final String hostHeader) {
            this.hostHeader = hostHeader;
            return this;
        }


        public Builder hosts(final Map<String, String> hosts) {
            if (hosts != null) {
                mappings.putAll(hosts);
            }
            return this;
        }

        public Builder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }


        public Set<DecoratorConfiguration> build() {

            final Set<DecoratorConfiguration> objects = new HashSet<>();
            final Map<Pattern, String> patterns = parseHostPatterns(mappings);;
            for (Map.Entry<Pattern, String> entry : patterns.entrySet()) {
                final Pattern host = entry.getKey();
                final String contextPath = entry.getValue();
                final DecoratorConfiguration config = new DecoratorConfiguration();
                config.enabled = enabled;
                config.hostHeader = hostHeader;
                config.contextPath = contextPath;
                config.hostPattern = host;
                validate(config);
                objects.add(config);
            }

            return ImmutableSet.copyOf(objects);
        }

        private void validate(final DecoratorConfiguration config) {
            config.valid = config.contextPath != null && config.hostPattern !=null;
        }

        private Map<Pattern, String> parseHostPatterns(final Map<String, String> hosts) {
            if (hosts == null) {
                throw new IllegalStateException("No host names provided");
            }
            final Map<Pattern, String> patterns = new HashMap<>();
            for (Map.Entry<String, String> entry : hosts.entrySet()) {
                String host = entry.getKey();
                if (Strings.isNullOrEmpty(host)) {
                    log.warn("Skipping empty host value");
                    continue;
                }
                try {
                    final Pattern pattern = Pattern.compile(host);
                    patterns.put(pattern, entry.getValue());
                } catch (Exception e) {
                    log.error("Invalid host value {}", host);
                    log.error("Error compiling host pattern: ", e);
                }
            }
            return ImmutableMap.copyOf(patterns);
        }


    }


    @Override
    public String toString() {
        return "FilterConfiguration{" +
                "valid=" + valid +
                ", enabled=" + enabled +
                ", hostHeader='" + hostHeader + '\'' +
                ", contextPath='" + contextPath + '\'' +
                ", hostPattern=" + hostPattern +
                '}';
    }
}
