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

import ninja.Result;
import ninja.Results;
import ninja.api.Api;
import ninja.api.Hidden;
import ninja.params.Header;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.validation.Required;

import com.google.inject.Singleton;

import etc.MinimalOrder;
import etc.OrderStatus;

/**
 * Demonstration of the minimum annotations required to generate API documentation.
 *
 * @author James Moger
 *
 */
@Singleton
@Api
public class MinimalController {

	@Api
	public Result getOrders(
			@Param("status")
			OrderStatus status,

			@Param("pg")
			int page,

			@Param("sz")
			int size) {

		Result result = Results.ok();
		return result;

	}

	@Api
	public Result createOrder(MinimalOrder order) {

		return Results.ok();
	}

	@Api
	public Result deleteOrder(
			@PathParam("orderId")
			String id) {

		return Results.badRequest().render("message", "Invalid name, try 'test'");
	}

	@Api
	public Result showHeaders(
			@Header("user-agent")
			@Hidden
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
