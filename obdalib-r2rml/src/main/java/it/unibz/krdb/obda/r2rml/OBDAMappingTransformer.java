package it.unibz.krdb.obda.r2rml;

/*
 * #%L
 * ontop-obdalib-sesame
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

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.SQLQueryImpl;
import it.unibz.krdb.obda.utils.IDGenerator;
import it.unibz.krdb.obda.utils.URITemplates;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;

import eu.optique.api.mapping.LogicalTable;
import eu.optique.api.mapping.MappingFactory;
import eu.optique.api.mapping.ObjectMap;
import eu.optique.api.mapping.PredicateMap;
import eu.optique.api.mapping.PredicateObjectMap;
import eu.optique.api.mapping.R2RMLMappingManager;
import eu.optique.api.mapping.R2RMLMappingManagerFactory;
import eu.optique.api.mapping.SubjectMap;
import eu.optique.api.mapping.Template;
import eu.optique.api.mapping.TermMap.TermMapType;
import eu.optique.api.mapping.TriplesMap;
/**
 * Transform OBDA mappings in R2rml mappings
 * @author Sarah, Mindas, Timi, Guohui, Martin
 *
 */
public class OBDAMappingTransformer {
	
	private ValueFactory vf;
	private OWLOntology ontology;
	private Set<OWLObjectProperty> objectProperties;
	
