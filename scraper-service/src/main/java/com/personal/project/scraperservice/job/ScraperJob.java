package com.personal.project.scraperservice.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import com.personal.project.scraperservice.remote.RemoteReportService;
import com.personal.project.scraperservice.remote.RemoteStockService;
import com.personal.project.scraperservice.scraper.webmagic.SeleniumDownloader;
import com.personal.project.scraperservice.scraper.webmagic.goofinfo.GoodInfoPipeline;
import com.personal.project.scraperservice.scraper.webmagic.goofinfo.GoodInfoScraper;
import com.personal.project.scraperservice.scraper.webmagic.goofinfo.GoodInfoStockListPipeline;
import com.personal.project.scraperservice.scraper.webmagic.goofinfo.GoodInfoStockListScraper;
import com.personal.project.scraperservice.scraper.webmagic.tpex.TPEXInitPipeline;
import com.personal.project.scraperservice.scraper.webmagic.tpex.TPEXInitScraper;
import com.personal.project.scraperservice.scraper.webmagic.twse.TWSEInitPipeline;
import com.personal.project.scraperservice.scraper.webmagic.twse.TWSEInitScraper;
import com.personal.project.scraperservice.service.ErrorMessageService;
import com.personal.project.scraperservice.service.impl.ErrorMessageServiceImpl;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Slf4j
public class ScraperJob {

    private final RemoteStockService remoteStockService;

    private final RemoteReportService remoteReportService;

    private final ErrorMessageService errorMessageService;

    private static final String classpath = ClassPathUtils.class.getClassLoader().getResource("").getFile();

    private static final String LISTED = "市";

    private static final String OTC = "櫃";
    private final ErrorMessageServiceImpl errorMessageServiceImpl;

    public ScraperJob(RemoteStockService remoteStockService, RemoteReportService remoteReportService, ErrorMessageService errorMessageService, ErrorMessageServiceImpl errorMessageServiceImpl) {
        this.remoteStockService = remoteStockService;
        this.errorMessageService = errorMessageService;
        this.remoteReportService = remoteReportService;
        this.errorMessageServiceImpl = errorMessageServiceImpl;
    }

    @XxlJob("testing")
    public void testing() {
        List<String> twseUrls = new ArrayList<>();
        TWSEInitScraper twseInitScraper = new TWSEInitScraper(twseUrls);
        TWSEInitPipeline twseInitPipeline = new TWSEInitPipeline();
        Spider spider = Spider.create(twseInitScraper)
//                .addUrl("https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY?date=20240504&stockNo=2547&response=html")
                .addUrl("https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY?date=20240705&stockNo=2548&response=html")
                .addPipeline(twseInitPipeline)
                .setDownloader(
                        new SeleniumDownloader(
                                classpath + "driver/chrome-driver-mac-arm64/chromedriver",
                                Duration.ofSeconds(10),
                                ExpectedConditions.or(
                                        ExpectedConditions.visibilityOfElementLocated(By.tagName("tbody")),
                                        ExpectedConditions.textToBe(By.xpath("/html/body/div"), "很抱歉，沒有符合條件的資料!")
                                ),
                                new HashSet<>()
                        )
                )
                .thread(3);

        spider.run();

        Map<String, List<DailyStockInfoDto>> twseResult = twseInitPipeline.getResult();
    }

