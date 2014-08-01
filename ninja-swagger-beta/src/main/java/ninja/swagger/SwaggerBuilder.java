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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ninja.BasicAuthFilter;
import ninja.Filter;
import ninja.FilterWith;
import ninja.Result;
import ninja.Route;
import ninja.Router;
import ninja.api.ApiInfo;
import ninja.api.Consumes;
import ninja.api.Form;
import ninja.api.FormField;
import ninja.api.Hidden;
import ninja.api.Json;
import ninja.api.Notes;
import ninja.api.Produces;
import ninja.api.ResponseCode;
import ninja.api.ResponseCodes;
import ninja.api.Returns;
import ninja.api.Values;
import ninja.api.Version;
import ninja.api.Xml;
import ninja.application.ApplicationRoutes;
import ninja.params.ArgumentExtractors;
import ninja.params.Header;
import ninja.params.Headers;
import ninja.params.Param;
import ninja.params.Params;
import ninja.params.PathParam;
import ninja.params.WithArgumentExtractor;
import ninja.validation.Length;
import ninja.validation.NumberValue;
import ninja.validation.Required;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import router.Path;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.smartbear.swagger4j.Api;
import com.smartbear.swagger4j.ApiDeclaration;
import com.smartbear.swagger4j.Authorizations;
import com.smartbear.swagger4j.Authorizations.AuthorizationType;
import com.smartbear.swagger4j.DataType;
import com.smartbear.swagger4j.Info;
import com.smartbear.swagger4j.Items;
import com.smartbear.swagger4j.Model;
import com.smartbear.swagger4j.Operation;
import com.smartbear.swagger4j.Parameter;
import com.smartbear.swagger4j.Parameter.ParamType;
import com.smartbear.swagger4j.Property;
import com.smartbear.swagger4j.ResourceListing;
import com.smartbear.swagger4j.ResourceListing.ResourceListingApi;
import com.smartbear.swagger4j.ResponseMessage;
import com.smartbear.swagger4j.Swagger;
import com.smartbear.swagger4j.SwaggerFactory;
import com.smartbear.swagger4j.SwaggerVersion;
import com.smartbear.swagger4j.impl.Utils;
import com.smartbear.swagger4j.impl.Utils.Types;

/**
 * Builds a Swagger Resource List from your ApplicationRoutes and Router.
 *
 * <ul>
 * <li>A Swagger ResourceListing is a list of documented Ninja controllers.
 * <li>A Swagger Resource (or Api) is a documented Ninja controller.
 * <li>A Swagger Operation is a documented Ninja controller method (Route).
 * </ul>
 *
 * @author James Moger
 *
 */
public class SwaggerBuilder {

    private final Logger logger = LoggerFactory.getLogger(SwaggerBuilder.class);

    private final SwaggerFactory factory;

    private final Map<Class<?>, ApiDeclaration> declaredApis;

    public SwaggerBuilder() {
        this.factory = Swagger.createSwaggerFactory();
        this.declaredApis = Maps.newHashMap();
    }

    /**
     * Builds a ResourceListing from the Router.
     *
     * @param applicationRoutes
     * @param router
     * @param baseUrl
     * @return a resource listing
     */
    public ResourceListing build(Class<? extends ApplicationRoutes> applicationRoutes,
                                 Router router,
                                 String baseUrl) {

        ResourceListing resourceListing = declareResourceListing(applicationRoutes);

        for (Route route : router.getRoutes()) {

            declareRoute(baseUrl, resourceListing, route);

        }

        return resourceListing;
    }

