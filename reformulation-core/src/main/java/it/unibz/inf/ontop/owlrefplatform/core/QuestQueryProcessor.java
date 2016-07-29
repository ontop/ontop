package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.*;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.BasicJoinOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.TopDownSubstitutionLiftOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.impl.QueryUnfolderImpl;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.*;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.renderer.DatalogProgramRenderer;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.pivotalrepr.datalog.Mapping2QueryConverter.convertMappings;

/**
 * BC: TODO: make it explicitly immutable
 */
public class QuestQueryProcessor {
	
	private final Map<String, ParsedQuery> parsedQueryCache = new ConcurrentHashMap<>();
	private final Map<ParsedQuery, List<String>> querySignatureCache = new ConcurrentHashMap<>();
	private final Map<ParsedQuery, String> translatedSQLCache = new ConcurrentHashMap<>();
	
	private final QueryRewriter rewriter;
	private final LinearInclusionDependencies sigma;
	private final VocabularyValidator vocabularyValidator;
	private final SemanticIndexURIMap uriMap;
	private final SQLQueryGenerator datasourceQueryGenerator;
	private final QueryUnfolder queryUnfolder;
	/**
	 * Old-style Datalog unfolder (not used for unfolding anymore)
	 */
	protected final QuestUnfolder unfolder;
	
	private static final Logger log = LoggerFactory.getLogger(QuestQueryProcessor.class);
	private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

	public QuestQueryProcessor(QueryRewriter rewriter, LinearInclusionDependencies sigma, QuestUnfolder unfolder, 
			VocabularyValidator vocabularyValidator, SemanticIndexURIMap uriMap, SQLQueryGenerator datasourceQueryGenerator) {
		this.rewriter = rewriter;
		this.sigma = sigma;
		this.unfolder = unfolder;

		Stream<IntermediateQuery> intermediateQueryStream =
				convertMappings(unfolder.getMappings(), unfolder.getExtensionalPredicates(), unfolder.getMetadataForQueryOptimization());

		this.queryUnfolder = new QueryUnfolderImpl(intermediateQueryStream);

		this.vocabularyValidator = vocabularyValidator;
		this.uriMap = uriMap;
		this.datasourceQueryGenerator = datasourceQueryGenerator;
	}

