package com.shaot.schedule.generator;

import java.time.DayOfWeek;
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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.mongodb.core.query.Update;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.company.CompanyTestAlarmPoint;
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
	private Set<DayView> schedule;
	private Set<DayView> lastWeekSchedule;
	private Set<GeneratorWorker> generatorWorkers;
	private Set<GeneratorShift> workingWeek;
	private TreeMap<String, List<DayView>> lastWeeks;
	private Long hoursPerShift;
	private LocalDate alarmPoint;
	private ScheduleConfigurationDto basicConfiguration;

	public ScheduleGeneratorImpl() {
		workerPrefers = new ConcurrentHashMap<>();
		schedule = new HashSet<>();
		lastWeekSchedule = new HashSet<>();
		workingWeek = new TreeSet<>();
		generatorWorkers = new HashSet<>();
		lastWeeks = new TreeMap<>();
	}

	@Override
	public ScheduleConfigurationDto generateWeek(ScheduleConfigurationDto weekGenerator) {
		workingWeek.clear();
		schedule.clear();
		this.alarmPoint = weekGenerator.getAlarmPoint();
		this.basicConfiguration = weekGenerator;
		LocalDate weekPointer = weekGenerator.getWeekStart();
		LocalDate weekEnd = weekGenerator.getWeekEnd();
		List<String> workDays = weekGenerator.getWorkDays();
		List<ScheduleConfigurationShiftTime> shiftData = weekGenerator.getShiftsTime();
		while (!weekPointer.isAfter(weekEnd)) {
			String weekPointerDayName = weekPointer.getDayOfWeek().toString();
			List<ShiftView> shiftViews = new ArrayList<>();
			if (workDays.contains(weekPointerDayName.toString())) {
				for (int i = 0; i < shiftData.size(); i++) {
					LocalDateTime shiftName = weekPointer.atTime(shiftData.get(i).getStart());
					GeneratorShift shift = new GeneratorShift(shiftName, weekPointerDayName,
							shiftData.get(i).getStart(), shiftData.get(i).getEnd());
					shift.setWorkerNeeded(shiftData.get(i).getWorkersNumberPerShift());
					workingWeek.add(shift);
					shiftViews.add(new ShiftView(shiftName, shiftData.get(i).getStart(), shiftData.get(i).getEnd(),
							new HashSet<>(), shiftData.get(i).getWorkersNumberPerShift()));
				}
			}
			schedule.add(new DayView(weekPointer, weekPointerDayName, shiftViews));
			weekPointer = weekPointer.plusDays(1);
		}

		if (lastWeekSchedule.isEmpty()) {
			lastWeekSchedule.addAll(schedule);
		}

		return weekGenerator;
	}

	@Override
	public void addPrefer(Long id, String workerName, WorkerPreferShiftsDto shiftPrefers, int priority) {
		GeneratorWorker generatorWorker = new GeneratorWorker(id, workerName);
		LocalDate dayName = shiftPrefers.getDayName();
		generatorWorker.setPriorityByShiftsNumber(priority);
		generatorWorkers.add(generatorWorker);
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
	public Set<DayView> generateSchedule() {
		List<DayView> dayViews = new ArrayList<>();
		dayViews.addAll(schedule);
		List<GeneratorShift> allShifts = copyWeek(workingWeek);
//		LocalDate testAlarm = alarmPointTest.getAlarmPoint() == null ? null : alarmPointTest.getAlarmPoint();

//		if ((testAlarm != null && (testAlarm.isEqual(alarmPoint) || testAlarm.isAfter(alarmPoint)))
//			|| LocalDate.now().equals(alarmPoint) || alarmPoint.isBefore(LocalDate.now())) {
		allShifts.forEach(generatorShift -> {
			while (generatorShift.getAvailable().size() > 0 && generatorShift.getWorkerNeeded() > 0) {
				GeneratorWorker candidate = getCandidate(generatorShift, allShifts);
				if (candidate == null) {
					generatorShift.removeAvailable();
				} else {
					generatorShift.setWorkerNeeded(generatorShift.getWorkerNeeded() - 1);
				}
				addWorkerToDayViews(candidate, dayViews, generatorShift);
			}
		});

		Collections.sort(dayViews);
		addScheduleToLastWeeks(dayViews);
		schedule.clear();
		schedule.addAll(dayViews);
		lastWeekSchedule.clear();
		lastWeekSchedule.addAll(dayViews);
		return schedule;
	}

	private void addScheduleToLastWeeks(List<DayView> dayViews) {
		if(lastWeeks.values().size() >= 5) {
			lastWeeks.pollFirstEntry();
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
		String weekStart = formatter.format(dayViews.get(0).getDayDate());
		String weekEnd = formatter.format(dayViews.get(dayViews.size() - 1).getDayDate());
		String period = weekStart + " - " + weekEnd;
		lastWeeks.put(period, dayViews);
	}

	private GeneratorWorker getCandidate(GeneratorShift generatorShift, List<GeneratorShift> workingWeek) {
		List<GeneratorWorker> available = generatorShift.getAvailable();
		GeneratorWorker worker = Collections.min(available);
		return updateWorker(generatorShift.getShiftName(), worker, generatorShift.getShiftEnd(), workingWeek);
	}

	private GeneratorWorker updateWorker(LocalDateTime shiftName, GeneratorWorker candidate, LocalTime shiftEnd,
			List<GeneratorShift> workingWeek) {
		if (!candidate.getRestrict().contains(shiftName)) {
			workingWeek.forEach(generatorShift -> {
				generatorShift.updateAvailable(candidate, shiftEnd, shiftName);
			});
			return candidate;
		}
		return null;
	}

	private List<DayView> addWorkerToDayViews(GeneratorWorker worker, List<DayView> dayViews,
			GeneratorShift generatorShift) {
		for (int i = 0; i < dayViews.size(); i++) {
			for (int j = 0; j < dayViews.get(i).getShifts().size(); j++) {
				if (dayViews.get(i).getShifts().get(j).getShiftName().equals(generatorShift.getShiftName())) {
					if (worker != null) {
						dayViews.get(i).getShifts().get(j).addWorkerName(worker.getName());
					}
				}
			}
		}
		return dayViews;
	}

//		ShiftView newShift = new ShiftView(generatorShift.getShiftName(), generatorShift.getDayName(),
//				generatorShift.getShiftStart(), generatorShift.getShiftEnd());
//		if (worker != null) {
//			newShift.addWorkerName(worker.getName());
//		}
//		shiftViews.add(newShift);
//		return shiftViews;
//	}

	private List<GeneratorShift> copyWeek(Set<GeneratorShift> workingWeek) {
		List<GeneratorShift> copy = new ArrayList<>();
		workingWeek.forEach(wd -> copy.add(wd));
		return copy;
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
	public List<String> getLastWeeksNames() {
		return lastWeeks.keySet().stream().toList();
	}
	
	@Override
	public List<DayView> getWeekByPeriod(String period) {
		return lastWeeks.get(period);
	}
	
	
	@Override
	public List<WorkerShiftView> getWorkerSchedule(Worker worker) {
//		List<WorkerShiftView> workerSchedule = new ArrayList<>();
//		schedule.stream().forEach(shiftView -> {
//			if (shiftView.getWorkerNames().contains(worker.getName())) {
//				workerSchedule.add(new WorkerShiftView(shiftView.getDayName(), shiftView.getShiftName()));
//			}
//		});
		return null;
	}

	public void setWorkerOnShiftManual(String workerName, LocalDateTime shiftName) {
		schedule.forEach(dv -> {
			dv.addWorkerManualy(workerName, shiftName);
		});
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
	public void addShift(CompanyAddShiftDto companyAddShiftDto) {
		// TODO
	}
	
	public Set<DayView> setSchedule(Set<DayView> schedule) {
		this.schedule = schedule;
		this.lastWeekSchedule = schedule;
		return this.schedule;
	}
}
