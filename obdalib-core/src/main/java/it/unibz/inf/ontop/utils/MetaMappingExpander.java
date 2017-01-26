package it.unibz.inf.ontop.utils;

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
 
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.URITemplatePredicate;
import it.unibz.inf.ontop.model.ValueConstant;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.TermUtils;
import it.unibz.inf.ontop.parser.PreprocessProjection;
import it.unibz.inf.ontop.sql.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import it.unibz.inf.ontop.sql.parser.SelectQueryAttributeExtractor2;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author xiao
 *
 */
public class MetaMappingExpander {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final Connection connection;
	private final DBMetadata metadata;
	private final QuotedIDFactory idfac;
	private final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

    /**
	 *
	 * 
	 * @param connection a JDBC connection
	 */
	public MetaMappingExpander(Connection connection, DBMetadata metadata) {
		this.connection = connection;
		this.metadata = metadata;
		this.idfac = metadata.getQuotedIDFactory();
	}

	/**
	 * this method expand the input mappings, which may include meta mappings, to the concrete mappings
	 * 
	 * @param mappings
	 * 		a list of mappings, which may include meta mappings
	 * @return
	 * 		expanded normal mappings
	 * @throws SQLException 
	 * @throws JSQLParserException 
	 */
	public Collection<OBDAMappingAxiom> expand(Collection<OBDAMappingAxiom> mappings) throws SQLException, JSQLParserException {

		// TODO: merge the 3 steps

		// SPLIT MAPPINGS

		List<OBDAMappingAxiom> splittedMappings = new LinkedList<>();

		for (OBDAMappingAxiom mapping : mappings) {
			List<Function> targetQuery = mapping.getTargetQuery();

			if (targetQuery.size() == 1) {
				// For mappings with only one body atom, we do not need to change it
				splittedMappings.add(mapping);
			}
			else {
				String id = mapping.getId();
				OBDASQLQuery sourceQuery = mapping.getSourceQuery();

				for (Function bodyAtom : targetQuery) {
					String newId = IDGenerator.getNextUniqueID(id + "#");
					OBDAMappingAxiom newMapping = dfac.getRDBMSMappingAxiom(newId, sourceQuery, Collections.singletonList(bodyAtom));
					splittedMappings.add(newMapping);
				}
			}
		}

		// EXPAND

		List<OBDAMappingAxiom> expandedMappings = new LinkedList<>();

		for (OBDAMappingAxiom mapping : splittedMappings) {

			try {

				Function atom = mapping.getTargetQuery().get(0); // splitted mappings, so a singleton

				if (!atom.getFunctionSymbol().isTriplePredicate()) {
					// for normal mappings, we do not need to expand it.
					expandedMappings.add(mapping);
				}
				else {
					String sql = mapping.getSourceQuery().toString();

					ImmutableList<SelectExpressionItem> columnList = getColumnList(sql);


					Function templateAtom;
					int arity;
					if (isURIRDFType(atom.getTerm(1))) {
						// variables are in the position of object
						arity = 1;
						templateAtom = (Function) atom.getTerm(2);
					}
					else {
						// variables are in the position of predicate
						arity = 2;
						templateAtom = (Function) atom.getTerm(1);
					}

					List<Variable> varsInTemplate = getVariablesInTemplate(templateAtom);
					if (varsInTemplate.isEmpty())
						throw new IllegalArgumentException("No variables could be found for this metamapping. Check that the variable in the metamapping is enclosed in a URI, for instance http://.../{var}");

					List<SelectExpressionItem> columnsForTemplate = getColumnsForTemplate(varsInTemplate, columnList);

					List<List<String>> paramsForTemplate = getParamsForTemplate(sql, columnsForTemplate, varsInTemplate);

					List<SelectExpressionItem>  columnsForValues = new ArrayList<>(columnList);
					columnsForValues.removeAll(columnsForTemplate);

					for(List<String> params : paramsForTemplate) {
						// create a new  query with the changed projection and selection
						Expression selection = getWhereClauseExtension(columnsForTemplate, params);
						String newSourceQuerySQL = getExtendedQuery(sql, columnsForValues, selection);
						OBDASQLQuery newSourceQuery =  dfac.getSQLQuery(newSourceQuerySQL);

						// construct new Target Query by expanding higher order atoms of the form
						// <pre>triple(t1, 'rdf:type', URI("http://example.org/{}", X))</pre>
						// to
						// <pre>http://example.org/cls(t1)</pre>, if X is t1
						// (similarly for properties)

						String predName = getPredicateName(templateAtom.getTerm(0), params);
						Function newTargetBody;
						if (arity == 1) {
							Predicate p = dfac.getClassPredicate(predName);
							newTargetBody = dfac.getFunction(p, atom.getTerm(0));
						}
						else {
							Predicate p = dfac.getObjectPropertyPredicate(predName);
							newTargetBody = dfac.getFunction(p, atom.getTerm(0), atom.getTerm(2));
						}

						String newId = IDGenerator.getNextUniqueID(mapping.getId() + "#");
						OBDAMappingAxiom newMapping = dfac.getRDBMSMappingAxiom(newId, newSourceQuery,
								Collections.singletonList(newTargetBody));

						expandedMappings.add(newMapping);

						log.debug("Expanded Mapping: {}", newMapping);
					}
				}
			}
			catch (Exception e) {
				log.warn("Parse exception, check no SQL reserved keywords have been used "+ e.getMessage());
				throw e;
			}
		}
		
		return expandedMappings;
	}


