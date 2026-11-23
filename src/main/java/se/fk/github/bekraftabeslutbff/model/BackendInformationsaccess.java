package se.fk.github.bekraftabeslutbff.model;

import java.util.UUID;

/** Informationsaccess-data från regeltjänstens GET-svar (BEKB-FR-07). */
public record BackendInformationsaccess(UUID uppgiftId,UUID regelId,String regelVersion,BackendAnvandare anvandare){}
