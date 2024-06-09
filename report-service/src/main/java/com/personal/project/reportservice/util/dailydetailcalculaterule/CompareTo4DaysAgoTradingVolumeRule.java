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
public class CompareTo4DaysAgoTradingVolumeRule extends AbstractDailyDetailCalculateRule{

    @Override
    public void cal(DailyStockInfoDetailDTO todayDetail, DailyStockInfoDetailDTO yesterdayDetail, List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS) {
        StockInfo4CalDetailDTO todayInfoDTO = stockInfo4CalDetailDTOS.stream()
                .filter(dto -> dto.getSequence() == 1)
                .findFirst().get();

        StockInfo4CalDetailDTO fDaysAgoInfoDTO = stockInfo4CalDetailDTOS.stream()
                .filter(dto -> dto.getSequence() == 5)
                .findFirst().orElseThrow(() -> new RuntimeException("4 days ago stock info not found"));

        long diff4DaysAgoVolume = todayInfoDTO.getTodayTradingVolumePiece() - fDaysAgoInfoDTO.getTodayTradingVolumePiece();
        String diff4DaysAgoVolumeStatus;
        if (0 == diff4DaysAgoVolume) {
            diff4DaysAgoVolumeStatus = CommonTerm.UNCHANGED;
        } else if (0 > diff4DaysAgoVolume) {
            diff4DaysAgoVolumeStatus = CommonTerm.FALL;
        } else {
            diff4DaysAgoVolumeStatus = CommonTerm.RISE;
        }

        todayDetail.getTags().setTradingVolumeVS4DaysAgo(new String[]{
                BigDecimal.valueOf(diff4DaysAgoVolume)
                        .divide(BigDecimal.valueOf(fDaysAgoInfoDTO.getTodayTradingVolumePiece()), 4, RoundingMode.FLOOR)
                        .multiply(BigDecimal.valueOf(100L))
                        .abs()
                        .toPlainString(),
                diff4DaysAgoVolumeStatus
        });
    }

    @Override
    public RuntimeException generateException(Exception e) {
        log.error("Calculate daily detail - CompareTo4DaysAgoTradingVolume failed", e);
        return new RuntimeException(
                StrUtil.format(ERROR_MSG_TEMPLATE,
                        "Calculate daily detail - CompareTo4DaysAgoTradingVolume failed",
                        e.getClass().getSimpleName(),
                        e.getCause().getMessage())
        );
    }
}
