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

package org.onehippo.forge.servlet.decorators.cms;

import javax.jcr.Session;

import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.forge.servlet.decorators.ConfigurableDecoratorFilter;
import org.onehippo.forge.servlet.decorators.service.CmsDecoratorConfigurationLoader;
import org.onehippo.forge.servlet.decorators.service.ServletDecoratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmsDecoratorFilter extends ConfigurableDecoratorFilter {

    private static final Logger log = LoggerFactory.getLogger(CmsDecoratorFilter.class);
    
    @Override
    protected void initializeConfigManager() {
        // check CMS service first
        final ServletDecoratorService service = HippoServiceRegistry.getService(ServletDecoratorService.class);
        if (service != null) {
            final Session session = service.getSession();
            if (session == null) {
                log.warn("ServletDecoratorService has no session");
                return;
            }
            configLoader = new CmsDecoratorConfigurationLoader(session, service);
            log.info("Successfully configured ServletDecoratorService");
            initialized = true;
        } else {
            // info because always so on startup
            log.info("ServletDecoratorService not yet available in registry");
        }
    }


}
