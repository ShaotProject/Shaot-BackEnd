package com.shaot.schedule.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.company.CompanyWeekGeneratorDto;
import com.shaot.dto.company.CompanyWorkingDayDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerShiftView;
import com.shaot.model.Worker;

import lombok.Getter;

@Getter
public class ScheduleGeneratorImpl implements ScheduleGenerator {
	private Map<Long, GeneratorWorker> workerPrefers; // check if needed ???
	private Map<String, List<ShiftView>> schedule;
	private Map<String, List<GeneratorShift>> workingWeek;
	Map<String, List<GeneratorShift>> workingWeekCopy;

	public ScheduleGeneratorImpl() {
		workerPrefers = new ConcurrentHashMap<>();
		schedule = new ConcurrentHashMap<>();
		workingWeek = new ConcurrentHashMap<>();
		workingWeekCopy = new ConcurrentHashMap<>();
	}

	@Override
	public Map<String, List<GeneratorShift>> generateWeek(CompanyWeekGeneratorDto weekGenerator) {
		List<String> dayNames = weekGenerator.getDayNames();
		List<String> shiftNames = weekGenerator.getShiftNames();
		dayNames.forEach(dayName -> {
			List<GeneratorShift> workingDay = new LinkedList<>();
			List<ShiftView> shiftViews = new ArrayList<>();
			shiftNames.forEach(shiftName -> {
				GeneratorShift workingShift = new GeneratorShift(shiftName, dayName);
				workingShift.setWorkerNeeded(1);
				workingDay.add(workingShift);
				shiftViews.add(new ShiftView(shiftName));
			});
			workingWeek.put(dayName, workingDay);
			schedule.put(dayName, shiftViews);
		});
		return workingWeek;
	}

	@Override
	public void addPrefer(Long id, String workerName, WorkerPreferShiftsDto shift) {
		GeneratorWorker generatorWorker = workerPrefers.get(id);
		List<String> shifts = shift.getShifts();
		if (generatorWorker == null) {
			generatorWorker = new GeneratorWorker();
		}
		generatorWorker.addPrefers(shift.getDayName(), shifts);
		generatorWorker.setName(workerName);
		generatorWorker.setId(id);
		workerPrefers.put(id, generatorWorker);
		addPreferToWorkingWeekAvailable(generatorWorker, shift.getDayName(), shifts);
	}

	private void addPreferToWorkingWeekAvailable(GeneratorWorker generatorWorker, String dayName, List<String> shifts) {
		List<GeneratorShift> workingDay = workingWeek.get(dayName);
		workingDay.forEach(wd -> {
			shifts.forEach(shiftName -> {
				if (wd.getShiftName().equals(shiftName)) {
					wd.addAvailable(generatorWorker);
				}
			});
		});
	}

	@Override
	public List<WorkerPreferShiftsDto> getWorkerPrefers(Long id) {
		List<WorkerPreferShiftsDto> list = new ArrayList<>();
		GeneratorWorker generatorWorkerShift = workerPrefers.get(id);

		generatorWorkerShift.getWeekPrefers().entrySet().forEach(entry -> {
			WorkerPreferShiftsDto workerPreferShiftsDto = new WorkerPreferShiftsDto(entry.getKey(), entry.getValue());
			list.add(workerPreferShiftsDto);
		});

		return list;
	}

	@Override
	public Map<String, List<ShiftView>> generateSchedule() {
		if(workingWeekCopy.isEmpty() && !workingWeek.isEmpty()) {
			workingWeekCopy.putAll(workingWeek);
		}
		if(workingWeek.isEmpty() && !workingWeekCopy.isEmpty()) {
			workingWeek.putAll(workingWeekCopy);
		}
		List<GeneratorShift> allShifts = concatAllShifts();
		Collections.sort(allShifts);
		allShifts.forEach(shift -> {
			while(shift.getWorkerNeeded() > 0) {
				saveWorker(shift);
				shift.setWorkerNeeded(shift.getWorkerNeeded() - 1);
			}
		});
		workingWeek.clear();
		return schedule;
	}
	
