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

import it.unibz.inf.ontop.protege.core.OBDAMappingListener;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class SynchronizedMappingListModel extends AbstractListModel<SQLPPTriplesMap> implements FilteredModel, OBDAMappingListener {

	private static final long serialVersionUID = 2317408823037931358L;
	
	private final OBDAModel obdaModel;
	private final List<TreeModelFilter<SQLPPTriplesMap>> filters = new LinkedList<>();

	public SynchronizedMappingListModel(OBDAModel obdaModel) {
		this.obdaModel = obdaModel;
		obdaModel.addMappingsListener(this);
	}

	public void setFocusedSource() {
		fireContentsChanged(this, 0, getSize());
	}

	@Override
	public void addFilter(TreeModelFilter<SQLPPTriplesMap> filter) {
		filters.add(filter);
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void addFilters(List<TreeModelFilter<SQLPPTriplesMap>> addFilters) {
		filters.addAll(addFilters);
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void removeFilter(TreeModelFilter<SQLPPTriplesMap> filter) {
		filters.remove(filter);
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void removeFilter(List<TreeModelFilter<SQLPPTriplesMap>> removeFilters) {
		filters.removeAll(removeFilters);
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void removeAllFilters() {
		filters.clear();
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public int getSize() {
		int filteredCount = 0;
		for (SQLPPTriplesMap mapping : obdaModel.getMapping()) {
			if (filters.stream().allMatch(f -> f.match(mapping)))
				filteredCount++;
		}
		return filteredCount;
	}

	@Override
	public SQLPPTriplesMap getElementAt(int index) {
		int filteredCount = -1;
		for (SQLPPTriplesMap mapping : obdaModel.getMapping()) {
			if (filters.stream().allMatch(f -> f.match(mapping)))
				filteredCount++;

			if (filteredCount == index)
				return mapping;
		}
		return null;
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