    /**
     * Declares a resource listing by extracting metadata from the @ApiControllerList
     * annotation on the application Routes class.
     *
     * @param applicationRoutes
     * @param resourceListing
     */
    private ResourceListing declareResourceListing(Class<? extends ApplicationRoutes> applicationRoutes) {

        ResourceListing resourceListing = factory
                .createResourceListing(SwaggerVersion.DEFAULT_VERSION);

        if (!applicationRoutes.isAnnotationPresent(ApiInfo.class)) {
            return resourceListing;
        }

        final ApiInfo spec = applicationRoutes.getAnnotation(ApiInfo.class);
        final Info apiInfo = resourceListing.getInfo();

        // api version
        if (applicationRoutes.isAnnotationPresent(Version.class)) {
            Version version = applicationRoutes.getAnnotation(Version.class);
            resourceListing.setApiVersion(version.value());
        }

        // title
        if (!spec.title().isEmpty()) {
            apiInfo.setTitle(spec.title());
        }

        // description
        if (!spec.description().isEmpty()) {
            apiInfo.setDescription(spec.description());
        }

        // contact
        if (!spec.contact().isEmpty()) {
            apiInfo.setContact(spec.contact());
        }

        // terms of service
        if (!spec.termsOfService().isEmpty()) {
            apiInfo.setTermsOfServiceUrl(spec.termsOfService());
        }

        // license
        if (!spec.license().isEmpty()) {
            apiInfo.setLicense(spec.license());
        }

        // license url
        if (!spec.licenseUrl().isEmpty()) {
            apiInfo.setLicenseUrl(spec.licenseUrl());
        }

        return resourceListing;
    }

    /**
     * Registers a route.
     *
     * @param baseUrl
     * @param resourceListing
     * @param route
     */
    protected void declareRoute(String baseUrl,
                                ResourceListing resourceListing,
                                Route route) {
        if (route.getControllerClass() == null
                || route.getControllerMethod() == null) {
            // invalid route
            logger.debug("ignoring invalid route {}", route.getUri());
            return;
        }

        final Class<?> implementationClass = route.getControllerClass();
        final Class<?> specificationClass = getSpecificationClass(route);

        if (specificationClass == null) {
            // not an api controller
            logger.debug("ignoring non-api controller {}",
                    implementationClass.getName());
            return;
        }

        if (specificationClass.isAnnotationPresent(Hidden.class)) {
            // undocumented api controller
            logger.debug("ignoring undocumented api controller {}",
                    implementationClass.getName());
            return;
        }

        final Method implementationMethod = route.getControllerMethod();
        final Method specificationMethod = getSpecificationMethod(
                specificationClass, route);

        if (specificationMethod == null) {
            logger.debug("ignoring non-api method {}.{}",
                    implementationClass.getName(),
                    implementationMethod.getName());
        }

        RouteSpec routeSpec = new RouteSpec(baseUrl, specificationClass,
                specificationMethod, route);

        if (specificationMethod.isAnnotationPresent(Hidden.class)) {
            // undocumented method
            logger.debug("ignoring undocumented api method {}.{}",
                    implementationClass.getName(),
                    implementationMethod.getName());
            return;
        }

        ApiDeclaration api = declareApis(resourceListing, routeSpec);

        try {
            Operation op;
            if (specificationMethod.isAnnotationPresent(Form.class)) {
                // Form method
                op = declareFormOperation(api, routeSpec);
            } else {
                // Standard method
                op = declareOperation(api, routeSpec);
            }

            // method specific notes
            if (specificationMethod.isAnnotationPresent(Notes.class)) {
                Notes notes = specificationMethod.getAnnotation(Notes.class);
                op.setNotes(notes.value());
            }

            // Defined return class
            if (specificationMethod.isAnnotationPresent(Returns.class)) {
                final Returns returnSpec = specificationMethod
                        .getAnnotation(Returns.class);
                final Class<?> type = returnSpec.value();
                final boolean isUnique = returnSpec.uniqueItems();

                Class<?> modelClass = setModelType(op, type, isUnique);

                if (modelClass != null) {
                    declareModel(api, modelClass);
                }

            }

            // Known error responses
            if (specificationMethod.isAnnotationPresent(ResponseCodes.class)) {
                ResponseCodes responses = specificationMethod
                        .getAnnotation(ResponseCodes.class);
                for (ResponseCode response : responses.value()) {
                    ResponseMessage message = op.addResponseMessage(
                            response.code(), response.message());
                    if (response.returns() != Void.class) {
                        message.setResponseModel(response.returns().getName());
                    }
                }
            }

            // Declare Ninja authentication filters as Authorizations
            if (implementationMethod.isAnnotationPresent(FilterWith.class)) {
                FilterWith filterWith = implementationMethod
                        .getAnnotation(FilterWith.class);
                declareAuthorizations(filterWith, resourceListing,
                        op.getAuthorizations());
            }

            // Declare Deprecated method
            declareDeprecated(op, specificationMethod, implementationMethod);
            declareParameters(op, routeSpec);

        } catch (Exception e) {
            logger.error(String.format("Failed to declare %s.%s",
                    implementationClass.getName(),
                    implementationMethod.getName()), e);
        }

    }

