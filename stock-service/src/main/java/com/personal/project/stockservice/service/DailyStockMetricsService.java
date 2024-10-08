package com.personal.project.stockservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.dto.response.DailyStockMetricsDTO;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;

import java.util.List;
import java.util.Map;

public interface DailyStockMetricsService extends IService<DailyStockMetricsDO> {

    /**
     * 報表服務獲取產出報告所需資料
     * 今日metrics(可能為空, 除非於定時任務前有手動按過)
     * 前一個交易日資料
     * @param date
     * @return
     */
    Map<String, List<DailyStockMetricsDTO>> query4CalMetrics(Long date);

    /**
     * 取得今日個股指標 & 上一個交易日個股指標
     *
     * @param date
     * @return
     */
    Map<String, List<DailyStockMetricsDTO>> query4CalDetail(Long date);

    /**
     * 取得今日個股指標 & 上一個交易日個股指標
     *
     * @param manualCalDTO
     * @return
     */
    List<DailyStockMetricsDTO> query4CalDetail(ManualCalDTO manualCalDTO);

    /**
     * 初始化任務用, 取得上一個交易日指標報告(以init來說, 等於最新一份)
     * @return
     */
    Map<String, DailyStockMetricsDTO> query4InitTodayMetrics();

    /**
     * 初始化任務用, 取得最新交易日, 上一個交易日指標報告
     * @return
     */
    Map<String, List<DailyStockMetricsDTO>> query4InitTodayDetail();
}
