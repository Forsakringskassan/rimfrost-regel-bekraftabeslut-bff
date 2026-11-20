package se.fk.github.bekraftabeslutbff.integration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.github.bekraftabeslutbff.model.BackendGetResponse;
import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.BekraftaBeslutControllerApi;
import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.DefaultApi;

@RegisterRestClient(configKey = "bekraftabeslut")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BekraftabeslutClient extends BekraftaBeslutControllerApi, DefaultApi
{
   /** Hämtar beslutsunderlag inkl. informationsaccess-wrapper (BEKB-FR-07). */
   @GET
   @Path("/regel/bekraftabeslut/{handlaggningId}")
   BackendGetResponse getHandlaggning(@PathParam("handlaggningId") UUID handlaggningId);

   @GET
   @Path("/regel/bekraftabeslut/utokadUppgiftsbeskrivning")
   Object getUppgiftsbeskrivning();

   @POST
   @Path("/regel/bekraftabeslut/{handlaggningId}/done")
   Response postDone(@PathParam("handlaggningId") String handlaggningId);
}