    /**
     * Declare an Authorization in the ResourceListing and reference the
     * Authorization scheme in the object's Authorizations spec.
     *
     * @param filterWith
     * @param resourceListing
     * @param objectAuthorizations
     */
    private void declareAuthorizations(FilterWith filterWith,
                                       ResourceListing resourceListing,
                                       Authorizations objectAuthorizations) {

        // currently only Basic Authentication is identified
        for (Class<? extends Filter> filterClass : filterWith.value()) {
            if (BasicAuthFilter.class.isAssignableFrom(filterClass)) {

                // declare the authorization in the resource list
                resourceListing.getAuthorizations()
                        .addAuthorization(AuthorizationType.BASIC.name(),
                                AuthorizationType.BASIC);

                // reference the authorization in the object's spec
                objectAuthorizations
                        .addAuthorization(AuthorizationType.BASIC.name(),
                                AuthorizationType.BASIC);
            }
        }
    }

    /**
     * Declares a controller as a Swagger Resource.
     *
     * @param resourceListing
     * @param specificationRoute
     * @return
     */
    private ApiDeclaration declareApis(ResourceListing resourceListing,
                                       RouteSpec specifiedRoute) {

        final Class<?> specificationClass = specifiedRoute.specificationClass;
        if (declaredApis.containsKey(specificationClass)) {
            return declaredApis.get(specificationClass);
        }
        final Class<?> implementationClass = specifiedRoute.route
                .getControllerClass();

        //
        // Register this api controller
        //

        ninja.api.Api api = specificationClass
                .getAnnotation(ninja.api.Api.class);

        // Ninja routes are absolute, no relative api paths
        final String relativeApiPath;
        if (implementationClass.isAnnotationPresent(Path.class)) {
            Path path = implementationClass.getAnnotation(Path.class);
            relativeApiPath = path.value()[0]; // XXX what about multiple controller paths?
        } else {
            relativeApiPath = null;
        }
        ApiDeclaration declaredApi = factory.createApiDeclaration(
                specifiedRoute.baseUrl, relativeApiPath);
        declaredApis.put(specificationClass, declaredApi);

        // Annotation for specifying Api Version
        if (specificationClass.isAnnotationPresent(Version.class)) {
            Version version = specificationClass.getAnnotation(Version.class);
            declaredApi.setApiVersion(version.value());
        }

        // Annotation for specifying APPLICATION/JSON
        if (specificationClass.isAnnotationPresent(Json.class)) {
            declaredApi.addConsumes(Result.APPLICATON_JSON);
            declaredApi.addProduces(Result.APPLICATON_JSON);
        }

        // Annotation for specifying APPLICATION/XML
        if (specificationClass.isAnnotationPresent(Xml.class)) {
            declaredApi.addConsumes(Result.APPLICATION_XML);
            declaredApi.addProduces(Result.APPLICATION_XML);
        }

        // produces (e.g. application/json)
        if (specificationClass.isAnnotationPresent(Produces.class)) {
            Produces produces = specificationClass
                    .getAnnotation(Produces.class);
            for (String value : produces.value()) {
                declaredApi.addProduces(value.trim());
            }
        }

        // consumes (e.g. application/json)
        if (specificationClass.isAnnotationPresent(Consumes.class)) {
            Consumes consumes = specificationClass
                    .getAnnotation(Consumes.class);
            for (String value : consumes.value()) {
                declaredApi.addConsumes(value.trim());
            }
        }

        // Declare controller authentication filters
        if (implementationClass.isAnnotationPresent(FilterWith.class)) {
            FilterWith filterWith = implementationClass
                    .getAnnotation(FilterWith.class);
            declareAuthorizations(filterWith, resourceListing,
                    declaredApi.getAuthorizations());
        }

        // Register the controller api with the root ResourceListing.
        //
        // The resource path only applies to Swagger document navigation
        final String resourcePath;
        if (api.name().isEmpty()) {
            resourcePath = "/" + specificationClass.getSimpleName();
        } else {
            resourcePath = "/" + api.name();
        }

        ResourceListingApi resource = resourceListing.addApi(declaredApi,
                resourcePath + ".{format}");

        if (!api.value().isEmpty()) {
            resource.setDescription(api.value());
        }

        return declaredApi;
    }

