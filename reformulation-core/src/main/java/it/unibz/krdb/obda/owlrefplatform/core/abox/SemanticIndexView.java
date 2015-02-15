package it.unibz.krdb.obda.owlrefplatform.core.abox;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;

public class SemanticIndexView {
	
	private final SemanticIndexViewID id;
	private final String selectCommand;
	private final String insertCommand;
	private final Set<Integer> indexes = new HashSet<>();
	
	public SemanticIndexView(SemanticIndexViewID id, String selectCommand, String insertCommand) {
		this.id = id;
		this.selectCommand = selectCommand;
		this.insertCommand = insertCommand;
	}
	
	public SemanticIndexViewID getId() {
		return id;
	}
	
	public String getSELECT() {
		return selectCommand;
	}
	
	public String getINSERT() {
		return insertCommand;
	}
	
	public boolean isEmptyForIntervals(List<Interval> intervals) {
		for (Interval interval : intervals) 
			for (Integer i = interval.getStart(); i <= interval.getEnd(); i++) 
				if (indexes.contains(i)) 
					return false;
		return true;
	}

	public void addIndex(Integer idx) {
		indexes.add(idx);			
	}
	
	public Set<Integer> getIndexes() {
		return Collections.unmodifiableSet(indexes);
	}
}
