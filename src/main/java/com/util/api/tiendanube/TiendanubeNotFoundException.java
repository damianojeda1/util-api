package com.util.api.tiendanube;

public class TiendanubeNotFoundException
        extends RuntimeException {

    public TiendanubeNotFoundException(
            String mensaje
    ) {
        super(mensaje);
    }
}