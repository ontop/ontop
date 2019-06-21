package it.unibz.inf.ontop.answering.reformulation.generation.impl;

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


import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLAdapterFactory;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.exception.UnsupportedFeatureForDatalogConversionException;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.impl.TermUtils;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * LEGACY
 *
 * Expects the IQTree to be normalized.
 *
 * @author mrezk, mariano, guohui, roman
 *
 */
@Singleton
public class LegacySQLIQTree2NativeNodeGenerator {

	/**
	 * Formatting templates
	 */
    private static final String VIEW_PREFIX = "Q";
    private static final String VIEW_SUFFIX = "VIEW";
    private static final String VIEW_ANS_SUFFIX = "View";
	private static final String OUTER_VIEW_NAME = "SUB_QVIEW";

	private static final String INDENT = "    ";
	private final IQ2DatalogTranslator iq2DatalogTranslator;

	private final boolean distinctResultSet;

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(LegacySQLIQTree2NativeNodeGenerator.class);
	private final Relation2Predicate relation2Predicate;
	private final DatalogNormalizer datalogNormalizer;
	private final DatalogFactory datalogFactory;
	private final TermFactory termFactory;
	private final IntermediateQueryFactory iqFactory;
	private final ImmutabilityTools immutabilityTools;
	private final UniqueTermTypeExtractor uniqueTermTypeExtractor;
	private final boolean isDistinct = false;
	private final OntopReformulationSQLSettings settings;
	private final String driverURI;


	@Inject
	private LegacySQLIQTree2NativeNodeGenerator(OntopReformulationSQLSettings settings,
												IQ2DatalogTranslator iq2DatalogTranslator,
												Relation2Predicate relation2Predicate,
												DatalogNormalizer datalogNormalizer, DatalogFactory datalogFactory,
												TermFactory termFactory,
												IntermediateQueryFactory iqFactory,
												ImmutabilityTools immutabilityTools,
												UniqueTermTypeExtractor uniqueTermTypeExtractor) {
		this.relation2Predicate = relation2Predicate;
		this.datalogNormalizer = datalogNormalizer;
		this.datalogFactory = datalogFactory;
		this.termFactory = termFactory;
		this.iqFactory = iqFactory;
		this.immutabilityTools = immutabilityTools;
		this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;

		this.driverURI = settings.getJdbcDriver();
		this.settings = settings;

		this.distinctResultSet = settings.isDistinctPostProcessingEnabled();
		this.iq2DatalogTranslator = iq2DatalogTranslator;
	}

	public NativeNode generate(IQTree iqTree, RDBMetadata metadata) {

		ImmutableSortedSet<Variable> signature = ImmutableSortedSet.copyOf(iqTree.getVariables());

		SQLDialectAdapter sqladapter = SQLAdapterFactory.getSQLDialectAdapter(driverURI, metadata.getDbmsVersion(),
				settings);

		DatalogProgram queryProgram = null;
		try {
			queryProgram = iq2DatalogTranslator.translate(iqTree, ImmutableList.copyOf(signature));
		} catch (UnsupportedFeatureForDatalogConversionException e) {
			throw new RuntimeException(e);
		}

		for (CQIE cq : queryProgram.getRules()) {
			datalogNormalizer.addMinimalEqualityToLeftOrNestedInnerJoin(cq);
		}
		log.debug("Program normalized for SQL translation:\n" + queryProgram);

//		MutableQueryModifiers queryModifiers = queryProgram.getQueryModifiers();
//		isDistinct = queryModifiers.hasModifiers() && queryModifiers.isDistinct();

		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(queryProgram.getRules());
		Multimap<Predicate, CQIE> ruleIndex = depGraph.getRuleIndex();
		List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();
		List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();

		SQLAndColumnNames sqlAndColumnNames = generateQuery(signature.stream()
						.map(Variable::getName)
						.collect(ImmutableCollectors.toList()),
				ruleIndex, predicatesInBottomUp, extensionalPredicates, sqladapter, metadata);

		ImmutableList<Variable> signatureList = ImmutableList.copyOf(signature);

		ImmutableMap<Variable, String> variableNames = IntStream.range(0, signature.size())
				.boxed()
				.collect(ImmutableCollectors.toMap(
						signatureList::get,
						sqlAndColumnNames.columnNames::get));

		return iqFactory.createNativeNode(
				signature,
				extractVariableTypeMap(iqTree),
				variableNames,
				sqlAndColumnNames.string,
				iqTree.getVariableNullability());
	}

