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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;

public abstract class CommonUndecorateFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(CommonUndecorateFilter.class);

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HippoDecoratedServletRequest) {
            log.debug("Un-decorating request");
            final HippoDecoratedServletRequest decoratedServletRequest = (HippoDecoratedServletRequest) request;
            final ServletRequest originalRequest = decoratedServletRequest.getRequest();
            chain.doFilter(originalRequest, response);
        } else if (request instanceof ServletRequestWrapper) {
            // wrapped into someone else wrapper e.g spring security wraps request three levels deep:
            unwrapDeep(request, (ServletRequestWrapper)request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private  void unwrapDeep(final ServletRequest original, final ServletRequestWrapper request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HippoDecoratedServletRequest) {
            final HippoDecoratedServletRequest decoratedServletRequest = (HippoDecoratedServletRequest) request;
            // reset our request
            log.debug("Un-decorating request: resetting original");
            decoratedServletRequest.setServeOriginal(true);
            chain.doFilter(original, response);
            return;
        }
        final ServletRequest wrappedRequest = request.getRequest();
        if (wrappedRequest instanceof ServletRequestWrapper) {
            unwrapDeep(original, (ServletRequestWrapper) wrappedRequest, response, chain);
            return;
        }
        chain.doFilter(original, response);

    }


    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        
    }


    @Override
    public void destroy() {

    }


}
