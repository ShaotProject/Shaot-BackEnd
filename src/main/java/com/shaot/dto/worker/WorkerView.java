package com.shaot.dto.worker;

import java.util.Set;

import com.shaot.dto.company.CompanyForWorkerDto;
import com.shaot.model.ShaotLoginUser;
import com.shaot.schedule.generator.DayView;
import com.shaot.schedule.generator.ShiftView;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class WorkerView extends ShaotLoginUser{
	private double wage;
	private CompanyForWorkerDto company;
	private Set<ShiftView> shifts;
}
