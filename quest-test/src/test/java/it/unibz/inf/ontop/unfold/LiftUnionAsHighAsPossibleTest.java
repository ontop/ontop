package it.unibz.inf.ontop.unfold;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;

import java.util.Optional;


public class LiftUnionAsHighAsPossibleTest {

	private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
	private static final Optional<ImmutableExpression> NO_EXPRESSION = Optional.empty();
	private static final Optional<ImmutableQueryModifiers> NO_MODIFIER = Optional.empty();
	private static final MetadataForQueryOptimization METADATA = new EmptyMetadataForQueryOptimization();

    private UnionNode unionAns2Node;

    private UnionNode unionAns4Node;


    public IntermediateQuery buildQuery1() throws Exception {
		Variable x =  DATA_FACTORY.getVariable("x");
		Variable y =  DATA_FACTORY.getVariable("y");


        /**
         * Ans 1
         */
		DistinctVariableOnlyDataAtom rootDataAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(new AtomPredicateImpl("ans1", 2), x, y);
        ConstructionNode root = new ConstructionNodeImpl(rootDataAtom.getVariables());

		IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA,
				QuestCoreConfiguration.defaultBuilder().build().getInjector());
		queryBuilder.init(rootDataAtom, root);


		LeftJoinNode topLJ = new LeftJoinNodeImpl(NO_EXPRESSION);
		queryBuilder.addChild(root, topLJ);

		InnerJoinNode join1 = new InnerJoinNodeImpl(NO_EXPRESSION);
		queryBuilder.addChild(topLJ, join1, NonCommutativeOperatorNode.ArgumentPosition.LEFT);

		/**
		 * Ans 2
		 */
		ConstructionNode topAns2Node = new ConstructionNodeImpl(ImmutableSet.of(x));
		queryBuilder.addChild(join1, topAns2Node);

		ImmutableSet<Variable> ans2ProjectedVariables = ImmutableSet.of(x);
		UnionNode unionAns2 = new UnionNodeImpl(ans2ProjectedVariables);
		queryBuilder.addChild(topAns2Node, unionAns2);

		Variable a = DATA_FACTORY.getVariable("a");
		ConstructionNode t1Ans2Node = new ConstructionNodeImpl(ans2ProjectedVariables,
				new ImmutableSubstitutionImpl<>(ImmutableMap.of(x, a)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns2, t1Ans2Node);

		ExtensionalDataNode t1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t1", 1), a));
		queryBuilder.addChild(t1Ans2Node, t1);

		Variable b = DATA_FACTORY.getVariable("b");
		ConstructionNode t2Ans2Node = new ConstructionNodeImpl(ans2ProjectedVariables,
				new ImmutableSubstitutionImpl<>(ImmutableMap.of(x, b)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns2, t2Ans2Node);

		ExtensionalDataNode t2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t2", 1), b));
		queryBuilder.addChild(t2Ans2Node, t2);

		/**
		 * Ans 3
		 */
		ImmutableSet<Variable> ans3Variables = ImmutableSet.of(x);
		Variable c =  DATA_FACTORY.getVariable("c");
		ConstructionNode ans3Node = new ConstructionNodeImpl(ans3Variables,
				new ImmutableSubstitutionImpl<>(ImmutableMap.of(x, c)), NO_MODIFIER);
		queryBuilder.addChild(join1, ans3Node);

		ExtensionalDataNode t3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t3", 1), c));
		queryBuilder.addChild(ans3Node, t3);


		/**
		 * Ans 4
		 */
		ImmutableSet<Variable> ans4Variables = ImmutableSet.of(x, y);
		ConstructionNode topAns4Node = new ConstructionNodeImpl(ans4Variables);
		queryBuilder.addChild(topLJ, topAns4Node, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

		UnionNode unionAns4 = new UnionNodeImpl(ans4Variables);
		queryBuilder.addChild(topAns4Node, unionAns4);

		Variable d = DATA_FACTORY.getVariable("d");
		Variable e = DATA_FACTORY.getVariable("e");
		ConstructionNode t4Ans4Node = new ConstructionNodeImpl(ans4Variables,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, d, y, e)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns4, t4Ans4Node);

		ExtensionalDataNode t4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t4", 2), d, e));
		queryBuilder.addChild(t4Ans4Node, t4);

		Variable f =  DATA_FACTORY.getVariable("f");
		Variable g =  DATA_FACTORY.getVariable("g");
		ConstructionNode t5Ans4Node = new ConstructionNodeImpl(ans4Variables,
				new ImmutableSubstitutionImpl<>(ImmutableMap.of(x, f, y, g)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns4, t5Ans4Node);

		ExtensionalDataNode t5 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t5", 2), f, g));
		queryBuilder.addChild(t5Ans4Node, t5);

        this.unionAns2Node = unionAns2;
        this.unionAns4Node = unionAns4;
        return queryBuilder.build();
    }


}
