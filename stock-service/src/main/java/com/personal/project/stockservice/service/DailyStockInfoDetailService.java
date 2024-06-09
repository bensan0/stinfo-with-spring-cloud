package com.personal.project.stockservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;

import java.util.List;

public interface DailyStockInfoDetailService extends IService<DailyStockInfoDetailDO> {

    /**
     * 獲取前一個交易日個股標籤數據
     *
     * @return
     */
    List<DailyStockInfoDetailDTO> query4CalDetail(Query4CalDTO query4CalDTO);

    /**
     * 獲取前一個交易日個股標籤數據
     *
     * @return
     */
    List<DailyStockInfoDetailDTO> query4CalDetail(ManualCalDTO manualCalDTO);

    /**
     * 根據日期獲取指標數據(id, stock_id)
     *
     * @param date
     * @return
     */
    List<SimpleDetailDTO> queryDetail(Long date);

    IPage<ConditionQueryUnionInfoDTO> queryCondition(Page<ConditionQueryUnionInfoDTO> page, QueryConditionDTO dto, Long date);
}
