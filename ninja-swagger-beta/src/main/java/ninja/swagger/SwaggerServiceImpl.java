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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ninja.Router;
import ninja.application.ApplicationRoutes;
import ninja.lifecycle.Start;
import ninja.lifecycle.State;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartbear.swagger4j.ResourceListing;
import com.smartbear.swagger4j.Swagger;
import com.smartbear.swagger4j.SwaggerFormat;
import com.smartbear.swagger4j.impl.Utils.MapSwaggerStore;

/**
 * The SwaggerService generates a Swagger documents at runtime and caches them
 * for use.
 *
 * @author James Moger
 */
@Singleton
public class SwaggerServiceImpl implements SwaggerService {

    private static final String ROUTES_CONVENTION_LOCATION = "conf.Routes";

    private final Logger logger = LoggerFactory
            .getLogger(SwaggerServiceImpl.class);

    private Map<String, String> documents;

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private Router router;

    private long startTime;

    /*
     * (non-Javadoc)
     *
     * @see ninja.lifecycle.LifecycleService#start()
     */
    @Override
    @Start(order = 10)
    public void start() {

        // generate the Swagger documents cache
        Map<String, String> docs = buildDocumentsCache();
        documents = Collections.unmodifiableMap(docs);
        startTime = System.currentTimeMillis();

    }

    /*
     * (non-Javadoc)
     *
     * @see ninja.lifecycle.LifecycleService#stop()
     */
    @Override
    public void stop() {
    }

    /*
     * (non-Javadoc)
     *
     * @see ninja.lifecycle.LifecycleService#isStarted()
     */
    @Override
    public boolean isStarted() {
        return documents != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see ninja.lifecycle.LifecycleService#getState()
     */
    @Override
    public State getState() {
        return isStarted() ? State.STARTED : State.STOPPED;
    }

    /*
     * (non-Javadoc)
     *
     * @see ninja.swagger.SwaggerService#getStartTime()
     */
    @Override
    public long getStartTime() {
        return startTime;
    }

    /*
     * (non-Javadoc)
     *
     * @see ninja.lifecycle.LifecycleService#getUpTime()
     */
    @Override
    public long getUpTime() {
        return System.currentTimeMillis() - startTime;
    }

    /*
     * (non-Javadoc)
     *
     * @see ninja.swagger.SwaggerService#getDocument(java.lang.String)
     */
    @Override
    public String getDocument(String name) {
        if (documents == null) {
            return null;
        }
        return documents.get(name);
    }

    /**
     * Builds Swagger documents from properly annotated routes in the Router.
     */
    protected Map<String, String> buildDocumentsCache() {
        long start = System.nanoTime();

        Map<String, String> cache = Maps.newHashMap();

        String serverUrl = ninjaProperties.getWithDefault(NinjaConstant.serverName,
                "http://localhost:8080");
        String appUrl = serverUrl + ninjaProperties.getContextPath();

        Class<? extends ApplicationRoutes> applicationRoutes = getApplicationRoutesClass();

        // builds the Swagger resource listing for the router
        SwaggerBuilder builder = new SwaggerBuilder();
        ResourceListing resourceListing = builder.build(applicationRoutes,
                router, appUrl);

        try {

            MapSwaggerStore store = new MapSwaggerStore();

            // Write Swagger documents to our memory store
            if (ninjaProperties.getBooleanWithDefault("swagger.generateJson",
                    true)) {
                Swagger.createWriter(SwaggerFormat.json).writeSwagger(store,
                        resourceListing);
            }

            if (ninjaProperties.getBooleanWithDefault("swagger.generateXml",
                    false)) {
                Swagger.createWriter(SwaggerFormat.xml).writeSwagger(store,
                        resourceListing);
            }

            // Create a finished document cache
            for (Map.Entry<String, StringWriter> entry : store.getFileMap()
                    .entrySet()) {
                final String doc = entry.getKey();
                final String finalPath;
                if (doc.charAt(0) == '/') {
                    finalPath = doc.substring(1);
                } else {
                    finalPath = doc;
                }
                cache.put(finalPath, entry.getValue().toString());
            }

            long end = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(end - start);
            logger.info("Generated {} Swagger document{} in {} ms",
                    resourceListing.getApis().size(), resourceListing.getApis()
                            .size() == 1 ? "" : "s", duration);

            return cache;
        } catch (IOException e) {
            logger.error(null, e);
        }

        return cache;
    }

    protected Class<? extends ApplicationRoutes> getApplicationRoutesClass() {
        // get custom base package for application modules and routes
        Optional<String> applicationModulesBasePackage = Optional
                .fromNullable(ninjaProperties
                        .get(NinjaConstant.APPLICATION_MODULES_BASE_PACKAGE));

        // Init routes
        String routesClassName = getClassNameWithOptionalUserDefinedPrefix(
                applicationModulesBasePackage, ROUTES_CONVENTION_LOCATION);

        try {
            Class<?> clazz = Class.forName(routesClassName);
            Class<? extends ApplicationRoutes> applicationRoutes = clazz
                    .asSubclass(ApplicationRoutes.class);
            return applicationRoutes;
        } catch (ClassNotFoundException e) {
        }

        return null;
    }

    private String getClassNameWithOptionalUserDefinedPrefix(Optional<String> optionalUserDefinedPrefixForPackage,
                                                             String classLocationAsDefinedByNinja) {

        if (optionalUserDefinedPrefixForPackage.isPresent()) {
            return new StringBuilder(optionalUserDefinedPrefixForPackage.get())
                    .append('.').append(classLocationAsDefinedByNinja)
                    .toString();
        } else {
            return classLocationAsDefinedByNinja;
        }
    }
}
