package com.personal.project.scraperservice.scraper.webmagic.mis;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDTO;
import lombok.Getter;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.*;

public class MisRealTimePipeline implements Pipeline {

    @Getter
    private final List<DailyStockInfoDTO> results = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void process(ResultItems resultItems, Task task) {
        List<DailyStockInfoDTO> dtos = resultItems.get("dtos");

        results.addAll(dtos);
    }
}
