package it.unibz.inf.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.input.InputQuery;
import it.unibz.inf.ontop.answering.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopTranslationException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.injection.OntopTranslationSettings;
import it.unibz.inf.ontop.injection.TranslationFactory;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.*;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingSameAsPredicateExtractor;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.*;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.*;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.datalog.DatalogProgramRenderer;

import java.util.*;

import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.answering.reformulation.QueryTranslator;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;

/**
 * TODO: rename it QueryTranslatorImpl ?
 */
public class QuestQueryProcessor implements QueryTranslator {

	private final QueryRewriter rewriter;
	private final LinearInclusionDependencies sigma;
	private final VocabularyValidator vocabularyValidator;
	private final Optional<IRIDictionary> iriDictionary;
	private final NativeQueryGenerator datasourceQueryGenerator;
	private final QueryCache queryCache;

	private final QueryUnfolder queryUnfolder;
	
	private static final Logger log = LoggerFactory.getLogger(QuestQueryProcessor.class);
	private final ExecutorRegistry executorRegistry;
	private final DatalogProgram2QueryConverter datalogConverter;
	private final ImmutableSet<Predicate> dataPropertiesAndClassesMapped;
	private final ImmutableSet<Predicate> objectPropertiesMapped;
	private final OntopTranslationSettings settings;
	private final UriTemplateMatcher uriTemplateMatcher;
	private final DBMetadata dbMetadata;
	private final JoinLikeOptimizer joinLikeOptimizer;

	@AssistedInject
	private QuestQueryProcessor(@Assisted OBDASpecification obdaSpecification,
								@Assisted ExecutorRegistry executorRegistry,
								@Nullable IRIDictionary iriDictionary,
								QueryCache queryCache,
								OntopTranslationSettings settings,
								DatalogProgram2QueryConverter datalogConverter,
								TranslationFactory translationFactory,
								QueryRewriter rewriter,
								JoinLikeOptimizer joinLikeOptimizer) {
		this.joinLikeOptimizer = joinLikeOptimizer;
		TBoxReasoner saturatedTBox = obdaSpecification.getSaturatedTBox();
		this.sigma = LinearInclusionDependencyTools.getABoxDependencies(saturatedTBox, true);

		this.rewriter = rewriter;
		this.rewriter.setTBox(saturatedTBox, obdaSpecification.getVocabulary(), sigma);

		Mapping saturatedMapping = obdaSpecification.getSaturatedMapping();

		this.queryUnfolder = translationFactory.create(saturatedMapping);

		this.vocabularyValidator = new VocabularyValidator(obdaSpecification.getSaturatedTBox(),
				obdaSpecification.getVocabulary());
		this.iriDictionary = Optional.ofNullable(iriDictionary);
		this.dbMetadata = obdaSpecification.getDBMetadata();
		this.datasourceQueryGenerator = translationFactory.create(dbMetadata);
		this.queryCache = queryCache;
		this.settings = settings;
		this.executorRegistry = executorRegistry;
		this.datalogConverter = datalogConverter;
		this.uriTemplateMatcher = saturatedMapping.getMetadata().getUriTemplateMatcher();

		if (settings.isSameAsInMappingsEnabled()) {
			MappingSameAsPredicateExtractor msa = new MappingSameAsPredicateExtractor(saturatedMapping);
			dataPropertiesAndClassesMapped = msa.getDataPropertiesAndClassesWithSameAs();
			objectPropertiesMapped = msa.getObjectPropertiesWithSameAs();
		} else {
			dataPropertiesAndClassesMapped = ImmutableSet.of();
			objectPropertiesMapped = ImmutableSet.of();
		}
	}
	
	private DatalogProgram translateAndPreProcess(InputQuery inputQuery)
			throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {
		InputQueryTranslator translator = new SparqlAlgebraToDatalogTranslator(uriTemplateMatcher, iriDictionary);
		InternalSparqlQuery translation = inputQuery.translate(translator);
		return preProcess(translation);
	}

	private DatalogProgram preProcess(InternalSparqlQuery translation) {
		DatalogProgram program = translation.getProgram();
		log.debug("Datalog program translated from the SPARQL query: \n{}", program);

		if (settings.isSameAsInMappingsEnabled()) {
			SameAsRewriter sameAs = new SameAsRewriter(dataPropertiesAndClassesMapped, objectPropertiesMapped);
			program = sameAs.getSameAsRewriting(program);
			//System.out.println("SAMEAS" + program);
		}

		log.debug("Replacing equivalences...");
		DatalogProgram newprogramEq = DATALOG_FACTORY.getDatalogProgram(program.getQueryModifiers());
		Predicate topLevelPredicate = null;
		for (CQIE query : program.getRules()) {
			// TODO: fix cloning
			CQIE rule = query.clone();
			// TODO: get rid of EQNormalizer
			EQNormalizer.enforceEqualities(rule);

			CQIE newquery = vocabularyValidator.replaceEquivalences(rule);
			if (newquery.getHead().getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_QUERY))
				topLevelPredicate = newquery.getHead().getFunctionSymbol();
			newprogramEq.appendRule(newquery);
		}

