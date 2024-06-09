package com.personal.project.reportservice.util.dailydetailcalculaterule;

import com.personal.project.reportservice.model.dto.StockInfo4CalDetailDTO;
import com.personal.project.reportservice.model.dto.DailyStockInfoDetailDTO;

import java.util.List;

public abstract class AbstractDailyDetailCalculateRule {

    static final String ERROR_MSG_TEMPLATE = "{}, e: {}, e.msg: {}";

    public void execute(DailyStockInfoDetailDTO todayDetail,
                        DailyStockInfoDetailDTO yesterdayDetail,
                        List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS) {
        try {
            check(todayDetail, yesterdayDetail, stockInfo4CalDetailDTOS);
            cal(todayDetail, yesterdayDetail, stockInfo4CalDetailDTOS);
        } catch (Exception e) {
            throw generateException(e);
        }
    }

    public void check(DailyStockInfoDetailDTO todayDetail,
                      DailyStockInfoDetailDTO yesterdayDetail,
                      List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS) {

    }


    public abstract void cal(DailyStockInfoDetailDTO todayDetail,
                             DailyStockInfoDetailDTO yesterdayDetail,
                             List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS);

    public abstract RuntimeException generateException(Exception e);


}
