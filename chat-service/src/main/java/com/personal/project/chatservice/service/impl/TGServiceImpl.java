package com.personal.project.chatservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.personal.project.chatservice.model.FRealTimeStockDTO;
import com.personal.project.chatservice.model.TGReceiveMsgDTO;
import com.personal.project.chatservice.model.TGSendMsgDTO;
import com.personal.project.chatservice.remote.RemoteStockService;
import com.personal.project.chatservice.service.TGClient;
import com.personal.project.chatservice.service.TGService;
import com.personal.project.chatservice.service.argsprocesser.ArgsProcessor;
import com.personal.project.chatservice.service.argsprocesser.QRTArgsProcessor;
import com.personal.project.chatservice.service.argsprocesser.RemoteRequestDTO;
import com.personal.project.commoncore.response.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class TGServiceImpl implements TGService {

	private final TGClient tgClient;

	private final RemoteStockService remoteStockService;

	private static final String Command_Query_Real_Time = "/qrt";

	@Override
	public void processCommand(TGReceiveMsgDTO dto) {
		String msgStr = dto.getMessage().getText();
		String[] cmdAndArgs = msgStr.split(" +");
		TGSendMsgDTO tgSendMsgDTO = new TGSendMsgDTO();
		tgSendMsgDTO.setChatId(dto.getMessage().getChat().getId().toString());

		//解析命令
		switch (cmdAndArgs[0]) {
			case Command_Query_Real_Time -> {
				List<String> args = Arrays.stream(cmdAndArgs).toList().subList(1, cmdAndArgs.length);
				ArgsProcessor argsProcessor = new QRTArgsProcessor();
				Optional<RemoteRequestDTO> rrDTOOp = argsProcessor.process(args);

				if (rrDTOOp.isEmpty()) {
					tgSendMsgDTO.setText("參數格式不正確");
					tgClient.sendMsg(tgSendMsgDTO);
				}

				CommonResponse<List<FRealTimeStockDTO>> response = remoteStockService.conditionFRealTimeQuery(rrDTOOp.get());

				List<FRealTimeStockDTO> data = response.getData();
				data.forEach(d -> {
					if (d.getMarket().equals("櫃")) {
						d.setLink(StrUtil.format("https://tw.stock.yahoo.com/quote/{}.TWO/technical-analysis", d.getStockId()));
					} else {
						d.setLink(StrUtil.format("https://tw.stock.yahoo.com/quote/{}.TW/technical-analysis", d.getStockId()));
					}
				});

				List<List<FRealTimeStockDTO>> partition = ListUtil.partition(data, 4);
				for (List<FRealTimeStockDTO> subList : partition) {
					StringBuilder sb = new StringBuilder();
					for (FRealTimeStockDTO realtime : subList) {
						sb.append("代碼:").append(realtime.getStockId()).append(" | ")
								.append("名稱:").append(realtime.getStockName()).append(" | ")
								.append("價格:").append(realtime.getTodayClosingPrice()).append(" | ")
								.append("價差:").append(realtime.getPriceGap()).append(" |\n")
								.append("漲跌幅:").append(realtime.getPriceGapPercent()).append(" | ")
								.append("交易量:").append(realtime.getTodayTradingVolumePiece()).append(" |\n")
								.append("標籤:").append(JSONUtil.toBean(realtime.getTags(), FRealTimeStockDTO.ExtraTags.class).getExtraTags()).append(" |\n")
								.append("連接:").append(realtime.getLink()).append(" |\n");
					}

					tgSendMsgDTO.setText(sb.toString());
					tgClient.sendMsg(tgSendMsgDTO);

					try {
						Thread.sleep(Duration.of(5, ChronoUnit.SECONDS));
					} catch (InterruptedException ignored) {}
				}
			}

			default -> {
				tgSendMsgDTO.setChatId(dto.getEditedMessage().getChat().getId().toString());
				tgSendMsgDTO.setText("命令不正確");
			}
		}
	}
}
