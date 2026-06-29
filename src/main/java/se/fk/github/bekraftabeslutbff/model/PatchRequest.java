package se.fk.github.bekraftabeslutbff.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class PatchRequest
{
   @NotNull
   @Size(min = 1)
   public List<Object> ersattningar;

   @Valid
   @NotNull
   public Beslut beslut;
}
