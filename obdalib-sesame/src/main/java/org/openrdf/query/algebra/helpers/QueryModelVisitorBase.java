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
package org.openrdf.query.algebra.helpers;

import gr.uoa.di.madgik.sesame.functions.SpatialOverlapFunc;

import org.openrdf.query.algebra.Add;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.ArbitraryLengthPath;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Clear;
import org.openrdf.query.algebra.Coalesce;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.CompareSubQueryValueOperator;
import org.openrdf.query.algebra.DescribeOperator;
import org.openrdf.query.algebra.Copy;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Create;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.DeleteData;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.IRIFunction;
import org.openrdf.query.algebra.If;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.InsertData;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsNumeric;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Label;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Like;
import org.openrdf.query.algebra.Load;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Modify;
import org.openrdf.query.algebra.Move;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.NAryValueOperator;
import org.openrdf.query.algebra.Namespace;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.SubQueryValueOperator;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ListMemberOperator;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.ZeroLengthPath;

/**
 * Base class for {@link QueryModelVisitor}s. This class implements all
 * <tt>meet(... node)</tt> methods from the visitor interface, forwarding the
 * call to a method for the node's supertype. This is done recursively until
 * {@link #meetNode} is reached. This allows subclasses to easily define default
 * behaviour for visited nodes of a certain type. The default implementation of
 * {@link #meetNode} is to visit the node's children.
 */
public abstract class QueryModelVisitorBase<X extends Exception> implements QueryModelVisitor<X> {

	public void meet(Add node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(And node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(ArbitraryLengthPath node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Avg node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(BindingSetAssignment node)
		throws X
	{
		meetNode(node);
	}

	public void meet(BNodeGenerator node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Bound node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Clear node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(Coalesce node)
		throws X
	{
		meetNAryValueOperator(node);
	}

	public void meet(Compare node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(CompareAll node)
		throws X
	{
		meetCompareSubQueryValueOperator(node);
	}

	public void meet(CompareAny node)
		throws X
	{
		meetCompareSubQueryValueOperator(node);
	}

	public void meet(DescribeOperator node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(Copy node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(Count node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Create node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(Datatype node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(DeleteData node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(Difference node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(Distinct node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(EmptySet node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Exists node)
		throws X
	{
		meetSubQueryValueOperator(node);
	}

	public void meet(Extension node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(ExtensionElem node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Filter node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(FunctionCall node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Group node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(GroupConcat node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(GroupElem node)
		throws X
	{
		meetNode(node);
	}

	public void meet(If node)
		throws X
	{
		meetNode(node);
	}

	public void meet(In node)
		throws X
	{
		meetCompareSubQueryValueOperator(node);
	}

	public void meet(InsertData node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(Intersection node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(IRIFunction node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(IsBNode node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(IsLiteral node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(IsNumeric node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(IsResource node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(IsURI node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Join node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(Label node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Lang node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(LangMatches node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(LeftJoin node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(Like node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Load node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(LocalName node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(MathExpr node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(Max node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Min node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Modify node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(Move node)
		throws X
	{
		meetUpdateExpr(node);
	}

	public void meet(MultiProjection node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(Namespace node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Not node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Or node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(Order node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(OrderElem node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Projection node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(ProjectionElem node)
		throws X
	{
		meetNode(node);
	}

	public void meet(ProjectionElemList node)
		throws X
	{
		meetNode(node);
	}

	public void meet(QueryRoot node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Reduced node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(Regex node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(SameTerm node)
		throws X
	{
		meetBinaryValueOperator(node);
	}
	
	public void meet(SpatialOverlapFunc node)
			throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(Sample node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Service node)
		throws X
	{
		meetNode(node);
	}

	public void meet(SingletonSet node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Slice node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(StatementPattern node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Str node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Sum node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Union node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(ValueConstant node)
		throws X
	{
		meetNode(node);
	}

	public void meet(ListMemberOperator node)
		throws X
	{
		meetNAryValueOperator(node);
	}

	public void meet(Var node)
		throws X
	{
		meetNode(node);
	}

	public void meet(ZeroLengthPath node)
		throws X
	{
		meetNode(node);
	}

	public void meetOther(QueryModelNode node)
		throws X
	{
		if (node instanceof UnaryTupleOperator) {
			meetUnaryTupleOperator((UnaryTupleOperator)node);
		}
		else if (node instanceof BinaryTupleOperator) {
			meetBinaryTupleOperator((BinaryTupleOperator)node);
		}
		else if (node instanceof CompareSubQueryValueOperator) {
			meetCompareSubQueryValueOperator((CompareSubQueryValueOperator)node);
		}
		else if (node instanceof SubQueryValueOperator) {
			meetSubQueryValueOperator((SubQueryValueOperator)node);
		}
		else if (node instanceof UnaryValueOperator) {
			meetUnaryValueOperator((UnaryValueOperator)node);
		}
		else if (node instanceof BinaryValueOperator) {
			meetBinaryValueOperator((BinaryValueOperator)node);
		}
		else if (node instanceof UpdateExpr) {
			meetUpdateExpr((UpdateExpr)node);
		}
		else {
			meetNode(node);
		}
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link BinaryTupleOperator} node as argument. Forwards the call to
	 * {@link #meetNode} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetBinaryTupleOperator(BinaryTupleOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link BinaryValueOperator} node as argument. Forwards the call to
	 * {@link #meetNode} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetBinaryValueOperator(BinaryValueOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link CompareSubQueryValueOperator} node as argument. Forwards the call
	 * to {@link #meetSubQueryValueOperator} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetCompareSubQueryValueOperator(CompareSubQueryValueOperator node)
		throws X
	{
		meetSubQueryValueOperator(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link org.openrdf.query.algebra.NAryValueOperator} node as argument.
	 * Forwards the call to {@link #meetNode} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetNAryValueOperator(NAryValueOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all of the other <tt>meet</tt> methods that are not
	 * overridden in subclasses. This method can be overridden in subclasses to
	 * define default behaviour when visiting nodes. The default behaviour of
	 * this method is to visit the node's children.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetNode(QueryModelNode node)
		throws X
	{
		node.visitChildren(this);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link SubQueryValueOperator} node as argument. Forwards the call to
	 * {@link #meetNode} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetSubQueryValueOperator(SubQueryValueOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link UnaryTupleOperator} node as argument. Forwards the call to
	 * {@link #meetNode} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetUnaryTupleOperator(UnaryTupleOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link UnaryValueOperator} node as argument. Forwards the call to
	 * {@link #meetNode} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetUnaryValueOperator(UnaryValueOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a {@link UpdateExpr} node
	 * as argument. Forwards the call to {@link #meetNode} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetUpdateExpr(UpdateExpr node)
		throws X
	{
		meetNode(node);
	}
}
