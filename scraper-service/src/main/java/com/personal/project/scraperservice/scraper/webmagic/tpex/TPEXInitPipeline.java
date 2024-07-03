package com.personal.project.scraperservice.scraper.webmagic.tpex;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.time.LocalDate;
import java.util.*;

@Slf4j
public class TPEXInitPipeline implements Pipeline {

    @Getter
    private Map<String, List<DailyStockInfoDto>> result = Collections.synchronizedMap(new HashMap<>());

    @Getter
    private List<ScraperErrorMessageDO> errors = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void process(ResultItems resultItems, Task task) {
        try {
            Pair<String, List<DailyStockInfoDto>> pair = resultItems.get("idToDTOs");

            result.computeIfAbsent(pair.getLeft(), k -> new ArrayList<>()).addAll(pair.getRight());
        } catch (Exception e) {
            ScraperErrorMessageDO error = new ScraperErrorMessageDO();
            error.setErrorMessage(StrUtil.format("TPEX init pipeline error"));
            error.setException(e.getClass().getSimpleName());
            error.setExceptionMessage(e.getMessage());
            error.setScraperName("TPEX init pipeline");
            error.setDate(LocalDate.now().format(DatePattern.PURE_DATE_FORMATTER));
            errors.add(error);

            log.error("TPEX init pipeline error", e);
        }
    }
}
