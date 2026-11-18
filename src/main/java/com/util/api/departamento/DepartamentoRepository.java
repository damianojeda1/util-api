package com.util.api.departamento;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DepartamentoRepository {
    private static final int GENERAL = 1;

    @Inject
    DataSource dataSource;

    public DepartamentoDTO findById(int codigo) throws SQLException {
        String sql = "SELECT codigo, nombre, habilitado FROM util.departamento WHERE codigo = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public DepartamentoDTO buscarPorDescripcion(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty() || nombre.trim().equals("0")) {
            return findById(GENERAL);
        }
        String sql = "SELECT codigo, nombre, habilitado FROM util.departamento " +
                     "WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<DepartamentoDTO> obtenerCompletos() throws SQLException {
        return listar(false);
    }

    public List<DepartamentoDTO> obtenerHabilitados() throws SQLException {
        return listar(true);
    }

    private List<DepartamentoDTO> listar(boolean soloHabilitados) throws SQLException {
        String sql = "SELECT codigo, nombre, habilitado FROM util.departamento " +
                     (soloHabilitados ? "WHERE habilitado = true " : "") +
                     "ORDER BY nombre";
        List<DepartamentoDTO> resultado = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.add(map(rs));
        }
        return resultado;
    }

    public boolean insert(DepartamentoDTO departamento) throws SQLException {
        if (!valido(departamento)) return false;
        String sql = "INSERT INTO util.departamento (nombre, habilitado) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, departamento.nombre.trim());
            ps.setBoolean(2, departamento.habilitado);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean update(DepartamentoDTO departamento) throws SQLException {
        if (!valido(departamento) || departamento.codigo <= GENERAL) return false;
        String sql = "UPDATE util.departamento SET nombre = ?, habilitado = ? WHERE codigo = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, departamento.nombre.trim());
            ps.setBoolean(2, departamento.habilitado);
            ps.setInt(3, departamento.codigo);
            return ps.executeUpdate() == 1;
        }
    }

    // Baja lógica: no modifica los tickets históricos.
    public boolean delete(int codigo) throws SQLException {
        if (codigo <= GENERAL) return false;
        String sql = "UPDATE util.departamento SET habilitado = false " +
                     "WHERE codigo = ? AND habilitado = true";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codigo);
            return ps.executeUpdate() == 1;
        }
    }

    private boolean valido(DepartamentoDTO d) {
        return d != null && d.nombre != null && !d.nombre.trim().isEmpty()
               && d.nombre.trim().length() <= 100;
    }

    private DepartamentoDTO map(ResultSet rs) throws SQLException {
        return new DepartamentoDTO(rs.getInt("codigo"), rs.getString("nombre"),
                                   rs.getBoolean("habilitado"));
    }
}