    /**
     * Declares a File Upload Swagger operation for the controller method.
     *
     * @param api
     * @param routeSpec
     * @return operation
     */
    private Operation declareFormOperation(ApiDeclaration api,
                                           RouteSpec routeSpec) {

        final Route route = routeSpec.route;
        final Class<?> implementationClass = route.getControllerClass();
        final String httpMethod = route.getHttpMethod();
        final Method method = routeSpec.specificationMethod;

        final Api declaredRoute = api.addApi(route.getUri());
        final Operation op = declaredRoute.addOperation(method.getName(),
                httpMethod);

        final ninja.api.Api spec = method.getAnnotation(ninja.api.Api.class);
        final Form formSpec = method.getAnnotation(Form.class);

        logger.debug("declaring api form method {} {}.{}", httpMethod,
                implementationClass.getName(), method.getName());

        // method nickname
        op.setNickName(method.getName());

        // method description
        if (!spec.value().isEmpty()) {
            op.setSummary(spec.value());
        }

        // consumes multipart/form-data for file uploads
        op.addConsumes("multipart/form-data");

        // Declare Form fields
        for (FormField field : formSpec.value()) {
            if (field.typeOf().equalsIgnoreCase("file")) {
                // File type is a body parameter
                Parameter formField = op.addParameter(field.name(),
                        ParamType.body);
                formField.setType(Types.File);
                formField.setDescription(field.description());
            } else {
                // Other types are form parameters
                Parameter formField = op.addParameter(field.name(),
                        ParamType.form);
                formField.setType(Types.String);
                formField.setDescription(field.description());
            }
        }

        return op;
    }

    /**
     * Declares a Swagger operation from the controller method.
     *
     * @param api
     * @param routeSpec
     * @return operation
     */
    private Operation declareOperation(ApiDeclaration api, RouteSpec routeSpec) {

        final Route route = routeSpec.route;
        final Class<?> implementationClass = route.getControllerClass();
        final String httpMethod = route.getHttpMethod();
        final Method method = routeSpec.specificationMethod;

        if (api.getApi(route.getUri()) == null) {
            api.addApi(route.getUri());
        }
        final Api routeApi = api.getApi(route.getUri());
        final Operation op = routeApi.addOperation(method.getName(),
                httpMethod);

        final ninja.api.Api spec = method.getAnnotation(ninja.api.Api.class);

        logger.debug("declaring api method {} {}.{}", httpMethod,
                implementationClass.getName(), method.getName());

        // method nickname
        op.setNickName(method.getName());

        // method description
        if (!spec.value().isEmpty()) {
            op.setSummary(spec.value());
        }

        // produces (e.g. application/json)
        if (method.isAnnotationPresent(Produces.class)) {
            // specified on method
            Produces produces = method.getAnnotation(Produces.class);
            for (String value : produces.value()) {
                op.addProduces(value);
            }
        } else {
            // inherit produces
            for (String value : api.getProduces()) {
                op.addProduces(value);
            }
        }

        // consumes (e.g. application/json)
        if (method.isAnnotationPresent(Consumes.class)) {
            // specified on method
            Consumes consumes = method.getAnnotation(Consumes.class);
            for (String value : consumes.value()) {
                op.addConsumes(value);
            }
        } else {
            // inherit consumes
            for (String value : api.getConsumes()) {
                op.addConsumes(value);
            }
        }
        return op;
    }

