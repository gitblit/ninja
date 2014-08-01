package etc;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ninja.api.Api;
import ninja.api.Required;

@Api(name="Pet")
public class Pet {

	@Api("unique identifier for the pet")
	@Required
	@Min(0)
	@Max(100)
	long id;

	@Api
	Category category;

	@Api
	@NotNull
	String name;

	@Api
	String [] photoUrls;

	@Api
	Tag[] tags;

	@Api("pet status in the store")
	PetStatus status;

}
