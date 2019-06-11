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
import it.unibz.inf.ontop.answering.reformulation.rewriting.ImmutableLinearInclusionDependenciesTools;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

import java.util.*;

/***
 * A query rewriter that used Sigma ABox dependencies to optimise BGPs.
 *
 */
public class DummyRewriter implements QueryRewriter {

    private ImmutableList<ImmutableLinearInclusionDependency<AtomPredicate>> sigma;

    protected final ImmutableLinearInclusionDependenciesTools inclusionDependencyTools;
    protected final IntermediateQueryFactory iqFactory;

    @Inject
    protected DummyRewriter(ImmutableLinearInclusionDependenciesTools inclusionDependencyTools,
                            IntermediateQueryFactory iqFactory) {
        this.inclusionDependencyTools = inclusionDependencyTools;
        this.iqFactory = iqFactory;
    }

    @Override
    public void setTBox(ClassifiedTBox reasoner) {
        sigma = inclusionDependencyTools.getABoxDependencies(reasoner, true);
    }

    protected ImmutableList<ImmutableLinearInclusionDependency<AtomPredicate>> getSigma() {
        return sigma;
    }

    @Override
	public IQ rewrite(IQ query) throws EmptyQueryException {

        return iqFactory.createIQ(query.getProjectionAtom(), query.getTree().acceptTransformer(new BasicGraphPatternTransformer(iqFactory) {
            @Override
            protected ImmutableList<IntensionalDataNode> transformBGP(ImmutableList<IntensionalDataNode> triplePatterns) {

                // optimise with Sigma ABox dependencies
                // mutable copy
                ArrayList<IntensionalDataNode> list = new ArrayList<>(triplePatterns);
                // this loop has to remain sequential (no streams)
                for (int i = 0; i < list.size(); i++) {
                    final DataAtom<AtomPredicate> atom = list.get(i).getProjectionAtom();
                    ImmutableSet<DataAtom> derived = inclusionDependencyTools.chaseAtom(atom, sigma);
                    if (!derived.isEmpty()) {
                        for (int j = 0; j < list.size(); j++)
                            if (i != j && derived.contains(list.get(j).getProjectionAtom())) {
                                //list.remove(j);
                                j--;
                            }
                    }
                }
                return ImmutableList.copyOf(list);
            }
        }));
	}
}
