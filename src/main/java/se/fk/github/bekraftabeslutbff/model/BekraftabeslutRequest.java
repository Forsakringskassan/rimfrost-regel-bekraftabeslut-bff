package se.fk.github.bekraftabeslutbff.model;

import jakarta.validation.constraints.NotBlank;

public class BekraftabeslutRequest
{
   @NotBlank
   public String handlaggningId;
}
