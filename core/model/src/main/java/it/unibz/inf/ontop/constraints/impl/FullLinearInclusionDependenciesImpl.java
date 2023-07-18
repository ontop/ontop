package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.constraints.HomomorphismFactory;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

/**
 * Full linear inclusion dependencies, which contain no existentially quantified variables
 * @param <P>
 */

public class FullLinearInclusionDependenciesImpl<P extends AtomPredicate> extends LinearInclusionDependenciesImpl<P> {

    private FullLinearInclusionDependenciesImpl(CoreUtilsFactory coreUtilsFactory,
                                                AtomFactory atomFactory,
                                                HomomorphismFactory homomorphismFactory,
                                                ImmutableList<LinearInclusionDependency<P>> dependencies) {
        super(coreUtilsFactory, atomFactory, homomorphismFactory, dependencies);
    }

    @Override
    protected Homomorphism extendWithLabelledNulls(LinearInclusionDependency<P> id, Homomorphism h) {
        return h;
    }

    @Override
    protected void registerVariables(DataAtom<P> atom) {
        // NO-OP
    }

    @Override
    protected void registerVariables(ImmutableCollection<DataAtom<P>> atoms) {
        // NO-OP
    }


    static class Builder<P extends AtomPredicate> extends LinearInclusionDependenciesImpl.Builder<P> {

        protected Builder(CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory, HomomorphismFactory homomorphismFactory) {
            super(coreUtilsFactory, atomFactory, homomorphismFactory);
        }

        @Override
        public Builder<P> add(DataAtom<P> head, DataAtom<P> body) {
            if (!body.getVariables().containsAll(head.getVariables()))
                throw new IllegalArgumentException();
            super.add(head, body);
            return this;
        }

        @Override
        public LinearInclusionDependencies<P> build() {
            return new FullLinearInclusionDependenciesImpl<>(coreUtilsFactory, atomFactory, homomorphismFactory, builder.build());
        }
    }
}
