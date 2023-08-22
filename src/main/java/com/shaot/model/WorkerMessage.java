package com.shaot.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class WorkerMessage {
	private String text;
	private LocalDateTime shiftName;
	private Long senderId;
	@Setter
	private boolean answer;
	@Setter
	private String reason;
	
	public WorkerMessage(String text, Long senderId, LocalDateTime shiftName) {
		this.text = text;
		this.senderId = senderId;
		this.shiftName = shiftName;
	}
}
