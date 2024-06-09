package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class TagsDTO {

    private List<String> priceSelections;

    private List<String> priceStatusSelections;

    private List<String> tradingVolumeSelections;

    private List<String> tradingAmountSelections;

    private List<String> extraTagsSelections;
}