	private ImmutableList<SelectExpressionItem>  getColumnList(String sql) {

		SelectQueryAttributeExtractor2 sqae = new SelectQueryAttributeExtractor2(metadata);

		PlainSelect plainSelect = sqae.getParsedSql(sql);

		Set<QualifiedAttributeID> attributes = sqae.getQueryBodyAttributes(plainSelect).keySet();

		List<SelectExpressionItem> list = new ArrayList<>();
		for (SelectItem si : plainSelect.getSelectItems()) {
			si.accept(new SelectItemVisitor() {
				@Override
				public void visit(AllColumns allColumns) {
					list.addAll(attributes.stream()
							.filter(id -> id.getRelation() == null)
							.map(id -> new SelectExpressionItem(new Column(id.getSQLRendering())))
							.collect(ImmutableCollectors.toList()));
				}

				@Override
				public void visit(AllTableColumns allTableColumns) {
					Table table = allTableColumns.getTable();
					RelationID tableId = idfac.createRelationID(table.getSchemaName(), table.getName());
					list.addAll(attributes.stream()
							.filter(id -> id.getRelation() != null && id.getRelation().equals(tableId))
							.map(id -> new SelectExpressionItem(new Column(table, id.getAttribute().getSQLRendering())))
							.collect(ImmutableCollectors.toList()));
				}

				@Override
				public void visit(SelectExpressionItem selectExpressionItem) {
					list.add(selectExpressionItem);
				}
			});
		}

		return ImmutableList.copyOf(list);
	}

	private List<List<String>> getParamsForTemplate(String sql,
													List<SelectExpressionItem> columnsForTemplate,
													List<Variable> varsInTemplate) throws SQLException, JSQLParserException {

		/*
		 * The query for params is almost the same with the original source query, except that
		 * we only need to distinct project the columns needed for the template expansion
		 */

		String distinctParamsSQL = getDistinctColumnsQuery(sql, columnsForTemplate);

		List<List<String>> paramsForClassTemplate = new LinkedList<>();
		try (Statement st = connection.createStatement()) {
			try (ResultSet rs = st.executeQuery(distinctParamsSQL)) {
				
				int varsInTemplateSize = varsInTemplate.size();
				while (rs.next()) {
					ArrayList<String> params = new ArrayList<>(varsInTemplateSize);
					for (int i = 1; i <= varsInTemplateSize; i++)
						params.add(rs.getString(i));

					paramsForClassTemplate.add(params);
				}
			}
		}
		return paramsForClassTemplate;
	}
	
	/**
	 * check if the term is {@code URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")}
	 * @param term
	 * @return
	 */
	private static boolean isURIRDFType(Term term) {
		if (term instanceof Function) {
			Function func = (Function) term;
			if (func.getArity() == 1 && (func.getFunctionSymbol() instanceof URITemplatePredicate)) {
				Term t0 = func.getTerm(0);
				if (t0 instanceof ValueConstant)
					return ((ValueConstant) t0).getValue().equals(OBDAVocabulary.RDF_TYPE);
			}
		}
		return false;
	}


	private static Expression getWhereClauseExtension(List<SelectExpressionItem> columnsForTemplate,
													 List<String> params) {
		Expression selection = null;
		int j = 0;
		for (SelectExpressionItem column : columnsForTemplate) {
			BinaryExpression condition = new EqualsTo();
			condition.setLeftExpression(column.getExpression());
			condition.setRightExpression(new StringValue("'" + params.get(j) + "'"));

			selection = (selection != null) ? new AndExpression(selection, condition) : condition;
			j++;
		}
		return selection;
	}
	
