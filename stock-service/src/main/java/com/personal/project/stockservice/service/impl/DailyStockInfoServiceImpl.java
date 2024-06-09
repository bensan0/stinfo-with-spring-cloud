package com.personal.project.stockservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.stockservice.mapper.DailyStockInfoMapper;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDO;
import com.personal.project.stockservice.service.DailyStockInfoService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyStockInfoServiceImpl extends ServiceImpl<DailyStockInfoMapper, DailyStockInfoDO> implements DailyStockInfoService {

    private final ApplicationContext applicationContext;

    public DailyStockInfoServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean saveAll(List<DailyStockInfoDTO> data) {
        List<DailyStockInfoDO> pojoList = data.stream().map(dto -> BeanUtil.copyProperties(dto, DailyStockInfoDO.class)).toList();

        return applicationContext.getBean(DailyStockInfoService.class).saveBatch(pojoList);
    }

    @Override
    public List<PastClosingPriceDTO> queryPastClosingPrice4CalMetrics(Query4CalDTO query4CalDTO) {

        return baseMapper.getLastMaPriceInfo(query4CalDTO);
    }

    @Override
    public List<PastClosingPriceDTO> queryPastClosingPrice4CalMetrics(ManualCalDTO manualCalDTO) {
        return baseMapper.getLastMaPriceInfo4Manual(manualCalDTO);
    }

    @Override
    public List<StockInfo4CalDetailDTO> queryInfo4CalDetail(Query4CalDTO query4CalDto) {

        return baseMapper.queryInfo4CalDetail(query4CalDto);
    }

    @Override
    public List<StockInfo4CalDetailDTO> queryInfo4CalDetail(ManualCalDTO manualCalDTO) {

        return baseMapper.queryInfo4CalDetail4Manual(manualCalDTO);
    }
}
