package com.personal.project.authservice.exception;

public class WrongPasswordException  extends CustomException{
	private String message = "Wrong password";

	private String extraData = "";

	public WrongPasswordException() {
	}

	public WrongPasswordException(String msg) {
		super(msg);
		this.message = msg;
	}

	public WrongPasswordException(String msg, String extraData) {
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
