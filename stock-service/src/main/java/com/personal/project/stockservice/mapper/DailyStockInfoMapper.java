package com.personal.project.stockservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personal.project.stockservice.model.dto.ManualCalDTO;
import com.personal.project.stockservice.model.dto.StockInfo4CalDetailDTO;
import com.personal.project.stockservice.model.dto.PastClosingPriceDTO;
import com.personal.project.stockservice.model.dto.Query4CalDTO;
import com.personal.project.stockservice.model.entity.DailyStockInfoDO;

import java.util.List;

public interface DailyStockInfoMapper extends BaseMapper<DailyStockInfoDO> {

    List<PastClosingPriceDTO> getLastMaPriceInfo(Query4CalDTO query4CalDTO);

    List<PastClosingPriceDTO> getLastMaPriceInfo4Manual(ManualCalDTO manualCalDTO);

    List<StockInfo4CalDetailDTO> queryInfo4CalDetail(Query4CalDTO query4CalDTO);

    List<StockInfo4CalDetailDTO> queryInfo4CalDetail4Manual(ManualCalDTO manualCalDTO);
}

