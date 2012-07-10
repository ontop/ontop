package it.unibz.krdb.obda.model;

import java.util.ArrayList;
import java.util.List;

public class OBDAQueryModifiers {

	private boolean isDistinct;
	
	private long limit;
	private long offset;
	
	private List<OrderCondition> orderConditions;
	
	public OBDAQueryModifiers() {
		isDistinct = false;
		limit = -1;
		offset = -1;
		orderConditions = new ArrayList<OrderCondition>();
	}
	
	public void copy(OBDAQueryModifiers other) {
		isDistinct = other.isDistinct;
		limit = other.limit;
		offset = other.offset;
		orderConditions.addAll(other.orderConditions);
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
	
	public void addOrderCondition(Variable var, int direction) {
		OrderCondition condition = new OrderCondition(var, direction);
		orderConditions.add(condition);
	}
	
	public List<OrderCondition> getSortConditions() {
		return orderConditions;
	}
	
	public boolean hasModifiers() {
		return limit != -1 || offset != -1 || !orderConditions.isEmpty();
	}
	
	/**
	 * A helper class to store the sort conditions
	 */
	public class OrderCondition {
		public static final int ORDER_ASCENDING = 1;
		public static final int ORDER_DESCENDING = 2;
		
		private Variable var;
		private int direction;
		
		OrderCondition(Variable var, int direction) {
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
