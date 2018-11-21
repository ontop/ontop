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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLAdapterFactory;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl.DB2SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.utils.COL_TYPE;
import it.unibz.inf.ontop.answering.reformulation.generation.utils.XsdDatatypeConverter;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.iq.optimizer.PushDownBooleanExpressionOptimizer;
import it.unibz.inf.ontop.iq.optimizer.PushUpBooleanExpressionOptimizer;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.impl.TermUtils;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class generates an SQLExecutableQuery from the datalog program coming from the
 * unfolder.
 *
 * This class is NOT thread-safe (attributes values are query-dependent).
 * Thus, an instance of this class should NOT BE SHARED between QuestStatements but be DUPLICATED.
 *
 *
 * @author mrezk, mariano, guohui, roman
 *
 */
public class OneShotSQLGeneratorEngine {

	/**
	 * Formatting templates
	 */
    private static final String VIEW_PREFIX = "Q";
    private static final String VIEW_SUFFIX = "VIEW";
    private static final String VIEW_ANS_SUFFIX = "View";
	private static final String OUTER_VIEW_NAME = "SUB_QVIEW";

	private static final String INDENT = "    ";

	private final RDBMetadata metadata;
	private final QuotedIDFactory idFactory;
	private final SQLDialectAdapter sqladapter;
	private final IQ2DatalogTranslator iq2DatalogTranslator;

	private final boolean distinctResultSet;
	private final boolean isIRISafeEncodingEnabled;

	@Nullable
	private final IRIDictionary uriRefIds;

	private final ImmutableMap<FunctionSymbol, String> operations;

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(OneShotSQLGeneratorEngine.class);
	private final JdbcTypeMapper jdbcTypeMapper;
	private final Relation2Predicate relation2Predicate;
	private final DatalogNormalizer datalogNormalizer;
	private final DatalogFactory datalogFactory;
	private final TypeFactory typeFactory;
	private final TermFactory termFactory;
	private final IQConverter iqConverter;
	private final AtomFactory atomFactory;
	private final UnionFlattener unionFlattener;
	private final PushDownBooleanExpressionOptimizer pushDownExpressionOptimizer;
	private final IntermediateQueryFactory iqFactory;
	private final OptimizerFactory optimizerFactory;
	private final PushUpBooleanExpressionOptimizer pullUpExpressionOptimizer;
	private final ImmutabilityTools immutabilityTools;
	private final UniqueTermTypeExtractor uniqueTermTypeExtractor;
	private final PostProcessingProjectionSplitter projectionSplitter;


	// the only two mutable (query-dependent) fields
	private boolean isDistinct = false;
	private boolean isOrderBy = false;


	OneShotSQLGeneratorEngine(DBMetadata metadata,
							  IRIDictionary iriDictionary,
							  OntopReformulationSQLSettings settings,
							  JdbcTypeMapper jdbcTypeMapper,
							  IQ2DatalogTranslator iq2DatalogTranslator,
							  Relation2Predicate relation2Predicate,
							  DatalogNormalizer datalogNormalizer, DatalogFactory datalogFactory,
							  TypeFactory typeFactory, TermFactory termFactory, IQConverter iqConverter,
							  AtomFactory atomFactory, UnionFlattener unionFlattener,
							  PushDownBooleanExpressionOptimizer pushDownExpressionOptimizer,
							  IntermediateQueryFactory iqFactory, OptimizerFactory optimizerFactory,
							  PushUpBooleanExpressionOptimizer pullUpExpressionOptimizer, ImmutabilityTools immutabilityTools, UniqueTermTypeExtractor uniqueTermTypeExtractor, PostProcessingProjectionSplitter projectionSplitter) {
		this.relation2Predicate = relation2Predicate;
		this.datalogNormalizer = datalogNormalizer;
		this.datalogFactory = datalogFactory;
		this.typeFactory = typeFactory;
		this.termFactory = termFactory;
		this.iqConverter = iqConverter;
		this.atomFactory = atomFactory;
		this.unionFlattener = unionFlattener;
		this.pushDownExpressionOptimizer = pushDownExpressionOptimizer;
		this.iqFactory = iqFactory;
		this.optimizerFactory = optimizerFactory;
		this.pullUpExpressionOptimizer = pullUpExpressionOptimizer;
		this.immutabilityTools = immutabilityTools;
		this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
		this.projectionSplitter = projectionSplitter;

		String driverURI = settings.getJdbcDriver();

		if (!(metadata instanceof RDBMetadata)) {
			throw new IllegalArgumentException("Not a DBMetadata!");
		}

		this.metadata = (RDBMetadata)metadata;
		this.idFactory = metadata.getQuotedIDFactory();
		this.sqladapter = SQLAdapterFactory.getSQLDialectAdapter(driverURI, this.metadata.getDbmsVersion(), settings);
		this.operations = buildOperations(sqladapter);
		this.distinctResultSet = settings.isDistinctPostProcessingEnabled();
		this.iq2DatalogTranslator = iq2DatalogTranslator;
		this.isIRISafeEncodingEnabled = settings.isIRISafeEncodingEnabled();
		this.uriRefIds = iriDictionary;
		this.jdbcTypeMapper = jdbcTypeMapper;
	}

