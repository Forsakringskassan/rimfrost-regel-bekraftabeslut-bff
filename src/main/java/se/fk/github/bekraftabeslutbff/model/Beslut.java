package se.fk.github.bekraftabeslutbff.model;

import jakarta.validation.constraints.NotBlank;

public record Beslut(@NotBlank String avslutstyp,@NotBlank String beslutstyp,@NotBlank String beslutsutfall){}
