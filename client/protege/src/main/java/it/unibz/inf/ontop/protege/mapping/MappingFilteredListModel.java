package it.unibz.inf.ontop.protege.mapping;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;

import javax.annotation.Nonnull;
import javax.swing.*;

public class MappingFilteredListModel extends AbstractListModel<TriplesMap> implements TriplesMapManagerListener {

	private static final long serialVersionUID = 2317408823037931358L;
	
	private final OBDAModelManager obdaModelManager;
	@Nonnull
	private ImmutableList<String> filter;

	public MappingFilteredListModel(OBDAModelManager obdaModelManager) {
		this.obdaModelManager = obdaModelManager;
		this.filter = ImmutableList.of();
	}

	private TriplesMapManager getCurrent() {
		return obdaModelManager.getCurrentOBDAModel().getTriplesMapManager();
	}

	public void setFilter(@Nonnull ImmutableList<String> filter) {
		this.filter = filter;
		fireContentsChanged(getCurrent(), 0, getSize());
	}

	@Override
	public int getSize() {
		return (int) getCurrent().stream()
				.filter(this::isIncludedByFilter)
				.count();
	}

	@Override
	public TriplesMap getElementAt(int index) {
		int filteredCount = -1;
		for (TriplesMap triplesMap : getCurrent()) {
			if (isIncludedByFilter(triplesMap))
				filteredCount++;

			if (filteredCount == index)
				return triplesMap;
		}
		return null;
	}

	private boolean isIncludedByFilter(TriplesMap triplesMap) {
		return filter.isEmpty()
				|| triplesMap.getTargetAtoms().stream()
						.flatMap(a -> a.getSubstitutedTerms().stream())
						.anyMatch(this::match);
	}

	private boolean match(ImmutableTerm term) {
		if (term instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionTerm = (ImmutableFunctionalTerm) term;
			if (match(functionTerm.getFunctionSymbol().toString()))
				return true;

			// Recursive
			return functionTerm.getTerms().stream()
					.anyMatch(this::match);
		}
		else if (term instanceof RDFConstant) {
			return match(((RDFConstant) term).getValue());
		}
		else
			return false;
	}

	private boolean match(String s) {
		return filter.stream().anyMatch(s::contains);
	}

	@Override
	public void changed(TriplesMapManager triplesMapCollection) {
		fireContentsChanged(triplesMapCollection, 0, getSize());
	}
}
