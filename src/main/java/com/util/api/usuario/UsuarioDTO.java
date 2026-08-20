package com.util.api.usuario;

import com.util.api.generico.ParDTO;

public final class UsuarioDTO {

    private UsuarioDTO() {
    }

    public static class Response {

        public int codigo;
        public String nombre;
        public String password;
        public ParDTO nivel;
        public boolean habilitado;
        public boolean mostrarcostos;
        public boolean facturar;

        public Response() {
        }
    }

    public static class LoginRequest {

        public String recordado;
        public String nombreUsuario;
        public String password;
        public boolean recordar;
        public String terminal;

        public LoginRequest() {
        }
    }

    public static class GuardarRequest {

        public int codigo;
        public String nombre;
        public String password;
        public int codigoNivel;
        public boolean habilitado;
        public boolean mostrarCostos;
        public boolean facturar;

        public GuardarRequest() {
        }
    }
}