package com.personal.project.scraperservice.scraper.webmagic.goofinfo;

import lombok.Getter;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GoodInfoStockListPipeline implements Pipeline {

    @Getter
    private final Map<String, Map<String, String>> result = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void process(ResultItems resultItems, Task task) {

        Map<String, Map<String, String>> marketToObj = resultItems.get("marketToObj");
        result.putAll(marketToObj);
    }
}
