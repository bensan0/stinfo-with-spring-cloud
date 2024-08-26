package com.personal.project.scraperservice.scraper.webmagic.yahoo;

import com.personal.project.scraperservice.model.dto.DailyIndexInfoDTO;
import lombok.Getter;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class YahooRealTimeIndexPipeline implements Pipeline {

	private final List<DailyIndexInfoDTO> results = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void process(ResultItems resultItems, Task task) {
		DailyIndexInfoDTO dto = resultItems.get("dto");

		results.add(dto);
	}
}
