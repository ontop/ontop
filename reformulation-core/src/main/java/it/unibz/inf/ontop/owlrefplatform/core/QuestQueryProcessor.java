package it.unibz.inf.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.*;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.BasicJoinOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.DatalogToSparqlTranslator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.SPARQLQueryFlattener;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.renderer.DatalogProgramRenderer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BC: TODO: make it explicitly immutable
 */
public class QuestQueryProcessor {

	private final Map<String, ParsedQuery> parsedQueryCache = new ConcurrentHashMap<>();

	private final QueryRewriter rewriter;
	private final LinearInclusionDependencies sigma;
	protected final QuestUnfolder unfolder;
	private final VocabularyValidator vocabularyValidator;
	private final SemanticIndexURIMap uriMap;
	private final NativeQueryGenerator datasourceQueryGenerator;
	private final QueryCache queryCache;
	
	private static final Logger log = LoggerFactory.getLogger(QuestQueryProcessor.class);
	private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
	private final boolean hasDistinctResultSet;

	public QuestQueryProcessor(QueryRewriter rewriter, LinearInclusionDependencies sigma, QuestUnfolder unfolder,
							   VocabularyValidator vocabularyValidator, SemanticIndexURIMap uriMap,
							   NativeQueryGenerator datasourceQueryGenerator,
							   QueryCache queryCache, boolean hasDistinctResultSet) {
		this.rewriter = rewriter;
		this.sigma = sigma;
		this.unfolder = unfolder;
		this.vocabularyValidator = vocabularyValidator;
		this.uriMap = uriMap;
		this.datasourceQueryGenerator = datasourceQueryGenerator;
		this.queryCache = queryCache;
		this.hasDistinctResultSet = hasDistinctResultSet;
	}

	/**
	 * BC: TODO: rename parseSPARQL
     */
	public ParsedQuery getParsedQuery(String sparql) throws MalformedQueryException {
		ParsedQuery pq = parsedQueryCache.get(sparql);
		if (pq == null) {
			QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
			pq = parser.parseQuery(sparql, null);
			parsedQueryCache.put(sparql,  pq);
		}
		return pq;
	}
	
	
	private DatalogProgram translateAndPreProcess(ParsedQuery pq)  {
		
		SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(unfolder.getUriTemplateMatcher(),
				uriMap);
		DatalogProgram program = translator.translate(pq);

		log.debug("Datalog program translated from the SPARQL query: \n{}", program);

		SPARQLQueryFlattener unfolder = new SPARQLQueryFlattener(program);
		program = unfolder.flatten();
		log.debug("Flattened program: \n{}", program);
			
		log.debug("Replacing equivalences...");
		DatalogProgram newprogram = OBDADataFactoryImpl.getInstance().getDatalogProgram(program.getQueryModifiers());
		for (CQIE query : program.getRules()) {
			CQIE newquery = vocabularyValidator.replaceEquivalences(query);
			newprogram.appendRule(newquery);
		}
		return newprogram;		
	}
	
	
	public void clearNativeQueryCache() {
		queryCache.clear();
	}
	
	
	public ExecutableQuery translateIntoNativeQuery(ParsedQuery pq,
													Optional<SesameConstructTemplate> optionalConstructTemplate)
			throws OBDAException {

		ExecutableQuery executableQuery = queryCache.get(pq);
		if (executableQuery != null)
			return executableQuery;

		SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
				unfolder.getUriTemplateMatcher(),
				uriMap);
		DatalogProgram translation = translator.translate(pq);

