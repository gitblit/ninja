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
import ninja.params.Param;
import ninja.params.PathParam;
import etc.User;

@Api(name="user", value="Operations about users")
@Version("1.0.1")
@Json
public class UserController {

	@Api("Creates users with given input array")
	public Result createUsersWithArrayInput(
			@Api("List of User object")
			User [] user) {

		return Results.ok();

	}

	@Api("Update user")
	@Notes("This can only be done by the logged in user.")
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid username supplied"),
		@ResponseCode(code=404, message="User not found"),
		})
	public Result updateUser(
			@Api("Username of User to be updated")
			@PathParam("username")
			@NotNull
			String username,

			@Api("Updated User object")
			User user) {

		return Results.ok();
	}

	@Api("Delete user")
	@Notes("This can only be done by the logged in user.")
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid username supplied"),
		@ResponseCode(code=404, message="User not found"),
		})
	public Result deleteUser(
			@Api("Username of User to be deleted")
			@PathParam("username")
			@NotNull
			String username) {

		return Results.ok();
	}

	@Api("Get user by username")
	@Returns(User.class)
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid username supplied"),
		@ResponseCode(code=404, message="User not found"),
		})
	public Result getUserByName(
			@Api("Username of User to be fetched. Use user1 for testing.")
			@PathParam("username")
			@NotNull
			String username) {

		return Results.ok();
	}

	@Api("Logs user into system")
	@Returns(String.class)
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid username and password combination"),
		})
	public Result loginUser(
			@Api("The Username for login")
			@Param("username")
			@NotNull
			String username,

			@Api("The password for login in clear text")
			@Param("password")
			@NotNull
			String password) {


		return Results.ok();
	}

	@Api("Logs out current logged in user session")
	public Result logoutUser() {

		return Results.ok();
	}

	@Api("Create user")
	@Notes("This can only be done by the logged in user.")
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid username supplied"),
		@ResponseCode(code=404, message="User not found"),
		})
	public Result createUser(
			@Api("User object to create")
			User user) {

		return Results.ok();
	}

}
