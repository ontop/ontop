package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.TranslationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.iq.node.SliceNode;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.NodeInGraphOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


/**
 * Transforms the SPARQL query into a native (e.g. SQL) query without post-processing.
 * Introduces the strong-typing restriction on the SPARQL query.
 *
 */
public class ToFullNativeQueryReformulator extends QuestQueryProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToFullNativeQueryReformulator.class);

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IQTreeTools iqTreeTools;

    @AssistedInject
    private ToFullNativeQueryReformulator(@Assisted OBDASpecification obdaSpecification,
                                          QueryCache queryCache,
                                          QueryUnfolder.Factory queryUnfolderFactory,
                                          TranslationFactory translationFactory,
                                          QueryRewriter queryRewriter,
                                          KGQueryFactory kgQueryFactory,
                                          KGQueryTranslator inputQueryTranslator,
                                          GeneralStructuralAndSemanticIQOptimizer generalOptimizer,
                                          NodeInGraphOptimizer nodeInGraphOptimizer,
                                          QueryPlanner queryPlanner,
                                          QueryLogger.Factory queryLoggerFactory,
                                          QueryContext.Factory queryContextFactory,
                                          IntermediateQueryFactory iqFactory,
                                          TermFactory termFactory,
                                          SubstitutionFactory substitutionFactory, IQTreeTools iqTreeTools) {
        super(obdaSpecification, queryCache, queryUnfolderFactory, translationFactory, queryRewriter, kgQueryFactory,
                inputQueryTranslator, generalOptimizer, nodeInGraphOptimizer, queryPlanner, queryLoggerFactory, queryContextFactory);
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
    }


    /**
     * Replaces the RDF terms by DB terms with the corresponding natural DB datatype.
     * Rejects non-strongly typed queries.
     */
    @Override
    protected IQ generateExecutableQuery(IQ iq) throws OntopReformulationException {
        DistinctVariableOnlyDataAtom initialProjectionAtom = iq.getProjectionAtom();
        IQTree initialTree = iq.getTree();

        Substitution<ImmutableTerm> definitions = extractDefinitions(initialTree);
        ImmutableMap<Variable, RDFTermType> rdfTypes = extractRDFTypes(definitions);

        IQTree dbTree = replaceRDFByDBTerms(initialTree, rdfTypes);

        LOGGER.debug("Producing the native query string...");

        IQ dbIQ = iqFactory.createIQ(initialProjectionAtom, dbTree);

        IQTree nativeTree = datasourceQueryGenerator.generateSourceQuery(dbIQ, true)
                .normalizeForOptimization().getTree();

        if (!(nativeTree instanceof NativeNode))
            throw new NotFullyTranslatableToNativeQueryException("the post-processing step could not be eliminated");

        NativeNode nativeNode = (NativeNode) nativeTree;

        ImmutableMap<Variable, DBTermType> dbTypeMap = nativeNode.getTypeMap();

        /*
         * HACK! The construction node reuses the same variables for the constructed RDF terms and the DB ones.
         * This is illegal but does the trick, as the IQTree does not get normalized anymore at that stage.
         *
         * TODO: find an alternative to that hack while preserving good variable names in the native query.
         *
         */
        ConstructionNode postProcessingToRDFNode = iqFactory.createConstructionNode(
                nativeTree.getVariables(),
                nativeTree.getVariables().stream()
                        .collect(substitutionFactory.toSubstitution(
                                v -> termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversion2RDFLexical(
                                                Optional.ofNullable(dbTypeMap.get(v))
                                                        .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting a type from the native node")),
                                                v,
                                                Optional.ofNullable(rdfTypes.get(v))
                                                        .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting an RDF type"))),
                                        termFactory.getRDFTermTypeConstant(rdfTypes.get(v))))));

        IQTree executableTree = iqFactory.createUnaryIQTree(
                postProcessingToRDFNode,
                nativeTree);

        IQ executableQuery = iqFactory.createIQ(dbIQ.getProjectionAtom(), executableTree);

        LOGGER.debug("Resulting native query:\n{}\n", executableQuery);

        return executableQuery;
    }

    private IQTree replaceRDFByDBTerms(IQTree tree, ImmutableMap<Variable, RDFTermType> rdfTypes) {
        if (rdfTypes.isEmpty())
            return tree;

        var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
        var construction = UnaryIQTreeDecomposition.of(slice, ConstructionNode.class);
        if (!construction.isPresent())
            throw new MinorOntopInternalBugException("Unexpected tree shape (proper exception should have already been thrown)");

        ConstructionNode newConstructionNode = iqTreeTools.replaceSubstitution(
                construction.getNode(),
                s -> s.builder()
                        .transform(rdfTypes::get, this::replaceRDFByDBTerm)
                        .build());

        return iqTreeTools.unaryIQTreeBuilder()
                .append(slice.getOptionalNode())
                .append(newConstructionNode)
                .build(construction.getTail());
    }

    private Substitution<ImmutableTerm> extractDefinitions(IQTree rdfTree) throws NotFullyTranslatableToNativeQueryException {
        if (rdfTree.getVariables().isEmpty())
            return substitutionFactory.getSubstitution();

        var slice = UnaryIQTreeDecomposition.of(rdfTree, SliceNode.class);
        var construction = UnaryIQTreeDecomposition.of(slice, ConstructionNode.class);
        if (!construction.isPresent())
            throw new NotFullyTranslatableToNativeQueryException("was expected to have an extended projection at the top. IQ: " + rdfTree);

        Substitution<ImmutableTerm> substitution = construction.getNode().getSubstitution();
        // NB: should not include any non-projected variable (illegal IQ)
        Set<Variable> missingVariables = Sets.difference(rdfTree.getVariables(), substitution.getDomain());
        if (!missingVariables.isEmpty())
            throw new NotFullyTranslatableToNativeQueryException(String.format(
                    "its variables %s are missing an independent definition",
                    missingVariables));

        return substitution;
    }


    private ImmutableTerm replaceRDFByDBTerm(ImmutableTerm definition,
                                               RDFTermType rdfType) {
        if (definition instanceof Variable)
            return definition;
        if (definition instanceof RDFConstant)
            return termFactory.getConversionFromRDFLexical2DB(
                    termFactory.getDBStringConstant(((RDFConstant) definition).getValue()), rdfType);
        if ((definition instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) definition).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
            return termFactory.getConversionFromRDFLexical2DB(
                    ((ImmutableFunctionalTerm) definition).getTerm(0), rdfType);
        }
        throw new MinorOntopInternalBugException("BI connector: unexpected tree shape " +
                "(proper exception should have already been thrown)");
    }

    private ImmutableMap<Variable, RDFTermType> extractRDFTypes(Substitution<ImmutableTerm> definitions)
            throws NotFullyTranslatableToNativeQueryException {

        try {
            return definitions.builder()
                    .toMap((v, t) -> extractRDFType(v, t, definitions));
        }
        catch (NotFullyTranslatableToNativeQueryRuntimeException e) {
            throw new NotFullyTranslatableToNativeQueryException(e.getMessage());
        }
    }

    private RDFTermType extractRDFType(Variable variable, ImmutableTerm definition, Substitution<ImmutableTerm> definitions)  {
        if (definition instanceof Variable) {
            Variable otherVariable = (Variable) definition;
            // recursively unravel definitions
            return extractRDFType(otherVariable, definitions.get(otherVariable), definitions);
        }
        else if (definition instanceof RDFConstant) {
            return ((RDFConstant) definition).getType();
        }
        else if ((definition instanceof ImmutableFunctionalTerm) &&
                ((ImmutableFunctionalTerm) definition).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
            ImmutableTerm termTypeTerm = ((ImmutableFunctionalTerm) definition).getTerms().get(1);
            if (termTypeTerm instanceof RDFTermTypeConstant) {
                return ((RDFTermTypeConstant) termTypeTerm).getRDFTermType();
            }
            else if (termTypeTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm termTypeFunctionalTerm = (ImmutableFunctionalTerm) termTypeTerm;
                if ((termTypeFunctionalTerm.getFunctionSymbol() instanceof DBIfElseNullFunctionSymbol)
                        && (termTypeFunctionalTerm.getTerm(1) instanceof RDFTermTypeConstant))
                    return ((RDFTermTypeConstant) termTypeFunctionalTerm.getTerm(1)).getRDFTermType();
            }
            throw new NotFullyTranslatableToNativeQueryRuntimeException(String.format(
                            "its variable %s may not be uniquely typed", variable));
        }
        throw new NotFullyTranslatableToNativeQueryRuntimeException(String.format(
                "could not infer the unique type of its variable %s", variable));
    }

    protected static class NotFullyTranslatableToNativeQueryException extends OntopReformulationException {
        protected NotFullyTranslatableToNativeQueryException(String message) {
            super("Not fully translatable to a native query: " + message);
        }
    }

    /**
     *  Exception required only for handling streams
     */
    private static class NotFullyTranslatableToNativeQueryRuntimeException extends RuntimeException {
        private NotFullyTranslatableToNativeQueryRuntimeException(String message) {
            super(message);
        }
    }

}
