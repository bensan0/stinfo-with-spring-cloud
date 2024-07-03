package com.personal.project.stockservice.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;
import com.personal.project.stockservice.service.DailyStockInfoDetailService;
import com.personal.project.stockservice.service.DailyStockInfoService;
import com.personal.project.stockservice.service.DailyStockMetricsService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feign/stock")
@Slf4j
public class FeignController {

    private final DailyStockInfoService dailyStockInfoService;

    private final DailyStockMetricsService dailyStockMetricsService;

    private final DailyStockInfoDetailService dailyStockInfoDetailService;

    public FeignController(DailyStockInfoService dailyStockInfoService,
                           DailyStockMetricsService dailyStockMetricsService,
                           DailyStockInfoDetailService dailyStockInfoDetailService) {
        this.dailyStockInfoService = dailyStockInfoService;
        this.dailyStockMetricsService = dailyStockMetricsService;
        this.dailyStockInfoDetailService = dailyStockInfoDetailService;
    }

    @GetMapping("/get-exists")
    public InnerResponse<List<String>> getExist(){
        List<String> ids = dailyStockInfoService.queryExist();
        return InnerResponse.ok(ids);
    }

    @PostMapping("/get-by-date")
    public InnerResponse<Map<String, DailyStockInfoDTO>> getByDate(@RequestBody String getByDateDTO) {
        JSONObject parsedObj = JSONUtil.parseObj(getByDateDTO);
        String date = parsedObj.get("date").toString();
        Map<String, DailyStockInfoDTO> stockIdToInfo = dailyStockInfoService.queryByDate(Long.parseLong(date));

        return InnerResponse.ok(stockIdToInfo);
    }