    /**
     * Declare the Swagger Operation Parameters for the Route.
     *
     * @param op
     * @param routeSpec
     */
    private void declareParameters(Operation op, RouteSpec routeSpec) {

        Class<?> implementationClass = routeSpec.route.getControllerClass();
        Method specificationMethod = routeSpec.specificationMethod;
        Method implementationMethod = routeSpec.route.getControllerMethod();

        for (int index = 0; index < implementationMethod.getParameterTypes().length; index++) {

            Class<?> parameterClass = implementationMethod.getParameterTypes()[index];

            if (ArgumentExtractors.getExtractorForType(parameterClass) != null) {
                // Ninja supplied parameter, not a request parameter
                continue;
            }

            // collect the parameter annotations
            Set<Annotation> annotations = Sets.newHashSet();
            annotations.addAll(Lists.newArrayList(implementationMethod
                    .getParameterAnnotations()[index]));

            if (!implementationMethod.equals(specificationMethod)) {
                // specification & implementation are different
                annotations.addAll(Lists.newArrayList(specificationMethod
                        .getParameterAnnotations()[index]));
            }

            // check to see if this parameter is undocumented
            boolean hidden = false;
            for (Annotation annotation : annotations) {
                if (annotation instanceof Hidden) {
                    hidden = true;
                    break;
                }
            }

            if (hidden) {
                // undocumented parameter, skip
                continue;
            }

            Parameter param = null;
            for (Annotation annotation : annotations) {

                if (annotation instanceof PathParam) {
                    // Path Parameter
                    PathParam spec = (PathParam) annotation;
                    param = op.addParameter(spec.value(), ParamType.path);
                    break;
                } else if (annotation instanceof Params) {
                    // Query Multi-valued Parameter
                    Params spec = (Params) annotation;
                    param = op.addParameter(spec.value(), ParamType.query);
                    param.setType(Types.Array);
                    break;
                } else if (annotation instanceof Param) {
                    // Query Parameter
                    Param spec = (Param) annotation;
                    param = op.addParameter(spec.value(), ParamType.query);
                    break;
                } else if (annotation instanceof Headers) {
                    // Headers Parameter
                    Headers spec = (Headers) annotation;
                    param = op.addParameter(spec.value(), ParamType.header);
                    param.setType(Types.Array);
                    break;
                } else if (annotation instanceof Header) {
                    // Header Parameter
                    Header spec = (Header) annotation;
                    param = op.addParameter(spec.value(), ParamType.header);
                    break;
                }

            }

            if (param == null) {
                // check to see if this is a Body parameter
                if (isBodyParameter(annotations)) {
                    param = op.addParameter(ParamType.body.name(),
                            ParamType.body);
                    param.setRequired(true);
                } else {
                    // not an API parameter, perhaps injected by Ninja
                    continue;
                }
            }

            final boolean isMultiple = isMultiple(parameterClass);
            final boolean isUnique = false; // XXX

            Class<?> parameterType;
            if (isMultiple && param.getType() == null) {
                // target type is an array but specification is a single value
                // prevent specify an 'array' type as the parameter
                // instead specify the array component type and 'allowMultiple'
                param.setAllowMultiple(true);
                parameterType = parameterClass.getComponentType();
            } else {
                parameterType = parameterClass;
            }

            try {
                Class<?> modelClass = setModelType(param, parameterType,
                        isUnique);

                if (modelClass != null) {
                    declareModel(op.getApi().getApiDeclaration(), modelClass);
                }

                defineAttributes(param,
                        annotations.toArray(new Annotation[annotations.size()]));
            } catch (Exception e) {
                logger.error(String.format("Failed to declare %s.%s (%s)",
                        implementationClass.getName(),
                        implementationMethod.getName(),
                        parameterClass.getName()), e);
            }
        }
    }

