package com.personal.project.scraperservice.scraper.webmagic.yahoo;

import lombok.Getter;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

@Getter
public class YahooRealTimeDateCheckPipeline implements Pipeline {

	private long pageDate;

	@Override
	public void process(ResultItems resultItems, Task task) {
		pageDate = resultItems.get("date");
	}
}
