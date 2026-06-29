package se.fk.github.bekraftabeslutbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RawAnstallning
{
   public String anstallningsdag;

   @JsonProperty("arbetstid_procent")
   public Object arbetstidProcent;

   @JsonProperty("sista_anstallningsdag")
   public String sistaAnstallningsdag;

   public String organisationsnamn;
   public String organisationsnummer;
}
