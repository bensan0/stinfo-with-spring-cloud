package com.personal.project.chatservice.service;

import com.personal.project.chatservice.model.TGSendMsgDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface TGClient {

	@PostExchange(value = "/sendMessage", contentType = MediaType.APPLICATION_JSON_VALUE)
	void sendMsg(@RequestBody TGSendMsgDTO msg);

	@GetExchange(value = "/sendMessage")
	void sendMsg(@RequestParam String chat_id, @RequestParam String text);
}
