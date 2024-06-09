package com.personal.project.stockservice.model.dto;

import lombok.Data;

@Data
public class SimpleDetailDTO {

    public SimpleDetailDTO(String stockId, Long detailId){
        this.stockId = stockId;
        this.detailId = detailId;
    }

    private String stockId;

    private Long detailId;
}
