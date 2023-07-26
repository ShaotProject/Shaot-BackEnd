package com.shaot.schedule.generator;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class GeneratorWorkingDay {
	private String dayName;
	private List<ShiftView> shiftViews = new ArrayList<>();
	
	public void addShiftView(ShiftView shiftView) {
		shiftViews.add(shiftView);
	}
}