	private void saveWorker(GeneratorShift shift) {
		GeneratorWorker worker = shift.getCandidate();
		saveWorkerPriority(worker);
		saveWorkerToSchedule(worker, shift.getShiftName(), shift.getDayName());
	}

	private void saveWorkerPriority(GeneratorWorker worker) {
		if (worker != null) {
			workingWeek.entrySet().forEach(entry -> {
				List<GeneratorShift> freshShifts = new ArrayList<>();
				String dayName = entry.getKey();
				entry.getValue().forEach(sh -> {
					List<GeneratorWorker> freshAvailable = new ArrayList<>();
					sh.getAvailable().forEach(w -> {
						if(w.equals(worker)) {
							w.addToSchedule();
						}
						freshAvailable.add(w);
					});
					freshShifts.add(sh);
				});
				workingWeek.put(dayName, freshShifts);
			});
			return;
		}
	}
	
	private void saveWorkerToSchedule(GeneratorWorker worker, String shiftName, String dayName) {
		if (worker != null) {
			schedule.entrySet().forEach(day -> {
				if (day.getKey().equals(dayName)) {
					List<ShiftView> shifts = day.getValue();
					List<String> workersToday = getAllWorkersForDay(day.getKey());
					shifts.forEach(sh -> {
						if (sh.getShiftName().equals(shiftName) && !workersToday.contains(worker.getName())) {
							sh.addWorkerName(worker.getName());
						}
					});
				}
			});
			return;
		}		
	}

	private List<String> getAllWorkersForDay(String key) {
		List<String> workersNames = new ArrayList<>();
		List<ShiftView> todayShifts = schedule.get(key);
		todayShifts.forEach(shift -> shift.getWorkerNames().forEach(name -> workersNames.add(name)));
		return workersNames;
	}

	private List<GeneratorShift> concatAllShifts() {
		List<GeneratorShift> allShifts = new ArrayList<>();
		workingWeek.entrySet().forEach(entry -> {
			List<GeneratorShift> list = entry.getValue();
			list.forEach(gs -> {
				allShifts.add(gs);
			});
		});
		return allShifts;
	}

	@Override
	public void addShift(CompanyAddShiftDto companyAddShiftDto) {
		// TODO
	}

	@Override
	public void addWorkingDay(CompanyAddWorkingDay companyAddWorkingDayDto) {
		CompanyWorkingDayDto workingDay = new CompanyWorkingDayDto(companyAddWorkingDayDto.getDayName(),
				companyAddWorkingDayDto.getWorkersQuantityPerShift());
		companyAddWorkingDayDto.getShifts()
				.forEach(s -> workingDay.addShift(new CompanyShiftDto(companyAddWorkingDayDto.getDayName(), s,
						new ArrayList<>(companyAddWorkingDayDto.getWorkersQuantityPerShift()))));
	}

	@Override
	public void removeShift(CompanyRemoveShiftDto companyRemoveShiftDto) {
		// TODO
	}

	@Override
	public void removeWorkingDay(CompanyRemoveWorkingDayDto companyRemoveWorkingDayDto) {
		// TODO
		// workingWeek.stream().filter(day ->
		// !day.getDayName().equals(companyRemoveWorkingDayDto.getDayName()));
	}

	@Override
	public Map<String, List<GeneratorShift>> configureWeek(ScheduleConfigurationDto configuration) {
		workingWeek.entrySet().forEach(entry -> {
			List<GeneratorShift> day = entry.getValue();
			day.forEach(shift -> shift.setWorkerNeeded(configuration.getWorkersNumberPerShift()));
			workingWeek.replace(entry.getKey(), day);
		});
		return workingWeek;
	}

	@Override
	public List<WorkerShiftView> getWorkerSchedule(Worker worker) {
		List<WorkerShiftView> workerSchedule = new ArrayList<>();
		schedule.entrySet().stream().forEach(entry -> {
			List<ShiftView> day = entry.getValue();
			day.stream().forEach(sv -> {
				if(sv.getWorkerNames().contains(worker.getName())) {
					workerSchedule.add(new WorkerShiftView(entry.getKey(), sv.getShiftName()));
				}
			});	
		});
		return workerSchedule;
	}
}
