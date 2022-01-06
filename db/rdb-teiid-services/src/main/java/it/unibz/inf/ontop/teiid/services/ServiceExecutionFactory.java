package it.unibz.inf.ontop.teiid.services;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.core.types.DataTypeManager;
import org.teiid.language.AndOr;
import org.teiid.language.Argument;
import org.teiid.language.BulkCommand;
import org.teiid.language.Call;
import org.teiid.language.ColumnReference;
import org.teiid.language.Command;
import org.teiid.language.Comparison;
import org.teiid.language.Condition;
import org.teiid.language.Delete;
import org.teiid.language.DerivedColumn;
import org.teiid.language.Expression;
import org.teiid.language.ExpressionValueSource;
import org.teiid.language.Insert;
import org.teiid.language.InsertValueSource;
import org.teiid.language.Literal;
import org.teiid.language.NamedTable;
import org.teiid.language.Parameter;
import org.teiid.language.QueryExpression;
import org.teiid.language.Select;
import org.teiid.language.TableReference;
import org.teiid.metadata.Procedure;
import org.teiid.metadata.ProcedureParameter;
import org.teiid.metadata.RuntimeMetadata;
import org.teiid.metadata.Table;
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
import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;

// TODO implement limit
// TODO implement order by
// TODO implement batched updates
// TODO implement bulk update
// TODO implement getCacheDirective

@Translator(name = "service")
public class ServiceExecutionFactory
        extends ExecutionFactory<ServiceConnectionFactory, ServiceConnection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExecutionFactory.class);

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

    // @Override
    // public boolean supportsFullDependentJoins() {
    // return true; // TODO may want to support this too
    // }

    @Override
    public boolean supportsRowLimit() {
        return true;
    }

    @Override
    public boolean supportsRowOffset() {
        return true;
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

        return new ServiceExecution(invoker, ImmutableList.of(inputTuple), null);
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

        // Process the WHERE clause, extracting <column, constant|parameter> pairs
        final Map<String, Expression> inputAttrs = getConstraints(select.getWhere());
        if (inputAttrs == null) {
            return null;
        }

        // Lookup a procedure/service matching the given table, input columns, output columns
        final Procedure proc = getProcedure(table, "select", inputAttrs.keySet(), outputAttrs);
        if (proc == null) {
            return null;
        }
        ServiceInvoker invoker = conn.getFactory().getInvoker(proc);

        // Prepare the service input tuples based on WHERE clause + parameters
        final List<Tuple> inputTuples = getTuples(invoker.getService().getInputSignature(),
                inputAttrs, pars);

        // Return a ResultSetExecution based on a (bulk) service invocation
        return new ServiceExecution(invoker, inputTuples,
                Signature.forAttributes(outputAttrsList));
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

        // TODO
        final ServiceInvoker invoker = conn.getFactory().getInvoker(proc);
        final List<Tuple> inputTuples = getTuples(invoker.getService().getInputSignature(), attrs,
                ((BulkCommand) command).getParameterValues());
        return new ServiceExecution(invoker, inputTuples, null);
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
            final Comparison e = (Comparison) condition;
            if (e.getOperator() != Comparison.Operator.EQ) {
                return false;
            }
            final Expression el = e.getLeftExpression();
            final Expression er = e.getRightExpression();
            final Expression c = el instanceof ColumnReference ? el : er;
            final Expression v = el instanceof ColumnReference ? er : el;
            return c instanceof ColumnReference && (v instanceof Literal || v instanceof Parameter)
                    && constrainedAttributes.put(((ColumnReference) c).getName(), v) == null;

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

    private List<Tuple> getTuples(final Signature signature, final Map<String, Expression> exprs,
            @Nullable final Iterator<? extends List<?>> pars) {

        // Check whether we need to expand parameters in the expressions
        final boolean parametric = exprs.values().stream().anyMatch(e -> e instanceof Parameter);

        if (!parametric) {
            // Parameters missing: return a single tuple based on literal values in expressions
            final Tuple tuple = Tuple.create(signature);
            for (final Entry<String, Expression> e : exprs.entrySet()) {
                tuple.set(e.getKey(), ((Literal) e.getValue()).getValue());
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
                    if (e.getValue() instanceof Literal) {
                        tuple.set(e.getKey(), ((Literal) e.getValue()).getValue());
                    } else {
                        final int index = ((Parameter) e.getValue()).getValueIndex();
                        tuple.set(e.getKey(), parameters.get(index));
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

    private static class ServiceExecution implements ProcedureExecution, UpdateExecution {

        private final ServiceInvoker invoker;

        private final List<Tuple> inputTuples;

        private final Signature outputProjection;

        private List<Iterator<Tuple>> outputTuples;

        public ServiceExecution(final ServiceInvoker invoker, final List<Tuple> inputTuples,
                @Nullable final Signature outputProjection) {
            this.invoker = invoker;
            this.inputTuples = inputTuples;
            this.outputProjection = outputProjection;
        }

        @Override
        public void execute() throws TranslatorException {
            this.outputTuples = Lists.newLinkedList(this.invoker.invokeBatch(this.inputTuples));
        }

        @Override
        public List<?> getOutputParameterValues() throws TranslatorException {
            return ImmutableList.of();
        }

        @Override
        public int[] getUpdateCounts() throws DataNotAvailableException, TranslatorException {
            return new int[] { 1 }; // TODO
        }

        @Override
        public Tuple next() throws TranslatorException, DataNotAvailableException {

            while (!this.outputTuples.isEmpty()) {

                final Iterator<Tuple> iter = this.outputTuples.get(0);
                if (iter.hasNext()) {
                    Tuple t = iter.next();
                    if (this.outputProjection != null) {
                        t = t.project(this.outputProjection);
                    }
                    return t;
                }

                closeQuietly(iter);
                this.outputTuples.remove(0);
            }
            return null;
        }

        @Override
        public void cancel() throws TranslatorException {
        }

        @Override
        public void close() {
            for (final Iterator<Tuple> iter : this.outputTuples) {
                closeQuietly(iter);
            }
            this.outputTuples = null;
        }

        private static void closeQuietly(@Nullable final Object object) {
            if (object instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) object).close();
                } catch (final Throwable ex) {
                    LOGGER.warn("Ignoring error closing " + object.getClass().getSimpleName(), ex);
                }
            }
        }

    }

}
