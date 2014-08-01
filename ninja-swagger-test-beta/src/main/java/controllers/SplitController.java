package controllers;

import ninja.Result;
import ninja.api.Api;
import ninja.api.Hidden;
import ninja.api.Json;
import ninja.api.Returns;
import etc.Order;
import etc.OrderStatus;

@Api(name="split", value="Api specification split from implementation")
@Json
public interface SplitController {

	@Api("List orders")
	@Returns(Order[].class)
	public Result getOrders(

			@Api("The status to include in results")
			OrderStatus status,

			@Api("The page number to fetch")
			int page,

			@Api("The number of orders per page")
			int size);

	@Api("Create an order")
	public Result createOrder(

			@Api("The order to create")
			Order order);

	@Api("Delete an order by ID")
	public Result deleteOrder(

			@Api("The ID of the order to delete")
			String id);

	@Api("Demonstrates header injection")
	public Result showHeaders(
			@Hidden
			String userAgent,

			@Api("The secret key")
			String secretKey,

			@Api("An integer value")
			int counter);

}