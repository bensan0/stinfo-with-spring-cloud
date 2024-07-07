package com.personal.project.scraperservice.scraper.webmagic.goofinfo;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class GoodInfoPipeline implements Pipeline {

    @Getter
    private final List<DailyStockInfoDto> dtos = Collections.synchronizedList(new ArrayList<>());

    private static final String Trillion = "兆";
    private static final String HundredMillion = "億";
    private static final String TenThousand = "萬";
    private static final String Thousand = "千";
    private static final String Hundred = "百";
    private static final String Ten = "十";


    @Override
    public void process(ResultItems resultItems, Task task) {

        List<DailyStockInfoDto> dtos = resultItems.get("dtos");
        this.dtos.addAll(dtos);
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
