package se.fk.github.bekraftabeslutbff.integration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.github.bekraftabeslutbff.model.BackendPatchRequest;
import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.model.GetDataResponse;

@RegisterRestClient(configKey = "bekraftabeslut")
@Path("/regel/bekraftabeslut")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BekraftabeslutClient
{

   @GET
   @Path("/{path}")
   Object getReferensdata(@PathParam("path") String path);

   @GET
   @Path("/{handlaggningId}")
   GetDataResponse getBekraftabeslut(@PathParam("handlaggningId") String handlaggningId);

   @PATCH
   @Path("/{handlaggningId}")
   Response patchBekraftabeslut(
         @PathParam("handlaggningId") String handlaggningId,
         BackendPatchRequest body);

   @GET
   @Path("/utokadUppgiftsbeskrivning")
   Object getUppgiftsbeskrivning();

   @POST
   @Path("/{handlaggningId}/done")
   Response postDone(
         @PathParam("handlaggningId") String handlaggningId,
         @HeaderParam("Authorization") String authorization);
}
