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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.SQLQueryImpl;
import it.unibz.krdb.obda.uri.UriTemplateHelper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.hp.hpl.jena.vocabulary.RDFS;

public class OBDAMappingTransformer {
	
	private ValueFactory vf;
	
	public OBDAMappingTransformer() {
		this.vf = new ValueFactoryImpl();
	}
	
	public Set<Statement> getStatements(OBDAMappingAxiom axiom) {
		Set<Statement> statements = new HashSet<Statement>();
		SQLQueryImpl squery = (SQLQueryImpl) axiom.getSourceQuery();
		CQIE tquery = (CQIE) axiom.getTargetQuery();
		
		int idx = 0;
		Random rand = new Random();
		idx = rand.nextInt(10000);
		
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
				Resource logicalTableNode = vf.createBNode("logicalTableNode" + idx);
				String tableName = sqlquery.substring(14);
				//tableName = trimApostrophes(tableName);
				statements.add(vf.createStatement(mainNode, R2RMLVocabulary.logicalTable, logicalTableNode));
				statements.add(vf.createStatement(logicalTableNode, R2RMLVocabulary.tableName, vf.createLiteral(tableName)));
//			} else if (sqlquery.contains("CHILD")) {
//				//join mapping
//				
			
		} else {
			//sqlquery -> general case
			statements.add(vf.createStatement(mainNode, R2RMLVocabulary.sqlQuery, vf.createLiteral(sqlquery)));
		}
		
		//get subject uri
		Resource subjectNode =  vf.createBNode("subjectMap" +idx);
		Function uriTemplate = (Function) tquery.getBody().get(0).getTerm(0); //URI("..{}..", , )
		String subjectURI =  UriTemplateHelper.getUriTemplateString(uriTemplate);
		//add subject Map to triples Map node
		statements.add(vf.createStatement(mainNode, R2RMLVocabulary.subjectMap, subjectNode));
		//add template subject
		statements.add(vf.createStatement(subjectNode, R2RMLVocabulary.template, vf.createURI(subjectURI)));
		//TODO: deal with column and termType
		
		
		//process target query
		for (Function func : tquery.getBody()) {
			Predicate pred = func.getFunctionSymbol();
			if (pred.isClass()) {
				//add class declaration to subject Map node
				statements.add(vf.createStatement(subjectNode, R2RMLVocabulary.classUri, vf.createURI(pred.getName())));
			} else {
				
				Resource predObjNode = vf.createBNode("predicateObjectMap"+ idx);
				//add predicateObjectMap to triples Map node
				statements.add(vf.createStatement(mainNode, R2RMLVocabulary.predicateObjectMap, predObjNode));
				//add predicate declaration to predObj node
				statements.add(vf.createStatement(predObjNode, R2RMLVocabulary.predicate, vf.createURI(pred.getName())));
				//add object declaration to predObj node
				//term 0 is always the subject, we are interested in term 1
				Term object = func.getTerm(1);
				Resource objNode = vf.createBNode("objectMap"+idx);
				statements.add(vf.createStatement(predObjNode, R2RMLVocabulary.objectMap, objNode));
				if (object instanceof Variable){
					statements.add(vf.createStatement(objNode, R2RMLVocabulary.column, vf.createLiteral(((Variable) object).getName())));
				} else if (object instanceof Function) {
					//check if uritemplate
					Predicate objectPred = ((Function) object).getFunctionSymbol();
					if (objectPred instanceof URITemplatePredicate) {
						String objectURI =  UriTemplateHelper.getUriTemplateString((Function)object);
						//add template object
						statements.add(vf.createStatement(objNode, R2RMLVocabulary.template, vf.createURI(objectURI)));
					}else if (objectPred instanceof DataTypePredicate) {
						statements.add(vf.createStatement(objNode, R2RMLVocabulary.constant, vf.createLiteral(((Function) object).getTerm(0).toString())));
						statements.add(vf.createStatement(objNode, R2RMLVocabulary.datatype, vf.createURI(objectPred.getName())));
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
