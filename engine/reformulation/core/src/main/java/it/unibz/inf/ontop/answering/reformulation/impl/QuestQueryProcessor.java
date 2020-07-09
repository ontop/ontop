package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.TranslationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * TODO: rename it QueryTranslatorImpl ?
 *
 * See ReformulationFactory for creating a new instance.
 *
 */
public class QuestQueryProcessor implements QueryReformulator {

	private final QueryRewriter rewriter;
	private final NativeQueryGenerator datasourceQueryGenerator;
	private final QueryCache queryCache;

	private final QueryUnfolder queryUnfolder;

	private static final Logger log = LoggerFactory.getLogger(QuestQueryProcessor.class);
	private final ExecutorRegistry executorRegistry;
	private final InputQueryTranslator inputQueryTranslator;
	private final InputQueryFactory inputQueryFactory;
	private final GeneralStructuralAndSemanticIQOptimizer generalOptimizer;
	private final QueryPlanner queryPlanner;
	private final QueryLogger.Factory queryLoggerFactory;

	@AssistedInject
	private QuestQueryProcessor(@Assisted OBDASpecification obdaSpecification,
								@Assisted ExecutorRegistry executorRegistry,
								QueryCache queryCache,
								TranslationFactory translationFactory,
								QueryRewriter queryRewriter,
								InputQueryFactory inputQueryFactory,
								InputQueryTranslator inputQueryTranslator,
								GeneralStructuralAndSemanticIQOptimizer generalOptimizer,
								QueryPlanner queryPlanner,
								QueryLogger.Factory queryLoggerFactory) {
		this.inputQueryFactory = inputQueryFactory;
		this.rewriter = queryRewriter;
		this.generalOptimizer = generalOptimizer;
		this.queryPlanner = queryPlanner;
		this.queryLoggerFactory = queryLoggerFactory;

		this.rewriter.setTBox(obdaSpecification.getSaturatedTBox());
		this.queryUnfolder = translationFactory.create(obdaSpecification.getSaturatedMapping());
		this.datasourceQueryGenerator = translationFactory.create(obdaSpecification.getDBParameters());

		this.inputQueryTranslator = inputQueryTranslator;
		this.queryCache = queryCache;
		this.executorRegistry = executorRegistry;

		log.info("Ontop has completed the setup and it is ready for query answering!");
	}

	@Override
	public IQ reformulateIntoNativeQuery(InputQuery inputQuery, QueryLogger queryLogger)
			throws OntopReformulationException {

		long beginning = System.currentTimeMillis();

		IQ cachedQuery = queryCache.get(inputQuery);
		if (cachedQuery != null) {
			queryLogger.declareReformulationFinishedAndSerialize(true);
			return cachedQuery;
		}

		try {
			log.debug("SPARQL query:\n{}", inputQuery.getInputString());
			IQ convertedIQ = inputQuery.translate(inputQueryTranslator);
			log.debug("Parsed query converted into IQ (after normalization):\n{}", convertedIQ);

            try {
                log.debug("Start the rewriting process...");
                IQ rewrittenIQ = rewriter.rewrite(convertedIQ);

                log.debug("Rewritten IQ:\n{}",rewrittenIQ);

                log.debug("Start the unfolding...");

                IQ unfoldedIQ = queryUnfolder.optimize(rewrittenIQ);
                if (unfoldedIQ.getTree().isDeclaredAsEmpty()) {
                	log.debug(String.format("Reformulation time: %d ms", System.currentTimeMillis() - beginning));
					queryLogger.declareReformulationFinishedAndSerialize(false);
					return unfoldedIQ;
				}
                log.debug("Unfolded query: \n" + unfoldedIQ.toString());

                IQ optimizedQuery = generalOptimizer.optimize(unfoldedIQ, executorRegistry);
				IQ plannedQuery = queryPlanner.optimize(optimizedQuery, executorRegistry);
				log.debug("Planned query: \n" + plannedQuery);

				IQ executableQuery = generateExecutableQuery(plannedQuery);
				queryCache.put(inputQuery, executableQuery);
				log.debug(String.format("Reformulation time: %d ms", System.currentTimeMillis() - beginning));
				queryLogger.declareReformulationFinishedAndSerialize(false);
				return executableQuery;

			}
            catch (OntopReformulationException e) {
            	queryLogger.declareReformulationException(e);
                throw e;
            }
        }
		/*
		 * Bug: should normally not be reached
		 * TODO: remove it
		 */
		catch (Exception e) {
			log.warn("Unexpected exception: " + e.getMessage(), e);
			OntopReformulationException exception = new OntopReformulationException(e);
			queryLogger.declareReformulationException(exception);
			throw exception;
		}
	}

	private IQ generateExecutableQuery(IQ iq) {

		log.debug("Producing the native query string...");

		IQ executableQuery = datasourceQueryGenerator.generateSourceQuery(iq);

		log.debug("Resulting native query: \n{}", executableQuery);

		return executableQuery;
	}


	/**
	 * Returns the final rewriting of the given query
	 */
	@Override
	public String getRewritingRendering(InputQuery query) throws OntopReformulationException {
		log.debug("SPARQL query:\n{}", query.getInputString());
		IQ convertedIQ = query.translate(inputQueryTranslator);
		log.debug("Parsed query converted into IQ:\n{}", convertedIQ);
		try {
			IQ rewrittenIQ = rewriter.rewrite(convertedIQ);
			return rewrittenIQ.toString();
		}
		catch (EmptyQueryException e) {
			e.printStackTrace();
		}
		return "EMPTY REWRITING";
	}

	@Override
	public InputQueryFactory getInputQueryFactory() {
		return inputQueryFactory;
	}

	@Override
	public QueryLogger.Factory getQueryLoggerFactory() {
		return  queryLoggerFactory;
	}
}
