package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCTupleResultSet extends AbstractTupleResultSet implements TupleResultSet {

    private final ImmutableList<Variable> sqlSignature;
    private final ImmutableMap<Variable, DBTermType> sqlTypeMap;
    private final ImmutableSubstitution sparqlVar2Term;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    //Assumption: entries are ordered based on the variable order in each SQL tuple (ImmutableMap is order preserving)

    public JDBCTupleResultSet(ResultSet rs,
                              ImmutableList<Variable> sqlSignature,
                              ImmutableMap<Variable, DBTermType> sqlTypeMap,
                              ConstructionNode constructionNode,
                              DistinctVariableOnlyDataAtom answerAtom,
                              TermFactory termFactory,
                              SubstitutionFactory substitutionFactory) {
        super(rs, answerAtom.getArguments());
        this.sqlSignature = sqlSignature;
        this.sqlTypeMap = sqlTypeMap;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;

        this.sparqlVar2Term = constructionNode.getSubstitution();
//        ImmutableMap<Variable, ImmutableList<Variable>> var2Aliases = computeVar2AliasesMap(inputSub);

//        this.substitution = normalizeSubstitution(inputSub, var2Aliases);
//        this.constantRetriever = new SQLConstantRetriever(
//                substitution,
//                computeVar2IndexMap(var2Aliases, sqlSignature),
//                termFactory,
//                substitutionFactory
//        );
    }

//    private ImmutableMap<Variable, Integer> computeVar2IndexMap(ImmutableMap<Variable, ImmutableList<Variable>> var2Aliases,
//                                                                ImmutableMap<Variable, DBTermType> sqlSignature) {
//        AtomicInteger i = new AtomicInteger(0);
//        ImmutableMap<Variable, Integer> var2int = sqlSignature.stream()
//                .collect(ImmutableCollectors.toMap(
//                        v -> v,
//                        v -> i.getAndIncrement()
//                ));
//        return var2Aliases.entrySet().stream()
//                .flatMap(e -> flattenAliases(var2int.get(e.getKey()), e.getValue()))
//                .collect(ImmutableCollectors.toMap());
//    }
//
//    private Stream<AbstractMap.SimpleImmutableEntry<Variable, Integer>> flattenAliases(Integer i, ImmutableList<Variable> vars) {
//        return vars.stream()
//                .map(v -> new AbstractMap.SimpleImmutableEntry<>(v, i));
//    }
//
//    private ImmutableMap<Variable, ImmutableList<Variable>> computeVar2AliasesMap(ImmutableSubstitution inputSub) {
//        ImmutableMap<Variable, Integer> var2occ = countVariableOccurrences(inputSub);
//        ImmutableList<String> prefixes = computeSafeVarPrefixes(ImmutableList.copyOf(var2occ.keySet()));
//        Iterator<String> it = prefixes.iterator();
//        return var2occ.entrySet().stream()
//                .collect(ImmutableCollectors.toMap(
//                        Map.Entry::getKey,
//                        e -> computeAliases(it.next(), e.getValue())
//                ));
//    }
//
//    private ImmutableList<String> computeSafeVarPrefixes(ImmutableList<Variable> variables) {
//        return variables.stream()
//                .map(v -> computeSafePrefix(v, variables))
//                .collect(ImmutableCollectors.toList());
//    }
//
//    private String computeSafePrefix(Variable inputVar, ImmutableList<Variable> variables) {
//        String safePrefix = inputVar.getName();
//        ImmutableList<String> ext = getExtensions(safePrefix, variables);
//        return (ext.size() == 0) ?
//                safePrefix :
//                expandPrefix(safePrefix, ext);
//    }
//
//    private String expandPrefix(String prefix, ImmutableList<String> ext) {
//        int i = 1;
//        while (hasExt(prefix + i, ext)) {
//            i++;
//        }
//        return prefix + i;
//
//    }
//
//    private boolean hasExt(String s, ImmutableList<String> ext) {
//        return ext.stream()
//                .anyMatch(e -> e.startsWith(s));
//    }
//
//    private ImmutableList<String> getExtensions(String inputPrefix, ImmutableList<Variable> variables) {
//        return variables.stream()
//                .filter(v -> !v.getName().equals(inputPrefix))
//                .filter(v -> v.getName().startsWith(inputPrefix))
//                .map(v -> v.getName().substring(0, inputPrefix.length() - 1))
//                .collect(ImmutableCollectors.toList());
//    }
//
//    private ImmutableList<Variable> computeAliases(String prefix, int occ) {
//        return IntStream.range(0, occ).boxed()
//                .map(i -> termFactory.getVariable(prefix + i))
//                .collect(ImmutableCollectors.toList());
//    }
//
//    private ImmutableSubstitution<ImmutableTerm> normalizeSubstitution(ImmutableSubstitution inputSub, ImmutableMap<Variable, ImmutableList<Variable>> aliases) {
//        ImmutableMap<Variable, Iterator<Variable>> aliasIterators = getAliasIterators(aliases);
//        return substitutionFactory.getSubstitution(
//                ((ImmutableMap<Variable, ImmutableTerm>) inputSub.getImmutableMap()).entrySet().stream()
//                        .collect(ImmutableCollectors.toMap(
//                                Map.Entry::getKey,
//                                e -> normalizeTerm(e.getValue(), aliasIterators)
//                        )));
//    }
//
//    private ImmutableMap<Variable, Iterator<Variable>> getAliasIterators(ImmutableMap<Variable, ImmutableList<Variable>> aliases) {
//        return aliases.entrySet().stream()
//                .collect(ImmutableCollectors.toMap(
//                        Map.Entry::getKey,
//                        e -> e.getValue().iterator()
//                ));
//    }
//
//    private ImmutableTerm normalizeTerm(ImmutableTerm term, ImmutableMap<Variable, Iterator<Variable>> aliasIterators) {
//        return substitutionFactory.getSubstitution(
//                term.getVariableStream().distinct()
//                        .collect(ImmutableCollectors.toMap(
//                                v -> v,
//                                v -> aliasIterators.get(v).next())
//                        )).apply(term);
//    }
//
//    private ImmutableMap<Variable, Integer> countVariableOccurrences(ImmutableSubstitution sub) {
//        Map<Variable, Integer> counts = new HashMap<>();
//        sub.getImmutableMap().values().stream()
//                .flatMap(t -> ((ImmutableTerm) t).getVariableStream())
//                .forEach(v -> {
//                    Integer i = counts.get(v);
//                    counts.put(
//                            (Variable) v,
//                            (i == null) ?
//                                    1 :
//                                    i + 1
//                    );
//                });
//        return ImmutableMap.copyOf(counts);
//    }
//
//    ImmutableSubstitution<DBConstant> retrieveAllConstants(ImmutableList<String> rawValues) {
//        Iterator<DBTermType> typesIt = termTypes.iterator();
//        return substitutionFactory.getSubstitution(
//                var2SQLIndex.entrySet().stream()
//                        .collect(ImmutableCollectors.toMap(
//                                Map.Entry::getKey,
//                                e -> termFactory.getDBConstant(
//                                        rawValues.get(e.getValue()),
//                                        typesIt.next()
//                                ))));
//    }

    @Override
    protected SQLOntopBindingSet readCurrentRow() throws OntopConnectionException {

        //builder (+loop) in order to throw checked exception
        final ImmutableMap.Builder<Variable,DBConstant> builder = ImmutableMap.builder();
        try {
            for (int i = 1; i <= getColumnCount(); i++) {
                Variable var = sqlSignature.get(i-1);
                builder.put(
                        var,
                        termFactory.getDBConstant(
                            rs.getString(i),
                            sqlTypeMap.get(var)
                        ));
            }
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
        return new SQLOntopBindingSet(
                bindingName2Index,
                substitutionFactory.getSubstitution(builder.build()),
                sparqlVar2Term
        );
    }
}
