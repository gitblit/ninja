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

import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.utils.HttpCacheToolkit;
import ninja.utils.MimeTypes;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This controller serves Swagger documents from a memory cache and the
 * Swagger-UI.
 *
 * @author James Moger
 *
 */
@Singleton
public class SwaggerController {

    public final static String DOCUMENT_PARAM = "doc";

    private final SwaggerService swaggerService;

    private final HttpCacheToolkit httpCacheToolkit;

    private final MimeTypes mimeTypes;

    private final String bannerTitle;

    private String uiPath;

    private String pathPrefix;

    private boolean showApiKey;

    @Inject
    public SwaggerController(SwaggerService swaggerService,
                             HttpCacheToolkit httpCacheToolkit,
                             MimeTypes mimeTypes,
                             NinjaProperties ninjaProperties) {

        this.swaggerService = swaggerService;
        this.httpCacheToolkit = httpCacheToolkit;
        this.mimeTypes = mimeTypes;

        this.bannerTitle = ninjaProperties.getWithDefault(
                NinjaConstant.applicationName, "swagger");
        this.uiPath = "/api";
        this.pathPrefix = uiPath;
    }

    /**
     * Sets the path to the Swagger UI.
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

    public Result serveUI() {

        Result result = Results.html();
        result.template("views/swagger/api-docs.ftl.html");
        result.render("bannerTitle", bannerTitle);
        result.render("apiUiPath", uiPath);
        result.render("apiDocsPath", pathPrefix + "/api-docs");
        result.render("webjarsPath", pathPrefix + "/webjars");
        result.render("showApiKey", showApiKey);

        return result;
    }

    public Result serveDoc(Context context) {
        String documentName = getDocumentNameFromRequest(context);

        if (documentName.isEmpty() || documentName.equals(".")) {
            documentName = "api-docs.json";
        }

        String extension = Files.getFileExtension(documentName);
        if (extension.isEmpty()) {
            // default to json
            documentName += ".json";
        }

        final String finalDocumentName = documentName;

        String document = swaggerService.getDocument(finalDocumentName);
        if (document == null) {
            return Results.notFound();
        }

        Result result = Results.ok();

        httpCacheToolkit
                .addEtag(context, result, swaggerService.getStartTime());

        final String finalExtension = Files.getFileExtension(finalDocumentName);
        if ("json".equals(finalExtension)) {
            result.json();
        } else if ("xml".equals(finalExtension)) {
            result.xml();
        } else {
            String mimeType = mimeTypes.getContentType(context,
                    finalDocumentName);
            if (mimeType != null && !mimeType.isEmpty()) {
                result.contentType(mimeType);
            }
        }

        return result.renderRaw(document);

    }

    private String getDocumentNameFromRequest(Context context) {

        String documentName = context.getPathParameter(DOCUMENT_PARAM);

        if (documentName == null) {
            documentName = context.getRequestPath();
        }

        String documentNameNormalized = Files.simplifyPath(documentName);

        if (documentNameNormalized.charAt(0) == '/') {
            return documentNameNormalized.substring(1);
        }

        return documentNameNormalized;

    }
}