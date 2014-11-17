package it.unibz.krdb.obda.owlrefplatform.core.abox;

/*
 * #%L
 * ontop-reformulation-core
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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.DatatypeFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;

import java.util.LinkedList;

import org.openrdf.model.vocabulary.XMLSchema;

public class ABoxToFactRuleConverter {
	private static final OBDADataFactory factory = OBDADataFactoryImpl.getInstance();

	public static CQIE getRule(ClassAssertion ca) {
		CQIE rule = null;
		ObjectConstant c = ca.getIndividual();
		Predicate p = ca.getConcept().getPredicate();
		Function head = factory.getFunction(p, 
						factory.getUriTemplate(factory.getConstantLiteral(c.getName())));
		rule = factory.getCQIE(head, new LinkedList<Function>());
		return rule;
	}

	public static CQIE getRule(DataPropertyAssertion da) {
		// WE IGNORE DATA PROPERTY ASSERTIONS UNTIL THE NEXT RELEASE
		return null;		
	}
	
	public static CQIE getRule(ObjectPropertyAssertion pa) {
		ObjectConstant s = pa.getSubject();
		ObjectConstant o = pa.getObject();
		Predicate p = pa.getProperty().getPredicate();
		Function head = factory.getFunction(p, 
						factory.getUriTemplate(factory.getConstantLiteral(s.getName())), 
						factory.getUriTemplate(factory.getConstantLiteral(o.getName())));
		CQIE rule = factory.getCQIE(head, new LinkedList<Function>());
		return rule;
		
//		else if (assertion instanceof DataPropertyAssertion) {
			/* 
			 * We ignore these for the moment until next release.
			 */
			
//			DataPropertyAssertion ca = (DataPropertyAssertion) assertion;
//			ObjectConstant s = ca.getObject();
//			ValueConstant o = ca.getValue();
//			String typeURI = getURIType(o.getType());
//			Predicate p = ca.getPredicate();
//			Predicate urifuction = factory.getUriTemplatePredicate(1);
//			head = factory.getFunction(p, factory.getFunction(urifuction, s), factory.getFunction(factory.getPredicate(typeURI,1), o));
//			rule = factory.getCQIE(head, new LinkedList<Function>());
//		} 	
	}

}
