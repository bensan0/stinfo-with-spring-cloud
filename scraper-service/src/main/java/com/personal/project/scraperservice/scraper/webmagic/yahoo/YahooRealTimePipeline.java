package com.personal.project.scraperservice.scraper.webmagic.yahoo;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import lombok.Getter;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YahooRealTimePipeline implements Pipeline {

    @Getter
    private List<DailyStockInfoDto> results = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void process(ResultItems resultItems, Task task) {
        results.addAll(resultItems.get("dtos"));
    }
}
