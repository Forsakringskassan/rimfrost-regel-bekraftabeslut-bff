package se.fk.github.bekraftabeslutbff;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.fk.github.bekraftabeslutbff.integration.BekraftabeslutClient;
import se.fk.github.bekraftabeslutbff.model.*;

import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BekraftabeslutController
{

   private static final Logger LOGGER = LoggerFactory.getLogger(BekraftabeslutController.class);

   @Inject
   @RestClient
   BekraftabeslutClient bekraftabeslutClient;

   // GET /api/health
   @GET
   @Path("/api/health")
   public Response health()
   {
      return Response.ok(Map.of("status", "ok")).build();
   }

   // GET /api/regel/bekraftabeslut/{path} - referensdata (avslutstyp, beslutstyp, beslutsutfallstyp, yrkandestatus)
   @GET
   @Path("/api/regel/bekraftabeslut/{path}")
   public Response getReferensdata(@PathParam("path") String path)
   {
      LOGGER.debug("GET /api/regel/bekraftabeslut/{}", path);
      try
      {
         Object result = bekraftabeslutClient.getReferensdata(path);
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         LOGGER.error("Failed to fetch referensdata path={}, upstream status={}", path, e.getResponse().getStatus(), e);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to fetch referensdata path={}, backend unreachable", path, e);
         return Response.status(502).entity(Map.of("error", "Backend unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to fetch referensdata path={}", path, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
   }

   // POST /api/regel/bekraftabeslut - fetch decision data
   @POST
   @Path("/api/regel/bekraftabeslut")
   public Response getBekraftabeslut(@Valid BekraftabeslutRequest body)
   {
      MDC.put("handlaggningId", body.handlaggningId);
      try
      {
         RawBekraftabeslutResponse raw = bekraftabeslutClient.getBekraftabeslut(body.handlaggningId);
         BekraftabeslutResponse result = BekraftabeslutMapper.transform(raw);
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         LOGGER.error("Failed to fetch bekraftabeslut for handlaggningId={}, upstream status={}", body.handlaggningId,
               e.getResponse().getStatus(), e);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to fetch bekraftabeslut for handlaggningId={}, backend unreachable", body.handlaggningId, e);
         return Response.status(502).entity(Map.of("error", "Backend unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to fetch bekraftabeslut for handlaggningId={}", body.handlaggningId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
      finally
      {
         MDC.remove("handlaggningId");
      }
   }

   // PATCH /api/regel/bekraftabeslut/{handlaggningId} - confirm decision
   @PATCH
   @Path("/api/regel/bekraftabeslut/{handlaggningId}")
   public Response patchBekraftabeslut(@PathParam("handlaggningId") String handlaggningId, @Valid PatchRequest body)
   {
      MDC.put("handlaggningId", handlaggningId);
      try
      {
         Response backendResponse = bekraftabeslutClient.patchBekraftabeslut(handlaggningId, body);
         return Response.status(backendResponse.getStatus()).build();
      }
      catch (WebApplicationException e)
      {
         LOGGER.error("Failed to patch bekraftabeslut for handlaggningId={}, upstream status={}", handlaggningId,
               e.getResponse().getStatus(), e);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to patch bekraftabeslut for handlaggningId={}, backend unreachable", handlaggningId, e);
         return Response.status(502).entity(Map.of("error", "Backend unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to patch bekraftabeslut for handlaggningId={}", handlaggningId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
      finally
      {
         MDC.remove("handlaggningId");
      }
   }

   // GET /api/uppgiftsbeskrivning
   @GET
   @Path("/api/uppgiftsbeskrivning")
   public Response getUppgiftsbeskrivning()
   {
      LOGGER.debug("GET /api/uppgiftsbeskrivning");
      try
      {
         Object result = bekraftabeslutClient.getUppgiftsbeskrivning();
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         LOGGER.error("Failed to fetch uppgiftsbeskrivning, upstream status={}", e.getResponse().getStatus(), e);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Failed to fetch from backend")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to fetch uppgiftsbeskrivning, backend unreachable", e);
         return Response.status(502).entity(Map.of("error", "Backend service unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to fetch uppgiftsbeskrivning", e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
   }

   // POST /api/regel/bekraftabeslut/done
   @POST
   @Path("/api/regel/bekraftabeslut/done")
   public Response done(@Valid BekraftabeslutRequest body, @HeaderParam("Authorization") String authorization)
   {
      MDC.put("handlaggningId", body.handlaggningId);
      try
      {
         bekraftabeslutClient.postDone(body.handlaggningId, authorization);
         return Response.status(204).build();
      }
      catch (WebApplicationException e)
      {
         LOGGER.error("Failed to call /done for handlaggningId={}, upstream status={}", body.handlaggningId,
               e.getResponse().getStatus(), e);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to call /done for handlaggningId={}, backend unreachable", body.handlaggningId, e);
         return Response.status(502).entity(Map.of("error", "Backend unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to call /done for handlaggningId={}", body.handlaggningId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
      finally
      {
         MDC.remove("handlaggningId");
      }
   }
}
