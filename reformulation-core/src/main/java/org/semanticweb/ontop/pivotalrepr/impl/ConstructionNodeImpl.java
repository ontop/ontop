package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.IndempotentVar2VarSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.*;

public class ConstructionNodeImpl extends QueryNodeImpl implements ConstructionNode {

    private Optional<ImmutableQueryModifiers> optionalModifiers;
    private PureDataAtom dataAtom;
    private final IndempotentVar2VarSubstitution renamings;
    private final ImmutableSubstitution<GroundTerm> groundTermBindings;
    private final ImmutableSubstitution<ImmutableFunctionalTerm> functionalBindings;

    /**
     * Without modifier
     */
    public ConstructionNodeImpl(PureDataAtom dataAtom, IndempotentVar2VarSubstitution renamings,
                                ImmutableSubstitution<GroundTerm> groundTermBindings,
                                ImmutableSubstitution<ImmutableFunctionalTerm> functionalBindings) {
        this.dataAtom = dataAtom;
        this.renamings = renamings;
        this.groundTermBindings = groundTermBindings;
        this.functionalBindings = functionalBindings;
        this.optionalModifiers = Optional.absent();
    }

    /**
     * Without renaming and without modifier
     */
    public ConstructionNodeImpl(PureDataAtom dataAtom,
                                ImmutableSubstitution<GroundTerm> groundTermBindings,
                                ImmutableSubstitution<ImmutableFunctionalTerm> functionalBindings) {
        this.dataAtom = dataAtom;
        this.renamings = new IndempotentVar2VarSubstitutionImpl(ImmutableMap.<VariableImpl, VariableImpl>of());
        this.groundTermBindings = groundTermBindings;
        this.functionalBindings = functionalBindings;
        this.optionalModifiers = Optional.absent();
    }

    public ConstructionNodeImpl(PureDataAtom dataAtom, IndempotentVar2VarSubstitution renamings,
                                ImmutableSubstitution<GroundTerm> groundTermBindings,
                                ImmutableSubstitution<ImmutableFunctionalTerm> functionalBindings,
                                ImmutableQueryModifiers queryModifiers) {
        this.dataAtom = dataAtom;
        this.renamings = renamings;
        this.groundTermBindings = groundTermBindings;
        this.functionalBindings = functionalBindings;
        this.optionalModifiers = Optional.of(queryModifiers);
    }

    /**
     * Without renaming
     */
    public ConstructionNodeImpl(PureDataAtom dataAtom,
                                ImmutableSubstitution<GroundTerm> groundTermBindings,
                                ImmutableSubstitution<ImmutableFunctionalTerm> functionalBindings,
                                ImmutableQueryModifiers queryModifiers) {
        this.dataAtom = dataAtom;
        this.renamings = new IndempotentVar2VarSubstitutionImpl(ImmutableMap.<VariableImpl, VariableImpl>of());
        this.groundTermBindings = groundTermBindings;
        this.functionalBindings = functionalBindings;
        this.optionalModifiers = Optional.of(queryModifiers);
    }

    public ConstructionNodeImpl(PureDataAtom dataAtom) {
        this.dataAtom = dataAtom;
        this.renamings = new IndempotentVar2VarSubstitutionImpl(ImmutableMap.<VariableImpl, VariableImpl>of());
        this.groundTermBindings = new ImmutableSubstitutionImpl<>(ImmutableMap.<VariableImpl, GroundTerm>of());
        this.functionalBindings = new ImmutableSubstitutionImpl<>(ImmutableMap.<VariableImpl, ImmutableFunctionalTerm>of());
        this.optionalModifiers = Optional.absent();
    }

    @Override
    public PureDataAtom getProjectionAtom() {
        return dataAtom;
    }

    @Override
    public IndempotentVar2VarSubstitution getRenamings() {
        return renamings;
    }

    @Override
    public ImmutableSubstitution<GroundTerm> getGroundTermBindings() {
        return groundTermBindings;
    }

    @Override
    public ImmutableSubstitution<ImmutableFunctionalTerm> getFunctionalBindings() {
        return functionalBindings;
    }

    @Override
    public Optional<ImmutableQueryModifiers> getOptionalModifiers() {
        return optionalModifiers;
    }

    /**
     * Immutable fields, can be shared.
     */
    @Override
    public ConstructionNode clone() {
        if (optionalModifiers.isPresent()) {
            return new ConstructionNodeImpl(dataAtom, renamings, groundTermBindings, functionalBindings,
                    optionalModifiers.get());
        }
        return new ConstructionNodeImpl(dataAtom, renamings, groundTermBindings, functionalBindings);
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }
}
