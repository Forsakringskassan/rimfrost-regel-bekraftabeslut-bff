package se.fk.github.bekraftabeslutbff;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class BekraftabeslutControllerTest
{

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
   }

   // --- GET /api/health ---

   @Test
   void health_returnsOk()
   {
      given()
            .when()
            .get("/api/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("ok"));
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

   // --- POST /api/regel/bekraftabeslut ---

   @Test
   void getBekraftabeslut_returnsMappedResponse()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/handlaggning-1"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("""
                        {
                            "handlaggning_id": "handlaggning-1",
                            "kund": {
                                "fornamn": "Anna",
                                "efternamn": "Svensson",
                                "kon": "K",
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
                                    "ersattning_id": "ers-1",
                                    "ersattningstyp": "SGI",
                                    "omfattning_procent": 75,
                                    "belopp": 500,
                                    "berakningsgrund": "grundbelopp",
                                    "beslutsutfall": "BEVILJAD",
                                    "avslagsanledning": null,
                                    "from": "2024-01-01",
                                    "tom": "2024-03-31"
                                }
                            ]
                        }
                        """)));

      given()
            .contentType(ContentType.JSON)
            .body("{\"handlaggningId\": \"handlaggning-1\"}")
            .when()
            .post("/api/regel/bekraftabeslut")
            .then()
            .statusCode(200)
            .body("handlaggningId", equalTo("handlaggning-1"))
            .body("kund.fornamn", equalTo("Anna"))
            .body("kund.efternamn", equalTo("Svensson"))
            .body("kund.anstallning.organisationsnamn", equalTo("Bolaget AB"))
            .body("ersattning", hasSize(1))
            .body("ersattning[0].ersattningId", equalTo("ers-1"))
            .body("ersattning[0].ersattningstyp", equalTo("SGI"));
   }

   @Test
   void getBekraftabeslut_returns400_whenHandlaggningIdIsBlank()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"handlaggningId\": \"\"}")
            .when()
            .post("/api/regel/bekraftabeslut")
            .then()
            .statusCode(400);
   }

   @Test
   void getBekraftabeslut_returns500_whenBackendFails()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/handlaggning-err"))
            .willReturn(aResponse().withStatus(500)));

      given()
            .contentType(ContentType.JSON)
            .body("{\"handlaggningId\": \"handlaggning-err\"}")
            .when()
            .post("/api/regel/bekraftabeslut")
            .then()
            .statusCode(500)
            .body("error", equalTo("Upstream error"));
   }

   // --- PATCH /api/regel/bekraftabeslut/{handlaggningId} ---

   @Test
   void patchBekraftabeslut_returns200_onSuccess()
   {
      WireMockTestResource.getServer().stubFor(patch(urlEqualTo("/regel/bekraftabeslut/handlaggning-1"))
            .willReturn(aResponse().withStatus(200)));

      given()
            .contentType(ContentType.JSON)
            .body("""
                  {
                      "ersattningar": [{"ersattningId": "ers-1"}],
                      "beslut": {
                          "avslutstyp": "AVSLUTAD",
                          "beslutstyp": "HELT_NEDSATT",
                          "beslutsutfall": "BEVILJAD"
                      }
                  }
                  """)
            .when()
            .patch("/api/regel/bekraftabeslut/handlaggning-1")
            .then()
            .statusCode(200);
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
            .patch("/api/regel/bekraftabeslut/handlaggning-1")
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
                      "ersattningar": [{"ersattningId": "ers-1"}],
                      "beslut": {
                          "avslutstyp": "AVSLUTAD",
                          "beslutstyp": "HELT_NEDSATT"
                      }
                  }
                  """)
            .when()
            .patch("/api/regel/bekraftabeslut/handlaggning-1")
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
      WireMockTestResource.getServer().stubFor(post(urlEqualTo("/regel/bekraftabeslut/handlaggning-1/done"))
            .willReturn(aResponse().withStatus(204)));

      given()
            .contentType(ContentType.JSON)
            .body("{\"handlaggningId\": \"handlaggning-1\"}")
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

}
