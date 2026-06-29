package se.fk.github.bekraftabeslutbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RawBekraftabeslutResponse
{
   @JsonProperty("handlaggning_id")
   public String handlaggningId;

   public RawKund kund;

   public List<RawErsattning> ersattning;
}
