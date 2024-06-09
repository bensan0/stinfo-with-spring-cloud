package com.personal.project.stockservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDO;

import java.util.List;

public interface DailyStockInfoService extends IService<DailyStockInfoDO> {

    boolean saveAll(List<DailyStockInfoDTO> data);

    /**
     * 取得5個交易日前(含今日), 10, 20, 60, 120, 240 的收盤價資料
     * @param query4CalDTO
     * @return
     */
    List<PastClosingPriceDTO> queryPastClosingPrice4CalMetrics(Query4CalDTO query4CalDTO);

    /**
     * 取得5個交易日前(含今日), 10, 20, 60, 120, 240 的收盤價資料
     * @param manualCalDTO
     * @return
     */
    List<PastClosingPriceDTO> queryPastClosingPrice4CalMetrics(ManualCalDTO manualCalDTO);

    /**
     * 取得今日, 上個交易日, 3個交易日前(含今日), 5個交易日前(含今日)個股資料
     * @param query4CalDto
     * @return
     */
    List<StockInfo4CalDetailDTO> queryInfo4CalDetail(Query4CalDTO query4CalDto);

    /**
     * 取得今日, 上個交易日, 3個交易日前(含今日), 5個交易日前(含今日)個股資料
     * @param manualCalDTO
     * @return
     */
    List<StockInfo4CalDetailDTO> queryInfo4CalDetail(ManualCalDTO manualCalDTO);


}
