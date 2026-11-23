package se.fk.github.bekraftabeslutbff.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.Readiness;
import se.fk.rimfrost.framework.bff.health.UpstreamHealthCheck;

/**
 * Produces readiness health checks for upstream services.
 */
@ApplicationScoped
public class HealthCheckProducer
{

   /**
    * Readiness check that probes the bekraftabeslut backend.
    */
   @Readiness
   @Produces
   HealthCheck bekraftabeslutHealthCheck()
   {
      return UpstreamHealthCheck.fromConfig(
            "bekraftabeslut-backend",
            "quarkus.rest-client.bekraftabeslut.url",
            2000);
   }
}
