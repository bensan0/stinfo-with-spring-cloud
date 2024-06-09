package com.personal.project.reportservice.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.personal.project.commoncore.constants.CommonTerm;
import com.personal.project.reportservice.job.ReportJob;
import com.personal.project.reportservice.model.dto.*;
import com.personal.project.reportservice.model.entity.ReportErrorMessageDO;
import com.personal.project.reportservice.service.ReportErrorMessageService;
import com.personal.project.reportservice.util.dailydetailcalculaterule.AbstractDailyDetailCalculateRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class ReportHelper {

    private final ReportErrorMessageService reportErrorMessageService;

    private final ObjectProvider<AbstractDailyDetailCalculateRule> ruleProvider;

    public ReportHelper(ReportErrorMessageService reportErrorMessageService,
                        ObjectProvider<AbstractDailyDetailCalculateRule> ruleProvider
    ) {
        this.reportErrorMessageService = reportErrorMessageService;
        this.ruleProvider = ruleProvider;
    }

    /**
     * 產出每日個股各項指標數據
     */
    public List<DailyStockMetricsDTO> calDailyMetrics(CalMetricsUnionDTO dto) {
        List<CalMetricsDTO> calMetricsInfos = dto.getCalMetricsInfo();//當日收盤資料 & 昨日指標
        List<PastClosingPriceDTO> pastClosingPrices = dto.getPastClosingPrice();//當日起算, 最早用於計算ＭＡ之價格資料
        List<SimpleMetricsDTO> todayExistedMetrics = dto.getTodayExistedMetrics();//當日已存在的指標數據(定時任務前手動計算)

        // stockId -> [5 -> {PastClosingPriceDTO}, 10 -> {PastClosingPriceDTO} ...]
        Map<String, Map<Integer, PastClosingPriceDTO>> idToPastPrice = new HashMap<>();
        pastClosingPrices.forEach(past -> {
            String stockId = past.getStockId();
            idToPastPrice.computeIfAbsent(stockId, k -> new HashMap<>());
            idToPastPrice.get(stockId).put(past.getMa(), past);
        });

        //Map<stockId, metrics id>
        Map<String, Long> stockIdToMetricsId = new HashMap<>();
        todayExistedMetrics.forEach(td -> stockIdToMetricsId.put(td.getStockId(), td.getMetricsId()));

        //計算
        //todo rsi 計算
        List<DailyStockMetricsDTO> todayMetricsDTOs = new ArrayList<>();
        List<ReportErrorMessageDO> errorMsgs = new ArrayList<>();
        for (CalMetricsDTO info : calMetricsInfos) {
            Map<Integer, PastClosingPriceDTO> maToPastDTO = idToPastPrice.get(info.getStockId());
            DailyStockMetricsDTO todayMetricsDTO;

            if (info.getTodayClosingPrice() == null) {
                try {
                    todayMetricsDTO = DailyStockMetricsDTO.builder()
                            .id(stockIdToMetricsId.getOrDefault(info.getStockId(), null))
                            .stockId(info.getStockId())
                            .stockName(info.getStockName())
                            .date(info.getDate())
                            .ma5(info.getMa5())
                            .lastMA5price(info.getLastMA5Price())
                            .ma10(info.getMa10())
                            .lastMA10price(info.getLastMA10Price())
                            .ma20(info.getMa20())
                            .lastMA20price(info.getLastMA20Price())
                            .ma60(info.getMa60())
                            .lastMA60price(info.getLastMA60Price())
                            .ma120(info.getMa120())
                            .lastMA120price(info.getLastMA120Price())
                            .ma240(info.getMa240())
                            .lastMA240price(info.getLastMA240Price())
                            .build();
                    todayMetricsDTOs.add(todayMetricsDTO);

                    log.error("Calculate daily metrics warning, today closing price is null, stock: {}", info.getStockId());
                    ReportErrorMessageDO errMsg = new ReportErrorMessageDO();
                    errMsg.setReportName(ReportJob.DAILY_STOCK_METRICS);
                    errMsg.setDate(DateUtil.now());
                    errMsg.setExtra("warning stock id: " + info.getStockId());
                    errMsg.setErrorMessage("Calculate daily metrics warning, today closing price is null");
                    errorMsgs.add(errMsg);
                } catch (Exception e) {
                    log.error("Calculate daily metrics failed, stock: {}", info.getStockId(), e);
                    ReportErrorMessageDO errMsg = new ReportErrorMessageDO();
                    errMsg.setReportName(ReportJob.DAILY_STOCK_METRICS);
                    errMsg.setDate(DateUtil.now());
                    errMsg.setExtra("failed stock id: " + info.getStockId());
                    errMsg.setException(e.getClass().getSimpleName());
                    errMsg.setExceptionMessage(e.getCause().getMessage());
                    errorMsgs.add(errMsg);
                }
            } else {
                todayMetricsDTO = DailyStockMetricsDTO.builder()
                        .id(stockIdToMetricsId.getOrDefault(info.getStockId(), null))
                        .stockId(info.getStockId())
                        .stockName(info.getStockName())
                        .date(info.getDate())
                        .ma5(calNewMA(CommonTerm.MA5.longValue(), info.getMa5(), info.getLastMA5Price(), info.getTodayClosingPrice()))
                        .lastMA5price(maToPastDTO.get(CommonTerm.MA5).getClosingPrice())
                        .ma10(calNewMA(CommonTerm.MA10.longValue(), info.getMa10(), info.getLastMA10Price(), info.getTodayClosingPrice()))
                        .lastMA10price(maToPastDTO.get(CommonTerm.MA10).getClosingPrice())
                        .ma20(calNewMA(CommonTerm.MA20.longValue(), info.getMa20(), info.getLastMA20Price(), info.getTodayClosingPrice()))
                        .lastMA20price(maToPastDTO.get(CommonTerm.MA20).getClosingPrice())
                        .ma60(calNewMA(CommonTerm.MA60.longValue(), info.getMa60(), info.getLastMA60Price(), info.getTodayClosingPrice()))
                        .lastMA60price(maToPastDTO.get(CommonTerm.MA60).getClosingPrice())
                        .ma120(calNewMA(CommonTerm.MA120.longValue(), info.getMa120(), info.getLastMA120Price(), info.getTodayClosingPrice()))
                        .lastMA120price(maToPastDTO.get(CommonTerm.MA120).getClosingPrice())
                        .ma240(calNewMA(CommonTerm.MA240.longValue(), info.getMa240(), info.getLastMA240Price(), info.getTodayClosingPrice()))
                        .lastMA240price(maToPastDTO.get(CommonTerm.MA240).getClosingPrice())
                        .build();
                todayMetricsDTOs.add(todayMetricsDTO);
            }
        }

        //錯誤入庫
        if (!errorMsgs.isEmpty()) {
            Thread.startVirtualThread(() -> {
                boolean saved = reportErrorMessageService.saveBatch(errorMsgs);
                if (!saved) {
                    log.error("Calculate daily metrics save error message failed");
                }
            });
        }

        return todayMetricsDTOs;
    }

    public List<DailyStockInfoDetailDTO> calDailyDetail(CalDetailUnionDTO dto) {
        //今日, 上個交易日, 3個交易日前(含今日), 5個交易日前(含今日)個股資料
        List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOs = dto.getStockInfo4CalDetailDTOS();
        Map<String, List<StockInfo4CalDetailDTO>> stockIdToInfoDTOs = new HashMap<>();
        stockInfo4CalDetailDTOs.forEach(o -> {
            stockIdToInfoDTOs.computeIfAbsent(o.getStockId(), k -> new ArrayList<>());
            stockIdToInfoDTOs.get(o.getStockId()).add(o);
        });

        //今日個股指標 & 上一個交易日個股指標
        List<DailyStockMetricsDTO> dailyStockMetricsDTOs = dto.getDailyStockMetricsDTOs();
        Map<String, List<DailyStockMetricsDTO>> stockIdToMetricsDTOs = new HashMap<>();
        dailyStockMetricsDTOs.forEach(o -> {
            stockIdToMetricsDTOs.computeIfAbsent(o.getStockId(), k -> new ArrayList<>());
            stockIdToMetricsDTOs.get(o.getStockId()).add(o);
        });

        //前一個交易日個股標籤
        List<DailyStockInfoDetailDTO> dailyStockInfoDetailDTOs = dto.getDailyStockInfoDetailDTOs();
        Map<String, DailyStockInfoDetailDTO> stockIdToDetailDTOs = new HashMap<>();
        dailyStockInfoDetailDTOs.forEach(o -> stockIdToDetailDTOs.put(o.getStockId(), o));

        //
        List<SimpleDetailDTO> todayExistedDetailDTOs = dto.getTodayExistedDetailDTOs();
        Map<String, Long> stockIdToDetailId = new HashMap<>();
        todayExistedDetailDTOs.forEach(o -> stockIdToDetailId.put(o.getStockId(), o.getDetailId()));

        LocalDateTime now = LocalDateTimeUtil.now();
        String pureDte = now.format(DatePattern.PURE_DATE_FORMATTER);
        List<DailyStockInfoDetailDTO> results = new ArrayList<>();
        List<ReportErrorMessageDO> errors = new ArrayList<>();

        //開始計算
        stockIdToInfoDTOs.forEach((stockId, infoDTOs) -> {
            Optional<StockInfo4CalDetailDTO> optional = infoDTOs.stream()
                    .filter(o -> o.getSequence() == 1)
                    .findFirst();

            DailyStockInfoDetailDTO todayDetailDTO = new DailyStockInfoDetailDTO();
            todayDetailDTO.setId(stockIdToDetailId.getOrDefault(stockId, null));

            //檢查sequence 1的日期是否為今日, 檢查sequence 1是否存在
            if (optional.isEmpty() || !Long.valueOf(pureDte).equals(optional.get().getDate())) {
                log.error("Cal daily detail failed, No sequence 1 info or date not match, {}", optional.map(JSONUtil::toJsonStr).orElse(null));
                ReportErrorMessageDO errMsg = new ReportErrorMessageDO();
                errMsg.setReportName(ReportJob.DAILY_STOCK_DETAIL);
                errMsg.setDate(DateUtil.now());
                errMsg.setErrorMessage(StrUtil.format("No sequence 1 info or date not match, {}",
                        optional.map(JSONUtil::toJsonStr).orElse(null)));
                errors.add(errMsg);
                return;
            }

            StockInfo4CalDetailDTO todayInfo = optional.get();

            //若今日的stockInfo收盤價為空, 直接複製昨日detail
            if (todayInfo.getTodayClosingPrice() == null) {
                log.warn("Cal daily detailed failed, today closing price is null, {}", JSONUtil.toJsonStr(optional.get()));

                ReportErrorMessageDO errMsg = new ReportErrorMessageDO();
                errMsg.setReportName(ReportJob.DAILY_STOCK_DETAIL);
                errMsg.setDate(DateUtil.now());
                errMsg.setErrorMessage(StrUtil.format("Cal daily detailed failed, today closing price is null, {}",
                        optional.get()));
                errors.add(errMsg);

                DailyStockInfoDetailDTO yesterdayDetailDTO = stockIdToDetailDTOs.get(stockId);
                BeanUtil.copyProperties(yesterdayDetailDTO, todayDetailDTO, "id");
                todayDetailDTO.setDate(Long.valueOf(pureDte));
                results.add(todayDetailDTO);

                return;
            }

            //K棒計算
            BigDecimal range = todayInfo.getHighestPrice().subtract(todayInfo.getLowestPrice());
            BigDecimal highestPrice = todayInfo.getHighestPrice();
            BigDecimal lowestPrice = todayInfo.getLowestPrice();
            BigDecimal openingPrice = todayInfo.getOpeningPrice();
            BigDecimal todayClosingPrice = todayInfo.getTodayClosingPrice();
            todayDetailDTO.setUpperShadow(highestPrice.subtract(openingPrice.max(todayClosingPrice)).divide(range, 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));
            todayDetailDTO.setRealBody(openingPrice.subtract(todayClosingPrice).divide(range, 4, RoundingMode.FLOOR).abs().multiply(BigDecimal.valueOf(100)));
            todayDetailDTO.setLowerShadow(openingPrice.min(todayClosingPrice).subtract(lowestPrice).divide(range, 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));

            //3日, 5日對比計算
            try {
                for (AbstractDailyDetailCalculateRule rule : ruleProvider) {
                    rule.execute(
                            todayDetailDTO,
                            stockIdToDetailDTOs.get(stockId),
                            stockIdToInfoDTOs.get(stockId)
                    );
                }
            } catch (Exception e) {
                log.error("Cal daily detail rule failed, stock: {}", stockId, e);
                ReportErrorMessageDO errMsg = new ReportErrorMessageDO();
                errMsg.setReportName(ReportJob.DAILY_STOCK_DETAIL);
                errMsg.setDate(DateUtil.now());
                errMsg.setExtra("failed stock id: " + stockId);
                errMsg.setException(e.getClass().getSimpleName());
                errMsg.setExceptionMessage(e.getCause().getMessage());
                errors.add(errMsg);
                return;
            }

            //extra tags計算
            try {
                StockInfo4CalDetailDTO yesterdayInfo = infoDTOs.stream().filter(o -> o.getSequence() == 2).findFirst().get();
                List<String> tags = TagHelper.generateTags(pureDte, todayDetailDTO, todayInfo, yesterdayInfo, stockIdToMetricsDTOs.get(stockId));
                todayDetailDTO.getTags().setExtraTags(tags);
            } catch (Exception e) {
                log.error("Cal daily detail extra tags failed, stock: {}", stockId, e);
                ReportErrorMessageDO errMsg = new ReportErrorMessageDO();
                errMsg.setReportName(ReportJob.DAILY_STOCK_DETAIL);
                errMsg.setDate(DateUtil.now());
                errMsg.setExtra("failed stock id: " + stockId);
                errMsg.setException(e.getClass().getSimpleName());
                errMsg.setExceptionMessage(e.getCause().getMessage());
                errors.add(errMsg);
                return;
            }

            results.add(todayDetailDTO);
        });

        if (!errors.isEmpty()) {
            Thread.startVirtualThread(() -> {
                boolean saved = reportErrorMessageService.saveBatch(errors);
                if (!saved) {
                    log.error("Calculate daily detail save error message failed");
                }
            });
        }

        return results;
    }

    private BigDecimal calNewMA(long ma, BigDecimal formerMA, BigDecimal lastClosingPrice, BigDecimal nowClosingPrice) {

        return formerMA.multiply(BigDecimal.valueOf(ma))
                .subtract(lastClosingPrice)
                .add(nowClosingPrice)
                .subtract(BigDecimal.valueOf(ma))
                .setScale(2, RoundingMode.DOWN);
    }
}