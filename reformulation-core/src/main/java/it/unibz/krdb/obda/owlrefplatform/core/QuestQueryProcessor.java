package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
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

import java.util.ArrayList;
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
	
	/**
	 * CAN BE CALLED ONLY AFTER getSQL
	 * 
	 * @param pq
	 * @return
	 */
	public List<String> getQuerySignature(ParsedQuery pq) {
		List<String> signature = querySignatureCache.get(pq);
		return signature;
	}
	
	
	private DatalogProgram translateAndPreProcess(ParsedQuery pq)  {
		
		SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(unfolder.getUriTemplateMatcher(), uriMap);	
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
	
	
	public void clearSQLCache() {
		translatedSQLCache.clear();
	}
	
	
	public String getSQL(ParsedQuery pq) throws OBDAException {
			
		String cachedSQL = translatedSQLCache.get(pq);
		if (cachedSQL != null)
			return cachedSQL;
		
		try {
			// log.debug("Input query:\n{}", strquery);
			
			SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(unfolder.getUriTemplateMatcher(), uriMap);	
			DatalogProgram translation = translator.translate(pq);

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

			DatalogProgram programAfterUnfolding = unfolder.unfold(programAfterRewriting);
			log.debug("Data atoms evaluated: \n{}", programAfterUnfolding);

			List<CQIE> toRemove = new LinkedList<>();
			for (CQIE rule : programAfterUnfolding.getRules()) {
				Predicate headPredicate = rule.getHead().getFunctionSymbol();
				if (!headPredicate.getName().equals(OBDAVocabulary.QUEST_QUERY)) {
					toRemove.add(rule);
				}
			}
			programAfterUnfolding.removeRules(toRemove);
			log.debug("Irrelevant rules removed: \n{}", programAfterUnfolding);

			ExpressionEvaluator evaluator = new ExpressionEvaluator(unfolder.getUriTemplateMatcher());
			evaluator.evaluateExpressions(programAfterUnfolding);
			
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

			log.debug("Boolean expression evaluated: \n{}", programAfterUnfolding);
			log.debug("Partial evaluation ended.");
			//unfoldingTime = System.currentTimeMillis() - startTime;

			List<String> signature = null;
			 // IMPORTANT: this is the original query 
			// (with original variable names, not the BINDings after flattening)
			for (CQIE q : translation.getRules()) 
				if (q.getHead().getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_QUERY)) {
					List<Term> terms = q.getHead().getTerms();
					signature = new ArrayList<>(terms.size());
					for (Term t : terms)
						signature.add(((Variable)t).getName()); // ALL VARIABLES by construction
					break;
				}
			querySignatureCache.put(pq, signature);
	
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
		catch (Exception e) {
			log.debug(e.getMessage(), e);
			e.printStackTrace();
			OBDAException ex = new OBDAException("Error rewriting and unfolding into SQL\n" +  e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
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
}