	public OBDAMappingTransformer() {
		this.vf = new ValueFactoryImpl();
		
	}
//	public OBDAMappingTransformer(OWLOntology onto) {
//		this.vf = new ValueFactoryImpl();
//		//get the objectProperties in the ontology so that we can distinguish between object property and data property
//		objectProperties = onto.getObjectPropertiesInSignature();
//	}
	/**
	 * Get Sesame statements from OBDA mapping axiom
	 * @param axiom
	 * @param prefixmng
	 * @return
	 */
	public List<Statement> getStatements(OBDAMappingAxiom axiom, PrefixManager prefixmng) {
		List<Statement> statements = new ArrayList<Statement>();
		SQLQueryImpl squery = (SQLQueryImpl) axiom.getSourceQuery();
		CQIE tquery = (CQIE) axiom.getTargetQuery();
		
		String random_number = IDGenerator.getNextUniqueID("");
		
		//triplesMap node
		String mapping_id = axiom.getId();
		if (!mapping_id.startsWith("http://"))
			mapping_id = "http://example.org/" + mapping_id;
		Resource mainNode = vf.createURI(mapping_id);
		statements.add(vf.createStatement(mainNode, vf.createURI(OBDAVocabulary.RDF_TYPE), R2RMLVocabulary.TriplesMap));
		
		//creating logical table node
		Resource logicalTableNode = vf.createBNode("logicalTable"+ random_number);
		
		//process source query
		String sqlquery = squery.getSQLQuery();
		OBDAQueryModifiers modifiers = squery.getQueryModifiers();
		if (sqlquery.startsWith("SELECT * FROM") &&
			 !sqlquery.contains("WHERE") && !sqlquery.contains(",")) {
				//tableName -> need small parser
				String tableName = sqlquery.substring(14);
				tableName = trimApostrophes(tableName);
				statements.add(vf.createStatement(mainNode, R2RMLVocabulary.logicalTable, logicalTableNode));
				
				if(tableName.startsWith("\"") && tableName.endsWith("\"")){
					tableName = tableName.substring(1, tableName.length() - 1);
				}
				
				statements.add(vf.createStatement(logicalTableNode, R2RMLVocabulary.tableName, vf.createLiteral(tableName)));
		} else {
			//sqlquery -> general case
			//creating triple main-node -- logical table
			statements.add(vf.createStatement(mainNode, R2RMLVocabulary.logicalTable, logicalTableNode));

			//the node is a view
			statements.add(vf.createStatement(logicalTableNode, vf.createURI(OBDAVocabulary.RDF_TYPE),  R2RMLVocabulary.r2rmlView));

			//this is the SQL in the logical table
			statements.add(vf.createStatement(logicalTableNode, R2RMLVocabulary.sqlQuery, vf.createLiteral(sqlquery)));
		}
		
		//get subject uri
		Resource subjectNode =  vf.createBNode("subjectMap" +random_number);
		
		//add subject Map to triples Map node
		statements.add(vf.createStatement(mainNode, R2RMLVocabulary.subjectMap, subjectNode));
		statements.add(vf.createStatement(subjectNode, vf.createURI(OBDAVocabulary.RDF_TYPE),   R2RMLVocabulary.termMap));		

		//Now we add the template!!
		Function uriTemplate = (Function) tquery.getBody().get(0).getTerm(0); //URI("..{}..", , )
		String subjectTemplate =  URITemplates.getUriTemplateString(uriTemplate, prefixmng);
		
		//add template subject
		statements.add(vf.createStatement(subjectNode, R2RMLVocabulary.template, vf.createLiteral(subjectTemplate)));
		
		
		
		//process target query
		for (Function func : tquery.getBody()) {
			random_number = IDGenerator.getNextUniqueID("");
			Predicate pred = func.getFunctionSymbol();
			

			String predName = pred.getName();
			URI predUri = null; String predURIString ="";
			
			if (pred.equals(OBDAVocabulary.QUEST_TRIPLE_PRED))
			{
				//triple
				Function predf = (Function)func.getTerm(1);
				if (predf.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI))
				{
					if (predf.getTerms().size() == 1) //fixed string
					{
						pred = OBDADataFactoryImpl.getInstance().getPredicate(((ValueConstant)(predf.getTerm(0))).getValue(), 1);
						predUri = vf.createURI(pred.getName());
					}
					else
					{
						//custom predicate
						predURIString = URITemplates.getUriTemplateString(predf, prefixmng);
						predUri = vf.createURI(predURIString);
					}
				}
				
			} else {
				predUri = vf.createURI(predName);
			}
			predURIString = predUri.stringValue();
			
			IRI propname = IRI.create(predURIString);
			OWLDataFactory factory =  OWLManager.getOWLDataFactory();
			OWLObjectProperty prop = factory.getOWLObjectProperty(propname);
		
			if (pred.isClass() && !predURIString.equals(OBDAVocabulary.RDF_TYPE)) {
				// The term is actually a SubjectMap (class)
			//	statements.add(vf.createStatement(nod_subject, vf.createURI(OBDAVocabulary.RDF_TYPE),   R2RMLVocabulary.subjectMapClass));		
				
				//add class declaration to subject Map node
				statements.add(vf.createStatement(subjectNode, R2RMLVocabulary.classUri, predUri));
				
			} else {
				Resource predObjNode = vf.createBNode("predicateObjectMap"+ random_number);
				
				//add predicateObjectMap to triples Map node
				Statement triple_main_predicate = vf.createStatement(mainNode, R2RMLVocabulary.predicateObjectMap, predObjNode);
				statements.add(triple_main_predicate);
				
				if (!predName.equals(OBDAVocabulary.QUEST_TRIPLE_STR)) {
					//add predicate declaration to predObj node
					Statement triple_predicateObject_predicate_uri = vf.createStatement(predObjNode, R2RMLVocabulary.predicate, predUri);
					statements.add(triple_predicateObject_predicate_uri);
				}
				else {
					//add predicate template declaration
					Resource predMapNode = vf.createBNode("predicateMap"+ random_number);
					Statement triple_predicateObject_predicate_map = vf.createStatement(predObjNode, R2RMLVocabulary.predicateMap, predMapNode);
					Statement triple_predicateTemplate = vf.createStatement(predMapNode, R2RMLVocabulary.template, vf.createLiteral(predURIString));
					statements.add(triple_predicateObject_predicate_map);
					statements.add(triple_predicateTemplate);
					
				}
				
				//add object declaration to predObj node
				//term 0 is always the subject, we are interested in term 1
				Term object = func.getTerm(1);
				
				
				
				Resource objNode = vf.createBNode("objectMap"+random_number);
				
				Statement triple_prop_obj = vf.createStatement(predObjNode, R2RMLVocabulary.objectMap, objNode);
				statements.add(triple_prop_obj);

				if (object instanceof Variable){
					statements.add(vf.createStatement(objNode, R2RMLVocabulary.column, vf.createLiteral(((Variable) object).getName())));
				} else if (object instanceof Function) {
					//check if uritemplate
					Predicate objectPred = ((Function) object).getFunctionSymbol();
					if (objectPred instanceof URITemplatePredicate) {
						String objectURI =  URITemplates.getUriTemplateString((Function)object, prefixmng);
						//add template object
						statements.add(vf.createStatement(objNode, R2RMLVocabulary.template, vf.createLiteral(objectURI)));
					}else if (objectPred instanceof DataTypePredicate) {
						Term objectTerm = ((Function) object).getTerm(0);

						if (objectTerm instanceof Variable) {
							//Now we add the template!!
							String objectTemplate =  "{"+ ((Variable) objectTerm).getName() +"}" ;
							//add template subject
							statements.add(vf.createStatement(objNode, R2RMLVocabulary.template, vf.createLiteral(objectTemplate)));
						} else if (objectTerm instanceof Constant) {
							statements.add(vf.createStatement(objNode, R2RMLVocabulary.constant, vf.createLiteral(((Constant) objectTerm).getValue())));
						}
					}
				} else {
					System.out.println("FOUND UNKNOWN: "+object.toString());
				}
			}
			
		}
		
