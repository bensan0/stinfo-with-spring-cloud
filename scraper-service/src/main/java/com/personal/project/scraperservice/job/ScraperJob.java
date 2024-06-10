package com.personal.project.scraperservice.job;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import com.personal.project.scraperservice.remote.RemoteStockService;
import com.personal.project.scraperservice.scraper.webmagic.SeleniumDownloader;
import com.personal.project.scraperservice.scraper.webmagic.goofinfo.GoodInfoPipeline;
import com.personal.project.scraperservice.scraper.webmagic.goofinfo.GoodInfoScraper;
import com.personal.project.scraperservice.scraper.webmagic.twse.TWSEScraper;
import com.personal.project.scraperservice.service.ErrorMessageService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import us.codecraft.webmagic.Spider;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class ScraperJob {

    private final RemoteStockService remoteStockService;

    private final ErrorMessageService errorMessageService;
    private final View error;

    public ScraperJob(RemoteStockService remoteStockService, ErrorMessageService errorMessageService, View error) {
        this.remoteStockService = remoteStockService;
        this.errorMessageService = errorMessageService;
        this.error = error;
    }

    @XxlJob("goodinfo-scrape")
    public void GoodInfoJobHandler() {
        log.info("Crawling goodinfo, start");

        GoodInfoScraper scraper = new GoodInfoScraper();
        GoodInfoPipeline pipeline = new GoodInfoPipeline();

        Spider.create(scraper)
                //從2330開始抓
                .addUrl("https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=2330")
                .addPipeline(pipeline)
                //開3個執行緒執行
                .thread(3)
                //啟動爬蟲
                .run();

        List<DailyStockInfoDto> infos = pipeline.getInfos();
        if (infos.isEmpty()) {
            log.warn("{} GoodInfo爬蟲 需入庫資料量為0", LocalDate.now());
        }
        List<ScraperErrorMessageDO> errors = pipeline.getErrors();

        if(!errors.isEmpty()){
            errorMessageService.saveBatch(errors);
        }

        InnerResponse<ObjectUtils.Null> innerResponse = remoteStockService.saveAll(infos.stream().map(dsi -> BeanUtil.copyProperties(dsi, DailyStockInfoDto.class)).toList(), null);

        log.info("【GoodInfo爬蟲結束】共抓取{}筆資料，入庫回應: {}", infos.size(), JSON.toJSON(innerResponse));
    }

    //todo
    @XxlJob("twse")
    public void TWSEJobHandler() {
        log.info("Crawling twse, start");

        TWSEScraper ts = new TWSEScraper();

        log.info("【TWSE爬蟲結束】共抓取" + "?" + "筆資料" + "，已保存到数据库");
    }

    public static void main(String[] args) {
        String classpath = ClassPathUtils.class.getClassLoader().getResource("").getFile();
        GoodInfoScraper scraper = new GoodInfoScraper();
        GoodInfoPipeline pipeline = new GoodInfoPipeline();
        Spider.create(scraper)
                //从2330开始抓
                .addUrl("https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=1617")
                .addPipeline(pipeline)
                //开启5个线程抓取
                .thread(2)
                .setDownloader(new SeleniumDownloader(classpath + "driver/chrome-driver-mac-arm64/chromedriver"))
                .run();
//
//        List<DailyStockInfoDto> infos = pipeline.getInfos();
//        System.out.println(infos.getFirst());



    }
}