	/**
	 * Returns a new QuestQueryProcessor(will be immutable in the future)
	 *
	 * Specific to the Classic A-box mode!
	 */
	public QuestQueryProcessor changeMappings(Collection<OBDAMappingAxiom> mappings, TBoxReasoner reformulationReasoner) {
		// TODO: clone it and then configure it
		// TODO: check that it is in the classic A-box mode
		unfolder.setupInSemanticIndexMode(mappings, reformulationReasoner);
		return new QuestQueryProcessor(rewriter, sigma, unfolder, vocabularyValidator, uriMap, datasourceQueryGenerator);
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
	
	/**
	 * CAN BE CALLED ONLY AFTER getSQL
	 *
	 * BC: TODO: we should avoid relying on a cache, looks hacky
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
		SparqlQuery translation = translator.translate(pq);
		DatalogProgram program = translation.getProgram();
		log.debug("Datalog program translated from the SPARQL query: \n{}", program);

		SameAsRewriter sameAs = new SameAsRewriter(unfolder.getSameAsDataPredicatesAndClasses(), unfolder.getSameAsObjectPredicates());
		program = sameAs.getSameAsRewriting(program);
		//System.out.println("SAMEAS" + program);

		log.debug("Replacing equivalences...");
		DatalogProgram newprogramEq = OBDADataFactoryImpl.getInstance().getDatalogProgram(program.getQueryModifiers());
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
		DatalogProgram newprogram = OBDADataFactoryImpl.getInstance().getDatalogProgram(program.getQueryModifiers(), p);

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
			SparqlQuery translation = translator.translate(pq);
			DatalogProgram program = translation.getProgram();
			log.debug("Datalog program translated from the SPARQL query: \n{}", program);
			//System.out.println("OUT " + program);

			SameAsRewriter sameAs = new SameAsRewriter(unfolder.getSameAsDataPredicatesAndClasses(), unfolder.getSameAsObjectPredicates());
			program = sameAs.getSameAsRewriting(program);
			//System.out.println("SAMEAS" + program);

			log.debug("Replacing equivalences...");
			DatalogProgram newprogramEq = OBDADataFactoryImpl.getInstance().getDatalogProgram(program.getQueryModifiers());
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
			log.debug("Program before flattening: \n{}", newprogramEq);

			SPARQLQueryFlattener fl = new SPARQLQueryFlattener(newprogramEq);
			List<CQIE> p = fl.flatten(newprogramEq.getRules(topLevelPredicate).get(0));
			DatalogProgram newprogram = OBDADataFactoryImpl.getInstance().getDatalogProgram(program.getQueryModifiers(), p);

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

			DatalogProgram programAfterUnfolding;
			try {
				IntermediateQuery intermediateQuery = DatalogProgram2QueryConverter.convertDatalogProgram(
						unfolder.getMetadataForQueryOptimization(), programAfterRewriting,
						unfolder.getExtensionalPredicates());
				log.debug("Directly translated (SPARQL) intermediate query: \n" + intermediateQuery.toString());

				log.debug("Start the unfolding...");

				intermediateQuery = queryUnfolder.optimize(intermediateQuery);

				log.debug("Unfolded query: \n" + intermediateQuery.toString());


				//lift bindings and union when it is possible
				IntermediateQueryOptimizer substitutionOptimizer = new TopDownSubstitutionLiftOptimizer();
				intermediateQuery = substitutionOptimizer.optimize(intermediateQuery);


				log.debug("New lifted query: \n" + intermediateQuery.toString());


				BasicJoinOptimizer joinOptimizer = new BasicJoinOptimizer();
				intermediateQuery = joinOptimizer.optimize(intermediateQuery);
				log.debug("New query after join optimization: \n" + intermediateQuery.toString());


				programAfterUnfolding = IntermediateQueryToDatalogTranslator.translate(intermediateQuery);

				log.debug("New Datalog query: \n" + programAfterUnfolding.toString());

				programAfterUnfolding = FunctionFlattener.flattenDatalogProgram(programAfterUnfolding);
				log.debug("New flattened Datalog query: \n" + programAfterUnfolding.toString());

				log.debug("Pulling out equalities...");

				PullOutEqualityNormalizer normalizer = new PullOutEqualityNormalizerImpl();

				List<CQIE> normalizedRules = new ArrayList<>();
				for (CQIE rule: programAfterUnfolding.getRules()) {
					normalizedRules.add(normalizer.normalizeByPullingOutEqualities(rule));
				}

				OBDAQueryModifiers queryModifiers = programAfterUnfolding.getQueryModifiers();
				programAfterUnfolding = DATA_FACTORY.getDatalogProgram(queryModifiers, normalizedRules);

			} catch (DatalogProgram2QueryConverter.InvalidDatalogProgramException e) {
				throw new OBDAException(e.getLocalizedMessage());
			}
			/**
			 * No solution.
			 */
			catch (EmptyQueryException e) {

				log.debug("Empty query --> no solution.");
				/**
				 * TODO: should we really return an sql query?
				 */
				String sql = "";
				querySignatureCache.put(pq, translation.getSignature());
				translatedSQLCache.put(pq, sql);
				return sql;
			}

			log.debug("Partial evaluation ended.");
			//unfoldingTime = System.currentTimeMillis() - startTime;

			querySignatureCache.put(pq, translation.getSignature());

			String sql;
			if (programAfterUnfolding.getRules().size() > 0) {
				log.debug("Producing the SQL string...");
				sql = datasourceQueryGenerator.generateSourceQuery(programAfterUnfolding, translation.getSignature());
				log.debug("Resulting SQL: \n{}", sql);
			}
			else
				sql = "";
			
			translatedSQLCache.put(pq, sql);
			return sql;
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
