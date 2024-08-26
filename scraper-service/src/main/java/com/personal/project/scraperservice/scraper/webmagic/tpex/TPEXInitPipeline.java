package com.personal.project.scraperservice.scraper.webmagic.tpex;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.*;

@Slf4j
public class TPEXInitPipeline implements Pipeline {

    @Getter
    private Map<String, List<DailyStockInfoDTO>> result = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void process(ResultItems resultItems, Task task) {
        List<DailyStockInfoDTO> dtos = resultItems.get("dtos");
        dtos.forEach(d -> result.computeIfAbsent(d.getStockId(), k -> new ArrayList<>()).add(d));
    }
}
