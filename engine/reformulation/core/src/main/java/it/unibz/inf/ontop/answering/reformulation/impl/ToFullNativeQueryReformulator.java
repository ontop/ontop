package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.TranslationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.SliceNode;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;

/**
 * Transforms the SPARQL query into a native (e.g. SQL) query without post-processing.
 * Introduces the strong-typing restriction on the SPARQL query.
 *
 */
public class ToFullNativeQueryReformulator extends QuestQueryProcessor {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @AssistedInject
    private ToFullNativeQueryReformulator(@Assisted OBDASpecification obdaSpecification,
                                          QueryCache queryCache,
                                          QueryUnfolder.Factory queryUnfolderFactory,
                                          TranslationFactory translationFactory,
                                          QueryRewriter queryRewriter,
                                          KGQueryFactory kgQueryFactory,
                                          KGQueryTranslator inputQueryTranslator,
                                          GeneralStructuralAndSemanticIQOptimizer generalOptimizer,
                                          QueryPlanner queryPlanner,
                                          QueryLogger.Factory queryLoggerFactory,
                                          IntermediateQueryFactory iqFactory,
                                          TermFactory termFactory,
                                          SubstitutionFactory substitutionFactory) {
        super(obdaSpecification, queryCache, queryUnfolderFactory, translationFactory, queryRewriter, kgQueryFactory,
                inputQueryTranslator, generalOptimizer, queryPlanner, queryLoggerFactory);
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
    }


    /**
     * Replaces the RDF terms by DB terms with the corresponding natural DB datatype.
     * Rejects non-strongly typed queries.
     */
    @Override
    protected IQ generateExecutableQuery(IQ iq) throws OntopReformulationException {
        DistinctVariableOnlyDataAtom initialProjectionAtom = iq.getProjectionAtom();
        IQTree initialTree = iq.getTree();

        ImmutableMap<Variable, ImmutableTerm> definitions = extractDefinitions(initialTree);
        ImmutableMap<Variable, RDFTermType> rdfTypes = extractRDFTypes(definitions);

        IQTree nativeTree = replaceRDFByDBTerms(initialTree, rdfTypes);

        return super.generateExecutableQuery(
                iqFactory.createIQ(initialProjectionAtom, nativeTree));
    }

    private IQTree replaceRDFByDBTerms(IQTree tree, ImmutableMap<Variable, RDFTermType> rdfTypes) {
        if (rdfTypes.isEmpty())
            return tree;
        QueryNode rootNode = tree.getRootNode();
        if (rootNode instanceof SliceNode) {
            return iqFactory.createUnaryIQTree((SliceNode) rootNode, replaceRDFByDBTerms(tree, rdfTypes));
        }
        else if (rootNode instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) rootNode;
            ImmutableSubstitution<ImmutableTerm> newSubstitution = replaceRDFByDBTermsInSubstitution(
                    constructionNode.getSubstitution(), rdfTypes);
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(constructionNode.getVariables(), newSubstitution),
                    ((UnaryIQTree)tree).getChild()
            );
        }
        else
            throw new MinorOntopInternalBugException("Unexpected tree shape " +
                    "(proper exception should have already been thrown)");
    }

    private ImmutableMap<Variable, ImmutableTerm> extractDefinitions(IQTree rdfTree) throws NotFullyTranslatableToNativeQueryException {
        QueryNode rootNode = rdfTree.getRootNode();
        if (rootNode instanceof ConstructionNode) {
            // NB: should not include any non-projected variable (illegal IQ)
            ImmutableMap<Variable, ImmutableTerm> substitutionMap = ((ConstructionNode) rootNode).getSubstitution().getImmutableMap();
            Sets.SetView<Variable> missingVariables = Sets.difference(rdfTree.getVariables(), substitutionMap.keySet());
            if (missingVariables.isEmpty())
                return substitutionMap;
            throw new NotFullyTranslatableToNativeQueryException(String.format(
                    "its variables %s are missing an independent definition",
                    missingVariables));
        }
        else if (rootNode instanceof SliceNode) {
            // Recursive
            return extractDefinitions(((UnaryIQTree)rootNode).getChild());
        }
        else if (rdfTree.getVariables().isEmpty()) {
            return ImmutableMap.of();
        }
        else {
            throw new NotFullyTranslatableToNativeQueryException("was expected to have an extended projection at the top. IQ: " + rdfTree);
        }
    }

    private ImmutableSubstitution<ImmutableTerm> replaceRDFByDBTermsInSubstitution(
            ImmutableSubstitution<ImmutableTerm> substitution, ImmutableMap<Variable, RDFTermType> rdfTypes) {
        ImmutableMap<Variable, ImmutableTerm> newMap = substitution.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> replaceRDFByDBTerm(e.getValue(), rdfTypes.get(e.getKey()))));

        return substitutionFactory.getSubstitution(newMap);
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

    private ImmutableMap<Variable, RDFTermType> extractRDFTypes(ImmutableMap<Variable, ImmutableTerm> definitions)
            throws NotFullyTranslatableToNativeQueryException {
        ImmutableMap.Builder<Variable, RDFTermType> mapBuilder = ImmutableMap.builder();

        for (Map.Entry<Variable, ImmutableTerm> entry : definitions.entrySet()) {
            mapBuilder.put(entry.getKey(), extractRDFType(entry.getKey(), entry.getValue(), definitions));
        }
        return mapBuilder.build();
    }

    private RDFTermType extractRDFType(Variable variable, ImmutableTerm definition,
                                         ImmutableMap<Variable, ImmutableTerm> definitions) throws NotFullyTranslatableToNativeQueryException {
        if (definition instanceof Variable) {
            Variable otherVariable = (Variable) definition;
            return extractRDFType(otherVariable, definitions.get(otherVariable), definitions);
        }
        else if (definition instanceof RDFConstant)
            return ((RDFConstant) definition).getType();
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
            throw new NotFullyTranslatableToNativeQueryException(String.format(
                            "its variable %s may not be uniquely typed", variable));
        }
        throw new NotFullyTranslatableToNativeQueryException(String.format(
                "could not infer the unique type of its variable %s", variable));
    }

    protected static class NotFullyTranslatableToNativeQueryException extends OntopReformulationException {
        protected NotFullyTranslatableToNativeQueryException(String message) {
            super("Not fully translatable to a native query: " + message);
        }

    }

}
