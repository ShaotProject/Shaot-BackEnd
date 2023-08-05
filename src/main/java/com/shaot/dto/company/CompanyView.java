package com.shaot.dto.company;

import java.util.List;

import com.shaot.dto.worker.WorkerForCompanyDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyView {
	String id;
	String name;
	List<WorkerForCompanyDto> workers;
	List<CompanyShiftDto> shifts;
}