    /**
     * Define Swagger Parameter attributes.
     *
     * @param parameter
     * @param annotations
     */
    private void defineAttributes(Parameter parameter, Annotation[] annotations) {

        for (Annotation annotation : annotations) {

            Class<? extends Annotation> clazz = annotation.getClass();

            if (ninja.api.Api.class.isAssignableFrom(clazz)) {
                defineAttributes(parameter, (ninja.api.Api) annotation);
            } else if (Required.class.isAssignableFrom(clazz)) {
                parameter.setRequired(true);
            } else if (NotNull.class.isAssignableFrom(clazz)) {
                parameter.setRequired(true);
            }

        }

        // define the DataType attributes
        defineAttributes((DataType) parameter, annotations);

    }

    /**
     * Define Swagger Data Type attributes.
     *
     * @param dataType
     * @param annotations
     */
    private void defineAttributes(DataType dataType, Annotation[] annotations) {

        for (Annotation annotation : annotations) {

            Class<? extends Annotation> clazz = annotation.getClass();

            if (Values.class.isAssignableFrom(clazz)) {
                defineAttributes(dataType, (Values) annotation);
            } else if (NumberValue.class.isAssignableFrom(clazz)) {
                defineAttributes(dataType, (NumberValue) annotation);
            } else if (Length.class.isAssignableFrom(clazz)) {
                defineAttributes(dataType, (Length) annotation);
            } else if (Size.class.isAssignableFrom(clazz)) {
                defineAttributes(dataType, (Size) annotation);
            } else if (Min.class.isAssignableFrom(clazz)) {
                defineAttributes(dataType, (Min) annotation);
            } else if (Max.class.isAssignableFrom(clazz)) {
                defineAttributes(dataType, (Max) annotation);
            } else if (DecimalMin.class.isAssignableFrom(clazz)) {
                defineAttributes(dataType, (DecimalMin) annotation);
            } else if (DecimalMax.class.isAssignableFrom(clazz)) {
                defineAttributes(dataType, (DecimalMax) annotation);
            }

        }

    }

    /**
     * Defines attributes of a Swagger Operation Parameter from
     * {@link ninja.swagger.ApiParam}
     *
     * @param param
     * @param spec
     */
    private void defineAttributes(Parameter parameter, ninja.api.Api spec) {
        parameter.setDescription(spec.value());
    }

    /**
     * Defines attributes of a Swagger Data Type from {@link ninja.api.Values}
     *
     * @param dataType
     * @param spec
     */
    private void defineAttributes(DataType dataType, Values spec) {
        dataType.setDefaultValue(spec.defaultValue());
        dataType.setAllowedValues(spec.allowedValues());
    }

    /**
     * Defines allowable values of a Swagger Property from
     * {@link javax.validation.constraints.Size}
     *
     * @param dataType
     * @param spec
     */
    private void defineAttributes(DataType dataType, Size spec) {
        dataType.setMinimum(String.valueOf(spec.min()));
        dataType.setMaximum(String.valueOf(spec.max()));
    }

    /**
     * Defines allowable values of a Swagger Property from
     * {@link ninja.validation.NumberValue}
     *
     * @param dataType
     * @param spec
     */
    private void defineAttributes(DataType dataType, NumberValue spec) {
        dataType.setMinimum(String.valueOf(spec.min()));
        dataType.setMaximum(String.valueOf(spec.max()));
    }

    /**
     * Defines allowable values of a Swagger Property from
     * {@link ninja.validation.Length}
     *
     * @param dataType
     * @param spec
     */
    private void defineAttributes(DataType dataType, Length spec) {
        dataType.setMinimum(String.valueOf(spec.min()));
        dataType.setMaximum(String.valueOf(spec.max()));
    }

    /**
     * Defines allowable values of a Swagger Data Type from
     * {@link javax.validation.constraints.Min}
     *
     * @param dataType
     * @param spec
     */
    private void defineAttributes(DataType dataType, Min spec) {
        dataType.setMinimum(String.valueOf(spec.value()));
    }

    /**
     * Defines allowable values of a Swagger Data Type from
     * {@link javax.validation.constraints.Max}
     *
     * @param dataType
     * @param spec
     */
    private void defineAttributes(DataType dataType, Max spec) {
        dataType.setMaximum(String.valueOf(spec.value()));
    }

