package it.unibz.inf.ontop.protege.gui;

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

import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.apache.commons.rdf.api.IRI;

public class PredicateItem {

	public enum PredicateType {
		DATA_PROPERTY,
		OBJECT_PROPERTY,
		CLASS
	}
	
	private IRI iri;
	private final PredicateType predicateType;
	private PrefixManager prefixManager;
	
	public PredicateItem(IRI iri, PredicateType predicateType, PrefixManager pm) {
		this.iri = iri;
		this.predicateType = predicateType;
		prefixManager = pm;
	}
	
	/**
	 * Obtains the short name from the full predicate URI. The method will omit the
	 * default prefix ":".
	 */
	public String getQualifiedName() {
		if (prefixManager != null) {
			String shortName = prefixManager.getShortForm(getFullName());
			if (shortName.startsWith(":")) {
				return shortName.substring(1, shortName.length());
			} else {
				return shortName;
			}
		} else {
			return getFullName();
		}
	}
	
	public IRI getSource() {
		return iri;
	}
	
	public String getFullName() {
		return iri.getIRIString();
	}
	
	public boolean isDataPropertyPredicate() {
		return predicateType == PredicateType.DATA_PROPERTY;
	}
	
	public boolean isObjectPropertyPredicate() {
		return predicateType == PredicateType.OBJECT_PROPERTY;
	}
	
	public PrefixManager getPrefixManager() {
		return prefixManager;
	}
	
	@Override
	public String toString() {
		return getQualifiedName();
	}
}
