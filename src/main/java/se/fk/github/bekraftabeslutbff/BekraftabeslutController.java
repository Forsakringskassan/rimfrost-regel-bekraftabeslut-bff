package se.fk.github.bekraftabeslutbff;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.bff.logging.LogContext;
import se.fk.github.bekraftabeslutbff.integration.BekraftabeslutClient;
import se.fk.github.bekraftabeslutbff.integration.InformationsaccessPublisher;
import se.fk.github.bekraftabeslutbff.model.*;
import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.model.PatchDataRequest;
import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.model.Referensdata;

import java.util.List;
import java.util.UUID;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BekraftabeslutController
{

   private static final Logger LOGGER = LoggerFactory.getLogger(BekraftabeslutController.class);

   @RestClient
   BekraftabeslutClient bekraftabeslutClient;

   @Inject
   InformationsaccessPublisher informationsaccessPublisher;

   // GET /api/regel/bekraftabeslut/{path} - referensdata (avslutstyp, beslutstyp, beslutsutfallstyp, yrkandestatus)
   @GET
   @Path("/api/regel/bekraftabeslut/{path}")
   public Response getReferensdata(@PathParam("path") String path)
   {
      LOGGER.debug("GET /api/regel/bekraftabeslut/{}", path);
      List<Referensdata> result;
      switch (path)
      {
         case "avslutstyp":
            result = bekraftabeslutClient.getAvslutstyp();
            break;
         case "beslutstyp":
            result = bekraftabeslutClient.getBeslutstyp();
            break;
         case "beslutsutfallstyp":
            result = bekraftabeslutClient.getBeslutsutfallstyp();
            break;
         case "yrkandestatus":
            result = bekraftabeslutClient.getYrkandestatus();
            break;
         default:
            return Response.status(404).build();
      }
      return Response.ok(result).build();
   }

   // GET /api/regel/bekraftabeslut/handlaggning/{handlaggningId} - fetch decision data
   @GET
   @Path("/api/regel/bekraftabeslut/handlaggning/{handlaggningId}")
   public Response getBekraftabeslut(@PathParam("handlaggningId") String handlaggningId)
   {
      try (var ignored = LogContext.put("handlaggningId", handlaggningId))
      {
         BackendGetResponse response = bekraftabeslutClient.getHandlaggning(UUID.fromString(handlaggningId));
         // Map before publishing so no access is logged for data never shown (BEKB-FR-07.3)
         informationsaccessPublisher.publish(handlaggningId, response);
         return Response.ok(response.data()).build();
      }
   }

   // PATCH /api/regel/bekraftabeslut/{handlaggningId} - confirm decision
   @PATCH
   @Path("/api/regel/bekraftabeslut/{handlaggningId}")
   public Response patchBekraftabeslut(@PathParam("handlaggningId") String handlaggningId, @Valid PatchRequest body)
   {
      try (var ignored = LogContext.put("handlaggningId", handlaggningId))
      {
         PatchDataRequest backendBody = new PatchDataRequest(
               body.ersattningar(),
               new se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.model.Beslut(
                     body.beslut().avslutstyp(), body.beslut().beslutstyp(), body.beslut().beslutsutfall()));
         bekraftabeslutClient.regelBekraftabeslutHandlaggningIdPatch(UUID.fromString(handlaggningId), backendBody);
         return Response.status(200).build();
      }
   }

   // GET /api/uppgiftsbeskrivning
   @GET
   @Path("/api/uppgiftsbeskrivning")
   public Response getUppgiftsbeskrivning()
   {
      LOGGER.debug("GET /api/uppgiftsbeskrivning");
      Object result = bekraftabeslutClient.getUppgiftsbeskrivning();
      return Response.ok(result).build();
   }

   // POST /api/regel/bekraftabeslut/done
   @POST
   @Path("/api/regel/bekraftabeslut/done")
   public Response done(@Valid BekraftabeslutRequest body)
   {
      try (var ignored = LogContext.put("handlaggningId", body.handlaggningId()))
      {
         bekraftabeslutClient.postDone(body.handlaggningId());
         return Response.status(204).build();
      }
   }
}
