package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.DatalogToSparqlTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.SPARQLQueryFlattener;
import it.unibz.krdb.obda.renderer.DatalogProgramRenderer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestQueryProcessor {
	
	private final Map<String, ParsedQuery> parsedQueryCache = new ConcurrentHashMap<>();
	private final Map<ParsedQuery, List<String>> querySignatureCache = new ConcurrentHashMap<>();
	private final Map<ParsedQuery, String> translatedSQLCache = new ConcurrentHashMap<>();
	
	private final QueryRewriter rewriter;
	private final LinearInclusionDependencies sigma;
	protected final QuestUnfolder unfolder;
	private final VocabularyValidator vocabularyValidator;
	private final SemanticIndexURIMap uriMap;
	private final SQLQueryGenerator datasourceQueryGenerator;
	
	private static final Logger log = LoggerFactory.getLogger(QuestQueryProcessor.class);
	
	public QuestQueryProcessor(QueryRewriter rewriter, LinearInclusionDependencies sigma, QuestUnfolder unfolder, 
			VocabularyValidator vocabularyValidator, SemanticIndexURIMap uriMap, SQLQueryGenerator datasourceQueryGenerator) {
		this.rewriter = rewriter;
		this.sigma = sigma;
		this.unfolder = unfolder;
		this.vocabularyValidator = vocabularyValidator;
		this.uriMap = uriMap;
		this.datasourceQueryGenerator = datasourceQueryGenerator;
	}
	
	public ParsedQuery getParsedQuery(String sparql) throws MalformedQueryException {
		ParsedQuery pq = parsedQueryCache.get(sparql);
		if (pq == null) {
			QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
			pq = parser.parseQuery(sparql, null);
			parsedQueryCache.put(sparql,  pq);
		}
		return pq;
	}
	
	public List<String> getQuerySignature(ParsedQuery pq) {
		List<String> signature = querySignatureCache.get(pq);
		if (signature == null) {
			signature = SparqlAlgebraToDatalogTranslator.getSignature(pq);
			querySignatureCache.put(pq, signature);
		}
		return signature;
	}
	
	
	private DatalogProgram translateAndPreProcess(ParsedQuery pq) throws OBDAException {
		DatalogProgram program;
		try {
			SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(unfolder.getUriTemplateMatcher(), uriMap);	
			program = translator.translate(pq);

			log.debug("Datalog program translated from the SPARQL query: \n{}", program);

			SPARQLQueryFlattener unfolder = new SPARQLQueryFlattener(program);
			program = unfolder.flatten();

			log.debug("Flattened program: \n{}", program);
		} 
		catch (Exception e) {
			e.printStackTrace();
			OBDAException ex = new OBDAException(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
		log.debug("Replacing equivalences...");
		DatalogProgram newprogram = OBDADataFactoryImpl.getInstance().getDatalogProgram(program.getQueryModifiers());
		for (CQIE query : program.getRules()) {
			CQIE newquery = vocabularyValidator.replaceEquivalences(query);
			newprogram.appendRule(newquery);
		}
		return newprogram;		
	}
	
	
	public void clearSQLCache() {
		translatedSQLCache.clear();
	}
	
	
	public String getSQL(ParsedQuery pq) throws OBDAException {
		
		DatalogProgram program = translateAndPreProcess(pq);
		try {
			// log.debug("Input query:\n{}", strquery);

			for (CQIE q : program.getRules()) 
				DatalogNormalizer.unfoldJoinTrees(q);

				log.debug("Normalized program: \n{}", program);

			/*
			 * Empty unfolding, constructing an empty result set
			 */
			if (program.getRules().size() < 1) 
				throw new OBDAException("Error, the translation of the query generated 0 rules. This is not possible for any SELECT query (other queries are not supported by the translator).");

			log.debug("Start the rewriting process...");

			//final long startTime0 = System.currentTimeMillis();
			DatalogProgram programAfterRewriting = getOptimizedRewriting(program);
			//rewritingTime = System.currentTimeMillis() - startTime0;

			//final long startTime = System.currentTimeMillis();
			DatalogProgram programAfterUnfolding = getUnfolding(programAfterRewriting);
			//unfoldingTime = System.currentTimeMillis() - startTime;

			List<String> signature = getQuerySignature(pq);

			String sql;
			if (programAfterUnfolding.getRules().size() > 0) {
				log.debug("Producing the SQL string...");
				sql = datasourceQueryGenerator.generateSourceQuery(programAfterUnfolding, signature);
				log.debug("Resulting SQL: \n{}", sql);
			}
			else
				sql = "";
			
			translatedSQLCache.put(pq, sql);
			return sql;
		} 
		catch (Exception e1) {
			log.debug(e1.getMessage(), e1);

			OBDAException obdaException = new OBDAException("Error rewriting and unfolding into SQL\n" + e1.getMessage());
			obdaException.setStackTrace(e1.getStackTrace());
			throw obdaException;
		}	
	}
	

	/***
	 * Returns the SQL query for a given SPARQL query. In the process, the
	 * signature of the query will be set into the query container and the jena
	 * Query object created (or cached) will be set as jenaQueryContainer[0] so
	 * that it can be used in other process after getUnfolding.
	 * 
	 * If the query is not already cached, it will be cached in this process.
	 * 
	 * @param strquery
	 * @return
	 * @throws Exception
	 */
	public String getSQL(String sparql) throws Exception {
		ParsedQuery pq = getParsedQuery(sparql);
		String sql = getSQL(pq);
		return sql;
	}
	
	
	private DatalogProgram getOptimizedRewriting(DatalogProgram cqie) throws OBDAException {
		// Query optimization w.r.t Sigma rules
		for (CQIE cq : cqie.getRules())
			CQCUtilities.optimizeQueryWithSigmaRules(cq.getBody(), sigma);
		cqie = rewriter.rewrite(cqie);
		for (CQIE cq : cqie.getRules())
			CQCUtilities.optimizeQueryWithSigmaRules(cq.getBody(), sigma);
		return cqie;
	}
	
	public DatalogProgram getRewriting(DatalogProgram cqie) throws OBDAException {
		return rewriter.rewrite(cqie);
	}

	/**
	 * Returns the final rewriting of the given query
	 */
	public String getRewriting(ParsedQuery query) throws Exception {
		// TODO FIX to limit to SPARQL input and output

		DatalogProgram program = translateAndPreProcess(query);

		DatalogProgram rewriting = getRewriting(program);
		return DatalogProgramRenderer.encode(rewriting);
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
			DatalogProgram programAfterRewriting = getRewriting(initialProgram);
			
			// Translate the output datalog program back to SPARQL string
			// TODO Re-enable the prefix manager using Sesame prefix manager
//			PrefixManager prefixManager = new SparqlPrefixManager(query.getPrefixMapping());
			DatalogToSparqlTranslator datalogTranslator = new DatalogToSparqlTranslator();
			return datalogTranslator.translate(programAfterRewriting);
		} 
		catch (MalformedQueryException e) {
			throw new OBDAException(e);
		}
	}

	
	
	
	private DatalogProgram getUnfolding(DatalogProgram query) throws OBDAException {

		log.debug("Start the partial evaluation process...");

		DatalogProgram unfolding = unfolder.unfold(query);
		//log.debug("Partial evaluation: \n{}", unfolding);
		log.debug("Data atoms evaluated: \n{}", unfolding);

		removeNonAnswerQueries(unfolding);

		//log.debug("After target rules removed: \n{}", unfolding);
		log.debug("Irrelevant rules removed: \n{}", unfolding);

		ExpressionEvaluator evaluator = new ExpressionEvaluator(unfolder.getUriTemplateMatcher());
		evaluator.evaluateExpressions(unfolding);
		
		/*
			UnionOfSqlQueries ucq = new UnionOfSqlQueries(questInstance.getUnfolder().getCQContainmentCheck());
			for (CQIE cq : unfolding.getRules())
				ucq.add(cq);
			
			List<CQIE> rules = new ArrayList<>(unfolding.getRules());
			unfolding.removeRules(rules); 
			
			for (CQIE cq : ucq.asCQIE()) {
				unfolding.appendRule(cq);
			}
			log.debug("CQC performed ({} rules): \n{}", unfolding.getRules().size(), unfolding);
		 
		 */

		log.debug("Boolean expression evaluated: \n{}", unfolding);
		log.debug("Partial evaluation ended.");

		return unfolding;
	}

	private static void removeNonAnswerQueries(DatalogProgram program) {
		List<CQIE> toRemove = new LinkedList<>();
		for (CQIE rule : program.getRules()) {
			Predicate headPredicate = rule.getHead().getFunctionSymbol();
			if (!headPredicate.getName().toString().equals(OBDAVocabulary.QUEST_QUERY)) {
				toRemove.add(rule);
			}
		}
		program.removeRules(toRemove);
	}

	
	
}
