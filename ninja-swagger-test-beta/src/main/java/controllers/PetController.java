package controllers;

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import ninja.ContentTypes;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.api.Api;
import ninja.api.Values;
import ninja.api.ResponseCode;
import ninja.api.Form;
import ninja.api.FormField;
import ninja.api.Json;
import ninja.api.Notes;
import ninja.api.Produces;
import ninja.api.ResponseCodes;
import ninja.api.Returns;
import ninja.api.Xml;
import ninja.params.Params;
import ninja.params.PathParam;
import etc.Pet;
import etc.PetStatus;

@Api(name="pet", value="Operations about pets")
@Json @Xml
public class PetController {

	@Api("Update an existing pet")
	@ResponseCodes({
			@ResponseCode(code=400, message="Invalid ID supplied"),
			@ResponseCode(code=404, message="Pet not found"),
			@ResponseCode(code=405, message="Validation exception"),
	})
	public Result updatePet(
			@Api("Pet object that needs to be updated in the store")
			Pet pet) {

		return Results.ok();
	}

	@Api("Add a new pet to the store")
	@ResponseCodes(@ResponseCode(code=405, message="Invalid input"))
	public Result addPet(
			@Api("Pet object that needs to be added to the store")
			Pet pet) {

		return Results.ok();
	}

	@Api("Finds pets by status")
	@Notes("Multiple status values may be provided by specifying multiple 'status' query parameters")
	@Returns(Pet[].class)
	@ResponseCodes(@ResponseCode(code=400, message="Invalid status value"))
	public Result findPetsByStatus(
			@Api("Status values that need to be considered for filter")
			@Params("status")
			@NotNull
			PetStatus[] status) {

		return Results.ok().renderRaw(Arrays.asList(status).toString());
	}

	@Api("Finds pets by tags")
	@Notes("Multiple tags may be provided by specifying  multiple 'tag' query parameters")
	@Returns(Pet[].class)
	@ResponseCodes(@ResponseCode(code=400, message="Invalid tag value"))
	public Result findPetsByTag(
			@Api("Tags to filter by")
			@Params("tag")
			@NotNull
			String[] tags) {

		return Results.ok().renderRaw(Arrays.asList(tags).toString());
	}

	@Api("Partial updates to a pet")
	@Returns(Pet.class)
	@ResponseCodes(@ResponseCode(code=400, message="Invalid tag value"))
	public Result partialUpdate(
			@Api("ID of pet that needs to be patched")
			@PathParam("petId")
			String petId,

			@Api("Pet object that needs to be partially updated")
			Pet pet) {

		return Results.ok();
	}

	@Api("Updates a pet in the store with form data")
	@Form({
		@FormField(name="name", description="Updated name of the pet"),
		@FormField(name="status", description="Updated status of the pet"),
	})
	@ResponseCodes(@ResponseCode(code=405, message="Invalid input"))
	public Result updatePetWithForm(
			@Api("ID of pet that needs to be updated")
			@PathParam("petId")
			String petId,

			Context context) {

		return Results.ok();
	}

	@Api("Deletes a pet")
	@ResponseCodes(@ResponseCode(code=400, message="Invalid pet value"))
	public Result deletePet(
			@Api("Pet id to delete")
			@PathParam("petId")
			String petId) {

		return Results.ok();
	}

	@Api("Finds pet by ID")
	@Produces({ContentTypes.APPLICATION_JSON, ContentTypes.APPLICATION_XML, "text/plain", ContentTypes.TEXT_HTML})
	@Notes("Returns a pet based on ID")
	@Returns(Pet.class)
	@ResponseCodes({
		@ResponseCode(code=400, message="Invalid Id supplied value"),
		@ResponseCode(code=404, message="Pet not found")
	})
	public Result getPetById(
			@Api("ID of pet that needs to be fetched")
			@Values(defaultValue="5")
			@PathParam("petId")
			int petId) {

		return Results.ok();
	}

	@Api("uploads an image")
	@Form({
		@FormField(name="additionalMetadata", description="Additional data to pass to server"),
		@FormField(name="file", typeOf="File", description="file to upload"),
	})
	public Result uploadFile(Context context) {

		return Results.ok();
	}

}
