/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 * 
 * @author Timea Bagosi
 */
package it.unibz.krdb.obda.sesame.r2rml;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

//import com.hp.hpl.jena.vocabulary.RDFS;

public class OBDAMappingTransformer {
	
	private ValueFactory vf;
	
	public OBDAMappingTransformer() {
		this.vf = new ValueFactoryImpl();
	}
	
	public List<Statement> getStatements(OBDAMappingAxiom axiom, PrefixManager prefixmng) {
		List<Statement> statements = new ArrayList<Statement>();
		SQLQueryImpl squery = (SQLQueryImpl) axiom.getSourceQuery();
		CQIE tquery = (CQIE) axiom.getTargetQuery();
		
		//int random_number = 0;
		Random rand = new Random();
		//random_number = rand.nextInt(10000);
		
		String random_number = IDGenerator.getNextUniqueID("");
		
		//triplesMap node
		String mapping_id = axiom.getId();
		if (!mapping_id.startsWith("http://"))
			mapping_id = "http://example.org/" + mapping_id;
		Resource mainNode = vf.createURI(mapping_id);
		statements.add(vf.createStatement(mainNode, vf.createURI(OBDAVocabulary.RDF_TYPE), R2RMLVocabulary.TriplesMap));
		
		//process source query
		String sqlquery = squery.getSQLQuery();
		OBDAQueryModifiers modifiers = squery.getQueryModifiers();
		if (sqlquery.startsWith("SELECT * FROM") &&
			 !sqlquery.contains("WHERE") && !sqlquery.contains(",")) {
				//tableName -> need small parser
				String str_nod_logtable = "http://example.org/" + random_number +"/"+ "MyLogTab";
				Resource logicalTableNode = vf.createURI(str_nod_logtable);
				String tableName = sqlquery.substring(14);
				tableName = trimApostrophes(tableName);
				statements.add(vf.createStatement(mainNode, R2RMLVocabulary.logicalTable, logicalTableNode));
				statements.add(vf.createStatement(logicalTableNode, R2RMLVocabulary.tableName, vf.createLiteral(tableName)));
//			} else if (sqlquery.contains("CHILD")) {
//				//join mapping
//				
			
		} else {
			//sqlquery -> general case
			
			//creating logical table node
			String str_nod_log_table = "http://example.org/" + random_number +"/"+ "MylogicalTable";
			Resource nod_log_table =  vf.createURI(str_nod_log_table);
			
			//creating triple main-node -- logical table
			statements.add(vf.createStatement(mainNode, R2RMLVocabulary.logicalTable, nod_log_table));

			//the node is a view
			statements.add(vf.createStatement(nod_log_table,vf.createURI(OBDAVocabulary.RDF_TYPE),  R2RMLVocabulary.r2rmlView));

			//this is the SQL in the logical table
			statements.add(vf.createStatement(nod_log_table, R2RMLVocabulary.sqlQuery, vf.createLiteral(sqlquery)));
		}
		
		//get subject uri
		//Resource subjectNode =  vf.createBNode("subjectMap" +random_number);
		
		//We define the subject of the mapping
		String str_nod_subject = "http://example.org/" + random_number +"/"+ "MySubjectMap";
		Resource nod_subject =  vf.createURI(str_nod_subject);
	
		//add subject Map to triples Map node
		statements.add(vf.createStatement(mainNode, R2RMLVocabulary.subjectMap, nod_subject));
		statements.add(vf.createStatement(nod_subject, vf.createURI(OBDAVocabulary.RDF_TYPE),   R2RMLVocabulary.termMap));		

		//Now we add the template!!
		Function uriTemplate = (Function) tquery.getBody().get(0).getTerm(0); //URI("..{}..", , )
		String subjectTemplate =  URITemplates.getUriTemplateString(uriTemplate, prefixmng);
		
		//add template subject
		statements.add(vf.createStatement(nod_subject, R2RMLVocabulary.template, vf.createLiteral(subjectTemplate)));
		//TODO: deal with column and termType

		
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
			
		
			if (pred.isClass() && !predURIString.equals(OBDAVocabulary.RDF_TYPE)) {
				// The term is actually a SubjectMap (class)
			//	statements.add(vf.createStatement(nod_subject, vf.createURI(OBDAVocabulary.RDF_TYPE),   R2RMLVocabulary.subjectMapClass));		
				
				//add class declaration to subject Map node
				statements.add(vf.createStatement(nod_subject, R2RMLVocabulary.classUri, predUri));
				
			} else {
				String str_nod_prop = "http://example.org/" + random_number +"/"+ "MyPropMap";
				//Resource predObjNode = vf.createBNode("predicateObjectMap"+ random_number);
				Resource predObjNode = vf.createURI(str_nod_prop);
				
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
					String str_nod_pred = "http://example.org/" + random_number +"/"+ "MyPredMap";
					Resource predMapNode = vf.createURI(str_nod_pred);
					Statement triple_predicateObject_predicate_map = vf.createStatement(predObjNode, R2RMLVocabulary.predicateMap, predMapNode);
					Statement triple_predicateTemplate = vf.createStatement(predMapNode, R2RMLVocabulary.template, vf.createLiteral(predURIString));
					statements.add(triple_predicateObject_predicate_map);
					statements.add(triple_predicateTemplate);
					
				}
				
				//add object declaration to predObj node
				//term 0 is always the subject, we are interested in term 1
				Term object = func.getTerm(1);
				
				//Resource objNode = vf.createBNode("objectMap"+random_number);
				
				String str_nod_obj = "http://example.org/" + random_number +"/"+ "MyObjectMap";
				//Resource predObjNode = vf.createBNode("predicateObjectMap"+ random_number);
				Resource objNode = vf.createURI(str_nod_obj);
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
							//TODO: deal with column and termType
							
						} else if (objectTerm instanceof Constant) {
							statements.add(vf.createStatement(objNode, R2RMLVocabulary.constant, vf.createLiteral(((Constant) objectTerm).getValue())));
						}
					//	statements.add(vf.createStatement(objNode, R2RMLVocabulary.datatype, vf.createURI(objectPred.getName())));
						//statements.add(vf.createStatement(objNode, R2RMLVocabulary.termType, R2RMLVocabulary.literal));
						
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
	

}
