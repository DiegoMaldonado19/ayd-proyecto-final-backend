package com.ayd.parkcontrol.application.dto.response.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta con información de una evidencia")
public class EvidenceResponse {

    @Schema(description = "ID de la evidencia", example = "1")
    private Long id;

    @Schema(description = "ID de la solicitud de cambio", example = "1")
    private Long change_request_id;

    @Schema(description = "ID del tipo de documento", example = "1")
    private Integer document_type_id;

    @Schema(description = "Código del tipo de documento", example = "IDENTIFICATION")
    private String document_type_code;

    @Schema(description = "Nombre del tipo de documento", example = "Identificación Personal")
    private String document_type_name;

    @Schema(description = "Nombre del archivo", example = "dpi-frontal.jpg")
    private String file_name;

    @Schema(description = "URL del archivo en Azure Storage", example = "https://storage.blob.core.windows.net/...")
    private String file_url;

    @Schema(description = "Tamaño del archivo en bytes", example = "1048576")
    private Long file_size;

    @Schema(description = "Email de quien subió el archivo", example = "juan.perez@email.com")
    private String uploaded_by;

    @Schema(description = "Fecha de carga", example = "2025-10-22T09:15:00")
    private LocalDateTime uploaded_at;
}
