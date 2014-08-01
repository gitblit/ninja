package etc;

import ninja.api.Api;
import ninja.api.Values;

@Api(name="User")
public class User {

	@Api
	long id;

	@Api
	String firstName;

	@Api
	String lastName;

	@Api
	String username;

	@Api
	String email;

	@Api
	String password;

	@Api
	String phone;

	@Api("User Status")
	@Values(allowedValues={"1-registered", "2-active", "3-closed"})
	int userStatus;
}