    private ImmutableMap<Variable, DBTermType> extractVariableTypeMap(IQTree normalizedSubTree) {
		return normalizedSubTree.getVariables().stream()
				.collect(ImmutableCollectors.toMap(
						v -> v,
						v -> extractUniqueKnownType(v, normalizedSubTree)));
	}

	private DBTermType extractUniqueKnownType(Variable v, IQTree normalizedSubTree) {
		return uniqueTermTypeExtractor.extractUniqueTermType(v, normalizedSubTree)
				.filter(t -> t instanceof DBTermType)
				.map(t -> (DBTermType) t)
				.orElseThrow(() -> new MinorOntopInternalBugException(
						"Was expecting an unique and known DB term type to be extracted " +
								"for the SQL variable " + v));
	}


	/**
	 * Generates the main part of SQL query (except top query modifiers and perhaps an additional projection).
	 * An important part of this program is {@link #generateQueryFromRules}
	 * that will create a view for every ans predicate in the input Datalog program.
	 *
	 * @param signature is the list of main columns in the ResultSet
	 * @param ruleIndex maps intentional predicates to its rules
	 * @param predicatesInBottomUp the topologically ordered predicates in the program
	 * @param extensionalPredicates are the predicates that are not defined by any rule
	 * @param sqladapter
	 * @param metadata
	 * @return
	 */
	private SQLAndColumnNames generateQuery(List<String> signature,
											Multimap<Predicate, CQIE> ruleIndex,
											List<Predicate> predicatesInBottomUp,
											List<Predicate> extensionalPredicates, SQLDialectAdapter sqladapter,
											RDBMetadata metadata) {

		AtomicInteger viewCounter = new AtomicInteger(0);

		// non-top-level intensional predicates - need to create subqueries

		ImmutableMap.Builder<Predicate, FromItem> subQueryDefinitionsBuilder = ImmutableMap.builder();
		Set<RelationID> usedAliases = new HashSet<>();
		// create a view for every ans predicate in the Datalog input program.
		int topLevel = predicatesInBottomUp.size() - 1;
		for (int i = 0; i < topLevel; i++) {
			Predicate pred = predicatesInBottomUp.get(i);
			if (!extensionalPredicates.contains(pred)) {
				// extensional predicates are defined by DBs, so, we skip them
				/*
				 * handle the semantics of OPTIONAL when there
				 * are multiple mappings or Unions. It will take mappings of the form
				 * <ul>
				 * <li>Concept <- definition1</li>
				 * <li>Concept <- definition2</li>
				 * </ul>
				 * And will generate a view of the form
				 * <ul>
				 * <li>QConceptView = definition1 UNION definition2
				 * </ul>
				 * This view is stored in the <code>metadata </code>. See DBMetadata
				 *
				 * The idea is to use the view definition in the case of Union in the
				 * Optionals/LeftJoins
				 */

				// all have the same arity
				int size = ruleIndex.get(pred).iterator().next().getHead().getArity();
				// create signature
				ImmutableList.Builder<String> varListBuilder = ImmutableList.builder();
				for (int k = 0; k < size; k++) {
					varListBuilder.add("v" + k);
				}
				ImmutableList<SignatureVariable> s = createSignature(varListBuilder.build(), sqladapter);

				// Creates the body of the subquery
				SQLAndColumnNames subSqlAndColumnNames = generateQueryFromRules(ruleIndex.get(pred), s,
						subQueryDefinitionsBuilder.build(), false, viewCounter, sqladapter, metadata);
				String subQuery = subSqlAndColumnNames.string;


				RelationID subQueryAlias = createAlias(pred.getName(), VIEW_ANS_SUFFIX, usedAliases, sqladapter,
						metadata.getQuotedIDFactory());
				usedAliases.add(subQueryAlias);

				ImmutableList.Builder<QualifiedAttributeID> columnsBuilder = ImmutableList.builder();
				for (SignatureVariable var : s) {
					columnsBuilder.add(new QualifiedAttributeID(subQueryAlias,
							metadata.getQuotedIDFactory().createAttributeID(var.columnAlias)));
				}
				FromItem item = new FromItem(subQueryAlias, inBrackets(subQuery), columnsBuilder.build());
				subQueryDefinitionsBuilder.put(pred, item);
			}
		}

		// top-level intensional predicate
		Predicate topLevelPredicate = predicatesInBottomUp.get(topLevel);
		ImmutableList<SignatureVariable> topSignature = createSignature(signature, sqladapter);

		return generateQueryFromRules(ruleIndex.get(topLevelPredicate), topSignature,
				subQueryDefinitionsBuilder.build(), isDistinct && !distinctResultSet,
                viewCounter, sqladapter, metadata);
	}


