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
import it.unibz.krdb.obda.ontology.DescriptionBT;
import it.unibz.krdb.obda.ontology.OClass;

public class ClassImpl implements OClass {

	private static final long serialVersionUID = -4930755519806785384L;

	private final Predicate predicate;
	private final String name;
	private final boolean isNothing, isThing;

	public static final String owlThingIRI = "http://www.w3.org/2002/07/owl#Thing";
	public static final String owlNothingIRI  = "http://www.w3.org/2002/07/owl#Nothing";

   	private static final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
    static final OClass owlThing = new ClassImpl(owlThingIRI); 
    static final OClass owlNothing = new ClassImpl(owlNothingIRI); 
    	
	ClassImpl(String name) {
		this.predicate = ofac.getClassPredicate(name);
		this.name = name;
		this.isNothing = name.equals(owlNothingIRI);
		this.isThing = name.equals(owlThingIRI);
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof ClassImpl) {
			ClassImpl other = (ClassImpl) obj;
			return name.equals(other.name);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isBottom() {
		return isNothing;
	}

	@Override
	public boolean isTop() {
		return isThing;
	}
}
