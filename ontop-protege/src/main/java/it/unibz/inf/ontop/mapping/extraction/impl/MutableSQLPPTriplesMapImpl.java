package it.unibz.inf.ontop.mapping.extraction.impl;

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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.mapping.extraction.MutableSQLPPTriplesMap;
import it.unibz.inf.ontop.mapping.extraction.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 * TODO: split into two classes (one mutable, one immutable)
 */
public class MutableSQLPPTriplesMapImpl implements MutableSQLPPTriplesMap {

	private static final long serialVersionUID = 5512895151633505075L;

	private String id;
	private OBDASQLQuery sourceQuery;
	private List<Function> mutableTargetAtoms;
	private ImmutableList<ImmutableFunctionalTerm> immutableTargetAtoms;

	public MutableSQLPPTriplesMapImpl(String id, OBDASQLQuery sourceQuery,
									  List<Function> mutableTargetAtoms) {
		this.id = id;
		setSourceQuery(sourceQuery);
		setTargetQuery(mutableTargetAtoms);
	}

	public MutableSQLPPTriplesMapImpl(OBDASQLQuery sourceQuery,
                                      List<Function> mutableTargetAtoms) {
		this(IDGenerator.getNextUniqueID("MAPID-"), sourceQuery, mutableTargetAtoms);
	}

	@Override
	public void setSourceQuery(OBDASQLQuery query) {
		this.sourceQuery = query;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public void setTargetQuery(List<Function> mutableTargetAtoms) {
		this.mutableTargetAtoms = mutableTargetAtoms;
		this.immutableTargetAtoms = mutableTargetAtoms.stream()
				.map(DATA_FACTORY::getImmutableFunctionalTerm)
				.collect(ImmutableCollectors.toList());
	}

	@Override
	public OBDASQLQuery getSourceQuery() {
		return sourceQuery;
	}

	@Override
	public List<Function> getTargetQuery() {
		return mutableTargetAtoms;
	}

	@Override
	public it.unibz.inf.ontop.mapping.extraction.MutableSQLPPTriplesMap clone() {
		List<Function> newbody = new ArrayList<>(mutableTargetAtoms.size());
		for (Function f : mutableTargetAtoms)
			newbody.add((Function)f.clone());

		return new MutableSQLPPTriplesMapImpl(this.getId(), sourceQuery.clone(), newbody);
	}
	
	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer();
		bf.append(sourceQuery.toString());
		bf.append(" ==> ");
		bf.append(mutableTargetAtoms.toString());
		return bf.toString();
	}

	@Override
	public ImmutableList<ImmutableFunctionalTerm> getTargetAtoms() {
		return immutableTargetAtoms;
	}

	@Override
	public PPMappingAssertionProvenance getProvenance(ImmutableFunctionalTerm atom) {
		throw new RuntimeException("TODO: implement getProvenanceMap()");
	}
}
