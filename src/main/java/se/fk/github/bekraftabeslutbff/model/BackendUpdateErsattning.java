package se.fk.github.bekraftabeslutbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BackendUpdateErsattning(@JsonProperty("ersattning_id")String ersattningId,String yrkandestatus){}
