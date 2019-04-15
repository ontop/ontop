package it.unibz.inf.ontop.spec.mapping.impl;

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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.MetaMappingExpansionException;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryAttributeExtractor2;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.URITemplates;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * MetaMappingExpander
 *
 * @author xiao, roman
 */

public class MetaMappingExpander {

	private static final Logger log = LoggerFactory.getLogger(MetaMappingExpander.class);
	
	private static final SQLMappingFactory MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
	private final TermFactory termFactory;
	private final ImmutableList<SQLPPTriplesMap> nonExpandableMappings;
	private final ImmutableList<Expansion> mappingsToBeExpanded;
	private final SubstitutionFactory substitutionFactory;


	private static final class Expansion {
		private final String id;
		private final OBDASQLQuery source;
		private final TargetAtom target;

		Expansion(String id, OBDASQLQuery source, TargetAtom target) {
			this.id = id;
			this.source = source;
			this.target = target;
		}
	}

	public MetaMappingExpander(Collection<SQLPPTriplesMap> mappings, TermFactory termFactory,
							   SubstitutionFactory substitutionFactory) {
		this.termFactory = termFactory;
		this.substitutionFactory = substitutionFactory;

		ImmutableList.Builder<SQLPPTriplesMap> builder1 = ImmutableList.builder();
		ImmutableList.Builder<Expansion> builder2 = ImmutableList.builder();

		for (SQLPPTriplesMap mapping : mappings) {

			//serach for not grounded elements in the predicate of each mapping (position 2 or 3)
			ImmutableList<TargetAtom> toBeExpanded = mapping.getTargetAtoms().stream()
					.filter(targetAtom -> {

						ImmutableTerm propertyTerm = targetAtom.getSubstitutedTerm(1);

							if(isURIRDFType(propertyTerm)){
								//check if the class is grounded
								return  !targetAtom.getSubstitutedTerm(2).isGround();
							}
							else{
								return !propertyTerm.isGround();
							}

						})
					.collect(ImmutableCollectors.toList());

			if (toBeExpanded.isEmpty()) {
				builder1.add(mapping);
			}
			else {
				builder2.addAll(toBeExpanded.stream()
						.map(target -> new Expansion(mapping.getId(), mapping.getSourceQuery(), target))
						.iterator());
			}
		}
		nonExpandableMappings = builder1.build();
		mappingsToBeExpanded = builder2.build();
	}

	public boolean hasMappingsToBeExpanded() { return !mappingsToBeExpanded.isEmpty(); }

	public ImmutableList<SQLPPTriplesMap> getNonExpandableMappings() { return nonExpandableMappings; }

