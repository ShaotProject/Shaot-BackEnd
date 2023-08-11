package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.mongodb.core.query.Update;

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
	private Map<Long, GeneratorWorker> workerPrefers;
	private Set<ShiftView> schedule;
	private Set<GeneratorShift> workingWeek;
	private Set<GeneratorShift> workingWeekCopy;
	private Long hoursPerShift;

	public ScheduleGeneratorImpl() {
		workerPrefers = new ConcurrentHashMap<>();
		schedule = new TreeSet<>();
		workingWeek = new UpdatableTreeSet<>();
		workingWeekCopy = new UpdatableTreeSet<>();
	}

	@Override
	public Set<GeneratorShift> generateWeek(ScheduleConfigurationDto weekGenerator) {
		workingWeek.clear();
		schedule.clear();
		LocalTime shiftStart = weekGenerator.getShiftsTime().get(0).getStart();
		LocalTime shiftEnd = weekGenerator.getShiftsTime().get(0).getEnd();
		hoursPerShift = ChronoUnit.HOURS.between(shiftStart, shiftEnd);
		LocalDate weekTemp = weekGenerator.getWeekStart();
		LocalDate weekEnd = weekGenerator.getWeekEnd();
		while (!weekTemp.isEqual(weekEnd.plusDays(1))) {
			List<ScheduleConfigurationShiftTime> shiftTimes = weekGenerator.getShiftsTime();
			for (int i = 0; i < shiftTimes.size(); i++) {
				LocalTime shiftStarts = shiftTimes.get(i).getStart();
				LocalTime shiftEnds = shiftTimes.get(i).getEnd();
				LocalDateTime shiftStartsName = shiftStarts.atDate(weekTemp);
				LocalDateTime shiftEndsName = shiftEnds.isBefore(shiftStarts) ? shiftEnds.atDate(weekTemp.plusDays(1))
						: shiftEnds.atDate(weekTemp);
				String formattedDate = shiftStartsName.format(DateTimeFormatter.ISO_DATE_TIME);
				GeneratorShift generatorShift = new GeneratorShift(formattedDate, weekTemp, shiftStartsName,
						shiftEndsName);
				generatorShift.setWorkerNeeded(weekGenerator.getWorkersNumberPerShift());
				workingWeek.add(generatorShift);
				schedule.add(new ShiftView(weekTemp, shiftTimes.get(i).getStart(), shiftTimes.get(i).getEnd()));
			}
			weekTemp = weekTemp.plusDays(1);
		}
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
		generatorWorker.setHoursPerShift(hoursPerShift);
		workerPrefers.put(id, generatorWorker);
		addPreferToWorkingWeekAvailable(generatorWorker, shift.getDayName(), shifts);
	}

	private void addPreferToWorkingWeekAvailable(GeneratorWorker generatorWorker, String dayName, List<String> shifts) {
		workingWeek.forEach(generatorShift -> {
			shifts.forEach(shiftName -> {
				LocalDateTime date = LocalDateTime.parse(shiftName);
				shiftName = date.format(DateTimeFormatter.ISO_DATE_TIME);
				if (generatorShift.getShiftName().equals(shiftName)) {
					generatorShift.addAvailable(generatorWorker);
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
	public Set<ShiftView> generateSchedule() {
		if (workingWeek.size() <= 0) {
			workingWeek = copyWeek(workingWeekCopy);
		} else {
			workingWeekCopy = copyWeek(workingWeek);
		}
		workingWeek.forEach(shift -> {
			while (shift.getWorkerNeeded() > 0) {
				if (saveWorker(shift)) {
					shift.setWorkerNeeded(shift.getWorkerNeeded() - 1);
				}
				if (shift.getAvailable().size() <= 0) {
					break;
				}
			}
		});
		workingWeek.clear();
		return schedule;
	}

	private Set<GeneratorShift> copyWeek(Set<GeneratorShift> workingWeek) {
		Set<GeneratorShift> copy = new HashSet<>();
		workingWeek.forEach(wd -> copy.add(wd));
		return copy;
	}

	private boolean saveWorker(GeneratorShift shift) {
		GeneratorWorker worker = shift.getCandidate();
		if (worker == null) {
			return false;
		}
		saveWorkerPriority(worker, shift.getShiftName());
		saveWorkerToSchedule(worker, shift.getShiftName(), shift.getDayName().toString());
		return true;
	}

	private void saveWorkerPriority(GeneratorWorker worker, String shiftName) {
		worker.addToSchedule(shiftName);
		workingWeek.forEach(generatorShift -> {
			if(generatorShift.getAvailable().contains(worker)) {
				generatorShift.removeAvailable(worker);
				generatorShift.addAvailable(worker);
			}
		});
	}

	private void saveWorkerToSchedule(GeneratorWorker worker, String shiftName, String dayName) {
		if (worker != null) {
			schedule.forEach(shiftView -> {
				if (shiftName.equals(shiftView.getShiftName())) {
					shiftView.addWorkerName(worker.getName());
				}
			});
			return;
		}
	}

//	private List<String> getAllWorkersForDay(String key) {
//		List<String> workersNames = new ArrayList<>();
//		List<ShiftView> todayShifts = schedule.get(key);
//		todayShifts.forEach(shift -> shift.getWorkerNames().forEach(name -> workersNames.add(name)));
//		return workersNames;
//	}

//	private List<GeneratorShift> concatAllShifts() {
//		List<GeneratorShift> allShifts = new ArrayList<>();
//		workingWeek.entrySet().forEach(entry -> {
//			List<GeneratorShift> list = entry.getValue();
//			list.forEach(gs -> {
//				allShifts.add(gs);
//			});
//		});
//		return allShifts;
//	}

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
		schedule.stream().forEach(shiftView -> {
			if (shiftView.getWorkerNames().contains(worker.getName())) {
				workerSchedule.add(new WorkerShiftView(shiftView.getDayName().toString(), shiftView.getShiftName()));
			}
//			day.stream().forEach(sv -> {
//				if (sv.getWorkerNames().contains(worker.getName())) {
//					workerSchedule.add(new WorkerShiftView(entry.getKey(), sv.getShiftName()));
//				}
//			});
		});
		return workerSchedule;
	}
}
