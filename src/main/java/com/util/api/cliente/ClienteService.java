package com.util.api.cliente;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class ClienteService {

    @Inject
    ClienteRepository repository;

    public List<ClienteDTO.Response> listar(
            Boolean habilitado,
            boolean excluirGenericos
    ) throws SQLException {

        return repository.listar(
                habilitado,
                excluirGenericos
        );
    }

    public ClienteDTO.Response findById(
            int codigo
    ) throws SQLException {

        return repository.findById(codigo);
    }

    public ClienteDTO.OperacionResponse insert(
            ClienteDTO.CrearRequest request
    ) {
        return repository.insert(request);
    }

    public ClienteDTO.OperacionResponse update(
            int codigo,
            ClienteDTO.ActualizarRequest request
    ) {
        return repository.update(
                codigo,
                request
        );
    }

    public ClienteDTO.CodigoResponse
    obtenerProximoCodigoEstimado() throws SQLException {

        int codigo =
                repository.obtenerProximoCodigoEstimado();

        return new ClienteDTO.CodigoResponse(
                codigo
        );
    }

    public ClienteDTO.SaldoResponse obtenerSaldo(
            int clienteId
    ) throws SQLException {

        double saldo =
                repository.obtenerSaldo(clienteId);

        return new ClienteDTO.SaldoResponse(
                clienteId,
                saldo
        );
    }
}