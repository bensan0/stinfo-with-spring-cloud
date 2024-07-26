package com.personal.project.gateway.msgqueue;

import cn.hutool.core.bean.BeanUtil;
import com.personal.project.gateway.model.dto.ConnectionInfoDTO;
import com.personal.project.gateway.model.entity.ConnectionInfoDO;
import com.personal.project.gateway.service.ConnectionInfoService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConnectionConsumer {

	private final ConnectionInfoService connectionInfoService;

	@RabbitListener(queues = "Connection Info Queue")
	public void processMsg(@Payload ConnectionInfoDTO info) {

		connectionInfoService.save(BeanUtil.copyProperties(info, ConnectionInfoDO.class));
	}
}
