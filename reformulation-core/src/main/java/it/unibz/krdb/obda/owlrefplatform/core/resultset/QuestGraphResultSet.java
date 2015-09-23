package it.unibz.krdb.obda.owlrefplatform.core.resultset;

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

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.AssertionFactory;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.InconsistentOntologyException;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.AssertionFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SesameConstructTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;



public class QuestGraphResultSet implements GraphResultSet {

	private List<List<Assertion>> results = new ArrayList<List<Assertion>>();

	private TupleResultSet tupleResultSet;


	private SesameConstructTemplate sesameTemplate;

	List <ExtensionElem> extList = null;
	
	HashMap <String, ValueExpr> extMap = null;
	
	//store results in case of describe queries
	private boolean storeResults = false;

	private OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private AssertionFactory ofac = AssertionFactoryImpl.getInstance();

	public QuestGraphResultSet(TupleResultSet results, SesameConstructTemplate template,
			boolean storeResult) throws OBDAException {
		this.tupleResultSet = results;
		this.sesameTemplate = template;
		this.storeResults = storeResult;
		processResultSet(tupleResultSet, sesameTemplate);
	}

	@Override
	public TupleResultSet getTupleResultSet() {
		return tupleResultSet;
	}

	private void processResultSet(TupleResultSet resSet, SesameConstructTemplate template)
			throws OBDAException {
		if (storeResults) {
			//process current result set into local buffer, 
			//since additional results will be collected
			while (resSet.nextRow()) {
				this.results.add(processResults(resSet, template));
			}
		}
	}
	
	@Override
	public void addNewResultSet(List<Assertion> result)
	{
		results.add(result);
	}

//	@Override
//	public Template getTemplate() {
//		return template;
//	}

	
	/**
	 * The method to actually process the current result set Row.
	 * Construct a list of assertions from the current result set row.
	 * In case of describe it is called to process and store all 
	 * the results from a resultset.
	 * In case of construct it is called upon next, to process
	 * the only current result set.
	 */
	
	private List<Assertion> processResults(TupleResultSet result,
			SesameConstructTemplate template) throws OBDAException {
		List<Assertion> tripleAssertions = new ArrayList<Assertion>();
		List<ProjectionElemList> peLists = template.getProjectionElemList();
		
		Extension ex = template.getExtension();
		if (ex != null) 
			{
				extList = ex.getElements();
				HashMap <String, ValueExpr> newExtMap = new HashMap<String, ValueExpr>();
				for (int i = 0; i < extList.size(); i++) {
					newExtMap.put(extList.get(i).getName(), extList.get(i).getExpr());
				}
				extMap = newExtMap;
			}
		for (ProjectionElemList peList : peLists) {
		int size = peList.getElements().size();
		
		for (int i = 0; i < size / 3; i++) {
			
			ObjectConstant subjectConstant = (ObjectConstant) getConstant(peList.getElements().get(i*3), result);
			Constant predicateConstant = getConstant(peList.getElements().get(i*3+1), result);
			Constant objectConstant = getConstant(peList.getElements().get(i*3+2), result);

			// Determines the type of assertion
			String predicateName = predicateConstant.getValue();
			Assertion assertion;
			try {
				if (predicateName.equals(OBDAVocabulary.RDF_TYPE)) {
					assertion = ofac.createClassAssertion(objectConstant.getValue(), subjectConstant);
				} 
				else {
					if ((objectConstant instanceof URIConstant) || (objectConstant instanceof BNode)) 
						assertion = ofac.createObjectPropertyAssertion(predicateName, 
								subjectConstant, (ObjectConstant) objectConstant);
					else 
						assertion = ofac.createDataPropertyAssertion(predicateName, 
									subjectConstant, (ValueConstant) objectConstant);
				} 
				if (assertion != null)
					tripleAssertions.add(assertion);
			}
			catch (InconsistentOntologyException e) {
				throw new RuntimeException("InconsistentOntologyException: " + 
							predicateName + " " + subjectConstant + " " + objectConstant);
			}
		}
		}
		return tripleAssertions;
	}
	
	@Override
	public boolean hasNext() throws OBDAException {
		//in case of describe, we return the collected results list information
		if (storeResults) {
			return results.size() != 0;
		} else {
			//in case of construct advance the result set cursor on hasNext
			return tupleResultSet.nextRow();
		}
	}

	@Override
	public List<Assertion> next() throws OBDAException {
		//if we collect results, then remove and return the next one in the list
		if (results.size() > 0) {
			return results.remove(0);
		} else {
			//otherwise we need to process the unstored result
			return processResults(tupleResultSet, sesameTemplate);
		}
	}

	private Constant getConstant(ProjectionElem node, TupleResultSet resSet)
			throws OBDAException {
		Constant constant = null;
		String node_name = node.getSourceName();
		ValueExpr ve = null;
		
		if (extMap!= null) {
			ve = extMap.get(node_name);
			if (ve!=null && ve instanceof Var)
				throw new RuntimeException ("Invalid query. Found unbound variable: "+ve);
		}
		
		if (node_name.charAt(0) == '-') {
			org.openrdf.query.algebra.ValueConstant vc = (org.openrdf.query.algebra.ValueConstant) ve;
			 if (vc.getValue() instanceof URIImpl) {
				 constant = dfac.getConstantURI(vc.getValue().stringValue());
			 } else if (vc.getValue() instanceof LiteralImpl) {
				 constant = dfac.getConstantLiteral(vc.getValue().stringValue());
			 } else {
				 constant = dfac.getConstantBNode(vc.getValue().stringValue());
			 }
		} else {
			constant = resSet.getConstant(node_name);
		}
		return constant;
	}
	
	@Override
	public void close() throws OBDAException {
		tupleResultSet.close();
	}

}