	/**
	 * Takes a union of CQs and returns its SQL translation.
	 * It is a helper method for{@link #generateQuery}
	 * @param cqs
	 * @param signature
	 * @param subQueryDefinitions
	 * @param unionNoDuplicates
	 * @param viewCounter
	 * @param sqladapter
	 * @param metadata
	 */
	private SQLAndColumnNames generateQueryFromRules(Collection<CQIE> cqs,
													 ImmutableList<SignatureVariable> signature,
													 ImmutableMap<Predicate, FromItem> subQueryDefinitions,
													 boolean unionNoDuplicates, AtomicInteger viewCounter,
													 SQLDialectAdapter sqladapter, DBMetadata metadata) {

		List<String> sqls = Lists.newArrayListWithExpectedSize(cqs.size());
		for (CQIE cq : cqs) {
		    /* Main loop, constructing the SPJ query for each CQ */
			AliasIndex index = new AliasIndex(cq, subQueryDefinitions, viewCounter, metadata, sqladapter);

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			if (isDistinct && !distinctResultSet) {
				sb.append("DISTINCT ");
			}

			List<String> select;
			if (!signature.isEmpty()) {
				List<ImmutableTerm> terms = cq.getHead().getTerms().stream()
						.map(immutabilityTools::convertIntoImmutableTerm)
						.collect(Collectors.toList());
				select = Lists.newArrayListWithCapacity(signature.size());
				for (int i = 0; i < signature.size(); i++) {
					select.add(getSelectClauseFragment(signature.get(i), terms.get(i), index, sqladapter));
				}
			}
			else {
				select = ImmutableList.of("'true' AS x"); // Only for ASK
			}
			Joiner.on(", ").appendTo(sb, select);

			List<Function> body = cq.getBody();
			sb.append("\nFROM \n").append(INDENT);
			List<String> from = getTableDefs(body, index, INDENT, sqladapter);
			if (from.isEmpty()) {
				from = ImmutableList.of(inBrackets(sqladapter.getDummyTable()) + " tdummy");
			}
			Joiner.on(",\n" + INDENT).appendTo(sb, from);

			Set<String> where = getConditionsSet(body, index, false, sqladapter);
			if (!where.isEmpty()) {
				sb.append("\nWHERE \n").append(INDENT);
				Joiner.on(" AND\n" + INDENT).appendTo(sb, where);
			}

			ImmutableList<QualifiedAttributeID> groupBy = getGroupBy(body, index);
			if (!groupBy.isEmpty()) {
				sb.append("\nGROUP BY ");
				Joiner.on(", ").appendTo(sb, groupBy.stream()
						.map(QualifiedAttributeID::getSQLRendering)
						.collect(ImmutableCollectors.toList()));
			}

			ImmutableList<Function> having = getHaving(body);
			if (!having.isEmpty()) {
				sb.append("\nHAVING (");
				Joiner.on(" AND ").appendTo(sb, getBooleanConditions(having, index, sqladapter));
				sb.append(") ");
			}

			sqls.add(sb.toString());
		}
		String sql = sqls.size() == 1
				? sqls.get(0)
				: inBrackets(Joiner.on(")\n " + (unionNoDuplicates ? "UNION" : "UNION ALL") + "\n (").join(sqls));
		return new SQLAndColumnNames(sql, signature.stream()
				.map(e -> e.columnAlias)
				.collect(ImmutableCollectors.toList()));
	}