    /**
     * 初始化系統用, 儲存過去一年份info
     *
     * @return
     */
    @PostMapping("/init/save-all")
    public InnerResponse<ObjectUtils.Null> init(
            @RequestBody List<DailyStockInfoDTO> initData) {
        try {
            boolean saved = dailyStockInfoService.initSaveAll(initData);
            if (saved) {
                return InnerResponse.ok(null);
            } else {
                return InnerResponse.failed("Stock service feign init-save-all failed");
            }
        } catch (Exception e) {
            String tc = IdUtil.randomUUID();
            log.error("Stock service feign init-save-all failed, trace code: {}", tc, e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign init-save-all failed, trace code:" + tc);
        }
    }

    /**
     * 日常爬蟲用, 獲取上個交易日info
     *
     * @return
     */
    @GetMapping("/get-former")
    public InnerResponse<Map<String, DailyStockInfoDTO>> getFormer() {
        Map<String, DailyStockInfoDTO> idToDTO = dailyStockInfoService.queryFormer();

        return InnerResponse.ok(idToDTO);
    }

    /**
     * 初始化系統用, 獲取產出上個交易日指標報告的必要info
     *
     * @return
     */
    @GetMapping("/query-4-init-yesterday-metrics")
    public InnerResponse<Map<String, List<StockInfo4InitMetricsDTO>>> get4InitYesterdayMetrics() {
        Map<String, List<StockInfo4InitMetricsDTO>> idToDTOs = dailyStockInfoService.query4InitYesterdayMetrics();

        return InnerResponse.ok(idToDTOs);
    }

    /**
     * 初始化系統用, 獲取產出上個交易日詳細報告的必要info(上個交易日＋往前10個交易日份價格/總額/數量 資料)與指標(上個交易日)
     *
     * @return
     */
    @GetMapping("/query-4-init-yesterday-detail")
    public InnerResponse<Map<String, List<StockInfo4InitDetailDTO>>> get4InitYesterdayDetail() {
        Map<String, List<StockInfo4InitDetailDTO>> infoDTOs = dailyStockInfoService.query4InitYesterdayDetail();

        return InnerResponse.ok(infoDTOs);
    }

    /**
     * 初始化系統用,
     * 取得產出最新交易日指標報告的必要info(最新交易日以及最新交易日往前回推第5, 10, 20, 60, 120, 240個交易日)
     * metrics(以init時的資料, 為最新一份)
     */
    @GetMapping("/query-4-init-today-metrics")
    public InnerResponse<Query4CalInitTodayMetricsDTO> get4InitTodayMetrics() {
        Map<String, List<StockInfo4InitMetricsDTO>> stockIdToInfos = dailyStockInfoService.query4InitTodayMetrics();
        Map<String, DailyStockMetricsDTO> stockIdToMetrics = dailyStockMetricsService.query4InitTodayMetrics();

        return InnerResponse.ok(new Query4CalInitTodayMetricsDTO(stockIdToInfos, stockIdToMetrics));
    }

    /**
     * 初始化系統用
     * 取得產出最新交易日指標報告的必要info(最新交易日以及最新交易日往前回推第2, 3, 5個交易日)
     * 取得最新交易日, 前一個交易日指標報告
     * 取得前一個交易日詳細報告(以init環境, 為最新)
     *
     * @return
     */
    @GetMapping("/query-4-init-today-detail")
    public InnerResponse<Query4CalInitTodayDetailDTO> get4InitTodayDetail() {
        Map<String, List<DailyStockInfoDTO>> stockIdToInfos = dailyStockInfoService.query4InitTodayDetail();
        Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = dailyStockMetricsService.query4InitTodayDetail();
        Map<String, DailyStockInfoDetailDTO> stockInfoDetail = dailyStockInfoDetailService.query4InitTodayDetail();

        return InnerResponse.ok(new Query4CalInitTodayDetailDTO(stockIdToInfos, stockIdToMetrics, stockInfoDetail));
    }

    /**
     * 新增爬蟲服務當日數據
     *
     * @param data
     * @param token
     * @return
     */
    @PostMapping("/save-all")
    public InnerResponse<ObjectUtils.Null> saveAll(
            @RequestBody List<DailyStockInfoDTO> data,
            @RequestHeader String token) {
        try {
            boolean saved = dailyStockInfoService.saveAll(data);
            if (saved) {
                return InnerResponse.ok(null);
            } else {
                return InnerResponse.failed("Stock service feign save-all failed");
            }
        } catch (Exception e) {
            String tc = IdUtil.randomUUID();
            log.error("Stock service feign save-all failed, trace code: {}", tc, e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign save-all failed, trace code:" + tc);
        }

    }

    /**
     * 報表服務獲取產出報告所需資料
     *
     * @return
     */
    @PostMapping("/query-4-cal-metrics")
    public InnerResponse<CalMetricsUnionDTO> query4CalMetrics(
            @RequestBody Query4CalDTO query4CalDTO
    ) {
        try {
            Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = dailyStockMetricsService.query4CalMetrics(query4CalDTO);
            Map<String, List<DailyStockInfoDTO>> stockIdToInfos = dailyStockInfoService.query4CalMetrics(query4CalDTO);

            return InnerResponse.ok(new CalMetricsUnionDTO(stockIdToMetrics, stockIdToInfos));
        } catch (Exception e) {
            log.error("Stock service feign query4CalMetrics failed", e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign query4CalMetrics failed");
        }
    }

    @PostMapping("/save-metrics")
    public InnerResponse<ObjectUtils.Null> saveMetrics(
            @RequestBody List<DailyStockMetricsDTO> metricsDTOs
    ) {
        try {
            boolean saved = dailyStockMetricsService.saveOrUpdateBatch(
                    metricsDTOs.stream()
                            .map(dto -> BeanUtil.copyProperties(dto, DailyStockMetricsDO.class))
                            .toList()
            );
            if (saved) {
                return InnerResponse.ok(null);
            }

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "metrics not saved");
        } catch (Exception e) {
            log.error("Stock service feign saveMetrics failed", e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), e.getClass().getSimpleName());
        }
    }

    @PostMapping("/query-4-cal-detail")
    public InnerResponse<CalDetailUnionDTO> query4CalDetail(
            @RequestBody Query4CalDTO query4CalDto
    ) {
        try {
            Map<String, List<DailyStockInfoDTO>> stockIdToInfos = dailyStockInfoService.queryInfo4CalDetail(query4CalDto);
            Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = dailyStockMetricsService.query4CalDetail(query4CalDto);
            Map<String, List<DailyStockInfoDetailDTO>> stockIdToDetails = dailyStockInfoDetailService.query4CalDetail(query4CalDto);

            CalDetailUnionDTO dto = new CalDetailUnionDTO(stockIdToInfos, stockIdToMetrics, stockIdToDetails);

            return InnerResponse.ok(dto);
        } catch (Exception e) {
            String tc = IdUtil.randomUUID();
            log.error("Stock service feign query4CalDetail failed", e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign query4CalDetail failed");
        }
    }

    @PostMapping("/save-detail")
    public InnerResponse<ObjectUtils.Null> saveDetail(
            @RequestBody List<DailyStockInfoDetailDTO> details
    ) {
        try {
            boolean saved = dailyStockInfoDetailService.saveOrUpdateBatch(
                    details.stream()
                            .map(o -> BeanUtil.copyProperties(o, DailyStockInfoDetailDO.class))
                            .toList()
            );
            if (saved) {
                return InnerResponse.ok(null);
            }

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "detail not saved");
        } catch (Exception e) {
            log.error("Stock service feign saveDetail failed", e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), e.getClass().getSimpleName());
        }
    }
}
