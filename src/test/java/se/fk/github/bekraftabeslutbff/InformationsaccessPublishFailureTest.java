package se.fk.github.bekraftabeslutbff;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.bff.ilogg.ILogg;
import se.fk.rimfrost.framework.bff.ilogg.exception.ILoggException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static se.fk.github.bekraftabeslutbff.BekraftabeslutControllerTest.HANDLAGGNING_UUID;
import static se.fk.github.bekraftabeslutbff.BekraftabeslutControllerTest.HANDLAGGNING_BODY;

/**
 * Tests BEKB-FR-07.5: publish failure must return error without exposing data.
 */
@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class InformationsaccessPublishFailureTest
{
   @InjectMock
   ILogg iLogg;

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
   }

   @Test
   @DisplayName("BEKB-FR-07.5: Om eventet inte kan publiceras returneras ett fel utan beslutsunderlag")
   void getBekraftabeslut_returnsErrorWithoutData_whenEventCannotBePublished() throws ILoggException
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/regel/bekraftabeslut/" + HANDLAGGNING_UUID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody(HANDLAGGNING_BODY)));
      Mockito.doThrow(new ILoggException("Kafka unavailable", new RuntimeException("nack")))
            .when(iLogg).publish(Mockito.any());

      given()
            .when()
            .get("/api/regel/bekraftabeslut/handlaggning/" + HANDLAGGNING_UUID)
            .then()
            .statusCode(500)
            .body("error", equalTo("Internal server error"))
            .body("kund", nullValue());
   }
}
