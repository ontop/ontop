package it.unibz.inf.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.QuestComponentFactory;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.injection.ReformulationFactory;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.*;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingSameAs;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.*;
import it.unibz.inf.ontop.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.DummyReformulator;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.TreeWitnessRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.*;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.pivotalrepr.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.renderer.DatalogProgramRenderer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.reformulation.OBDAQueryProcessor;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParser;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 * BC: TODO: make it explicitly immutable
 */
public class QuestQueryProcessor implements OBDAQueryProcessor {

	private final Map<String, ParsedQuery> parsedQueryCache = new ConcurrentHashMap<>();

	private final QueryRewriter rewriter;
	private final LinearInclusionDependencies sigma;
	private final VocabularyValidator vocabularyValidator;
	private final SemanticIndexURIMap uriMap;
	private final NativeQueryGenerator datasourceQueryGenerator;
	private final QueryCache queryCache;

	private final QueryUnfolder queryUnfolder;
	
	private static final Logger log = LoggerFactory.getLogger(QuestQueryProcessor.class);
	private final ExecutorRegistry executorRegistry;
	private final MetadataForQueryOptimization metadataForOptimization;
	private final DatalogProgram2QueryConverter datalogConverter;
	private final ImmutableSet<Predicate> dataPropertiesAndClassesMapped;
	private final ImmutableSet<Predicate> objectPropertiesMapped;
	private final QuestCoreSettings settings;

	@AssistedInject
	private QuestQueryProcessor(@Assisted OBDASpecification obdaSpecification,
								@Assisted ExecutorRegistry executorRegistry,
								QuestComponentFactory questComponentFactory,
								QueryCache queryCache,
								QuestCoreSettings settings,
								DatalogProgram2QueryConverter datalogConverter,
								ReformulationFactory reformulationFactory) {
		TBoxReasoner saturatedTBox = obdaSpecification.getSaturatedTBox();
		this.sigma = LinearInclusionDependencies.getABoxDependencies(saturatedTBox, true);

		// TODO: use Guice instead
		// Setting up the reformulation engine
		if (!settings.getRequiredBoolean(QuestCoreSettings.REWRITE))
			rewriter = new DummyReformulator();
		else if (settings.getProperty(QuestCoreSettings.REFORMULATION_TECHNIQUE)
				.filter(v -> v.equals(QuestConstants.TW))
				.isPresent())
			rewriter = new TreeWitnessRewriter();
		else
			throw new IllegalArgumentException("Invalid value for argument: " + QuestCoreSettings.REFORMULATION_TECHNIQUE);
		rewriter.setTBox(saturatedTBox, obdaSpecification.getVocabulary(), sigma);

		Mapping saturatedMapping = obdaSpecification.getSaturatedMapping();

		this.queryUnfolder = reformulationFactory.create(saturatedMapping);
		this.metadataForOptimization = saturatedMapping.getMetadataForOptimization();

		this.vocabularyValidator = new VocabularyValidator(obdaSpecification.getSaturatedTBox(),
				obdaSpecification.getVocabulary());
		this.datasourceQueryGenerator = questComponentFactory.create(metadataForOptimization.getDBMetadata());
		this.queryCache = queryCache;
		this.settings = settings;
		this.executorRegistry = executorRegistry;
		this.datalogConverter = datalogConverter;

		if (settings.isSameAsInMappingsEnabled()) {
			MappingSameAs msa = new MappingSameAs(saturatedMapping);
			dataPropertiesAndClassesMapped = msa.getDataPropertiesAndClassesWithSameAs();
			objectPropertiesMapped = msa.getObjectPropertiesWithSameAs();
		} else {
			dataPropertiesAndClassesMapped = ImmutableSet.of();
			objectPropertiesMapped = ImmutableSet.of();
		}

		// TODO: get rid of it
		this.uriMap = null;
	}

//	/**
//	 * Returns a new QuestQueryProcessor(will be immutable in the future)
//	 *
//	 * Specific to the Classic A-box mode!
//	 */
//	public QuestQueryProcessor changeMappings(Collection<OBDAMappingAxiom> mappings, TBoxReasoner reformulationReasoner) {
//		// TODO: clone it and then configure it
//		// TODO: check that it is in the classic A-box mode
//		unfolder.changeMappings(mappings, reformulationReasoner);
//		queryCache.clear();
//		return new QuestQueryProcessor(rewriter, sigma, unfolder, vocabularyValidator, uriMap, datasourceQueryGenerator,
//				queryCache, hasDistinctResultSet, modelFactory, executorRegistry);
//	}

