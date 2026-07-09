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
   private static final String HANDLAGGNING_UUID = "11111111-1111-1111-1111-111111111111";
   private static final String ERSATTNING_UUID = "22222222-2222-2222-2222-222222222222";

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
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
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("""
                        {
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
                        }
                        """.formatted(HANDLAGGNING_UUID, ERSATTNING_UUID))));

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

}
