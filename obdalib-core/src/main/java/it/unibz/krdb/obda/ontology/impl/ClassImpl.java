package it.unibz.krdb.obda.ontology.impl;

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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.OClass;

public class ClassImpl implements OClass {

	private static final long serialVersionUID = -4930755519806785384L;

	private final Predicate predicate;
	private final String string;
	private final boolean isNothing, isThing;

	public static final String owlThingIRI = "http://www.w3.org/2002/07/owl#Thing";
	public static final String owlNothingIRI  = "http://www.w3.org/2002/07/owl#Nothing";
	
    static final OClass owlThing = initialize(owlThingIRI); 
    static final OClass owlNothing = initialize(owlNothingIRI); 
    
    private static OClass initialize(String uri) {
    	final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
		Predicate prop = ofac.getClassPredicate(uri);
		return new ClassImpl(prop);  	
    }
	
	
	ClassImpl(Predicate p) {
		this.predicate = p;
		string = predicate.toString();
		isNothing = predicate.getName().equals(owlNothingIRI);
		isThing = predicate.getName().equals(owlThingIRI);
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public String getName() {
		return predicate.getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClassImpl)) {
			return false;
		}
		ClassImpl concept2 = (ClassImpl) obj;
		return (predicate.equals(concept2.getPredicate()));
	}
	
	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public String toString() {
		return string;
	}


	@Override
	public boolean isNothing() {
		return isNothing;
	}


	@Override
	public boolean isThing() {
		return isThing;
	}
}
