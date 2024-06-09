package com.personal.project.stockservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;

import java.util.List;

public interface DailyStockMetricsService extends IService<DailyStockMetricsDO> {

    List<SimpleMetricsDTO> queryMetrics(Long date);

    /**
     * 報表服務獲取產出報告所需資料
     * 本日個股資料join昨日指標資料使用日期入參當作join條件理由為：
     * 個股資料除六,日外,每日皆會產生一筆資料, 若當日為國定假日或是因其他原因導致未開市, 或者該個股未開盤, 則當日收盤價等資料為null.
     * 又, 每個交易日的指標資料於報表中心產出時, 會判斷若個股當日收盤價為空(非交易日), 則該日資料為複製前一日之資料, 代表若當日非交易日則
     * 上一個交易日的資料會一直延續, 因此報表程序執行的時間點由於今日指標報表尚未產出, 因此昨日資料一定為上個交易日的指標資料
     *
     * @param query4CalDto
     * @return
     */
    List<CalMetricsDTO> query4CalMetrics(Query4CalDTO query4CalDto);

    List<CalMetricsDTO> query4CalMetrics(ManualCalDTO manualCalDTO);

    /**
     * 取得今日個股指標 & 上一個交易日個股指標
     *
     * @param query4CalDto
     * @return
     */
    List<DailyStockMetricsDTO> query4CalDetail(Query4CalDTO query4CalDto);

    /**
     * 取得今日個股指標 & 上一個交易日個股指標
     *
     * @param manualCalDTO
     * @return
     */
    List<DailyStockMetricsDTO> query4CalDetail(ManualCalDTO manualCalDTO);


}
