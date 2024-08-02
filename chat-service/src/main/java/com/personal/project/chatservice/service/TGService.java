package com.personal.project.chatservice.service;

import com.personal.project.chatservice.model.TGReceiveMsgDTO;

public interface TGService {

	void processCommand(TGReceiveMsgDTO dto);
}