	private ImmutableList<Function> convert(List<Term> terms) {
		return terms.stream().map(c -> (Function)c).collect(ImmutableCollectors.toList());
	}

	private ImmutableList<Function> getHaving(List<Function> body) {
		for (Function atom : body) {
			if (atom.getFunctionSymbol().equals(datalogFactory.getSparqlHavingPredicate())) {
				return convert(atom.getTerms());
			}
		}
		return ImmutableList.of();
	}

	private ImmutableList<QualifiedAttributeID> getGroupBy(List<Function> body, AliasIndex index) {
		return body.stream()
				.filter(a -> a.getFunctionSymbol().equals(datalogFactory.getSparqlGroupPredicate()))
				.map(Function::getVariables)
				.flatMap(Collection::stream)
				.map(index::getColumns)
				.flatMap(Collection::stream)
				.collect(ImmutableCollectors.toList());
	}

	private RelationID createAlias(String predicateName, String suffix, Collection<RelationID> usedAliases,
								   SQLDialectAdapter sqladapter, QuotedIDFactory idFactory) {
		// escapes the predicate name
		String safePredicateName = predicateName
				.replace('.', '_')
				.replace(':', '_')
				.replace('/', '_')
				.replace(' ', '_');
		String alias = sqladapter.nameView(VIEW_PREFIX, safePredicateName, suffix, usedAliases);
		return idFactory.createRelationID(null, alias);
	}

	/**
	 * Returns a string with boolean conditions formed with the boolean atoms
	 * found in the atoms list.
	 */
	private Set<String> getBooleanConditions(List<Function> atoms, AliasIndex index, SQLDialectAdapter sqladapter) {
		Set<String> conditions = new LinkedHashSet<>();
		for (Function atom : atoms) {
			if (atom.isOperation()) {  // Boolean expression
				ImmutableFunctionalTerm functionalTerm =  (ImmutableFunctionalTerm)
						immutabilityTools.convertIntoImmutableTerm(atom);
				String condition = getSQLCondition(functionalTerm, index, sqladapter);
				conditions.add(condition);
			}
		}
		return conditions;
	}

	/**
	 * Returns the SQL for an atom representing an SQL condition (booleans).
	 */
	private String getSQLCondition(ImmutableFunctionalTerm atom, AliasIndex index, SQLDialectAdapter sqladapter) {
		Predicate functionSymbol = atom.getFunctionSymbol();
		if (functionSymbol instanceof DBBooleanFunctionSymbol) {
			return ((DBFunctionSymbol) functionSymbol).getNativeDBString(atom.getTerms(),
					// TODO: try to get rid of useBrackets
					t -> getSQLString(t, index, false, sqladapter), termFactory);
		}

		throw new RuntimeException("The builtin function " + functionSymbol + " is not supported yet!");
	}

	private ImmutableList<String> getTableDefs(List<Function> atoms, AliasIndex index, String indent, SQLDialectAdapter sqladapter) {
		return atoms.stream()
				.map(a -> getTableDefinition(a, index, indent, sqladapter))
				.filter(Objects::nonNull)
				.collect(ImmutableCollectors.toList());
	}

