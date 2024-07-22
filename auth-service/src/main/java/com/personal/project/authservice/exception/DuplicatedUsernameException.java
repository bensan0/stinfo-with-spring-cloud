package com.personal.project.authservice.exception;

public class DuplicatedUsernameException extends CustomException {

	private String message = "Username duplicated";

	private String extraData = "";

	public DuplicatedUsernameException() {
	}

	public DuplicatedUsernameException(String msg) {
		super(msg);
		this.message = msg;
	}

	public DuplicatedUsernameException(String msg, String extraData) {
		super(msg);
		this.message = msg;
		this.extraData = extraData;
	}

	@Override
	public String getDefaultMessage() {
		return message;
	}

	@Override
	public String getExtraData() {
		return extraData;
	}
}
