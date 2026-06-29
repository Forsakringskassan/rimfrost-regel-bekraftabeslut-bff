package se.fk.github.bekraftabeslutbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RawErsattning
{
   @JsonProperty("ersattning_id")
   public String ersattningId;

   public String ersattningstyp;

   @JsonProperty("omfattning_procent")
   public Object omfattningProcent;

   public Object belopp;
   public Object berakningsgrund;
   public String beslutsutfall;
   public String avslagsanledning;
   public String from;
   public String tom;
}