	/**
	 * This method get the columns which will be used for the predicate template 
	 * 
	 * @param varsInTemplate
	 * @param columnList
     * @return
	 */
	private List<SelectExpressionItem> getColumnsForTemplate(List<Variable> varsInTemplate,
															 List<SelectExpressionItem> columnList) {
		
		List<SelectExpressionItem> columnsForTemplate = new ArrayList<>(varsInTemplate.size());

		for (Variable var : varsInTemplate) {
			boolean found = false;
			for (SelectExpressionItem selectExpression : columnList) {

				// ROMAN (23 Sep 2015): SelectExpressionItem is of the form Expression AS Alias
				// this code does not work for complex expressions (i.e., 3 * A)
				// String expression = column.getExpression().toString();
				if (selectExpression.getExpression() instanceof Column) {
					Column c = (Column)selectExpression.getExpression();

		        	QuotedID column1 = idfac.createAttributeID(c.getColumnName());
		        	RelationID relation = null;
		        	if (c.getTable().getName() != null)
		        		relation = idfac.createRelationID(c.getTable().getSchemaName(), c.getTable().getName());

		        	QualifiedAttributeID qa = new QualifiedAttributeID(relation, column1);

					if ((selectExpression.getAlias() == null && qa.getAttribute().getName().equals(var.getName())) ||
							(selectExpression.getAlias() != null && selectExpression.getAlias().getName().equals(var.getName()))) {
						columnsForTemplate.add(selectExpression);
						found = true;
						break;
					}
				}
				else {
					if (selectExpression.getExpression() instanceof StringValue) {
						if (selectExpression.getAlias() != null && selectExpression.getAlias().getName().equals(var.getName())) {
							columnsForTemplate.add(selectExpression);
							found = true;
							break;
						}
					}
				}
			}
			if(!found){
                String format = "The placeholder '%s' in the target does not occur in the body of the mapping";

                throw new IllegalStateException(String.format(format,
                        var.getName()/*,  mapping.toString()*/));
			}
		}
		
		return columnsForTemplate;
	}

	/**
	 * 
	 * This method extracts the variables in the template from the atom 
	 * <p>
	 * Example:
	 * <p>
	 * Input templateAtom:
	 * <pre>URI("http://example.org/{}/{}", X, Y)</pre>
	 * 
	 * Output: [X, Y]
	 * <p>
	 *
	 * @param templateAtom
	 * @return
	 */
	private static List<Variable> getVariablesInTemplate(Function templateAtom) {
		
		int len = templateAtom.getTerms().size();

		//consider the case of <{varUri}>
		if (len == 1) {
			Term uri = templateAtom.getTerm(0);
			if (uri instanceof Variable) {
				List<Variable> vars = new ArrayList<>(1);
				vars.add((Variable) uri);
				return vars;
			}
			else {
				return Collections.emptyList();
			}
		}
		else {
			List<Variable> vars = new ArrayList<>(len - 1);
			// TODO: check when getTerms().size() != getArity()
			// index 0 is for the URI template term
			for (int i = 1; i < len; i++)
				vars.add((Variable) templateAtom.getTerm(i));
			return vars;
		}
	}

	

	

	private static String getPredicateName(Term templateTerm, List<String> values) {
		if (templateTerm instanceof Variable) {
			return values.get(0);
		}
		else {
			String uriTemplate = ((ValueConstant) templateTerm).getValue();
			return URITemplates.format(uriTemplate, values);
		}
	}

	/**
	 * Set the object construction for the SELECT clause
	 */

	private static String getDistinctColumnsQuery(String originalSQL, List<SelectExpressionItem> columnList) throws JSQLParserException {
		Select select = (Select)CCJSqlParserUtil.parse(originalSQL);
		PlainSelect plainSelect = (PlainSelect)select.getSelectBody();

		plainSelect.setDistinct(new Distinct());
		plainSelect.setSelectItems(ImmutableList.copyOf(columnList));

		return select.toString();
	}

	/**
	 * Set the object construction for the WHERE clause
	 */

	private static String getExtendedQuery(String originalSQL, List<SelectExpressionItem> columnList, Expression whereClauseExtension) throws JSQLParserException {
		Select select = (Select)CCJSqlParserUtil.parse(originalSQL);
		PlainSelect plainSelect = (PlainSelect)select.getSelectBody();

		if (!columnList.isEmpty())
			plainSelect.setSelectItems(ImmutableList.copyOf(columnList));
		else
			plainSelect.setSelectItems(ImmutableList.of(new AllColumns()));

		if (whereClauseExtension != null)
			if (plainSelect.getWhere() != null)
				plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), whereClauseExtension));
			else
				plainSelect.setWhere(whereClauseExtension);

		return select.toString();
	}
}
