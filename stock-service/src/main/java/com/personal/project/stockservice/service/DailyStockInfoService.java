package com.personal.project.stockservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.personal.project.stockservice.model.dto.request.Query4CalDTO;
import com.personal.project.stockservice.model.dto.request.QueryConditionDTO;
import com.personal.project.stockservice.model.dto.request.QueryConditionRealTimeDTO;
import com.personal.project.stockservice.model.dto.response.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDO;

import java.util.List;
import java.util.Map;

public interface DailyStockInfoService extends IService<DailyStockInfoDO> {


    boolean initSaveAll(List<DailyStockInfoDTO> data);

    boolean saveAll(List<DailyStockInfoDTO> data);

    /**
     * 獲取前一個交易日資料
     * @return
     */
    Map<String, DailyStockInfoDTO> queryFormer(Long date);

    /**
     * 初始化任務用, 獲取計算上一個交易日指標所需資料(上個交易日～今天算起之前第241個交易日)
     * @return
     */
    Map<String, List<StockInfo4InitMetricsDTO>> query4InitYesterdayMetrics();

    /**
     * 初始化任務用, 獲取計算上一個交易日Detail所需資料
     * @return
     */
    Map<String, List<StockInfo4InitDetailDTO>> query4InitYesterdayDetail();

    /**
     * 初始化任務用, 獲取計算最新交易日metrics所需資料
     * @return
     */
    Map<String, List<StockInfo4InitMetricsDTO>> query4InitTodayMetrics();

    /**
     * 初始化任務用, 獲取計算最新交易日detail所需資料
     * @return
     */
    Map<String, List<DailyStockInfoDTO>> query4InitTodayDetail();

    /**
     * 取得今日 ＋ 今日算起第5, 10, 20, 60, 120, 240 個交易日的收盤價資料
     * @return
     */
    Map<String, List<DailyStockInfoDTO>> query4CalMetrics(Query4CalDTO query4CalDTO);

    /**
     * 取得今日, 上個交易日, 3個交易日前(含今日), 5個交易日前(含今日)個股資料
     * @return
     */
    Map<String, List<DailyStockInfoDTO>> queryInfo4CalDetail(Query4CalDTO query4CalDto);

    Map<String, DailyStockInfoDTO> queryByDate(long date);

    List<DailyStockInfoDTO> queryByDateAndId(Long date, String stockId);

    Map<String, List<DailyStockInfoDTO>> query4CalRealTimeMetrics(Long date);

    Map<String, List<DailyStockInfoDTO>> queryInfo4CalRealTimeDetail(Long date);

    PageInfo<DailyStockInfoDTO> queryByStockId(String stockId, int current, int size);

    PageInfo<CompleteStockDTO> conditionQuery(int current, int size, QueryConditionDTO dto);

    List<RealTimeStockDTO> conditionRealTimeQuery(Long date, QueryConditionRealTimeDTO dto);

    List<FRealTimeStockDTO> conditionFRealTimeQuery(Long date, QueryConditionRealTimeDTO dto);

	long checkInit();
}
