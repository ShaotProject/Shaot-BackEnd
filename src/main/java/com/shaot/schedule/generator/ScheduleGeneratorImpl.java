package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.company.CompanyWorkingDayDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.company.ScheduleConfigurationShiftTime;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerShiftView;
import com.shaot.model.Worker;

import lombok.Getter;

@Getter
public class ScheduleGeneratorImpl implements ScheduleGenerator {
	private Map<Long, GeneratorWorker> workerPrefers; // check if needed ???
	private Map<LocalDate, List<ShiftView>> schedule;
	private Map<LocalDate, List<GeneratorShift>> workingWeek;
	private Map<LocalDate, List<GeneratorShift>> workingWeekCopy;
	private Long hoursPerShift;

	public ScheduleGeneratorImpl() {
		workerPrefers = new ConcurrentHashMap<>();
		schedule = new ConcurrentHashMap<>();
		workingWeek = new ConcurrentHashMap<>();
		workingWeekCopy = new ConcurrentHashMap<>();
	}

	@Override
	public Map<LocalDate, List<GeneratorShift>> generateWeek(ScheduleConfigurationDto weekGenerator) {
		LocalTime shiftStart = weekGenerator.getShiftsTime().get(0).getStart();
		LocalTime shiftEnd = weekGenerator.getShiftsTime().get(0).getEnd();
		hoursPerShift = ChronoUnit.HOURS.between(shiftStart, shiftEnd);
		LocalDate weekTemp = weekGenerator.getWeekStart();
		LocalDate weekEnd = weekGenerator.getWeekEnd();
		List<LocalDate> dayNames = new ArrayList<>();
		List<GeneratorShift> workingDay = new ArrayList<>();
		List<ShiftView> shiftViews = new ArrayList<>();
		while (weekTemp.isBefore(weekEnd)) {
			dayNames.add(weekTemp);
			List<ScheduleConfigurationShiftTime> shiftTimes = weekGenerator.getShiftsTime();
			workingDay = new ArrayList<>();
			shiftViews = new ArrayList<>();
			for (int i = 0; i < shiftTimes.size(); i++) {
				LocalTime shiftStarts = shiftTimes.get(i).getStart();
				LocalTime shiftEnds = shiftTimes.get(i).getEnd();
				LocalDateTime shiftStartsName = shiftStarts.atDate(weekTemp);
				LocalDateTime shiftEndsName = shiftEnds.isBefore(shiftStarts) ? shiftEnds.atDate(weekTemp.plusDays(1))
						: shiftEnds.atDate(weekTemp);
				GeneratorShift generatorShift = new GeneratorShift(shiftStartsName, weekTemp, shiftStartsName,
						shiftEndsName);
				generatorShift.setWorkerNeeded(weekGenerator.getWorkersNumberPerShift());
				workingDay.add(generatorShift);
				shiftViews.add(new ShiftView(weekTemp, shiftTimes.get(i).getStart(), shiftTimes.get(i).getEnd()));
			}
			workingWeek.put(weekTemp, workingDay);
			schedule.put(weekTemp, shiftViews);
			weekTemp = weekTemp.plusDays(1);
		}
		return workingWeek;
	}

	@Override
	public void addPrefer(Long id, String workerName, WorkerPreferShiftsDto shift) {
		GeneratorWorker generatorWorker = workerPrefers.get(id);
		List<LocalDateTime> shifts = shift.getShifts();
		if (generatorWorker == null) {
			generatorWorker = new GeneratorWorker();
		}
		generatorWorker.addPrefers(shift.getDayName(), shifts);
		generatorWorker.setName(workerName);
		generatorWorker.setId(id);
		generatorWorker.setHoursPerShift(hoursPerShift);
		workerPrefers.put(id, generatorWorker);
		addPreferToWorkingWeekAvailable(generatorWorker, shift.getDayName(), shifts);
	}

	private void addPreferToWorkingWeekAvailable(GeneratorWorker generatorWorker, LocalDate dayName,
			List<LocalDateTime> shifts) {
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
	public Map<LocalDate, List<ShiftView>> generateSchedule() {
		if (workingWeekCopy.isEmpty() && !workingWeek.isEmpty()) {
			workingWeekCopy.putAll(workingWeek);
		}
		if (workingWeek.isEmpty() && !workingWeekCopy.isEmpty()) {
			workingWeek.putAll(workingWeekCopy);
		}
		List<GeneratorShift> allShifts = concatAllShifts();
		Collections.sort(allShifts);
		allShifts.forEach(shift -> {
			while (shift.getWorkerNeeded() > 0) {
				if(saveWorker(shift)) {
					shift.setWorkerNeeded(shift.getWorkerNeeded() - 1);
				}
				if(shift.getAvailable().size() <= 0) {
					break;
				}
			}
		});
		workingWeek.clear();
		return schedule;
	}

	private boolean saveWorker(GeneratorShift shift) {
		GeneratorWorker worker = shift.getCandidate();
		saveWorkerPriority(worker);
		saveWorkerToSchedule(worker, shift.getShiftName(), shift.getDayName());
		return true;
	}

	private void saveWorkerPriority(GeneratorWorker worker) {
		if (worker != null) {
			workingWeek.entrySet().forEach(entry -> {
				List<GeneratorShift> freshShifts = new ArrayList<>();
				LocalDate dayName = entry.getKey();
				entry.getValue().forEach(sh -> {
					List<GeneratorWorker> freshAvailable = new ArrayList<>();
					sh.getAvailable().forEach(w -> {
						if (w.equals(worker)) {
							w.addToSchedule(sh.getShiftName());
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

	private void saveWorkerToSchedule(GeneratorWorker worker, LocalDateTime shiftName, LocalDate dayName) {
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

	private List<String> getAllWorkersForDay(LocalDate key) {
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
	public List<WorkerShiftView> getWorkerSchedule(Worker worker) {
		List<WorkerShiftView> workerSchedule = new ArrayList<>();
		schedule.entrySet().stream().forEach(entry -> {
			List<ShiftView> day = entry.getValue();
			day.stream().forEach(sv -> {
				if (sv.getWorkerNames().contains(worker.getName())) {
					workerSchedule.add(new WorkerShiftView(entry.getKey(), sv.getShiftName()));
				}
			});
		});
		return workerSchedule;
	}
}
