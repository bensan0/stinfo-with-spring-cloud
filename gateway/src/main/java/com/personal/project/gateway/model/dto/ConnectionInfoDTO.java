package com.personal.project.gateway.model.dto;

import lombok.Data;

@Data
public class ConnectionInfoDTO {

	public ConnectionInfoDTO(String ip, String path, String date) {
		this.ip = ip;
		this.path = path;
		this.date = date;
	}

	private String ip;

	private String path;

	private String date;
}
