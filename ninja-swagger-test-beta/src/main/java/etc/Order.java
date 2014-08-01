package etc;

import java.util.Date;

import ninja.api.Api;

@Api(name="Order")
public class Order {

	@Api
	long id;

	@Api
	long petId;

	@Api
	int quantity;

	@Api("Order Status")
	OrderStatus status;

	@Api
	Date shipDate;

}
