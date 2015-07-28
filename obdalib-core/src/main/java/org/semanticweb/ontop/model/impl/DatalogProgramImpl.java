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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDAQueryModifiers;
import org.semanticweb.ontop.model.Predicate;

public class DatalogProgramImpl implements DatalogProgram {

	private static final long serialVersionUID = -1644491423712454150L;

	private List<CQIE> rules = null;

	private Map<Predicate, List<CQIE>> predicateIndex = null;

	private OBDAQueryModifiers modifiers;

	@Override
	public DatalogProgram clone() {
		DatalogProgramImpl clone = new DatalogProgramImpl();
		for (CQIE query : rules) {
			clone.appendRule(query.clone());
		}
		clone.modifiers = modifiers.clone();
		return clone;
	}

	protected DatalogProgramImpl() {
		modifiers = new OBDAQueryModifiers();
		rules = new LinkedList<CQIE>();
		predicateIndex = new HashMap<Predicate, List<CQIE>>();
	}

	public void appendRule(CQIE rule) {
		if (rule == null) {
			throw new IllegalArgumentException("DatalogProgram: Recieved a null rule.");
		}
		if (rules.contains(rule)) {
			return; // Skip if the rule already exists!
		}

		rules.add(rule);

		Function head = rule.getHead();
		if (head != null) {
			Predicate predicate = rule.getHead().getFunctionSymbol();
			List<CQIE> indexedRules = predicateIndex.get(predicate);
			if (indexedRules == null) {
				indexedRules = new LinkedList<CQIE>();
				predicateIndex.put(predicate, indexedRules);
			}
			indexedRules.add(rule);
		}
	}

	public void appendRule(Collection<CQIE> rules) {
		for (CQIE rule : rules) {
			appendRule(rule);
		}
	}

	public void removeRule(CQIE rule) {
		if (rule == null) {
			throw new RuntimeException("Invalid parameter: null");
		}
		rules.remove(rule);

		Predicate predicate = rule.getHead().getFunctionSymbol();
		List<CQIE> indexedRules = this.predicateIndex.get(predicate);
		if (indexedRules != null)
			indexedRules.remove(rule);
	}

	public void removeRules(Collection<CQIE> rules) {
		for (CQIE rule : rules) {
			removeRule(rule);
		}
	}

	public boolean isUCQ() {
		if (rules.size() > 1) {
			boolean isucq = true;
			CQIE rule0 = rules.get(0);
			Function head0 = rule0.getHead();
			for (int i = 1; i < rules.size() && isucq; i++) {
				CQIE ruleI = rules.get(i);
				Function headI = ruleI.getHead();
				if (head0.getArity() != headI.getArity() || !(head0.getFunctionSymbol().equals(headI.getFunctionSymbol()))) {
					isucq = false;
				}
			}
			return isucq;
		} else if (rules.size() == 1) {
			return true;
		} else {
			return false;
		}
	}

	public List<CQIE> getRules() {
		return Collections.unmodifiableList(rules);
	}

	public String toString() {
		StringBuffer bf = new StringBuffer();
		for (CQIE rule : rules) {
			bf.append(rule.toString());
			bf.append("\n");
		}
		return bf.toString();
	}

	@Override
	public List<CQIE> getRules(Predicate headPredicate) {
		List<CQIE> rules = predicateIndex.get(headPredicate);
		if (rules == null) {
			rules = new LinkedList<CQIE>();
		}
		return Collections.unmodifiableList(rules);
	}

	@Override
	public OBDAQueryModifiers getQueryModifiers() {
		return modifiers;
	}

	@Override
	public boolean hasModifiers() {
		return modifiers.hasModifiers();
	}
}
