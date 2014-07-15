/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.algebra;

import gr.uoa.di.madgik.sesame.functions.EHContainsFunc;
import gr.uoa.di.madgik.sesame.functions.EHCoveredByFunc;
import gr.uoa.di.madgik.sesame.functions.EHCoversFunc;
import gr.uoa.di.madgik.sesame.functions.EHDisjointFunc;
import gr.uoa.di.madgik.sesame.functions.EHEqualsFunc;
import gr.uoa.di.madgik.sesame.functions.EHInsideFunc;
import gr.uoa.di.madgik.sesame.functions.EHOverlapFunc;
import gr.uoa.di.madgik.sesame.functions.SpatialContainFunc;
import gr.uoa.di.madgik.sesame.functions.SpatialCrossesFunc;
import gr.uoa.di.madgik.sesame.functions.SpatialDisjointFunc;
import gr.uoa.di.madgik.sesame.functions.SpatialEqualFunc;
import gr.uoa.di.madgik.sesame.functions.SpatialIntersectsFunc;
import gr.uoa.di.madgik.sesame.functions.SpatialOverlapFunc;
import gr.uoa.di.madgik.sesame.functions.SpatialTouchesFunc;
import gr.uoa.di.madgik.sesame.functions.SpatialWithinFunc;

/**
 * An interface for query model visitors, implementing the Visitor pattern. Core
 * query model nodes will call their type-specific method when
 * {@link QueryModelNode#visit(QueryModelVisitor)} is called. The method
 * {@link #meetOther(QueryModelNode)} is provided as a hook for foreign query
 * model nodes.
 */
public interface QueryModelVisitor<X extends Exception> {
	
	public void meet(QueryRoot node)
			throws X;

	public void meet(Add node)
			throws X;
	
	public void meet(And node)
			throws X;

	public void meet(ArbitraryLengthPath node)
		throws X;

	public void meet(Avg node)
		throws X;

	public void meet(BindingSetAssignment node)
		throws X;

	public void meet(BNodeGenerator node)
		throws X;
	
	public void meet(Bound node)
		throws X;

	public void meet(Clear clear)
			throws X;

	public void meet(Coalesce node)
		throws X;

	public void meet(Compare node)
		throws X;

	public void meet(CompareAll node)
		throws X;

	public void meet(CompareAny node)
		throws X;

	public void meet(DescribeOperator node)
			throws X;

	public void meet(Copy copy)
			throws X;

	public void meet(Count node)
		throws X;

	public void meet(Create create)
			throws X;

	public void meet(Datatype node)
		throws X;

	public void meet(DeleteData deleteData)
			throws X;

	public void meet(Difference node)
		throws X;

	public void meet(Distinct node)
		throws X;

	public void meet(EmptySet node)
		throws X;

	public void meet(Exists node)
		throws X;

	public void meet(Extension node)
		throws X;

	public void meet(ExtensionElem node)
		throws X;

	public void meet(Filter node)
		throws X;

	public void meet(FunctionCall node)
		throws X;

	public void meet(Group node)
		throws X;

	public void meet(GroupConcat node)
		throws X;

	public void meet(GroupElem node)
		throws X;

	public void meet(If node)
		throws X;

	public void meet(In node)
		throws X;

	public void meet(InsertData insertData)
			throws X;

	public void meet(Intersection node)
		throws X;

	public void meet(IRIFunction node)
		throws X;

	public void meet(IsBNode node)
		throws X;

	public void meet(IsLiteral node)
		throws X;

	public void meet(IsNumeric node)
		throws X;

	public void meet(IsResource node)
		throws X;

	public void meet(IsURI node)
		throws X;

	public void meet(Join node)
		throws X;

	public void meet(Label node)
		throws X;

	public void meet(Lang node)
		throws X;

	public void meet(LangMatches node)
		throws X;

	public void meet(LeftJoin node)
		throws X;

	public void meet(Like node)
		throws X;

	public void meet(Load load)
			throws X;

	public void meet(LocalName node)
		throws X;

	public void meet(MathExpr node)
		throws X;

	public void meet(Max node)
		throws X;

	public void meet(Min node)
		throws X;

	public void meet(Modify modify)
			throws X;

	public void meet(Move move)
			throws X;

	public void meet(MultiProjection node)
		throws X;

	public void meet(Namespace node)
		throws X;

	public void meet(Not node)
		throws X;

	public void meet(Or node)
		throws X;

	public void meet(Order node)
		throws X;

	public void meet(OrderElem node)
		throws X;

	public void meet(Projection node)
		throws X;
	
	public void meet(ProjectionElem node)
		throws X;

	public void meet(ProjectionElemList node)
		throws X;

	public void meet(Reduced node)
		throws X;

	public void meet(Regex node)
		throws X;

	public void meet(SameTerm node)
		throws X;

	public void meet(SpatialOverlapFunc node)
			throws X;

	public void meet(SpatialContainFunc node)
			throws X;
	
	public void meet(SpatialCrossesFunc node)
			throws X;
	
	public void meet(SpatialDisjointFunc node)
			throws X;
	
	public void meet(SpatialEqualFunc node)
			throws X;
	
	public void meet(SpatialIntersectsFunc node)
			throws X;
	
	public void meet(SpatialTouchesFunc node)
			throws X;
	
	public void meet(SpatialWithinFunc node)
			throws X;
	
	
	public void meet(EHCoveredByFunc node)
			throws X;
	
	public void meet(EHCoversFunc node)
			throws X;
	
	public void meet(EHDisjointFunc node)
			throws X;
	
	public void meet(EHEqualsFunc node)
			throws X;
	
	public void meet(EHInsideFunc node)
			throws X;
	
	public void meet(EHOverlapFunc node)
			throws X;
	
	public void meet(EHContainsFunc node)
			throws X;
	
	public void meet(Sample node)
		throws X;

	public void meet(Service node)
		throws X;

	public void meet(SingletonSet node)
		throws X;

	public void meet(Slice node)
		throws X;

	public void meet(StatementPattern node)
		throws X;

	public void meet(Str node)
		throws X;

	public void meet(Sum node)
		throws X;

	public void meet(Union node)
		throws X;

	public void meet(ValueConstant node)
		throws X;

	/**
	 * @since 2.7.4
	 */
	public void meet(ListMemberOperator node)
			throws X;

	public void meet(Var node)
		throws X;

	public void meet(ZeroLengthPath node)
		throws X;

	public void meetOther(QueryModelNode node)
		throws X;




}
