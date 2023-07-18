package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

import java.util.Iterator;

public class HomomorphismFactoryImpl implements HomomorphismFactory {

    private final CoreUtilsFactory coreUtilsFactory;
    private final AtomFactory atomFactory;

    @Inject
    public HomomorphismFactoryImpl(CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory) {
        this.coreUtilsFactory = coreUtilsFactory;
        this.atomFactory = atomFactory;
    }

    @Override
    public Homomorphism.Builder getHomomorphismBuilder() {
        return new HomomorphismImpl.BuilderImpl();
    }

    @Override
    public <P extends AtomPredicate> Iterator<Homomorphism> getHomomorphismIterator(Homomorphism baseHomomorphism, ImmutableList<DataAtom<P>> from, ImmutableCollection<DataAtom<P>> to) {
        return new HomomorphismIteratorImpl<>(this, baseHomomorphism, from, to);
    }

    @Override
    public LinearInclusionDependencies<RelationPredicate> getDBLinearInclusionDependencies() {
        return new DBLinearInclusionDependenciesImpl(coreUtilsFactory, atomFactory);
    }

    @Override
    public <P extends AtomPredicate> LinearInclusionDependencies.Builder<P> getLinearInclusionDependenciesBuilder() {
        return new LinearInclusionDependenciesImpl.Builder<>(coreUtilsFactory, atomFactory, this);
    }

    @Override
    public <P extends AtomPredicate> LinearInclusionDependencies.Builder<P> getFullLinearInclusionDependenciesBuilder() {
        return new FullLinearInclusionDependenciesImpl.Builder<>(coreUtilsFactory, atomFactory, this);
    }

    @Override
    public <P extends AtomPredicate> ImmutableCQContainmentCheck<P> getCQSyntacticContainmentCheck() {
        return new ImmutableCQSyntacticContainmentCheck<>();
    }

    @Override
    public <P extends AtomPredicate> ImmutableCQContainmentCheck<P> getCQContainmentCheck(LinearInclusionDependencies<P> dependencies) {
        return new ImmutableCQContainmentCheckUnderLIDs<>(this, dependencies);
    }
}
