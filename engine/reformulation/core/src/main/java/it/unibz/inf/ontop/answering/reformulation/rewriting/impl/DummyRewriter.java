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
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;

import java.util.*;

/***
 * A query rewriter that does nothing on the given query.
 *
 */
public class DummyRewriter implements QueryRewriter {

    private ImmutableList<ImmutableLinearInclusionDependency<AtomPredicate>> sigma;
    private final ImmutableLinearInclusionDependenciesTools inclusionDependencyTools;
    private final DatalogProgram2QueryConverter datalogConverter;
    private final ImmutableUnificationTools immutableUnificationTools;
    private final IntermediateQueryFactory iqFactory;


    @Inject
    private DummyRewriter(ImmutableLinearInclusionDependenciesTools inclusionDependencyTools,
                          DatalogProgram2QueryConverter datalogConverter,
                          ImmutableUnificationTools immutableUnificationTools,
                          IntermediateQueryFactory iqFactory) {
        this.inclusionDependencyTools = inclusionDependencyTools;
        this.datalogConverter = datalogConverter;
        this.immutableUnificationTools = immutableUnificationTools;
        this.iqFactory = iqFactory;
    }


    @Override
	public IQ rewrite(DatalogProgram program) throws EmptyQueryException {

        IQ convertedIQ =  datalogConverter.convertDatalogProgram(program, ImmutableList.of());

        return iqFactory.createIQ(convertedIQ.getProjectionAtom(), convertedIQ.getTree().acceptTransformer(new BasicGraphPatternTransformer(iqFactory) {
            @Override
            protected ImmutableList<IntensionalDataNode> transformBGP(ImmutableList<IntensionalDataNode> triplePatterns) {
                return optimizeQueryWithSigmaRules(triplePatterns, sigma);
            }
        }));
	}

	@Override
	public void setTBox(ClassifiedTBox reasoner) {
        sigma = inclusionDependencyTools.getABoxDependencies(reasoner, true);
	}


    private ImmutableList<IntensionalDataNode> optimizeQueryWithSigmaRules(ImmutableList<IntensionalDataNode> bgp, ImmutableList<ImmutableLinearInclusionDependency<AtomPredicate>> dependencies) {

        ArrayList<IntensionalDataNode> list = new ArrayList<>(bgp);

        // for each atom in query body
        for (int i = 0; i < list.size(); i++) {
            IntensionalDataNode tp = list.get(i);
            ImmutableSet<DataAtom<AtomPredicate>> derived = getDerivedAtoms(tp, dependencies);

            for (int j = 0; j < list.size(); j++)
                if (i != j && derived.contains(tp.getProjectionAtom())) {
                    System.out.println("LID2: " + tp + " IN " + list);
                    list.remove(j);
                    j--;
                }
        }

        return ImmutableList.copyOf(list);
    }

    private ImmutableSet<DataAtom<AtomPredicate>> getDerivedAtoms(IntensionalDataNode tp, ImmutableList<ImmutableLinearInclusionDependency<AtomPredicate>> dependencies) {
        ImmutableSet.Builder<DataAtom<AtomPredicate>> derived = ImmutableSet.builder();
        // collect all derived atoms
        for (ImmutableLinearInclusionDependency<AtomPredicate> lid : dependencies) {
            // try to unify current query body atom with tbox rule body atom
            Optional<ImmutableSubstitution<VariableOrGroundTerm>> theta
                    = immutableUnificationTools.computeAtomMGU(lid.getBody(), tp.getProjectionAtom());
            theta.ifPresent(t -> { if (!t.isEmpty()) derived.add(t.applyToDataAtom(lid.getHead())); });
        }
        return derived.build();
    }

}
