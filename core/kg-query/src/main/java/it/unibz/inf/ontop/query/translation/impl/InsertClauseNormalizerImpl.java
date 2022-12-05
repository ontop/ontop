package it.unibz.inf.ontop.query.translation.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.query.translation.InsertClauseNormalizer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class InsertClauseNormalizerImpl implements InsertClauseNormalizer {

    private final CoreUtilsFactory coreUtilsFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final FunctionSymbolFactory functionSymbolFactory;

    @Inject
    protected InsertClauseNormalizerImpl(CoreUtilsFactory coreUtilsFactory, IntermediateQueryFactory iqFactory,
                                         SubstitutionFactory substitutionFactory,
                                         TermFactory termFactory,
                                         FunctionSymbolFactory functionSymbolFactory) {
        this.coreUtilsFactory = coreUtilsFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.functionSymbolFactory = functionSymbolFactory;
    }

    @Override
    public Result normalize(ImmutableSet<IntensionalDataNode> dataNodes, IQTree whereTree) {
        ImmutableSet<BNode> bNodes = dataNodes.stream()
                .flatMap(n -> n.getProjectionAtom().getArguments().stream())
                .filter(a -> a instanceof BNode)
                .map(a -> (BNode) a)
                .collect(ImmutableCollectors.toSet());

        if (bNodes.isEmpty())
            return new ResultImpl(ImmutableMap.of());

        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Sets.union(
                        whereTree.getKnownVariables(),
                        dataNodes.stream()
                                .flatMap(n -> n.getKnownVariables().stream())
                                .collect(ImmutableCollectors.toSet())));

        ImmutableMap<BNode, Variable> bNodeMap = bNodes.stream()
                .collect(ImmutableCollectors.toMap(
                        b -> b,
                        b -> variableGenerator.generateNewVariable()));

        ImmutableSet<ImmutableSet<Variable>> uniqueConstraints = whereTree.inferUniqueConstraints();

        ImmutableMap<Variable, ImmutableTerm> substitutionMap = uniqueConstraints.isEmpty()
                ? createBNodeDefinitionsWithoutUniqueConstraint(bNodeMap, whereTree.getVariables())
                : createBNodeDefinitionsFromUniqueConstraint(bNodeMap, uniqueConstraints.iterator().next());

        ImmutableSet<Variable> newProjectedVariables = Sets.union(whereTree.getKnownVariables(), ImmutableSet.copyOf(bNodeMap.values()))
                .immutableCopy();

        return new ResultImpl(bNodeMap,
                iqFactory.createConstructionNode(
                        newProjectedVariables,
                        substitutionFactory.getSubstitution(substitutionMap)));
    }

    private ImmutableMap<Variable, ImmutableTerm> createBNodeDefinitionsFromUniqueConstraint(ImmutableMap<BNode, Variable> bNodeMap,
                                                                                             ImmutableSet<Variable> uniqueConstraint) {
        return bNodeMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getValue,
                        e -> generateBNodeTemplateFunctionalTerm(uniqueConstraint)));
    }

    /**
     * This implementation does not preserve duplicated rows, as foreseen by R2RML for the virtual setting
     * (see https://www.w3.org/TR/r2rml/#default-mappings)
     */
    private ImmutableMap<Variable, ImmutableTerm> createBNodeDefinitionsWithoutUniqueConstraint(
            ImmutableMap<BNode, Variable> bNodeMap, ImmutableSet<Variable> whereVariables) {
        if (whereVariables.isEmpty())
            return bNodeMap.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getValue,
                            e -> termFactory.getConstantBNode(UUID.randomUUID().toString())));

        return bNodeMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getValue,
                        e -> generateBNodeTemplateFunctionalTerm(whereVariables)));
    }

    private ImmutableFunctionalTerm generateBNodeTemplateFunctionalTerm(ImmutableSet<Variable> variables) {
        if (variables.isEmpty())
            throw new RuntimeException("Was expecting at least one variable");

        Template.Builder templateBuilder = Template.builder()
                .addSeparator("rule-" + UUID.randomUUID())
                .addColumn();

        variables.stream()
                .skip(1)
                .forEach(v -> templateBuilder.addSeparator("/").addColumn());

        ImmutableList<ImmutableFunctionalTerm> arguments = variables.stream()
                .map(v -> termFactory.getImmutableFunctionalTerm(
                        functionSymbolFactory.getExtractLexicalTermFromRDFTerm(),
                        v))
                .collect(ImmutableCollectors.toList());

        return termFactory.getBnodeFunctionalTerm(templateBuilder.build(), arguments);
    }


    protected static class ResultImpl implements InsertClauseNormalizer.Result {

        @Nonnull
        private final ImmutableMap<BNode, Variable> map;

        @Nullable
        private final ConstructionNode constructionNode;

        protected ResultImpl(@Nonnull ImmutableMap<BNode, Variable> map, @Nullable ConstructionNode constructionNode) {
            this.constructionNode = constructionNode;
            this.map = map;
        }

        protected ResultImpl(@Nonnull ImmutableMap<BNode, Variable> map) {
            this.constructionNode = null;
            this.map = map;
        }

        @Override
        public ImmutableMap<BNode, Variable> getBNodeVariableMap() {
            return map;
        }

        @Override
        public Optional<ConstructionNode> getConstructionNode() {
            return Optional.ofNullable(constructionNode);
        }
    }
}