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
public class CompareTo4DaysAgoTradingAmountRule extends AbstractDailyDetailCalculateRule {

    @Override
    public void cal(DailyStockInfoDetailDTO todayDetail, DailyStockInfoDetailDTO yesterdayDetail, List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS) {
        StockInfo4CalDetailDTO todayInfoDTO = stockInfo4CalDetailDTOS.stream()
                .filter(dto -> dto.getSequence() == 1)
                .findFirst().get();

        StockInfo4CalDetailDTO fDaysAgoInfoDTO = stockInfo4CalDetailDTOS.stream()
                .filter(dto -> dto.getSequence() == 5)
                .findFirst().orElseThrow(() -> new RuntimeException("4 days ago stock info not found"));

        BigDecimal diff4DaysAgoAmount = todayInfoDTO.getTodayTradingVolumeMoney().subtract(fDaysAgoInfoDTO.getTodayTradingVolumeMoney());
        String diff4DaysAgoAmountStatus;
        if (BigDecimal.ZERO.equals(diff4DaysAgoAmount)) {
            diff4DaysAgoAmountStatus = CommonTerm.UNCHANGED;
        } else if (BigDecimal.ZERO.compareTo(diff4DaysAgoAmount) > 0) {
            diff4DaysAgoAmountStatus = CommonTerm.FALL;
        } else {
            diff4DaysAgoAmountStatus = CommonTerm.RISE;
        }

        todayDetail.getTags().setTradingAmountVS4DaysAgo(new String[]{
                diff4DaysAgoAmount
                        .divide(fDaysAgoInfoDTO.getTodayTradingVolumeMoney(), 4, RoundingMode.FLOOR)
                        .multiply(BigDecimal.valueOf(100L))
                        .abs()
                        .toPlainString(),
                diff4DaysAgoAmountStatus
        });
    }

    @Override
    public RuntimeException generateException(Exception e) {
        log.error("Calculate daily detail - CompareTo4DaysAgoTradingAmount failed", e);
        return new RuntimeException(
                StrUtil.format(ERROR_MSG_TEMPLATE,
                        "Calculate daily detail - CompareTo4DaysAgoTradingAmount failed",
                        e.getClass().getSimpleName(),
                        e.getCause().getMessage())
        );
    }
}
