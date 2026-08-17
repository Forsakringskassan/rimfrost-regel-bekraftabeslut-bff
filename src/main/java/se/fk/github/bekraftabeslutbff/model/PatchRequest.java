package se.fk.github.bekraftabeslutbff.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.model.UpdateErsattning;

import java.util.List;

public record PatchRequest(@NotNull @NotEmpty @Valid List<UpdateErsattning>ersattningar,@Valid @NotNull Beslut beslut){}
