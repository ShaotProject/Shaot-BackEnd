package com.shaot.dto.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkerDto {
	private long id;
	private String name;
	private String mail;
	private String password;
}