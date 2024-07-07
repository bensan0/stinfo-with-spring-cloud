package com.personal.project.reportservice.service;

import com.personal.project.reportservice.model.dto.DailyStockInfoDTO;

import java.util.List;
import java.util.Map;

public interface InfoService {

    void organizing(Map<String, List<DailyStockInfoDTO>> stockIdToDTOs);
}
