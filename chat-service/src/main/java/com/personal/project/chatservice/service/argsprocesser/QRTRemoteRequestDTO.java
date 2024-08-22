package com.personal.project.chatservice.service.argsprocesser;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QRTRemoteRequestDTO implements RemoteRequestDTO {

	private Long tradingVolumePieceStart;

	private BigDecimal priceGapPercent;

	private List<String> extraTags;
}
