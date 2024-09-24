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

package org.onehippo.forge.servlet.decorators.hst;

import org.hippoecm.hst.core.container.ComponentManager;
import org.hippoecm.hst.site.HstServices;
import org.onehippo.forge.servlet.decorators.ConfigurableDecoratorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HstDecorateFilter extends ConfigurableDecoratorFilter {

    private static final Logger log = LoggerFactory.getLogger(HstDecorateFilter.class);


    @Override
    protected void initializeConfigManager() {

        if (HstServices.isAvailable()) {
            final ComponentManager componentManager = HstServices.getComponentManager();
            try {
                configLoader = componentManager.getComponent(HstDecoratorConfigurationLoader.class.getName(), HstDecoratorConfigurationLoader.class.getPackage().getName());
            } catch (Exception e) {
                log.error("Error loafing HstDecoratorConfigurationLoader service", e);
            }
            if (configLoader == null) {
                log.error("Configuration loader was null");
            } else {
                initialized = true;
            }
        } else {
            log.info("HstService not available yet...waiting..");
        }

    }


}
