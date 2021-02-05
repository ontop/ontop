package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.constraints.impl.FullLinearInclusionDependenciesImpl;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.Function;

/***
 * A query rewriter that used Sigma ABox dependencies to optimise BGPs.
 *
 */
public class DummyRewriter implements QueryRewriter {

    private FullLinearInclusionDependenciesImpl<RDFAtomPredicate> sigma;

    protected final IntermediateQueryFactory iqFactory;
    protected final AtomFactory atomFactory;
    protected final TermFactory termFactory;
    protected final CoreUtilsFactory coreUtilsFactory;

    @Inject
    protected DummyRewriter(IntermediateQueryFactory iqFactory, AtomFactory atomFactory, TermFactory termFactory, CoreUtilsFactory coreUtilsFactory) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public void setTBox(ClassifiedTBox reasoner) {

        FullLinearInclusionDependenciesImpl.Builder<RDFAtomPredicate> builder = FullLinearInclusionDependenciesImpl.builder(coreUtilsFactory, atomFactory);

        Variable x = termFactory.getVariable("x");
        Variable y = termFactory.getVariable("y");

        traverseDAG(reasoner.objectPropertiesDAG(), p -> !p.isInverse(), ope -> getAtom(x, ope, y), builder);

        traverseDAG(reasoner.dataPropertiesDAG(), p -> true, dpe -> getAtom(x, dpe, y), builder);

        // the head will have no existential variables
        traverseDAG(reasoner.classesDAG(), c -> (c instanceof OClass), c -> getAtom(c, x, () -> y), builder);

        sigma = builder.build();
    }

    protected FullLinearInclusionDependenciesImpl<RDFAtomPredicate> getSigma() {
        return sigma;
    }

    /*
        optimise with Sigma ABox dependencies
     */
    @Override
	public IQ rewrite(IQ query) throws EmptyQueryException {
        return iqFactory.createIQ(query.getProjectionAtom(),
                query.getTree().acceptTransformer(new BasicGraphPatternTransformer(iqFactory) {
            @Override
            protected ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> bgp) {
                return removeRedundantAtoms(bgp);
            }
        }));
	}

	private ImmutableList<IQTree> removeRedundantAtoms(ImmutableList<IntensionalDataNode> bgp) {
        ArrayList<IntensionalDataNode> list = new ArrayList<>(bgp); // mutable copy
        // this loop has to remain sequential (no streams)
        for (int i = 0; i < list.size(); i++) {
            DataAtom<RDFAtomPredicate> atom = (DataAtom)list.get(i).getProjectionAtom();
            ImmutableSet<DataAtom<RDFAtomPredicate>> derived = sigma.chaseAtom(atom);
            for (int j = 0; j < list.size(); j++) {
                DataAtom<AtomPredicate> curr = list.get(j).getProjectionAtom();
                if (j != i && derived.contains(curr)) {
                    ImmutableSet<Variable> variables = list.stream()
                            .map(IntensionalDataNode::getProjectionAtom)
                            .filter(a -> (a != curr))
                            .flatMap(a -> a.getVariables().stream())
                            .collect(ImmutableCollectors.toSet());
                    // atom to be removed cannot contain a variable occurring nowhere else
                    if (variables.containsAll(curr.getVariables())) {
                        list.remove(j);
                        j--;
                        if (j < i) // removing in front of the atom
                            i--; // shift the atom position too
                    }
                }
            }
        }
        return ImmutableList.copyOf(list);
    }

    private static <T> void traverseDAG(EquivalencesDAG<T> dag,
                                        java.util.function.Predicate<T> filter,
                                        Function<T, DataAtom<RDFAtomPredicate>> translate,
                                        FullLinearInclusionDependenciesImpl.Builder<RDFAtomPredicate> builder) {
        for (Equivalences<T> node : dag)
            for (Equivalences<T> subNode : dag.getSub(node))
                for (T sub : subNode)
                    for (T e : node)
                        if (e != sub && filter.test(e)) {
                            DataAtom<RDFAtomPredicate> head = translate.apply(e);
                            DataAtom<RDFAtomPredicate> body = translate.apply(sub);
                            builder.add(head, body);
                        }
    }


    protected DataAtom<RDFAtomPredicate> getAtom(ClassExpression ce, VariableOrGroundTerm x, Provider<VariableOrGroundTerm> y) {
        if (ce instanceof OClass) {
            return getAtom(x, (OClass) ce);
        }
        else if (ce instanceof ObjectSomeValuesFrom) {
            return getAtom(x, ((ObjectSomeValuesFrom) ce).getProperty(), y.get());
        }
        else {
            return getAtom(x, ((DataSomeValuesFrom) ce).getProperty(), y.get());
        }
    }

    protected <T extends AtomPredicate> DataAtom<T> getAtom(VariableOrGroundTerm t1, ObjectPropertyExpression property, VariableOrGroundTerm t2) {
        return property.isInverse()
                ? (DataAtom)atomFactory.getIntensionalTripleAtom(t2, property.getIRI(), t1)
                : (DataAtom)atomFactory.getIntensionalTripleAtom(t1, property.getIRI(), t2);
    }

    protected <T extends AtomPredicate> DataAtom<T> getAtom(VariableOrGroundTerm t1, DataPropertyExpression property, VariableOrGroundTerm t2) {
        return (DataAtom)atomFactory.getIntensionalTripleAtom(t1, property.getIRI(), t2);
    }

    protected <T extends AtomPredicate> DataAtom<T> getAtom(VariableOrGroundTerm t, OClass oc) {
        return (DataAtom)atomFactory.getIntensionalTripleAtom(t, oc.getIRI());
    }
}
