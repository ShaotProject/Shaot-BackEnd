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
	private Set<GeneratorWorker> generatorWorkers;
	private Set<GeneratorShift> workingWeek;
	private Set<GeneratorShift> workingWeekCopy;
	private Long hoursPerShift;

	public ScheduleGeneratorImpl() {
		workerPrefers = new ConcurrentHashMap<>();
		schedule = new HashSet<>();
		workingWeek = new TreeSet<>();
		generatorWorkers = new HashSet<>();
	}

	@Override
	public Set<GeneratorShift> generateWeek(ScheduleConfigurationDto weekGenerator) {
		workingWeek.clear();
		schedule.clear();
		LocalDate weekPointer = weekGenerator.getWeekStart();
		LocalDate weekEnd = weekGenerator.getWeekEnd();
		List<ScheduleConfigurationShiftTime> time = weekGenerator.getShiftsTime();
		while (!weekPointer.isAfter(weekEnd)) {
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
	public Set<ShiftView> generateSchedule() {
		List<ShiftView> shiftViews = new ArrayList<>();
		List<GeneratorShift> allShifts = copyWeek(workingWeek);

		allShifts.forEach(generatorShift -> {
			while(generatorShift.getAvailable().size() > 0 && generatorShift.getWorkerNeeded() > 0) {
				GeneratorWorker candidate = getCandidate(generatorShift, allShifts);
				if(candidate == null) {
					generatorShift.removeAvailable();
				} else {
					generatorShift.setWorkerNeeded(generatorShift.getWorkerNeeded() - 1);
				}
				addWorkerToShiftViews(candidate, shiftViews, generatorShift);
			}
		});

		schedule.clear();
		Collections.sort(shiftViews);
		schedule.addAll(shiftViews);
		return schedule;
	}
	
	private GeneratorWorker getCandidate(GeneratorShift generatorShift, List<GeneratorShift> workingWeek) {
		List<GeneratorWorker> available = generatorShift.getAvailable();
		GeneratorWorker worker = Collections.min(available);
		return updateWorker(generatorShift.getShiftName(), worker, generatorShift.getShiftEnd(), workingWeek);
	}
	
	
	private GeneratorWorker updateWorker(LocalDateTime shiftName, GeneratorWorker candidate, LocalTime shiftEnd, List<GeneratorShift> workingWeek) {
		if(!candidate.getRestrict().contains(shiftName)) {
			workingWeek.forEach(generatorShift -> {
				generatorShift.updateAvailable(candidate, shiftEnd, shiftName);
			});
			return candidate;
		}
		return null;
	}

	private List<ShiftView> addWorkerToShiftViews(GeneratorWorker worker, List<ShiftView> shiftViews, GeneratorShift generatorShift) {
		for(int i = 0; i < shiftViews.size(); i++) {
			if(shiftViews.get(i).getShiftName().equals(generatorShift.getShiftName())) {
				if(worker != null) {
					shiftViews.get(i).addWorkerName(worker.getName());
				}
				return shiftViews;
			}
		}
		ShiftView newShift = new ShiftView(generatorShift.getShiftName(), generatorShift.getDayName(), generatorShift.getShiftStart(), generatorShift.getShiftEnd());
		if(worker != null) {
			newShift.addWorkerName(worker.getName());
		}
		shiftViews.add(newShift);
		return shiftViews;
	}
	

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
	public List<WorkerShiftView> getWorkerSchedule(Worker worker) {
		List<WorkerShiftView> workerSchedule = new ArrayList<>();
		schedule.stream().forEach(shiftView -> {
			if (shiftView.getWorkerNames().contains(worker.getName())) {
				workerSchedule.add(new WorkerShiftView(shiftView.getDayName(), shiftView.getShiftName()));
			}
		});
		return workerSchedule;
	}
	
	public void setWorkerOnShiftManual(Long workerId, String workerName, LocalDateTime shiftName) {
		schedule.forEach(generatorShift -> {
			if(generatorShift.getShiftName().equals(shiftName)) {
				generatorShift.addWorkerName(workerName);
			}
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
}
