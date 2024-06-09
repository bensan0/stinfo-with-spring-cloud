package com.personal.project.stockservice.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;
import com.personal.project.stockservice.service.DailyStockInfoDetailService;
import com.personal.project.stockservice.service.DailyStockInfoService;
import com.personal.project.stockservice.service.DailyStockMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 新增爬蟲服務當日數據
     *
     * @param data
     * @param token
     * @return
     */
    @PostMapping("/save-all")
    public InnerResponse<Object> saveAll(
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
            List<CalMetricsDTO> dtos = dailyStockMetricsService.query4CalMetrics(query4CalDTO);
            List<PastClosingPriceDTO> pastClosingPriceDTOs = dailyStockInfoService.queryPastClosingPrice4CalMetrics(query4CalDTO);
            List<SimpleMetricsDTO> simpleMetricsDTOs = dailyStockMetricsService.queryMetrics(query4CalDTO.date());

            return InnerResponse.ok(new CalMetricsUnionDTO(dtos, pastClosingPriceDTOs, simpleMetricsDTOs));
        } catch (Exception e) {
            String tc = IdUtil.randomUUID();
            log.error("Stock service feign query4CalMetrics failed, trace code: {}", tc, e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign query4CalMetrics failed trace code:" + tc);
        }
    }

    /**
     * 手動報表服務獲取產出報告所需資料
     *
     * @return
     */
    @PostMapping("/manual/query-4-cal-metrics")
    public InnerResponse<CalMetricsUnionDTO> query4CalMetrics(
            @RequestBody ManualCalDTO manualCalDTO
            //todo 未來優化成輸入List<Long> dates ＝ 可以重算多天的報告
    ) {
        try {
            List<CalMetricsDTO> dtos = dailyStockMetricsService.query4CalMetrics(manualCalDTO);
            List<PastClosingPriceDTO> pastClosingPriceDTOs = dailyStockInfoService.queryPastClosingPrice4CalMetrics(manualCalDTO);
            List<SimpleMetricsDTO> simpleMetricsDTOs = dailyStockMetricsService.queryMetrics(manualCalDTO.getDate());

            return InnerResponse.ok(new CalMetricsUnionDTO(dtos, pastClosingPriceDTOs, simpleMetricsDTOs));
        } catch (Exception e) {
            String tc = IdUtil.randomUUID();
            log.error("Stock service feign manual query4CalMetrics failed, trace code: {}", tc, e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign manual query4CalMetrics failed trace code:" + tc);
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
            List<StockInfo4CalDetailDTO> calDetailInfoDTOs = dailyStockInfoService.queryInfo4CalDetail(query4CalDto);
            List<DailyStockMetricsDTO> dailyStockMetricsDTOs = dailyStockMetricsService.query4CalDetail(query4CalDto);
            List<DailyStockInfoDetailDTO> dailyStockInfoDetailDTOs = dailyStockInfoDetailService.query4CalDetail(query4CalDto);
            List<SimpleDetailDTO> todayExistedDetailDTOs = dailyStockInfoDetailService.queryDetail(query4CalDto.date());

            CalDetailUnionDTO dto = new CalDetailUnionDTO(calDetailInfoDTOs, dailyStockMetricsDTOs, dailyStockInfoDetailDTOs, todayExistedDetailDTOs);

            return InnerResponse.ok(dto);
        } catch (Exception e) {
            String tc = IdUtil.randomUUID();
            log.error("Stock service feign query4CalDetail failed, trace code: {}", tc, e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign query4CalDetail failed trace code:" + tc);
        }
    }

    @PostMapping("/manual/query-4-cal-detail")
    public InnerResponse<CalDetailUnionDTO> query4CalDetail(
            @RequestBody ManualCalDTO manualCalDTO
    ) {
        try {
            List<StockInfo4CalDetailDTO> calDetailInfoDTOs = dailyStockInfoService.queryInfo4CalDetail(manualCalDTO);
            List<DailyStockMetricsDTO> dailyStockMetricsDTOs = dailyStockMetricsService.query4CalDetail(manualCalDTO);
            List<DailyStockInfoDetailDTO> dailyStockInfoDetailDTOs = dailyStockInfoDetailService.query4CalDetail(manualCalDTO);
            List<SimpleDetailDTO> todayExistedDetailDTOs = dailyStockInfoDetailService.queryDetail(manualCalDTO.getDate());

            CalDetailUnionDTO dto = new CalDetailUnionDTO(calDetailInfoDTOs, dailyStockMetricsDTOs, dailyStockInfoDetailDTOs, todayExistedDetailDTOs);

            return InnerResponse.ok(dto);
        } catch (Exception e) {
            String tc = IdUtil.randomUUID();
            log.error("Stock service feign manual query4CalDetail failed, trace code: {}", tc, e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign manual query4CalDetail failed trace code:" + tc);
        }
    }

    @PostMapping("/save-detail")
    public InnerResponse<ObjectUtils.Null> saveDetail(
            @RequestBody List<DailyStockInfoDetailDTO> detailDTOs
    ) {
        try{
            boolean saved = dailyStockInfoDetailService.saveOrUpdateBatch(
                    detailDTOs.stream()
                            .map(o -> BeanUtil.copyProperties(o, DailyStockInfoDetailDO.class))
                            .toList()
            );
            if (saved) {
                return InnerResponse.ok(null);
            }

            return InnerResponse.failed(ResponseCode.Failed.getCode(), "detail not saved");
        }catch (Exception e){
            log.error("Stock service feign saveDetail failed", e);

            return InnerResponse.failed(ResponseCode.Failed.getCode(), e.getClass().getSimpleName());
        }
    }
}
