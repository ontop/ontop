package it.unibz.krdb.obda.model;

import java.util.ArrayList;
import java.util.List;

public class OBDAQueryModifiers {

	private boolean isDistinct;
	
	private long limit;
	private long offset;
	
	private List<SortCondition> sortConditions;
	
	public OBDAQueryModifiers() {
		isDistinct = false;
		limit = -1;
		offset = -1;
		sortConditions = new ArrayList<SortCondition>();
	}
	
	public void copy(OBDAQueryModifiers other) {
		isDistinct = other.isDistinct;
		limit = other.limit;
		offset = other.offset;
		sortConditions.addAll(other.sortConditions);
	}
	
	public void setDistinct() {
		isDistinct = true;
	}
	
	public boolean isDistinct() {
		return isDistinct;
	}
	
	public void setLimit(long limit) {
		this.limit = limit;
	}
	
	public long getLimit() {
		return limit;
	}
	
	public void setOffset(long offset) {
		this.offset = offset;
	}
	
	public long getOffset() {
		return offset;
	}
	
	public void addSortCondition(Variable var, int direction) {
		SortCondition condition = new SortCondition(var, direction);
		sortConditions.add(condition);
	}
	
	public List<SortCondition> getSortConditions() {
		return sortConditions;
	}
	
	/**
	 * A helper class to store the sort conditions
	 */
	class SortCondition {
		public static final int ORDER_ASCENDING = 1;
		public static final int ORDER_DESCENDING = 2;
		
		private Variable var;
		private int direction;
		
		SortCondition(Variable var, int direction) {
			this.var = var;
			this.direction = direction;
		}
		
		public Variable getVariable() {
			return var;
		}
		
		public int getDirection() {
			return direction;
		}
	}
}
