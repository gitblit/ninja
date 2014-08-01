package etc;

import ninja.api.Api;

@Api(name="Category")
public class Category {

	@Api
	long id;

	@Api
	String name;
}
