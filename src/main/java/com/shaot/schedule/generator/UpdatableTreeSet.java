package com.shaot.schedule.generator;

import java.util.TreeSet;

public class UpdatableTreeSet<T> extends TreeSet<GeneratorShift> {
	private static final long serialVersionUID = 1L;
	
	public boolean updateValue(GeneratorShift from, GeneratorShift to) {
		remove(from);
		return add(to);
	}
}
