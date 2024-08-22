package com.personal.project.chatservice.service.argsprocesser;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class QRTArgsProcessor implements ArgsProcessor {

	private final String TRADING_VOL = "交易量";
	private final String PRICE_GAP_PERCENT = "漲跌幅";
	private final String TAGS = "標籤";

	@Override
	public Optional<RemoteRequestDTO> process(List<String> args) {

		QRTRemoteRequestDTO dto = new QRTRemoteRequestDTO();
		try {
			args.forEach(arg -> {
				String[] kv = arg.split(":");
				String key = kv[0].trim();
				String value = kv[1].trim();

				if (key.equals(TRADING_VOL)) {
					String val = value.replaceAll("[^0-9]", "");
					dto.setTradingVolumePieceStart(Long.parseLong(val));
				}

				if (key.equals(PRICE_GAP_PERCENT)) {
					String val = value.replaceAll("[^0-9.]", "");
					dto.setPriceGapPercent(new BigDecimal(val));
				}

				if (key.equals(TAGS)) {
					List<String> tags = Arrays.stream(value.split(","))
							.map(String::trim)
							.toList();
					dto.setExtraTags(tags);
				}
			});
		} catch (Exception e) {
			return Optional.empty();
		}

		return Optional.of(dto);
	}
}
