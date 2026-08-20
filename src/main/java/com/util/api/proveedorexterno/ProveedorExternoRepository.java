package com.util.api.proveedorexterno;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProveedorExternoRepository {

    @Inject
    AgroalDataSource dataSource;

    public List<ProveedorExternoDTO.ProveedorResponse>
    listarProveedoresInstalados() {

        List<ProveedorExternoDTO.ProveedorResponse> resultado =
                new ArrayList<>();

        String sql =
                " SELECT ins.codigoproveedor,\n"
                        + "        ins.idproveedorlocal,\n"
                        + "        ins.codigocliente,\n"
                        + "        prov.razonsocial,\n"
                        + "        COALESCE(\n"
                        + "          param.valor,\n"
                        + "          'Sin fecha'\n"
                        + "        ) AS ultimaactualizacion\n"
                        + " FROM util.proveedorexternoinstalacion AS ins\n"
                        + " INNER JOIN util.proveedor AS prov\n"
                        + "   ON prov.codigo = ins.idproveedorlocal\n"
                        + " LEFT JOIN "
                        + "fcentral.proveedorexternoparametro AS param\n"
                        + "   ON param.codigoproveedor = "
                        + "ins.codigoproveedor\n"
                        + "  AND param.codigo = "
                        + "'actualizacionarticulos'\n"
                        + " ORDER BY prov.razonsocial";

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                ProveedorExternoDTO.ProveedorResponse item =
                        new ProveedorExternoDTO.ProveedorResponse();

                item.setCodigoProveedor(
                        rs.getInt("codigoproveedor")
                );
                item.setIdProveedorLocal(
                        rs.getInt("idproveedorlocal")
                );
                item.setCodigoCliente(
                        rs.getInt("codigocliente")
                );
                item.setRazonSocial(
                        rs.getString("razonsocial")
                );
                item.setUltimaActualizacion(
                        rs.getString("ultimaactualizacion")
                );

                resultado.add(item);
            }

            return resultado;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error obteniendo proveedores externos",
                    e
            );
        }
    }
}