package br.com.shooping.list.application.dto.shoppinglist;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de resposta para um item de lista de compras.
 * Retornado pelos endpoints de gerenciamento de itens.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

    /**
     * ID único do item
     */
    private Long id;

    /**
     * Nome do item
     */
    private String name;

    /**
     * Quantidade
     */
    private BigDecimal quantity;

    /**
     * Unidade de medida (opcional)
     */
    private String unit;

    /**
     * Preço unitário (opcional)
     */
    private BigDecimal unitPrice;

    /**
     * Status do item (PENDING ou PURCHASED)
     */
    private String status;

    /**
     * Data de criação do item
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    /**
     * Data da última atualização
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
}