    /**
     * Defines allowable values of a Swagger Data Type from
     * {@link javax.validation.constraints.DecimalMin}
     *
     * @param dataType
     * @param spec
     */
    private void defineAttributes(DataType dataType, DecimalMin spec) {
        dataType.setMinimum(String.valueOf(spec.value()));
    }

    /**
     * Defines allowable values of a Swagger Data Type from
     * {@link javax.validation.constraints.DecimalMax}
     *
     * @param dataType
     * @param spec
     */
    private void defineAttributes(DataType dataType, DecimalMax spec) {
        dataType.setMaximum(String.valueOf(spec.value()));
    }

    /**
     * Declare a Swagger Model from the specified class.
     *
     * @param apiDeclaration
     * @param modelClass
     */
    private void declareModel(ApiDeclaration apiDeclaration, Class<?> modelClass) {
        // this method is called recursively, ensure we declare a type once
        final String id = getModelId(modelClass);
        if (apiDeclaration.hasModel(id)) {
            return;
        }

        logger.debug("declaring api model class {}", modelClass.getName());

        // add a model reference
        final Model model = apiDeclaration.addModel(id);

        // document any exposed model properties
        for (Field field : modelClass.getDeclaredFields()) {

            if (!field.isAnnotationPresent(ninja.api.Api.class)) {
                // not an api field
                continue;
            }

            if (field.isAnnotationPresent(Hidden.class)) {
                // undocumented field, skip
                continue;
            }

            final ninja.api.Api spec = field.getAnnotation(ninja.api.Api.class);
            final boolean required = field.isAnnotationPresent(Required.class)
                    || field.isAnnotationPresent(NotNull.class);

            final Property property = model.addProperty(field.getName(),
                    required);

            property.setDescription(spec.value());

            final Class<?> fieldType = field.getType();
            final boolean isUnique = false; // XXX

            try {
                Class<?> submodelClass = setModelType(property, fieldType,
                        isUnique);

                // declare the submodel class
                if (submodelClass != null) {
                    declareModel(apiDeclaration, submodelClass);
                }

                // define data type attributes from annotations
                defineAttributes(property, field.getAnnotations());
            } catch (Exception e) {
                logger.error(String.format(
                        "Failed to declare model field %s.%s (%s)",
                        modelClass.getName(), field.getName(),
                        fieldType.getName()), e);
            }
        }
    }

    /**
     * Determine if the parameter represents a Body parameter by the absence of
     * an explicit argument extractor.
     *
     * @param annotations
     * @return true if this is a body parameter
     */
    private boolean isBodyParameter(Set<Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return true;
        }
        for (Annotation annotation : annotations) {
            if (annotation.getClass().isAnnotationPresent(
                    WithArgumentExtractor.class)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if this class contains multiple values.
     *
     * @param clazz
     * @return true if this class contains multiple values
     */
    private boolean isMultiple(Class<?> clazz) {
        if (clazz.isArray()) {
            return true;
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    "Collections are not supported! Try declaring Object[] instead.");
        }

        return clazz.isArray();
    }

    /**
     * Sets the type information for the model class into the DataType.
     *
     * @param dataType
     * @param clazz
     * @param isUnique
     * @return the modelClass if the DataType is ModelType, otherwise null
     */
    private Class<?> setModelType(final DataType dataType,
                                  final Class<?> clazz,
                                  final boolean isUnique) {

        final boolean isMultiple = isMultiple(clazz);

        final Class<?> modelClass;
        if (isMultiple) {
            modelClass = clazz.getComponentType();
        } else {
            modelClass = clazz;
        }

        final String swaggerType = Utils.determineSwaggerType(modelClass);
        final String swaggerFormat = Utils.determineSwaggerFormat(modelClass);

        if (isMultiple) {
            // Multiple values
            dataType.setType(Types.Array);
            dataType.setUniqueItems(isUnique);

            if (modelClass.isEnum()) {
                // Define the allowed values for the Enum
                Enum<?>[] values = (Enum[]) modelClass.getEnumConstants();
                String[] names = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    names[i] = values[i].name();
                }

                dataType.setItems(Items.typeOf(Types.String));
                dataType.setAllowedValues(names);

            } else if (Types.Model == swaggerType) {
                // Model Type: use $ref for linking to models
                final String modelId = getModelId(modelClass);
                dataType.setItems(Items.refOf(modelId));

                return modelClass;
            } else {
                // Primitive Type
                dataType.setItems(Items.typeOf(swaggerType));
                dataType.setFormat(swaggerFormat);
            }

        } else {
            // Single value
            if (modelClass.isEnum()) {

                // Define the allowed values for the Enum
                Enum<?>[] values = (Enum[]) modelClass.getEnumConstants();
                String[] names = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    names[i] = values[i].name();
                }

                dataType.setType(Types.String);
                dataType.setAllowedValues(names);

            } else if (Types.Model == swaggerType) {
                // Model Type: use $ref for linking to models
                final String modelId = getModelId(modelClass);
                dataType.setRef(modelId);
                dataType.setType(modelId);

                return modelClass;
            } else {
                // Primitive Type
                dataType.setType(swaggerType);
                dataType.setFormat(swaggerFormat);
            }
        }

