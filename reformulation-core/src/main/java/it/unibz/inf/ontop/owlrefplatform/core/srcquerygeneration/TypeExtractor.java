package it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration;

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.LITERAL;
import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 * Extracts the TermTypes and the cast types from a set of Datalog rules.
 */
public class TypeExtractor {

    private static final TermType LITERAL_TYPE = DATA_FACTORY.getTermType(LITERAL);

    public static class TypeResults {
        private final ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap;
        private final ImmutableMap<Predicate, ImmutableList<Predicate.COL_TYPE>> castTypeMap;

        private TypeResults(ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
                           ImmutableMap<Predicate, ImmutableList<Predicate.COL_TYPE>> castTypeMap) {
            this.termTypeMap = termTypeMap;
            this.castTypeMap = castTypeMap;
        }

        public ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> getTermTypeMap() {
            return termTypeMap;
        }

        public ImmutableMap<Predicate, ImmutableList<Predicate.COL_TYPE>> getCastTypeMap() {
            return castTypeMap;
        }
    }

    /**
     * Main method.
     *
     * Extracts the TermTypes and the cast types from a set of Datalog rules.
     */
    public static TypeResults extractTypes(Multimap<Predicate, CQIE> ruleIndex, List<Predicate> predicatesInBottomUp) {
        ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap = extractTermTypeMap(ruleIndex.values());

        return new TypeResults(termTypeMap,
                extractCastTypeMap(ruleIndex, predicatesInBottomUp, termTypeMap));
    }

    private static ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> extractTermTypeMap(Collection<CQIE> rules) {
        return rules.stream()
                .collect(ImmutableCollectors.toMap(
                        // Key mapper
                        rule -> rule,
                        // Value mapper
                        rule -> rule.getHead().getTerms().stream()
                                .map(TermTypeInferenceTools::inferType)
                                .collect(ImmutableCollectors.toList())
                ));
    }

    /**
     * Infers cast types for each predicate in the bottom up order
     */
    private static ImmutableMap<Predicate,ImmutableList<Predicate.COL_TYPE>> extractCastTypeMap(
            Multimap<Predicate, CQIE> ruleIndex, List<Predicate> predicatesInBottomUp,
            ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap) {

        // Append-only
        Map<Predicate,ImmutableList<TermType>> mutableCastMap = Maps.newHashMap();

        for (Predicate predicate : predicatesInBottomUp) {
            ImmutableList<TermType> castTypes = inferCastTypes(predicate, ruleIndex.get(predicate), termTypeMap,
                    mutableCastMap);

            mutableCastMap.put(predicate, castTypes);
        }

        /**
         * Convert term types into cast column types
         */
        return mutableCastMap.entrySet().stream()
                .map(e -> new SimpleEntry<>(
                        e.getKey(),
                        e.getValue().stream()
                                .map(TypeExtractor::getCastType)
                                .collect(ImmutableCollectors.toList())
                        ))
                .collect(ImmutableCollectors.toMap());
    }

    /**
     * Infers the cast types for one intensional predicate
     *
     * No side-effect on alreadyKnownCastTypes
     */
    private static ImmutableList<TermType> inferCastTypes(
            Predicate predicate, Collection<CQIE> samePredicateRules,
            ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
            Map<Predicate, ImmutableList<TermType>> alreadyKnownCastTypes) {

        if (samePredicateRules.isEmpty()) {
            ImmutableList.Builder<TermType> defaultTypeBuilder = ImmutableList.builder();
            IntStream.range(0, predicate.getArity())
                    .forEach(i -> defaultTypeBuilder.add(LITERAL_TYPE));
            return defaultTypeBuilder.build();
        }

        ImmutableMultimap<Integer, TermType> collectedProposedCastTypes = collectProposedCastTypes(
                samePredicateRules, termTypeMap, alreadyKnownCastTypes);

        return collectedProposedCastTypes.keySet().stream()
                // 0 to n
                .sorted()
                .map(i -> collectedProposedCastTypes.get(i).stream()
                            .reduce(
                                    // Neutral
                                    null,
                                    (type1, type2) -> type1 == null ? type2 : unifyCastTypes(type1, type2)))
                .map(type -> {
                    if (type != null) {
                        return type;
                    }
                    throw new IllegalStateException("Every argument is expected to have a COL_TYPE");
                })
                .collect(ImmutableCollectors.toList());
    }

