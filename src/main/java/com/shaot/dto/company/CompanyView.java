package com.shaot.dto.company;

import java.util.List;

import com.shaot.dto.worker.WorkerForCompanyView;
import com.shaot.model.ShaotLoginUser;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyView extends ShaotLoginUser{
	double generalWage;
	List<WorkerForCompanyView> workers;
	List<CompanyShiftDto> shifts;
}
