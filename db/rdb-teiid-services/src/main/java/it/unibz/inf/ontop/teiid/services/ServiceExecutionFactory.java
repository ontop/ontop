package it.unibz.inf.ontop.teiid.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Booleans;

import org.teiid.core.types.DataTypeManager;
import org.teiid.language.AndOr;
import org.teiid.language.Argument;
import org.teiid.language.BulkCommand;
import org.teiid.language.Call;
import org.teiid.language.ColumnReference;
import org.teiid.language.Command;
import org.teiid.language.Comparison;
import org.teiid.language.Comparison.Operator;
import org.teiid.language.Condition;
import org.teiid.language.Delete;
import org.teiid.language.DerivedColumn;
import org.teiid.language.Expression;
import org.teiid.language.ExpressionValueSource;
import org.teiid.language.Insert;
import org.teiid.language.InsertValueSource;
import org.teiid.language.Limit;
import org.teiid.language.Literal;
import org.teiid.language.NamedTable;
import org.teiid.language.OrderBy;
import org.teiid.language.Parameter;
import org.teiid.language.QueryExpression;
import org.teiid.language.Select;
import org.teiid.language.SortSpecification;
import org.teiid.language.SortSpecification.NullOrdering;
import org.teiid.language.SortSpecification.Ordering;
import org.teiid.language.TableReference;
import org.teiid.metadata.BaseColumn.NullType;
import org.teiid.metadata.Column;
import org.teiid.metadata.Procedure;
import org.teiid.metadata.ProcedureParameter;
import org.teiid.metadata.RuntimeMetadata;
import org.teiid.metadata.Table;
import org.teiid.query.processor.relational.ListNestedSortComparator;
import org.teiid.translator.DataNotAvailableException;
import org.teiid.translator.ExecutionContext;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.ProcedureExecution;
import org.teiid.translator.ResultSetExecution;
import org.teiid.translator.Translator;
import org.teiid.translator.TranslatorException;
import org.teiid.translator.TypeFacility.RUNTIME_CODES;
import org.teiid.translator.UpdateExecution;

import it.unibz.inf.ontop.teiid.services.invokers.ServiceInvoker;
import it.unibz.inf.ontop.teiid.services.model.Attribute;
import it.unibz.inf.ontop.teiid.services.model.Attribute.Role;
import it.unibz.inf.ontop.teiid.services.model.Datatype;
import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.util.Iteration;

// TODO implement batched updates
// TODO implement bulk update
// TODO implement getCacheDirective

