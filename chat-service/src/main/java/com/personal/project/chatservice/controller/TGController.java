package com.personal.project.chatservice.controller;

import com.personal.project.chatservice.model.TGReceiveMsgDTO;
import com.personal.project.chatservice.service.TGService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg")
@AllArgsConstructor
public class TGController {

	private final TGService tgService;

	@PostMapping("/command")
	public void process(@RequestBody TGReceiveMsgDTO dto) {
		dto.sync();
		tgService.processCommand(dto);
	}
}
