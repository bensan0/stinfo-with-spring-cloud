package com.personal.project.reportservice.model.dto;

import lombok.Data;

@Data
public class Query4ManualCalDTO {

    public Query4ManualCalDTO(Long date, Long lastTradingDay){
        this.date = date;
        this.lastTradingDay = lastTradingDay;
    }

    private Long date;

    private Long lastTradingDay;
}