    //半年線,年線都先暫不計算(routine任務關於這部分計算都先去除)
    //todo 另外分一個任務可帶參數去獲取更久以前的資料(以後計算年線半年線用)
    @XxlJob("twse-tpex-system-init-scrape")
    public void twseAndTPEXInitHandle() {
        int eachGroupLimit = 5; //一次爬蟲/入庫 要多少支個股為一組
        int offMonths = 6; //現在往前推幾個月～本月資料
        log.info("資料初始化任務 twse-tpex-system-init-scrape 開始執行");
        TimeInterval timer = DateUtil.timer();

        //獲取股票代號清單
        Map<String, Map<String, String>> stockListResult = scrapeGoodInfoInitStockList();
        log.info("資料初始化任務 twse-tpex-system-init-scrape 獲取股票代碼列表花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(stockListResult));

        List<String> existedIds = remoteStockService.getExist().getData();

        //上市股票代碼
        List<String> listedIds = stockListResult.get(LISTED).keySet().stream()
                .filter(StrUtil::isNumeric)
                .filter(id -> !existedIds.contains(id))
                .toList();

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> listedFuture = executorService.submit(() -> {
                //獲取上市股票
                for (int i = 0; i < listedIds.size(); i += eachGroupLimit) {
                    List<String> group = listedIds.stream().skip(i).limit(eachGroupLimit).toList();
                    log.info("資料初始化任務 twse-tpex-system-init-scrape, 上市個股：{}   爬蟲開始", group);

                    //產出下載地址
                    List<String> twseUrls = generateTWSEUrls(group, offMonths);

                    //爬蟲開始
                    TWSEInitScraper twseInitScraper = new TWSEInitScraper(twseUrls);
                    TWSEInitPipeline twseInitPipeline = new TWSEInitPipeline();

                    Triple<Map<String, List<DailyStockInfoDto>>, Set<String>, List<ScraperErrorMessageDO>> rawData = scrapeTWSEInitDailyStockInfo(classpath + "driver/chrome-driver-mac-arm64/chromedriver", twseInitScraper, twseInitPipeline);
                    Map<String, List<DailyStockInfoDto>> data = rawData.getLeft();

                    //問題網址重下載
                    Set<String> failedUrls = rawData.getMiddle();
                    List<ScraperErrorMessageDO> errors = null;
                    if (!failedUrls.isEmpty()) {
                        log.error("問題網址重下載: {}", failedUrls);
                        Triple<Map<String, List<DailyStockInfoDto>>, Set<String>, List<ScraperErrorMessageDO>> reDownloadRawData = scrapeTWSEInitDailyStockInfo(classpath + "driver/chrome-driver-mac-arm64/chromedriver", new TWSEInitScraper(failedUrls.stream().toList()), new TWSEInitPipeline());
                        if (reDownloadRawData != null) {
                            reDownloadRawData.getLeft().forEach((k, v) -> data.computeIfAbsent(k, key -> new ArrayList<>()).addAll(v));
                        }
                        if (!reDownloadRawData.getMiddle().isEmpty()) {
                            errors = reDownloadRawData.getRight();
                            log.error("問題網址重下載後還有問題的網址：{}", reDownloadRawData.getMiddle());
                        }
                    }

                    data.forEach((k, v) -> v.sort(Comparator.comparingLong(DailyStockInfoDto::getDate).reversed()));

                    //資料清洗
                    cleanupTWSEData(data);

                    //資料補全
                    completeTWSEData(data);

                    //填充
                    List<DailyStockInfoDto> finalResults = fillData(data);

                    //資料入庫
                    InnerResponse<ObjectUtils.Null> response = remoteStockService.initSaveAll(finalResults, null);
                    log.info("資料初始化任務 twse-tpex-system-init-scrape 上市個股：{}, 入庫結束, {}", group, JSON.toJSON(response));

                    //錯誤入庫
                    if (errors != null && !errors.isEmpty()) {
                        boolean saved = errorMessageService.saveBatch(errors);
                        log.info("資料初始化任務 twse-tpex-system-init-scrape 上市: {} 錯誤入庫結束, {}", group, saved);
                    } else {
                        log.info("資料初始化任務 twse-tpex-system-init-scrape 上市個股：{}, 無錯誤", group);
                    }
                }
            });

            //上櫃股票代碼
            List<String> otcIds = stockListResult.get(OTC).keySet().stream()
                    .filter(StrUtil::isNumeric)
                    .filter(id -> !existedIds.contains(id))
                    .toList();

            Future<?> otcFuture = executorService.submit(() -> {
                //5支一組入庫
                for (int i = 0; i < otcIds.size(); i += eachGroupLimit) {
                    List<String> group = otcIds.stream().skip(i).limit(eachGroupLimit).toList();
                    log.info("資料初始化任務 twse-tpex-system-init-scrape, 上櫃個股：{}   爬蟲開始", group);

                    //產出下載地址
                    List<String> tpexUrls = generateTPEXUrls(group, offMonths);

                    TPEXInitScraper tpexInitScraper = new TPEXInitScraper(tpexUrls);
                    TPEXInitPipeline tpexInitPipeline = new TPEXInitPipeline();

                    //爬蟲開始
                    Triple<Map<String, List<DailyStockInfoDto>>, Set<String>, List<ScraperErrorMessageDO>> rawData = scrapeTPEXInitDailyStockInfo(classpath + "driver/chrome-driver-mac-arm64/chromedriver", tpexInitScraper, tpexInitPipeline);

                    Map<String, List<DailyStockInfoDto>> data = rawData.getLeft();

                    //問題網址重下載
                    Set<String> failedUrls = rawData.getMiddle();
                    List<ScraperErrorMessageDO> errors = null;
                    if (!failedUrls.isEmpty()) {
                        log.error("問題網址重下載: {}", failedUrls);
                        Triple<Map<String, List<DailyStockInfoDto>>, Set<String>, List<ScraperErrorMessageDO>> reDownloadRawData = scrapeTWSEInitDailyStockInfo(classpath + "driver/chrome-driver-mac-arm64/chromedriver", new TWSEInitScraper(failedUrls.stream().toList()), new TWSEInitPipeline());
                        if (reDownloadRawData != null) {
                            reDownloadRawData.getLeft().forEach((k, v) -> data.computeIfAbsent(k, key -> new ArrayList<>()).addAll(v));
                        }
                        if (!reDownloadRawData.getMiddle().isEmpty()) {
                            errors = reDownloadRawData.getRight();
                            log.error("問題網址重下載後還有問題的網址：{}", reDownloadRawData.getMiddle());
                        }
                    }

                    data.forEach((k, v) -> v.sort(Comparator.comparingLong(DailyStockInfoDto::getDate).reversed()));

                    //清洗
                    cleanupTPEXData(data);

                    //補全
                    completeTPEXData(data);

                    //填充
                    List<DailyStockInfoDto> finalResults = fillData(data);

                    //資料入庫
                    InnerResponse<ObjectUtils.Null> response = remoteStockService.initSaveAll(finalResults, null);
                    log.info("資料初始化任務 twse-tpex-system-init-scrape 上櫃個股：{}, 入庫結束, {}", group, JSON.toJSON(response));

                    //錯誤入庫
                    if (errors != null && !errors.isEmpty()) {
                        boolean saved = errorMessageService.saveBatch(rawData.getRight());
                        log.info("資料初始化任務 twse-tpex-system-init-scrape 上櫃: {} 錯誤入庫結束, {}", group, saved);
                    } else {
                        log.info("資料初始化任務 twse-tpex-system-init-scrape 上櫃個股：{}, 無錯誤", group);
                    }
                }
            });

            listedFuture.get();
            otcFuture.get();

        } catch (InterruptedException | ExecutionException e) {
            log.error("oh my god", e);
        }

        log.error("資料初始化任務 twse-tpex-system-init-scrape 爬蟲部分結束, 花費 {} 毫秒", timer.intervalRestart());

        //call remote report to cal yesterday metric and detail
        InnerResponse<ObjectUtils.Null> response1 = remoteReportService.initYesterdayReport(null);
        log.info("資料初始化任務 twse-tpex-system-init-scrape 初始化上一個交易日的指標報告/詳細報告花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(response1));

        //call remote report to cal newest trading day metric and detail
        InnerResponse<ObjectUtils.Null> response2 = remoteReportService.initTodayReport(null);
        log.info("資料初始化任務 twse-tpex-system-init-scrape 初始化最新交易日的指標報告/詳細報告花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(response2));

        log.info("資料初始化任務 twse-tpex-system-init-scrape 結束");
    }

    /**
     * 系統初始化時, 從goodinfo爬取未來需要的股票代號清單
     *
     * @return ex: {"市" -> {"2330" -> "台積電", "0050" -> "元大ETF", ...}, "櫃" -> {"1742" -> "台蠟", ...} }
     */
    private Map<String, Map<String, String>> scrapeGoodInfoInitStockList() {
        GoodInfoStockListScraper listScraper = new GoodInfoStockListScraper();
        GoodInfoStockListPipeline listPipeline = new GoodInfoStockListPipeline();
        Spider.create(listScraper)
                //推測Spider設定的thread是單指一個網址而言, 若同時在Spider.addUrl設定多個網址,
                // 則每個網址會同時分配一條thread, 導致Spider.thread無意義(每個url會在同一時間以多條thread一起發出請求, 會被反爬搞), 因此應於PageProcessor內addTargetRequest的方式才是預想中使thread, sleep有效的方式
                //todo 待追source code驗證
//                .addUrl(stockListUrls.toArray(new String[0]))
                .addUrl("https://goodinfo.tw/tw2/StockList.asp?MARKET_CAT=上櫃&INDUSTRY_CAT=上櫃全部&SHEET=交易狀況&SHEET2=日&RPT_TIME=最新資料", "https://goodinfo.tw/tw2/StockList.asp?MARKET_CAT=上市&INDUSTRY_CAT=上市全部&SHEET=交易狀況&SHEET2=日&RPT_TIME=最新資料")
                .addPipeline(listPipeline)
                .setDownloader(
                        new SeleniumDownloader(classpath + "driver/chrome-driver-mac-arm64/chromedriver",
                                Duration.ofSeconds(5),
                                ExpectedConditions.visibilityOfElementLocated(By.id("tblStockList")),
                                new HashSet<>()
                        )
                )
                .thread(1)
                .run();

        return listPipeline.getResult();
    }

    /**
     * 產出TWSE個股日成交資訊地址
     *
     * @param stockIds
     * @param offMonths
     * @return
     */
    private List<String> generateTWSEUrls(List<String> stockIds, Integer offMonths) {
        List<String> twseUrls = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate lastYear = now.minusMonths(offMonths);
        LocalDate tempDate;
        for (String id : stockIds) {
            tempDate = now;
            while (lastYear.isBefore(tempDate)) {
                twseUrls.add(StrUtil.format("https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY?date={}&stockNo={}&response=html", tempDate.format(DatePattern.PURE_DATE_FORMATTER), id));
                tempDate = tempDate.minusMonths(1);
            }
        }

        return twseUrls;
    }

    /**
     * 爬TWSE(上市)個股日成交資訊
     *
     * @param twseInitScraper
     * @param twseInitPipeline
     * @return
     */
    private Triple<Map<String, List<DailyStockInfoDto>>, Set<String>, List<ScraperErrorMessageDO>> scrapeTWSEInitDailyStockInfo(String driverPath, TWSEInitScraper twseInitScraper, TWSEInitPipeline twseInitPipeline) {

        SeleniumDownloader downloader = new SeleniumDownloader(
                driverPath,
                Duration.ofSeconds(10),
                ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(By.tagName("tbody")),
                        ExpectedConditions.textToBePresentInElementLocated(By.xpath("/html/body/div"), "很抱歉，沒有符合條件的資料!")
                ),
                new HashSet<>()
        );

        Spider.create(twseInitScraper)
                .addUrl(twseInitScraper.getUrlsFirst())
                .addPipeline(twseInitPipeline)
                .setDownloader(downloader)
                .thread(3)
                .run();

        Map<String, List<DailyStockInfoDto>> twseResult = twseInitPipeline.getResult();
        Set<String> downloadingFailedUrls = downloader.getFailedUrls(); //下載時有問題地址
        List<String> analyzingFailedUrls = twseInitScraper.getFailedUrls(); //解析時有問題網址
        List<ScraperErrorMessageDO> analyzingErrors = twseInitScraper.getErrors();  //解析錯誤
        List<ScraperErrorMessageDO> pipelineErrors = twseInitPipeline.getErrors();//pipe line 錯誤

        downloadingFailedUrls.addAll(analyzingFailedUrls);
        analyzingErrors.addAll(pipelineErrors);

        return Triple.of(twseResult, downloadingFailedUrls, analyzingErrors);
    }

    /**
     * 清洗TWSE資料(補全高開低收)
     * TWSE資料於開高低收有可能為--, 於scraper中替換為-1, 此處替換為前一日收盤
     *
     * @param sortedData
     */
    private void cleanupTWSEData(Map<String, List<DailyStockInfoDto>> sortedData) {
        sortedData.forEach((k, v) -> {
            for (int i = 0; i < v.size(); i++) {
                DailyStockInfoDto dataLostDTO = v.get(i);
                if (dataLostDTO.getTodayClosingPrice().compareTo(BigDecimal.ZERO) < 0) {
                    List<DailyStockInfoDto> subList;
                    BigDecimal lastEffectivePrice = BigDecimal.valueOf(-1);
                    if (i == 0) {
                        subList = v.subList(1, v.size());
                        lastEffectivePrice = subList.stream()
                                .filter(dto -> dto.getTodayClosingPrice().compareTo(BigDecimal.ZERO) > 0)
                                .findFirst()
                                .map(DailyStockInfoDto::getTodayClosingPrice)
                                .orElse(lastEffectivePrice);
                        log.error("無效日期最新, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
                    } else if (i == v.size() - 1) {
                        subList = v.subList(0, i).reversed();
                        lastEffectivePrice = subList.stream()
                                .filter(dto -> dto.getTodayClosingPrice().compareTo(BigDecimal.ZERO) > 0)
                                .findFirst()
                                .map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
                                .orElse(lastEffectivePrice);
                        log.error("無效日期最舊, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
                    } else {
                        subList = v.subList(i, v.size());
                        lastEffectivePrice = subList.stream()
                                .filter(dto -> dto.getTodayClosingPrice().compareTo(BigDecimal.ZERO) > 0)
                                .findFirst()
                                .map(DailyStockInfoDto::getTodayClosingPrice)
                                .orElse(lastEffectivePrice);
                        if (lastEffectivePrice.compareTo(BigDecimal.valueOf(-1)) == 0) {
                            subList = v.subList(0, i).reversed();
                            lastEffectivePrice = subList.stream()
                                    .filter(dto -> dto.getTodayClosingPrice().compareTo(BigDecimal.ZERO) > 0)
                                    .findFirst()
                                    .map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
                                    .orElse(lastEffectivePrice);
                        }
                        log.error("無效日期中間, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
                    }
                    if (lastEffectivePrice.compareTo(BigDecimal.valueOf(-1)) == 0) {
                        log.error("上市沒救, 往前後找都ＧＧ, {}, {}", k, dataLostDTO.getDate());
                    }
                    dataLostDTO.setTodayClosingPrice(lastEffectivePrice);

                    if (dataLostDTO.getHighestPrice().compareTo(BigDecimal.ZERO) < 0) {
                        dataLostDTO.setHighestPrice(lastEffectivePrice);
                    }
                    if (dataLostDTO.getOpeningPrice().compareTo(BigDecimal.ZERO) < 0) {
                        dataLostDTO.setOpeningPrice(lastEffectivePrice);
                    }
                    if (dataLostDTO.getLowestPrice().compareTo(BigDecimal.ZERO) < 0) {
                        dataLostDTO.setLowestPrice(lastEffectivePrice);
                    }
                }
            }
        });
    }

    /**
     * 補全TWSE資料(昨額 昨收 昨量 差額 漲跌幅)
     *
     * @param sortedData
     */
    private void completeTWSEData(Map<String, List<DailyStockInfoDto>> sortedData) {
        sortedData.forEach((k, v) -> {
            DailyStockInfoDto tempDTO = null;
            for (int i = 0; i < v.size(); i++) {
                DailyStockInfoDto thisRoundDTO = v.get(i);
                //tempDTO填上缺失的昨日資訊
                if (tempDTO != null) {
                    tempDTO.setYesterdayClosingPrice(thisRoundDTO.getTodayClosingPrice());
                    tempDTO.setYesterdayTradingVolumePiece(thisRoundDTO.getTodayTradingVolumePiece());
                    tempDTO.setYesterdayTradingVolumeMoney(thisRoundDTO.getTodayTradingVolumeMoney());
                    tempDTO.setPriceGap(tempDTO.getTodayClosingPrice().subtract(tempDTO.getYesterdayClosingPrice()));
                    tempDTO.setPriceGapPercent(tempDTO.getPriceGap().divide(tempDTO.getYesterdayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));
                }
                tempDTO = thisRoundDTO;

                if (i == v.size() - 1) {
                    thisRoundDTO.setPriceGapPercent(null);
                    thisRoundDTO.setPriceGap(null);
                    thisRoundDTO.setYesterdayClosingPrice(null);
                }
            }
        });
    }

    /**
     * 填充平日但非交易日的資料
     */
    private List<DailyStockInfoDto> fillData(Map<String, List<DailyStockInfoDto>> sortedData) {
        List<DailyStockInfoDto> finalResults = new ArrayList<>();
        sortedData.forEach((k, v) -> {
            DailyStockInfoDto tempDTO = null;
            for (int i = 0; i < v.size(); i++) {
                DailyStockInfoDto thisRoundDTO = v.get(i);
                if (tempDTO != null) {
                    LocalDate tempDTODate = LocalDate.parse(tempDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);//0502
                    LocalDate thisDate = LocalDate.parse(thisRoundDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);//0430
                    if (!thisDate.isEqual(tempDTODate.minusDays(1)) &&
                            thisDate.getDayOfWeek() != DayOfWeek.FRIDAY) {
                        LocalDate flagDate = tempDTODate.minusDays(1);
                        while (!flagDate.isEqual(thisDate)) {
                            if (flagDate.getDayOfWeek() != DayOfWeek.SATURDAY && flagDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                DailyStockInfoDto fillDTO = new DailyStockInfoDto();
                                fillDTO.setStockId(k);
                                fillDTO.setStockName(thisRoundDTO.getStockName());
                                fillDTO.setDate(Long.valueOf(flagDate.format(DatePattern.PURE_DATE_FORMATTER)));
                                finalResults.add(fillDTO);
                            }
                            flagDate = flagDate.minusDays(1);
                        }
                    }
                }
                tempDTO = thisRoundDTO;
            }
            finalResults.addAll(v);
        });

        return finalResults;
    }

    /**
     * 爬TPEX(櫃買)個股日成交資訊
     *
     * @param driverPath
     * @param tpexInitScraper
     * @param tpexInitPipeline
     * @return
     */
    private Triple<Map<String, List<DailyStockInfoDto>>, Set<String>, List<ScraperErrorMessageDO>> scrapeTPEXInitDailyStockInfo(String driverPath, TPEXInitScraper tpexInitScraper, TPEXInitPipeline tpexInitPipeline) {

        SeleniumDownloader downloader = new SeleniumDownloader(
                driverPath, Duration.ofSeconds(10),
                ExpectedConditions.visibilityOfElementLocated(By.tagName("tbody")),
                new HashSet<>()
        );

        Spider.create(tpexInitScraper)
                .addUrl(tpexInitScraper.getUrlsFirst())
                .addPipeline(tpexInitPipeline)
                .setDownloader(downloader)
                .thread(3)
                .run();

        Map<String, List<DailyStockInfoDto>> tpexResult = tpexInitPipeline.getResult();
        Set<String> downloadingFailedUrls = downloader.getFailedUrls(); //下載時有問題地址
        List<String> analyzingFailedUrls = tpexInitScraper.getFailedUrls(); //解析時有問題網址
        List<ScraperErrorMessageDO> analyzingErrors = tpexInitScraper.getErrors();  //解析錯誤
        List<ScraperErrorMessageDO> pipelineErrors = tpexInitPipeline.getErrors();//pipe line 錯誤

        downloadingFailedUrls.addAll(analyzingFailedUrls);
        analyzingErrors.addAll(pipelineErrors);

        return Triple.of(tpexResult, downloadingFailedUrls, analyzingErrors);
    }

    /**
     * 產出TPEX個股日成交資訊地址
     *
     * @param stockIds
     * @param offMonths
     * @return
     */
    private List<String> generateTPEXUrls(List<String> stockIds, Integer offMonths) {
        List<String> tpexUrls = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate lastYear = now.minusMonths(offMonths);
        LocalDate tempDate;
        for (String id : stockIds) {
            tempDate = now;
            while (lastYear.isBefore(tempDate)) {
                tpexUrls.add(StrUtil.format("https://www.tpex.org.tw/web/stock/aftertrading/daily_trading_info/st43_print.php?l=zh-tw&d={}/{}&stkno={}&s=0,asc,0", tempDate.getYear() - 1911, tempDate.getMonth().getValue(), id));
                tempDate = tempDate.minusMonths(1);
            }
        }

        return tpexUrls;
    }

    /**
     * 清洗TPEX資料(補全高開低收)
     * TPEX資料於開高低收有可能為--, 於scraper中替換為-1, 此處替換為前一日收盤
     *
     * @param sortedData
     */
    private void cleanupTPEXData(Map<String, List<DailyStockInfoDto>> sortedData) {
        sortedData.forEach((k, v) -> {
            for (int i = 0; i < v.size(); i++) {
                DailyStockInfoDto dataLostDTO = v.get(i);
                if (dataLostDTO.getTodayClosingPrice().compareTo(BigDecimal.ZERO) < 0) {
                    List<DailyStockInfoDto> subList;
                    BigDecimal lastEffectivePrice = BigDecimal.valueOf(-1);
                    if (i == 0) {
                        subList = v.subList(1, v.size());
                        lastEffectivePrice = subList.stream()
                                .filter(dto -> dto.getTodayClosingPrice().compareTo(BigDecimal.ZERO) > 0)
                                .findFirst()
                                .map(DailyStockInfoDto::getTodayClosingPrice)
                                .orElse(lastEffectivePrice);
                        log.error("無效日期最新, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
                    } else if (i == v.size() - 1) {
                        subList = v.subList(0, i).reversed();
                        lastEffectivePrice = subList.stream()
                                .filter(dto -> dto.getTodayClosingPrice().compareTo(BigDecimal.ZERO) > 0)
                                .findFirst()
                                .map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
                                .orElse(lastEffectivePrice);
                        log.error("無效日期最舊, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
                    } else {
                        subList = v.subList(i, v.size());
                        lastEffectivePrice = subList.stream()
                                .filter(dto -> dto.getTodayClosingPrice().compareTo(BigDecimal.ZERO) > 0)
                                .findFirst()
                                .map(DailyStockInfoDto::getTodayClosingPrice)
                                .orElse(lastEffectivePrice);
                        if (lastEffectivePrice.compareTo(BigDecimal.valueOf(-1)) == 0) {
                            subList = v.subList(0, i).reversed();
                            lastEffectivePrice = subList.stream()
                                    .filter(dto -> dto.getTodayClosingPrice().compareTo(BigDecimal.ZERO) > 0)
                                    .findFirst()
                                    .map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
                                    .orElse(lastEffectivePrice);
                        }
                        log.error("無效日期中間, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
                    }
                    if (lastEffectivePrice.compareTo(BigDecimal.valueOf(-1)) == 0) {
                        log.error("上櫃沒救, 往前後找都ＧＧ, {}, {}", k, dataLostDTO.getDate());
                    }
                    dataLostDTO.setTodayClosingPrice(lastEffectivePrice);

                    if (dataLostDTO.getHighestPrice().compareTo(BigDecimal.ZERO) < 0) {
                        dataLostDTO.setHighestPrice(lastEffectivePrice);
                    }
                    if (dataLostDTO.getOpeningPrice().compareTo(BigDecimal.ZERO) < 0) {
                        dataLostDTO.setOpeningPrice(lastEffectivePrice);
                    }
                    if (dataLostDTO.getLowestPrice().compareTo(BigDecimal.ZERO) < 0) {
                        dataLostDTO.setLowestPrice(lastEffectivePrice);
                    }
                }
            }
        });
    }

    /**
     * 補全TPEX資料(差額, 昨收, 漲跌幅, 昨額 昨量)
     *
     * @param sortedData
     */
    private void completeTPEXData(Map<String, List<DailyStockInfoDto>> sortedData) {
        sortedData.forEach((k, v) -> {
            DailyStockInfoDto tempDTO = null;
            for (int i = 0; i < v.size(); i++) {
                DailyStockInfoDto thisRoundDTO = v.get(i);
                //tempDTO填上缺失的昨日資訊
                if (tempDTO != null) {
                    tempDTO.setYesterdayClosingPrice(thisRoundDTO.getTodayClosingPrice());
                    tempDTO.setYesterdayTradingVolumePiece(thisRoundDTO.getTodayTradingVolumePiece());
                    tempDTO.setYesterdayTradingVolumeMoney(thisRoundDTO.getTodayTradingVolumeMoney());
                    tempDTO.setPriceGap(tempDTO.getTodayClosingPrice().subtract(tempDTO.getYesterdayClosingPrice()));
                    tempDTO.setPriceGapPercent(tempDTO.getPriceGap().divide(tempDTO.getYesterdayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));
                }
                tempDTO = thisRoundDTO;

                if (i == v.size() - 1) {
                    thisRoundDTO.setPriceGapPercent(null);
                    thisRoundDTO.setPriceGap(null);
                    thisRoundDTO.setYesterdayClosingPrice(null);
                }
            }
        });
    }


    @XxlJob("goodinfo-routine-scrape")
    public void goodinfoJobHandle() {
        log.info("爬蟲任務 goodinfo-scrape 開始執行");
        LocalDate now = LocalDate.now();
        String nowStr = now.format(DatePattern.PURE_DATE_FORMATTER);
        GoodInfoScraper scraper = new GoodInfoScraper();
        GoodInfoPipeline pipeline = new GoodInfoPipeline();

        Spider.create(scraper)
                .addUrl("https://goodinfo.tw/tw2/StockList.asp?MARKET_CAT=上櫃&INDUSTRY_CAT=上櫃全部&SHEET=交易狀況&SHEET2=日&RPT_TIME=最新資料", "https://goodinfo.tw/tw2/StockList.asp?MARKET_CAT=上市&INDUSTRY_CAT=上市全部&SHEET=交易狀況&SHEET2=日&RPT_TIME=最新資料")
                .addPipeline(pipeline)
                //開3個執行緒執行
                .thread(1)
                .setDownloader(
                        new SeleniumDownloader(
                                classpath + "driver/chrome-driver-mac-arm64/chromedriver",
                                Duration.ofSeconds(5),
                                ExpectedConditions.visibilityOfElementLocated(By.id("tblStockList")),
                                new HashSet<>()
                        )
                )
                //啟動爬蟲
                .run();

        List<DailyStockInfoDto> dtos = pipeline.getDtos();
        List<ScraperErrorMessageDO> errors = pipeline.getErrors();

        if (!errors.isEmpty()) {
            errorMessageService.saveBatch(errors);
        }

        //獲取上個交易日資料, 填充缺失的昨張 昨額
        Map<String, DailyStockInfoDto> formers = remoteStockService.getFormer(null).getData();

        List<DailyStockInfoDto> results = new ArrayList<>();
        dtos.forEach(dto -> {
            //以防萬一平日國定假日而資料是昨日, 檢查日期
            if (!dto.getDate().toString().equals(nowStr)) {
                DailyStockInfoDto result = new DailyStockInfoDto();
                result.setStockId(dto.getStockId());
                result.setStockName(dto.getStockName());
                result.setDate(Long.parseLong(nowStr));
                results.add(result);
                return;
            }

            DailyStockInfoDto former = formers.get(dto.getStockId());
            dto.setYesterdayTradingVolumePiece(former.getTodayTradingVolumePiece());
            dto.setYesterdayTradingVolumeMoney(former.getTodayTradingVolumeMoney());
            results.add(dto);

            //為了檢查昨日有資料而今日無資料之股票
            formers.remove(dto.getStockId());
        });

        formers.forEach((k, v) -> {
            DailyStockInfoDto result = new DailyStockInfoDto();
            result.setStockId(k);
            result.setStockName(v.getStockName());
            result.setDate(Long.parseLong(nowStr));
            results.add(result);
        });

        //檢查今日執行定時任務前有無被手動按過
        JSONObject json = new JSONObject();
        json.set("date", nowStr);
        Map<String, DailyStockInfoDto> stockIdToInfo = remoteStockService.getByDate(json.toString(), null).getData();

        dtos.forEach(dto -> {
            if (stockIdToInfo.get(dto.getStockId()) != null) {
                dto.setId(stockIdToInfo.get(dto.getStockId()).getId());
            }
        });

        InnerResponse<ObjectUtils.Null> innerResponse = remoteStockService.saveAll(dtos, null);

        log.info("【GoodInfo爬蟲結束】共抓取{}筆資料，入庫回應: {}", dtos.size(), JSON.toJSON(innerResponse));
    }

    public static void main(String[] args) {
        List<Integer> integers = List.of(1, 2, 3, 4, 5);
        System.out.println(integers.subList(3, 0));
    }
}
