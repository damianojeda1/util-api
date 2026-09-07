package com.util.api.actualizacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;

@ApplicationScoped
public class ActualizacionRepository {

    @Inject
    DataSource dataSource;

    public int actualizarDB(
            int ver,
            String terminal
    ) throws SQLException {

        if (terminal == null || terminal.isBlank()) {
            throw new IllegalArgumentException(
                    "Terminal obligatoria"
            );
        }

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            try {

                int versionFinal = ver;

                if (ver < 35) {

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.comprobantecompracab
                            DROP COLUMN netoexcento,
                            DROP COLUMN netonogravado,
                            DROP COLUMN importesubtotal,
                            DROP COLUMN bonificacionimporte,
                            DROP COLUMN bonificacionporcentaje,
                            DROP COLUMN iva,
                            DROP COLUMN percepcioniva,
                            DROP COLUMN percepcioningbrutos,
                            DROP COLUMN percepcionganancias,

                            ADD COLUMN conceptosnogravados numeric(10,2) DEFAULT 0,
                            ADD COLUMN exentos numeric(10,2) DEFAULT 0,
                            ADD COLUMN periibb numeric(10,2) DEFAULT 0,
                            ADD COLUMN periva numeric(10,2) DEFAULT 0,
                            ADD COLUMN perimpinternos numeric(10,2) DEFAULT 0,
                            ADD COLUMN perimpmunicipales numeric(10,2) DEFAULT 0,
                            ADD COLUMN perotrosimp numeric(10,2) DEFAULT 0,
                            ADD COLUMN retiva numeric(10,2) DEFAULT 0,
                            ADD COLUMN retganancias numeric(10,2) DEFAULT 0
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            CREATE TABLE IF NOT EXISTS util.proveedorusuario(
                                codigo serial NOT NULL,
                                codigoproveedor integer NOT NULL,
                                codigousuario integer NOT NULL,
                                estado integer DEFAULT 0,

                                CONSTRAINT pk_proveedorusuario
                                    PRIMARY KEY (codigo),

                                CONSTRAINT fk_proveedor_proveedorusuario
                                    FOREIGN KEY (codigoproveedor)
                                    REFERENCES util.proveedor (codigo)
                                    ON UPDATE CASCADE
                                    ON DELETE CASCADE,

                                CONSTRAINT fk_usuario_proveedorusuario
                                    FOREIGN KEY (codigousuario)
                                    REFERENCES util.usuario (codigo)
                                    ON UPDATE CASCADE
                                    ON DELETE CASCADE
                            )
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            INSERT INTO util.articulo(
                                codigo,
                                descripcion,
                                unidadventa,
                                costo,
                                categoria,
                                impuesto,
                                proveedor,
                                llevaStock,
                                stock,
                                stockminimo,
                                observacion,
                                origen,
                                habilitado
                            )
                            VALUES (
                                '0',
                                'Varios',
                                1,
                                1,
                                1,
                                3,
                                0,
                                FALSE,
                                0,
                                0,
                                '',
                                1,
                                TRUE
                            )
                            ON CONFLICT (codigo) DO NOTHING
                            """
                    );

                    actualizarVersion(
                            conn,
                            terminal,
                            35,
                            "V35"
                    );

                    versionFinal = 35;
                }

                if (ver < 36) {

                    ejecutar(
                            conn,
                            """
                            CREATE TABLE IF NOT EXISTS util.medio_pago (
                                id serial PRIMARY KEY,
                                codigo varchar(30) NOT NULL UNIQUE,
                                descripcion varchar(80) NOT NULL,
                                activo boolean NOT NULL DEFAULT TRUE,
                                orden integer NOT NULL DEFAULT 0,
                                es_fisico boolean NOT NULL DEFAULT FALSE,
                                permite_ref boolean NOT NULL DEFAULT TRUE,
                                tipo_legacy integer,
                                codigo_enum varchar(30),
                                porcentaje_comision numeric(10,2)
                                    NOT NULL DEFAULT 0
                            )
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.medio_pago
                            ADD COLUMN IF NOT EXISTS tipo_legacy integer
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.medio_pago
                            ADD COLUMN IF NOT EXISTS codigo_enum varchar(30)
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.medio_pago
                            ADD COLUMN IF NOT EXISTS porcentaje_comision
                            numeric(10,2) NOT NULL DEFAULT 0
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            INSERT INTO util.medio_pago (
                                codigo,
                                descripcion,
                                activo,
                                orden,
                                es_fisico,
                                permite_ref,
                                tipo_legacy,
                                codigo_enum,
                                porcentaje_comision
                            )
                            VALUES
                                ('EFECTIVO', 'Efectivo',
                                 TRUE, 10, TRUE, TRUE,
                                 1, 'EFECTIVO', 0),

                                ('DEBITO', 'Débito',
                                 TRUE, 20, FALSE, TRUE,
                                 2, 'DEBITO', 0),

                                ('CREDITO', 'Crédito',
                                 TRUE, 30, FALSE, TRUE,
                                 3, 'CREDITO', 0),

                                ('TRANSFERENCIA', 'Transferencia',
                                 TRUE, 40, FALSE, TRUE,
                                 4, 'TRANSFERENCIA', 0),

                                ('OTRO', 'Otros',
                                 TRUE, 50, TRUE, TRUE,
                                 5, 'OTRO', 0)

                            ON CONFLICT (codigo) DO NOTHING
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            UPDATE util.medio_pago
                               SET codigo_enum = UPPER(codigo)
                             WHERE codigo_enum IS NULL
                                OR TRIM(codigo_enum) = ''
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            UPDATE util.medio_pago
                               SET tipo_legacy =
                                   CASE UPPER(codigo)
                                       WHEN 'EFECTIVO' THEN 1
                                       WHEN 'DEBITO' THEN 2
                                       WHEN 'CREDITO' THEN 3
                                       WHEN 'TRANSFERENCIA' THEN 4
                                       WHEN 'OTRO' THEN 5
                                       ELSE tipo_legacy
                                   END
                             WHERE tipo_legacy IS NULL
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            UPDATE util.medio_pago
                               SET porcentaje_comision = 0
                             WHERE porcentaje_comision IS NULL
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.pago
                            ADD COLUMN IF NOT EXISTS
                            referencia varchar(120)
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.pago
                            ADD COLUMN IF NOT EXISTS
                            id_medio_pago integer
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            DO $$
                            BEGIN

                                IF NOT EXISTS (
                                    SELECT 1
                                    FROM pg_constraint
                                    WHERE conname =
                                        'fk_pago_medio_pago'
                                ) THEN

                                    ALTER TABLE util.pago
                                    ADD CONSTRAINT
                                        fk_pago_medio_pago
                                    FOREIGN KEY (id_medio_pago)
                                    REFERENCES util.medio_pago(id);

                                END IF;

                            END
                            $$
                            """
                    );

                    actualizarVersion(
                            conn,
                            terminal,
                            36,
                            "V36"
                    );

                    versionFinal = 36;
                }

                if (ver < 37) {

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.medio_pago
                            ADD COLUMN IF NOT EXISTS
                            porcentaje_comision
                            numeric(10,2) DEFAULT 0
                            """
                    );

                    actualizarVersion(
                            conn,
                            terminal,
                            37,
                            "V37"
                    );

                    versionFinal = 37;
                }

                if (ver < 38) {

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.caja
                            ADD COLUMN IF NOT EXISTS
                            importeaperturavirtual
                            numeric(10,2)
                            NOT NULL DEFAULT 0
                            """
                    );

                    ejecutar(
                            conn,
                            """
                            ALTER TABLE util.caja
                            ADD COLUMN IF NOT EXISTS
                            importecierrevirtual
                            numeric(10,2)
                            """
                    );

                    actualizarVersion(
                            conn,
                            terminal,
                            38,
                            "V38"
                    );

                    versionFinal = 38;
                }

                if (ver < 39) {

                    actualizarVersion(
                            conn,
                            terminal,
                            39,
                            "V39"
                    );

                    versionFinal = 39;
                }

                if (ver < 40) {

                    ejecutar(
                            conn,
                            """
                                CREATE TABLE util.articuloCanal (
                                    codigoArticulo varchar NOT NULL,
                                    codigoProveedor varchar NOT NULL,
                                    canal varchar NOT NULL,              -- TIENDANUBE / MERCADOLIBRE
                                    idExterno varchar,
                                    idVarianteExterna varchar,
                                    publicado boolean DEFAULT false,
                                    ultimaSincronizacion timestamp,
                                    PRIMARY KEY (codigoArticulo, codigoProveedor, canal)
                                );
                            """
                    );

                    actualizarVersion(
                            conn,
                            terminal,
                            40,
                            "V40"
                    );

                    versionFinal = 40;
                }

                if (ver < 41) {

                    ejecutar(
                            conn,
                            """
                                CREATE TABLE util.canalConfiguracion (
                                      codigo serial PRIMARY KEY,
                                      canal varchar NOT NULL,
                                      storeId varchar,
                                      accessToken varchar,
                                      activo boolean DEFAULT true,
                                      fechaVinculacion timestamp,
                                      UNIQUE (canal)
                                  );
                            """
                    );

                    actualizarVersion(
                            conn,
                            terminal,
                            41,
                            "V41"
                    );

                    versionFinal = 41;
                }

                if (ver < 42) {

                    ejecutar(
                            conn,
                            """
                                ALTER TABLE util.item
                                            ALTER COLUMN cantidad
                                            TYPE numeric(16,6)
                                            USING cantidad::numeric(16,6);
                            """
                    );

                    actualizarVersion(
                            conn,
                            terminal,
                            42,
                            "V42"
                    );

                    versionFinal = 42;
                }

                conn.commit();

                return versionFinal;

            } catch (Exception ex) {

                conn.rollback();

                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }

                throw new SQLException(
                        "Error actualizando base",
                        ex
                );
            }
        }
    }

    private void ejecutar(
            Connection conn,
            String sql
    ) {

        Savepoint savepoint = null;

        try {

            savepoint = conn.setSavepoint();

            try (PreparedStatement ps =
                         conn.prepareStatement(sql)) {

                ps.executeUpdate();
            }

            conn.releaseSavepoint(savepoint);

        } catch (SQLException ex) {

            try {

                if (savepoint != null) {
                    conn.rollback(savepoint);
                    conn.releaseSavepoint(savepoint);
                }

            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }

            System.out.println(
                    "Error ejecutando actualización: "
                            + ex.getMessage()
            );
        }
    }

    private void actualizarVersion(
            Connection conn,
            String terminal,
            int version,
            String observacion
    ) throws SQLException {

        String sql =
                """
                UPDATE util.version
                   SET versionactual = ?,
                       observacion = ?,
                       fechaact = CURRENT_DATE
                 WHERE idapp = 1
                   AND terminal = ?
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, version);
            ps.setString(2, observacion);
            ps.setString(3, terminal.trim());

            int actualizados = ps.executeUpdate();

            if (actualizados != 1) {
                throw new SQLException(
                        "No se pudo actualizar util.version " +
                                "para terminal " + terminal
                );
            }
        }
    }

}