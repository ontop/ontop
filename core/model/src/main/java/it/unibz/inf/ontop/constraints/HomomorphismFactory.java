package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

import java.util.Iterator;

public interface HomomorphismFactory {

    Homomorphism.Builder getHomomorphismBuilder();

    <P extends AtomPredicate> Iterator<Homomorphism> getHomomorphismIterator(Homomorphism baseHomomorphism, ImmutableList<DataAtom<P>> from, ImmutableCollection<DataAtom<P>> to);

    LinearInclusionDependencies<RelationPredicate> getDBLinearInclusionDependencies();

    <P extends AtomPredicate> LinearInclusionDependencies.Builder<P> getLinearInclusionDependenciesBuilder();

    <P extends AtomPredicate> LinearInclusionDependencies.Builder<P> getFullLinearInclusionDependenciesBuilder();

    <P extends AtomPredicate> ImmutableCQContainmentCheck<P> getCQSyntacticContainmentCheck();

    <P extends AtomPredicate> ImmutableCQContainmentCheck<P> getCQContainmentCheck(LinearInclusionDependencies<P> dependencies);
}
