package com.shaot.dto.worker;

import java.util.List;

import com.shaot.dto.company.CompanyForWorkerDto;
import com.shaot.model.ShaotLoginUser;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class WorkerView extends ShaotLoginUser{
	private double wage;
	private CompanyForWorkerDto company;
	private List<WorkerScheduleDto> shifts;
}
