package com.shaot.dto.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class WorkerForCompanyView {
	private long id;
	private String name;
	private double wage;
}