        return null;
    }

    /**
     * Returns the Class<?> which is properly annotated with Api annotations.
     *
     * @param route
     * @return the specification class or null
     */
    private Class<?> getSpecificationClass(Route route) {
        Set<Class<?>> classes = Sets.newHashSet();
        classes.add(route.getControllerClass());
        classes.add(route.getControllerClass().getSuperclass());
        for (Class<?> clazz : route.getControllerClass().getInterfaces()) {
            classes.add(clazz);
        }

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(ninja.api.Api.class)) {
                return clazz;
            }
        }

        return null;
    }

    /**
     * Returns the Method which is properly annotated with Api annotations.
     *
     * @param specificationClass
     * @param route
     * @return the specification method or null
     */
    private Method getSpecificationMethod(Class<?> specificationClass,
                                          Route route) {

        final Method implementationMethod = route.getControllerMethod();

        if (specificationClass == route.getControllerClass()) {
            // specification & implementation are the same class
            if (implementationMethod.isAnnotationPresent(ninja.api.Api.class)
                    || implementationMethod.isAnnotationPresent(Form.class)) {
                return implementationMethod;
            }

            return null;
        }

        // specification & implementation are separate classes
        for (Method method : specificationClass.getMethods()) {

            if (method.isAnnotationPresent(ninja.api.Api.class)
                    || method.isAnnotationPresent(Form.class)) {

                if (method.getName().equals(implementationMethod.getName())) {
                    if (Arrays.equals(method.getParameterTypes(),
                            implementationMethod.getParameterTypes())) {
                        return method;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the ID of the model class. Defaults to the complete classname if
     * the name is unspecified.
     *
     * @param clazz
     * @return the model id
     */
    private String getModelId(Class<?> clazz) {
        if (clazz.isAnnotationPresent(ninja.api.Api.class)) {
            ninja.api.Api model = clazz.getAnnotation(ninja.api.Api.class);
            if (!model.name().isEmpty()) {
                return model.name();
            }
        }
        return clazz.getName();
    }

    /**
     * Flags the operation as deprecated if one of the methods is so annotated.
     *
     * @param op
     * @param specificationMethod
     * @param implementationMethod
     */
    private void declareDeprecated(Operation op,
                                   Method specificationMethod,
                                   Method implementationMethod) {
        if (specificationMethod.isAnnotationPresent(Deprecated.class)) {
            op.setDeprecated(true);
        } else if (implementationMethod.isAnnotationPresent(Deprecated.class)) {
            op.setDeprecated(true);
        }

    }

    private static class RouteSpec {
        final String baseUrl;
        final Class<?> specificationClass;
        final Method specificationMethod;
        final Route route;

        RouteSpec(String baseUrl,
                  Class<?> specificationClass,
                  Method specificationMethod,
                  Route route) {
            this.baseUrl = baseUrl;
            this.specificationClass = specificationClass;
            this.specificationMethod = specificationMethod;
            this.route = route;
        }
    }
}
