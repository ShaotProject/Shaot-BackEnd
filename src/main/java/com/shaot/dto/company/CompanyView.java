package com.shaot.dto.company;

import java.util.List;
import java.util.Map;

import com.shaot.dto.worker.WorkerForCompanyDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyView {
	String id;
	String name;
	Map<Long, WorkerForCompanyDto> workers;
	List<CompanyShiftDto> shifts;
}
