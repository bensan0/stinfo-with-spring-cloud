package com.personal.project.reportservice.util.dailydetailcalculaterule;

import cn.hutool.core.util.StrUtil;
import com.personal.project.commoncore.constants.CommonTerm;
import com.personal.project.reportservice.model.dto.StockInfo4CalDetailDTO;
import com.personal.project.reportservice.model.dto.DailyStockInfoDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ConsecutiveTradingAmountRule extends AbstractDailyDetailCalculateRule {

    /**
     * 交易額連Ｎ
     * @param todayDetail
     * @param yesterdayDetail
     * @param stockInfo4CalDetailDTOS
     */
    @Override
    public void cal(DailyStockInfoDetailDTO todayDetail, DailyStockInfoDetailDTO yesterdayDetail, List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS) {

        StockInfo4CalDetailDTO todayInfoDTO = stockInfo4CalDetailDTOS.stream()
                .filter(dto -> dto.getSequence() == 1)
                .findFirst().get();

        String todayTradingAmountStatus = switch (todayInfoDTO.getTodayTradingVolumeMoney().compareTo(todayInfoDTO.getYesterdayTradingVolumeMoney())) {
            case 1 -> CommonTerm.RISE;
            case 0 -> CommonTerm.UNCHANGED;
            default -> CommonTerm.FALL;
        };

        if (yesterdayDetail.getTags().getConsecutiveTradingAmount()[1].equals(todayTradingAmountStatus)) {
            todayDetail.getTags().setConsecutiveTradingAmount(
                    new String[]{
                            String.valueOf(Integer.parseInt(todayDetail.getTags().getConsecutiveTradingAmount()[0]) + 1),
                            todayTradingAmountStatus
                    }
            );
        } else {
            todayDetail.getTags().setConsecutiveTradingAmount(
                    new String[]{
                            "1",
                            todayTradingAmountStatus
                    }
            );
        }
    }

    @Override
    public RuntimeException generateException(Exception e) {
        log.error("Calculate daily detail - ConsecutiveTradingAmount failed", e);
        return new RuntimeException(
                StrUtil.format(ERROR_MSG_TEMPLATE,
                        "Calculate daily detail - ConsecutiveTradingAmount failed",
                        e.getClass().getSimpleName(),
                        e.getCause().getMessage())
        );
    }
}
