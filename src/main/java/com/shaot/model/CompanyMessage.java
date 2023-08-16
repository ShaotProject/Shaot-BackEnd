package com.shaot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class CompanyMessage {
	private Long workerId;
	private String reason;
	private boolean answer;
}
