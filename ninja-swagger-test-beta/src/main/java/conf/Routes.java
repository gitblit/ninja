/**
 * Copyright (C) 2012 the original author or authors.
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

package conf;


import ninja.Router;
import ninja.api.ApiInfo;
import ninja.api.Version;
import ninja.application.ApplicationRoutes;
import ninja.swagger.SwaggerRouter;
import router.AnnotationRouter;

import com.google.inject.Inject;

import controllers.BasicAuthController;
import controllers.MinimalController;
import controllers.PetController;
import controllers.StoreController;
import controllers.UserController;

@ApiInfo(
        title="Swagger Sample App",
        description="This is a Ninja-based example of the Swagger Pet Store. It is not really functional.",
        license="Apache 2.0",
        licenseUrl="http://www.apache.org/licenses/LICENSE-2.0.html")
@Version("1.0.0")
public class Routes implements ApplicationRoutes {

	@Inject
	AnnotationRouter annotationRouter;

	@Inject
	SwaggerRouter swaggerRouter;

    @Override
    public void init(Router router) {

        ///////////////////////////////////////////////////////////////////////
        // Operations about pets
        ///////////////////////////////////////////////////////////////////////
        router.PUT().route("/pet").with(PetController.class, "updatePet");
        router.POST().route("/pet").with(PetController.class, "addPet");
        router.GET().route("/pet/findByStatus").with(PetController.class, "findPetsByStatus");
        router.GET().route("/pet/findByTags").with(PetController.class, "findPetsByTag");
        router.METHOD("PATCH").route("/pet/{petId}").with(PetController.class, "partialUpdate");
        router.POST().route("/pet/{petId}").with(PetController.class, "updatePetWithForm");
        router.DELETE().route("/pet/{petId}").with(PetController.class, "deletePet");
        router.GET().route("/pet/{petId}").with(PetController.class, "getPetById");
        router.POST().route("/pet/uploadImage").with(PetController.class, "uploadFile");


        ///////////////////////////////////////////////////////////////////////
        // Operations about users
        ///////////////////////////////////////////////////////////////////////
        router.POST().route("/user/createWithArray").with(UserController.class, "createUsersWithArrayInput");
        router.PUT().route("/user/{username}").with(UserController.class, "updateUser");
        router.DELETE().route("/user/{username}").with(UserController.class, "deleteUser");
        router.GET().route("/user/{username}").with(UserController.class, "getUserByName");
        router.GET().route("/user/login").with(UserController.class, "loginUser");
        router.GET().route("/user/logout").with(UserController.class, "logoutUser");
        router.POST().route("/user").with(UserController.class, "createUser");


        ///////////////////////////////////////////////////////////////////////
        // Operations about store
        ///////////////////////////////////////////////////////////////////////
        router.DELETE().route("/store/order/{orderId}").with(StoreController.class, "deleteOrder");
        router.GET().route("/store/order/{orderId}").with(StoreController.class, "getOrderById");
        router.POST().route("/store/order").with(StoreController.class, "placeOrder");


        ///////////////////////////////////////////////////////////////////////
        // Minimal Controller example
        ///////////////////////////////////////////////////////////////////////
        router.GET().route("/minimal/orders").with(MinimalController.class, "getOrders");
        router.POST().route("/minimal/order").with(MinimalController.class, "createOrder");
        router.DELETE().route("/minimal/order/{orderId}").with(MinimalController.class, "deleteOrder");
        router.GET().route("/minimal/headers").with(MinimalController.class, "showHeaders");


        ///////////////////////////////////////////////////////////////////////
        // Split Controller example (annotated)
        ///////////////////////////////////////////////////////////////////////

        annotationRouter.init(router);

        ///////////////////////////////////////////////////////////////////////
        // Basic Authentication Controller example
        ///////////////////////////////////////////////////////////////////////
        router.GET().route("/basic/orders").with(BasicAuthController.class, "getOrders");
        router.POST().route("/basic/order").with(BasicAuthController.class, "createOrder");
        router.DELETE().route("/basic/order/{orderId}").with(BasicAuthController.class, "deleteOrder");
        router.GET().route("/basic/headers").with(BasicAuthController.class, "showHeaders");


        ///////////////////////////////////////////////////////////////////////
        // Swagger
        ///////////////////////////////////////////////////////////////////////
        swaggerRouter.setPath("/");
        swaggerRouter.init(router);

    }

}
