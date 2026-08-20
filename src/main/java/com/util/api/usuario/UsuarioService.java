package com.util.api.usuario;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository repository;

    public List<UsuarioDTO.Response> obtenerUsuarios(
            boolean soloHabilitados,
            boolean incluirAdmin
    ) throws SQLException {

        return repository.obtenerUsuarios(
                soloHabilitados,
                incluirAdmin
        );
    }

    public boolean login(
            UsuarioDTO.LoginRequest request
    ) throws SQLException {

        return repository.login(request);
    }

    public UsuarioDTO.Response obtenerUsuario(
            int codigoUsuario
    ) throws SQLException {

        return repository.obtenerUsuario(
                codigoUsuario
        );
    }

    public UsuarioDTO.Response obtenerUsuarioRecordado(
            String terminal
    ) throws SQLException {

        return repository.obtenerUsuarioRecordado(
                terminal
        );
    }

    public UsuarioDTO.Response obtenerUsuarioNombre(
            String nombre
    ) throws SQLException {

        return repository.obtenerUsuarioNombre(
                nombre
        );
    }

    public boolean insertarUsuario(
            UsuarioDTO.GuardarRequest request
    ) throws SQLException {

        return repository.insertarUsuario(
                request
        );
    }

    public boolean actualizarUsuario(
            int codigoUsuario,
            UsuarioDTO.GuardarRequest request
    ) throws SQLException {

        return repository.actualizarUsuario(
                codigoUsuario,
                request
        );
    }
}