		SPARQLQueryFlattener fl = new SPARQLQueryFlattener(newprogramEq);
		List<CQIE> p = fl.flatten(newprogramEq.getRules(topLevelPredicate).get(0));
		DatalogProgram newprogram = DATALOG_FACTORY.getDatalogProgram(program.getQueryModifiers(), p);

		return newprogram;
	}
	

	public void clearNativeQueryCache() {
		queryCache.clear();
	}


	@Override
	public ExecutableQuery translateIntoNativeQuery(InputQuery inputQuery)
			throws OntopTranslationException {

		ExecutableQuery cachedQuery = queryCache.get(inputQuery);
		if (cachedQuery != null)
			return cachedQuery;

		try {
			InputQueryTranslator translator = new SparqlAlgebraToDatalogTranslator(uriTemplateMatcher, iriDictionary);
			InternalSparqlQuery translation = inputQuery.translate(translator);
			DatalogProgram newprogram = preProcess(translation);

			for (CQIE q : newprogram.getRules()) 
				DatalogNormalizer.unfoldJoinTrees(q);
			log.debug("Normalized program: \n{}", newprogram);

			if (newprogram.getRules().size() < 1)
				throw new OntopInvalidInputQueryException("Error, the translation of the query generated 0 rules. " +
						"This is not possible for any SELECT query (other queries are not supported by the translator).");

			log.debug("Start the rewriting process...");

			//final long startTime0 = System.currentTimeMillis();
			for (CQIE cq : newprogram.getRules())
				CQCUtilities.optimizeQueryWithSigmaRules(cq.getBody(), sigma);
			DatalogProgram programAfterRewriting = rewriter.rewrite(newprogram);

			//rewritingTime = System.currentTimeMillis() - startTime0;

			//final long startTime = System.currentTimeMillis();

			DatalogProgram programAfterUnfolding;
			try {
				IntermediateQuery intermediateQuery = datalogConverter.convertDatalogProgram(
						dbMetadata, programAfterRewriting, ImmutableList.of(), executorRegistry);

				log.debug("Directly translated (SPARQL) intermediate query: \n" + intermediateQuery.toString());

				log.debug("Start the unfolding...");

				intermediateQuery = queryUnfolder.optimize(intermediateQuery);

				log.debug("Unfolded query: \n" + intermediateQuery.toString());


				//lift bindings and union when it is possible
				IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();
				intermediateQuery = substitutionOptimizer.optimize(intermediateQuery);

				log.debug("New lifted query: \n" + intermediateQuery.toString());

				ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
				intermediateQuery = projectionShrinkingOptimizer.optimize(intermediateQuery);

				log.debug("After projection shrinking: \n" + intermediateQuery.toString());


				intermediateQuery = joinLikeOptimizer.optimize(intermediateQuery);
				log.debug("New query after fixed point join optimization: \n" + intermediateQuery.toString());

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
		catch (OntopTranslationException e) {
			throw e;
		}
		/*
		 * Bug: should normally not be reached
		 * TODO: remove it
		 */
		catch (Exception e) {
			log.warn("Unexpected exception: " + e.getMessage(), e);
			throw new OntopTranslationException(e);
			//throw new OntopReformulationException("Error rewriting and unfolding into SQL\n" + e.getMessage());
		}
	}

	private ExecutableQuery generateExecutableQuery(IntermediateQuery intermediateQuery, ImmutableList<String> signature)
			throws OntopTranslationException {
		log.debug("Producing the native query string...");

		ExecutableQuery executableQuery = datasourceQueryGenerator.generateSourceQuery(intermediateQuery, signature);

		log.debug("Resulting native query: \n{}", executableQuery);

		return executableQuery;
	}


	/**
	 * Returns the final rewriting of the given query
	 */
	@Override
	public String getRewritingRendering(InputQuery query) throws OntopTranslationException {
		DatalogProgram program = translateAndPreProcess(query);
		DatalogProgram rewriting = rewriter.rewrite(program);
		return DatalogProgramRenderer.encode(rewriting);
	}
}
