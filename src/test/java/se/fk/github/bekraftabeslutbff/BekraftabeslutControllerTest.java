package se.fk.github.bekraftabeslutbff;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import java.util.Map;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.informationsaccess.InformationsaccessEvent;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class BekraftabeslutControllerTest
{
   static final String HANDLAGGNING_UUID = "11111111-1111-1111-1111-111111111111";
   private static final String ERSATTNING_UUID = "22222222-2222-2222-2222-222222222222";
   private static final String UPPGIFT_ID = "8a2b3c4d-5e6f-4a1b-8c2d-3e4f5a6b7c8d";
   private static final String REGEL_ID = "a11f3429-3a6b-4389-ba1c-c2747b0fb45b";
   private static final String ANVANDARE_TYP_ID = "9b1e4f2a-6c3d-4e8b-a7f5-2d0c1b9e8a36";

   /** Backend GET-svar med data-fält och informationsaccess-wrapper (BEKB-FR-07). */
   static final String HANDLAGGNING_BODY = """
         {
           "data": {
             "handlaggning_id": "%s",
             "kund": {
               "fornamn": "Anna",
               "efternamn": "Svensson",
               "kon": "KVINNA",
               "anstallning": {
                 "anstallningsdag": "2020-01-01",
                 "arbetstid_procent": 100,
                 "sista_anstallningsdag": "2025-12-31",
                 "organisationsnamn": "Bolaget AB",
                 "organisationsnummer": "556000-0000"
               }
             },
             "ersattning": [
               {
                 "ersattning_id": "%s",
                 "ersattningstyp": "SGI",
                 "omfattning_procent": 75,
                 "belopp": 500,
                 "berakningsgrund": 1,
                 "beslutsutfall": "JA",
                 "avslagsanledning": null,
                 "from": "2024-01-01",
                 "tom": "2024-03-31"
               }
             ]
           },
           "informationsaccess": {
             "uppgiftId": "%s",
             "regelId": "%s",
             "regelVersion": "1",
             "anvandare": {
               "typId": "%s",
               "varde": "H123456"
             }
           }
         }
         """.formatted(HANDLAGGNING_UUID, ERSATTNING_UUID, UPPGIFT_ID, REGEL_ID, ANVANDARE_TYP_ID);

   @Inject
   @Connector("smallrye-in-memory")
   InMemoryConnector inMemoryConnector;

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
      inMemoryConnector.sink("ilogg-event").clear();
   }

   // --- GET /api/regel/bekraftabeslut/{path} ---

   @Test
   void getReferensdata_proxiesBackendResponse()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/avslutstyp"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("[{\"kod\": \"AVSLUTAD\", \"beskrivning\": \"Avslutad\"}]")));

      given()
            .when()
            .get("/api/regel/bekraftabeslut/avslutstyp")
            .then()
            .statusCode(200)
            .body("[0].kod", equalTo("AVSLUTAD"));
   }

   // --- GET /api/regel/bekraftabeslut/handlaggning/{handlaggningId} ---

   @Test
   void getBekraftabeslut_returnsMappedResponse()
   {
      stubHandlaggning();

      given()
            .when()
            .get("/api/regel/bekraftabeslut/handlaggning/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(200)
            .body("handlaggning_id", equalTo(HANDLAGGNING_UUID))
            .body("kund.fornamn", equalTo("Anna"))
            .body("kund.efternamn", equalTo("Svensson"))
            .body("kund.anstallning.organisationsnamn", equalTo("Bolaget AB"))
            .body("ersattning", hasSize(1))
            .body("ersattning[0].ersattning_id", equalTo(ERSATTNING_UUID))
            .body("ersattning[0].ersattningstyp", equalTo("SGI"));
   }

   @Test
   void getBekraftabeslut_returns500_whenBackendFails()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID))
            .willReturn(aResponse().withStatus(500)));

      given()
            .when()
            .get("/api/regel/bekraftabeslut/handlaggning/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(500)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   void getBekraftabeslut_returns404_whenBackendReturns404()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID))
            .willReturn(aResponse().withStatus(404)));

      given()
            .when()
            .get("/api/regel/bekraftabeslut/handlaggning/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(404);
   }

   @Test
   @DisplayName("BEKB-FR-07.1, BEKB-FR-07.2: Hämtning av beslutsunderlag publicerar ett informationsaccess-event med förväntat innehåll")
   @SuppressWarnings("unchecked")
   void getBekraftabeslut_publishesInformationsaccessEvent()
   {
      stubHandlaggning();

      given()
            .when()
            .get("/api/regel/bekraftabeslut/handlaggning/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(200);

      var received = inMemoryConnector.sink("ilogg-event").received();
      assertEquals(1, received.size());
      var event = (InformationsaccessEvent) received.getFirst().getPayload();
      assertNotNull(event.getId());
      assertEquals(ANVANDARE_TYP_ID, event.getAnvandare().getTypId());
      assertEquals("H123456", event.getAnvandare().getVarde());
      assertNotNull(event.getTidpunkt());
      assertEquals("rimfrost-regel-bekraftabeslut-bff", event.getKalla());
      assertEquals(HANDLAGGNING_UUID, event.getHandlaggningId());
      assertEquals(UPPGIFT_ID, event.getUppgiftId());
      assertEquals(REGEL_ID, event.getRegelId());
      assertEquals("1", event.getRegelVersion());
      var kund = (Map<String, Object>) event.getVisadInformation().get("kund");
      assertEquals("Anna", kund.get("fornamn"));
      assertTrue(event.getVisadInformation().containsKey("ersattning"));
   }

   @Test
   @DisplayName("BEKB-FR-07.3: Informationsaccessdata skickas inte vidare till frontend")
   void getBekraftabeslut_doesNotForwardInformationsaccessToFrontend()
   {
      stubHandlaggning();

      given()
            .when()
            .get("/api/regel/bekraftabeslut/handlaggning/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(200)
            .body("informationsaccess", nullValue())
            .body("data", nullValue())
            .body("handlaggning_id", equalTo(HANDLAGGNING_UUID));
   }

   @Test
   @DisplayName("BEKB-FR-07.4: Hämtning av referensdata ger inget informationsaccess-event")
   void getReferensdata_publishesNoEvent()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/avslutstyp"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("[{\"kod\": \"AVSLUTAD\", \"beskrivning\": \"Avslutad\"}]")));

      given().when().get("/api/regel/bekraftabeslut/avslutstyp").then().statusCode(200);

      assertTrue(inMemoryConnector.sink("ilogg-event").received().isEmpty());
   }

   // --- PATCH /api/regel/bekraftabeslut/{handlaggningId} ---

   @Test
   void patchBekraftabeslut_returns200_onSuccess()
   {
      WireMockTestResource.getServer().stubFor(patch(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID))
            .willReturn(aResponse().withStatus(200)));

      given()
            .contentType(ContentType.JSON)
            .body("""
                  {
                      "ersattningar": [{"ersattning_id": "%s", "yrkandestatus": "BEVILJAD"}],
                      "beslut": {
                          "avslutstyp": "AVSLUTAD",
                          "beslutstyp": "HELT_NEDSATT",
                          "beslutsutfall": "BEVILJAD"
                      }
                  }
                  """.formatted(ERSATTNING_UUID))
            .when()
            .patch("/api/regel/bekraftabeslut/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(200);
   }

   @Test
   void patchBekraftabeslut_returns404_whenBackendReturns404()
   {
      WireMockTestResource.getServer().stubFor(patch(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID))
            .willReturn(aResponse().withStatus(404)));

      given()
            .contentType(ContentType.JSON)
            .body("""
                  {
                      "ersattningar": [{"ersattning_id": "%s", "yrkandestatus": "BEVILJAD"}],
                      "beslut": {
                          "avslutstyp": "AVSLUTAD",
                          "beslutstyp": "HELT_NEDSATT",
                          "beslutsutfall": "BEVILJAD"
                      }
                  }
                  """.formatted(ERSATTNING_UUID))
            .when()
            .patch("/api/regel/bekraftabeslut/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(404);
   }

   @Test
   void patchBekraftabeslut_returns400_whenErsattningarIsEmpty()
   {
      given()
            .contentType(ContentType.JSON)
            .body("""
                  {
                      "ersattningar": [],
                      "beslut": {
                          "avslutstyp": "AVSLUTAD",
                          "beslutstyp": "HELT_NEDSATT",
                          "beslutsutfall": "BEVILJAD"
                      }
                  }
                  """)
            .when()
            .patch("/api/regel/bekraftabeslut/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(400);
   }

   @Test
   void patchBekraftabeslut_returns400_whenBeslutFieldIsMissing()
   {
      given()
            .contentType(ContentType.JSON)
            .body("""
                  {
                      "ersattningar": [{"ersattning_id": "%s", "yrkandestatus": "BEVILJAD"}],
                      "beslut": {
                          "avslutstyp": "AVSLUTAD",
                          "beslutstyp": "HELT_NEDSATT"
                      }
                  }
                  """.formatted(ERSATTNING_UUID))
            .when()
            .patch("/api/regel/bekraftabeslut/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(400);
   }

   // --- GET /api/uppgiftsbeskrivning ---

   @Test
   void getUppgiftsbeskrivning_proxiesBackendResponse()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/utokadUppgiftsbeskrivning"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"beskrivning\": \"En utökad beskrivning\"}")));

      given()
            .when()
            .get("/api/uppgiftsbeskrivning")
            .then()
            .statusCode(200)
            .body("beskrivning", equalTo("En utökad beskrivning"));
   }

   // --- POST /api/regel/bekraftabeslut/done ---

   @Test
   void done_returns204_onSuccess()
   {
      WireMockTestResource.getServer().stubFor(post(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID + "/done"))
            .willReturn(aResponse().withStatus(204)));

      given()
            .contentType(ContentType.JSON)
            .body("{\"handlaggningId\": \"" + HANDLAGGNING_UUID + "\"}")
            .when()
            .post("/api/regel/bekraftabeslut/done")
            .then()
            .statusCode(204);
   }

   @Test
   void done_returns400_whenHandlaggningIdIsBlank()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"handlaggningId\": \"\"}")
            .when()
            .post("/api/regel/bekraftabeslut/done")
            .then()
            .statusCode(400);
   }

   @Test
   @DisplayName("BEKB-NFR-02.1: BFF:n vidarebefordrar auktoriseringsuppgifter till regeltjänsten oförändrade")
   void done_forwardsAuthorizationHeader()
   {
      WireMockTestResource.getServer().stubFor(post(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID + "/done"))
            .withHeader("Authorization", WireMock.equalTo("Bearer test-token"))
            .willReturn(aResponse().withStatus(204)));

      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .body("{\"handlaggningId\": \"" + HANDLAGGNING_UUID + "\"}")
            .when()
            .post("/api/regel/bekraftabeslut/done")
            .then()
            .statusCode(204);
   }

   @Test
   @DisplayName("BEKB-NFR-01.2: Beredskapskontroll probar bakomliggande regeltjänst och returnerar UP när den svarar")
   void healthReady_returnsUp_whenBackendReachable()
   {
      WireMockTestResource.getServer().stubFor(head(anyUrl())
            .willReturn(aResponse().withStatus(200)));

      given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
   }

   private static void stubHandlaggning()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody(HANDLAGGNING_BODY)));
   }
}
