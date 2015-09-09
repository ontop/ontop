package org.semanticweb.ontop.unfold;

/*
 * #%L
 * ontop-test
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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.AtomPredicateImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.*;
import org.semanticweb.ontop.pivotalrepr.UnionLiftProposalExecutor;
import org.semanticweb.ontop.pivotalrepr.impl.jgrapht.JgraphtIntermediateQueryBuilder;
import org.semanticweb.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;

import static org.junit.Assert.assertEquals;
import static org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition.*;


public class UnionLiftTest {

	private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
	private static final Optional<ImmutableBooleanExpression> NO_EXPRESSION = Optional.absent();
	private static final Optional<ImmutableQueryModifiers> NO_MODIFIER = Optional.absent();

    private UnionNode unionAns2Node;

    private UnionNode unionAns4Node;


    public IntermediateQuery buildQuery1() throws Exception {
		Variable x = (Variable) DATA_FACTORY.getVariable("x");
		Variable y = (Variable) DATA_FACTORY.getVariable("y");


        /**
         * Ans 1
         */
        DataAtom rootDataAtom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans1", 2), x, y);
        ConstructionNode root = new ConstructionNodeImpl(rootDataAtom);

		IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder();
		queryBuilder.init(root);


		LeftJoinNode topLJ = new LeftJoinNodeImpl(NO_EXPRESSION);
		queryBuilder.addChild(root, topLJ);

		InnerJoinNode join1 = new InnerJoinNodeImpl(NO_EXPRESSION);
		queryBuilder.addChild(topLJ, join1, LEFT);

		/**
		 * Ans 2
		 */
		DataAtom ans2Atom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans2", 1), x);
		ConstructionNode topAns2Node = new ConstructionNodeImpl(ans2Atom);
		queryBuilder.addChild(join1, topAns2Node);

		UnionNode unionAns2 = new UnionNodeImpl();
		queryBuilder.addChild(topAns2Node, unionAns2);

		Variable a = (Variable) DATA_FACTORY.getVariable("a");
		ConstructionNode t1Ans2Node = new ConstructionNodeImpl(ans2Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, a)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns2, t1Ans2Node);

		TableNode t1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t1", 1), a));
		queryBuilder.addChild(t1Ans2Node, t1);

		Variable b = (Variable) DATA_FACTORY.getVariable("b");
		ConstructionNode t2Ans2Node = new ConstructionNodeImpl(ans2Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, b)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns2, t2Ans2Node);

		TableNode t2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t2", 1), b));
		queryBuilder.addChild(t2Ans2Node, t2);

		/**
		 * Ans 3
		 */
		DataAtom ans3Atom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans3", 1), x);
		Variable c = (Variable) DATA_FACTORY.getVariable("c");
		ConstructionNode ans3Node = new ConstructionNodeImpl(ans3Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, c)), NO_MODIFIER);
		queryBuilder.addChild(join1, ans3Node);

		TableNode t3 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t3", 1), c));
		queryBuilder.addChild(ans3Node, t3);


		/**
		 * Ans 4
		 */
		DataAtom ans4Atom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans4", 2), x, y);
		ConstructionNode topAns4Node = new ConstructionNodeImpl(ans4Atom);
		queryBuilder.addChild(topLJ, topAns4Node, RIGHT);

		UnionNode unionAns4 = new UnionNodeImpl();
		queryBuilder.addChild(topAns4Node, unionAns4);

		Variable d = (Variable) DATA_FACTORY.getVariable("d");
		Variable e = (Variable) DATA_FACTORY.getVariable("e");
		ConstructionNode t4Ans4Node = new ConstructionNodeImpl(ans4Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, d, y, e)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns4, t4Ans4Node);

		TableNode t4 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t4", 2), d, e));
		queryBuilder.addChild(t4Ans4Node, t4);

		Variable f = (Variable) DATA_FACTORY.getVariable("f");
		Variable g = (Variable) DATA_FACTORY.getVariable("g");
		ConstructionNode t5Ans4Node = new ConstructionNodeImpl(ans4Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, f, y, g)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns4, t5Ans4Node);

		TableNode t5 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t5", 2), f, g));
		queryBuilder.addChild(t5Ans4Node, t5);

        this.unionAns2Node = unionAns2;
        this.unionAns4Node = unionAns4;
        return queryBuilder.build();
    }

	@Test
	public void testUnionLift1() throws Exception {
        IntermediateQuery intermediateQuery = buildQuery1();

        System.out.println("Query 1: \n" + intermediateQuery);

        UnionLiftProposal unionLiftProposal = new UnionLiftProposalImpl(unionAns2Node);

        UnionLiftProposalExecutor executor = new UnionLiftProposalExecutorImpl();

        IntermediateQuery newQuery = executor.apply(unionLiftProposal, intermediateQuery).getResultingQuery();

        System.out.println("New Query: \n" + newQuery);

        System.out.flush();
	}


    @Test
    public void testUnionLift2() throws Exception {
        IntermediateQuery intermediateQuery = buildQuery1();

        System.out.println("Query 1: \n" + intermediateQuery);

        UnionLiftProposal unionLiftProposal = new UnionLiftProposalImpl(unionAns4Node);

        UnionLiftProposalExecutor executor = new UnionLiftProposalExecutorImpl();

        IntermediateQuery newQuery = executor.apply(unionLiftProposal, intermediateQuery).getResultingQuery();

        System.out.println("New Query: \n" + newQuery);

        System.out.flush();
    }

}
