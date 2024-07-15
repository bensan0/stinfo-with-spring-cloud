package com.personal.project.stockservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDO;

import java.util.List;

public interface DailyStockInfoMapper extends BaseMapper<DailyStockInfoDO> {

    List<DailyStockInfoDTO> query4CalMetrics(Query4CalDTO query4CalDTO);

    List<DailyStockInfoDTO> queryInfo4CalDetail(Query4CalDTO query4CalDTO);

    List<DailyStockInfoDO> queryFormer(Long date);

    List<StockInfo4InitMetricsDTO> query4InitYesterdayMetrics();

    List<StockInfo4InitDetailDTO> queryInfo4InitYesterdayDetail();

    List<StockInfo4InitMetricsDTO> query4InitTodayMetrics();

    List<DailyStockInfoDTO> query4InitTodayDetail();

    List<DailyStockInfoDTO> query4RealTimeMetrics(Long date);

    List<DailyStockInfoDTO> query4RealTimeDetail(Long date);

    List<DailyStockInfoDTO> queryLatest();

    List<DailyStockInfoDTO> queryByStockId(String stockId);

    List<CompleteStockDTO> queryByConditions(QueryConditionDTO dto);
}

