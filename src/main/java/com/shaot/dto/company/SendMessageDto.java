package com.shaot.dto.company;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SendMessageDto {
	private long messageId; //temporarly
	private LocalDateTime shiftNeed;
}
