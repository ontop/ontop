package it.unibz.inf.ontop.rdf4j.predefined.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQuery;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.rdf4j.predefined.InvalidBindingSetException;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQuery;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.impl.MapBindingSet;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AbstractPredefinedQuery<Q extends RDF4JInputQuery> implements PredefinedQuery<Q> {

    private final String id;
    protected final PredefinedQueryConfigEntry queryConfig;
    private final Q inputQuery;
    private final ConcurrentMap<String, String> referenceValues;

    public AbstractPredefinedQuery(String id, Q inputQuery, PredefinedQueryConfigEntry queryConfig) {
        this.id = id;
        this.queryConfig = queryConfig;
        this.inputQuery = inputQuery;
        this.referenceValues = new ConcurrentHashMap<>();
    }

    @Override
    public Q getInputQuery() {
        return inputQuery;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<String> getName() {
        return queryConfig.getName();
    }

    @Override
    public Optional<String> getDescription() {
        return queryConfig.getDescription();
    }

    @Override
    public boolean shouldReturn404IfEmpty() {
        return queryConfig.shouldReturn404IfEmpty();
    }

    @Override
    public boolean isResultStreamingEnabled() {
        return queryConfig.isResultStreamingEnabled();
    }

    @Override
    public void validate(ImmutableMap<String, String> bindings) throws InvalidBindingSetException {
        ImmutableMap<String, PredefinedQueryConfigEntry.QueryParameter> parameterConfigMap = queryConfig.getParameters();

        ImmutableList.Builder<String> problemBuilder = ImmutableList.builder();

        for (Map.Entry<String, PredefinedQueryConfigEntry.QueryParameter> parameterEntry : parameterConfigMap.entrySet()) {
            String parameterId = parameterEntry.getKey();
            PredefinedQueryConfigEntry.QueryParameter queryParameter = parameterEntry.getValue();

            if (!bindings.containsKey(parameterId)) {
                if (queryParameter.getRequired())
                    problemBuilder.add("The required parameter " + parameterId + " is missing");
            }
            else
                validate(bindings.get(parameterId), parameterEntry.getValue().getType())
                        .ifPresent(problemBuilder::add);
        }

        ImmutableList<String> problems = problemBuilder.build();
        switch (problems.size()) {
            case 0:
                return;
            case 1:
                throw new InvalidBindingSetException(problems.get(0));
            default:
                throw new InvalidBindingSetException("Invalid parameters: \n - " + String.join("\n - ", problems));
        }
    }

    @Override
    public BindingSet convertBindings(ImmutableMap<String, String> bindings) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();

        ImmutableMap<String, PredefinedQueryConfigEntry.QueryParameter> parameterConfigMap = queryConfig.getParameters();

        MapBindingSet bindingSet = new MapBindingSet();

        for (Map.Entry<String, PredefinedQueryConfigEntry.QueryParameter> parameterEntry : parameterConfigMap.entrySet()) {
            String parameterId = parameterEntry.getKey();

            if (bindings.containsKey(parameterId)) {
                String lexicalValue = bindings.get(parameterId);

                PredefinedQueryConfigEntry.QueryParameter queryParameter = parameterEntry.getValue();
                bindingSet.addBinding(parameterId, convertAndValidate(lexicalValue, queryParameter.getType(), valueFactory));
            }
        }

        return bindingSet;
    }

    @Override
    public ImmutableMap<String, String> replaceWithReferenceValues(ImmutableMap<String, String> bindings) {
        ImmutableMap<String, PredefinedQueryConfigEntry.QueryParameter> parameterMap = queryConfig.getParameters();

        return bindings.entrySet().stream()
                .filter(e -> parameterMap.containsKey(e.getKey()))
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> getReferenceValue(
                                    e.getKey(),
                                    parameterMap.get(e.getKey()))
                                .orElseGet(e::getValue)));
    }

    private Value convertAndValidate(String lexicalValue, PredefinedQueryConfigEntry.QueryParameterType parameterType,
                                     ValueFactory valueFactory) {
        switch (parameterType.getCategory()) {
            case IRI:
                return valueFactory.createIRI(lexicalValue);
            case TYPED_LITERAL:
                return valueFactory.createLiteral(lexicalValue,
                        parameterType.getDatatypeIRI()
                                .orElseThrow(() -> new MinorOntopInternalBugException("A typed literal must have a datatype IRI")));
            default:
                throw new MinorOntopInternalBugException("Not supported category: " + parameterType.getCategory());
        }
    }

    /**
     * Returns a string for the error message if any
     *
     * TODO: implement it
     */
    private Optional<String> validate(String lexicalValue, PredefinedQueryConfigEntry.QueryParameterType parameterType) {
        return Optional.empty();
    }

    /**
     * Returns a reference value only if safe for random generation
     */
    private Optional<String> getReferenceValue(String paramId, PredefinedQueryConfigEntry.QueryParameter queryParameter) {
        if (!queryParameter.isSafeForRandomGeneration())
            return Optional.empty();

        if (!referenceValues.containsKey(paramId)) {
            synchronized (this) {
                return Optional.of(
                        referenceValues.computeIfAbsent(
                                paramId,
                                k -> generateReferenceValue(queryParameter.getType())));
            }
        }
        return Optional.of(referenceValues.get(paramId));
    }

    private static String generateReferenceValue(PredefinedQueryConfigEntry.QueryParameterType paramType) {

        switch (paramType.getCategory()) {
            case IRI:
                // TODO: support templates
                return String.format("http://%s.example.org/fake", UUID.randomUUID().toString());
            case TYPED_LITERAL:
                return generateReferenceLiteralValue(paramType.getDatatypeIRI()
                        .orElseThrow(() -> new MinorOntopInternalBugException("Invalid typed literal")));
            default:
                throw new MinorOntopInternalBugException("Unexpected parameter type: " + paramType.getCategory());
        }
    }

    private static String generateReferenceLiteralValue(IRI datatype) {
        String iriString = datatype.stringValue();

        // TODO: avoid the series of IF
        if (iriString.equals(XSD.STRING.getIRIString())) {
            return UUID.randomUUID().toString();
        }
        else
            throw new RuntimeException("TODO: support random generation of " + datatype);
    }

}
