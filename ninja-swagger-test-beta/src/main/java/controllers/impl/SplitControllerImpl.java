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

package controllers.impl;

import javax.validation.constraints.Min;

import ninja.Result;
import ninja.Results;
import ninja.params.Header;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.validation.Required;
import router.DELETE;
import router.GET;
import router.POST;
import router.Path;

import com.google.inject.Singleton;

import controllers.SplitController;
import etc.Order;
import etc.OrderStatus;

/**
 * Demonstration of splitting the controller specification from the controller
 * implementation so you don't clutter your controllers with documentation annotations.
 *
 * @author James Moger
 *
 */
@Singleton
@Path("/split")
public class SplitControllerImpl implements SplitController {

	@Override
	@Path("/orders")
	@GET
	public Result getOrders(
			@Param("status") OrderStatus status,
			@Param("pg") @Min(0) int page,
			@Param("sz") @Min(0) int size) {

		Result result = Results.ok();
		return result;

	}

	@Override
	@Path("/order")
	@POST
	public Result createOrder(Order order) {

		return Results.ok();
	}

	@Override
	@Path("/order/{orderId}")
	@DELETE
	public Result deleteOrder(@PathParam("orderId") String id) {

		return Results.badRequest().render("message", "Invalid name, try 'test'");
	}

	@Override
	@Path("/headers")
	@GET
	public Result showHeaders(
			@Header("user-agent")
			String userAgent,

			@Header("secret-key")
			@Required
			String secretKey,

			@Header("counter")
			int counter) {

		Result result = Results.ok()
				.render("user-agent", userAgent)
				.render("secret-key", secretKey)
				.render("counter", counter);
		return result;
	}
}
