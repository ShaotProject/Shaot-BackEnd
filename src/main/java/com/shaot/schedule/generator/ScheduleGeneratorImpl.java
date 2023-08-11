package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneOffsetTransitionRule;
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
		workingWeek = new HashSet<>();
		workingWeekCopy = new HashSet<>();
	}

	@Override
	public Set<GeneratorShift> generateWeek(ScheduleConfigurationDto weekGenerator) {
		workingWeek.clear();
		schedule.clear();
		LocalDate weekPointer = weekGenerator.getWeekStart();
		LocalDate weekEnd = weekGenerator.getWeekEnd();
		List<ScheduleConfigurationShiftTime> time = weekGenerator.getShiftsTime();
		while (weekPointer.isBefore(weekEnd)) {
			for (int i = 0; i < time.size(); i++) {
				LocalDateTime shiftName = time.get(i).getStart().atDate(weekPointer);
				GeneratorShift generatorShift = new GeneratorShift(shiftName, weekPointer, time.get(i).getStart(),
						time.get(i).getEnd());
				generatorShift.setWorkerNeeded(weekGenerator.getWorkersNumberPerShift());
				ShiftView shiftView = new ShiftView(shiftName, weekPointer, time.get(i).getStart(),
						time.get(i).getEnd());
				workingWeek.add(generatorShift);
				schedule.add(shiftView);
			}
			weekPointer = weekPointer.plusDays(1);
		}
		return workingWeek;
	}

	@Override
	public void addPrefer(Long id, String workerName, WorkerPreferShiftsDto shiftPrefers) {
		GeneratorWorker generatorWorker = new GeneratorWorker(id, workerName);
		LocalDate dayName = shiftPrefers.getDayName();
		shiftPrefers.getShifts().forEach(time -> {
			LocalDateTime shiftName = time.atDate(dayName);
			workingWeek.forEach(generatorShift -> {
				if (generatorShift.getShiftName().equals(shiftName)) {
					generatorShift.addAvailable(generatorWorker);
				}
			});
		});
	}

	@Override
	public Set<ShiftView> generateSchedule() {
		List<GeneratorWorker> workers = new ArrayList<>();
		List<ShiftView> shiftViews = new ArrayList<>();
		workingWeek.forEach(generatorShift -> {
			while (generatorShift.getWorkerNeeded() > 0 && generatorShift.getAvailable().size() > 0) {
				GeneratorWorker worker = generatorShift.getCandidate();
				if (worker != null) {
					Long hoursPerShift = Long
							.valueOf(generatorShift.getShiftEnd().getHour() - generatorShift.getShiftStart().getHour());
					worker.setHoursPerShift(hoursPerShift);
					if (workers.contains(worker)) {
						int index = workers.indexOf(worker);
						worker = workers.get(index);
						workers.remove(index);
					}
					worker.addToSchedule(generatorShift.getShiftName());
					workers.add(worker);

					ShiftView shiftView = new ShiftView(generatorShift.getShiftName(), generatorShift.getDayName(),
							generatorShift.getShiftStart(), generatorShift.getShiftEnd());
					if (shiftViews.contains(shiftView)) {
						int index = shiftViews.indexOf(shiftView);
						shiftView = shiftViews.get(index);
						shiftViews.remove(index);
					}
					if (!worker.getRestrict().contains(generatorShift.getShiftName())) {
						shiftView.addWorkerName(worker.getName());
						generatorShift.setWorkerNeeded(generatorShift.getWorkerNeeded() - 1);
					}
					generatorShift.removeAvailable(worker);
					shiftViews.add(shiftView);
				}
			}
		});

		schedule.clear();
		schedule.addAll(shiftViews);
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
		saveWorkerToSchedule(worker, shift.getShiftName());
		return true;
	}

	private void saveWorkerPriority(GeneratorWorker worker, LocalDateTime shiftName) {
		worker.addToSchedule(shiftName);
		workingWeek.forEach(generatorShift -> {
			if (generatorShift.getAvailable().contains(worker)) {
				generatorShift.removeAvailable(worker);
				generatorShift.addAvailable(worker);
			}
		});
	}

	private void saveWorkerToSchedule(GeneratorWorker worker, LocalDateTime shiftName) {
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
				workerSchedule.add(new WorkerShiftView(shiftView.getDayName(), shiftView.getShiftName()));
			}
		});
		return workerSchedule;
	}
}