@Translator(name = "service")
public class ServiceExecutionFactory
        extends ExecutionFactory<ServiceConnectionFactory, ServiceConnection> {

    private static final String OPERATION_SELECT = "select";

    private static final String OPERATION_INSERT = "insert";

    private static final String OPERATION_UPSERT = "upsert";

    private static final String OPERATION_UPDATE = "update";

    private static final String OPERATION_DELETE = "delete";

    public ServiceExecutionFactory() {
        setImmutable(false); // users may change this
        setSupportsOrderBy(true); // users may disable (supportsOrderByNullOrdering == false)
        setSourceRequired(true);
        setSupportsSelectDistinct(false);
    }

    @Override
    public boolean supportsCompareCriteriaEquals() {
        return true;
    }

    @Override
    public boolean supportsCompareCriteriaOrdered() {
        return true;
    }

    @Override
    public boolean supportsCompareCriteriaOrderedExclusive() {
        return true;
    }

    @Override
    public boolean supportsIsNullCriteria() {
        return true;
    }

    @Override
    public boolean supportsInCriteria() {
        return true;
    }

    @Override
    public boolean supportsDependentJoins() {
        return true; // required for bulk calls
    }

    @Override
    public boolean supportsRowLimit() {
        return true;
    }

    @Override
    public boolean supportsRowOffset() {
        return true;
    }

    @Override
    public boolean supportsOrderByNullOrdering() {
        // Required as Ontop usually specify NULLS FIRST/LAST and without this flag sorting will
        // be always done inside TEIID
        return true;
    }

    @Override
    public NullOrder getDefaultNullOrder() {
        return NullOrder.UNKNOWN; // Depends on the service
    }

    @Override
    public boolean supportsUpsert() {
        return true;
    }

    @Override
    public boolean supportsBatchedUpdates() {
        return true;
    }

    @Override
    public boolean supportsBulkUpdate() {
        return true;
    }

    @Override
    public boolean supportsConvert(final int fromType, final int toType) {
        if (fromType == RUNTIME_CODES.STRING && toType == RUNTIME_CODES.CLOB) {
            return true;
        }
        return super.supportsConvert(fromType, toType);
    }

    @Override
    public TransactionSupport getTransactionSupport() {
        return TransactionSupport.NONE;
    }

    @Override
    public ResultSetExecution createResultSetExecution(final QueryExpression command,
            final ExecutionContext context, final RuntimeMetadata metadata,
            final ServiceConnection conn) throws TranslatorException {

        ResultSetExecution e = trySelectUsingProcedure(command, metadata, conn);
        e = e != null ? e : super.createResultSetExecution(command, context, metadata, conn);
        return e;
    }

    @Override
    public UpdateExecution createUpdateExecution(final Command command,
            final ExecutionContext context, final RuntimeMetadata metadata,
            final ServiceConnection conn) throws TranslatorException {

        UpdateExecution e = tryUpdateUsingProcedure(command, conn);
        e = e != null ? e : super.createUpdateExecution(command, context, metadata, conn);
        return e;
    }

    @Override
    public ProcedureExecution createProcedureExecution(final Call command,
            final ExecutionContext executionContext, final RuntimeMetadata metadata,
            final ServiceConnection connection) throws TranslatorException {

        final ServiceInvoker invoker = connection.getFactory()
                .getInvoker(command.getMetadataObject());

        final Tuple inputTuple = Tuple.create(invoker.getService().getInputSignature());
        for (final Argument arg : command.getArguments()) {
            final String name = arg.getMetadataObject().getName();
            final Object value = arg.getArgumentValue().getValue();
            inputTuple.set(name, value);
        }

        return new ProcedureExecutionImpl(() -> invoker.invoke(inputTuple));
    }

    @Nullable
    private ResultSetExecution trySelectUsingProcedure(final QueryExpression command,
            final RuntimeMetadata metadata, final ServiceConnection conn)
            throws TranslatorException {

        // Require the command to be a SELECT
        if (!(command instanceof Select)) {
            return null;
        }
        final Select select = (Select) command;

        // Process dependent values: if present, there must be at most a list of parameters
        final Map<String, List<? extends List<?>>> depVals = select.getDependentValues();
        if (depVals != null && depVals.size() > 1) {
            return null;
        }
        final Iterator<? extends List<?>> pars = depVals == null || depVals.isEmpty() ? null
                : depVals.values().iterator().next().iterator();

        // Process the FROM clause, checking it comprises a single table
        final List<TableReference> from = select.getFrom();
        if (from.size() != 1 && !(from.get(0) instanceof NamedTable)) {
            return null;
        }
        final Table table = ((NamedTable) from.get(0)).getMetadataObject();

        // Process the SELECT clause, extracting projected output columns (no exprs supported)
        final Set<String> outputAttrs = Sets.newHashSet();
        final List<Attribute> outputAttrsList = Lists.newArrayList();
        for (final DerivedColumn c : select.getDerivedColumns()) {
            final Expression e = c.getExpression();
            if (!(e instanceof ColumnReference)) {
                return null;
            }
            final String a = ((ColumnReference) e).getName();
            if (c.getAlias() != null && !c.getAlias().equals(a)) {
                return null;
            }
            outputAttrs.add(a);
            outputAttrsList.add(Attribute.create(a, DataTypeManager.getDataTypeName(e.getType())));
        }
        final Signature outputSignature = Signature.forAttributes(outputAttrsList);

        // Process the WHERE clause, extracting <column, constant|parameter> pairs
        final Map<String, Expression> inputAttrs = getConstraints(select.getWhere());
        if (inputAttrs == null) {
            return null;
        }

        // Process the ORDER BY clause, delegating it to the service if possible
        OrderBy orderBy = select.getOrderBy();
        if (orderBy != null && orderBy.getSortSpecifications().isEmpty()) {
            orderBy = null; // no need to apply, ignore
        }
        boolean orderByDelegable = false;
        if (orderBy != null && orderBy.getSortSpecifications().size() == 1) {
            final SortSpecification ss = orderBy.getSortSpecifications().get(0);
            if (ss.getExpression() instanceof ColumnReference) {
                final Column column = ((ColumnReference) ss.getExpression()).getMetadataObject();
                if (column.getNullType() == NullType.No_Nulls) {
                    orderByDelegable = true;
                    inputAttrs.put(Role.SORT_BY.getPrefix(),
                            new Literal(column.getName(), Datatype.STRING.getValueClass()));
                    inputAttrs.put(Role.SORT_ASC.getPrefix(), new Literal(
                            ss.getOrdering() == Ordering.ASC, Datatype.BOOLEAN.getValueClass()));
                }
            }
        }

        // Process the LIMIT clause, delegating it to the service if possible
        Limit limit = select.getLimit();
        if (limit != null && limit.getRowOffset() <= 0 && limit.getRowLimit() < 0) {
            limit = null; // no need to apply, ignore
        }
        if (limit != null) {
            inputAttrs.put(Role.OFFSET.getPrefix(),
                    new Literal(limit.getRowOffset(), Datatype.INTEGER.getValueClass()));
            inputAttrs.put(Role.LIMIT.getPrefix(),
                    new Literal(limit.getRowLimit(), Datatype.INTEGER.getValueClass()));
        }

        // Lookup a procedure/service matching the given table, input columns, output columns
        final Procedure proc = getProcedure(table, "select", inputAttrs.keySet(), outputAttrs);
        if (proc == null) {
            return null;
        }
        final ServiceInvoker invoker = conn.getFactory().getInvoker(proc);
        final Signature srvInputSignature = invoker.getService().getInputSignature();
        final Signature srvOutputSignature = invoker.getService().getOutputSignature();
        final Signature srvMergedSignature = Signature
                .join(ImmutableList.of(srvOutputSignature, srvInputSignature));

        // Determine which kind of post-processing FILTER is needed
        final Map<String, Expression> postProcessFilter = Maps.newHashMap();
        for (final Entry<String, Expression> e : inputAttrs.entrySet()) {
            final Role role = Role.decodeAttributeName(e.getKey()).getKey();
            if (!srvInputSignature.has(e.getKey()) && (role == Role.EQUAL
                    || role == Role.MAX_EXCLUSIVE || role == Role.MAX_INCLUSIVE
                    || role == Role.MIN_EXCLUSIVE || role == Role.MIN_INCLUSIVE)) {
                if (!(e.getValue() instanceof Literal)) {
                    return null; // TODO: should handle this case by exploding call
                }
                postProcessFilter.put(e.getKey(), e.getValue());
            }
        }

        // Decide whether post-processing ORDER BY is needed
        final OrderBy postProcessOrderBy = orderBy == null
                || orderByDelegable && srvInputSignature.has(Role.SORT_BY.getPrefix())
                        && srvInputSignature.has(Role.SORT_ASC.getPrefix()) ? null : orderBy;
        if (postProcessOrderBy != null) {
            inputAttrs.remove(Role.SORT_BY.getPrefix()); // don't sort in the service
            inputAttrs.remove(Role.SORT_ASC.getPrefix());
        }

        // Decide whether post-processing LIMIT is needed
        final Limit postProcessLimit = limit == null || postProcessOrderBy == null
                && (limit.getRowLimit() < 0 || srvInputSignature.has(Role.LIMIT.getPrefix()))
                && (limit.getRowOffset() <= 0 || srvInputSignature.has(Role.OFFSET.getPrefix()))
                        ? null
                        : limit;
        if (postProcessLimit != null) {
            inputAttrs.remove(Role.LIMIT.getPrefix()); // don't apply limit in the service
            inputAttrs.remove(Role.OFFSET.getPrefix());
        }

        // Prepare the service input tuples based on WHERE clause + parameters
        final List<Tuple> inputTuples = getTuples(srvInputSignature, inputAttrs, pars);

        // Return an empty result in case there are no invocations to perform
        // (may only happen with bulk calls if no parameters are supplied)
        if (inputTuples.isEmpty()) {
            return new ProcedureExecutionImpl(() -> Collections.emptyIterator());
        }

        // Build a supplier that will invoke the service and perform post-processing when called
        final Supplier<Iterator<Tuple>> outputTuplesSupplier = () -> {

            // Invoke service
            // TODO: double check that there are no issues when combining output tuples with
            // input tuples in presence of batch service calls
            Iterator<Tuple> output = Iteration.transform(invoker.invokeBatch(inputTuples),
                    t -> Tuple.project(srvMergedSignature, t, inputTuples.get(0)));

            // Enforce WHERE filters, if needed
            if (postProcessFilter != null) {
                output = Iteration.filter(output,
                        getFilter(srvMergedSignature, postProcessFilter));
            }

            // Enforce ORDER BY clause, if needed
            if (postProcessOrderBy != null) {
                output = Iteration.sort(output,
                        getComparator(srvMergedSignature, postProcessOrderBy));
            }

            // Enforce LIMIT clause, if needed
            if (postProcessLimit != null) {
                output = Iteration.slice(output, postProcessLimit.getRowOffset(),
                        postProcessLimit.getRowLimit());
            }

            output = Iteration.transform(output, t -> t.project(outputSignature));
            return output;
        };

        // Return a ResultSetExecution based on a (bulk) service invocation
        return new ProcedureExecutionImpl(outputTuplesSupplier);
    }

    @Nullable
    private UpdateExecution tryUpdateUsingProcedure(final Command command,
            final ServiceConnection conn) throws TranslatorException {

        Table table = null;
        String operation = null;
        Map<String, Expression> attrs = null;

        if (command instanceof Insert) {
            final Insert insert = (Insert) command;
            table = insert.getTable().getMetadataObject();
            operation = insert.isUpsert() ? OPERATION_UPSERT : OPERATION_INSERT;
            final InsertValueSource vs = insert.getValueSource();
            if (!(vs instanceof ExpressionValueSource)) {
                return null;
            }
            attrs = getConstraints(insert.getColumns(), ((ExpressionValueSource) vs).getValues());

        } else if (command instanceof Delete) {
            final Delete delete = (Delete) command;
            table = delete.getTable().getMetadataObject();
            operation = OPERATION_DELETE;
            attrs = getConstraints(delete.getWhere());
        }

        if (operation == null || table == null || attrs == null) {
            return null;
        }

        final Procedure proc = getProcedure(table, operation, attrs.keySet(), ImmutableSet.of());
        if (proc == null) {
            return null;
        }

        final ServiceInvoker invoker = conn.getFactory().getInvoker(proc);
        final List<Tuple> inputTuples = getTuples(invoker.getService().getInputSignature(), attrs,
                ((BulkCommand) command).getParameterValues());

        return new UpdateExecutionImpl(() -> {
            final Iterator<Tuple> output = invoker.invokeBatch(inputTuples);
            Iterators.advance(output, Integer.MAX_VALUE);
            Iteration.closeQuietly(output);
            final int[] result = new int[inputTuples.size()];
            Arrays.fill(result, -1);
            return result;
        });
    }

    @Nullable
    private Map<String, Expression> getConstraints(final Condition condition) {
        final Map<String, Expression> constrainedAttributes = Maps.newHashMap();
        final boolean success = getConstraints(condition, constrainedAttributes);
        return success ? constrainedAttributes : null;
    }

    private boolean getConstraints(final Condition condition,
            final Map<String, Expression> constrainedAttributes) {

        if (condition instanceof Comparison) {

            // Match <col> <op> <value> or <value> <op> <col>, excluding "!=" <op>
            final Comparison e = (Comparison) condition;
            final Operator op = e.getOperator();
            final boolean rev = !(e.getLeftExpression() instanceof ColumnReference); // reverse
            final Expression c = rev ? e.getRightExpression() : e.getLeftExpression();
            final Expression v = rev ? e.getLeftExpression() : e.getRightExpression();
            if (!(c instanceof ColumnReference)
                    || !(v instanceof Literal || v instanceof Parameter) //
                    || op == Operator.NE) {
                return false; // don't know how to handle, but Teiid should not send this
            }

            // Identify attribute role (equal, min/max inclusive/exclusive)
            Role role = Role.EQUAL;
            if (op == Operator.GE) {
                role = rev ? Role.MAX_INCLUSIVE : Role.MIN_INCLUSIVE;
            } else if (op == Operator.GT) {
                role = rev ? Role.MAX_EXCLUSIVE : Role.MIN_EXCLUSIVE;
            } else if (op == Operator.LE) {
                role = rev ? Role.MIN_INCLUSIVE : Role.MAX_INCLUSIVE;
            } else if (op == Operator.LT) {
                role = rev ? Role.MIN_EXCLUSIVE : Role.MAX_EXCLUSIVE;
            }

            // Build the attribute name to be supplied to the service
            final String target = ((ColumnReference) c).getName();
            final String name = Role.encodeAttributeName(role, target);

            // Store the constraint, failing on multiple constraint for same attribute
            return constrainedAttributes.put(name, v) == null;

        } else if (condition instanceof AndOr) {
            final AndOr e = (AndOr) condition;
            if (e.getOperator() != AndOr.Operator.AND) {
                return false;
            }
            return getConstraints(e.getLeftCondition(), constrainedAttributes)
                    && getConstraints(e.getRightCondition(), constrainedAttributes);

        } else {
            return condition == null;
        }
    }

    @Nullable
    private Map<String, Expression> getConstraints(final List<ColumnReference> columns,
            final List<Expression> values) {

        if (columns.size() != values.size()) {
            return null;
        }

        final Map<String, Expression> constraints = Maps.newHashMap();
        for (int i = 0; i < columns.size(); ++i) {
            final String column = columns.get(i).getName();
            final Expression value = values.get(i);
            if (!(value instanceof Literal) && !(value instanceof Parameter)) {
                return null;
            }
            constraints.put(column, value);
        }

        return constraints;
    }

    @SuppressWarnings("rawtypes")
    private Predicate<Tuple> getFilter(final Signature signature,
            final Map<String, Expression> constraints) {

        final Comparator<Comparable> comparator = com.google.common.collect.Ordering.natural();

        final List<Predicate<Tuple>> filters = Lists.newArrayList();

        for (final Entry<String, Expression> e : constraints.entrySet()) {

            final Entry<Role, String> roleTarget = Role.decodeAttributeName(e.getKey());
            final Role role = roleTarget.getKey();
            final String target = roleTarget.getValue();
            final int index = signature.nameToIndex(target);

            final Object value = ((Literal) e.getValue()).getValue();

            if (role == Role.EQUAL) {
                filters.add(t -> Objects.equals(t.get(index), value));
            } else if (role == Role.MIN_EXCLUSIVE) {
                filters.add(t -> Objects.compare((Comparable) t.get(index), (Comparable) value,
                        comparator) > 0);
            } else if (role == Role.MIN_INCLUSIVE) {
                filters.add(t -> Objects.compare((Comparable) t.get(index), (Comparable) value,
                        comparator) >= 0);
            } else if (role == Role.MAX_EXCLUSIVE) {
                filters.add(t -> Objects.compare((Comparable) t.get(index), (Comparable) value,
                        comparator) < 0);
            } else if (role == Role.MAX_INCLUSIVE) {
                filters.add(t -> Objects.compare((Comparable) t.get(index), (Comparable) value,
                        comparator) <= 0);
            }
        }

        return t -> {
            for (final Predicate<Tuple> filter : filters) {
                if (!filter.test(t)) {
                    return false;
                }
            }
            return true;
        };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Comparator<Tuple> getComparator(final Signature signature, final OrderBy order) {

        final int size = order.getSortSpecifications().size();
        final int[] parameters = new int[size];
        final boolean[] ascendings = new boolean[size];
        final NullOrdering[] nulls = new NullOrdering[size];

        for (int i = 0; i < size; ++i) {
            final SortSpecification item = order.getSortSpecifications().get(i);
            parameters[i] = signature
                    .nameToIndex(((ColumnReference) item.getExpression()).getName());
            ascendings[i] = !(item.getOrdering() == Ordering.DESC);
            nulls[i] = item.getNullOrdering();
        }

        ListNestedSortComparator<Comparable<? super Comparable<?>>> comparator;
        comparator = new ListNestedSortComparator<>(parameters, Booleans.asList(ascendings));
        comparator.setNullOrdering(Arrays.asList(nulls));

        return (Comparator) comparator;
    }

    private List<Tuple> getTuples(final Signature signature, final Map<String, Expression> exprs,
            @Nullable final Iterator<? extends List<?>> pars) {

        // Check whether we need to expand parameters in the expressions
        final boolean parametric = exprs.values().stream().anyMatch(e -> e instanceof Parameter);

        if (!parametric) {
            // Parameters missing: return a single tuple based on literal values in expressions
            final Tuple tuple = Tuple.create(signature);
            for (final Entry<String, Expression> e : exprs.entrySet()) {
                final int attrIndex = signature.nameToIndex(e.getKey(), -1);
                if (attrIndex >= 0) {
                    tuple.set(attrIndex, ((Literal) e.getValue()).getValue());
                }
            }
            return ImmutableList.of(tuple);

        } else {
            // Parameters present: return a tuple for each parameters' tuple, based on literals'
            // and parameters' values (return no tuples if there are no parameters' tuples)
            final ImmutableList.Builder<Tuple> builder = ImmutableList.builder();
            while (pars != null && pars.hasNext()) {
                final List<?> parameters = pars.next();
                final Tuple tuple = Tuple.create(signature);
                for (final Entry<String, Expression> e : exprs.entrySet()) {
                    final int attrIndex = signature.nameToIndex(e.getKey(), -1);
                    if (attrIndex >= 0) {
                        if (e.getValue() instanceof Literal) {
                            tuple.set(e.getKey(), ((Literal) e.getValue()).getValue());
                        } else {
                            final int index = ((Parameter) e.getValue()).getValueIndex();
                            tuple.set(e.getKey(), parameters.get(index));
                        }
                    }
                }
                builder.add(tuple);
            }
            return builder.build();
        }
    }

    private Procedure getProcedure(final Table table, final String operation,
            final Set<String> inputAttrs, @Nullable final Set<String> outputAttrs)
            throws TranslatorException {

        // Differentiate between select vs. update operations
        final boolean isSelect = operation.equals(OPERATION_SELECT);

        // Retrieve all procedures in the same schema of the table
        final Map<String, Procedure> procs = table.getParent().getProcedures();

        // Iterate over the procedure names in comma-separated list of operation option, selecting
        // the procedure callable given input/output attrs for which the largest number of
        // parameters can be supplied (optimality criterion to solve ambiguity cases)
        Procedure bestProc = null;
        int bestProcNumParams = 0;
        outer: for (final String procName : Splitter.on(',').trimResults().omitEmptyStrings()
                .split(Strings.nullToEmpty(table.getProperty(operation)))) {

            // Lookup the procedure for the current procedure name. Fail if undefined
            final Procedure proc = procs.get(procName);
            if (proc == null) {
                throw new TranslatorException("Unknown procedure " + procName + " for operation "
                        + operation + " of table " + table.getName());
            }

            // Verify that all procedure's required parameters can be assigned
            int numParams = 0;
            for (final ProcedureParameter param : proc.getParameters()) {
                if (inputAttrs.contains(param.getName())) {
                    ++numParams; // param can be assigned based on input tuples
                } else if (!isOptional(param)) {
                    continue outer; // param cannot be assigned and is mandatory
                }
            }

            if (isSelect) {
                // For select operations, verify that expected (unconstrained) output attributes
                // can be found in procedure output
                final Set<String> requiredOutputAttrs = Sets.difference(outputAttrs, inputAttrs);
                final Set<String> outputNames = proc.getResultSet().getColumns().stream()
                        .map(c -> c.getName()).collect(Collectors.toSet());
                if (!outputNames.containsAll(requiredOutputAttrs)) {
                    continue outer;
                }

            } else {
                // For update operations, verify that all constrained attributes can be supplied
                // as input parameters to the procedure
                final Set<String> paramNames = proc.getParameters().stream().map(p -> p.getName())
                        .collect(Collectors.toSet());
                if (!paramNames.containsAll(inputAttrs)) {
                    continue outer;
                }
            }

            // Select the procedure if better than the one (if any) selected before
            if (bestProc == null || numParams > bestProcNumParams) {
                bestProc = proc;
                bestProcNumParams = numParams;
            }
        }

        // Return the best matching procedure, or null if no suitable procedure could be found
        return bestProc;
    }

    private boolean isOptional(final ProcedureParameter param) {
        final boolean hasDefault = param.getDefaultValue() != null;
        return hasDefault; // note: Teiid maps DEFAULT NULL to lowercase string 'null'
    }

    private static class ProcedureExecutionImpl implements ProcedureExecution {

        private final Supplier<Iterator<Tuple>> callback;

        private Iterator<Tuple> iterator;

        public ProcedureExecutionImpl(final Supplier<Iterator<Tuple>> callback) {
            this.callback = callback;
        }

        @Override
        public void execute() throws TranslatorException {
            this.iterator = this.callback.get();
        }

        @Override
        public List<?> getOutputParameterValues() throws TranslatorException {
            return ImmutableList.of();
        }

        @Override
        public Tuple next() throws TranslatorException, DataNotAvailableException {
            return this.iterator.hasNext() ? this.iterator.next() : null;
        }

        @Override
        public void cancel() throws TranslatorException {
        }

        @Override
        public void close() {
            Iteration.closeQuietly(this.iterator);
            this.iterator = null;
        }

    }

    private static class UpdateExecutionImpl implements UpdateExecution {

        private final Supplier<int[]> callback;

        private int[] updateCounts;

        public UpdateExecutionImpl(final Supplier<int[]> callback) {
            this.callback = callback;
        }

        @Override
        public void execute() throws TranslatorException {
            this.updateCounts = this.callback.get();
        }

        @Override
        public int[] getUpdateCounts() throws DataNotAvailableException, TranslatorException {
            return this.updateCounts;
        }

        @Override
        public void cancel() throws TranslatorException {
        }

        @Override
        public void close() {
            this.updateCounts = null;
        }

    }

}
