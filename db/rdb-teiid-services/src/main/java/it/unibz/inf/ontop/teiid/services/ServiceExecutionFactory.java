package it.unibz.inf.ontop.teiid.services;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.core.types.DataTypeManager;
import org.teiid.language.AndOr;
import org.teiid.language.Argument;
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
import org.teiid.language.QueryExpression;
import org.teiid.language.Select;
import org.teiid.language.TableReference;
import org.teiid.metadata.AbstractMetadataRecord;
import org.teiid.metadata.BaseColumn.NullType;
import org.teiid.metadata.FunctionMethod;
import org.teiid.metadata.FunctionParameter;
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

import it.unibz.inf.ontop.teiid.services.util.Attribute;
import it.unibz.inf.ontop.teiid.services.util.Signature;
import it.unibz.inf.ontop.teiid.services.util.Tuple;

@Translator(name = "service")
public class ServiceExecutionFactory
        extends ExecutionFactory<ServiceConnectionFactory, ServiceConnection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExecutionFactory.class);

    public ServiceExecutionFactory() {
        // TODO: supportsSelectExpression, supportsRowLimit/supportsRowOffset
        // TODO: supportsOnlyLiteralComparison, supportsOnlyFormatLiterals
        // TODO: supportsBatchedUpdates, supportsBulkUpdate
        // TODO: getCacheDirective, isThreadBound
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public boolean isSourceRequired() {
        return true;
    }

    @Override
    public boolean isSourceRequiredForCapabilities() {
        return false;
    }

    @Override
    public boolean supportsSelectDistinct() {
        return false;
    }

    @Override
    public boolean supportsInnerJoins() {
        return false;
    }

    @Override
    public boolean supportsOuterJoins() {
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() {
        return false;
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
    public boolean supportsConvert(final int fromType, final int toType) {
        if (fromType == RUNTIME_CODES.STRING && toType == RUNTIME_CODES.CLOB) {
            return true;
        }
        return super.supportsConvert(fromType, toType);
    }

    @Override
    public boolean supportsUpsert() {
        return true;
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

        final Service service = getService(connection.getServiceManager(),
                command.getMetadataObject());

        final Tuple inputTuple = Tuple.create(service.getInputSignature());
        for (final Argument arg : command.getArguments()) {
            final String name = arg.getMetadataObject().getName();
            final Object value = arg.getArgumentValue().getValue();
            inputTuple.set(name, value);
        }

        return new ServiceExecution(service, inputTuple, null);
    }

    @Nullable
    private ResultSetExecution trySelectUsingProcedure(final QueryExpression command,
            final RuntimeMetadata metadata, final ServiceConnection conn)
            throws TranslatorException {

        if (!(command instanceof Select)) {
            return null;
        }
        final Select select = (Select) command;

        final List<TableReference> from = select.getFrom();
        if (from.size() != 1 && !(from.get(0) instanceof NamedTable)) {
            return null;
        }
        final Table table = ((NamedTable) from.get(0)).getMetadataObject();

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

        final Map<String, Object> inputAttrs = getConstraints(select.getWhere());
        if (inputAttrs == null) {
            return null;
        }

        final Procedure proc = getProcedure(table, "select", inputAttrs.keySet(), outputAttrs);
        if (proc == null) {
            return null;
        }

        final Service service = getService(conn.getServiceManager(), proc);
        final Tuple inputTuple = Tuple.create(service.getInputSignature(), inputAttrs);
        final Signature outputSignature = Signature.create(outputAttrsList);
        return new ServiceExecution(service, inputTuple, outputSignature);
    }

    @Nullable
    private UpdateExecution tryUpdateUsingProcedure(final Command command,
            final ServiceConnection conn) throws TranslatorException {

        Table table = null;
        String operation = null;
        Map<String, Object> attrs = null;

        if (command instanceof Insert) {
            final Insert insert = (Insert) command;
            table = insert.getTable().getMetadataObject();
            operation = insert.isUpsert() ? "upsert" : "insert";
            final InsertValueSource vs = insert.getValueSource();
            if (!(vs instanceof ExpressionValueSource)) {
                return null;
            }
            attrs = getConstraints(insert.getColumns(), ((ExpressionValueSource) vs).getValues());

        } else if (command instanceof Delete) {
            final Delete delete = (Delete) command;
            table = delete.getTable().getMetadataObject();
            operation = "delete";
            attrs = getConstraints(delete.getWhere());
        }

        if (operation == null || table == null || attrs == null) {
            return null;
        }

        final Procedure proc = getProcedure(table, operation, attrs.keySet(), ImmutableSet.of());
        if (proc == null) {
            return null;
        }

        final Service service = getService(conn.getServiceManager(), proc);
        final Tuple inputTuple = Tuple.create(service.getInputSignature(), attrs);
        return new ServiceExecution(service, inputTuple, null);
    }

    private Service getService(final ServiceManager manager, final AbstractMetadataRecord metadata)
            throws TranslatorException {

        final String name = metadata.getName();

        Service service = manager.get(name);
        if (service != null) {
            return service;
        }

        final Map<String, Object> properties = ImmutableMap.copyOf(metadata.getProperties());

        final Signature inputSignature, outputSignature;
        if (metadata instanceof Procedure) {
            final Procedure p = (Procedure) metadata;
            inputSignature = Signature.create(Iterables.transform(p.getParameters(),
                    c -> Attribute.create(c.getName(), c.getRuntimeType())));
            outputSignature = Signature.create(Iterables.transform(p.getResultSet().getColumns(),
                    c -> Attribute.create(c.getName(), c.getRuntimeType())));
        } else if (metadata instanceof FunctionMethod) {
            final FunctionMethod f = (FunctionMethod) metadata;
            final FunctionParameter p = f.getOutputParameter();
            inputSignature = Signature.create(Iterables.transform(f.getInputParameters(),
                    c -> Attribute.create(c.getName(), c.getRuntimeType())));
            outputSignature = Signature.create(Attribute
                    .create(MoreObjects.firstNonNull(p.getName(), "result"), p.getRuntimeType()));
        } else {
            throw new Error();
        }

        try {
            service = manager.define(name, inputSignature, outputSignature, properties);
            if (service == null) {
                throw new UnsupportedOperationException();
            }
            LOGGER.debug("Mapped {} to {}", metadata, service);
            return service;

        } catch (final Throwable ex) {
            throw new TranslatorException(ex,
                    "Cannot define service " + name + "(" + Joiner.on(", ").join(inputSignature)
                            + "): (" + Joiner.on(", ").join(outputSignature) + ") with properties "
                            + properties);
        }
    }

    @Nullable
    private Map<String, Object> getConstraints(final Condition condition) {
        final Map<String, Object> constrainedAttributes = Maps.newHashMap();
        final boolean success = getConstraints(condition, constrainedAttributes);
        return success ? constrainedAttributes : null;
    }

    private boolean getConstraints(final Condition condition,
            final Map<String, Object> constrainedAttributes) {

        if (condition instanceof Comparison) {
            final Comparison e = (Comparison) condition;
            if (e.getOperator() != Comparison.Operator.EQ) {
                return false;
            }
            final Expression el = e.getLeftExpression();
            final Expression er = e.getRightExpression();
            final ColumnReference c = (ColumnReference) (el instanceof ColumnReference ? el
                    : er instanceof ColumnReference ? er : null);
            final Literal l = (Literal) (el instanceof Literal ? el
                    : er instanceof Literal ? er : null);
            return c != null && l != null
                    && constrainedAttributes.put(c.getName(), l.getValue()) == null;

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
    private Map<String, Object> getConstraints(final List<ColumnReference> columns,
            final List<Expression> values) {

        if (columns.size() != values.size()) {
            return null;
        }

        final Map<String, Object> constraints = Maps.newHashMap();
        for (int i = 0; i < columns.size(); ++i) {
            final String column = columns.get(i).getName();
            final Expression value = values.get(i);
            if (!(value instanceof Literal)) {
                return null;
            }
            constraints.put(column, ((Literal) value).getValue());
        }

        return constraints;
    }

    private Procedure getProcedure(final Table table, final String operation,
            final Set<String> inputAttrs, @Nullable final Set<String> outputAttrs)
            throws TranslatorException {

        // Differentiate between select vs. update operations
        final boolean isSelect = operation.equals("select");

        // Retrieve all procedures in the same schema of the table
        final Map<String, Procedure> procs = table.getParent().getProcedures();

        // Iterate over the procedure names in comma-separated list of operation option
        outer: for (final String procName : Splitter.on(',').trimResults().omitEmptyStrings()
                .split(Strings.nullToEmpty(table.getProperty(operation)))) {

            // Lookup the procedure for the current procedure name. Fail if undefined
            final Procedure proc = procs.get(procName);
            if (proc == null) {
                throw new TranslatorException("Unknown procedure " + procName + " for operation "
                        + operation + " of table " + table.getName());
            }

            // Verify that all procedure's required parameters can be assigned
            for (final ProcedureParameter param : proc.getParameters()) {
                if (!isOptional(param) && !inputAttrs.contains(param.getName())) {
                    continue outer;
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

            // Return the procedure if all checks have been satisfied
            return proc;
        }

        // Return null if no suitable procedure could be found
        return null;
    }

    private boolean isOptional(final ProcedureParameter param) {
        return param.getNullType() == NullType.Nullable; // TODO
    }

    private static class ServiceExecution implements ProcedureExecution, UpdateExecution {

        private final Service service;

        private final Tuple inputTuple;

        private Iterator<Tuple> outputTuples;

        @Nullable
        private final Signature outputProjection;

        public ServiceExecution(final Service service, final Tuple inputTuple,
                @Nullable final Signature outputProjection) {
            this.service = service;
            this.inputTuple = inputTuple;
            this.outputProjection = outputProjection;
        }

        @Override
        public void execute() throws TranslatorException {
            this.outputTuples = this.service.invoke(this.inputTuple);
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
        public List<?> next() throws TranslatorException, DataNotAvailableException {
            if (!this.outputTuples.hasNext()) {
                return null;
            }
            Tuple t = this.outputTuples.next();
            if (this.outputProjection != null) {
                t = t.project(this.outputProjection);
            }
            return t;
        }

        @Override
        public void cancel() throws TranslatorException {
        }

        @Override
        public void close() {
            if (this.outputTuples instanceof Closeable) {
                try {
                    ((Closeable) this.outputTuples).close();
                } catch (final Throwable ex) {
                    // ignore
                }
            }
            this.outputTuples = null;
        }

    }

}
