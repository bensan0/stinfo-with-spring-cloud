package com.personal.project.scraperservice.scraper.webmagic.twse;

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
public class TWSEInitPipeline implements Pipeline {

    @Getter
    private Map<String, List<DailyStockInfoDto>> result = Collections.synchronizedMap(new HashMap<>());

    @Getter
    private List<ScraperErrorMessageDO> errors = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void process(ResultItems resultItems, Task task) {
        try {
            if (resultItems.get("errors") != null) {
                errors.addAll(resultItems.get("errors"));
                return;
            }

            Pair<String, List<DailyStockInfoDto>> pair = resultItems.get("idToDTOs");

            if (result.get(pair.getLeft()) == null) {
                result.put(pair.getLeft(), pair.getRight());
            } else {
                result.get(pair.getLeft()).addAll(pair.getRight());
            }
        } catch (Exception e) {
            ScraperErrorMessageDO error = new ScraperErrorMessageDO();
            error.setErrorMessage(StrUtil.format("TWSE init pipeline error"));
            error.setException(e.getClass().getSimpleName());
            error.setExceptionMessage(e.getMessage());
            error.setScraperName("TWSE init pipeline");
            error.setDate(LocalDate.now().format(DatePattern.PURE_DATE_FORMATTER));
            errors.add(error);

            log.error("TWSE init pipeline error", e);
        }
    }
}
