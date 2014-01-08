package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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
 
import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.api.VisitedQuery;
import it.unibz.krdb.sql.api.ProjectionJSQL;
import it.unibz.krdb.sql.api.SelectionJSQL;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author xiao
 *
 */
public class MetaMappingExpander {

	Logger log = LoggerFactory.getLogger(this.getClass());
	
	private Connection connection;
	private SQLQueryTranslator translator;
	private List<OBDAMappingAxiom> expandedMappings;
	private OBDADataFactory dfac;

	/**
	 * TODO
	 * 
	 * @param connection
	 * @param metadata
	 */
	public MetaMappingExpander(Connection connection) {
		this.connection = connection;
		translator = new SQLQueryTranslator();
		expandedMappings = new ArrayList<OBDAMappingAxiom>();
		dfac = OBDADataFactoryImpl.getInstance();
	}

	/**
	 * this method expand the input mappings, which may include meta mappings, to the concrete mappings
	 * 
	 * @param mappings
	 * 		a list of mappings, which may include meta mappings
	 * @return
	 * 		expanded normal mappings
	 */
	private List<OBDAMappingAxiom> expand(ArrayList<OBDAMappingAxiom> mappings) {
		
		for (OBDAMappingAxiom mapping : mappings) {

			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			List<Function> body = targetQuery.getBody();
			Function bodyAtom = targetQuery.getBody().get(0);

			OBDASQLQuery sourceQuery = (OBDASQLQuery)mapping.getSourceQuery();

			Function firstBodyAtom = body.get(0);
			
			Predicate pred = firstBodyAtom.getFunctionSymbol();
			if (!pred.equals(OBDAVocabulary.QUEST_TRIPLE_PRED)){
				/**
				 * for normal mappings, we do not need to expand it.
				 */
				expandedMappings.add(mapping);
				
			} else {
				
				int arity;
				
				Term term1 = bodyAtom.getTerm(1);
				
				// variables are in the position of object
				if (isURIRDFType(term1)){
					arity = 1;
				} else {
				// variables are in the position of predicate
					arity = 2;
				}
				
				List<Variable> varsInTemplate = getVariablesInTemplate(bodyAtom, arity);
				
				if (varsInTemplate.isEmpty()){
					throw new IllegalArgumentException("No Variables could be found for this metamapping. Check that the variable in the metamapping is enclosed in a URI, for instance http://.../{var}");
				}
				
				// Construct the SQL query tree from the source query
				VisitedQuery sourceQueryParsed = translator.constructParser(sourceQuery.toString());
				
				ProjectionJSQL distinctParamsProjection = new ProjectionJSQL();
				
				distinctParamsProjection.setType(ProjectionJSQL.SELECT_DISTINCT);
				
				
				ArrayList<SelectExpressionItem> columnList = null;
				try {
					columnList = (ArrayList<SelectExpressionItem>) sourceQueryParsed.getProjection().getColumnList();
				} catch (JSQLParserException e2) {
					
					e2.printStackTrace();
				}
				
				List<SelectExpressionItem> columnsForTemplate = getColumnsForTemplate(varsInTemplate, columnList);
				
				distinctParamsProjection.addAll(columnsForTemplate);
				
				/**
				 * The query for params is almost the same with the original source query, except that
				 * we only need to distinct project the columns needed for the template expansion 
				 */
				
				VisitedQuery distinctParsedQuery = null;
				try {
					distinctParsedQuery = new VisitedQuery(sourceQueryParsed.getStatement());
					
				} catch (JSQLParserException e1) {
					
					e1.printStackTrace();
				}

				distinctParsedQuery.setProjection(distinctParamsProjection);
				
				String distinctParamsSQL = distinctParsedQuery.toString();
				List<List<String>> paramsForClassTemplate = new ArrayList<List<String>>();
				
				
				Statement st;
				try {
					st = connection.createStatement();
					ResultSet rs = st.executeQuery(distinctParamsSQL);
					while(rs.next()){
						ArrayList<String> params = new ArrayList<String>(varsInTemplate.size());
						for(int i = 1 ; i <= varsInTemplate.size(); i++){
							 params.add(rs.getString(i));
						}
						paramsForClassTemplate.add(params);
						
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				List<SelectExpressionItem>  columnsForValues = new ArrayList<SelectExpressionItem>(columnList);
				columnsForValues.removeAll(columnsForTemplate);
				
				String id = mapping.getId();
				
				for(List<String> params : paramsForClassTemplate) {
					String newId = IDGenerator.getNextUniqueID(id + "#");
					OBDARDBMappingAxiom newMapping = instantiateMapping(newId, targetQuery,
							bodyAtom, sourceQueryParsed, columnsForTemplate,
							columnsForValues, params, arity);
										
					expandedMappings.add(newMapping);	
					
					log.debug("Expanded Mapping: {}", newMapping);
				}
				
			}

		}
		
	
		return expandedMappings;
	}

	/**
	 * check if the term is {@code URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")}
	 * @param term
	 * @return
	 */
	private boolean isURIRDFType(Term term) {
		boolean result = true;
		if(term instanceof Function){
			Function func = (Function) term;
			if (func.getArity() != 1){
				result =false;
			} else {
				result  = result && func.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI);
				result  = result && (func.getTerm(0) instanceof ValueConstant) &&
						((ValueConstant) func.getTerm(0)).getValue(). equals(OBDAVocabulary.RDF_TYPE);
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * This method instantiate a meta mapping by the concrete parameters
	 * 
	 * @param targetQuery
	 * @param bodyAtom
	 * @param sourceParsedQuery
	 * @param columnsForTemplate
	 * @param columnsForValues
	 * @param params
	 * @return
	 */
	private OBDARDBMappingAxiom instantiateMapping(String id, CQIE targetQuery,
			Function bodyAtom, VisitedQuery sourceParsedQuery,
			List<SelectExpressionItem> columnsForTemplate,
			List<SelectExpressionItem> columnsForValues,
			List<String> params, int arity) {
		
		/*
		 * First construct new Target Query 
		 */
		Function newTargetHead = targetQuery.getHead();
		Function newTargetBody = expandHigherOrderAtom(bodyAtom, params, arity);
		CQIE newTargetQuery = dfac.getCQIE(newTargetHead, newTargetBody);
		
		/*
		 * Then construct new Source Query
		 */
		
		/*
		 * new Selection 
		 */
		SelectionJSQL selection = null;
		try {
			selection = sourceParsedQuery.getSelection();
			
		} catch (JSQLParserException e1) {
			
			e1.printStackTrace();
		}
		SelectionJSQL newSelection;
		
		if(selection != null){
			newSelection = selection;
		} else {
			newSelection = new SelectionJSQL();
		}
		
			int j=0;
			for(SelectExpressionItem column : columnsForTemplate){
				
				Expression columnRefExpression = column.getExpression();
				
				StringValue clsStringValue = new StringValue("'"+params.get(j)+"'");
				
				//we are considering only equivalences
				BinaryExpression condition = new EqualsTo();
				condition.setLeftExpression(columnRefExpression);
				condition.setRightExpression(clsStringValue);
				

				if(newSelection.getRawConditions()!=null){
					BinaryExpression andOperator = new AndExpression(newSelection.getRawConditions(), condition);
					newSelection.addCondition(andOperator);
				}
				else
				newSelection.addCondition(condition);
				j++;	
			}
			
			
		
		/*
		 * new Projection
		 */
		ProjectionJSQL newProjection = new ProjectionJSQL();
		newProjection.addAll(columnsForValues);
		
		/*
		 * new statement for the source query
		 * we create a new statement with the changed projection and selection
		 */
		
		VisitedQuery newSourceParsedQuery = null;
		try {
			newSourceParsedQuery = new VisitedQuery(sourceParsedQuery.getStatement());
			newSourceParsedQuery.setProjection(newProjection);
			newSourceParsedQuery.setSelection(newSelection);
			
		} catch (JSQLParserException e) {
			
			e.printStackTrace();
		}
		
		
		String newSourceQuerySQL = newSourceParsedQuery.toString();
		OBDASQLQuery newSourceQuery =  dfac.getSQLQuery(newSourceQuerySQL);

		OBDARDBMappingAxiom newMapping = dfac.getRDBMSMappingAxiom(id, newSourceQuery, newTargetQuery);
		return newMapping;
	}

	/**
	 * This method get the columns which will be used for the predicate template 
	 * 
	 * @param varsInTemplate
	 * @param columnList
	 * @return
	 */
	private List<SelectExpressionItem> getColumnsForTemplate(List<Variable> varsInTemplate,
			ArrayList<SelectExpressionItem> columnList) {
		List<SelectExpressionItem> columnsForTemplate = new ArrayList<SelectExpressionItem>();

		for (Variable var : varsInTemplate) {
			boolean found = false;
			for (SelectExpressionItem column : columnList) {
				String expression=column.getExpression().toString();
				if(expression.contains("\"")) //remove the quotes when present to compare with var
					expression= expression.substring(1, expression.length()-1);
					
				
				if ((column.getAlias()==null && expression.equals(var.getName())) ||
						(column.getAlias()!=null && column.getAlias().equals(var.getName()))) {
					columnsForTemplate.add(column);
					found = true;
					break;
				}
			}
			if(!found){
				throw new IllegalStateException();
			}
		}
		
		return columnsForTemplate;
	}

	/**
	 * 
	 * This method extracts the variables in the template from the atom 
	 * <p>
	 * Example 1: 
	 * <p>
	 * arity = 1. 
	 * Input Atom:
	 * <pre>triple(t1, 'rdf:type', URI("http://example.org/{}/{}", X, Y))</pre>
	 * 
	 * Output: [X, Y]
	 * <p>
	 * Example 2: 
	 * <p>
	 * arity = 2. 
	 * Input Atom:
	 * <pre>triple(t1,  URI("http://example.org/{}/{}", X, Y), t2)</pre>
	 * 
	 * Output: [X, Y]

	 * 
	 * @param atom
	 * @param arity 
	 * @return
	 */
	private List<Variable> getVariablesInTemplate(Function atom, int arity) {
		
		Function uriTermForPredicate = findTemplatePredicateTerm(atom, arity);
		
		List<Variable> vars = new ArrayList<Variable>();
		for(int i = 1; i < uriTermForPredicate.getArity(); i++){
			vars.add((Variable) uriTermForPredicate.getTerm(i));
		}
		return vars;
	}

	

	
	/***
	 * This method expands the higher order atom 
	 * <pre>triple(t1, 'rdf:type', URI("http://example.org/{}", X))</pre>
	 *  to 
	 *  <pre>http://example.org/cls(t1)</pre>, if X is t1
	 * 
	 * @param atom 
	 * 			a Function in form of triple(t1, 'rdf:type', X)
	 * @param values
	 * 			the concrete name of the X 
	 * @param arity 
	 * @return
	 * 			expanded atom in form of <pre>http://example.org/cls(t1)</pre>
	 */
	private Function expandHigherOrderAtom(Function atom, List<String> values, int arity) {

		Function uriTermForPredicate = findTemplatePredicateTerm(atom, arity);
		
		String uriTemplate = ((ValueConstant) uriTermForPredicate.getTerm(0)).getValue();

		String predName = URITemplates.format(uriTemplate, values);
		
		Function result = null;
		Predicate p; 
		if(arity == 1){
			p = dfac.getPredicate(predName, arity, new COL_TYPE[]{COL_TYPE.OBJECT});
			result = dfac.getFunction(p, atom.getTerm(0));
		} else if (arity == 2){
			p = dfac.getPredicate(predName, arity, new COL_TYPE[]{COL_TYPE.OBJECT, COL_TYPE.OBJECT});
			result = dfac.getFunction(p, atom.getTerm(0), atom.getTerm(2));
		}
		return result;
	}
	
	/**
	 * This method finds the term for the predicate template
	 */
	private Function findTemplatePredicateTerm(Function atom, int arity) {
		Function uriTermForPredicate;
		
		if(arity == 1){
			uriTermForPredicate = (Function) atom.getTerm(2);
		} else if (arity == 2){
			uriTermForPredicate = (Function) atom.getTerm(1);	
		} else {
			throw new IllegalArgumentException("The parameter arity should be 1 or 2");
		}
		return uriTermForPredicate;
	}

	/**
	 * this method expands the input mappings, which may include meta mappings, to the concrete mappings
	 * 
	 * @param mappings
	 * 		a list of mappings, which may include meta mappings
	 * @return
	 * 		expanded normal mappings
	 */
	public void expand(OBDAModel obdaModel, URI sourceURI) {
		List<OBDAMappingAxiom> expandedMappings = expand(obdaModel.getMappings(sourceURI));
		
		obdaModel.removeAllMappings();
		for(OBDAMappingAxiom mapping : expandedMappings){
			try {
				obdaModel.addMapping(sourceURI, mapping);
			} catch (DuplicateMappingException e) {
				throw new RuntimeException("Error: Duplicate Mappings generated by the MetaMappingExpander");
			}
		}
		
	}
	
	

}
