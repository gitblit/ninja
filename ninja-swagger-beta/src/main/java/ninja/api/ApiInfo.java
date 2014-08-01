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

package ninja.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an ApplicationRoutes class as a Swagger Resource Listing.
 * <p/>
 * The resource affects both the root document of Swagger, the Resource Listing,
 * and the API Declaration of that specific resource.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiInfo {

    /**
     * Corresponds to the `title` field of the Resource Listing.
     * <p/>
     * This should be the title of the resource listing.
     */
    String title();

    /**
     * Corresponds to the `description` field of the Resource Listing.
     * <p/>
     * This should be a short description of the resource listing.
     */
    String description() default "";

    /**
     * Corresponds to the `contact` field of the Resource Listing.
     * <p/>
     * This should be an email address or an url.
     */
    String contact() default "";

    /**
     * Corresponds to the `termsOfService` field of the Resource Listing.
     * <p/>
     * This should be an url for your ToS.
     */
    String termsOfService() default "";

    /**
     * Corresponds to the `license` field of the Resource Listing.
     * <p/>
     * This should be the short name of your API license.
     */
    String license() default "";

    /**
     * Corresponds to the `licenseUrl` field of the Resource Listing.
     * <p/>
     * This should be the url to your chosen license.
     */
    String licenseUrl() default "";

}
