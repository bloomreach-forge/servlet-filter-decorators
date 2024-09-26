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

package org.onehippo.forge.servlet.decorators.service;

import javax.jcr.Session;

import org.onehippo.forge.servlet.decorators.common.DecoratorConfigurationLoader;

public class CmsDecoratorConfigurationLoader extends DecoratorConfigurationLoader {

    private static final String DEFAULT_CONFIGURATION_PATH = "/hippo:configuration/hippo:modules/servlet-filter-decorators-module/hippo:moduleconfig";

    
    private final Session session;
    private final ServletDecoratorService service;

    public CmsDecoratorConfigurationLoader(final Session session, final ServletDecoratorService service) {
        this.session = session;
        this.service = service;
        setConfigurationLocation(getDefaultConfigurationLocation());
    }

    @Override
    public boolean needReloading() {
        if (service.configurationChanged()) {
            needRefresh = true;
            service.setConfigurationChanged(false);
        }
        return needRefresh;

    }

    @Override
    protected void closeSession(final Session session) {
        // ignore for CMS module, we share one session
    }

    @Override
    public Session getSession() {
        return session;
    }

    protected String getDefaultConfigurationLocation() {
        return DEFAULT_CONFIGURATION_PATH;
    }

    
}