	/**
	 * For clone purposes only
	 */
	private OneShotSQLGeneratorEngine(RDBMetadata metadata, SQLDialectAdapter sqlAdapter,
									  boolean isIRISafeEncodingEnabled, boolean distinctResultSet,
									  IRIDictionary uriRefIds, JdbcTypeMapper jdbcTypeMapper,
									  ImmutableMap<FunctionSymbol, String> operations,
									  IQ2DatalogTranslator iq2DatalogTranslator, Relation2Predicate relation2Predicate,
									  DatalogNormalizer datalogNormalizer, DatalogFactory datalogFactory,
									  TypeFactory typeFactory, TermFactory termFactory, IQConverter iqConverter,
									  AtomFactory atomFactory, UnionFlattener unionFlattener,
									  PushDownBooleanExpressionOptimizer pushDownExpressionOptimizer,
									  IntermediateQueryFactory iqFactory, OptimizerFactory optimizerFactory,
									  PushUpBooleanExpressionOptimizer pullUpExpressionOptimizer,
									  ImmutabilityTools immutabilityTools,
									  UniqueTermTypeExtractor uniqueTermTypeExtractor, PostProcessingProjectionSplitter projectionSplitter) {
		this.metadata = metadata;
		this.idFactory = metadata.getQuotedIDFactory();
		this.sqladapter = sqlAdapter;
		this.operations = operations;
		this.isIRISafeEncodingEnabled = isIRISafeEncodingEnabled;
		this.distinctResultSet = distinctResultSet;
		this.uriRefIds = uriRefIds;
		this.jdbcTypeMapper = jdbcTypeMapper;
		this.iq2DatalogTranslator = iq2DatalogTranslator;
		this.relation2Predicate = relation2Predicate;
		this.datalogNormalizer = datalogNormalizer;
		this.datalogFactory = datalogFactory;
		this.typeFactory = typeFactory;
		this.termFactory = termFactory;
		this.iqConverter = iqConverter;
		this.atomFactory = atomFactory;
		this.unionFlattener = unionFlattener;
		this.pushDownExpressionOptimizer = pushDownExpressionOptimizer;
		this.iqFactory = iqFactory;
		this.optimizerFactory = optimizerFactory;
		this.pullUpExpressionOptimizer = pullUpExpressionOptimizer;
		this.immutabilityTools = immutabilityTools;
		this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
		this.projectionSplitter = projectionSplitter;
	}

	private static ImmutableMap<FunctionSymbol, String> buildOperations(SQLDialectAdapter sqladapter) {
		ImmutableMap.Builder<FunctionSymbol, String> builder = new ImmutableMap.Builder<FunctionSymbol, String>()
				.put(ExpressionOperation.ADD, "%s + %s")
				.put(ExpressionOperation.SUBTRACT, "%s - %s")
				.put(ExpressionOperation.MULTIPLY, "%s * %s")
				.put(ExpressionOperation.DIVIDE, "(1.0 * %s) / %s")
				.put(ExpressionOperation.ABS, "ABS(%s)")
				.put(ExpressionOperation.CEIL, sqladapter.ceil())
				.put(ExpressionOperation.FLOOR, "FLOOR(%s)")
				.put(ExpressionOperation.ROUND, sqladapter.round())
				.put(ExpressionOperation.RAND, sqladapter.rand())
				.put(BooleanExpressionOperation.EQ, "%s = %s")
				.put(BooleanExpressionOperation.NEQ, "%s <> %s")
				.put(BooleanExpressionOperation.GT, "%s > %s")
				.put(BooleanExpressionOperation.GTE, "%s >= %s")
				.put(BooleanExpressionOperation.LT, "%s < %s")
				.put(BooleanExpressionOperation.LTE, "%s <= %s")
				.put(BooleanExpressionOperation.AND, "%s AND %s")
				.put(BooleanExpressionOperation.OR, "%s OR %s")
				.put(BooleanExpressionOperation.NOT, "NOT %s")
				.put(BooleanExpressionOperation.IS_NULL, "%s IS NULL")
				.put(BooleanExpressionOperation.IS_NOT_NULL, "%s IS NOT NULL")
				//.put(ExpressionOperation.IS_TRUE, "%s IS TRUE")
				.put(BooleanExpressionOperation.SQL_LIKE, "%s LIKE %s")
				.put(BooleanExpressionOperation.STR_STARTS, sqladapter.strStartsOperator())
				.put(BooleanExpressionOperation.STR_ENDS, sqladapter.strEndsOperator())
				.put(BooleanExpressionOperation.CONTAINS, sqladapter.strContainsOperator())
				.put(ExpressionOperation.NOW, sqladapter.dateNow());

		try {
			builder.put(ExpressionOperation.STRUUID, sqladapter.strUuid());
		} catch (UnsupportedOperationException e) {
			// ignore
		}
		try {
			builder.put(ExpressionOperation.UUID, sqladapter.uuid());
		} catch (UnsupportedOperationException e) {
			// ignore
		}
		return builder.build();
	}

	/**
	 * SQLGenerator must not be shared between threads but CLONED.
	 *
	 * roman: this is an incorrect way of overriding clone - see, e.g.,
	 * https://docs.oracle.com/javase/tutorial/java/IandI/objectclass.html
	 *
	 * @return A cloned object without any query-dependent value
	 */
	@Override
	public OneShotSQLGeneratorEngine clone() {
		return new OneShotSQLGeneratorEngine(metadata, sqladapter,
				isIRISafeEncodingEnabled, distinctResultSet, uriRefIds, jdbcTypeMapper, operations, iq2DatalogTranslator,
                relation2Predicate, datalogNormalizer, datalogFactory,
                typeFactory, termFactory, iqConverter, atomFactory, unionFlattener, pushDownExpressionOptimizer, iqFactory, optimizerFactory, pullUpExpressionOptimizer, immutabilityTools, uniqueTermTypeExtractor, projectionSplitter);
	}

