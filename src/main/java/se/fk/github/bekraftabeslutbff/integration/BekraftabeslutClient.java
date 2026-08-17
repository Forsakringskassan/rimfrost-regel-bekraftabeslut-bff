package se.fk.github.bekraftabeslutbff.integration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.BekraftaBeslutControllerApi;
import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.DefaultApi;

@RegisterRestClient(configKey = "bekraftabeslut")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BekraftabeslutClient extends BekraftaBeslutControllerApi, DefaultApi
{
   @GET
   @Path("/regel/bekraftabeslut/utokadUppgiftsbeskrivning")
   Object getUppgiftsbeskrivning();

   @POST
   @Path("/regel/bekraftabeslut/{handlaggningId}/done")
   Response postDone(
         @PathParam("handlaggningId") String handlaggningId,
         @HeaderParam("Authorization") String authorization);
}