	/**
	 * this method expand the input mappings, which may include meta mappings, to the concrete mappings
	 *
	 * @return
	 * 		expanded normal mappings
	 */
	public ImmutableList<SQLPPTriplesMap> getExpandedMappings(Connection connection, DBMetadata metadata)
			throws MetaMappingExpansionException {

		List<String> errorMessages = new LinkedList<>();

		ImmutableList.Builder<SQLPPTriplesMap> builder = ImmutableList.builder();
		builder.addAll(nonExpandableMappings);

		for (Expansion m : mappingsToBeExpanded) {
			try {
				boolean isClass = isURIRDFType(m.target.getSubstitutedTerm(1));
				// if isClass, then the template is the object;
				// otherwise, it's a property and the template is the predicate
				ImmutableFunctionalTerm templateAtom = (ImmutableFunctionalTerm)m.target.getSubstitutedTerm(isClass ? 2 : 1);

				List<QuotedID> templateColumnIds = getTemplateColumnNames(metadata.getQuotedIDFactory(), templateAtom.getTerms());

				Map<QuotedID, SelectExpressionItem> queryColumns = getQueryColumns(metadata, m.source.getSQLQuery());

				List<SelectExpressionItem> templateColumns;
				try {
					templateColumns = templateColumnIds.stream()
							.map(id -> queryColumns.get(id))
							.collect(ImmutableCollectors.toList());
				}
				catch (NullPointerException e) {
					throw new IllegalArgumentException(templateColumnIds.stream()
							.filter(id -> !queryColumns.containsKey(id))
							.map(Object::toString)
							.collect(Collectors.joining(", ",
									"The placeholder(s) ",
									" in the target do(es) not occur in the body of the mapping")));
				}

				List<SelectItem> newColumns = queryColumns.values().stream()
						.filter(c -> !templateColumns.contains(c))
						.collect(ImmutableCollectors.toList());
				if (newColumns.isEmpty())   // avoid empty SELECT clause
					newColumns = ImmutableList.of(new AllColumns());

				String query = getTemplateValuesQuery(m.source.getSQLQuery(), templateColumns);
				final int size = templateColumns.size();
				try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(query)) {
					while (rs.next()) {
						List<String> values = Lists.newArrayListWithCapacity(size);
						for (int i = 1; i <= size; i++)
							values.add(rs.getString(i));

						String newSourceQuery = getInstantiatedSQL(m.source.getSQLQuery(), newColumns, templateColumns, values);

						ImmutableList<Variable> templateVariables = templateAtom.getTerms().stream()
										.filter(t -> t instanceof Variable)
										.map(v -> (Variable) v)
										.distinct()
										.collect(ImmutableCollectors.toList());

						// Transforms the result set into a substitution
						ImmutableSubstitution<ValueConstant> valueSubstitution = substitutionFactory.getSubstitution(
								IntStream.range(0, values.size())
								.boxed()
								.collect(ImmutableCollectors.toMap(
										templateVariables::get,
										i -> termFactory.getConstantLiteral(values.get(i)))));

						// In a mapping assertion, we create ground terms instead of constants for IRIs
						// (so as to guarantee that RDF functions can ALWAYS be lifted after unfolding)
						GroundFunctionalTerm predicateTerm = (GroundFunctionalTerm) termFactory.getImmutableUriTemplate(
								termFactory.getConstantLiteral(getPredicateName(templateAtom.getTerm(0), values)));
						Variable predicateVariable = m.target.getProjectionAtom().getArguments().get(isClass ? 2 : 1);
						// TODO: simplify (for the beta-4)
						ImmutableSubstitution<ImmutableTerm> newSubstitution = valueSubstitution.composeWith(
								m.target.getSubstitution()
										.composeWith(substitutionFactory.getSubstitution(predicateVariable, predicateTerm)));

						TargetAtom newTarget = m.target.changeSubstitution(newSubstitution);

						// TODO: see how to keep the provenance
						SQLPPTriplesMap newMapping = new OntopNativeSQLPPTriplesMap(
								IDGenerator.getNextUniqueID(m.id + "#"),
								MAPPING_FACTORY.getSQLQuery(newSourceQuery),
								ImmutableList.of(newTarget));

						builder.add(newMapping);
						log.debug("Expanded Mapping: {}", newMapping);
					}
				}
			}
			catch (Exception e) {
				log.warn("Expanding meta-mappings exception: " + e.getMessage());
				errorMessages.add(e.getMessage());
			}
		}

		if (!errorMessages.isEmpty())
			throw new MetaMappingExpansionException(Joiner.on("\n").join(errorMessages));

