package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.generation.TemporalNativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.rewriting.LinearInclusionDependencyTools;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.answering.reformulation.rewriting.SameAsRewriter;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.CQCUtilities;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DBMetadataMerger;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.injection.TemporalTranslationFactory;
import it.unibz.inf.ontop.injection.TranslationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.reformulation.RuleUnfolder;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.TemporalOBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.mapping.transformer.RedundantTemporalCoalesceEliminator;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalogProgram2QueryConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.atom.PredicateConstants.ONTOP_QUERY;

public class TemporalQueryProcessor implements QueryReformulator {

    private final QueryRewriter rewriter;
    private final LinearInclusionDependencies sigma;
    private final TemporalNativeQueryGenerator datasourceQueryGenerator;
    private final QueryCache queryCache;

    //private final QueryUnfolder queryUnfolder;
    private final SameAsRewriter sameAsRewriter;
    private final BindingLiftOptimizer bindingLiftOptimizer;

    private static final Logger log = LoggerFactory.getLogger(TemporalQueryProcessor.class);
    private final ExecutorRegistry executorRegistry;
    private final TemporalDatalogProgram2QueryConverter datalogConverter;
    private final OntopReformulationSettings settings;
    private final DBMetadata dbMetadata;
    private final DBMetadata temporalDBMetadata;
    private final JoinLikeOptimizer joinLikeOptimizer;
    private final InputQueryTranslator inputQueryTranslator;
    private final InputQueryFactory inputQueryFactory;
    private final DatalogFactory datalogFactory;
    private final DatalogNormalizer datalogNormalizer;
    private final EQNormalizer eqNormalizer;
    private final UnifierUtilities unifierUtilities;
    private final SubstitutionUtilities substitutionUtilities;
    private final CQCUtilities cqcUtilities;
    private final ImmutabilityTools immutabilityTools;
    private final RuleUnfolder ruleUnfolder;
    private final RedundantTemporalCoalesceEliminator tcEliminator;
    private final TemporalMapping temporalSaturatedMapping;

    ImmutableMap<AtomPredicate, IntermediateQuery> mergedMappingMap;

    @AssistedInject
    private TemporalQueryProcessor(@Assisted OBDASpecification obdaSpecification,
                                   @Assisted ExecutorRegistry executorRegistry,
                                   QueryCache queryCache,
                                   BindingLiftOptimizer bindingLiftOptimizer, OntopReformulationSettings settings,
                                   TemporalDatalogProgram2QueryConverter datalogConverter,
                                   TranslationFactory translationFactory,
                                   QueryRewriter queryRewriter,
                                   JoinLikeOptimizer joinLikeOptimizer,
                                   InputQueryFactory inputQueryFactory,
                                   LinearInclusionDependencyTools inclusionDependencyTools,
                                   AtomFactory atomFactory, TermFactory termFactory, DatalogFactory datalogFactory,
                                   DatalogNormalizer datalogNormalizer, EQNormalizer eqNormalizer,
                                   UnifierUtilities unifierUtilities, SubstitutionUtilities substitutionUtilities,
                                   CQCUtilities cqcUtilities, ImmutabilityTools immutabilityTools, RuleUnfolder ruleUnfolder,
                                   TemporalTranslationFactory temporalTranslationFactory,
                                   DBMetadataMerger dbMetadataMerger, RedundantTemporalCoalesceEliminator tcEliminator) {
        this.bindingLiftOptimizer = bindingLiftOptimizer;
        this.settings = settings;
        this.joinLikeOptimizer = joinLikeOptimizer;
        this.inputQueryFactory = inputQueryFactory;
        this.datalogFactory = datalogFactory;
        this.datalogNormalizer = datalogNormalizer;
        this.eqNormalizer = eqNormalizer;
        this.unifierUtilities = unifierUtilities;
        this.substitutionUtilities = substitutionUtilities;
        this.cqcUtilities = cqcUtilities;
        this.immutabilityTools = immutabilityTools;
        this.ruleUnfolder = ruleUnfolder;
        this.tcEliminator = tcEliminator;
        ClassifiedTBox saturatedTBox = obdaSpecification.getSaturatedTBox();
        this.sigma = inclusionDependencyTools.getABoxDependencies(saturatedTBox, true);

        this.rewriter = queryRewriter;
        this.rewriter.setTBox(saturatedTBox, sigma);

        Mapping saturatedMapping = obdaSpecification.getSaturatedMapping();
        temporalSaturatedMapping = ((TemporalOBDASpecification)obdaSpecification).getTemporalSaturatedMapping();

        mergedMappingMap = mergeMappings(saturatedMapping, temporalSaturatedMapping);

        if(log.isDebugEnabled()){
            log.debug("Mapping: \n{}", Joiner.on("\n").join(saturatedMapping.getQueries()));
        }

        //this.queryUnfolder = translationFactory.create(temporalSaturatedMapping);

        this.dbMetadata = obdaSpecification.getDBMetadata();
        this.temporalDBMetadata = ((TemporalOBDASpecification)obdaSpecification).getTemporalDBMetadata();
        DBMetadata mergedDBMetadata = dbMetadataMerger.mergeDBMetadata((RDBMetadata) temporalDBMetadata, (RDBMetadata) dbMetadata);
        this.datasourceQueryGenerator = temporalTranslationFactory.create(mergedDBMetadata);
        this.inputQueryTranslator = translationFactory.createInputQueryTranslator(saturatedMapping.getMetadata()
                .getUriTemplateMatcher());
        this.sameAsRewriter = translationFactory.createSameAsRewriter(saturatedMapping);
        this.queryCache = queryCache;
        this.executorRegistry = executorRegistry;
        this.datalogConverter = datalogConverter;

        log.info("Ontop has completed the setup and it is ready for query answering!");
    }

