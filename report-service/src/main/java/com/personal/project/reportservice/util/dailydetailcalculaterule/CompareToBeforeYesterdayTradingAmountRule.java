package com.personal.project.reportservice.util.dailydetailcalculaterule;

import cn.hutool.core.util.StrUtil;
import com.personal.project.commoncore.constants.CommonTerm;
import com.personal.project.reportservice.model.dto.StockInfo4CalDetailDTO;
import com.personal.project.reportservice.model.dto.DailyStockInfoDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@Slf4j
public class CompareToBeforeYesterdayTradingAmountRule extends AbstractDailyDetailCalculateRule{

    /**
     * 對比往前第二個交易日交易額
     * @param todayDetail
     * @param yesterdayDetail
     * @param stockInfo4CalDetailDTOS
     */
    @Override
    public void cal(DailyStockInfoDetailDTO todayDetail, DailyStockInfoDetailDTO yesterdayDetail, List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS) {

        StockInfo4CalDetailDTO todayInfoDTO = stockInfo4CalDetailDTOS.stream()
                .filter(dto -> dto.getSequence() == 1)
                .findFirst().get();

        StockInfo4CalDetailDTO beforeYesterdayInfoDTO = stockInfo4CalDetailDTOS.stream()
                .filter(dto -> dto.getSequence() == 3)
                .findFirst().orElseThrow(() -> new RuntimeException("Day before yesterday stock info not found"));

        BigDecimal diffBeforeYesterdayTradingAmount = todayInfoDTO.getTodayTradingVolumeMoney().subtract(beforeYesterdayInfoDTO.getTodayTradingVolumeMoney());
        String diffBeforeYesterdayTradingAmountStatus;
        if (BigDecimal.ZERO.equals(diffBeforeYesterdayTradingAmount)) {
            diffBeforeYesterdayTradingAmountStatus = CommonTerm.UNCHANGED;
        } else if (BigDecimal.ZERO.compareTo(diffBeforeYesterdayTradingAmount) > 0) {
            diffBeforeYesterdayTradingAmountStatus = CommonTerm.FALL;
        } else {
            diffBeforeYesterdayTradingAmountStatus = CommonTerm.RISE;
        }

        todayDetail.getTags().setTradingAmountVS2DaysAgo(new String[]{
                diffBeforeYesterdayTradingAmount
                        .divide(beforeYesterdayInfoDTO.getTodayTradingVolumeMoney(), 4, RoundingMode.FLOOR)
                        .multiply(BigDecimal.valueOf(100))
                        .abs()
                        .toPlainString(),
                diffBeforeYesterdayTradingAmountStatus
        });
    }

    @Override
    public RuntimeException generateException(Exception e) {
        log.error("Calculate daily detail - CompareToBeforeYesterdayTradingAmount failed", e);
        return new RuntimeException(
                StrUtil.format(ERROR_MSG_TEMPLATE,
                        "Calculate daily detail - CompareToBeforeYesterdayTradingAmount failed",
                        e.getClass().getSimpleName(),
                        e.getCause().getMessage())
        );
    }
}