	/**
	 * Returns the table definition for these atoms. By default, a list of atoms
	 * represents JOIN or LEFT JOIN of all the atoms, left to right. All boolean
	 * atoms in the list are considered conditions in the ON clause of the JOIN.
	 *
	 * <p>
	 * If the list is a LeftJoin, then it can only have 2 data atoms, and it HAS
	 * to have 2 data atoms.
	 *
	 * <p>
	 * If process boolean operators is enabled, all boolean conditions will be
	 * added to the ON clause of the first JOIN.
	 *
	 * @param atoms
	 * @param index
	 * @param JOIN_KEYWORD
	 * @param parenthesis
	 * @param indent
	 *
	 * @param sqladapter
	 * @return
	 */
	private String getTableDefinitions(List<Function> atoms,
									   AliasIndex index,
									   String JOIN_KEYWORD,
									   boolean parenthesis,
									   String indent, SQLDialectAdapter sqladapter) {

		List<String> tables = getTableDefs(atoms, index, INDENT + indent, sqladapter);
		switch (tables.size()) {
			case 0:
				throw new RuntimeException("Cannot generate definition for empty data");

			case 1:
				return tables.get(0);

			default:
				String JOIN = "%s\n" + indent + JOIN_KEYWORD + "\n" + INDENT + indent + "%s";
				/*
		 		 * Now we generate the table definition: Join/LeftJoin
				 * (possibly nested if there are more than 2 table definitions in the
				 * current list) in case this method was called recursively.
				 *
				 * To form the JOIN we will cycle through each data definition,
				 * nesting the JOINs as we go. The conditions in the ON clause will
				 * go on the TOP level only.
				 */
				int size = tables.size();
				String currentJoin = tables.get(size - 1);

				currentJoin = String.format(JOIN, tables.get(size - 2),
						parenthesis ? inBrackets(currentJoin) : currentJoin);

				for (int i = size - 3; i >= 0; i--) {
					currentJoin = String.format(JOIN, tables.get(i), inBrackets(currentJoin));
				}

				Set<String> on = getConditionsSet(atoms, index, true, sqladapter);

				if (on.isEmpty())
					return currentJoin;

				StringBuilder sb = new StringBuilder();
				sb.append(currentJoin).append("\n").append(indent).append("ON ");
				Joiner.on(" AND\n" + indent).appendTo(sb, on);
				return sb.toString();
		}
	}

	/**
	 * Returns the table definition for the given atom. If the atom is a simple
	 * table or view, then it returns the value as defined by the
	 * AliasIndex. If the atom is a Join or Left Join, it will call
	 * getTableDefinitions on the nested term list.
	 */
	private String getTableDefinition(Function atom, AliasIndex index, String indent, SQLDialectAdapter sqladapter) {

		if (atom.isAlgebraFunction()) {
			Predicate functionSymbol = atom.getFunctionSymbol();
			ImmutableList<Function> joinAtoms = convert(atom.getTerms());
			if (functionSymbol.equals(datalogFactory.getSparqlJoinPredicate())) {
				// nested joins we need to add parenthesis later
				boolean parenthesis = joinAtoms.get(0).isAlgebraFunction()
						|| joinAtoms.get(1).isAlgebraFunction();

				return getTableDefinitions(joinAtoms, index,
						"JOIN", parenthesis, indent + INDENT, sqladapter);
			}
			else if (functionSymbol.equals(datalogFactory.getSparqlLeftJoinPredicate())) {
				// in case of left join we want to add the parenthesis only for the right tables
				// we ignore nested joins from the left tables
				boolean parenthesis = joinAtoms.get(1).isAlgebraFunction();

				return getTableDefinitions(joinAtoms, index,
						"LEFT OUTER JOIN", parenthesis, indent + INDENT, sqladapter);
			}
		}
		else if (!atom.isOperation()) {
			return index.getViewDefinition(atom);  // a database atom
		}
		return null;
	}

	/**
	 * Generates all the conditions on the given atoms, e.g., shared variables
	 * and boolean conditions. This string can then be used to form a WHERE or
	 * an ON clause.
	 *
	 * <p>
	 * The method assumes that no variable in this list (or nested ones) referes
	 * to an upper level one.
	 */
	private Set<String> getConditionsSet(List<Function> atoms, AliasIndex index, boolean processShared, SQLDialectAdapter sqladapter) {

		Set<String> conditions = new LinkedHashSet<>();
		if (processShared) {
			// guohui: After normalization, do we have shared variables?
			// TODO: should we remove this ??
			Set<Variable> currentLevelVariables = new LinkedHashSet<>();
			for (Function atom : atoms) {
	 			// assume that no variables are shared across deeper levels of
	 			// nesting (through Join or LeftJoin atoms), it will not call itself
	 			// recursively. Nor across upper levels.
				collectVariableReferencesWithLeftJoin(currentLevelVariables, atom);
			}
			Set<String> conditionsSharedVariables = getConditionsSharedVariables(currentLevelVariables, index);
			conditions.addAll(conditionsSharedVariables);
		}

		Set<String> eqConstants = getEqConditionsForConstants(atoms, index, sqladapter);
		conditions.addAll(eqConstants);

		Set<String> booleanConditions = getBooleanConditions(atoms, index, sqladapter);
		conditions.addAll(booleanConditions);

		return conditions;
	}

