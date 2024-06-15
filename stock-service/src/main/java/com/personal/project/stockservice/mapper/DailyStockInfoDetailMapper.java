package com.personal.project.stockservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DailyStockInfoDetailMapper extends BaseMapper<DailyStockInfoDetailDO> {

    List<DailyStockInfoDetailDTO> query4CalDetail(Query4CalDTO query4CalDTO);

    List<DailyStockInfoDetailDTO> query4CalDetailManual(ManualCalDTO manualCalDTO);

    IPage<ConditionQueryUnionInfoDTO> queryCondition(Page<ConditionQueryUnionInfoDTO> page, @Param("dto") QueryConditionDTO dto, @Param("date") Long date);

    List<DailyStockInfoDetailDTO> query4InitTodayDetail();
}
