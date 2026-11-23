package se.fk.github.bekraftabeslutbff.integration;

/** Thrown when ILogg publishing fails, preventing data from being returned (BEKB-FR-07.5). */
public class InformationsaccessPublishException extends RuntimeException
{
   public InformationsaccessPublishException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