	/**
	 * BC: TODO: rename parseSPARQL
     */
	@Override
	public ParsedQuery getParsedQuery(String sparql) throws MalformedQueryException {
		ParsedQuery pq = parsedQueryCache.get(sparql);
		if (pq == null) {
			QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
			pq = parser.parseQuery(sparql, null);
			parsedQueryCache.put(sparql,  pq);
		}
		return pq;
	}

	
	private DatalogProgram translateAndPreProcess(ParsedQuery pq) {
		SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
				metadataForOptimization.getUriTemplateMatcher(), uriMap);
		SparqlQuery translation = translator.translate(pq);
		return preProcess(translation);
	}

	private DatalogProgram preProcess(SparqlQuery translation) {
		DatalogProgram program = translation.getProgram();
		log.debug("Datalog program translated from the SPARQL query: \n{}", program);

		if (settings.isSameAsInMappingsEnabled()) {
			SameAsRewriter sameAs = new SameAsRewriter(dataPropertiesAndClassesMapped, objectPropertiesMapped);
			program = sameAs.getSameAsRewriting(program);
			//System.out.println("SAMEAS" + program);
		}

		log.debug("Replacing equivalences...");
		DatalogProgram newprogramEq = DATA_FACTORY.getDatalogProgram(program.getQueryModifiers());
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
		DatalogProgram newprogram = DATA_FACTORY.getDatalogProgram(program.getQueryModifiers(), p);

		return newprogram;
	}
	
	
	public void clearNativeQueryCache() {
		queryCache.clear();
	}
	

	@Override
	public ExecutableQuery translateIntoNativeQuery(ParsedQuery pq,
													Optional<SesameConstructTemplate> optionalConstructTemplate)
			throws OBDAException {

		ExecutableQuery executableQuery = queryCache.get(pq);
		if (executableQuery != null)
			return executableQuery;

		try {
			SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
					metadataForOptimization.getUriTemplateMatcher(), uriMap);
			SparqlQuery translation = translator.translate(pq);
			DatalogProgram newprogram = preProcess(translation);

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
				IntermediateQuery intermediateQuery = datalogConverter.convertDatalogProgram(
						metadataForOptimization, programAfterRewriting, ImmutableList.of(), executorRegistry);

				log.debug("Directly translated (SPARQL) intermediate query: \n" + intermediateQuery.toString());

				log.debug("Start the unfolding...");

				intermediateQuery = queryUnfolder.optimize(intermediateQuery);

				log.debug("Unfolded query: \n" + intermediateQuery.toString());


				//lift bindings and union when it is possible
				IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();
				intermediateQuery = substitutionOptimizer.optimize(intermediateQuery);


				log.debug("New lifted query: \n" + intermediateQuery.toString());


				JoinLikeOptimizer joinLikeOptimizer = new FixedPointJoinLikeOptimizer();
				intermediateQuery = joinLikeOptimizer.optimize(intermediateQuery);
				log.debug("New query after fixed point join optimization: \n" + intermediateQuery.toString());

//				BasicLeftJoinOptimizer leftJoinOptimizer = new BasicLeftJoinOptimizer();
//				intermediateQuery = leftJoinOptimizer.optimize(intermediateQuery);
//				log.debug("New query after left join optimization: \n" + intermediateQuery.toString());
//
//				BasicJoinOptimizer joinOptimizer = new BasicJoinOptimizer();
//				intermediateQuery = joinOptimizer.optimize(intermediateQuery);
//				log.debug("New query after join optimization: \n" + intermediateQuery.toString());

				executableQuery = generateExecutableQuery(intermediateQuery, ImmutableList.copyOf(translation.getSignature()),
						optionalConstructTemplate);
				queryCache.put(pq, executableQuery);
				return executableQuery;

			} catch (DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException e) {
				throw new OBDAException(e.getLocalizedMessage());
			}
			/**
			 * No solution.
			 */
			catch (EmptyQueryException e) {
				ExecutableQuery emptyQuery = datasourceQueryGenerator.generateEmptyQuery(
						ImmutableList.copyOf(translation.getSignature()), optionalConstructTemplate);

				log.debug("Empty query --> no solution.");
				queryCache.put(pq, emptyQuery);
				return emptyQuery;
			}

			//unfoldingTime = System.currentTimeMillis() - startTime;
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
	@Override
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
	@Override
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

	@Override
	public boolean hasDistinctResultSet() {
		return settings.isDistinctPostProcessingEnabled();
	}

	@Override
	public DBMetadata getDBMetadata() {
		return metadataForOptimization.getDBMetadata();
	}
}
