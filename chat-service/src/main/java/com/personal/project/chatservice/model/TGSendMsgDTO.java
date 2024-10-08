package com.personal.project.chatservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TGSendMsgDTO {

	@JsonProperty("chat_id")
	private String chatId;

	private String text;
}
