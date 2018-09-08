package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.answering.reformulation.rewriting.SameAsRewriter;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.CQCUtilities;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.injection.TranslationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static it.unibz.inf.ontop.model.atom.PredicateConstants.ONTOP_QUERY;

/**
 * TODO: rename it QueryTranslatorImpl ?
 */
public class QuestQueryProcessor implements QueryReformulator {

	private final QueryRewriter rewriter;
	private final NativeQueryGenerator datasourceQueryGenerator;
	private final QueryCache queryCache;

	private final QueryUnfolder queryUnfolder;
	private final SameAsRewriter sameAsRewriter;
	private final BindingLiftOptimizer bindingLiftOptimizer;

	private static final Logger log = LoggerFactory.getLogger(QuestQueryProcessor.class);
	private final ExecutorRegistry executorRegistry;
	private final DatalogProgram2QueryConverter datalogConverter;
	private final OntopReformulationSettings settings;
	private final DBMetadata dbMetadata;
	private final JoinLikeOptimizer joinLikeOptimizer;
	private final InputQueryTranslator inputQueryTranslator;
	private final InputQueryFactory inputQueryFactory;
	private final DatalogFactory datalogFactory;
	private final DatalogNormalizer datalogNormalizer;
	private final FlattenUnionOptimizer flattenUnionOptimizer;
	private final EQNormalizer eqNormalizer;
	private final UnifierUtilities unifierUtilities;
	private final SubstitutionUtilities substitutionUtilities;
	private final CQCUtilities cqcUtilities;
	private final PushUpBooleanExpressionOptimizer pullUpExpressionOptimizer;
	private final IQConverter iqConverter;

	@AssistedInject
	private QuestQueryProcessor(@Assisted OBDASpecification obdaSpecification,
								@Assisted ExecutorRegistry executorRegistry,
								QueryCache queryCache,
								BindingLiftOptimizer bindingLiftOptimizer, OntopReformulationSettings settings,
								DatalogProgram2QueryConverter datalogConverter,
								TranslationFactory translationFactory,
								QueryRewriter queryRewriter,
								JoinLikeOptimizer joinLikeOptimizer,
								InputQueryFactory inputQueryFactory,
								DatalogFactory datalogFactory,
								DatalogNormalizer datalogNormalizer, FlattenUnionOptimizer flattenUnionOptimizer,
								EQNormalizer eqNormalizer, UnifierUtilities unifierUtilities,
								SubstitutionUtilities substitutionUtilities, CQCUtilities cqcUtilities,
								PushUpBooleanExpressionOptimizer pullUpExpressionOptimizer,
								IQConverter iqConverter) {
		this.bindingLiftOptimizer = bindingLiftOptimizer;
		this.settings = settings;
		this.joinLikeOptimizer = joinLikeOptimizer;
		this.inputQueryFactory = inputQueryFactory;
		this.datalogFactory = datalogFactory;
		this.datalogNormalizer = datalogNormalizer;
		this.flattenUnionOptimizer = flattenUnionOptimizer;
		this.eqNormalizer = eqNormalizer;
		this.unifierUtilities = unifierUtilities;
		this.substitutionUtilities = substitutionUtilities;
		this.cqcUtilities = cqcUtilities;
		this.pullUpExpressionOptimizer = pullUpExpressionOptimizer;
		this.iqConverter = iqConverter;
		this.rewriter = queryRewriter;

		this.rewriter.setTBox(obdaSpecification.getSaturatedTBox());

		Mapping saturatedMapping = obdaSpecification.getSaturatedMapping();

		if(log.isDebugEnabled()){
			log.debug("Mapping: \n{}", Joiner.on("\n").join(
					saturatedMapping.getRDFAtomPredicates().stream()
						.flatMap(p -> saturatedMapping.getQueries(p).stream())
						.iterator()));
		}

		this.queryUnfolder = translationFactory.create(saturatedMapping);

		this.dbMetadata = obdaSpecification.getDBMetadata();
		this.datasourceQueryGenerator = translationFactory.create(dbMetadata);
		this.inputQueryTranslator = translationFactory.createInputQueryTranslator(saturatedMapping.getMetadata()
				.getUriTemplateMatcher());
		this.sameAsRewriter = translationFactory.createSameAsRewriter(saturatedMapping);
		this.queryCache = queryCache;
		this.executorRegistry = executorRegistry;
		this.datalogConverter = datalogConverter;

		log.info("Ontop has completed the setup and it is ready for query answering!");
	}

	private DatalogProgram preProcess(InternalSparqlQuery translation) throws OntopInvalidInputQueryException {
		DatalogProgram program = translation.getProgram();
		log.debug("Datalog program translated from the SPARQL query: \n{}", program);

		if(settings.isSameAsInMappingsEnabled()){
			program = sameAsRewriter.getSameAsRewriting(program);
			log.debug("Datalog program after SameAs rewriting \n" + program);
		}

		log.debug("Replacing equivalences...");
		DatalogProgram newprogramEq = datalogFactory.getDatalogProgram(program.getQueryModifiers());
		Predicate topLevelPredicate = null;
		for (CQIE query : program.getRules()) {
			// TODO: fix cloning
			CQIE rule = query.clone();
			// TODO: get rid of EQNormalizer
			eqNormalizer.enforceEqualities(rule);

			if (rule.getHead().getFunctionSymbol().getName().equals(ONTOP_QUERY))
				topLevelPredicate = rule.getHead().getFunctionSymbol();
			newprogramEq.appendRule(rule);
		}

		SPARQLQueryFlattener fl = new SPARQLQueryFlattener(newprogramEq, datalogFactory,
				eqNormalizer, unifierUtilities, substitutionUtilities);
		List<CQIE> p = fl.flatten(newprogramEq.getRules(topLevelPredicate).get(0));
		DatalogProgram newprogram = datalogFactory.getDatalogProgram(program.getQueryModifiers(), p);


		for (CQIE q : newprogram.getRules())
			datalogNormalizer.unfoldJoinTrees(q);
		log.debug("Normalized program: \n{}", newprogram);

		if (newprogram.getRules().isEmpty())
			throw new OntopInvalidInputQueryException("Error, the translation of the query generated 0 rules. " +
					"This is not possible for any SELECT query (other queries are not supported by the translator).");

		return newprogram;
	}
	

