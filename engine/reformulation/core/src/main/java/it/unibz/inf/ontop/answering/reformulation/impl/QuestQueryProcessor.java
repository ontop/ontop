package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.TranslationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: rename it QueryTranslatorImpl ?
 *
 * See ReformulationFactory for creating a new instance.
 *
 */
public class QuestQueryProcessor implements QueryReformulator {

	private static final Logger LOGGER = LoggerFactory.getLogger(QuestQueryProcessor.class);

	private final QueryRewriter rewriter;
	protected final NativeQueryGenerator datasourceQueryGenerator;
	private final QueryCache queryCache;

	private final QueryUnfolder queryUnfolder;

	private final KGQueryTranslator inputQueryTranslator;
	private final KGQueryFactory kgQueryFactory;
	private final GeneralStructuralAndSemanticIQOptimizer generalOptimizer;
	private final QueryPlanner queryPlanner;
	private final QueryLogger.Factory queryLoggerFactory;

	@AssistedInject
	protected QuestQueryProcessor(@Assisted OBDASpecification obdaSpecification,
								QueryCache queryCache,
								QueryUnfolder.Factory queryUnfolderFactory,
								TranslationFactory translationFactory,
								QueryRewriter queryRewriter,
								KGQueryFactory kgQueryFactory,
								KGQueryTranslator inputQueryTranslator,
								GeneralStructuralAndSemanticIQOptimizer generalOptimizer,
								QueryPlanner queryPlanner,
								QueryLogger.Factory queryLoggerFactory) {
		this.kgQueryFactory = kgQueryFactory;
		this.rewriter = queryRewriter;
		this.generalOptimizer = generalOptimizer;
		this.queryPlanner = queryPlanner;
		this.queryLoggerFactory = queryLoggerFactory;

		this.rewriter.setTBox(obdaSpecification.getSaturatedTBox());
		this.queryUnfolder = queryUnfolderFactory.create(obdaSpecification.getSaturatedMapping());
		this.datasourceQueryGenerator = translationFactory.create(obdaSpecification.getDBParameters());

		this.inputQueryTranslator = inputQueryTranslator;
		this.queryCache = queryCache;

		LOGGER.info("Ontop has completed the setup and it is ready for query answering!");
	}

	@Override
	public IQ reformulateIntoNativeQuery(KGQuery<?> inputQuery, QueryLogger queryLogger)
			throws OntopReformulationException {

		long beginning = System.currentTimeMillis();

		IQ cachedQuery = queryCache.get(inputQuery);
		if (cachedQuery != null) {
			queryLogger.declareReformulationFinishedAndSerialize(cachedQuery,true);
			return cachedQuery;
		}

		try {
			LOGGER.debug("SPARQL query:\n{}\n", inputQuery.getOriginalString());
			IQ convertedIQ = inputQuery.translate(inputQueryTranslator);
			LOGGER.debug("Parsed query converted into IQ (after normalization):\n{}\n", convertedIQ);

			queryLogger.setSparqlIQ(convertedIQ);

            try {
                LOGGER.debug("Start the rewriting process...");

                IQ rewrittenIQ = rewriter.rewrite(convertedIQ);
                LOGGER.debug("Rewritten IQ:\n{}\n", rewrittenIQ);

                LOGGER.debug("Start the unfolding...");
                IQ unfoldedIQ = queryUnfolder.optimize(rewrittenIQ);
                if (unfoldedIQ.getTree().isDeclaredAsEmpty()) {
					queryLogger.declareReformulationFinishedAndSerialize(unfoldedIQ, false);
                	LOGGER.debug("Reformulation time: {} ms\n", System.currentTimeMillis() - beginning);
					return unfoldedIQ;
				}

				LOGGER.debug("Unfolded query:\n{}\n", unfoldedIQ);

                IQ optimizedQuery = generalOptimizer.optimize(unfoldedIQ);
				IQ plannedQuery = queryPlanner.optimize(optimizedQuery);
				LOGGER.debug("Planned query:\n{}\n", plannedQuery);

				queryLogger.setPlannedQuery(plannedQuery);

				IQ executableQuery = generateExecutableQuery(plannedQuery);
				queryCache.put(inputQuery, executableQuery);
				queryLogger.declareReformulationFinishedAndSerialize(executableQuery, false);
				LOGGER.debug("Reformulation time: {} ms\n", System.currentTimeMillis() - beginning);
				return executableQuery;
			}
            catch (OntopReformulationException e) {
            	queryLogger.declareReformulationException(e);
                throw e;
            }
        }
		catch (OntopInvalidKGQueryException e) {
			OntopInvalidInputQueryException reformulationException = new OntopInvalidInputQueryException(e.getMessage());
			queryLogger.declareReformulationException(reformulationException);
			throw reformulationException;
		}
		catch (OntopUnsupportedKGQueryException e) {
			OntopUnsupportedInputQueryException reformulationException = new OntopUnsupportedInputQueryException(e.getMessage());
			queryLogger.declareReformulationException(reformulationException);
			throw reformulationException;
		}
		/*
		 * Bug: should normally not be reached
		 * TODO: remove it
		 */
		catch (Exception e) {
			LOGGER.warn("Unexpected exception: " + e.getMessage(), e);
			OntopReformulationException exception = new OntopReformulationException(e);
			queryLogger.declareReformulationException(exception);
			throw exception;
		}
	}

	protected IQ generateExecutableQuery(IQ iq) throws OntopReformulationException {
		LOGGER.debug("Producing the native query string...");

		IQ executableQuery = datasourceQueryGenerator.generateSourceQuery(iq);
		LOGGER.debug("Resulting native query:\n{}\n", executableQuery);

		return executableQuery;
	}


	/**
	 * Returns the final rewriting of the given query
	 */
	@Override
	public String getRewritingRendering(KGQuery<?> query) throws OntopReformulationException {
		LOGGER.debug("SPARQL query:\n{}\n", query.getOriginalString());

		try {
			IQ convertedIQ = query.translate(inputQueryTranslator);
			LOGGER.debug("Parsed query converted into IQ:\n{}\n", convertedIQ);

			try {
				IQ rewrittenIQ = rewriter.rewrite(convertedIQ);
				return rewrittenIQ.toString();
			} catch (EmptyQueryException ignored) {
				return "EMPTY REWRITING";
			}
		} catch (OntopInvalidKGQueryException e) {
			throw new OntopInvalidInputQueryException(e.getMessage());
		}
		 catch (OntopUnsupportedKGQueryException e) {
			throw new OntopUnsupportedInputQueryException(e.getMessage());
		 }
	}

	@Override
	public KGQueryFactory getInputQueryFactory() {
		return kgQueryFactory;
	}

	@Override
	public QueryLogger.Factory getQueryLoggerFactory() {
		return  queryLoggerFactory;
	}
}
