package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JDBCSolutionMappingSet extends AbstractTupleResultSet implements TupleResultSet {

    private final SQLConstantRetriever constantRetriever;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final ImmutableSubstitution substitution;

    public JDBCSolutionMappingSet(ResultSet rs, ImmutableList<Variable> SQLSignature,
                                  ConstructionNode constructionNode,
                                  TermFactory termFactory,
                                  SubstitutionFactory substitutionFactory) {
        super(rs, SQLSignature);
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;

        ImmutableSubstitution inputSub = constructionNode.getSubstitution();
        ImmutableMap<Variable, ImmutableList<Variable>> var2Aliases = computeVar2AliasesMap(inputSub);

        this.substitution = normalizeSubstitution(inputSub, var2Aliases);
        this.constantRetriever = new SQLConstantRetriever(
                substitution,
                computeVar2IndexMap(var2Aliases, SQLSignature),
                termFactory,
                substitutionFactory
        );
    }

    private ImmutableMap<Variable, Integer> computeVar2IndexMap(ImmutableMap<Variable, ImmutableList<Variable>> var2Aliases,
                                                                ImmutableList<Variable> sqlSignature) {
        AtomicInteger i = new AtomicInteger(0);
        ImmutableMap<Variable, Integer> var2int = sqlSignature.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        v -> i.getAndIncrement()
                ));
        return var2Aliases.entrySet().stream()
                .flatMap(e -> flattenAliases(var2int.get(e.getKey()), e.getValue()))
                .collect(ImmutableCollectors.toMap());
    }

    private Stream<AbstractMap.SimpleImmutableEntry<Variable, Integer>> flattenAliases(Integer i, ImmutableList<Variable> vars) {
        return vars.stream()
                .map(v -> new AbstractMap.SimpleImmutableEntry<>(v, i));
    }

    private ImmutableMap<Variable, ImmutableList<Variable>> computeVar2AliasesMap(ImmutableSubstitution inputSub) {
        ImmutableMap<Variable, Integer> var2occ = countVariableOccurrences(inputSub);
        ImmutableList<String> prefixes = computeSafeVarPrefixes(ImmutableList.copyOf(var2occ.keySet()));
        Iterator<String> it = prefixes.iterator();
        return var2occ.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> computeAliases(it.next(), e.getValue())
                ));
    }

    private ImmutableList<String> computeSafeVarPrefixes(ImmutableList<Variable> variables) {
        return variables.stream()
                .map(v -> computeSafePrefix(v, variables))
                .collect(ImmutableCollectors.toList());
    }

    private String computeSafePrefix(Variable inputVar, ImmutableList<Variable> variables) {
        String safePrefix = inputVar.getName();
        ImmutableList<String> ext = getExtensions(safePrefix, variables);
        return (ext.size() == 0) ?
                safePrefix :
                expandPrefix(safePrefix, ext);
    }

    private String expandPrefix(String prefix, ImmutableList<String> ext) {
        int i = 1;
        while (hasExt(prefix + i, ext)) {
            i++;
        }
        return prefix + i;

    }

    private boolean hasExt(String s, ImmutableList<String> ext) {
        return ext.stream()
                .anyMatch(e -> e.startsWith(s));
    }

    private ImmutableList<String> getExtensions(String inputPrefix, ImmutableList<Variable> variables) {
        return variables.stream()
                .filter(v -> !v.getName().equals(inputPrefix))
                .filter(v -> v.getName().startsWith(inputPrefix))
                .map(v -> v.getName().substring(0, inputPrefix.length() - 1))
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableList<Variable> computeAliases(String prefix, int occ) {
        return IntStream.range(0, occ).boxed()
                .map(i -> termFactory.getVariable(prefix + i))
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableSubstitution<ImmutableTerm> normalizeSubstitution(ImmutableSubstitution inputSub, ImmutableMap<Variable, ImmutableList<Variable>> aliases) {
        ImmutableMap<Variable, Iterator<Variable>> aliasIterators = getAliasIterators(aliases);
        return substitutionFactory.getSubstitution(
                ((ImmutableMap<Variable, ImmutableTerm>) inputSub.getImmutableMap()).entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> normalizeTerm(e.getValue(), aliasIterators)
                        )));
    }

    private ImmutableMap<Variable, Iterator<Variable>> getAliasIterators(ImmutableMap<Variable, ImmutableList<Variable>> aliases) {
        return aliases.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().iterator()
                ));
    }

    private ImmutableTerm normalizeTerm(ImmutableTerm term, ImmutableMap<Variable, Iterator<Variable>> aliasIterators) {
        return substitutionFactory.getSubstitution(
                term.getVariableStream().distinct()
                        .collect(ImmutableCollectors.toMap(
                                v -> v,
                                v -> aliasIterators.get(v).next())
                        )).apply(term);
    }

    private ImmutableMap<Variable, Integer> countVariableOccurrences(ImmutableSubstitution sub) {
        Map<Variable, Integer> counts = new HashMap<>();
        sub.getImmutableMap().values().stream()
                .flatMap(t -> ((ImmutableTerm) t).getVariableStream())
                .forEach(v -> {
                    Integer i = counts.get(v);
                    counts.put(
                            (Variable) v,
                            (i == null) ?
                                    1 :
                                    i + 1
                    );
                });
        return ImmutableMap.copyOf(counts);
    }

    @Override
    protected SQLOntopBindingSet readCurrentRow() throws OntopConnectionException {

        //builder (+loop) in order to throw checked exception
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        try {
            for (int i = 1; i <= getColumnCount(); i++) {
                builder.add(String.valueOf(rs.getObject(i)));
            }
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
        return new SQLOntopBindingSet(builder.build(), bindingName2Index, constantRetriever, substitution);
    }
}
