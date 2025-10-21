package com.ayd.parkcontrol.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    @Value("${azure.storage.account-name}")
    private String accountName;

    @Value("${azure.storage.account-key}")
    private String accountKey;

    @Value("${azure.storage.container.incidents:evidencias-incidentes}")
    private String incidentsContainer;

    @Value("${azure.storage.container.plate-changes:documentos-cambios}")
    private String plateChangesContainer;

    @Bean
    public BlobServiceClient blobServiceClient() {
        String connectionString = String.format(
                "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                accountName, accountKey);

        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public String getIncidentsContainer() {
        return incidentsContainer;
    }

    public String getPlateChangesContainer() {
        return plateChangesContainer;
    }
}