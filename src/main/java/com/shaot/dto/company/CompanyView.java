package com.shaot.dto.company;

import java.util.List;

import com.shaot.dto.worker.WorkerForCompanyView;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyView {
	String id;
	String name;
	double generalWage;
	List<WorkerForCompanyView> workers;
	List<CompanyShiftDto> shifts;
}