    private DatalogProgram translateAndPreProcess(InputQuery inputQuery)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {
        InternalSparqlQuery translation = inputQuery.translate(inputQueryTranslator);
        return preProcess(translation);
    }

    private DatalogProgram preProcess(InternalSparqlQuery translation) {
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

            for (CQIE q : newprogram.getRules())
                datalogNormalizer.unfoldJoinTrees(q);
            log.debug("Normalized program: \n{}", newprogram);

            if (newprogram.getRules().size() < 1)
                throw new OntopInvalidInputQueryException("Error, the translation of the query generated 0 rules. " +
                        "This is not possible for any SELECT query (other queries are not supported by the translator).");

            log.debug("Start the rewriting process...");

            //final long startTime0 = System.currentTimeMillis();
            for (CQIE cq : newprogram.getRules())
                cqcUtilities.optimizeQueryWithSigmaRules(cq.getBody(), sigma);
            DatalogProgram programAfterRewriting = rewriter.rewrite(newprogram);

            //rewritingTime = System.currentTimeMillis() - startTime0;

            //final long startTime = System.currentTimeMillis();

            try {
                IntermediateQuery intermediateQuery = datalogConverter.convertDatalogProgram(
                        dbMetadata, programAfterRewriting, ImmutableList.of(), executorRegistry);

                log.debug("Directly translated (SPARQL) intermediate query: \n" + intermediateQuery.toString());

                log.debug("Start the unfolding...");

                //intermediateQuery = queryUnfolder.optimize(intermediateQuery);
                intermediateQuery = ruleUnfolder.unfold(intermediateQuery, mergedMappingMap);

                log.debug("Unfolded query: \n" + intermediateQuery.toString());

                intermediateQuery = tcEliminator.removeRedundantTemporalCoalesces(intermediateQuery,temporalDBMetadata, temporalSaturatedMapping.getExecutorRegistry());

                log.debug("Redundant temporal coalesces Eliminated: \n" + intermediateQuery.toString());

                //lift bindings and union when it is possible
//                intermediateQuery = bindingLiftOptimizer.optimize(intermediateQuery);
//                log.debug("New query after substitution lift optimization: \n" + intermediateQuery.toString());
//
//                log.debug("New lifted query: \n" + intermediateQuery.toString());

                /*
                 * TODO: USE INJECTION!
//                 */
//                intermediateQuery = new PushUpBooleanExpressionOptimizerImpl(false, immutabilityTools).optimize(intermediateQuery);
//                log.debug("After pushing up boolean expressions: \n" + intermediateQuery.toString());
//
//                intermediateQuery = new ProjectionShrinkingOptimizer().optimize(intermediateQuery);
//
//                log.debug("After projection shrinking: \n" + intermediateQuery.toString());
//
//
//                intermediateQuery = joinLikeOptimizer.optimize(intermediateQuery);
//                log.debug("New query after fixed point join optimization: \n" + intermediateQuery.toString());

//				BasicLeftJoinOptimizer leftJoinOptimizer = new BasicLeftJoinOptimizer();
//				intermediateQuery = leftJoinOptimizer.optimize(intermediateQuery);
//				log.debug("New query after left join optimization: \n" + intermediateQuery.toString());
//
//				BasicJoinOptimizer joinOptimizer = new BasicJoinOptimizer();
//				intermediateQuery = joinOptimizer.optimize(intermediateQuery);
//				log.debug("New query after join optimization: \n" + intermediateQuery.toString());

                //TODO: implement this part
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
        DatalogProgram program = translateAndPreProcess(query);
        DatalogProgram rewriting = rewriter.rewrite(program);
        return DatalogProgramRenderer.encode(rewriting);
    }

    @Override
    public InputQueryFactory getInputQueryFactory() {
        return inputQueryFactory;
    }

    //merge temporal and static saturated mappings
    private ImmutableMap<AtomPredicate, IntermediateQuery> mergeMappings(Mapping mapping, TemporalMapping temporalMapping){
        Map <AtomPredicate, IntermediateQuery> mergedMap = new HashMap<>();
        mergedMap.putAll(mapping.getPredicates().stream()
                .collect(Collectors.toMap(p-> p, p-> mapping.getDefinition(p).get())));
        mergedMap.putAll(temporalMapping.getPredicates().stream()
                .collect(Collectors.toMap(p-> p, p -> temporalMapping.getDefinition(p).get())));
        return ImmutableMap.copyOf(mergedMap);
    }
}
