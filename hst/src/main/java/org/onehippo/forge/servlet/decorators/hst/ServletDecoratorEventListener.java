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

import javax.jcr.observation.Event;

import org.hippoecm.hst.core.jcr.GenericEventListener;
import org.onehippo.forge.servlet.decorators.common.DecoratorConfigurationLoader;

public class ServletDecoratorEventListener extends GenericEventListener {


    private DecoratorConfigurationLoader configLoader;

    public void setConfigLoader(final DecoratorConfigurationLoader configLoader) {
        this.configLoader = configLoader;
    }

    @Override
    protected void onNodeAdded(Event event) {
        doInvalidation(event);
    }

    @Override
    protected void onNodeRemoved(Event event) {
        doInvalidation(event);
    }

    @Override
    protected void onPropertyAdded(Event event) {
        doInvalidation(event);
    }

    @Override
    protected void onPropertyChanged(Event event) {
        doInvalidation(event);
    }

    @Override
    protected void onPropertyRemoved(Event event) {
        doInvalidation(event);
    }

    private void doInvalidation(final Event path) {
        configLoader.invalidate(path);
    }
}
