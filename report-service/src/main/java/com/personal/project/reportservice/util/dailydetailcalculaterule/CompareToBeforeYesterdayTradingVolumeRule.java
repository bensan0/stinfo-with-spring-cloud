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
public class CompareToBeforeYesterdayTradingVolumeRule extends AbstractDailyDetailCalculateRule{

    /**
     * 往前第二個交易日交易量
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

        long diffBeforeYesterdayTradingVolume = todayInfoDTO.getTodayTradingVolumePiece() - beforeYesterdayInfoDTO.getTodayTradingVolumePiece();
        String diffBeforeYesterdayTradingVolumeStatus;
        if (0 == diffBeforeYesterdayTradingVolume) {
            diffBeforeYesterdayTradingVolumeStatus = CommonTerm.UNCHANGED;
        } else if (diffBeforeYesterdayTradingVolume < 0) {
            diffBeforeYesterdayTradingVolumeStatus = CommonTerm.FALL;
        } else {
            diffBeforeYesterdayTradingVolumeStatus = CommonTerm.RISE;
        }

        todayDetail.getTags().setTradingVolumeVS2DaysAgo(new String[]{
                BigDecimal.valueOf(diffBeforeYesterdayTradingVolume)
                        .divide(BigDecimal.valueOf(beforeYesterdayInfoDTO.getTodayTradingVolumePiece()), 4, RoundingMode.FLOOR)
                        .multiply(BigDecimal.valueOf(100))
                        .abs()
                        .toPlainString(),
                diffBeforeYesterdayTradingVolumeStatus
        });
    }

    @Override
    public RuntimeException generateException(Exception e) {
        log.error("Calculate daily detail - CompareToBeforeYesterdayTradingVolume failed", e);
        return new RuntimeException(
                StrUtil.format(ERROR_MSG_TEMPLATE,
                        "Calculate daily detail - CompareToBeforeYesterdayTradingVolume failed",
                        e.getClass().getSimpleName(),
                        e.getCause().getMessage())
        );
    }
}
