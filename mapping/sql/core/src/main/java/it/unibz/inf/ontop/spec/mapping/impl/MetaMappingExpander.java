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
import com.google.common.collect.Lists;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.MetaMappingExpansionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.SelectQueryAttributeExtractor2;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.Templates;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * MetaMappingExpander
 *
 * @author xiao, roman
 */

public class MetaMappingExpander {

	private static final Logger log = LoggerFactory.getLogger(MetaMappingExpander.class);

	private final SQLPPSourceQueryFactory sourceQueryFactory;
	private final TermFactory termFactory;
	private final SubstitutionFactory substitutionFactory;
	private final org.apache.commons.rdf.api.RDF rdfFactory;

	private static final class Expansion {
		private final String id;
		private final SQLPPSourceQuery source;
		private final TargetAtom target;

		Expansion(String id, SQLPPSourceQuery source, TargetAtom target) {
			this.id = id;
			this.source = source;
			this.target = target;
		}
	}

	public MetaMappingExpander(TermFactory termFactory,
							   SubstitutionFactory substitutionFactory,
							   org.apache.commons.rdf.api.RDF rdfFactory,
							   SQLPPSourceQueryFactory sourceQueryFactory) {
		this.termFactory = termFactory;
		this.substitutionFactory = substitutionFactory;
		this.rdfFactory = rdfFactory;
		this.sourceQueryFactory = sourceQueryFactory;
	}