    /**
     * Collects the proposed cast types by the definitions of the current predicate
     */
    private static ImmutableMultimap<Integer, TermType> collectProposedCastTypes(
            Collection<CQIE> samePredicateRules, ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
            Map<Predicate, ImmutableList<TermType>> alreadyKnownCastTypes) {

        ImmutableMultimap.Builder<Integer, TermType> indexedCastTypeBuilder = ImmutableMultimap.builder();

        int arity = samePredicateRules.iterator().next().getHead().getTerms().size();

        /**
         * For each rule...
         */
        samePredicateRules.stream()
                .forEach(rule -> {
                    List<Term> headArguments = rule.getHead().getTerms();
                    ImmutableList<Optional<TermType>> termTypes = termTypeMap.get(rule);

                    IntStream.range(0, arity)
                            .forEach(i -> {

                                TermType type = termTypes.get(i)
                                        /**
                                         * If not defined, extracts the cast type of the variable by looking at its defining
                                         * data atom (normally intensional)
                                         */
                                        .orElseGet(() -> getCastTypeFromSubRule(headArguments.get(i),
                                                extractDataAtoms(rule.getBody()).collect(ImmutableCollectors.toList()),
                                                alreadyKnownCastTypes));

                                indexedCastTypeBuilder.put(i, type);
                            });
                });

        return indexedCastTypeBuilder.build();

    }

    private static Predicate.COL_TYPE getCastType(TermType termType) {
        Predicate.COL_TYPE type = termType.getColType();
        switch (type) {
            case OBJECT:
            case BNODE:
            case NULL:
                return LITERAL;
            default:
                return type;
        }
    }

    /**
     * Extracts all the data atoms (without preserving the algebraic structure)
     */
    private static Stream<Function> extractDataAtoms(Collection<? extends Term> atoms) {
        return atoms.stream()
                .filter(t -> t instanceof Function)
                .map(f -> (Function) f)
                .flatMap(a -> {
                    if (a.isDataFunction()) {
                        return Stream.of(a);
                    }
                    else if (a.isAlgebraFunction()) {
                        return extractDataAtoms(a.getTerms());
                    }
                    else {
                        return Stream.empty();
                    }
                });
    }

    /**
     * Extracts the cast type of one projected variable
     * from the body atom that provides it.
     */
    private static TermType getCastTypeFromSubRule(
            Term term,
            ImmutableList<Function> bodyDataAtoms,
            Map<Predicate, ImmutableList<TermType>> alreadyKnownCastTypes) {

        if (term instanceof Variable) {
            Variable variable = (Variable) term;

            for (Function bodyDataAtom : bodyDataAtoms) {

                List<Term> arguments = bodyDataAtom.getTerms();
                for (int i = 0; i < arguments.size(); i++) {
                    /**
                     * Finds the position of the variable in the current body atom
                     */
                    if (arguments.get(i).equals(variable)) {

                        // i is not final...
                        final int index = i;

                        return Optional.ofNullable(alreadyKnownCastTypes.get(bodyDataAtom.getFunctionSymbol()))
                                .map(types -> types.get(index))
                                // TODO: may look for the COL_TYPE of the extensional atom in the DBMetadata
                                .orElse(LITERAL_TYPE);
                    }
                }
            }

            throw new IllegalStateException("Unbounded variable: " + variable);
        }
        else if (term instanceof Expression) {
            Expression expression = (Expression) term;
            ImmutableList<Optional<TermType>> argumentTypes = expression.getTerms().stream()
                    .map(t -> getCastTypeFromSubRule(t, bodyDataAtoms, alreadyKnownCastTypes))
                    .map(Optional::of)
                    .collect(ImmutableCollectors.toList());

            return expression.getOptionalTermType(argumentTypes)
                    .orElseThrow(() -> new IllegalStateException("No type could be inferred for " + term));
        }
        else {
            throw new IllegalStateException("The type should already be for a non-variable - non-expression term (was " + term + ")");
        }
    }

    /**
     * Unifies the input cast types
     *
     * For instance,
     *
     * [INTEGER, DOUBLE] -> DOUBLE
     * [INTEGER, LITERAL] -> LITERAL
     * [INTEGER, INTEGER] -> INTEGER
     *
     */
    private static TermType unifyCastTypes(TermType type1, TermType type2) {
        return type1.getCommonDenominator(type2)
                /**
                 * Every head argument must have a TermType. By default,
                 * we cast it as a LITERAL (VARCHAR)
                 */
                .orElse(LITERAL_TYPE);
    }
}
