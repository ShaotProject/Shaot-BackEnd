package com.shaot.dto.worker;

import java.util.List;

import com.shaot.dto.company.CompanyForWorkerDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class WorkerView {
	private long id;
	private String name;
	private double wage;
	private CompanyForWorkerDto company;
	private List<WorkerScheduleDto> shifts;
}
