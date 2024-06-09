package com.personal.project.scraperservice.job;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.remote.RemoteStockService;
import com.personal.project.scraperservice.scraper.webmagic.SeleniumDownloader;
import com.personal.project.scraperservice.scraper.webmagic.goofinfo.GoodInfoPipeline;
import com.personal.project.scraperservice.scraper.webmagic.goofinfo.GoodInfoScraper;
import com.personal.project.scraperservice.scraper.webmagic.twse.TWSEScraper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScraperJob {

    private final RemoteStockService remoteStockService;

    public ScraperJob(RemoteStockService remoteStockService) {
        this.remoteStockService = remoteStockService;
    }

    @XxlJob("goodinfo")
    public void GoodInfoJobHandler() {
        XxlJobHelper.log("Crawling goodinfo, start");

        GoodInfoScraper scraper = new GoodInfoScraper();
        GoodInfoPipeline pipeline = new GoodInfoPipeline();

        Spider.create(scraper)
                //从2330开始抓
                .addUrl("https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=2330")
                .addPipeline(pipeline)
                //开启5个线程抓取
                .thread(2)
                //启动爬虫
                .run();

        List<DailyStockInfoDto> infos = pipeline.getInfos();
        if (infos.isEmpty()) {
            XxlJobHelper.handleFail(LocalDate.now() + " GoodInfo爬蟲 需入庫資料量為0");
        }
        InnerResponse<ObjectUtils.Null> innerResponse = remoteStockService.saveAll(infos.stream().map(dsi -> BeanUtil.copyProperties(dsi, DailyStockInfoDto.class)).toList(), null);

        XxlJobHelper.log("【GoodInfo爬蟲結束】共抓取" + infos.size() + "筆資料" + "，入庫回應: " + JSON.toJSON(innerResponse));
    }

    //todo
    @XxlJob("twse")
    public void TWSEJobHandler() {
        XxlJobHelper.log("Crawling twse, start");

        TWSEScraper ts = new TWSEScraper();

        XxlJobHelper.log("【TWSE爬蟲結束】共抓取" + "?" + "筆資料" + "，已保存到数据库");
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
