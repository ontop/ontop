package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.*;
import java.util.stream.Collectors;

public class RepositoryTableSlice {

	private final ImmutableList<RDFTermType> id;
	private final String selectCommand;
	private final String insertCommand;
	private final RepositoryTableManager.PreparedStatementInsertAction insertAction;
	private final Set<Integer> indexes = new HashSet<>();
	
	public RepositoryTableSlice(ImmutableList<RDFTermType> id, String selectCommand, String insertCommand, RepositoryTableManager.PreparedStatementInsertAction insertAction) {
		this.id = id;
		this.selectCommand = selectCommand;
		this.insertCommand = insertCommand;
		this.insertAction = insertAction;
	}

	public ImmutableList<RDFTermType> getId() {
		return id;
	}
	
	public String getSELECT(SemanticIndexRange range) {
		String filter = range.getIntervals().stream()
				.map(RepositoryTableSlice::getIntervalString)
				.collect(Collectors.joining(" OR "));

		return selectCommand + " AND " + filter;
	}

	private static String getIntervalString(Interval interval) {
		if (interval.getStart() == interval.getEnd())
			return String.format("%s = %d", RepositoryTableManager.IDX_COLUMN, interval.getStart());
		else
			return String.format("%s >= %d AND %s <= %d", RepositoryTableManager.IDX_COLUMN, interval.getStart(),
					RepositoryTableManager.IDX_COLUMN, interval.getEnd());
	}


	public String getINSERT() {
		return insertCommand;
	}

	public RepositoryTableManager.PreparedStatementInsertAction getInsertAction() { return insertAction; }
	
	public boolean isEmptyForIntervals(List<Interval> intervals) {
		for (Interval interval : intervals) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++)
				if (indexes.contains(i)) 
					return false;
		return true;
	}

	public void addIndex(int idx) {
		indexes.add(idx);			
	}
}