		return builder.build();
	}



	private ImmutableMap<QuotedID, SelectExpressionItem> getQueryColumns(DBMetadata metadata, String sql)
			throws InvalidSelectQueryException, UnsupportedSelectQueryException {

		SelectQueryAttributeExtractor2 sqae = new SelectQueryAttributeExtractor2(metadata, termFactory);
		PlainSelect plainSelect = sqae.getParsedSql(sql);
		ImmutableMap<QualifiedAttributeID, Term> attributes = sqae.getQueryBodyAttributes(plainSelect);

		ImmutableMap.Builder<QuotedID, SelectExpressionItem> builder = ImmutableMap.builder();
		for (SelectItem si : plainSelect.getSelectItems()) {
			si.accept(new SelectItemVisitor() {
				@Override
				public void visit(AllColumns allColumns) {
					builder.putAll(sqae.expandStar(attributes).keySet().stream()
							.collect(ImmutableCollectors.toMap(
									id -> id.getAttribute(),
									id -> new SelectExpressionItem(new Column(id.getAttribute().getSQLRendering())))));
				}

				@Override
				public void visit(AllTableColumns allTableColumns) {
					Table table = allTableColumns.getTable();
					builder.putAll(sqae.expandStar(attributes, table).keySet().stream()
							.collect(ImmutableCollectors.toMap(
									id -> id.getAttribute(),
									id -> new SelectExpressionItem(new Column(table, id.getAttribute().getSQLRendering())))));
				}

				@Override
				public void visit(SelectExpressionItem selectExpressionItem) {
					builder.put(sqae.getSelectItemAliasedId(selectExpressionItem), selectExpressionItem);
				}
			});
		}

		return builder.build();
	}

	/**
		The query for obtaining values of parameters is almost the same with the original source query,
		except that we only need to distinct project the columns needed for the template expansion
	 */

	private static String getTemplateValuesQuery(String sql, List<SelectExpressionItem> templateColumns) throws JSQLParserException {

		Select select = (Select)CCJSqlParserUtil.parse(sql);
		PlainSelect plainSelect = (PlainSelect)select.getSelectBody();

		plainSelect.setDistinct(new Distinct());
		plainSelect.setSelectItems(ImmutableList.copyOf(templateColumns)); // SelectExpressionItem -> SelectItem

		return select.toString();
	}

	/**
	 * Create a new query with the changed projection and selection
	 */

	private static String getInstantiatedSQL(String sql,
											 List<SelectItem> newColumns,
											 List<SelectExpressionItem> templateColumns,
											 List<String> values) throws JSQLParserException {

		Select select = (Select) CCJSqlParserUtil.parse(sql);
		PlainSelect plainSelect = (PlainSelect)select.getSelectBody();

		Expression where = plainSelect.getWhere();
		int size = templateColumns.size(); // both lists have the same size
		for (int i = 0; i < size; i++) {
			BinaryExpression condition = new EqualsTo();
			condition.setLeftExpression(templateColumns.get(i).getExpression());
			condition.setRightExpression(new StringValue("'" + values.get(i) + "'"));

			where = (where == null) ? condition : new AndExpression(where, condition);
		}

		plainSelect.setWhere(where); // where cannot be null
		plainSelect.setSelectItems(newColumns);

		return select.toString();
	}

	/**
	 * check if the term is {@code URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")}
	 *
	 * TODO: refactor so as to use RDFPredicate.getClassIRI() instead
	 */
	private static boolean isURIRDFType(ImmutableTerm term) {
		if (term instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm func = (ImmutableFunctionalTerm) term;
			if (func.getArity() == 1 && (func.getFunctionSymbol() instanceof URITemplatePredicate)) {
				ImmutableTerm t0 = func.getTerm(0);
				if (t0 instanceof ValueConstant)
					return ((ValueConstant) t0).getValue().equals(RDF.TYPE.getIRIString());
			}
		}
		return false;
	}

	/**
	 *
	 * Extracts the column names from the URI template atom
	 * <p>
	 * Example Input: <pre>URI("http://example.org/{}/{}", X, Y)</pre>
	 *
	 * Output: [X, Y]
	 *
	 */

	private static ImmutableList<QuotedID> getTemplateColumnNames(QuotedIDFactory idfac,
																  ImmutableList<? extends ImmutableTerm> templateTerms) {

		final ImmutableList<Variable> vars;
		int len = templateTerms.size();
		if (len == 1) { // the case of <{varUri}>
			ImmutableTerm uri = templateTerms.get(0);
			if (uri instanceof Variable)
				 vars = ImmutableList.of((Variable) uri);
			else
				throw new IllegalArgumentException("No variables could be found for this metamapping." +
					"Check that the variable in the metamapping is enclosed in a URI, for instance, " +
					"http://.../{var}");
		}
		else {
			ImmutableList.Builder<Variable> builder = ImmutableList.builder();
			for (int i = 1; i < len; i++) // index 0 is for the URI template term
				builder.add((Variable) templateTerms.get(i));
			vars = builder.build();
		}
		return vars.stream()
				.map(v -> QuotedID.createIdFromDatabaseRecord(idfac, v.getName()))
				.collect(ImmutableCollectors.toList());
	}

	private static String getPredicateName(ImmutableTerm templateTerm, List<String> values) {
		if (templateTerm instanceof Variable) {
			return values.get(0);
		}
		else {
			String uriTemplate = ((ValueConstant) templateTerm).getValue();
			return URITemplates.format(uriTemplate, values);
		}
	}
}