	/**
	 * this method expand the input mappings, which may include meta mappings, to the concrete mappings
	 *
	 * @return
	 * 		expanded normal mappings
	 */
	public SQLPPMapping getExpandedMappings(SQLPPMapping ppMapping, Connection connection, MetadataLookup metadata, QuotedIDFactory idFactory)
			throws MetaMappingExpansionException {

		ImmutableList.Builder<SQLPPTriplesMap> result = ImmutableList.builder();
		ImmutableList.Builder<Expansion> builder2 = ImmutableList.builder();

		for (SQLPPTriplesMap mapping : ppMapping.getTripleMaps()) {

			// search for non-ground elements in the target atom of each mapping (in class or property positions)
			ImmutableList<TargetAtom> nonGroundCPs = mapping.getTargetAtoms().stream()
					.filter(t -> t.getProjectionAtom().getPredicate() instanceof RDFAtomPredicate)
					.filter(t -> {
						RDFAtomPredicate predicate = (RDFAtomPredicate)t.getProjectionAtom().getPredicate();
						return predicate.getPropertyIRI(t.getSubstitutedTerms())
								.map(i -> i.equals(RDF.TYPE)
										&& !predicate.getObject(t.getSubstitutedTerms()).isGround())
								.orElse(!predicate.getProperty(t.getSubstitutedTerms()).isGround());
					})
					.collect(ImmutableCollectors.toList());

			if (nonGroundCPs.isEmpty()) {
				result.add(mapping);
			}
			else {
				builder2.addAll(nonGroundCPs.stream()
						.map(t -> new Expansion(mapping.getId(), mapping.getSourceQuery(), t))
						.iterator());
				if (nonGroundCPs.size() < mapping.getTargetAtoms().size()) {
					result.add(new OntopNativeSQLPPTriplesMap(
							mapping.getId(),
							mapping.getSourceQuery(),
							mapping.getTargetAtoms().stream()
									.filter(a -> !nonGroundCPs.contains(a))
									.collect(ImmutableCollectors.toList())));
				}
			}
		}
		ImmutableList<Expansion> expansions = builder2.build();

		if (expansions.isEmpty())
			return ppMapping;

		List<String> errorMessages = new LinkedList<>();
		for (Expansion m : expansions) {
			try {
				RDFAtomPredicate predicate = (RDFAtomPredicate)m.target.getProjectionAtom().getPredicate();
				boolean isC = predicate.getPropertyIRI(m.target.getSubstitutedTerms())
						.map(i -> i.equals(RDF.TYPE)
								&& !predicate.getObject(m.target.getSubstitutedTerms()).isGround()).isPresent();

				ImmutableFunctionalTerm templateTerm = isC
						? (ImmutableFunctionalTerm)predicate.getObject(m.target.getSubstitutedTerms())
						: (ImmutableFunctionalTerm)predicate.getProperty(m.target.getSubstitutedTerms());

				ImmutableList<Variable> templateVariables = extractTemplateVariables(templateTerm);

				List<QuotedID> templateColumnIds = getTemplateColumnNames(idFactory, templateVariables);

				Map<QuotedID, SelectExpressionItem> queryColumns = getQueryColumns(metadata, idFactory, m.source.getSQL());

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
									" in the target do(es) not occur in the body of the mapping: " + m.source.getSQL())));
				}

				String query = getTemplateValuesQuery(m.source.getSQL(), templateColumns);
				final int size = templateColumns.size();
				try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(query)) {
					while (rs.next()) {
						List<String> values = Lists.newArrayListWithCapacity(size);
						for (int i = 1; i <= size; i++)
							values.add(rs.getString(i));

						// Cannot build an IRI out of nulls
						if (values.contains(null))
							continue;

						String newSourceQuery = getInstantiatedSQL(m.source.getSQL(), templateColumns, values);

						IRIConstant predicateTerm = termFactory.getConstantIRI(
								rdfFactory.createIRI(getPredicateName(templateTerm.getTerm(0), values)));

						Variable predicateVariable = isC
								? predicate.getObject(m.target.getProjectionAtom().getArguments())
								: predicate.getProperty(m.target.getProjectionAtom().getArguments());

						ImmutableSubstitution<ImmutableTerm> newSubstitution = m.target.getSubstitution()
								.composeWith(substitutionFactory.getSubstitution(predicateVariable, predicateTerm));

						TargetAtom newTarget = m.target.changeSubstitution(newSubstitution);

						// TODO: see how to keep the provenance
						SQLPPTriplesMap newMapping = new OntopNativeSQLPPTriplesMap(
								IDGenerator.getNextUniqueID(m.id + "#"),
								sourceQueryFactory.createSourceQuery(newSourceQuery),
								ImmutableList.of(newTarget));

						result.add(newMapping);
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

		return new SQLPPMappingImpl(result.build(), ppMapping.getPrefixManager());
	}

	private ImmutableList<Variable> extractTemplateVariables(ImmutableFunctionalTerm templateAtom) {
		return Optional.of(templateAtom)
				.filter(a -> a.getFunctionSymbol() instanceof RDFTermFunctionSymbol)
				.map(a -> a.getTerm(0))
				.map(l -> (l instanceof ImmutableFunctionalTerm)
						&& (((ImmutableFunctionalTerm) l).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol)
						? ((ImmutableFunctionalTerm) l).getTerms().stream()
						: Stream.of(l))
				.map(s -> s
						// Unwrap CASTs
						.map(t -> (t instanceof ImmutableFunctionalTerm)
								&& (((ImmutableFunctionalTerm) t).getFunctionSymbol() instanceof DBTypeConversionFunctionSymbol)
								? ((ImmutableFunctionalTerm) t).getTerm(0)
								: t)
						.filter(t -> t instanceof Variable)
						.map(t -> (Variable) t)
						.collect(ImmutableCollectors.toList()))
				.orElseThrow(() -> new MinorOntopInternalBugException(
						String.format("Unexpected template atom %s: was expected to a RDF functional term", templateAtom)));
	}


	private ImmutableMap<QuotedID, SelectExpressionItem> getQueryColumns(MetadataLookup metadata, QuotedIDFactory idFactory, String sql)
			throws InvalidSelectQueryException, UnsupportedSelectQueryException {

		SelectQueryAttributeExtractor2 sqae = new SelectQueryAttributeExtractor2(metadata, idFactory, termFactory);
		PlainSelect plainSelect = sqae.getParsedSql(sql);
		ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes = sqae.getQueryBodyAttributes(plainSelect);

		ImmutableMap.Builder<QuotedID, SelectExpressionItem> builder = ImmutableMap.builder();
		for (SelectItem si : plainSelect.getSelectItems()) {
			si.accept(new SelectItemVisitor() {
				@Override
				public void visit(AllColumns allColumns) {
					builder.putAll(sqae.expandStar(attributes).keySet().stream()
							.collect(ImmutableCollectors.toMap(
									QualifiedAttributeID::getAttribute,
									id -> new SelectExpressionItem(new Column(id.getAttribute().getSQLRendering())))));
				}

				@Override
				public void visit(AllTableColumns allTableColumns) {
					Table table = allTableColumns.getTable();
					builder.putAll(sqae.expandStar(attributes, table).keySet().stream()
							.collect(ImmutableCollectors.toMap(
									QualifiedAttributeID::getAttribute,
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

		Select select = (Select)CCJSqlParserUtil.parse(sql); // TODO: throws an Error if lexer fails
		PlainSelect plainSelect = (PlainSelect)select.getSelectBody();

		plainSelect.setDistinct(new Distinct());
		plainSelect.setSelectItems(ImmutableList.copyOf(templateColumns)); // SelectExpressionItem -> SelectItem

		return select.toString();
	}

	/**
	 * Create a new query with the changed projection and selection
	 */

	private static String getInstantiatedSQL(String sql,
											 List<SelectExpressionItem> templateColumns,
											 List<String> values) throws JSQLParserException {

		Select select = (Select) CCJSqlParserUtil.parse(sql);  // TODO: throws an Error if lexer fails
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
		return select.toString();
	}


	private static ImmutableList<QuotedID> getTemplateColumnNames(QuotedIDFactory idfac,
																  ImmutableList<Variable> templateVariables) {
		// TODO : case-insensitive
		QuotedIDFactory rawIdFactory = new RawQuotedIDFactory(idfac);
		return templateVariables.stream()
				.map(v -> rawIdFactory.createAttributeID(v.getName()))
				.collect(ImmutableCollectors.toList());
	}

	private static String getPredicateName(ImmutableTerm lexicalTerm, List<String> values) {
		if (lexicalTerm instanceof Variable) {
			// Hacky!! TODO: clean this code
			return values.get(0);
		}
		else if (lexicalTerm instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionalLexicalTerm = (ImmutableFunctionalTerm) lexicalTerm;
			FunctionSymbol functionSymbol = functionalLexicalTerm.getFunctionSymbol();
			if (functionSymbol instanceof ObjectStringTemplateFunctionSymbol) {
				String iriTemplate = ((ObjectStringTemplateFunctionSymbol)
						functionalLexicalTerm.getFunctionSymbol()).getTemplate();
				return Templates.format(iriTemplate, values);
			}
			else if ((functionSymbol instanceof DBTypeConversionFunctionSymbol)
					&& ((DBTypeConversionFunctionSymbol) functionSymbol).isTemporary()) {
				return getPredicateName(functionalLexicalTerm.getTerm(0), values);
			}
		}
		throw new MinorOntopInternalBugException("Unexpected lexical template term: " + lexicalTerm);
	}
}