	/**
	 * Collects (recursively) the set of variables that participate in data atoms
	 * (either in this atom directly or in nested ones, excluding those on the
	 * right-hand side of left joins.
	 *
	 * @param vars
	 * @param atom
	 * @return
	 */
	private void collectVariableReferencesWithLeftJoin(Set<Variable> vars, Function atom) {
		if (atom.isDataFunction()) {
			TermUtils.addReferencedVariablesTo(vars, atom);
		}
		else if (atom.isAlgebraFunction()) {
			Predicate functionSymbol = atom.getFunctionSymbol();
			if (functionSymbol.equals(datalogFactory.getSparqlJoinPredicate())) {
				// if it's a join, we need to collect all the variables of each nested atom
				convert(atom.getTerms()).stream()
						.filter(f -> !f.isOperation())
						.forEach(f -> collectVariableReferencesWithLeftJoin(vars, f));
			}
			else if (functionSymbol.equals(datalogFactory.getSparqlLeftJoinPredicate())) {
				// if it's a left join, only of the first data/algebra atom (the left atom)
				collectVariableReferencesWithLeftJoin(vars, (Function) atom.getTerm(0));
			}
		}
	}

	/**
	 * Returns a list of equality conditions that reflect the semantics of the
	 * shared variables in the list of atoms.
	 * <p>
	 * When generating equalities recursively, we will also generate a minimal
	 * number of equalities. E.g., if we have A(x), Join(R(x,y), Join(R(y,
	 * x),B(x))
	 *
	 */
	private Set<String> getConditionsSharedVariables(Set<Variable> vars, AliasIndex index) {
		/*
		 * For each variable we collect all the columns that should be equated
		 * (due to repeated positions of the variable)
		 * then we create atoms of the form "COL1 = COL2"
		 */
		Set<String> equalities = new LinkedHashSet<>();
		for (Variable var : vars) {
			Set<QualifiedAttributeID> columns = index.getColumns(var);
			if (columns.size() >= 2) {
				// if 1, then no need for equality
				Iterator<QualifiedAttributeID> iterator = columns.iterator();
				QualifiedAttributeID leftColumn = iterator.next();
				while (iterator.hasNext()) {
					QualifiedAttributeID rightColumn = iterator.next();
					String equality = String.format("(%s = %s)",
							leftColumn.getSQLRendering(),
							rightColumn.getSQLRendering());
					equalities.add(equality);
					leftColumn = rightColumn;
				}
			}
		}
		return equalities;
	}

	private Set<String> getEqConditionsForConstants(List<Function> atoms, AliasIndex index, SQLDialectAdapter sqladapter) {
		Set<String> equalities = new LinkedHashSet<>();
		for (Function atom : atoms) {
			if (atom.isDataFunction())  {
				for (int i = 0; i < atom.getArity(); i++) {
					Term t = atom.getTerm(i);
					if (t instanceof Constant) {
						String value = getSQLString((Constant)t, index, false, sqladapter);
						QualifiedAttributeID column = index.getColumn(atom, i);
						equalities.add(String.format("(%s = %s)", column.getSQLRendering(), value));
					}
				}
			}
		}
		return equalities;
	}

	// Use string instead
	@Deprecated
	private static final class SignatureVariable {
		private final String columnAlias;
		SignatureVariable(String columnAlias) {
			this.columnAlias = columnAlias;
		}
	}

	/**
	 * produces the select clause of the sql query for the given CQIE
	 *
	 * @return the sql select clause
	 */
	private String getSelectClauseFragment(SignatureVariable var,
										   ImmutableTerm term,
										   AliasIndex index, SQLDialectAdapter sqladapter) {
		String mainColumn = getMainColumnForSELECT(term, index, sqladapter);

		return "\n   " + mainColumn + " AS " + var.columnAlias;
	}

	private ImmutableList<SignatureVariable> createSignature(List<String> names, SQLDialectAdapter sqladapter) {
		/*
		 * Set that contains all the variable names created on the top query.
		 * It helps the dialect adapter to generate variable names according to its possible restrictions.
		 * Currently, this is needed for the Oracle adapter (max. length of 30 characters).
		 */
		Set<String> columnAliases = new HashSet<>();
		ImmutableList.Builder<SignatureVariable> builder = ImmutableList.builder();
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);

