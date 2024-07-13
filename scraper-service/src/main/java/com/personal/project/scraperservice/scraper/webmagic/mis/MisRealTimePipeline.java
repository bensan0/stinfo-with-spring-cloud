package com.personal.project.scraperservice.scraper.webmagic.mis;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import lombok.Getter;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.*;

public class MisRealTimePipeline implements Pipeline {

    @Getter
    private final List<DailyStockInfoDto> results = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void process(ResultItems resultItems, Task task) {
        List<DailyStockInfoDto> dtos = resultItems.get("dtos");

        results.addAll(dtos);
    }
}
