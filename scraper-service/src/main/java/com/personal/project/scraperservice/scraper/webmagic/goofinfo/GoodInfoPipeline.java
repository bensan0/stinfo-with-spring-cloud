package com.personal.project.scraperservice.scraper.webmagic.goofinfo;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.personal.project.scraperservice.constant.Term;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class GoodInfoPipeline implements Pipeline {

    @Getter
    private final List<DailyStockInfoDto> infos = Collections.synchronizedList(new ArrayList<>());

    @Getter
    private final List<ScraperErrorMessageDO> errors = Collections.synchronizedList(new ArrayList<>());

    private static final String Trillion = "兆";
    private static final String HundredMillion = "億";
    private static final String TenThousand = "萬";
    private static final String Thousand = "千";
    private static final String Hundred = "百";
    private static final String Ten = "十";


    @Override
    public void process(ResultItems resultItems, Task task) {
        DailyStockInfoDto info = new DailyStockInfoDto();
        try {
            info.setStockId(resultItems.get(Term.STOCK_ID.getFieldName()));
            info.setStockName(resultItems.get(Term.STOCK_NAME.getFieldName()));
            info.setDate(resultItems.get(Term.DATE.getFieldName()));
            info.setTodayClosingPrice(new BigDecimal(resultItems.get(Term.TODAY_CLOSING_PRICE.getFieldName()).toString().replace(",", "")));
            info.setYesterdayClosingPrice(new BigDecimal(resultItems.get(Term.YESTERDAY_CLOSING_PRICE.getFieldName()).toString().replace(",", "")));
            info.setPriceGap(new BigDecimal(resultItems.get(Term.PRICE_GAP.getFieldName()).toString()));
            info.setPriceGapPercent(Double.valueOf(resultItems.get(Term.PRICE_GAP_PERCENT.getFieldName()).toString().replace("%", "")));
            info.setOpeningPrice(new BigDecimal(resultItems.get(Term.OPENING_PRICE.getFieldName()).toString().replace(",", "")));
            info.setHighestPrice(new BigDecimal(resultItems.get(Term.HIGHEST_PRICE.getFieldName()).toString().replace(",", "")));
            info.setLowestPrice(new BigDecimal(resultItems.get(Term.LOWEST_PRICE.getFieldName()).toString().replace(",", "")));
            info.setTodayTradingVolumePiece(Long.valueOf(resultItems.get(Term.TODAY_TRADING_VOLUME_PIECE.getFieldName()).toString().replace(",", "")));
            info.setTodayTradingVolumeMoney(calculateChineseMoneyUnit(resultItems.get(Term.TODAY_TRADING_VOLUME_MONEY.getFieldName()).toString().replace(",", "")));
            info.setYesterdayTradingVolumePiece(Long.valueOf(resultItems.get(Term.YESTERDAY_TRADING_VOLUME_PIECE.getFieldName()).toString().replace(",", "")));
            info.setYesterdayTradingVolumeMoney(calculateChineseMoneyUnit(resultItems.get(Term.YESTERDAY_TRADING_VOLUME_MONEY.getFieldName()).toString().replace(",", "")));
            infos.add(info);
        } catch (Exception e) {
            log.error("{} 當日股票資訊轉換錯誤, 原始資料:{}", LocalDate.now(), JSON.toJSONString(resultItems.getAll()), e);
            ScraperErrorMessageDO error = new ScraperErrorMessageDO();
            error.setDate(LocalDateTime.now().toString());
            error.setErrorMessage(StrUtil.format("{} 當日股票資訊轉換錯誤, 原始資料:{}", LocalDate.now(), JSON.toJSONString(resultItems.getAll())));
            error.setException(e.getClass().getSimpleName());
            error.setScraperName("goodinfo pipeline");
            error.setExceptionMessage(e.getMessage());
            errors.add(error);
        }
    }

    private BigDecimal calculateChineseMoneyUnit(String str) {
        str = str.replace(",", "").replace("元", "");
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (str.contains(Trillion)) {
            int index = str.indexOf(Trillion);
            if (index != -1) {
                totalAmount = totalAmount.add(
                        new BigDecimal(str.substring(0, index)).multiply(BigDecimal.valueOf(1000000000000L)));
                str = str.substring(index + 1);
            }

        }

        if (str.contains(HundredMillion)) {
            int index = str.indexOf(HundredMillion);
            if (index != -1) {
                totalAmount = totalAmount.add(
                        new BigDecimal(str.substring(0, index)).multiply(BigDecimal.valueOf(100000000)));
                str = str.substring(index + 1);
            }
        }

        if (str.contains(TenThousand)) {
            int index = str.indexOf(TenThousand);
            if (index != -1) {
                totalAmount = totalAmount.add(
                        new BigDecimal(str.substring(0, index)).multiply(BigDecimal.valueOf(10000)));
                str = str.substring(index + 1);
            }
        }

        if (str.contains(Thousand)) {
            int index = str.indexOf(Thousand);
            if (index != -1) {
                totalAmount = totalAmount.add(
                        new BigDecimal(str.substring(0, index)).multiply(BigDecimal.valueOf(1000)));
                str = str.substring(index + 1);
            }
        }

        if (str.contains(Hundred)) {
            int index = str.indexOf(Hundred);
            if (index != -1) {
                totalAmount = totalAmount.add(
                        new BigDecimal(str.substring(0, index)).multiply(BigDecimal.valueOf(100)));
                str = str.substring(index + 1);
            }
        }

        if (str.contains(Ten)) {
            int index = str.indexOf(Ten);
            if (index != -1) {
                totalAmount = totalAmount.add(
                        new BigDecimal(str.substring(0, index)).multiply(BigDecimal.valueOf(10)));
                str = str.substring(index + 1);
            }
        }

        totalAmount = str.isEmpty() ? totalAmount : totalAmount.add(new BigDecimal(str));

        return totalAmount;
    }
}
