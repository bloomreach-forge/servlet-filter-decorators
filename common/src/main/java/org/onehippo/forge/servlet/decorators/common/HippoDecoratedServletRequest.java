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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class HippoDecoratedServletRequest extends HttpServletRequestWrapper {

    private static final Logger log = LoggerFactory.getLogger(HippoDecoratedServletRequest.class);
    private final DecoratorConfiguration config;
    /**
     * Flag we can set in case we cannot unwrap our decorator e.g. when it is deeply decorated by Spring security wrappers
     */
    private boolean serveOriginal;

    public HippoDecoratedServletRequest(HttpServletRequest request, final DecoratorConfiguration config) {
        super(request);
        this.config = config;
    }

    @Override
    public String getRequestURI() {
        final String uri = super.getRequestURI();
        if (isDisabled()) {
            log.debug("Serving original getRequestURI");
            return uri;
        }
        if (uri == null) {
            return null;
        }

        final String newContextPath = config.getContextPath();
        final String oldContextPath = super.getContextPath();
        if (oldContextPath.equals(newContextPath)) {
            return uri;
        }
        final String stripped = stripOldContext(uri);
        if (Strings.isNullOrEmpty(newContextPath) || (newContextPath.equals("/") && stripped.startsWith("/"))) {
            return stripped;
        }
        return newContextPath + stripped;
    }


    @Override
    public String getContextPath() {
        if (isDisabled()) {
            log.debug("Serving original context path");
            return super.getContextPath();
        }
        final String contextPath = config.getContextPath();
        log.debug("Using decorated context path: {}", contextPath);
        return contextPath;
    }

    public void setServeOriginal(final boolean serveOriginal) {
        this.serveOriginal = serveOriginal;
    }

    private boolean isDisabled() {
        return serveOriginal || config.disabled() || config.invalid();
    }


    private String stripOldContext(final String uri) {
        if (uri == null) {
            return null;
        }
        final String oldContext = super.getContextPath();
        final int length = oldContext.length();
        if (length > 1 && uri.startsWith(oldContext)) {
            return uri.substring(length);
        }
        return uri;
    }


}


