package org.semanticweb.ontop.model.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.semanticweb.ontop.model.OBDAQueryModifiers;
import org.semanticweb.ontop.model.OrderCondition;
import org.semanticweb.ontop.model.Variable;

import java.util.ArrayList;
import java.util.List;

public class MutableQueryModifiersImpl implements OBDAQueryModifiers {
	private boolean isCount;
	private boolean isDistinct;

	private long limit;
	private long offset;

	private List<org.semanticweb.ontop.model.OrderCondition> orderConditions;
	private List<Variable> groupConditions;
	
	public MutableQueryModifiersImpl() {
		isCount = false;
		isDistinct = false;
		limit = -1;
		offset = -1;
		orderConditions = new ArrayList<OrderCondition>();
		groupConditions = new ArrayList<Variable>();
	}

	@Override
	public OBDAQueryModifiers clone() {
		MutableQueryModifiersImpl clone = new MutableQueryModifiersImpl();
		clone.isCount = isCount;
		clone.isDistinct = isDistinct;
		clone.limit = limit;
		clone.offset = offset;
		for (OrderCondition c : orderConditions) {
			clone.orderConditions.add(c.clone());
		}
		clone.groupConditions.addAll(groupConditions);
		return clone;
	}

	@Override
	public void copy(OBDAQueryModifiers other) {
		isDistinct = other.isDistinct();
		isCount = other.isCount();
		limit = other.getLimit();
		offset = other.getOffset();
		orderConditions.addAll(other.getSortConditions());
		groupConditions.addAll(other.getGroupConditions());
	}

	@Override
	public void setDistinct() {
		isDistinct = true;
	}

	@Override
	public boolean isDistinct() {
		return isDistinct;
	}
	
	@Override
	public void setCount() {
		isCount = true;
	}

	@Override
	public boolean isCount() {
		return isCount;
	}

	@Override
	public void setLimit(long limit) {
		this.limit = limit;
	}

	@Override
	public long getLimit() {
		return limit;
	}

	@Override
	public boolean hasLimit() {
		return limit != -1 ? true : false;
	}

	@Override
	public void setOffset(long offset) {
		this.offset = offset;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public boolean hasOffset() {
		return offset != -1 ? true : false;
	}

	@Override
	public void addOrderCondition(Variable var, int direction) {
		OrderCondition condition = new OrderConditionImpl(var, direction);
		orderConditions.add(condition);
	}
	
	@Override
	public void addGroupCondition(Variable var) {
		groupConditions.add(var);
	}

	@Override
	public List<Variable> getGroupConditions() {
		return groupConditions;
	}
	
	@Override
	public List<OrderCondition> getSortConditions() {
		return orderConditions;
	}

	@Override
	public boolean hasOrder() {
		return !orderConditions.isEmpty() ? true : false;
	}
	
	@Override
	public boolean hasGroup() {
		return !groupConditions.isEmpty() ? true : false;
	}

	@Override
	public boolean hasModifiers() {
		return isDistinct || hasLimit() || hasOffset() || hasOrder() || hasGroup();
	}

	/**
	 * A helper class to store the sort conditions
	 */
	public class OrderConditionImpl implements OrderCondition {

		private Variable var;
		private int direction;

		OrderConditionImpl(Variable var, int direction) {
			this.var = var;
			this.direction = direction;
		}

		@Override
		public Variable getVariable() {
			return var;
		}

		@Override
		public int getDirection() {
			return direction;
		}

		@Override
		public OrderCondition clone() {
			return this;
		}
	}
}
