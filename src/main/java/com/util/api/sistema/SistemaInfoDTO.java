package com.util.api.sistema;

public class SistemaInfoDTO {

    public String tenantId;
    public String apiVersion;
    public String database;
    public String status;

    public SistemaInfoDTO() {
    }

    public SistemaInfoDTO(
            String tenantId,
            String apiVersion,
            String database,
            String status
    ) {
        this.tenantId = tenantId;
        this.apiVersion = apiVersion;
        this.database = database;
        this.status = status;
    }
}