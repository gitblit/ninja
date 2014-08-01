/**
 * Copyright (C) 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ninja.swagger;

import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The Swagger router configures required routes.
 *
 * @author James Moger
 *
 */
@Singleton
public class SwaggerRouter implements ApplicationRoutes {

    @Inject
    SwaggerController swaggerController;

    String uiPath;
    String pathPrefix;

    public SwaggerRouter() {
        this.uiPath = "/api";
        this.pathPrefix = uiPath;
    }

    /**
     * Sets the path to the Swagger UI. The docs & webjars paths will be
     * {path}/api-docs and {path}/webjars.
     *
     * @param path
     */
    public void setPath(String path) {
        if (Strings.isNullOrEmpty(path)) {
            path = "/";
        }

        this.uiPath = path;

        // Determine route prefixes
        this.pathPrefix = path;
        if (pathPrefix.endsWith("/")) {
            pathPrefix = path.substring(0, path.length() - 1);
        }
    }

    @Override
    public void init(Router router) {

        // set the path in the SwaggerController
        swaggerController.setPath(uiPath);

        // /////////////////////////////////////////////////////////////////////
        // Swagger UI
        // /////////////////////////////////////////////////////////////////////
        router.GET().route(uiPath).with(SwaggerController.class, "serveUI");

        // /////////////////////////////////////////////////////////////////////
        // Swagger Docs
        // /////////////////////////////////////////////////////////////////////
        router.GET().route(pathPrefix + "/api-docs{doc: .*}")
                .with(SwaggerController.class, "serveDoc");

        // /////////////////////////////////////////////////////////////////////
        // WebJars Route
        // /////////////////////////////////////////////////////////////////////
        router.GET().route(pathPrefix + "/webjars/{fileName: .*}")
                .with(AssetsController.class, "serveWebJars");

    }

}
