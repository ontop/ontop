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
package org.openrdf.query.algebra.evaluation.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.IntersectIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.LimitIteration;
import info.aduna.iteration.OffsetIteration;
import info.aduna.iteration.ReducedIteration;
import info.aduna.iteration.SingletonIteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.ArbitraryLengthPath;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Coalesce;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.DescribeOperator;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.IRIFunction;
import org.openrdf.query.algebra.If;
import org.openrdf.query.algebra.In;
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
import org.openrdf.query.algebra.ListMemberOperator;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Namespace;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.ZeroLengthPath;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.federation.FederatedService;
import org.openrdf.query.algebra.evaluation.federation.FederatedService.QueryType;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceManager;
import org.openrdf.query.algebra.evaluation.federation.ServiceJoinIterator;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.iterator.BadlyDesignedLeftJoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.BottomUpJoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.DescribeIteration;
import org.openrdf.query.algebra.evaluation.iterator.ExtensionIterator;
import org.openrdf.query.algebra.evaluation.iterator.FilterIterator;
import org.openrdf.query.algebra.evaluation.iterator.GroupIterator;
import org.openrdf.query.algebra.evaluation.iterator.JoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.LeftJoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.MultiProjectionIterator;
import org.openrdf.query.algebra.evaluation.iterator.OrderIterator;
import org.openrdf.query.algebra.evaluation.iterator.PathIteration;
import org.openrdf.query.algebra.evaluation.iterator.ProjectionIterator;
import org.openrdf.query.algebra.evaluation.iterator.SPARQLMinusIteration;
import org.openrdf.query.algebra.evaluation.iterator.SilentIteration;
import org.openrdf.query.algebra.evaluation.iterator.ZeroLengthPathIteration;
import org.openrdf.query.algebra.evaluation.util.MathUtil;
import org.openrdf.query.algebra.evaluation.util.OrderComparator;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.TupleExprs;
import org.openrdf.query.algebra.helpers.VarNameCollector;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.repository.RepositoryException;

/**
 * Evaluates the TupleExpr and ValueExpr using Iterators and common tripleSource
 * API.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 * @author David Huynh
 * @author Andreas Schwarte
 */
public class EvaluationStrategyImpl implements EvaluationStrategy {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final TripleSource tripleSource;

	protected final Dataset dataset;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public EvaluationStrategyImpl(TripleSource tripleSource) {
		this(tripleSource, null);
	}

