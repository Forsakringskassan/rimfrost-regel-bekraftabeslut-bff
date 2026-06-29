package se.fk.github.bekraftabeslutbff;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.net.HttpURLConnection;
import java.net.URL;

@Readiness
@ApplicationScoped
public class BekraftabeslutHealthCheck implements HealthCheck
{

   @ConfigProperty(name = "quarkus.rest-client.bekraftabeslut.url")
   String bekraftabeslutUrl;

   @Override
   public HealthCheckResponse call()
   {
      HttpURLConnection connection = null;
      try
      {
         connection = (HttpURLConnection) new URL(bekraftabeslutUrl).openConnection();
         connection.setConnectTimeout(2000);
         connection.setReadTimeout(2000);
         connection.setRequestMethod("HEAD");
         int status = connection.getResponseCode();
         if (status < 500)
         {
            return HealthCheckResponse.up("bekraftabeslut-backend");
         }
         return HealthCheckResponse.named("bekraftabeslut-backend").down()
               .withData("status", status)
               .build();
      }
      catch (Exception e)
      {
         return HealthCheckResponse.named("bekraftabeslut-backend").down()
               .withData("error", e.getMessage())
               .build();
      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }
}
