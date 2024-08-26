package com.personal.project.scraperservice.scraper.webmagic.tpex;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDTO;
import lombok.Getter;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.List;

public class TPEXRoutinePipeline implements Pipeline {

    @Getter
    private List<DailyStockInfoDTO> results = new ArrayList<>();

    @Override
    public void process(ResultItems resultItems, Task task) {
        results.addAll(resultItems.get("dtos"));
    }
}