	/**
	 * Generates and SQL query ready to be executed by Quest. Each query is a
	 * SELECT FROM WHERE query. To know more about each of these see the inner
	 * method descriptions.
	 * Observe that the SQL is produced by {@link #generateQuery}
	 *
	 * @param initialIQ
	 */
	public IQ generateSourceQuery(IQ initialIQ, ExecutorRegistry executorRegistry)
			throws OntopReformulationException {

		PostProcessingProjectionSplitter.PostProcessingSplit split = projectionSplitter.split(initialIQ);

		/*
		 * Only the SUB-tree is translated into SQL
		 */
		IQTree normalizedSubTree = normalizeSubTree(split.getSubTree(), split.getVariableGenerator(), executorRegistry);
		ImmutableList<Variable> childSignature = ImmutableList.copyOf(normalizedSubTree.getVariables());

		DatalogProgram queryProgram = iq2DatalogTranslator.translate(normalizedSubTree, childSignature);

		for (CQIE cq : queryProgram.getRules()) {
			datalogNormalizer.addMinimalEqualityToLeftOrNestedInnerJoin(cq);
		}
		log.debug("Program normalized for SQL translation:\n" + queryProgram);

		MutableQueryModifiers queryModifiers = queryProgram.getQueryModifiers();
		isDistinct = queryModifiers.hasModifiers() && queryModifiers.isDistinct();
		isOrderBy = queryModifiers.hasModifiers() && !queryModifiers.getSortConditions().isEmpty();

		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(queryProgram.getRules());
		Multimap<Predicate, CQIE> ruleIndex = depGraph.getRuleIndex();
		List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();
		List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();

		final String resultingQuery;
		String queryString = generateQuery(childSignature.stream()
				.map(Variable::getName)
				.collect(ImmutableCollectors.toList()),
				ruleIndex, predicatesInBottomUp, extensionalPredicates);
		if (queryModifiers.hasModifiers()) {
			//List<Variable> groupby = queryProgram.getQueryModifiers().getGroupConditions();
			// if (!groupby.isEmpty()) {
			// subquery += "\n" + sqladapter.sqlGroupBy(groupby, "") + " " +
			// havingStr + "\n";
			// }
			// List<OrderCondition> conditions =
			// query.getQueryModifiers().getSortConditions();

			long limit = queryModifiers.getLimit();
			long offset = queryModifiers.getOffset();
			List<OrderCondition> conditions = queryModifiers.getSortConditions();

			final String modifier;
			if (!conditions.isEmpty()) {
				modifier = sqladapter.sqlOrderByAndSlice(conditions, OUTER_VIEW_NAME, limit, offset) + "\n";
			}
			else if (limit != -1 || offset != -1) {
				modifier = sqladapter.sqlSlice(limit, offset) + "\n";
			}
			else {
				modifier = "";
			}

			resultingQuery = "SELECT *\n" +
					"FROM " + inBrackets("\n" + queryString + "\n") + " " + OUTER_VIEW_NAME + "\n" +
					modifier;
		}
		else {
			resultingQuery = queryString;
		}

		ImmutableMap<Variable, DBTermType> variableTypeMap = extractVariableTypeMap(normalizedSubTree);

		NativeNode nativeNode = iqFactory.createNativeNode(variableTypeMap, resultingQuery,
				normalizedSubTree.getVariableNullability());
		UnaryIQTree newTree = iqFactory.createUnaryIQTree(split.getPostProcessingConstructionNode(), nativeNode);

		return iqFactory.createIQ(initialIQ.getProjectionAtom(), newTree);
	}

	private IQTree normalizeSubTree(IQTree subTree, VariableGenerator variableGenerator, ExecutorRegistry executorRegistry) {

		IQTree flattenSubTree = unionFlattener.optimize(subTree, variableGenerator);
		log.debug("New query after flattening the union: \n" + flattenSubTree);

		// Just here for converting the IQTree into an IntermediateQuery (will be ignored later on)
		DistinctVariableOnlyDataAtom temporaryProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
				atomFactory.getRDFAnswerPredicate(flattenSubTree.getVariables().size()),
				ImmutableList.copyOf(flattenSubTree.getVariables()));

