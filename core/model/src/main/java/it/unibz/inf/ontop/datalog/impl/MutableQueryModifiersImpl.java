package it.unibz.inf.ontop.datalog.impl;

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

import it.unibz.inf.ontop.datalog.MutableQueryModifiers;
import it.unibz.inf.ontop.datalog.OrderCondition;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.ArrayList;
import java.util.List;

public class MutableQueryModifiersImpl implements MutableQueryModifiers {
	private boolean isDistinct;

	private long limit;
	private long offset;

	private final List<OrderCondition> orderConditions;
	private final List<Variable> groupConditions;
	
	public MutableQueryModifiersImpl() {
		isDistinct = false;
		limit = -1;
		offset = -1;
		orderConditions = new ArrayList<>();
		groupConditions = new ArrayList<>();
	}

	@Override
	public MutableQueryModifiers clone() {
		MutableQueryModifiersImpl clone = new MutableQueryModifiersImpl();
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
	public void setDistinct() {
		isDistinct = true;
	}

	@Override
	public boolean isDistinct() {
		return isDistinct;
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
		return limit != -1;
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
		return offset != -1;
	}

	@Override
	public void addOrderCondition(Variable var, int direction) {
		OrderCondition condition = new OrderConditionImpl(var, direction);
		orderConditions.add(condition);
	}
	
	@Override
	public List<OrderCondition> getSortConditions() {
		return orderConditions;
	}

	@Override
	public boolean hasOrder() {
		return !orderConditions.isEmpty();
	}
	
	@Override
	public boolean hasGroup() {
		return !groupConditions.isEmpty();
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

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			if (obj == null || !(obj instanceof OrderConditionImpl)) {
				return false;
			}
			OrderConditionImpl name2 = (OrderConditionImpl) obj;
			return this.var.equals(name2.var) && this.direction == name2.direction;
		}

		@Override
		public OrderCondition newVariable(Variable newVariable) {
			return new OrderConditionImpl(newVariable, direction);
		}
	}
}
