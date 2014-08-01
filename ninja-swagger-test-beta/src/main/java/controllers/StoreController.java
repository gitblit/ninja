package controllers;

import javax.validation.constraints.NotNull;

import ninja.Result;
import ninja.Results;
import ninja.api.Api;
import ninja.api.ResponseCode;
import ninja.api.Json;
import ninja.api.Notes;
import ninja.api.ResponseCodes;
import ninja.api.Returns;
import ninja.api.Version;
import ninja.params.PathParam;
import etc.Order;

@Api(name="store", value="Operations about store")
@Version("1.0.2")
@Json
public class StoreController {

	@Api("Delete purchase order by ID")
	@Notes("For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers will generate API errors")
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid ID supplied"),
		@ResponseCode(code=404, message="Order not found"),
		})
	public Result deleteOrder(
			@Api("ID of the order that needs to be deleted")
			@PathParam("orderId")
			@NotNull
			String orderId) {

		return Results.ok();
	}

	@Api("Find purchase order by ID")
	@Notes("For valid response try integer IDs with value <= 5. Anything above 5 or nonintegers will generate API errors")
	@Returns(Order.class)
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid ID supplied"),
		@ResponseCode(code=404, message="Order not found"),
		})
	public Result getOrderById(
			@Api("ID of the order that needs to be fetched")
			@PathParam("orderId")
			@NotNull
			String orderId) {

		return Results.ok();
	}

	@Api("Place an order for a pet")
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid order"),
		})
	public Result placeOrder(
			@Api("Order placed for purchasing the pet")
			Order order) {

		return Results.ok();
	}

}
