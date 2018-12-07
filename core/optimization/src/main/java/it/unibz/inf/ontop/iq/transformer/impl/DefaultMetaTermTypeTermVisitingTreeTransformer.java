package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.MetaTermTypeTermLiftTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;


/**
 * Meta term type definitions are blocked by unions
 *
 * TODO: explain it further
 */
public class DefaultMetaTermTypeTermVisitingTreeTransformer
        extends DefaultRecursiveIQTreeVisitingTransformer implements MetaTermTypeTermLiftTransformer {

    private final VariableGenerator variableGenerator;
    private final TypeConstantDictionary dictionary;
    private final TermFactory termFactory;
    private final Constant nullValue;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private DefaultMetaTermTypeTermVisitingTreeTransformer(@Assisted VariableGenerator variableGenerator,
                                                           TermFactory termFactory,
                                                           IntermediateQueryFactory iqFactory,
                                                           TypeConstantDictionary typeConstantDictionary,
                                                           SubstitutionFactory substitutionFactory) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
        this.dictionary = typeConstantDictionary;
        this.termFactory = termFactory;
        this.nullValue = termFactory.getNullConstant();
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree transform(IQTree tree) {
        // Makes sure the tree is already normalized before transforming it
        return tree.normalizeForOptimization(variableGenerator)
                .acceptTransformer(this);
    }


    /**
     * TODO: implement it seriously
     */
    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        // Recursive
        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.acceptTransformer(this))
                .collect(ImmutableCollectors.toList());

        ImmutableSet<Variable> metaTermTypeVariables = rootNode.getVariables().stream()
                .filter(v -> isMetaTermTypeVariable(liftedChildren))
                .collect(ImmutableCollectors.toSet());

        if (metaTermTypeVariables.isEmpty())
            return iqFactory.createNaryIQTree(rootNode, liftedChildren)
                    .normalizeForOptimization(variableGenerator);

        ImmutableMultimap<Variable, RDFTermTypeConstant> possibleConstantMultimap =
                metaTermTypeVariables.stream()
                        .flatMap(v -> liftedChildren.stream()
                            .flatMap(child -> extractPossibleTermTypeConstants(v, child))
                            .map(c -> Maps.immutableEntry(v, c)))
                        .collect(ImmutableCollectors.toMultimap());

        // Meta RDF term type variable -> new integer variable
        ImmutableMap<Variable, Variable> integerVariableMap = metaTermTypeVariables.stream()
                .collect(ImmutableCollectors.toMap(v -> v,
                        variableGenerator::generateNewVariableFromVar));

        ImmutableSet<Variable> newUnionVariables = rootNode.getVariables().stream()
                .map(v -> Optional.ofNullable(integerVariableMap.get(v))
                        .orElse(v))
                .collect(ImmutableCollectors.toSet());

        ImmutableList<IQTree> newChildren = liftedChildren.stream()
                .map(c -> transformUnionChild(c, integerVariableMap, newUnionVariables))
                .collect(ImmutableCollectors.toList());

        NaryIQTree newUnionTree = iqFactory.createNaryIQTree(
                iqFactory.createUnionNode(newUnionVariables),
                newChildren);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = createNewUnionSubstitution(possibleConstantMultimap);

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(rootNode.getVariables(), newSubstitution),
                newUnionTree);
    }

    /**
     * TODO: implement it seriously
     */
    private boolean isMetaTermTypeVariable(ImmutableList<IQTree> unionChildren) {
        return false;
    }

    private Stream<RDFTermTypeConstant> extractPossibleTermTypeConstants(Variable variable, IQTree child) {
        ConstructionNode constructionNode = Optional.of(child.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "Was expecting the child to start with a ConstructionNode"));

        ImmutableTerm definition = Optional.ofNullable(constructionNode.getSubstitution().get(variable))
                .orElseThrow(() ->  new MinorOntopInternalBugException(
                        "Was expecting the child to define the blocked definition of the RDF term type variable"));

        if (definition instanceof RDFTermTypeConstant)
            return Stream.of((RDFTermTypeConstant) definition);
        else if (definition.equals(nullValue))
            return Stream.of();
        else if (definition instanceof ImmutableFunctionalTerm)
            return extractPossibleTermTypeConstants((ImmutableFunctionalTerm) definition);
        else
            throw new MinorOntopInternalBugException("Was not expecting a Variable or a different kind of Constant");
    }

    /**
     * We expect to receive only one particular type of function symbol
     * TODO: define it
     */
    private Stream<RDFTermTypeConstant> extractPossibleTermTypeConstants(ImmutableFunctionalTerm definition) {
        throw new RuntimeException("TODO: implement that case");
    }

    private IQTree transformUnionChild(IQTree child, ImmutableMap<Variable, Variable> integerVariableMap,
                                       ImmutableSet<Variable> newProjectedVariables) {
        ConstructionNode initialConstructionNode = Optional.of(child.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "Was expecting the child to start with a ConstructionNode"));

        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(
                initialConstructionNode.getSubstitution().getImmutableMap().entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> Optional.ofNullable(integerVariableMap.get(e.getKey())).orElseGet(e::getKey),
                                e -> transformIntoIntegerDefinition(e.getValue())
                        )));

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(newProjectedVariables, newSubstitution),
                ((UnaryIQTree) child).getChild());
    }

    private ImmutableTerm transformIntoIntegerDefinition(ImmutableTerm term) {
        if (term instanceof RDFTermTypeConstant)
            return dictionary.convert((RDFTermTypeConstant) term);
        throw new RuntimeException("TODO: support other conversions");
    }

    private ImmutableSubstitution<ImmutableTerm> createNewUnionSubstitution(
            ImmutableMultimap<Variable, RDFTermTypeConstant> possibleConstantMultimap) {
        throw new RuntimeException("TODO: implement createNewUnionSubstitution()");
    }

    protected IQTree transformLeaf(LeafIQTree leaf){
        return leaf.normalizeForOptimization(variableGenerator);
    }

    protected IQTree transformUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
        return super.transformUnaryNode(rootNode, child)
                .normalizeForOptimization(variableGenerator);
    }

    protected IQTree transformNaryCommutativeNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return super.transformNaryCommutativeNode(rootNode, children)
                .normalizeForOptimization(variableGenerator);
    }

    protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        return super.transformBinaryNonCommutativeNode(rootNode, leftChild, rightChild)
                .normalizeForOptimization(variableGenerator);
    }

}
