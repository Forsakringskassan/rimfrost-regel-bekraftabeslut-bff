package se.fk.github.bekraftabeslutbff.model;

import jakarta.validation.constraints.NotBlank;

public record BekraftabeslutRequest(@NotBlank String handlaggningId){}