	public void clearNativeQueryCache() {
		queryCache.clear();
	}


	@Override
	public ExecutableQuery reformulateIntoNativeQuery(InputQuery inputQuery)
			throws OntopReformulationException {

		ExecutableQuery cachedQuery = queryCache.get(inputQuery);
		if (cachedQuery != null)
			return cachedQuery;

		try {
			InternalSparqlQuery translation = inputQuery.translate(inputQueryTranslator);
			DatalogProgram newprogram = preProcess(translation);

			log.debug("Start the rewriting process...");

			for (CQIE cq : newprogram.getRules())
				cqcUtilities.optimizeQueryWithSigmaRules(cq.getBody(), rewriter.getSigma());

			DatalogProgram programAfterRewriting = datalogFactory.getDatalogProgram(newprogram.getQueryModifiers());
			programAfterRewriting.appendRule(rewriter.rewrite(newprogram.getRules()));

			try {
				IQ convertedIQ =  datalogConverter.convertDatalogProgram(programAfterRewriting, ImmutableList.of());

				log.debug("Directly translated (SPARQL) IQ: \n" + convertedIQ.toString());

				log.debug("Start the unfolding...");

				IQ unfoldedIQ = queryUnfolder.optimize(convertedIQ);
				if (unfoldedIQ.getTree().isDeclaredAsEmpty())
					throw new EmptyQueryException();
				log.debug("Unfolded query: \n" + unfoldedIQ.toString());

				// Non-final
				IntermediateQuery intermediateQuery = iqConverter.convert(unfoldedIQ, dbMetadata, executorRegistry);

				//lift bindings and union when it is possible
				intermediateQuery = bindingLiftOptimizer.optimize(intermediateQuery);
				log.debug("New query after substitution lift optimization: \n" + intermediateQuery.toString());

				log.debug("New lifted query: \n" + intermediateQuery.toString());

				intermediateQuery = pullUpExpressionOptimizer.optimize(intermediateQuery);
				log.debug("After pushing up boolean expressions: \n" + intermediateQuery.toString());

				intermediateQuery = new ProjectionShrinkingOptimizer().optimize(intermediateQuery);

				log.debug("After projection shrinking: \n" + intermediateQuery.toString());


				intermediateQuery = joinLikeOptimizer.optimize(intermediateQuery);
				log.debug("New query after fixed point join optimization: \n" + intermediateQuery.toString());

				intermediateQuery = flattenUnionOptimizer.optimize(intermediateQuery);
				log.debug("New query after flattening Unions: \n" + intermediateQuery.toString());
//				BasicLeftJoinOptimizer leftJoinOptimizer = new BasicLeftJoinOptimizer();
//				intermediateQuery = leftJoinOptimizer.optimize(intermediateQuery);
//				log.debug("New query after left join optimization: \n" + intermediateQuery.toString());
//
//				BasicJoinOptimizer joinOptimizer = new BasicJoinOptimizer();
//				intermediateQuery = joinOptimizer.optimize(intermediateQuery);
//				log.debug("New query after join optimization: \n" + intermediateQuery.toString());

				ExecutableQuery executableQuery = generateExecutableQuery(intermediateQuery,
						ImmutableList.copyOf(translation.getSignature()));
				queryCache.put(inputQuery, executableQuery);
				return executableQuery;

			}
			/**
			 * No solution.
			 */
			catch (EmptyQueryException e) {
				ExecutableQuery emptyQuery = datasourceQueryGenerator.generateEmptyQuery(
						ImmutableList.copyOf(translation.getSignature()));

				log.debug("Empty query --> no solution.");
				queryCache.put(inputQuery, emptyQuery);
				return emptyQuery;
			}

			//unfoldingTime = System.currentTimeMillis() - startTime;
		}
		catch (OntopReformulationException e) {
			throw e;
		}
		/*
		 * Bug: should normally not be reached
		 * TODO: remove it
		 */
		catch (Exception e) {
			log.warn("Unexpected exception: " + e.getMessage(), e);
			throw new OntopReformulationException(e);
			//throw new OntopReformulationException("Error rewriting and unfolding into SQL\n" + e.getMessage());
		}
	}

	private ExecutableQuery generateExecutableQuery(IntermediateQuery intermediateQuery, ImmutableList<String> signature)
			throws OntopReformulationException {
		log.debug("Producing the native query string...");

		ExecutableQuery executableQuery = datasourceQueryGenerator.generateSourceQuery(intermediateQuery, signature);

		log.debug("Resulting native query: \n{}", executableQuery);

		return executableQuery;
	}


	/**
	 * Returns the final rewriting of the given query
	 */
	@Override
	public String getRewritingRendering(InputQuery query) throws OntopReformulationException {
		InternalSparqlQuery translation = query.translate(inputQueryTranslator);
		DatalogProgram program = preProcess(translation);
		List<CQIE> rewriting = rewriter.rewrite(program.getRules());
		return Joiner.on("\n").join(rewriting);
	}

	@Override
	public InputQueryFactory getInputQueryFactory() {
		return inputQueryFactory;
	}
}
