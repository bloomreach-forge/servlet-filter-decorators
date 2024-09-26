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

public final class DecoratorConst {


    public static final String CONFIG_ENABLED = "enabled";
    public static final String CONFIG_HOSTNAME = "hostnames";
    public static final String CONFIG_CONTEXT_PATHS = "contextpaths";
    public static final String CONFIG_HEADER_HOST = "hostheader";



    public static final String HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";


    public static final int CACHE_EXPIRES_IN_DAYS = 30;
    public static final long CACHE_MAX_SIZE = 100;

    private DecoratorConst() {
    }
}
