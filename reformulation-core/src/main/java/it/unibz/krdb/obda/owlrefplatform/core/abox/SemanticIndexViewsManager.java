package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticIndexViewsManager {

	private final Set<SemanticIndexRecord> nonEmptyEntityRecord = new HashSet<>();

	public void setNonEmpty(int idx, COL_TYPE t1, COL_TYPE t2) {
		SemanticIndexRecord record = new SemanticIndexRecord(t1, t2, idx);
		nonEmptyEntityRecord.add(record);
	}
	
	public void setNonEmpty(int idx, COL_TYPE t1) {
		SemanticIndexRecord record = new SemanticIndexRecord(t1, idx);
		nonEmptyEntityRecord.add(record);		
	}
	
	public void setNonEmpty(int sitable, int type1, int type2, int idx) {
		SemanticIndexRecord.checkTypeValue(type1);
		SemanticIndexRecord.checkTypeValue(type2);
		SemanticIndexRecord.checkSITableValue(sitable);
		
		SemanticIndexRecord r = new SemanticIndexRecord(sitable, type1, type2, idx);
		nonEmptyEntityRecord.add(r);
	}

	
	public boolean isMappingEmpty(List<Interval> intervals,  COL_TYPE type1)  {
		
		for (Interval interval : intervals) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
				SemanticIndexRecord record = new SemanticIndexRecord(type1, i);
				if (nonEmptyEntityRecord.contains(record))
					return false;
			}
		
		return true;
	}

	public boolean isMappingEmpty(List<Interval> intervals, COL_TYPE type1, COL_TYPE type2)  {
		
		for (Interval interval : intervals) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
				SemanticIndexRecord record = new SemanticIndexRecord(type1, type2, i);
				if (nonEmptyEntityRecord.contains(record)) 
					return false;
			}
		
		return true;
	}
	
	public Set<SemanticIndexRecord> getRecords() {
		return nonEmptyEntityRecord;
	}
	
}
