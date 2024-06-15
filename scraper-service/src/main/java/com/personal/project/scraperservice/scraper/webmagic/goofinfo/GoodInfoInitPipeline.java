package com.personal.project.scraperservice.scraper.webmagic.goofinfo;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class GoodInfoInitPipeline implements Pipeline {

    @Getter
    private final List<List<DailyStockInfoDto>> infos = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void process(ResultItems resultItems, Task task) {
        List<DailyStockInfoDto> dtos = resultItems.get("DTOs");
        infos.add(dtos);
    }
}
