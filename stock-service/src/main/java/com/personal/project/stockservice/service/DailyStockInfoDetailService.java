package com.personal.project.stockservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;

import java.util.List;
import java.util.Map;

public interface DailyStockInfoDetailService extends IService<DailyStockInfoDetailDO> {

    /**
     * 獲取今日, 前一個交易日個股標籤數據
     *
     * @return
     */
    Map<String, List<DailyStockInfoDetailDTO>> query4CalDetail(Long date);

    /**
     * 獲取前一個交易日個股標籤數據
     *
     * @return
     */
    List<DailyStockInfoDetailDTO> query4CalDetail(ManualCalDTO manualCalDTO);

    IPage<ConditionQueryUnionInfoDTO> queryCondition(Page<ConditionQueryUnionInfoDTO> page, QueryConditionDTO dto, Long date);

    /**
     * 系統初始化用, 取得計算最新交易日詳細報告所需資料
     * @return
     */
    Map<String, DailyStockInfoDetailDTO> query4InitTodayDetail();
}
