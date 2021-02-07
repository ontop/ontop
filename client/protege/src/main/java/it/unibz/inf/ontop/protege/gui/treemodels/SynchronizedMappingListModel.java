package it.unibz.inf.ontop.protege.gui.treemodels;

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

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.protege.core.OBDAMappingListener;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;

import javax.annotation.Nullable;
import javax.swing.*;

public class SynchronizedMappingListModel extends AbstractListModel<SQLPPTriplesMap> implements OBDAMappingListener {

	private static final long serialVersionUID = 2317408823037931358L;
	
	private final OBDAModel obdaModel;
	@Nullable
	private String filter;

	public SynchronizedMappingListModel(OBDAModel obdaModel) {
		this.obdaModel = obdaModel;
		this.filter = null;
		obdaModel.addMappingsListener(this);
	}

	public void setFocusedSource() {
		fireContentsChanged(this, 0, getSize());
	}

	public void setFilter(@Nullable String filter) {
		this.filter = filter;
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public int getSize() {
		return (int)obdaModel.getMapping().stream()
				.filter(this::isIncludedByFilter)
				.count();
	}

	@Override
	public SQLPPTriplesMap getElementAt(int index) {
		int filteredCount = -1;
		for (SQLPPTriplesMap mapping : obdaModel.getMapping()) {
			if (isIncludedByFilter(mapping))
				filteredCount++;

			if (filteredCount == index)
				return mapping;
		}
		return null;
	}

	private boolean isIncludedByFilter(SQLPPTriplesMap mapping) {
		return filter == null
				|| mapping.getTargetAtoms().stream()
						.flatMap(a -> a.getSubstitutedTerms().stream())
						.anyMatch(a -> match(filter, a));
	}

	private static boolean match(String keyword, ImmutableTerm term) {
		if (term instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionTerm = (ImmutableFunctionalTerm) term;
			if (functionTerm.getFunctionSymbol().toString().contains(keyword)) { // match found!
				return true;
			}
			// Recursive
			return functionTerm.getTerms().stream()
					.anyMatch(t -> match(keyword, t));
//        } else if (term instanceof Variable) {
//            return ((Variable) term).getName().contains(keyword); // match found!
		}
		else if (term instanceof RDFConstant) {
			return ((RDFConstant) term).getValue().contains(keyword); // match found!
		}
		else
			return false;
	}


	@Override
	public void mappingInserted() {
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void mappingDeleted() {
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void mappingUpdated() {
		fireContentsChanged(obdaModel, 0, getSize());
	}
}
