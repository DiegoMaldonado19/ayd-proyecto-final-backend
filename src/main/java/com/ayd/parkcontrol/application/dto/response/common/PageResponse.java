package com.ayd.parkcontrol.application.dto.response.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta paginada genérica")
public class PageResponse<T> {

    @Schema(description = "Contenido de la página")
    private List<T> content;

    @Schema(description = "Número de página actual (base 0)", example = "0")
    private Integer page_number;

    @Schema(description = "Tamaño de página", example = "20")
    private Integer page_size;

    @Schema(description = "Total de elementos", example = "100")
    private Long total_elements;

    @Schema(description = "Total de páginas", example = "5")
    private Integer total_pages;

    @Schema(description = "Indica si es la primera página", example = "true")
    private Boolean is_first;

    @Schema(description = "Indica si es la última página", example = "false")
    private Boolean is_last;

    @Schema(description = "Indica si tiene página anterior", example = "false")
    private Boolean has_previous;

    @Schema(description = "Indica si tiene página siguiente", example = "true")
    private Boolean has_next;
}
