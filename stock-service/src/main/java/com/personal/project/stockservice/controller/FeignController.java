package com.personal.project.stockservice.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.IdUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.stockservice.model.dto.request.DailyIndexInfoDTO;
import com.personal.project.stockservice.model.dto.request.Query4CalDTO;
import com.personal.project.stockservice.model.dto.request.QueryConditionRealTimeDTO;
import com.personal.project.stockservice.model.dto.response.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;
import com.personal.project.stockservice.service.DailyIndexInfoService;
import com.personal.project.stockservice.service.DailyStockInfoDetailService;
import com.personal.project.stockservice.service.DailyStockInfoService;
import com.personal.project.stockservice.service.DailyStockMetricsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feign/stock")
@Slf4j
@AllArgsConstructor
public class FeignController {

	private final DailyStockInfoService dailyStockInfoService;

	private final DailyStockMetricsService dailyStockMetricsService;

	private final DailyStockInfoDetailService dailyStockInfoDetailService;

	private final DailyIndexInfoService dailyIndexInfoService;

	@GetMapping("/get-by-date")
	public InnerResponse<Map<String, DailyStockInfoDTO>> getByDate(@RequestParam Long date) {

		Map<String, DailyStockInfoDTO> result = dailyStockInfoService.queryByDate(date);

		return InnerResponse.ok(result);
	}

	@GetMapping("/index/get-by-date")
	public InnerResponse<Map<String, DailyIndexInfoDTO>> getIndexByDate(@RequestParam Long date) {

		Map<String, DailyIndexInfoDTO> result = dailyIndexInfoService.queryByDate(date);

		return InnerResponse.ok(result);
	}

	/**
	 * 初始化系統檢查用, 查看資料庫中是否已經有資料
	 */
	@GetMapping("/init/check")
	public InnerResponse<Boolean> checkInit() {
		long count = dailyStockInfoService.checkInit();

		return InnerResponse.ok(count > 0);
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
	public InnerResponse<Map<String, DailyStockInfoDTO>> getFormer(@RequestParam Long date) {
		Map<String, DailyStockInfoDTO> idToDTO = dailyStockInfoService.queryFormer(date);

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
	 */
	@PostMapping("/save-all")
	public InnerResponse<ObjectUtils.Null> saveAll(
			@RequestBody List<DailyStockInfoDTO> data) {
		boolean saved = dailyStockInfoService.saveAll(data);

		if (saved) {
			return InnerResponse.ok(null);
		} else {
			return InnerResponse.failed("Stock service feign save-all failed");
		}
	}

	@PostMapping("/index/save-all")
	public InnerResponse<ObjectUtils.Null> indexSaveAll(
			@RequestBody List<DailyIndexInfoDTO> data) {

		boolean saved = dailyIndexInfoService.saveAll(data);

		if (saved) {
			return InnerResponse.ok(null);
		} else {
			return InnerResponse.failed("Stock service feign index save-all failed, index: " + data);
		}
	}

	/**
	 * 報表服務獲取產出報告所需資料
	 */
	@PostMapping("/query-4-cal-metrics")
	public InnerResponse<CalMetricsUnionDTO> query4CalMetrics(
			@RequestBody Query4CalDTO query4CalMetricsDTO
	) {
		try {
			Map<String, List<DailyStockInfoDTO>> stockIdToInfos = dailyStockInfoService.query4CalMetrics(query4CalMetricsDTO);
			Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = dailyStockMetricsService.query4CalMetrics(query4CalMetricsDTO.date());


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
			@RequestBody Query4CalDTO query4CalDetailDTO
	) {
		try {
			Map<String, List<DailyStockInfoDTO>> stockIdToInfos = dailyStockInfoService.queryInfo4CalDetail(query4CalDetailDTO);
			Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = dailyStockMetricsService.query4CalDetail(query4CalDetailDTO.date());
			Map<String, List<DailyStockInfoDetailDTO>> stockIdToDetails = dailyStockInfoDetailService.query4CalDetail(query4CalDetailDTO.date());

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

	@GetMapping("/query-info-by-cond")
	public InnerResponse<List<DailyStockInfoDTO>> queryInfoByCond(
			@RequestParam Long date,
			@RequestParam String stockId
	) {
		List<DailyStockInfoDTO> result = dailyStockInfoService.queryByDateAndId(date, stockId);

		return InnerResponse.ok(result);
	}

	@GetMapping("/query-4-cal-real-time-metrics")
	public InnerResponse<CalMetricsUnionDTO> query4RealTimeMetrics(@RequestParam Long date) {
		try {
			Map<String, List<DailyStockInfoDTO>> stockIdToInfos = dailyStockInfoService.query4CalRealTimeMetrics(date);
			Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = dailyStockMetricsService.query4CalMetrics(date);


			return InnerResponse.ok(new CalMetricsUnionDTO(stockIdToMetrics, stockIdToInfos));
		} catch (Exception e) {
			log.error("Stock service feign query4CalMetrics failed", e);

			return InnerResponse.failed(ResponseCode.Failed.getCode(), "Stock service feign query4CalMetrics failed");
		}
	}

	@GetMapping("/query-4-cal-real-time-detail")
	InnerResponse<CalDetailUnionDTO> queryRealTimeDetail(@RequestParam Long date) {
		Map<String, List<DailyStockInfoDTO>> stockIdToInfos = dailyStockInfoService.queryInfo4CalRealTimeDetail(date);
		Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = dailyStockMetricsService.query4CalDetail(date);
		Map<String, List<DailyStockInfoDetailDTO>> stockIdToDetails = dailyStockInfoDetailService.query4CalDetail(date);

		CalDetailUnionDTO dto = new CalDetailUnionDTO(stockIdToInfos, stockIdToMetrics, stockIdToDetails);

		return InnerResponse.ok(dto);
	}

	@PostMapping("/condition/real-time/list")
	InnerResponse<List<FRealTimeStockDTO>> conditionFRealTimeQuery(@RequestBody QueryConditionRealTimeDTO dto) {
		LocalDateTime now = LocalDateTime.now();
		String nowStr = now.format(DatePattern.PURE_DATE_FORMATTER);
		if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return InnerResponse.failed(ResponseCode.Not_Valid.getCode(), "Today is not trading day");
		}

//		if (now.toLocalTime().isAfter(LocalTime.of(14, 0))) {
//			return InnerResponse.failed(ResponseCode.Not_Valid.getCode(), "Now is off trading, use other api to check today's data");
//		}

		List<FRealTimeStockDTO> results = dailyStockInfoService.conditionFRealTimeQuery(Long.parseLong(nowStr), dto);

		return InnerResponse.ok(results);
	}
}
