package com.util.api.departamento;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/departamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DepartamentoController {
    @Inject
    DepartamentoService service;

    @GET
    public Response obtenerCompletos() {
        try { return Response.ok(service.obtenerCompletos()).build(); }
        catch (Exception e) { e.printStackTrace(); return Response.serverError().entity("Error obteniendo departamentos").build(); }
    }

    @GET
    @Path("/habilitados")
    public Response obtenerHabilitados() {
        try { return Response.ok(service.obtenerHabilitados()).build(); }
        catch (Exception e) { e.printStackTrace(); return Response.serverError().entity("Error obteniendo departamentos habilitados").build(); }
    }

    @GET
    @Path("/buscar")
    public Response buscarPorDescripcion(@QueryParam("nombre") String nombre) {
        try { return Response.ok(service.buscarPorDescripcion(nombre)).build(); }
        catch (Exception e) { e.printStackTrace(); return Response.serverError().entity("Error buscando departamento").build(); }
    }

    @GET
    @Path("/{codigo}")
    public Response findById(@PathParam("codigo") int codigo) {
        try { return Response.ok(service.findById(codigo)).build(); }
        catch (Exception e) { e.printStackTrace(); return Response.serverError().entity("Error buscando departamento").build(); }
    }

    @POST
    public Response insert(DepartamentoDTO departamento) {
        try { return Response.ok(service.insert(departamento)).build(); }
        catch (Exception e) { e.printStackTrace(); return Response.serverError().entity(false).build(); }
    }

    @PUT
    @Path("/{codigo}")
    public Response update(@PathParam("codigo") int codigo, DepartamentoDTO departamento) {
        try { return Response.ok(service.update(codigo, departamento)).build(); }
        catch (Exception e) { e.printStackTrace(); return Response.serverError().entity(false).build(); }
    }

    @DELETE
    @Path("/{codigo}")
    public Response delete(@PathParam("codigo") int codigo) {
        try { return Response.ok(service.delete(codigo)).build(); }
        catch (Exception e) { e.printStackTrace(); return Response.serverError().entity(false).build(); }
    }
}
