package com.personal.project.gateway.msgqueue;

import com.personal.project.gateway.model.dto.ConnectionInfoDTO;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConnectionProducer {

	private final RabbitTemplate rabbitTemplate;

	public void send(ConnectionInfoDTO info) {
		rabbitTemplate.convertAndSend("Connection Info Queue", info);
	}
}
