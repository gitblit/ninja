/**
 * Copyright (C) 2014 the original author or authors.
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

package controllers;

import ninja.BasicAuthFilter;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.api.Api;
import ninja.api.Hidden;
import ninja.api.Notes;
import ninja.api.Returns;
import ninja.params.Header;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.validation.Required;

import com.google.inject.Singleton;

import etc.Order;
import etc.OrderStatus;

/**
 * Demonstration of controller that requires authentication.
 *
 * @author James Moger
 *
 */
@Singleton
@Api(name="basic", value="Basic Authentication demo")
@FilterWith(BasicAuthFilter.class)
public class BasicAuthController {

	@Api("List orders")
	@Notes("Login credentials are 'demo:demo'. Use a private browsing window.")
	@Returns(Order[].class)
	public Result getOrders(

			@Api("The status to include in results")
			@Param("status")
			OrderStatus status,

			@Api("The page number to fetch")
			@Param("pg")
			int page,

			@Api("The number of orders per page")
			@Param("sz")
			int size) {

		Result result = Results.ok();
		return result;

	}

	@Api("Create an order")
	@Notes("Login credentials are 'demo:demo'. Use a private browsing window.")
	public Result createOrder(
			@Api("The order to create")
			Order order) {

		return Results.ok();
	}

	@Api("Delete an order by ID")
	@Notes("Login credentials are 'demo:demo'. Use a private browsing window.")
	public Result deleteOrder(
			@Api("The ID of the order to delete")
			@PathParam("orderId")
			String id) {

		return Results.badRequest().render("message", "Invalid name, try 'test'");
	}

	@Api("Demonstrates header injection")
	@Notes("Login credentials are 'demo:demo'. Use a private browsing window.")
	public Result showHeaders(
			@Header("user-agent")
			@Hidden
			String userAgent,

			@Api("The secret key")
			@Header("secret-key")
			@Required
			String secretKey,

			@Api("An integer value")
			@Header("counter")
			int counter) {

		Result result = Results.ok()
				.render("user-agent", userAgent)
				.render("secret-key", secretKey)
				.render("counter", counter);
		return result;
	}
}
