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
public class ConsecutivePriceAndPriceStatusRule extends AbstractDailyDetailCalculateRule {

    /**
     * 金額連Ｎ, 狀態
     *
     * @param todayDetail
     * @param yesterdayDetail
     * @param stockInfo4CalDetailDTOS
     */
    @Override
    public void cal(DailyStockInfoDetailDTO todayDetail, DailyStockInfoDetailDTO yesterdayDetail, List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS) {
        StockInfo4CalDetailDTO todayInfoDTO = stockInfo4CalDetailDTOS.stream()
                .filter(dto -> dto.getSequence() == 1)
                .findFirst().get();

        String todayPriceStatus = switch (todayInfoDTO.getTodayClosingPrice().compareTo(todayInfoDTO.getYesterdayClosingPrice())) {
            case 1 -> CommonTerm.RISE;
            case 0 -> CommonTerm.UNCHANGED;
            default -> CommonTerm.FALL;
        };

        if (yesterdayDetail.getTags().getConsecutivePrice()[1].equals(todayPriceStatus)) {
            todayDetail.getTags().setConsecutivePrice(
                    new String[]{
                            String.valueOf(Integer.parseInt(todayDetail.getTags().getConsecutivePrice()[0]) + 1),
                            todayPriceStatus
                    }
            );
            todayDetail.getTags().setPriceStatus(todayPriceStatus);
        } else {
            todayDetail.getTags().setPriceStatus(CommonTerm.TURN_TO + todayPriceStatus);
            todayDetail.getTags().setConsecutivePrice(
                    new String[]{
                            "1",
                            todayPriceStatus
                    }
            );
        }
    }

    @Override
    public RuntimeException generateException(Exception e) {
        log.error("Calculate daily detail - ConsecutivePriceAndPriceStatus failed", e);
        return new RuntimeException(
                StrUtil.format(ERROR_MSG_TEMPLATE,
                        "Calculate daily detail - ConsecutivePriceAndPriceStatus failed",
                        e.getClass().getSimpleName(),
                        e.getCause().getMessage())
        );
    }
}