			String mainAlias = sqladapter.nameTopVariable(name, columnAliases);
			columnAliases.add(mainAlias);

			builder.add(new SignatureVariable(mainAlias));
		}
		return builder.build();
	}

	private String getMainColumnForSELECT(ImmutableTerm ht, AliasIndex index, SQLDialectAdapter sqladapter) {

		return getSQLString(ht, index, false, sqladapter);
	}

	/**
	 * Generates the SQL string that forms or retrieves the given term. The
	 * function takes as input either: a constant (value or URI), a variable, or
	 * a Function (i.e., uri(), eq(..), ISNULL(..), etc)).
	 * <p>
	 * If the input is a constant, it will return the SQL that generates the
	 * string representing that constant.
	 * <p>
	 * If its a variable, it returns the column references to the position where
	 * the variable first appears.
	 * <p>
	 * If its a function uri(..) it returns the SQL string concatenation that
	 * builds the result of uri(...)
	 * <p>
	 * If its a boolean comparison, it returns the corresponding SQL comparison.
	 */
	private String getSQLString(ImmutableTerm term, AliasIndex index, boolean useBrackets, SQLDialectAdapter sqladapter) {

		if (term == null) {
			return "";
		}
		if (term instanceof Constant) {
			if (term.isNull())
				return ((Constant) term).getValue();
			if (!(term instanceof DBConstant)) {
				throw new MinorOntopInternalBugException("Only DBConstants or NULLs are expected in sub-tree " +
						"to be translated into SQL. Found: " + term);
			}
			return sqladapter.render((DBConstant) term);
		}
		else if (term instanceof Variable) {
			Set<QualifiedAttributeID> columns = index.getColumns((Variable) term);
			return columns.iterator().next().getSQLRendering();
		}

		// If it's not constant, or variable it's a function
		ImmutableFunctionalTerm function = (ImmutableFunctionalTerm) term;
		Predicate functionSymbol = function.getFunctionSymbol();

		if (functionSymbol instanceof DBFunctionSymbol) {
			return ((DBFunctionSymbol) functionSymbol).getNativeDBString(
					function.getTerms(),
                    // TODO: try to get rid of useBrackets
                    t -> getSQLString(t, index, false, sqladapter), termFactory);
		}

		throw new RuntimeException("Unexpected function in the query: " + functionSymbol);
	}

	private static final class FromItem {
		private final RelationID alias;
		private final String definition;
		private final ImmutableList<QualifiedAttributeID> attributes;

		FromItem(RelationID alias, String definition, ImmutableList<QualifiedAttributeID> attributes) {
			this.alias = alias;
			this.definition = definition;
			this.attributes = attributes;
		}
		@Override
		public String toString() {
			return alias + " " + definition;
		}
	}

	/**
	 * Utility class to resolve "database" atoms to view definitions ready to be
	 * used in a FROM clause, and variables, to column references defined over
	 * the existing view definitions of a query.
	 */
	public final class AliasIndex {

		final Map<Function, FromItem> fromItemsForAtoms = new HashMap<>();
		final Map<RelationID, FromItem> subQueryFromItems = new HashMap<>();
		final Map<Variable, Set<QualifiedAttributeID>> columnsForVariables = new HashMap<>();
		final Map<RelationID, RelationDefinition> relationsForAliases = new HashMap<>();
		private final AtomicInteger viewCounter;
		private final DBMetadata metadata;
		private final SQLDialectAdapter sqladapter;

		AliasIndex(CQIE query, ImmutableMap<Predicate, FromItem> subQueryDefinitions, AtomicInteger viewCounter,
				   DBMetadata metadata, SQLDialectAdapter sqladapter) {
			this.viewCounter = viewCounter;
			this.metadata = metadata;
			this.sqladapter = sqladapter;
			for (Function atom : query.getBody()) {
				// This will be called recursively if necessary
				generateViewsIndexVariables(atom, subQueryDefinitions, metadata, sqladapter);
			}
		}

		/***
		 * We associate each atom to a view definition. This will be
		 * <p>
		 * "tablename" as "viewX" or
		 * <p>
		 * (some nested sql view) as "viewX"
		 *
		 * <p>
		 * View definitions are only done for data atoms. Join/LeftJoin and
		 * boolean atoms are not associated to view definitions.
		 * @param atom
		 * @param subQueryDefinitions
		 * @param metadata
		 * @param sqladapter
		 */
		private void generateViewsIndexVariables(Function atom,
												 ImmutableMap<Predicate, FromItem> subQueryDefinitions,
												 DBMetadata metadata, SQLDialectAdapter sqladapter) {
			if (atom.isOperation()) {
				return;
			}
			else if (atom.isAlgebraFunction()) {
				for (Term subatom : atom.getTerms()) {
					if (subatom instanceof Function) {
						generateViewsIndexVariables((Function) subatom, subQueryDefinitions, metadata, sqladapter);
					}
				}
			}

			Predicate predicate = atom.getFunctionSymbol();
			boolean isSubquery = subQueryDefinitions.containsKey(predicate);
			final FromItem fromItem;
			if (isSubquery) {
				fromItem = subQueryDefinitions.get(predicate);
				subQueryFromItems.put(fromItem.alias, fromItem);
			}
			else {
				RelationDefinition relation = metadata.getRelation(relation2Predicate.createRelationFromPredicateName(
						metadata.getQuotedIDFactory(), predicate));
				if (relation == null)
					return;   // because of dummyN - what exactly is that?

				RelationID relationAlias = createAlias(predicate.getName(),
						VIEW_SUFFIX + viewCounter.getAndIncrement(),
						fromItemsForAtoms.entrySet().stream()
								.map(e -> e.getValue().alias).collect(Collectors.toList()), sqladapter,
						metadata.getQuotedIDFactory());

				fromItem = new FromItem(
						relationAlias,
						relation instanceof DatabaseRelationDefinition
								? relation.getID().getSQLRendering()
								: inBrackets(((ParserViewDefinition)relation).getStatement()),
						relation.getAttributes().stream()
								.map(a -> new QualifiedAttributeID(relationAlias, a.getID()))
								.collect(ImmutableCollectors.toList()));

				relationsForAliases.put(fromItem.alias, relation);
			}
			fromItemsForAtoms.put(atom, fromItem);

			for (int i = 0; i < atom.getTerms().size(); i++) {
				Term term = atom.getTerms().get(i);
				if (term instanceof Variable) {
					Set<QualifiedAttributeID> columns = columnsForVariables.get(term);
					if (columns == null) {
						columns = new LinkedHashSet<>();
						columnsForVariables.put((Variable) term, columns);
					}
					columns.add(fromItem.attributes.get(i));
				}
			}
		}

		/**
		 * Returns all the column aliases that correspond to this variable,
		 * across all the DATA atoms in the query (not algebra operators or
		 * boolean conditions.
		 *
		 * @param var
		 *            The variable we want the referenced columns.
		 */
		Set<QualifiedAttributeID> getColumns(Variable var) {
			Set<QualifiedAttributeID> columns = columnsForVariables.get(var);
			if (columns == null || columns.isEmpty())
				throw new RuntimeException("Unbound variable found in WHERE clause: " + var);
			return columns;
		}

		/**
		 * Generates the view definition, i.e., "tablename viewname".
		 */
		String getViewDefinition(Function atom) {

			FromItem dd = fromItemsForAtoms.get(atom);
			if (dd != null) {
				return sqladapter.sqlTableName(dd.definition, dd.alias.getSQLRendering());
			}
			else if (atom.getArity() == 0) {
				 // Special case of nullary atoms
				return inBrackets(sqladapter.getDummyTable()) + " tdummy";
			}
			throw new RuntimeException(
						"Impossible to get data definition for: " + atom + ", type: " + dd);
		}

		QualifiedAttributeID getColumn(Function atom, int column) {
			FromItem dd = fromItemsForAtoms.get(atom);
			return dd.attributes.get(column);
		}
	}

	private static String inBrackets(String s) {
		return "(" + s + ")";
	}

	private static class SQLAndColumnNames {

		private final String string;
		private final ImmutableList<String> columnNames;

		private SQLAndColumnNames(String string, ImmutableList<String> columnNames) {
			this.string = string;
			this.columnNames = columnNames;
		}
	}
}
