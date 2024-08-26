package com.personal.project.scraperservice.scraper.webmagic.twse;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDTO;
import lombok.Getter;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TWSERoutinePipeline implements Pipeline {

	private final List<DailyStockInfoDTO> results = new ArrayList<>();

	@Override
	public void process(ResultItems resultItems, Task task) {
		results.addAll(resultItems.get("dtos"));
	}
}
