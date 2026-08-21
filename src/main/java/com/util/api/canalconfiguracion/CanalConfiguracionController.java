package com.util.api.canalconfiguracion;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/canalconfiguracion")
@Produces(MediaType.APPLICATION_JSON)
public class CanalConfiguracionController {

    @Inject
    CanalConfiguracionService service;

}