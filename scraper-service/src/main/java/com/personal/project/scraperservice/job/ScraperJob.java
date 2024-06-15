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
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    public ScraperJob(RemoteStockService remoteStockService, RemoteReportService remoteReportService, ErrorMessageService errorMessageService) {
        this.remoteStockService = remoteStockService;
        this.errorMessageService = errorMessageService;
        this.remoteReportService = remoteReportService;
    }

    @XxlJob("twse-tpex-system-init-scrape")
    public void twseAndTPEXInitHandle() {
        log.info("資料初始化任務 twse-tpex-system-init-scrape 開始執行");
        TimeInterval timer = DateUtil.timer();
        LocalDate now = LocalDate.now();

        //獲取股票代號清單
        Map<String, Map<String, String>> stockListResult = scrapeGoodInfoInitStockList();
        log.info("資料初始化任務 twse-tpex-system-init-scrape 獲取股票代碼列表花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(stockListResult));

        List<DailyStockInfoDto> twseDTOs = null;
        List<DailyStockInfoDto> tpexDTOs = null;
        List<ScraperErrorMessageDO> allErrors = new ArrayList<>();

        //VT, 啟動
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

            //獲取上市股票最新-一年前
            Future<Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>>> listedFuture = executorService.submit(() -> {
                //篩選上市股票
                List<String> listedIds = stockListResult.get(LISTED).keySet().stream()
                        .filter(StrUtil::isNumeric)
                        .toList();

                //產出下載地址
                List<String> twseUrls = new ArrayList<>();
                LocalDate lastYear = now.minusMonths(14);
                LocalDate tempDate;
                for (String id : listedIds) {
                    tempDate = now;
                    while (lastYear.isBefore(tempDate)) {
                        twseUrls.add(StrUtil.format("https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY?date={}&stockNo={}&response=html", tempDate.format(DatePattern.PURE_DATE_FORMATTER), id));
                        tempDate = tempDate.minusMonths(1);
                    }
                }

                TWSEInitScraper twseInitScraper = new TWSEInitScraper(twseUrls);
                TWSEInitPipeline twseInitPipeline = new TWSEInitPipeline();

                return scrapeTWSEInitDailyStockInfo(twseInitScraper, twseInitPipeline);
            });

            //獲取上櫃股票最新-一年前
            Future<Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>>> otcFuture = executorService.submit(() -> {
                //篩選上櫃股票
                List<String> otcIds = stockListResult.get(OTC).keySet().stream()
                        .filter(StrUtil::isNumeric)
                        .toList();

                //產出下載地址
                LocalDate lastYear = now.minusMonths(14);
                LocalDate tempDate;
                List<String> tpexUrls = new ArrayList<>();
                for (String id : otcIds) {
                    tempDate = now;
                    while (lastYear.isBefore(tempDate)) {
                        tpexUrls.add(StrUtil.format("https://www.tpex.org.tw/web/stock/aftertrading/daily_trading_info/st43_print.php?l=zh-tw&d={}/{}&stkno={}&s=0,asc,0", tempDate.getYear() - 1911, tempDate.getMonth().getValue(), id));
                        tempDate = tempDate.minusMonths(1);
                    }
                }

                TPEXInitScraper tpexInitScraper = new TPEXInitScraper(tpexUrls);
                TPEXInitPipeline tpexInitPipeline = new TPEXInitPipeline();

                return scrapeTPEXInitDailyStockInfo(classpath + "driver/chrome-driver-mac-arm64/chromedriver", tpexInitScraper, tpexInitPipeline);

            });

            Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>> otcPair = otcFuture.get();
            Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>> listedPair = listedFuture.get();

            twseDTOs = otcPair.getLeft();
            tpexDTOs = listedPair.getLeft();
            allErrors.addAll(otcPair.getRight());
            allErrors.addAll(listedPair.getRight());

        } catch (Exception e) {
            log.error("資料初始化任務 twse-tpex-system-init-scrape 出現錯誤", e);
        }

        List<DailyStockInfoDto> resultCollect = new ArrayList<>();
        resultCollect.addAll(twseDTOs);
        resultCollect.addAll(tpexDTOs);
        log.info("資料初始化任務 twse-tpex-system-init-scrape 獲取股票資料花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(resultCollect));

        //init data 呼叫入庫
        InnerResponse<ObjectUtils.Null> response = remoteStockService.initSaveAll(resultCollect, null);
        log.info("資料初始化任務 twse-tpex-system-init-scrape 股票資料入庫花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(response));

        //錯誤入庫
        if (!allErrors.isEmpty()) {
            boolean saved = errorMessageService.saveBatch(allErrors);
            log.info("資料初始化任務 twse-tpex-system-init-scrape 錯誤入庫花費 {} 毫秒, 結果 {}", timer.intervalRestart(), saved);

        }

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
                                Duration.ofSeconds(10),
                                ExpectedConditions.visibilityOfElementLocated(By.id("tblStockList"))
                        )
                )
                .thread(1)
                .run();
        return listPipeline.getResult();
    }

    /**
     * 從
     *
     * @return
     */
    private Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>> scrapeTWSEInitDailyStockInfo(TWSEInitScraper twseInitScraper, TWSEInitPipeline twseInitPipeline) {

        Spider.create(twseInitScraper)
                .addUrl(twseInitScraper.getUrlsFirst())
                .addPipeline(twseInitPipeline)
                .thread(3)
                .run();

        Map<String, List<DailyStockInfoDto>> twseResult = twseInitPipeline.getResult();
        List<DailyStockInfoDto> finalResult = new ArrayList<>();
        //填補平日未開市資料
        twseResult.forEach((k, v) -> {
            v.sort(Comparator.comparingLong(DailyStockInfoDto::getDate).reversed());
            finalResult.addAll(v);
            DailyStockInfoDto tempDTO = null;
            for (int i = 0; i < v.size(); i++) {
                DailyStockInfoDto thisRoundDTO = v.get(i);
                //檢查本次與temp, 本次非temp前一天則檢查本次是否週五, 非週五則本次與temp中間非週六日期填上空物件
                if (tempDTO != null) {
                    LocalDate tempDTODate = LocalDate.parse(tempDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
                    LocalDate thisDate = LocalDate.parse(thisRoundDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
                    if (!thisDate.isEqual(tempDTODate.minusDays(1)) &&
                            thisDate.getDayOfWeek() != DayOfWeek.FRIDAY) {
                        LocalDate flagDate = tempDTODate.minusDays(1);
                        while (!flagDate.isEqual(thisDate)) {
                            if (flagDate.getDayOfWeek() != DayOfWeek.SATURDAY && flagDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                DailyStockInfoDto fillDTO = new DailyStockInfoDto();
                                fillDTO.setStockId(k);
                                fillDTO.setStockName(thisRoundDTO.getStockName());
                                fillDTO.setDate(Long.valueOf(flagDate.format(DatePattern.PURE_DATE_FORMATTER)));
                                finalResult.add(fillDTO);
                            }
                            flagDate = flagDate.minusDays(1);
                        }
                    }
                    //tempDTO填上缺失的昨日資訊
                    tempDTO.setYesterdayTradingVolumePiece(thisRoundDTO.getTodayTradingVolumePiece());
                    tempDTO.setTodayTradingVolumeMoney(thisRoundDTO.getTodayTradingVolumeMoney());
                }

                tempDTO = thisRoundDTO;
            }
        });

        return Pair.of(finalResult, twseInitPipeline.getErrors());
    }

    private Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>> scrapeTPEXInitDailyStockInfo(String driverPath, TPEXInitScraper tpexInitScraper, TPEXInitPipeline tpexInitPipeline) {
        Spider.create(tpexInitScraper)
                .addUrl(tpexInitScraper.getUrlsFirst())
                .addPipeline(tpexInitPipeline)
                .setDownloader(new SeleniumDownloader(driverPath, Duration.ofSeconds(5), ExpectedConditions.visibilityOfElementLocated(By.tagName("tbody"))))
                .thread(3)
                .run();

        Map<String, List<DailyStockInfoDto>> twseResult = tpexInitPipeline.getResult();
        List<DailyStockInfoDto> finalResult = new ArrayList<>();
        //填補平日未開市資料
        twseResult.forEach((k, v) -> {
            v.sort(Comparator.comparingLong(DailyStockInfoDto::getDate).reversed());
            finalResult.addAll(v);
            DailyStockInfoDto tempDTO = null;
            for (int i = 0; i < v.size(); i++) {
                DailyStockInfoDto thisRoundDTO = v.get(i);
                //檢查本次與temp, 本次非temp前一天則檢查本次是否週五, 非週五則本次與temp中間非週六日期填上空物件
                if (tempDTO != null) {
                    LocalDate tempDTODate = LocalDate.parse(tempDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
                    LocalDate thisDate = LocalDate.parse(thisRoundDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
                    if (!thisDate.isEqual(tempDTODate.minusDays(1)) &&
                            thisDate.getDayOfWeek() != DayOfWeek.FRIDAY) {
                        LocalDate flagDate = tempDTODate.minusDays(1);
                        while (!flagDate.isEqual(thisDate)) {
                            if (flagDate.getDayOfWeek() != DayOfWeek.SATURDAY && flagDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                DailyStockInfoDto fillDTO = new DailyStockInfoDto();
                                fillDTO.setStockId(k);
                                fillDTO.setStockName(thisRoundDTO.getStockName());
                                fillDTO.setDate(Long.valueOf(flagDate.format(DatePattern.PURE_DATE_FORMATTER)));
                                finalResult.add(fillDTO);
                            }
                            flagDate = flagDate.minusDays(1);
                        }
                    }
                    //tempDTO填上缺失的昨日資訊
                    tempDTO.setYesterdayClosingPrice(thisRoundDTO.getTodayClosingPrice());
                    tempDTO.setYesterdayTradingVolumePiece(thisRoundDTO.getTodayTradingVolumePiece());
                    tempDTO.setYesterdayTradingVolumeMoney(thisRoundDTO.getTodayTradingVolumeMoney());
                    tempDTO.setPriceGap(tempDTO.getTodayClosingPrice().subtract(thisRoundDTO.getTodayClosingPrice()));
                    tempDTO.setPriceGapPercent(tempDTO.getPriceGap().divide(tempDTO.getYesterdayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));
                }

                tempDTO = thisRoundDTO;
            }
        });

        return Pair.of(finalResult, tpexInitPipeline.getErrors());
    }


    @XxlJob("goodinfo-scrape")
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
                .setDownloader(new SeleniumDownloader(classpath + "driver/chrome-driver-mac-arm64/chromedriver", Duration.ofSeconds(5), ExpectedConditions.visibilityOfElementLocated(By.id("tblStockList"))))
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
}