		return statements;
	}
	
	private String trimApostrophes(String input) {
		input = input.trim();
		while (input.startsWith("\""))
			input = input.substring(1);
		while (input.endsWith("\""))
			input = input.substring(0, input.length()-1);
		return input;
	}

	/**
	 * Get R2RML TriplesMap from OBDA mapping axiom
	 * @param axiom
	 * @param prefixmng
	 * @return
	 */
	public TriplesMap getTriplesMap(OBDAMappingAxiom axiom,
			PrefixManager prefixmng) {
		
		SQLQueryImpl squery = (SQLQueryImpl) axiom.getSourceQuery();
		CQIE tquery = (CQIE) axiom.getTargetQuery();
		
		String random_number = IDGenerator.getNextUniqueID("");
		
		//triplesMap node
		String mapping_id = axiom.getId();
		if (!mapping_id.startsWith("http://"))
			mapping_id = "http://example.org/" + mapping_id;
		Resource mainNode = vf.createURI(mapping_id);

		R2RMLMappingManager mm = R2RMLMappingManagerFactory.getSesameMappingManager();
		MappingFactory mfact = mm.getMappingFactory();
		
		//Table
		LogicalTable lt = mfact.createR2RMLView(squery.getSQLQuery());
		
		//SubjectMap
		Function uriTemplate = (Function) tquery.getBody().get(0).getTerm(0); //URI("..{}..", , )
		String subjectTemplate =  URITemplates.getUriTemplateString(uriTemplate, prefixmng);		
		Template templs = mfact.createTemplate(subjectTemplate);
		SubjectMap sm = mfact.createSubjectMap(templs);
		
		TriplesMap tm = mfact.createTriplesMap(lt, sm);
		
		//process target query
		for (Function func : tquery.getBody()) {
			random_number = IDGenerator.getNextUniqueID("");
			Predicate pred = func.getFunctionSymbol();
			String predName = pred.getName();
			URI predUri = null; String predURIString ="";
			
			if (pred.equals(OBDAVocabulary.QUEST_TRIPLE_PRED))
			{
				//triple
				Function predf = (Function)func.getTerm(1);
				if (predf.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI))
				{
					if (predf.getTerms().size() == 1) //fixed string
					{
						pred = OBDADataFactoryImpl.getInstance().getPredicate(((ValueConstant)(predf.getTerm(0))).getValue(), 1);
						predUri = vf.createURI(pred.getName());
					}
					else
					{
						//custom predicate
						predURIString = URITemplates.getUriTemplateString(predf, prefixmng);
						predUri = vf.createURI(predURIString);
					}
				}
				
			} else {
				predUri = vf.createURI(predName);
			}
			predURIString = predUri.stringValue();
			
			IRI propname = IRI.create(predURIString);
			OWLDataFactory factory =  OWLManager.getOWLDataFactory();
			OWLObjectProperty prop = factory.getOWLObjectProperty(propname);
			
			if (pred.isClass() && !predURIString.equals(OBDAVocabulary.RDF_TYPE)) {
				// The term is actually a SubjectMap (class)
				//add class declaration to subject Map node
				sm.addClass(predUri);
				
			} else {
				
				PredicateMap predM = mfact.createPredicateMap(TermMapType.CONSTANT_VALUED, "predicateObjectMap"+ random_number);
				ObjectMap obm = null; PredicateObjectMap pom = null;
				if (!predName.equals(OBDAVocabulary.QUEST_TRIPLE_STR)) {
					predM.setConstant(predURIString);
				}
				else {
					//add predicate template declaration
					Template templo = mfact.createTemplate(predURIString);
					obm = mfact.createObjectMap(templo);
					pom = mfact.createPredicateObjectMap(predM, obm);
					tm.addPredicateObjectMap(pom);
				}

				//add object declaration to predObj node
				//term 0 is always the subject, we are interested in term 1
				Term object = func.getTerm(1);

				if (object instanceof Variable){
					if(ontology!= null && objectProperties.contains(prop)){
						obm = mfact.createObjectMap(TermMapType.COLUMN_VALUED, vf.createLiteral(((Variable) object).getName()).stringValue());
						obm.setTermType(R2RMLVocabulary.iri);
					}
					else{
					obm = mfact.createObjectMap(TermMapType.COLUMN_VALUED, vf.createLiteral(((Variable) object).getName()).stringValue());
					}
					//we add the predicate object map in case of literal
					pom = mfact.createPredicateObjectMap(predM, obm);
					tm.addPredicateObjectMap(pom);
				} else if (object instanceof Function) {
					//check if uritemplate
					Predicate objectPred = ((Function) object).getFunctionSymbol();
					if (objectPred instanceof URITemplatePredicate) {
						String objectURI =  URITemplates.getUriTemplateString((Function)object, prefixmng);
						//add template object
						//statements.add(vf.createStatement(objNode, R2RMLVocabulary.template, vf.createLiteral(objectURI)));
						//obm.setTemplate(mfact.createTemplate(objectURI));
						obm = mfact.createObjectMap(mfact.createTemplate(objectURI));
					}else if (objectPred instanceof DataTypePredicate) {
						Term objectTerm = ((Function) object).getTerm(0);

						if (objectTerm instanceof Variable) {
							//Now we add the template!!
							String objectTemplate =  "{"+ ((Variable) objectTerm).getName() +"}" ;
							//add template subject
							//statements.add(vf.createStatement(objNode, R2RMLVocabulary.template, vf.createLiteral(objectTemplate)));
							//obm.setTemplate(mfact.createTemplate(objectTemplate));
							obm = mfact.createObjectMap(mfact.createTemplate(objectTemplate));
							obm.setTermType(R2RMLVocabulary.literal);
							
							
							
						} else if (objectTerm instanceof Constant) {
							//statements.add(vf.createStatement(objNode, R2RMLVocabulary.constant, vf.createLiteral(((Constant) objectTerm).getValue())));
							//obm.setConstant(vf.createLiteral(((Constant) objectTerm).getValue()).stringValue());
							obm = mfact.createObjectMap(TermMapType.CONSTANT_VALUED, vf.createLiteral(((Constant) objectTerm).getValue()).stringValue());
						}
						
					}
					pom = mfact.createPredicateObjectMap(predM, obm);
					tm.addPredicateObjectMap(pom);
				} else {
					System.out.println("FOUND UNKNOWN: "+object.toString());
				}
			}
			
		}

		return tm;
	}
	
	public OWLOntology getOntology() {
		return ontology;
	}
	
	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
		objectProperties = ontology.getObjectPropertiesInSignature();
	}
	

}
