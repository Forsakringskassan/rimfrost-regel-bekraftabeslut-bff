package se.fk.github.bekraftabeslutbff.model;

import jakarta.validation.constraints.NotBlank;

public class Beslut
{
   @NotBlank
   public String avslutstyp;

   @NotBlank
   public String beslutstyp;

   @NotBlank
   public String beslutsutfall;
}