		List<String> signature = null;
		// IMPORTANT: this is the original query
		// (with original variable names, not the BINDings after flattening)
		for (CQIE q : translation.getRules()) {
			if (q.getHead().getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_QUERY)) {
				List<Term> terms = q.getHead().getTerms();
				signature = new ArrayList<>(terms.size());
				for (Term t : terms)
					signature.add(((Variable) t).getName()); // ALL VARIABLES by construction
				break;
			}
		}
		
		try {
			// log.debug("Input query:\n{}", strquery);


			log.debug("Datalog program translated from the SPARQL query: \n{}", translation);

			SPARQLQueryFlattener flattener = new SPARQLQueryFlattener(translation);
			DatalogProgram program = flattener.flatten();
			log.debug("Flattened program: \n{}", program);
				
			log.debug("Replacing equivalences...");
			DatalogProgram newprogram = OBDADataFactoryImpl.getInstance().getDatalogProgram(program.getQueryModifiers());
			for (CQIE query : program.getRules()) {
				CQIE newquery = vocabularyValidator.replaceEquivalences(query);
				newprogram.appendRule(newquery);
			}

			for (CQIE q : newprogram.getRules()) 
				DatalogNormalizer.unfoldJoinTrees(q);
			log.debug("Normalized program: \n{}", newprogram);

			if (newprogram.getRules().size() < 1) 
				throw new OBDAException("Error, the translation of the query generated 0 rules. This is not possible for any SELECT query (other queries are not supported by the translator).");

			log.debug("Start the rewriting process...");

			//final long startTime0 = System.currentTimeMillis();
			for (CQIE cq : newprogram.getRules())
				CQCUtilities.optimizeQueryWithSigmaRules(cq.getBody(), sigma);
			DatalogProgram programAfterRewriting = rewriter.rewrite(newprogram);
			
			//rewritingTime = System.currentTimeMillis() - startTime0;

			//final long startTime = System.currentTimeMillis();
			log.debug("Start the partial evaluation process...");

			DatalogUnfolder datalogUnfolder = unfolder.getDatalogUnfolder();

			// TODO: make it final
			DatalogProgram programAfterUnfolding = datalogUnfolder
					.unfold(programAfterRewriting,
							"ans1", QuestConstants.BUP, true);
			log.debug("Data atoms evaluated: \n{}", programAfterUnfolding);


			ExpressionEvaluator evaluator = new ExpressionEvaluator(unfolder.getUriTemplateMatcher());
			evaluator.evaluateExpressions(programAfterUnfolding);
			log.debug("Boolean expression evaluated: \n{}", programAfterUnfolding);

			if (programAfterUnfolding.getRules().isEmpty()) {
				throw new EmptyQueryException();
			}
			else {
				try {
					IntermediateQuery intermediateQuery = DatalogProgram2QueryConverter.convertDatalogProgram(
							unfolder.getMetadataForQueryOptimization(), programAfterUnfolding,
							datalogUnfolder.getExtensionalPredicates());
					log.debug("New directly translated intermediate query: \n" + intermediateQuery.toString());

					// BasicTypeLiftOptimizer typeLiftOptimizer = new BasicTypeLiftOptimizer();
					// intermediateQuery = typeLiftOptimizer.optimize(intermediateQuery);

					log.debug("New lifted query: \n" + intermediateQuery.toString());


					BasicJoinOptimizer joinOptimizer = new BasicJoinOptimizer();
					intermediateQuery = joinOptimizer.optimize(intermediateQuery);
					log.debug("New query after join optimization: \n" + intermediateQuery.toString());

					log.debug("Partial evaluation ended.");

					executableQuery = generateExecutableQuery(intermediateQuery, ImmutableList.copyOf(signature),
							optionalConstructTemplate);
					queryCache.put(pq, executableQuery);
					return executableQuery;


				} catch (DatalogProgram2QueryConverter.InvalidDatalogProgramException e) {
					throw new OBDAException(e.getLocalizedMessage());
				}
				/**
				 * No solution.
				 */
				catch (EmptyQueryException e) {
					ExecutableQuery emptyQuery = datasourceQueryGenerator.generateEmptyQuery(
							ImmutableList.copyOf(signature), optionalConstructTemplate);

					log.debug("Empty query --> no solution.");
					queryCache.put(pq, emptyQuery);
					return emptyQuery;
				}
			}

			//unfoldingTime = System.currentTimeMillis() - startTime;
		}
		catch (EmptyQueryException e) {
			return datasourceQueryGenerator.generateEmptyQuery(ImmutableList.copyOf(signature), optionalConstructTemplate);
		}
		catch (OBDAException e) {
			throw e;
		}
		catch (Exception e) {
			log.debug(e.getMessage(), e);
			e.printStackTrace();

			OBDAException ex = new OBDAException("Error rewriting and unfolding into SQL\n" + e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}

	private ExecutableQuery generateExecutableQuery(IntermediateQuery intermediateQuery, ImmutableList<String> signature,
													Optional<SesameConstructTemplate> optionalConstructTemplate) throws OBDAException {
		log.debug("Producing the native query string...");

		ExecutableQuery executableQuery = datasourceQueryGenerator.generateSourceQuery(intermediateQuery, signature, optionalConstructTemplate);

		log.debug("Resulting native query: \n{}", executableQuery);

		return executableQuery;
	}


	/**
	 * Returns the final rewriting of the given query
	 */
	public String getRewriting(ParsedQuery query) throws OBDAException {
		try {
			DatalogProgram program = translateAndPreProcess(query);
			DatalogProgram rewriting = rewriter.rewrite(program);
			return DatalogProgramRenderer.encode(rewriting);
		}
		catch (Exception e) {
			log.debug(e.getMessage(), e);
			e.printStackTrace();
			OBDAException ex = new OBDAException("Error rewriting\n" +  e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	
	/**
	 * Rewrites the given input SPARQL query and returns back an expanded SPARQL
	 * query. The query expansion involves query transformation from SPARQL
	 * algebra to Datalog objects and then translating back to SPARQL algebra.
	 * The transformation to Datalog is required to apply the rewriting
	 * algorithm.
	 * 
	 * @param sparql
	 *            The input SPARQL query.
	 * @return An expanded SPARQL query.
	 * @throws OBDAException
	 *             if errors occur during the transformation and translation.
	 */
	public String getSPARQLRewriting(String sparql) throws OBDAException {
		if (!SPARQLQueryUtility.isSelectQuery(sparql)) {
			throw new OBDAException("Support only SELECT query");
		}
		try {
			// Parse the SPARQL string into SPARQL algebra object
			ParsedQuery query = getParsedQuery(sparql);
			
			// Translate the SPARQL algebra to datalog program
			DatalogProgram initialProgram = translateAndPreProcess(query);
			
			// Perform the query rewriting
			DatalogProgram programAfterRewriting = rewriter.rewrite(initialProgram);
			
			// Translate the output datalog program back to SPARQL string
			// TODO Re-enable the prefix manager using Sesame prefix manager
//			PrefixManager prefixManager = new SparqlPrefixManager(query.getPrefixMapping());
			DatalogToSparqlTranslator datalogTranslator = new DatalogToSparqlTranslator();
			return datalogTranslator.translate(programAfterRewriting);
		} 
		catch (Exception e) {
			log.debug(e.getMessage(), e);
			e.printStackTrace();
			OBDAException ex = new OBDAException("Error rewriting\n" +  e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}

	public boolean hasDistinctResultSet() {
		return hasDistinctResultSet;
	}
}
