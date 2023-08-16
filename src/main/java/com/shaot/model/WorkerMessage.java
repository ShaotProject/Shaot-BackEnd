package com.shaot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class WorkerMessage {
	private String text;
	private Long senderId;
	@Setter
	private boolean answer;
	@Setter
	private String reason;
	
	public WorkerMessage(String text, Long senderId) {
		this.text = text;
		this.senderId = senderId;
	}
}
