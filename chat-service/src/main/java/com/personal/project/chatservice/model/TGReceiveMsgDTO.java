package com.personal.project.chatservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TGReceiveMsgDTO {

	@JsonProperty("update_id")
	private Long updateId;

	@JsonProperty("message")
	private Message message;

	@JsonProperty("edited_message")
	private Message editedMessage;

	@Data
	public static class Message {

		@JsonProperty("message_id")
		private Long messageId;

		private From from;

		private Chat chat;

		private Long date;

		@JsonProperty("edit_date")
		private Long editDate;

		private String text;

		private List<Entity> entities;

		@Data
		public static class From {

			private Long id;

			@JsonProperty("is_bot")
			private Boolean isBot;

			@JsonProperty("first_name")
			private String firstName;

			private String username;

			@JsonProperty("language_code")
			private String languageCode;
		}

		@Data
		public static class Chat {
			private Long id;

			@JsonProperty("first_name")
			private String firstName;

			private String username;

			private String type;
		}

		@Data
		public static class Entity {

			private Long offset;

			private Long length;

			private String type;
		}
	}

	public void sync() {
		if (this.message == null) {
			this.message = this.editedMessage;
		}
	}
}
