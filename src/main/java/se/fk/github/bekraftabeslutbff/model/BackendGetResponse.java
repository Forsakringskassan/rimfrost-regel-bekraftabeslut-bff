package se.fk.github.bekraftabeslutbff.model;

import se.fk.rimfrost.regel.bekraftabeslut.openapi.jaxrsspec.controllers.generatedsource.model.GetDataResponse;

/** Regeltjänstens GET-svar innehåller beslutsunderlag och informationsaccess-data (BEKB-FR-07). */
public record BackendGetResponse(GetDataResponse data,BackendInformationsaccess informationsaccess){}