		try {
			IQTree treeAfterPullOut = optimizerFactory.createEETransformer(variableGenerator).transform(flattenSubTree);
			log.debug("Query tree after pulling out equalities: \n" + treeAfterPullOut);


			IQ pulledOutSubQuery = iqFactory.createIQ(temporaryProjectionAtom, treeAfterPullOut);

            // Trick for pushing down expressions under unions:
            //   - there the context may be concrete enough for evaluating certain expressions
            //   - useful for dealing with SPARQL EBVs for instance
            IntermediateQuery pushedDownQuery = pushDownExpressionOptimizer.optimize(
            		iqConverter.convert(pulledOutSubQuery, metadata, executorRegistry));
            log.debug("New query after pushing down the boolean expressions (temporary): \n" + pushedDownQuery);


			// Pulling up is needed when filtering conditions appear above a data atom on the left
			// (causes problems to the IQ2DatalogConverter)
			IntermediateQuery queryAfterPullUp = pullUpExpressionOptimizer.optimize(pushedDownQuery);
			log.debug("New query after pulling up the boolean expressions: \n" + queryAfterPullUp);

			return iqConverter.convert(queryAfterPullUp).getTree();
		} catch (EmptyQueryException e) {
			// Not expected
			throw new MinorOntopInternalBugException(e.getMessage());
		}
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
	 * Generates the full SQL query.
	 * An important part of this program is {@link #generateQueryFromRules}
	 * that will create a view for every ans predicate in the input Datalog program.
	 *
	 * @param signature is the list of main columns in the ResultSet
	 * @param ruleIndex maps intentional predicates to its rules
	 * @param predicatesInBottomUp the topologically ordered predicates in the program
	 * @param extensionalPredicates are the predicates that are not defined by any rule
	 * @return
	 */
	private String generateQuery(List<String> signature,
								 Multimap<Predicate, CQIE> ruleIndex,
								 List<Predicate> predicatesInBottomUp,
								 List<Predicate> extensionalPredicates) throws OntopReformulationException {

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
				ImmutableList<SignatureVariable> s = createSignature(varListBuilder.build());

				// Creates the body of the subquery
				String subQuery = generateQueryFromRules(ruleIndex.get(pred), s,
						subQueryDefinitionsBuilder.build(), false, viewCounter);

				RelationID subQueryAlias = createAlias(pred.getName(), VIEW_ANS_SUFFIX, usedAliases);
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
		ImmutableList<SignatureVariable> topSignature = createSignature(signature);

		return generateQueryFromRules(ruleIndex.get(topLevelPredicate), topSignature,
				subQueryDefinitionsBuilder.build(), isDistinct && !distinctResultSet,
                viewCounter);
	}


	/**
	 * Takes a union of CQs and returns its SQL translation.
	 * It is a helper method for{@link #generateQuery}
	 *  @param cqs
	 * @param signature
	 * @param subQueryDefinitions
	 * @param unionNoDuplicates
	 * @param viewCounter
	 */
	private String generateQueryFromRules(Collection<CQIE> cqs,
                                          ImmutableList<SignatureVariable> signature,
                                          ImmutableMap<Predicate, FromItem> subQueryDefinitions,
                                          boolean unionNoDuplicates, AtomicInteger viewCounter) {

		List<String> sqls = Lists.newArrayListWithExpectedSize(cqs.size());
		for (CQIE cq : cqs) {
		    /* Main loop, constructing the SPJ query for each CQ */
			AliasIndex index = new AliasIndex(cq, subQueryDefinitions, viewCounter);

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			if (isDistinct && !distinctResultSet) {
				sb.append("DISTINCT ");
			}

			List<String> select;
			if (!signature.isEmpty()) {
				List<Term> terms = cq.getHead().getTerms();
				select = Lists.newArrayListWithCapacity(signature.size());
				for (int i = 0; i < signature.size(); i++) {
					select.add(getSelectClauseFragment(signature.get(i), terms.get(i), index));
				}
			}
			else {
				select = ImmutableList.of("'true' AS x"); // Only for ASK
			}
			Joiner.on(", ").appendTo(sb, select);

			List<Function> body = cq.getBody();
			sb.append("\nFROM \n").append(INDENT);
			List<String> from = getTableDefs(body, index, INDENT);
			if (from.isEmpty()) {
				from = ImmutableList.of(inBrackets(sqladapter.getDummyTable()) + " tdummy");
			}
			Joiner.on(",\n" + INDENT).appendTo(sb, from);

			Set<String> where = getConditionsSet(body, index, false);
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
				Joiner.on(" AND ").appendTo(sb, getBooleanConditions(having, index));
				sb.append(") ");
			}

			sqls.add(sb.toString());
		}
		return sqls.size() == 1
				? sqls.get(0)
				: inBrackets(Joiner.on(")\n " + (unionNoDuplicates ? "UNION" : "UNION ALL") + "\n (").join(sqls));
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

	private RelationID createAlias(String predicateName, String suffix, Collection<RelationID> usedAliases) {
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
	private Set<String> getBooleanConditions(List<Function> atoms, AliasIndex index) {
		Set<String> conditions = new LinkedHashSet<>();
		for (Function atom : atoms) {
			if (atom.isOperation()) {  // Boolean expression
				if (atom.getFunctionSymbol() == BooleanExpressionOperation.AND) {
					// flatten ANDs
					for (Term t : atom.getTerms()) {
						Set<String> arg = getBooleanConditions(ImmutableList.of((Function)t), index);
						conditions.addAll(arg);
					}
				}
				else {
					String condition = getSQLCondition(atom, index);
					conditions.add(condition);
				}
			}
		}
		return conditions;
	}

	/**
	 * Returns the SQL for an atom representing an SQL condition (booleans).
	 */
	private String getSQLCondition(Function atom, AliasIndex index) {
		Predicate functionSymbol = atom.getFunctionSymbol();
		if (operations.containsKey(functionSymbol)) {
			String expressionFormat = operations.get(functionSymbol);
			if (functionSymbol.getArity() == 1) {
				// For unary boolean operators, e.g., NOT, IS NULL, IS NOT NULL.
				Term term = atom.getTerm(0);
				final String arg;
				if (functionSymbol == BooleanExpressionOperation.NOT) {
					arg = getSQLString(term, index, false);
				}
				else {
					arg = getSQLString(term, index, false);
				}
				return String.format(expressionFormat, arg);
			}
			else if (functionSymbol.getArity() == 2) {
				// For binary boolean operators, e.g., AND, OR, EQ, GT, LT, etc.
				String left = getSQLString(atom.getTerm(0), index, true);
				String right = getSQLString(atom.getTerm(1), index, true);
				return String.format(inBrackets(expressionFormat), left, right);
			}
		}
		else if (functionSymbol == BooleanExpressionOperation.IS_TRUE) {
			return effectiveBooleanValue(atom.getTerm(0), index);
		}
		else if (functionSymbol == BooleanExpressionOperation.REGEX) {
			boolean caseinSensitive = false, multiLine = false, dotAllMode = false;
			if (atom.getArity() == 3) {
				String options = atom.getTerm(2).toString();
				caseinSensitive = options.contains("i");
				multiLine = options.contains("m");
				dotAllMode = options.contains("s");
			}
			String column = getSQLString(atom.getTerm(0), index, false);
			String pattern = getSQLString(atom.getTerm(1), index, false);
			return sqladapter.sqlRegex(column, pattern, caseinSensitive, multiLine, dotAllMode);
		}

		throw new RuntimeException("The builtin function " + functionSymbol + " is not supported yet!");
	}

	private ImmutableList<String> getTableDefs(List<Function> atoms, AliasIndex index, String indent) {
		return atoms.stream()
				.map(a -> getTableDefinition(a, index, indent))
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
	 * @return
	 */
	private String getTableDefinitions(List<Function> atoms,
									   AliasIndex index,
									   String JOIN_KEYWORD,
									   boolean parenthesis,
									   String indent) {

		List<String> tables = getTableDefs(atoms, index, INDENT + indent);
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

				Set<String> on = getConditionsSet(atoms, index, true);

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
	private String getTableDefinition(Function atom, AliasIndex index, String indent) {

		if (atom.isAlgebraFunction()) {
			Predicate functionSymbol = atom.getFunctionSymbol();
			ImmutableList<Function> joinAtoms = convert(atom.getTerms());
			if (functionSymbol.equals(datalogFactory.getSparqlJoinPredicate())) {
				// nested joins we need to add parenthesis later
				boolean parenthesis = joinAtoms.get(0).isAlgebraFunction()
						|| joinAtoms.get(1).isAlgebraFunction();

				return getTableDefinitions(joinAtoms, index,
						"JOIN", parenthesis, indent + INDENT);
			}
			else if (functionSymbol.equals(datalogFactory.getSparqlLeftJoinPredicate())) {
				// in case of left join we want to add the parenthesis only for the right tables
				// we ignore nested joins from the left tables
				boolean parenthesis = joinAtoms.get(1).isAlgebraFunction();

				return getTableDefinitions(joinAtoms, index,
						"LEFT OUTER JOIN", parenthesis, indent + INDENT);
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
	private Set<String> getConditionsSet(List<Function> atoms, AliasIndex index, boolean processShared) {

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

		Set<String> eqConstants = getEqConditionsForConstants(atoms, index);
		conditions.addAll(eqConstants);

		Set<String> booleanConditions = getBooleanConditions(atoms, index);
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

	private Set<String> getEqConditionsForConstants(List<Function> atoms, AliasIndex index) {
		Set<String> equalities = new LinkedHashSet<>();
		for (Function atom : atoms) {
			if (atom.isDataFunction())  {
				for (int i = 0; i < atom.getArity(); i++) {
					Term t = atom.getTerm(i);
					if (t instanceof Constant) {
						String value = getSQLString(t, index, false);
						QualifiedAttributeID column = index.getColumn(atom, i);
						equalities.add(String.format("(%s = %s)", column.getSQLRendering(), value));
					}
				}
			}
		}
		return equalities;
	}

	/**
	 * TODO: remove
	 */
	private String effectiveBooleanValue(Term term, AliasIndex index) {

		String column = getSQLString(term, index, false);
		// find data type of term and evaluate accordingly
		switch (getDataType(term)) {
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.DOUBLE:
			case Types.FLOAT:
				return String.format("%s != 0", column);
			case Types.VARCHAR:
				return String.format("LENGTH(%s) > 0", column);
			case Types.BOOLEAN:
				return column;
			default:
				return "1";
		}
	}

	// return the SQL data type
    // TODO: get rid of it
    @Deprecated
	private int getDataType(Term term) {
		if (term instanceof Function){
			Function functionalTerm = (Function) term;
			return Optional.of((ImmutableFunctionalTerm) immutabilityTools.convertIntoImmutableTerm(functionalTerm))
					.flatMap(ImmutableFunctionalTerm::inferType)
					.flatMap(TermTypeInference::getTermType)
					.map(jdbcTypeMapper::getSQLType)
					.orElse(Types.VARCHAR);
		}
        else if (term instanceof Variable) {
            throw new RuntimeException("Cannot return the SQL type for: " + term);
        }
		/*
		 * Boolean constant
		 */
		else if (term.equals(termFactory.getBooleanConstant(false))
				 || term.equals(termFactory.getBooleanConstant(true))) {
			return Types.BOOLEAN;
		}

		return Types.VARCHAR;
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
										   Term term,
										   AliasIndex index) {
		String mainColumn = getMainColumnForSELECT(term, index);

		return "\n   " + mainColumn + " AS " + var.columnAlias;
	}

	private ImmutableList<SignatureVariable> createSignature(List<String> names) {
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

	private String getMainColumnForSELECT(Term ht, AliasIndex index) {

		return getSQLString(ht, index, false);
	}


	private static final Pattern pQuotes = Pattern.compile("[\"`\\['][^\\.]*[\"`\\]']");

	private String getSQLStringForTemplateFunction(List<Term> terms, AliasIndex index) {

		// The first argument determines the form of the result
		Term term0 = terms.get(0);
		if (term0 instanceof RDFLiteralConstant || term0 instanceof BNode) {
			// An actual template: the first term is a string of the form
			// http://.../.../ or empty "{}" with placeholders of the form {}
			// The other terms are variables or constants that should replace
			// the placeholders. We need to tokenize and form the CONCAT
			String template = (term0 instanceof BNode)
					? ((BNode) term0).getName()   // getValue should be removed from Constant
					: ((RDFLiteralConstant) term0).getValue();
			// strip the template of all quotation marks (dubious step)
			while (pQuotes.matcher(template).matches()) {
				template = template.substring(1, template.length() - 1);
			}
			String[] split = template.split("[{][}]");

			List<String> vex = new ArrayList<>();
			if (split.length > 0 && !split[0].isEmpty()) { // fragment before the first {}
				vex.add(sqladapter.getSQLLexicalFormString(split[0]));
			}

			int size = terms.size();
			for (int i = 1; i < size; i++) {
				Term term = terms.get(i);
				String arg = getSQLString(term, index, false);
				String cast = isStringColType(term, index)
						? arg
						: sqladapter.sqlCast(arg, Types.VARCHAR);
				// empty placeholder: the correct uri is in the column of DB no need to replace
				vex.add((split.length > 0 && isIRISafeEncodingEnabled)
						? sqladapter.iriSafeEncode(cast)
						: cast);
				if (i < split.length) { // fragment after the current {} (if it exists)
					vex.add(sqladapter.getSQLLexicalFormString(split[i]));
				}
			}

			// if there is only one element there is nothing to concatenate
			return (vex.size() == 1) ? vex.get(0) : getStringConcatenation(vex.toArray(new String[0]));
		}
		else {
			// a concrete uri, a variable or a complex expression like in uri(CONCAT(x, "a"))
			// use the first term as the result string and ignore other terms
			return getSQLString(term0, index, false);
		}
	}

	// TODO: move to SQLAdapter
	private String getStringConcatenation(String[] params) {
		String toReturn = sqladapter.strConcat(params);
		if (sqladapter instanceof DB2SQLDialectAdapter) {
			/*
			 * A work around to handle DB2 (>9.1) issue SQL0134N: Improper use
			 * of a string column, host variable, constant, or function name.
			 * http
			 * ://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic
			 * =%2Fcom.ibm.db2.luw.messages.sql.doc%2Fdoc%2Fmsql00134n.html
			 */
			if (isDistinct || isOrderBy) {
				return sqladapter.sqlCast(toReturn, Types.VARCHAR);
			}
		}
		return toReturn;
	}

	private boolean isStringColType(Term term, AliasIndex index) {
		if (term instanceof Function) {
			Function function = (Function) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			if (functionSymbol instanceof IRIStringTemplateFunctionSymbol) {
				/*
				 * A URI function always returns a string, thus it is a string
				 * column type.
				 */
				return !hasIRIDictionary();
			}
			else {
				if (functionSymbol.getArity() == 1) {
					if (functionSymbol.getName().equals("Count")) {
						return false;
					}
					/*
					 * Update the term with the parent term's first parameter.
					 * Note: this method is confusing :(
					 */
					term = function.getTerm(0);
					return isStringColType(term, index);
				}
			}
		}
		else if (term instanceof Variable) {
			Set<QualifiedAttributeID> columns = index.getColumns((Variable) term);
			QualifiedAttributeID column0 = columns.iterator().next();

			RelationDefinition relation = index.relationsForAliases.get(column0.getRelation());
			if (relation != null) {
				QuotedID columnId = column0.getAttribute();
				for (Attribute a : relation.getAttributes()) {
					if (a.getID().equals(columnId)) {
						// TODO: check if it is ok to treat non-typed columns as string
						// (was the previous behavior)
						return !a.getTermType()
								.filter(t -> !t.isString())
								.isPresent();
					}
				}
			}
		}
		return false;
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
	private String getSQLString(Term term, AliasIndex index, boolean useBrackets) {

		if (term == null) {
			return "";
		}
		if (term instanceof RDFLiteralConstant) {
			RDFLiteralConstant ct = (RDFLiteralConstant) term;
			if (hasIRIDictionary()) {
				if (ct.getType().isA(XSD.STRING)) {
					int id = getUriid(ct.getValue());
					if (id >= 0)
						//return jdbcutil.getSQLLexicalForm(String.valueOf(id));
						return String.valueOf(id);
				}
			}
			return getSQLLexicalForm(ct);
		}
		else if (term instanceof IRIConstant) {
			IRIConstant uc = (IRIConstant) term;
			if (hasIRIDictionary()) {
				int id = getUriid(uc.getValue());
				return sqladapter.getSQLLexicalFormString(String.valueOf(id));
			}
			return sqladapter.getSQLLexicalFormString(uc.toString());
		}
		else if (term instanceof Variable) {
			Set<QualifiedAttributeID> columns = index.getColumns((Variable) term);
			return columns.iterator().next().getSQLRendering();
		}

		// If it's not constant, or variable it's a function
		Function function = (Function) term;
		Predicate functionSymbol = function.getFunctionSymbol();
		int size = function.getTerms().size();

		if (functionSymbol instanceof ObjectStringTemplateFunctionSymbol) {

		 	// The atom must be of the form uri("...", x, y)
			return getSQLStringForTemplateFunction(function.getTerms(), index);
		}
		if (operations.containsKey(functionSymbol)) {
			String expressionFormat = operations.get(functionSymbol);
			switch (function.getArity()) {
				case 0:
					return expressionFormat;
				case 1:
					// for unary functions, e.g., NOT, IS NULL, IS NOT NULL
					String arg = getSQLString(function.getTerm(0), index, true);
					return String.format(expressionFormat, arg);
				case 2:
					// for binary functions, e.g., AND, OR, EQ, NEQ, GT etc.
					String left = getSQLString(function.getTerm(0), index, true);
					String right = getSQLString(function.getTerm(1), index, true);
					String result = String.format(expressionFormat, left, right);
					return useBrackets ? inBrackets(result) : result;
				default:
					throw new RuntimeException("Cannot translate boolean function: " + functionSymbol);
			}
		}
		if (functionSymbol == BooleanExpressionOperation.IS_TRUE) {
			return effectiveBooleanValue(function.getTerm(0), index);
		}
		if (functionSymbol == BooleanExpressionOperation.REGEX) {
			boolean caseinSensitive = false, multiLine = false, dotAllMode = false;
			if (function.getArity() == 3) {
				String options = function.getTerm(2).toString();
				caseinSensitive = options.contains("i");
				multiLine = options.contains("m");
				dotAllMode = options.contains("s");
			}
			String column = getSQLString(function.getTerm(0), index, false);
			String pattern = getSQLString(function.getTerm(1), index, false);
			return sqladapter.sqlRegex(column, pattern, caseinSensitive, multiLine, dotAllMode);
		}
		/*
		 * TODO: make sure that SPARQL_LANG are eliminated earlier on
		 */
		if (functionSymbol == ExpressionOperation.SPARQL_LANG) {
			throw new RuntimeException("SPARQL_LANG is not supported by the SQL generator");
		}
		/*
		  TODO: replace by a switch
		 */
		if (functionSymbol.equals(ExpressionOperation.IF_ELSE_NULL)) {
			String condition = getSQLString(function.getTerm(0), index, false);
			String value = getSQLString(function.getTerm(1), index, false);
			return sqladapter.ifElseNull(condition, value);
		}
		if (functionSymbol == ExpressionOperation.QUEST_CAST) {
			String columnName = getSQLString(function.getTerm(0), index, false);
			String datatype = ((Constant) function.getTerm(1)).getValue();
			int sqlDatatype = datatype.equals(XMLSchema.STRING.stringValue())
					? Types.VARCHAR
					: Types.LONGVARCHAR;
			return isStringColType(function, index) ? columnName : sqladapter.sqlCast(columnName, sqlDatatype);
		}
		if (functionSymbol == ExpressionOperation.SPARQL_STR) {
			String columnName = getSQLString(function.getTerm(0), index, false);
			return isStringColType(function, index) ? columnName : sqladapter.sqlCast(columnName, Types.VARCHAR);
		}
		if (functionSymbol == ExpressionOperation.REPLACE) {
			String orig = getSQLString(function.getTerm(0), index, false);
			String out_str = getSQLString(function.getTerm(1), index, false);
			String in_str = getSQLString(function.getTerm(2), index, false);
			// TODO: handle flags
			return sqladapter.strReplace(orig, out_str, in_str);
		}
		if (functionSymbol == ExpressionOperation.CONCAT) {
			String left = getSQLString(function.getTerm(0), index, false);
			String right = getSQLString(function.getTerm(1), index, false);
			return sqladapter.strConcat(new String[]{left, right});
		}
		if (functionSymbol == ExpressionOperation.STRLEN) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.strLength(literal);
		}
		if (functionSymbol == ExpressionOperation.YEAR) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.dateYear(literal);
		}
		if (functionSymbol == ExpressionOperation.MINUTES) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.dateMinutes(literal);
		}
		if (functionSymbol == ExpressionOperation.DAY) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.dateDay(literal);
		}
		if (functionSymbol == ExpressionOperation.MONTH) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.dateMonth(literal);
		}
		if (functionSymbol == ExpressionOperation.SECONDS) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.dateSeconds(literal);
		}
		if (functionSymbol == ExpressionOperation.HOURS) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.dateHours(literal);
		}
		if (functionSymbol == ExpressionOperation.TZ) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.dateTZ(literal);
		}
		if (functionSymbol == ExpressionOperation.ENCODE_FOR_URI) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.iriSafeEncode(literal);
		}
		if (functionSymbol == ExpressionOperation.UCASE) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.strUcase(literal);
		}
		if (functionSymbol == ExpressionOperation.MD5) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.MD5(literal);
		}
		if (functionSymbol == ExpressionOperation.SHA1) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.SHA1(literal);
		}
		if (functionSymbol == ExpressionOperation.SHA256) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.SHA256(literal);
		}
		if (functionSymbol == ExpressionOperation.SHA512) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.SHA512(literal); //TODO FIX
		}
		if (functionSymbol == ExpressionOperation.LCASE) {
			String literal = getSQLString(function.getTerm(0), index, false);
			return sqladapter.strLcase(literal);
		}
		if (functionSymbol == ExpressionOperation.SUBSTR2) {
			String string = getSQLString(function.getTerm(0), index, false);
			String start = getSQLString(function.getTerm(1), index, false);
			return sqladapter.strSubstr(string, start);
		}
		if (functionSymbol == ExpressionOperation.SUBSTR3) {
			String string = getSQLString(function.getTerm(0), index, false);
			String start = getSQLString(function.getTerm(1), index, false);
			String end = getSQLString(function.getTerm(2), index, false);
			return sqladapter.strSubstr(string, start, end);
		}
		if (functionSymbol == ExpressionOperation.STRBEFORE) {
			String string = getSQLString(function.getTerm(0), index, false);
			String before = getSQLString(function.getTerm(1), index, false);
			return sqladapter.strBefore(string, before);
		}
		if (functionSymbol == ExpressionOperation.STRAFTER) {
			String string = getSQLString(function.getTerm(0), index, false);
			String after = getSQLString(function.getTerm(1), index, false);
			return sqladapter.strAfter(string, after);
		}
		if (functionSymbol == ExpressionOperation.COUNT) {
			if (function.getTerm(0).toString().equals("*")) {
				return "COUNT(*)";
			}
			String columnName = getSQLString(function.getTerm(0), index, false);
			//havingCond = true;
			return "COUNT(" + columnName + ")";
		}
		if (functionSymbol == ExpressionOperation.AVG) {
			String columnName = getSQLString(function.getTerm(0), index, false);
			//havingCond = true;
			return "AVG(" + columnName + ")";
		}
		if (functionSymbol == ExpressionOperation.SUM) {
			String columnName = getSQLString(function.getTerm(0), index, false);
			//havingCond = true;
			return "SUM(" + columnName + ")";
		}

		throw new RuntimeException("Unexpected function in the query: " + functionSymbol);
	}

	/***
	 * Returns the valid SQL lexical form of rdf literals based on the current
	 * database and the datatype specified in the function predicate.
	 *
	 * <p>
	 * For example, if the function is xsd:boolean, and the current database is
	 * H2, the SQL lexical form would be for "true" "TRUE" (or any combination
	 * of lower and upper case) or "1" is always
	 *
	 * @param constant
	 * @return
	 */
	private String getSQLLexicalForm(RDFLiteralConstant constant) {

		if (constant.equals(termFactory.getNullConstant())) {
			// TODO: we should not have to treat NULL as a special case!
			// It is because this constant is currently of type COL_TYPE.STRING!
			return "NULL";
		}
		switch (COL_TYPE.getColType(constant.getType().getIRI())) {
			case BNODE:
			case OBJECT:
			case STRING:
				return sqladapter.getSQLLexicalFormString(constant.getValue());
			case BOOLEAN:
				boolean v = XsdDatatypeConverter.parseXsdBoolean(constant.getValue());
				return sqladapter.getSQLLexicalFormBoolean(v);
			case DATETIME:
				return sqladapter.getSQLLexicalFormDatetime(constant.getValue());
			case DATETIME_STAMP:
				return sqladapter.getSQLLexicalFormDatetimeStamp(constant.getValue());
			case DECIMAL:
			case DOUBLE:
			case INTEGER:
			case LONG:
			case FLOAT:
			case NON_POSITIVE_INTEGER:
			case INT:
			case UNSIGNED_INT:
			case NEGATIVE_INTEGER:
			case POSITIVE_INTEGER:
			case NON_NEGATIVE_INTEGER:
				return constant.getValue();
			case NULL:
				return "NULL";
			default:
				return "'" + constant.getValue() + "'";
		}
	}



	private boolean hasIRIDictionary() {
		return uriRefIds != null;
	}

	/**
	 * We look for the ID in the list of IDs, if it's not there, then we return -2,
	 * which we know will never appear on the DB. This is correct because if a
	 * constant appears in a query, and that constant was never inserted in the
	 * DB, the query must be empty (that atom), by putting -2 as id, we will
	 * enforce that.
	 *
	 * @param uri
	 * @return
	 */
	private int getUriid(String uri) {
		Integer id = uriRefIds.getId(uri);
		if (id != null)
			return id;
		return -2;
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

		AliasIndex(CQIE query, ImmutableMap<Predicate, FromItem> subQueryDefinitions, AtomicInteger viewCounter) {
			this.viewCounter = viewCounter;
			for (Function atom : query.getBody()) {
				// This will be called recursively if necessary
				generateViewsIndexVariables(atom, subQueryDefinitions);
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
		 *
		 * @param atom
		 * @param subQueryDefinitions
		 */
		private void generateViewsIndexVariables(Function atom,
												 ImmutableMap<Predicate, FromItem> subQueryDefinitions) {
			if (atom.isOperation()) {
				return;
			}
			else if (atom.isAlgebraFunction()) {
				for (Term subatom : atom.getTerms()) {
					if (subatom instanceof Function) {
						generateViewsIndexVariables((Function) subatom, subQueryDefinitions);
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
								.map(e -> e.getValue().alias).collect(Collectors.toList()));

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
					int idx = isSubquery
							? 3 * i + 2 // a view from an Ans predicate
							: i;        // a database relation
					columns.add(fromItem.attributes.get(idx));
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
}
