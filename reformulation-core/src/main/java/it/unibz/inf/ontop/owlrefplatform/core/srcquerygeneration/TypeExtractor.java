package it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration;

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.LITERAL;

/**
 * Extracts the TermTypes and the cast types from a set of Datalog rules.
 */
public class TypeExtractor {

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
        Map<Predicate,ImmutableList<Predicate.COL_TYPE>> mutableCastMap = Maps.newHashMap();

        for (Predicate predicate : predicatesInBottomUp) {
            ImmutableList<Predicate.COL_TYPE> castTypes = inferCastTypes(predicate, ruleIndex.get(predicate), termTypeMap,
                    mutableCastMap);

            mutableCastMap.put(predicate, castTypes);
        }

        return ImmutableMap.copyOf(mutableCastMap);
    }

    /**
     * Infers the cast types for one intensional predicate
     *
     * No side-effect on alreadyKnownCastTypes
     */
    private static ImmutableList<Predicate.COL_TYPE> inferCastTypes(
            Predicate predicate, Collection<CQIE> samePredicateRules,
            ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
            Map<Predicate, ImmutableList<Predicate.COL_TYPE>> alreadyKnownCastTypes) {

        if (samePredicateRules.isEmpty()) {
            ImmutableList.Builder<Predicate.COL_TYPE> defaultTypeBuilder = ImmutableList.builder();
            IntStream.range(0, predicate.getArity())
                    .forEach(i -> defaultTypeBuilder.add(LITERAL));
            return defaultTypeBuilder.build();
        }

        ImmutableMultimap<Integer, Predicate.COL_TYPE> collectedProposedCastTypes = collectProposedCastTypes(
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
    private static ImmutableMultimap<Integer, Predicate.COL_TYPE> collectProposedCastTypes(
            Collection<CQIE> samePredicateRules, ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
            Map<Predicate, ImmutableList<Predicate.COL_TYPE>> alreadyKnownCastTypes) {

        ImmutableMultimap.Builder<Integer, Predicate.COL_TYPE> indexedCastTypeBuilder = ImmutableMultimap.builder();

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

                                Predicate.COL_TYPE type = termTypes.get(i)
                                        /**
                                         *  If the term type is defined, converts into a cast type
                                         */
                                        .map(TypeExtractor::getCastType)
                                        /**
                                         * Otherwise, extracts the cast type of the variable by looking at its defining
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
    private static Predicate.COL_TYPE getCastTypeFromSubRule(
            Term term,
            ImmutableList<Function> bodyDataAtoms,
            Map<Predicate, ImmutableList<Predicate.COL_TYPE>> alreadyKnownCastTypes) {

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
                                .orElse(LITERAL);
                    }
                }
            }

            throw new IllegalStateException("Unbounded variable: " + variable);
        }
        else {
            throw new IllegalStateException("The type should already be for a non-variable (was " + term + ")");
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
    private static Predicate.COL_TYPE unifyCastTypes(Predicate.COL_TYPE type1, Predicate.COL_TYPE type2) {
        return TermTypeInferenceTools.getCommonDenominatorType(type1, type2)
                /**
                 * Every head argument must have a COL_TYPE. By default,
                 * we cast it as a LITERAL (VARCHAR)
                 */
                .orElse(LITERAL);
    }
}
