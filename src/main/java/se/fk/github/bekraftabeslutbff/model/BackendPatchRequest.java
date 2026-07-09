package se.fk.github.bekraftabeslutbff.model;

import java.util.List;

public record BackendPatchRequest(List<BackendUpdateErsattning>ersattningar,Beslut beslut){}
