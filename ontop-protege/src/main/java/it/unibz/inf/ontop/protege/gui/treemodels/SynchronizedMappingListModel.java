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

import it.unibz.inf.ontop.model.SQLPPMappingAxiom;
import it.unibz.inf.ontop.protege.core.OBDAMappingListener;
import it.unibz.inf.ontop.protege.core.OBDAModel;

import javax.swing.*;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class SynchronizedMappingListModel extends AbstractListModel implements FilteredModel, OBDAMappingListener {

	private static final long serialVersionUID = 2317408823037931358L;
	
	private OBDAModel obdaModel;
	private URI focusedSource;
	private List<TreeModelFilter<SQLPPMappingAxiom>> filters;

	public SynchronizedMappingListModel(OBDAModel obdaModel) {
		this.obdaModel = obdaModel;
		obdaModel.addMappingsListener(this);
		filters = new LinkedList<TreeModelFilter<SQLPPMappingAxiom>>();
	}

	public void setFocusedSource(URI source) {
		focusedSource = source;
		fireContentsChanged(this, 0, getSize());
	}

	@Override
	public void addFilter(TreeModelFilter<SQLPPMappingAxiom> filter) {
		filters.add(filter);
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void addFilters(List<TreeModelFilter<SQLPPMappingAxiom>> filters) {
		this.filters.addAll(filters);
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void removeFilter(TreeModelFilter<SQLPPMappingAxiom> filter) {
		filters.remove(filter);
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void removeFilter(List<TreeModelFilter<SQLPPMappingAxiom>> filters) {
		this.filters.removeAll(filters);
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void removeAllFilters() {
		this.filters.clear();
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public int getSize() {
		if (focusedSource == null)
			return 0;

		List<SQLPPMappingAxiom> mappings = obdaModel.getMappings(focusedSource);
		int filteredCount = 0;
		for (SQLPPMappingAxiom mapping : mappings) {
			boolean passedAllFilters = true;
			for (TreeModelFilter<SQLPPMappingAxiom> filter : filters) {
				passedAllFilters = passedAllFilters && filter.match(mapping);
			}
			if (passedAllFilters)
				filteredCount += 1;
		}
		return filteredCount;
	}

	@Override
	public Object getElementAt(int index) {
		List<SQLPPMappingAxiom> mappings = obdaModel.getMappings(focusedSource);
		int filteredCount = -1;
		for (SQLPPMappingAxiom mapping : mappings) {
			boolean passedAllFilters = true;
			for (TreeModelFilter<SQLPPMappingAxiom> filter : filters) {
				passedAllFilters = passedAllFilters && filter.match(mapping);
			}
			if (passedAllFilters) {
				filteredCount += 1;
			}

			if (filteredCount == index)
				return mapping;
		}
		return null;
	}

	@Override
	public void mappingInserted(URI srcid) {
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void mappingDeleted(URI srcid) {
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void mappingUpdated(URI srcid) {
		fireContentsChanged(obdaModel, 0, getSize());
	}

	@Override
	public void currentSourceChanged(URI oldsrcid, URI newsrcid) {
		// NO-OP
	}

	@Override
	public void allMappingsRemoved() {
		fireContentsChanged(obdaModel, 0, getSize());
	}
}
