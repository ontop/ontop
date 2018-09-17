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
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.rewriting.LinearInclusionDependencyTools;
import it.unibz.inf.ontop.answering.reformulation.rewriting.QueryRewriter;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.LinearInclusionDependency;
import it.unibz.inf.ontop.datalog.impl.CQCUtilities;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

import java.util.List;

/***
 * A query rewriter that does nothing on the given query.
 * 
 * @author mariano
 *
 */
public class DummyRewriter implements QueryRewriter {

    private ImmutableMultimap<Predicate, LinearInclusionDependency> sigma;
    private final CQCUtilities cqcUtilities;
    private final LinearInclusionDependencyTools inclusionDependencyTools;

    @Inject
    private DummyRewriter(CQCUtilities cqcUtilities, LinearInclusionDependencyTools inclusionDependencyTools) {
        this.cqcUtilities = cqcUtilities;
        this.inclusionDependencyTools = inclusionDependencyTools;
    }


    @Override
	public List<CQIE> rewrite(List<CQIE> input) {

        for (CQIE cq : input)
            cqcUtilities.optimizeQueryWithSigmaRules(cq.getBody(), sigma);

        return input;
	}

	@Override
	public void setTBox(ClassifiedTBox reasoner) {
        ImmutableList<LinearInclusionDependency> s = inclusionDependencyTools.getABoxDependencies(reasoner, true);
        sigma = LinearInclusionDependency.toMultimap(s);
	}

}