	public EvaluationStrategyImpl(TripleSource tripleSource, Dataset dataset) {
		this.tripleSource = tripleSource;
		this.dataset = dataset;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		if (expr instanceof StatementPattern) {
			return evaluate((StatementPattern)expr, bindings);
		}
		else if (expr instanceof UnaryTupleOperator) {
			return evaluate((UnaryTupleOperator)expr, bindings);
		}
		else if (expr instanceof BinaryTupleOperator) {
			return evaluate((BinaryTupleOperator)expr, bindings);
		}
		else if (expr instanceof SingletonSet) {
			return evaluate((SingletonSet)expr, bindings);
		}
		else if (expr instanceof EmptySet) {
			return evaluate((EmptySet)expr, bindings);
		}
		else if (expr instanceof ExternalSet) {
			return evaluate((ExternalSet)expr, bindings);
		}
		else if (expr instanceof ZeroLengthPath) {
			return evaluate((ZeroLengthPath)expr, bindings);
		}
		else if (expr instanceof ArbitraryLengthPath) {
			return evaluate((ArbitraryLengthPath)expr, bindings);
		}
		else if (expr instanceof BindingSetAssignment) {
			return evaluate((BindingSetAssignment)expr, bindings);
		}
		else if (expr == null) {
			throw new IllegalArgumentException("expr must not be null");
		}
		else {
			throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(ArbitraryLengthPath alp,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		final Scope scope = alp.getScope();
		final Var subjectVar = alp.getSubjectVar();
		final TupleExpr pathExpression = alp.getPathExpression();
		final Var objVar = alp.getObjectVar();
		final Var contextVar = alp.getContextVar();
		final long minLength = alp.getMinLength();

		return new PathIteration(this, scope, subjectVar, pathExpression, objVar, contextVar, minLength,
				bindings);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(ZeroLengthPath zlp,
			final BindingSet bindings)
		throws QueryEvaluationException
	{

		final Var subjectVar = zlp.getSubjectVar();
		final Var objVar = zlp.getObjectVar();
		final Var contextVar = zlp.getContextVar();

		Value subj = null;
		try {
			subj = evaluate(subjectVar, bindings);
		}
		catch (QueryEvaluationException e) {
		}

		Value obj = null;
		try {
			obj = evaluate(objVar, bindings);
		}
		catch (QueryEvaluationException e) {
		}

		if (subj != null && obj != null) {
			if (!subj.equals(obj)) {
				return new EmptyIteration<BindingSet, QueryEvaluationException>();
			}
		}

		return getZeroLengthPathIterator(bindings, subjectVar, objVar, contextVar, subj, obj);
	}

	protected ZeroLengthPathIteration getZeroLengthPathIterator(final BindingSet bindings,
			final Var subjectVar, final Var objVar, final Var contextVar, Value subj, Value obj)
	{
		return new ZeroLengthPathIteration(this, subjectVar, objVar, subj, obj, contextVar, bindings);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service service,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		Var serviceRef = service.getServiceRef();

		String serviceUri;
		if (serviceRef.hasValue())
			serviceUri = serviceRef.getValue().stringValue();
		else {
			if (bindings != null && bindings.hasBinding(serviceRef.getName())) {
				serviceUri = bindings.getBinding(serviceRef.getName()).getValue().stringValue();
			}
			else {
				throw new QueryEvaluationException("SERVICE variables must be bound at evaluation time.");
			}
		}

		try {

			FederatedService fs = FederatedServiceManager.getInstance().getService(serviceUri);

			// create a copy of the free variables, and remove those for which
			// bindings are available (we can set them as constraints!)
			Set<String> freeVars = new HashSet<String>(service.getServiceVars());
			freeVars.removeAll(bindings.getBindingNames());

			// Get bindings from values pre-bound into variables.
			MapBindingSet allBindings = new MapBindingSet();
			for (Binding binding : bindings) {
				allBindings.addBinding(binding.getName(), binding.getValue());
			}

			Set<Var> boundVars = getBoundVariables(service);
			for (Var boundVar : boundVars) {
				freeVars.remove(boundVar.getName());
				allBindings.addBinding(boundVar.getName(), boundVar.getValue());
			}
			bindings = allBindings;

			String baseUri = service.getBaseURI();

			// depending on freeVars.size: either SELECT or ASK query
			String queryString = service.getQueryString(freeVars);

			// special case: no free variables => perform ASK query
			if (freeVars.size() == 0) {
				return fs.evaluate(queryString, bindings, baseUri, QueryType.ASK, service);
			}

			// otherwise: perform a SELECT query
			CloseableIteration<BindingSet, QueryEvaluationException> result = fs.evaluate(queryString, bindings,
					baseUri, QueryType.SELECT, service);

			if (service.isSilent())
				return new SilentIteration(result);
			else
				return result;

		}
		catch (QueryEvaluationException e) {
			// suppress exceptions if silent
			if (service.isSilent()) {
				return new SingletonIteration<BindingSet, QueryEvaluationException>(bindings);
			}
			else {
				throw e;
			}
		}
		catch (RuntimeException e) {
			// suppress special exceptions (e.g. UndeclaredThrowable with wrapped
			// QueryEval) if silent
			if (service.isSilent()) {
				return new SingletonIteration<BindingSet, QueryEvaluationException>(bindings);
			}
			else {
				throw e;
			}
		}
		catch (RepositoryException e) {
			// suppress exceptions if silent
			if (service.isSilent()) {
				return new SingletonIteration<BindingSet, QueryEvaluationException>(bindings);
			}
			else {
				throw new QueryEvaluationException(e);
			}
		}

	}

	private Set<Var> getBoundVariables(Service service) {
		BoundVarVisitor visitor = new BoundVarVisitor();
		visitor.meet(service);
		return visitor.boundVars;
	}

	private static class BoundVarVisitor extends QueryModelVisitorBase<RuntimeException> {

		private final Set<Var> boundVars = new HashSet<Var>();

		@Override
		public void meet(Var var) {
			if (var.hasValue()) {
				boundVars.add(var);
			}
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(DescribeOperator operator,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(operator.getArg(), bindings);
		return new DescribeIteration(iter, this, operator.getBindingNames(), bindings);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(StatementPattern sp,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		final Var subjVar = sp.getSubjectVar();
		final Var predVar = sp.getPredicateVar();
		final Var objVar = sp.getObjectVar();
		final Var conVar = sp.getContextVar();

		final Value subjValue = getVarValue(subjVar, bindings);
		final Value predValue = getVarValue(predVar, bindings);
		final Value objValue = getVarValue(objVar, bindings);
		final Value contextValue = getVarValue(conVar, bindings);

		CloseableIteration<? extends Statement, QueryEvaluationException> stIter = null;

		try {
			Resource[] contexts;

			Set<URI> graphs = null;
			boolean emptyGraph = false;

			if (dataset != null) {
				if (sp.getScope() == Scope.DEFAULT_CONTEXTS) {
					graphs = dataset.getDefaultGraphs();
					emptyGraph = graphs.isEmpty() && !dataset.getNamedGraphs().isEmpty();
				}
				else {
					graphs = dataset.getNamedGraphs();
					emptyGraph = graphs.isEmpty() && !dataset.getDefaultGraphs().isEmpty();
				}
			}

			if (emptyGraph) {
				// Search zero contexts
				return new EmptyIteration<BindingSet, QueryEvaluationException>();
			}
			else if (graphs == null || graphs.isEmpty()) {
				// store default behaivour
				if (contextValue != null) {
					contexts = new Resource[] { (Resource)contextValue };
				}
				/* TODO activate this to have an exclusive (rather than inclusive) interpretation of the default graph in SPARQL querying.
				else if (sp.getScope() == Scope.DEFAULT_CONTEXTS ) {
					contexts = new Resource[] { (Resource)null };
				}
				*/
				else {
					contexts = new Resource[0];
				}
			}
			else if (contextValue != null) {
				if (graphs.contains(contextValue)) {
					contexts = new Resource[] { (Resource)contextValue };
				}
				else {
					// Statement pattern specifies a context that is not part of
					// the dataset
					return new EmptyIteration<BindingSet, QueryEvaluationException>();
				}
			}
			else {
				contexts = new Resource[graphs.size()];
				int i = 0;
				for (URI graph : graphs) {
					URI context = null;
					if (!SESAME.NIL.equals(graph)) {
						context = graph;
					}
					contexts[i++] = context;
				}
			}

			stIter = tripleSource.getStatements((Resource)subjValue, (URI)predValue, objValue, contexts);

			if (contexts.length == 0 && sp.getScope() == Scope.NAMED_CONTEXTS) {
				// Named contexts are matched by retrieving all statements from
				// the store and filtering out the statements that do not have a
				// context.
				stIter = new FilterIteration<Statement, QueryEvaluationException>(stIter) {

					@Override
					protected boolean accept(Statement st) {
						return st.getContext() != null;
					}

				}; // end anonymous class
			}
		}
		catch (ClassCastException e) {
			// Invalid value type for subject, predicate and/or context
			return new EmptyIteration<BindingSet, QueryEvaluationException>();
		}

		// The same variable might have been used multiple times in this
		// StatementPattern, verify value equality in those cases.
		// TODO: skip this filter if not necessary
		stIter = new FilterIteration<Statement, QueryEvaluationException>(stIter) {

			@Override
			protected boolean accept(Statement st) {
				Resource subj = st.getSubject();
				URI pred = st.getPredicate();
				Value obj = st.getObject();
				Resource context = st.getContext();

				if (subjVar != null && subjValue == null) {
					if (subjVar.equals(predVar) && !subj.equals(pred)) {
						return false;
					}
					if (subjVar.equals(objVar) && !subj.equals(obj)) {
						return false;
					}
					if (subjVar.equals(conVar) && !subj.equals(context)) {
						return false;
					}
				}

				if (predVar != null && predValue == null) {
					if (predVar.equals(objVar) && !pred.equals(obj)) {
						return false;
					}
					if (predVar.equals(conVar) && !pred.equals(context)) {
						return false;
					}
				}

				if (objVar != null && objValue == null) {
					if (objVar.equals(conVar) && !obj.equals(context)) {
						return false;
					}
				}

				return true;
			}
		};

		// Return an iterator that converts the statements to var bindings
		return new ConvertingIteration<Statement, BindingSet, QueryEvaluationException>(stIter) {

			@Override
			protected BindingSet convert(Statement st) {
				QueryBindingSet result = new QueryBindingSet(bindings);

				if (subjVar != null && !subjVar.isConstant() && !result.hasBinding(subjVar.getName())) {
					result.addBinding(subjVar.getName(), st.getSubject());
				}
				if (predVar != null && !predVar.isConstant() && !result.hasBinding(predVar.getName())) {
					result.addBinding(predVar.getName(), st.getPredicate());
				}
				if (objVar != null && !objVar.isConstant() && !result.hasBinding(objVar.getName())) {
					result.addBinding(objVar.getName(), st.getObject());
				}
				if (conVar != null && !conVar.isConstant() && !result.hasBinding(conVar.getName()) && st.getContext() != null) {
					result.addBinding(conVar.getName(), st.getContext());
				}

				return result;
			}
		};
	}

	protected Value getVarValue(Var var, BindingSet bindings) {
		if (var == null) {
			return null;
		}
		else if (var.hasValue()) {
			return var.getValue();
		}
		else {
			return bindings.getValue(var.getName());
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(UnaryTupleOperator expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		if (expr instanceof Projection) {
			return evaluate((Projection)expr, bindings);
		}
		else if (expr instanceof MultiProjection) {
			return evaluate((MultiProjection)expr, bindings);
		}
		else if (expr instanceof Filter) {
			return evaluate((Filter)expr, bindings);
		}
		else if (expr instanceof Service) {
			return evaluate((Service)expr, bindings);
		}
		else if (expr instanceof Slice) {
			return evaluate((Slice)expr, bindings);
		}
		else if (expr instanceof Extension) {
			return evaluate((Extension)expr, bindings);
		}
		else if (expr instanceof Distinct) {
			return evaluate((Distinct)expr, bindings);
		}
		else if (expr instanceof Reduced) {
			return evaluate((Reduced)expr, bindings);
		}
		else if (expr instanceof Group) {
			return evaluate((Group)expr, bindings);
		}
		else if (expr instanceof Order) {
			return evaluate((Order)expr, bindings);
		}
		else if (expr instanceof QueryRoot) {
			return evaluate(((QueryRoot)expr).getArg(), bindings);
		}
		else if (expr instanceof DescribeOperator) {
			return evaluate((DescribeOperator)expr, bindings);
		}
		else if (expr == null) {
			throw new IllegalArgumentException("expr must not be null");
		}
		else {
			throw new QueryEvaluationException("Unknown unary tuple operator type: " + expr.getClass());
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(BindingSetAssignment bsa,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;

		final Iterator<BindingSet> iter = bsa.getBindingSets().iterator();

		final QueryBindingSet b = new QueryBindingSet(bindings);
		
		result = new CloseableIterationBase<BindingSet, QueryEvaluationException>() {

			public boolean hasNext()
				throws QueryEvaluationException
			{
				return iter.hasNext();
			}

			public BindingSet next()
				throws QueryEvaluationException
			{
				final QueryBindingSet result = new QueryBindingSet(b);
				result.addAll(iter.next());
				return result;
			}

			public void remove()
				throws QueryEvaluationException
			{
				iter.remove();
			}
		};

		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Projection projection,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;

		result = this.evaluate(projection.getArg(), bindings);
		result = new ProjectionIterator(projection, result, bindings);
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(MultiProjection multiProjection,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;
		result = this.evaluate(multiProjection.getArg(), bindings);
		result = new MultiProjectionIterator(multiProjection, result, bindings);
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Filter filter, BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;
		result = this.evaluate(filter.getArg(), bindings);
		result = new FilterIterator(filter, result, this);
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Slice slice, BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result = evaluate(slice.getArg(), bindings);

		if (slice.hasOffset()) {
			result = new OffsetIteration<BindingSet, QueryEvaluationException>(result, slice.getOffset());
		}

		if (slice.hasLimit()) {
			result = new LimitIteration<BindingSet, QueryEvaluationException>(result, slice.getLimit());
		}

		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Extension extension,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;
		try {
			result = this.evaluate(extension.getArg(), bindings);
		}
		catch (ValueExprEvaluationException e) {
			// a type error in an extension argument should be silently ignored and
			// result in zero bindings.
			result = new EmptyIteration<BindingSet, QueryEvaluationException>();
		}

		result = new ExtensionIterator(extension, result, this);
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Distinct distinct,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new DistinctIteration<BindingSet, QueryEvaluationException>(
				evaluate(distinct.getArg(), bindings));
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Reduced reduced,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new ReducedIteration<BindingSet, QueryEvaluationException>(evaluate(reduced.getArg(), bindings));
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Group node, BindingSet bindings)
		throws QueryEvaluationException
	{
		return new GroupIterator(this, node, bindings);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Order node, BindingSet bindings)
		throws QueryEvaluationException
	{
		ValueComparator vcmp = new ValueComparator();
		OrderComparator cmp = new OrderComparator(this, node, vcmp);
		boolean reduced = isReducedOrDistinct(node);
		long limit = getLimit(node);
		return new OrderIterator(evaluate(node.getArg(), bindings), cmp, limit, reduced);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(BinaryTupleOperator expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		if (expr instanceof Join) {
			return evaluate((Join)expr, bindings);
		}
		else if (expr instanceof LeftJoin) {
			return evaluate((LeftJoin)expr, bindings);
		}
		else if (expr instanceof Union) {
			return evaluate((Union)expr, bindings);
		}
		else if (expr instanceof Intersection) {
			return evaluate((Intersection)expr, bindings);
		}
		else if (expr instanceof Difference) {
			return evaluate((Difference)expr, bindings);
		}
		else if (expr == null) {
			throw new IllegalArgumentException("expr must not be null");
		}
		else {
			throw new QueryEvaluationException("Unsupported binary tuple operator type: " + expr.getClass());
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Join join, BindingSet bindings)
		throws QueryEvaluationException
	{
		// efficient computation of a SERVICE join using vectored evaluation
		// TODO maybe we can create a ServiceJoin node already in the parser?
		if (join.getRightArg() instanceof Service) {
			CloseableIteration<BindingSet, QueryEvaluationException> leftIter = evaluate(join.getLeftArg(),
					bindings);
			return new ServiceJoinIterator(leftIter, (Service)join.getRightArg(), bindings, this);
		}

		if (TupleExprs.containsProjection(join.getRightArg())) {
			return new BottomUpJoinIterator(this, join, bindings);
		}
		else {
			return new JoinIterator(this, join, bindings);
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(LeftJoin leftJoin,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		// Check whether optional join is "well designed" as defined in section
		// 4.2 of "Semantics and Complexity of SPARQL", 2006, Jorge Pérez et al.
		VarNameCollector optionalVarCollector = new VarNameCollector();
		leftJoin.getRightArg().visit(optionalVarCollector);
		if (leftJoin.hasCondition()) {
			leftJoin.getCondition().visit(optionalVarCollector);
		}

		Set<String> problemVars = optionalVarCollector.getVarNames();
		problemVars.removeAll(leftJoin.getLeftArg().getBindingNames());
		problemVars.retainAll(bindings.getBindingNames());

		if (problemVars.isEmpty()) {
			// left join is "well designed"
			return new LeftJoinIterator(this, leftJoin, bindings);
		}
		else {
			return new BadlyDesignedLeftJoinIterator(this, leftJoin, bindings, problemVars);
		}
	}

	@SuppressWarnings("unchecked")
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Union union,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(union.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(union.getRightArg(), bindings);
			}
		};

		return new UnionIteration<BindingSet, QueryEvaluationException>(leftArg, rightArg);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Intersection intersection,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(intersection.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(intersection.getRightArg(), bindings);
			}
		};

		return new IntersectIteration<BindingSet, QueryEvaluationException>(leftArg, rightArg);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Difference difference,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(difference.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(difference.getRightArg(), bindings);
			}
		};

		return new SPARQLMinusIteration<QueryEvaluationException>(leftArg, rightArg);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(SingletonSet singletonSet,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new SingletonIteration<BindingSet, QueryEvaluationException>(bindings);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(EmptySet emptySet,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new EmptyIteration<BindingSet, QueryEvaluationException>();
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(ExternalSet external,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return external.evaluate(bindings);
	}

	public Value evaluate(ValueExpr expr, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		if (expr instanceof Var) {
			return evaluate((Var)expr, bindings);
		}
		else if (expr instanceof ValueConstant) {
			return evaluate((ValueConstant)expr, bindings);
		}
		else if (expr instanceof BNodeGenerator) {
			return evaluate((BNodeGenerator)expr, bindings);
		}
		else if (expr instanceof Bound) {
			return evaluate((Bound)expr, bindings);
		}
		else if (expr instanceof Str) {
			return evaluate((Str)expr, bindings);
		}
		else if (expr instanceof Label) {
			return evaluate((Label)expr, bindings);
		}
		else if (expr instanceof Lang) {
			return evaluate((Lang)expr, bindings);
		}
		else if (expr instanceof LangMatches) {
			return evaluate((LangMatches)expr, bindings);
		}
		else if (expr instanceof Datatype) {
			return evaluate((Datatype)expr, bindings);
		}
		else if (expr instanceof Namespace) {
			return evaluate((Namespace)expr, bindings);
		}
		else if (expr instanceof LocalName) {
			return evaluate((LocalName)expr, bindings);
		}
		else if (expr instanceof IsResource) {
			return evaluate((IsResource)expr, bindings);
		}
		else if (expr instanceof IsURI) {
			return evaluate((IsURI)expr, bindings);
		}
		else if (expr instanceof IsBNode) {
			return evaluate((IsBNode)expr, bindings);
		}
		else if (expr instanceof IsLiteral) {
			return evaluate((IsLiteral)expr, bindings);
		}
		else if (expr instanceof IsNumeric) {
			return evaluate((IsNumeric)expr, bindings);
		}
		else if (expr instanceof IRIFunction) {
			return evaluate((IRIFunction)expr, bindings);
		}
		else if (expr instanceof Regex) {
			return evaluate((Regex)expr, bindings);
		}
		else if (expr instanceof Coalesce) {
			return evaluate((Coalesce)expr, bindings);
		}
		else if (expr instanceof Like) {
			return evaluate((Like)expr, bindings);
		}
		else if (expr instanceof FunctionCall) {
			return evaluate((FunctionCall)expr, bindings);
		}
		else if (expr instanceof And) {
			return evaluate((And)expr, bindings);
		}
		else if (expr instanceof Or) {
			return evaluate((Or)expr, bindings);
		}
		else if (expr instanceof Not) {
			return evaluate((Not)expr, bindings);
		}
		else if (expr instanceof SameTerm) {
			return evaluate((SameTerm)expr, bindings);
		}
		else if (expr instanceof SpatialOverlapFunc) {
			return evaluate((SpatialOverlapFunc)expr, bindings);
		}
		else if (expr instanceof SpatialContainFunc) {
			return evaluate((SpatialContainFunc)expr, bindings);
		}else if (expr instanceof SpatialCrossesFunc) {
			return evaluate((SpatialCrossesFunc)expr, bindings);
		}else if (expr instanceof SpatialDisjointFunc) {
			return evaluate((SpatialDisjointFunc)expr, bindings);
		}else if (expr instanceof SpatialEqualFunc) {
			return evaluate((SpatialEqualFunc)expr, bindings);
		}else if (expr instanceof SpatialIntersectsFunc) {
			return evaluate((SpatialIntersectsFunc)expr, bindings);
		}else if (expr instanceof SpatialTouchesFunc) {
			return evaluate((SpatialTouchesFunc)expr, bindings);
		}else if (expr instanceof SpatialWithinFunc) {
			return evaluate((SpatialWithinFunc)expr, bindings);
		}else if (expr instanceof EHCoveredByFunc) {
			return evaluate((EHCoveredByFunc)expr, bindings);
		}else if (expr instanceof EHCoversFunc) {
			return evaluate((EHCoversFunc)expr, bindings);
		}else if (expr instanceof EHDisjointFunc) {
			return evaluate((EHDisjointFunc)expr, bindings);
		}else if (expr instanceof EHEqualsFunc) {
			return evaluate((EHEqualsFunc)expr, bindings);
		}else if (expr instanceof EHContainsFunc) {
			return evaluate((EHContainsFunc)expr, bindings);
		}else if (expr instanceof EHInsideFunc) {
			return evaluate((EHInsideFunc)expr, bindings);
		}else if (expr instanceof EHOverlapFunc) {
			return evaluate((EHOverlapFunc)expr, bindings);
		}else if (expr instanceof Compare) {
			return evaluate((Compare)expr, bindings);
		}
		else if (expr instanceof MathExpr) {
			return evaluate((MathExpr)expr, bindings);
		}
		else if (expr instanceof In) {
			return evaluate((In)expr, bindings);
		}
		else if (expr instanceof CompareAny) {
			return evaluate((CompareAny)expr, bindings);
		}
		else if (expr instanceof CompareAll) {
			return evaluate((CompareAll)expr, bindings);
		}
		else if (expr instanceof Exists) {
			return evaluate((Exists)expr, bindings);
		}
		else if (expr instanceof If) {
			return evaluate((If)expr, bindings);
		}
		else if (expr instanceof ListMemberOperator) {
			return evaluate((ListMemberOperator)expr, bindings);
		}
		else if (expr == null) {
			throw new IllegalArgumentException("expr must not be null");
		}
		else {
			throw new QueryEvaluationException("Unsupported value expr type: " + expr.getClass());
		}
	}

	public Value evaluate(Var var, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value value = var.getValue();

		if (value == null) {
			value = bindings.getValue(var.getName());
		}

		if (value == null) {
			throw new ValueExprEvaluationException();
		}

		return value;
	}

	public Value evaluate(ValueConstant valueConstant, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		return valueConstant.getValue();
	}

	public Value evaluate(BNodeGenerator node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		ValueExpr nodeIdExpr = node.getNodeIdExpr();

		if (nodeIdExpr != null) {
			Value nodeId = evaluate(nodeIdExpr, bindings);

			if (nodeId instanceof Literal) {
				String nodeLabel = ((Literal)nodeId).getLabel() + (bindings.toString().hashCode());
				return tripleSource.getValueFactory().createBNode(nodeLabel);
			}
			else {
				throw new ValueExprEvaluationException("BNODE function argument must be a literal");
			}
		}
		return tripleSource.getValueFactory().createBNode();
	}

	public Value evaluate(Bound node, BindingSet bindings)
		throws QueryEvaluationException
	{
		try {
			Value argValue = evaluate(node.getArg(), bindings);
			return BooleanLiteralImpl.valueOf(argValue != null);
		}
		catch (ValueExprEvaluationException e) {
			return BooleanLiteralImpl.FALSE;
		}
	}

	public Value evaluate(Str node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof URI) {
			return tripleSource.getValueFactory().createLiteral(argValue.toString());
		}
		else if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			if (QueryEvaluationUtil.isSimpleLiteral(literal) && literal.getDatatype() == null) {
				return literal;
			}
			else {
				return tripleSource.getValueFactory().createLiteral(literal.getLabel());
			}
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}

	public Value evaluate(Label node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		// FIXME: deprecate Label in favour of Str(?)
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			if (QueryEvaluationUtil.isSimpleLiteral(literal) && literal.getDatatype() == null) {
				return literal;
			}
			else {
				return tripleSource.getValueFactory().createLiteral(literal.getLabel());
			}
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}

	public Value evaluate(Lang node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			String langTag = literal.getLanguage();
			if (langTag == null) {
				langTag = "";
			}

			return tripleSource.getValueFactory().createLiteral(langTag);
		}

		throw new ValueExprEvaluationException();
	}

	public Value evaluate(Datatype node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value v = evaluate(node.getArg(), bindings);

		if (v instanceof Literal) {
			Literal literal = (Literal)v;

			if (literal.getDatatype() != null) {
				// literal with datatype
				return literal.getDatatype();
			}
			else if (literal.getLanguage() != null) {
				return RDF.LANGSTRING;
			}
			else {
				// simple literal
				return XMLSchema.STRING;
			}

		}

		throw new ValueExprEvaluationException();
	}

	public Value evaluate(Namespace node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof URI) {
			URI uri = (URI)argValue;
			return tripleSource.getValueFactory().createURI(uri.getNamespace());
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}

	public Value evaluate(LocalName node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof URI) {
			URI uri = (URI)argValue;
			return tripleSource.getValueFactory().createLiteral(uri.getLocalName());
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}

	/**
	 * Determines whether the operand (a variable) contains a Resource.
	 * 
	 * @return <tt>true</tt> if the operand contains a Resource, <tt>false</tt>
	 *         otherwise.
	 */
	public Value evaluate(IsResource node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		return BooleanLiteralImpl.valueOf(argValue instanceof Resource);
	}

	/**
	 * Determines whether the operand (a variable) contains a URI.
	 * 
	 * @return <tt>true</tt> if the operand contains a URI, <tt>false</tt>
	 *         otherwise.
	 */
	public Value evaluate(IsURI node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		return BooleanLiteralImpl.valueOf(argValue instanceof URI);
	}

	/**
	 * Determines whether the operand (a variable) contains a BNode.
	 * 
	 * @return <tt>true</tt> if the operand contains a BNode, <tt>false</tt>
	 *         otherwise.
	 */
	public Value evaluate(IsBNode node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		return BooleanLiteralImpl.valueOf(argValue instanceof BNode);
	}

	/**
	 * Determines whether the operand (a variable) contains a Literal.
	 * 
	 * @return <tt>true</tt> if the operand contains a Literal, <tt>false</tt>
	 *         otherwise.
	 */
	public Value evaluate(IsLiteral node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		return BooleanLiteralImpl.valueOf(argValue instanceof Literal);
	}

	/**
	 * Determines whether the operand (a variable) contains a numeric datatyped
	 * literal, i.e. a literal with datatype xsd:float, xsd:double, xsd:decimal,
	 * or a derived datatype of xsd:decimal.
	 * 
	 * @return <tt>true</tt> if the operand contains a numeric datatyped literal,
	 *         <tt>false</tt> otherwise.
	 */
	public Value evaluate(IsNumeric node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof Literal) {
			Literal lit = (Literal)argValue;
			URI datatype = lit.getDatatype();

			return BooleanLiteralImpl.valueOf(datatype != null && XMLDatatypeUtil.isNumericDatatype(datatype));
		}
		else {
			return BooleanLiteralImpl.FALSE;
		}

	}

	/**
	 * Creates a URI from the operand value (a plain literal or a URI).
	 * 
	 * @param node
	 *        represents an invocation of the SPARQL IRI function
	 * @param bindings
	 *        used to generate the value that the URI is based on
	 * @return a URI generated from the given arguments
	 * @throws ValueExprEvaluationException
	 * @throws QueryEvaluationException
	 */
	public URI evaluate(IRIFunction node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof Literal) {
			Literal lit = (Literal)argValue;

			String baseURI = node.getBaseURI();

			URI result = null;
			try {
				result = tripleSource.getValueFactory().createURI(lit.getLabel());
			}
			catch (IllegalArgumentException e) {
				try {
					result = tripleSource.getValueFactory().createURI(baseURI, lit.getLabel());
				}
				catch (IllegalArgumentException e1) {
					throw new ValueExprEvaluationException(e1.getMessage());
				}
			}
			return result;
		}
		else if (argValue instanceof URI) {
			return ((URI)argValue);
		}

		throw new ValueExprEvaluationException();
	}

	/**
	 * Determines whether the two operands match according to the
	 * <code>regex</code> operator.
	 * 
	 * @return <tt>true</tt> if the operands match according to the
	 *         <tt>regex</tt> operator, <tt>false</tt> otherwise.
	 */
	public Value evaluate(Regex node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value arg = evaluate(node.getArg(), bindings);
		Value parg = evaluate(node.getPatternArg(), bindings);
		Value farg = null;
		ValueExpr flagsArg = node.getFlagsArg();
		if (flagsArg != null) {
			farg = evaluate(flagsArg, bindings);
		}

		if (QueryEvaluationUtil.isStringLiteral(arg) && QueryEvaluationUtil.isSimpleLiteral(parg)
				&& (farg == null || QueryEvaluationUtil.isSimpleLiteral(farg)))
		{
			String text = ((Literal)arg).getLabel();
			String ptn = ((Literal)parg).getLabel();
			String flags = "";
			if (farg != null) {
				flags = ((Literal)farg).getLabel();
			}
			// TODO should this Pattern be cached?
			int f = 0;
			for (char c : flags.toCharArray()) {
				switch (c) {
					case 's':
						f |= Pattern.DOTALL;
						break;
					case 'm':
						f |= Pattern.MULTILINE;
						break;
					case 'i':
						f |= Pattern.CASE_INSENSITIVE;
						break;
					case 'x':
						f |= Pattern.COMMENTS;
						break;
					case 'd':
						f |= Pattern.UNIX_LINES;
						break;
					case 'u':
						f |= Pattern.UNICODE_CASE;
						break;
					default:
						throw new ValueExprEvaluationException(flags);
				}
			}
			Pattern pattern = Pattern.compile(ptn, f);
			boolean result = pattern.matcher(text).find();
			return BooleanLiteralImpl.valueOf(result);
		}

		throw new ValueExprEvaluationException();
	}

	public Value evaluate(LangMatches node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value langTagValue = evaluate(node.getLeftArg(), bindings);
		Value langRangeValue = evaluate(node.getRightArg(), bindings);

		if (QueryEvaluationUtil.isSimpleLiteral(langTagValue)
				&& QueryEvaluationUtil.isSimpleLiteral(langRangeValue))
		{
			String langTag = ((Literal)langTagValue).getLabel();
			String langRange = ((Literal)langRangeValue).getLabel();

			boolean result = false;
			if (langRange.equals("*")) {
				result = langTag.length() > 0;
			}
			else if (langTag.length() == langRange.length()) {
				result = langTag.equalsIgnoreCase(langRange);
			}
			else if (langTag.length() > langRange.length()) {
				// check if the range is a prefix of the tag
				String prefix = langTag.substring(0, langRange.length());
				result = prefix.equalsIgnoreCase(langRange) && langTag.charAt(langRange.length()) == '-';
			}

			return BooleanLiteralImpl.valueOf(result);
		}

		throw new ValueExprEvaluationException();

	}

	/**
	 * Determines whether the two operands match according to the
	 * <code>like</code> operator. The operator is defined as a string comparison
	 * with the possible use of an asterisk (*) at the end and/or the start of
	 * the second operand to indicate substring matching.
	 * 
	 * @return <tt>true</tt> if the operands match according to the <tt>like</tt>
	 *         operator, <tt>false</tt> otherwise.
	 */
	public Value evaluate(Like node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value val = evaluate(node.getArg(), bindings);
		String strVal = null;

		if (val instanceof URI) {
			strVal = ((URI)val).toString();
		}
		else if (val instanceof Literal) {
			strVal = ((Literal)val).getLabel();
		}

		if (strVal == null) {
			throw new ValueExprEvaluationException();
		}

		if (!node.isCaseSensitive()) {
			// Convert strVal to lower case, just like the pattern has been done
			strVal = strVal.toLowerCase();
		}

		int valIndex = 0;
		int prevPatternIndex = -1;
		int patternIndex = node.getOpPattern().indexOf('*');

		if (patternIndex == -1) {
			// No wildcards
			return BooleanLiteralImpl.valueOf(node.getOpPattern().equals(strVal));
		}

		String snippet;

		if (patternIndex > 0) {
			// Pattern does not start with a wildcard, first part must match
			snippet = node.getOpPattern().substring(0, patternIndex);
			if (!strVal.startsWith(snippet)) {
				return BooleanLiteralImpl.FALSE;
			}

			valIndex += snippet.length();
			prevPatternIndex = patternIndex;
			patternIndex = node.getOpPattern().indexOf('*', patternIndex + 1);
		}

		while (patternIndex != -1) {
			// Get snippet between previous wildcard and this wildcard
			snippet = node.getOpPattern().substring(prevPatternIndex + 1, patternIndex);

			// Search for the snippet in the value
			valIndex = strVal.indexOf(snippet, valIndex);
			if (valIndex == -1) {
				return BooleanLiteralImpl.FALSE;
			}

			valIndex += snippet.length();
			prevPatternIndex = patternIndex;
			patternIndex = node.getOpPattern().indexOf('*', patternIndex + 1);
		}

		// Part after last wildcard
		snippet = node.getOpPattern().substring(prevPatternIndex + 1);

		if (snippet.length() > 0) {
			// Pattern does not end with a wildcard.

			// Search last occurence of the snippet.
			valIndex = strVal.indexOf(snippet, valIndex);
			int i;
			while ((i = strVal.indexOf(snippet, valIndex + 1)) != -1) {
				// A later occurence was found.
				valIndex = i;
			}

			if (valIndex == -1) {
				return BooleanLiteralImpl.FALSE;
			}

			valIndex += snippet.length();

			if (valIndex < strVal.length()) {
				// Some characters were not matched
				return BooleanLiteralImpl.FALSE;
			}
		}

		return BooleanLiteralImpl.TRUE;
	}

	/**
	 * Evaluates a function.
	 */
	public Value evaluate(FunctionCall node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Function function = FunctionRegistry.getInstance().get(node.getURI());

		if (function == null) {
			throw new QueryEvaluationException("Unknown function '" + node.getURI() + "'");
		}

		List<ValueExpr> args = node.getArgs();

		Value[] argValues = new Value[args.size()];

		for (int i = 0; i < args.size(); i++) {
			argValues[i] = evaluate(args.get(i), bindings);
		}

		return function.evaluate(tripleSource.getValueFactory(), argValues);
	}

	public Value evaluate(And node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		try {
			Value leftValue = evaluate(node.getLeftArg(), bindings);
			if (QueryEvaluationUtil.getEffectiveBooleanValue(leftValue) == false) {
				// Left argument evaluates to false, we don't need to look any
				// further
				return BooleanLiteralImpl.FALSE;
			}
		}
		catch (ValueExprEvaluationException e) {
			// Failed to evaluate the left argument. Result is 'false' when
			// the right argument evaluates to 'false', failure otherwise.
			Value rightValue = evaluate(node.getRightArg(), bindings);
			if (QueryEvaluationUtil.getEffectiveBooleanValue(rightValue) == false) {
				return BooleanLiteralImpl.FALSE;
			}
			else {
				throw new ValueExprEvaluationException();
			}
		}

		// Left argument evaluated to 'true', result is determined
		// by the evaluation of the right argument.
		Value rightValue = evaluate(node.getRightArg(), bindings);
		return BooleanLiteralImpl.valueOf(QueryEvaluationUtil.getEffectiveBooleanValue(rightValue));
	}

	public Value evaluate(Or node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		try {
			Value leftValue = evaluate(node.getLeftArg(), bindings);
			if (QueryEvaluationUtil.getEffectiveBooleanValue(leftValue) == true) {
				// Left argument evaluates to true, we don't need to look any
				// further
				return BooleanLiteralImpl.TRUE;
			}
		}
		catch (ValueExprEvaluationException e) {
			// Failed to evaluate the left argument. Result is 'true' when
			// the right argument evaluates to 'true', failure otherwise.
			Value rightValue = evaluate(node.getRightArg(), bindings);
			if (QueryEvaluationUtil.getEffectiveBooleanValue(rightValue) == true) {
				return BooleanLiteralImpl.TRUE;
			}
			else {
				throw new ValueExprEvaluationException();
			}
		}

		// Left argument evaluated to 'false', result is determined
		// by the evaluation of the right argument.
		Value rightValue = evaluate(node.getRightArg(), bindings);
		return BooleanLiteralImpl.valueOf(QueryEvaluationUtil.getEffectiveBooleanValue(rightValue));
	}

	public Value evaluate(Not node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		boolean argBoolean = QueryEvaluationUtil.getEffectiveBooleanValue(argValue);
		return BooleanLiteralImpl.valueOf(!argBoolean);
	}

	public Value evaluate(SameTerm node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftVal = evaluate(node.getLeftArg(), bindings);
		Value rightVal = evaluate(node.getRightArg(), bindings);

		return BooleanLiteralImpl.valueOf(leftVal != null && leftVal.equals(rightVal));
	}

	public Value evaluate(Coalesce node, BindingSet bindings)
		throws ValueExprEvaluationException
	{
		Value result = null;

		for (ValueExpr expr : node.getArguments()) {
			try {
				result = evaluate(expr, bindings);

				// return first result that does not produce an error on evaluation.
				break;
			}
			catch (ValueExprEvaluationException e) {
				continue;
			}
			catch (QueryEvaluationException e) {
				continue;
			}
		}

		if (result == null) {
			throw new ValueExprEvaluationException("COALESCE arguments do not evaluate to a value: "
					+ node.getSignature());
		}

		return result;
	}

	public Value evaluate(Compare node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftVal = evaluate(node.getLeftArg(), bindings);
		Value rightVal = evaluate(node.getRightArg(), bindings);

		return BooleanLiteralImpl.valueOf(QueryEvaluationUtil.compare(leftVal, rightVal, node.getOperator()));
	}

	public Value evaluate(MathExpr node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		// Do the math
		Value leftVal = evaluate(node.getLeftArg(), bindings);
		Value rightVal = evaluate(node.getRightArg(), bindings);

		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			return MathUtil.compute((Literal)leftVal, (Literal)rightVal, node.getOperator());
		}

		throw new ValueExprEvaluationException("Both arguments must be numeric literals");
	}

	public Value evaluate(If node, BindingSet bindings)
		throws QueryEvaluationException
	{
		Value result = null;

		boolean conditionIsTrue;

		try {
			Value value = evaluate(node.getCondition(), bindings);
			conditionIsTrue = QueryEvaluationUtil.getEffectiveBooleanValue(value);
		}
		catch (ValueExprEvaluationException e) {
			// in case of type error, if-construction should result in empty
			// binding.
			return null;
		}

		if (conditionIsTrue) {
			result = evaluate(node.getResult(), bindings);
		}
		else {
			result = evaluate(node.getAlternative(), bindings);
		}
		return result;
	}

	public Value evaluate(In node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftValue = evaluate(node.getArg(), bindings);

		// Result is false until a match has been found
		boolean result = false;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), bindings);
		try {
			while (result == false && iter.hasNext()) {
				BindingSet bindingSet = iter.next();

				Value rightValue = bindingSet.getValue(bindingName);

				result = leftValue == null && rightValue == null || leftValue != null
						&& leftValue.equals(rightValue);
			}
		}
		finally {
			iter.close();
		}

		return BooleanLiteralImpl.valueOf(result);
	}

	public Value evaluate(ListMemberOperator node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		List<ValueExpr> args = node.getArguments();
		Value leftValue = evaluate(args.get(0), bindings);

		boolean result = false;
		ValueExprEvaluationException typeError = null;
		for (int i = 1; i < args.size(); i++) {
			ValueExpr arg = args.get(i);
			try {
				Value rightValue = evaluate(arg, bindings);
				result = leftValue == null && rightValue == null;
				if (!result) {
					result = QueryEvaluationUtil.compare(leftValue, rightValue, CompareOp.EQ);
				}
				if (result) {
					break;
				}
			}
			catch (ValueExprEvaluationException caught) {
				typeError = caught;
			}
		}

		if (typeError != null && !result) {
			// cf. SPARQL spec a type error is thrown if the value is not in the
			// list and one of the list members caused a type error in the
			// comparison.
			throw typeError;
		}

		return BooleanLiteralImpl.valueOf(result);
	}

	public Value evaluate(CompareAny node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftValue = evaluate(node.getArg(), bindings);

		// Result is false until a match has been found
		boolean result = false;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), bindings);
		try {
			while (result == false && iter.hasNext()) {
				BindingSet bindingSet = iter.next();

				Value rightValue = bindingSet.getValue(bindingName);

				try {
					result = QueryEvaluationUtil.compare(leftValue, rightValue, node.getOperator());
				}
				catch (ValueExprEvaluationException e) {
					// ignore, maybe next value will match
				}
			}
		}
		finally {
			iter.close();
		}

		return BooleanLiteralImpl.valueOf(result);
	}

	public Value evaluate(CompareAll node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftValue = evaluate(node.getArg(), bindings);

		// Result is true until a mismatch has been found
		boolean result = true;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), bindings);
		try {
			while (result == true && iter.hasNext()) {
				BindingSet bindingSet = iter.next();

				Value rightValue = bindingSet.getValue(bindingName);

				try {
					result = QueryEvaluationUtil.compare(leftValue, rightValue, node.getOperator());
				}
				catch (ValueExprEvaluationException e) {
					// Exception thrown by ValueCompare.isTrue(...)
					result = false;
				}
			}
		}
		finally {
			iter.close();
		}

		return BooleanLiteralImpl.valueOf(result);
	}

	public Value evaluate(Exists node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), bindings);
		try {
			return BooleanLiteralImpl.valueOf(iter.hasNext());
		}
		finally {
			iter.close();
		}
	}

	public boolean isTrue(ValueExpr expr, BindingSet bindings)
		throws QueryEvaluationException
	{
		try {
			Value value = evaluate(expr, bindings);
			return QueryEvaluationUtil.getEffectiveBooleanValue(value);
		}
		catch (ValueExprEvaluationException e) {
			return false;
		}
	}

	protected boolean isReducedOrDistinct(QueryModelNode node) {
		QueryModelNode parent = node.getParentNode();
		if (parent instanceof Slice) {
			return isReducedOrDistinct(parent);
		}
		return parent instanceof Distinct || parent instanceof Reduced;
	}

	/**
	 * Returns the limit of the current variable bindings before any further
	 * projection.
	 */
	protected long getLimit(QueryModelNode node) {
		long offset = 0;
		if (node instanceof Slice) {
			Slice slice = (Slice)node;
			if (slice.hasOffset() && slice.hasLimit()) {
				return slice.getOffset() + slice.getLimit();
			}
			else if (slice.hasLimit()) {
				return slice.getLimit();
			}
			else if (slice.hasOffset()) {
				offset = slice.getOffset();
			}
		}
		QueryModelNode parent = node.getParentNode();
		if (parent instanceof Distinct || parent instanceof Reduced || parent instanceof Slice) {
			long limit = getLimit(parent);
			if (offset > 0L && limit < Long.MAX_VALUE) {
				return offset + limit;
			}
			else {
				return limit;
			}
		}
		return Long.MAX_VALUE;
	}

}
