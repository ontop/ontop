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
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.constraints.FullLinearInclusionDependencies;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/***
 * A query rewriter that used Sigma ABox dependencies to optimise BGPs.
 *
 */
public class DummyRewriter implements QueryRewriter {

    private FullLinearInclusionDependencies<AtomPredicate> sigma;

    protected final IntermediateQueryFactory iqFactory;
    protected final AtomFactory atomFactory;
    protected final TermFactory termFactory;
    protected final ImmutableUnificationTools immutableUnificationTools;
    protected final CoreUtilsFactory coreUtilsFactory;
    protected final SubstitutionFactory substitutionFactory;

    @Inject
    protected DummyRewriter(IntermediateQueryFactory iqFactory, AtomFactory atomFactory, TermFactory termFactory, ImmutableUnificationTools immutableUnificationTools, CoreUtilsFactory coreUtilsFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.immutableUnificationTools = immutableUnificationTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public void setTBox(ClassifiedTBox reasoner) {

        FullLinearInclusionDependencies.Builder<AtomPredicate> builder = FullLinearInclusionDependencies.builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory, atomFactory);

        traverseDAG(reasoner.objectPropertiesDAG(), p -> !p.isInverse(), this::translate, builder);

        traverseDAG(reasoner.dataPropertiesDAG(), p -> true, this::translate, builder);

        // the head will have no existential variables
        traverseDAG(reasoner.classesDAG(), c -> (c instanceof OClass), this::translate, builder);

        sigma = builder.build();
    }

    protected FullLinearInclusionDependencies<AtomPredicate> getSigma() {
        return sigma;
    }

    /*
        optimise with Sigma ABox dependencies
     */
    @Override
	public IQ rewrite(IQ query) throws EmptyQueryException {

        return iqFactory.createIQ(query.getProjectionAtom(), query.getTree().acceptTransformer(new BasicGraphPatternTransformer(iqFactory) {
            @Override
            protected ImmutableList<IntensionalDataNode> transformBGP(ImmutableList<IntensionalDataNode> triplePatterns) {

                // mutable copy
                ArrayList<IntensionalDataNode> list = new ArrayList<>(triplePatterns);
                //System.out.println("DUMMY: " + list + " WITH " + sigma);
                // this loop has to remain sequential (no streams)
                // TODO: CAREFUL WITH J < I
                for (int i = 0; i < list.size(); i++) {
                    DataAtom<AtomPredicate> atom = list.get(i).getProjectionAtom();
                    ImmutableSet<DataAtom<AtomPredicate>> derived = sigma.chaseAtom(atom);
                    //System.out.println("DUMMY: " + atom + " CHASED: " + derived);
                    if (!derived.isEmpty()) {
                        for (int j = 0; j < list.size(); j++) {
                            DataAtom<AtomPredicate> curr = list.get(j).getProjectionAtom();
                            if (i != j && derived.contains(curr)) {
                                ImmutableSet<Variable> variables = list.stream()
                                        .map(t -> t.getProjectionAtom())
                                        .filter(a -> (a != curr))
                                        .flatMap(a -> a.getVariables().stream())
                                        .collect(ImmutableCollectors.toSet());
                                // atom to be removed cannot contain a variable occurring nowhere else
                                if (variables.containsAll(curr.getVariables())) {
                                    list.remove(j);
                                    j--;
                                    //System.out.println("DUMMY: REMOVE " + curr);
                                }
                                //else
                                    //System.out.println("DUMMY: BLOCKED " + curr);

                            }
                        }
                    }
                }
                return ImmutableList.copyOf(list);
            }
        }));
	}

    private static <T> void traverseDAG(EquivalencesDAG<T> dag,
                                        java.util.function.Predicate<T> filter,
                                        Function<T, DataAtom<AtomPredicate>> translate,
                                        FullLinearInclusionDependencies.Builder<AtomPredicate> builder) {
        for (Equivalences<T> node : dag)
            for (Equivalences<T> subNode : dag.getSub(node))
                for (T sub : subNode)
                    for (T e : node)
                        if (e != sub && filter.test(e)) {
                            DataAtom<AtomPredicate> head = translate.apply(e);
                            DataAtom<AtomPredicate> body = translate.apply(sub);
                            builder.add(head, body);
                        }
    }

    private DataAtom<AtomPredicate> translate(ObjectPropertyExpression property) {
        return property.isInverse()
                ? atomFactory.getIntensionalTripleAtom(
                    termFactory.getVariable("y"),
                    property.getIRI(),
                    termFactory.getVariable("x"))
                : atomFactory.getIntensionalTripleAtom(
                    termFactory.getVariable("x"),
                    property.getIRI(),
                    termFactory.getVariable("y"));
    }

    private DataAtom<AtomPredicate> translate(DataPropertyExpression property) {
        return atomFactory.getIntensionalTripleAtom(
                termFactory.getVariable("x"),
                property.getIRI(),
                termFactory.getVariable("y"));
    }

    private DataAtom<AtomPredicate> translate(ClassExpression description) {
        if (description instanceof OClass) {
            return atomFactory.getIntensionalTripleAtom(
                    termFactory.getVariable("x"),
                    ((OClass) description).getIRI());
        }
        else if (description instanceof ObjectSomeValuesFrom) {
            return translate(((ObjectSomeValuesFrom) description).getProperty());
        }
        else {
            return translate(((DataSomeValuesFrom) description).getProperty());
        }
    }

}
