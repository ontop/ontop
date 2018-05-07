package it.unibz.inf.ontop.answering.reformulation.generation.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.dbschema.JdbcTypeMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Extracts the TermTypes and the cast types from a set of Datalog rules.
 */
public class TypeExtractor {

    private final TermType literalType;
    private final Relation2Predicate relation2Predicate;
    private final TermTypeInferenceTools termTypeInferenceTools;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private TypeExtractor(Relation2Predicate relation2Predicate, TermTypeInferenceTools termTypeInferenceTools,
                          TypeFactory typeFactory, ImmutabilityTools immutabilityTools, JdbcTypeMapper jdbcTypeMapper) {
        this.relation2Predicate = relation2Predicate;
        this.termTypeInferenceTools = termTypeInferenceTools;
        this.literalType = typeFactory.getAbstractRDFSLiteral();
        this.immutabilityTools = immutabilityTools;
    }


    public static class TypeResults {
        private final ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap;
        private final ImmutableMap<Predicate, ImmutableList<TermType>> castTypeMap;

        private TypeResults(ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
                           ImmutableMap<Predicate, ImmutableList<TermType>> castTypeMap) {
            this.termTypeMap = termTypeMap;
            this.castTypeMap = castTypeMap;
        }

        public ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> getTermTypeMap() {
            return termTypeMap;
        }

        public ImmutableMap<Predicate, ImmutableList<TermType>> getCastTypeMap() {
            return castTypeMap;
        }
    }

    /**
     * Main method.
     *
     * Extracts the TermTypes and the cast types from a set of Datalog rules.
     */
    public TypeResults extractTypes(Multimap<Predicate, CQIE> ruleIndex, List<Predicate> predicatesInBottomUp, DBMetadata metadata)
            throws IncompatibleTermException {
        ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap = extractTermTypeMap(ruleIndex.values());

        return new TypeResults(termTypeMap,
                extractCastTypeMap(ruleIndex, predicatesInBottomUp, termTypeMap, metadata));
    }

    private ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> extractTermTypeMap(Collection<CQIE> rules)
            throws IncompatibleTermException {
        return rules.stream()
                .collect(ImmutableCollectors.toMap(
                        // Key mapper
                        rule -> rule,
                        // Value mapper
                        rule -> rule.getHead().getTerms().stream()
                                .map(immutabilityTools::convertIntoImmutableTerm)
                                .map(termTypeInferenceTools::inferType)
                                .collect(ImmutableCollectors.toList())
                ));
    }

    /**
     * Infers cast types for each predicate in the bottom up order
     */
    private ImmutableMap<Predicate,ImmutableList<TermType>> extractCastTypeMap(
            Multimap<Predicate, CQIE> ruleIndex, List<Predicate> predicatesInBottomUp,
            ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap, DBMetadata metadata) {

        // Append-only
        Map<Predicate,ImmutableList<TermType>> mutableCastMap = Maps.newHashMap();

        for (Predicate predicate : predicatesInBottomUp) {
            ImmutableList<TermType> castTypes = inferCastTypes(predicate, ruleIndex.get(predicate), termTypeMap,
                    mutableCastMap,metadata);

            mutableCastMap.put(predicate, castTypes);
        }

        return ImmutableMap.copyOf(mutableCastMap);
    }

    /**
     * Infers the cast types for one intensional predicate
     *
     * No side-effect on alreadyKnownCastTypes
     */
    private ImmutableList<TermType> inferCastTypes(
            Predicate predicate, Collection<CQIE> samePredicateRules,
            ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
            Map<Predicate, ImmutableList<TermType>> alreadyKnownCastTypes, DBMetadata metadata) {

        if (samePredicateRules.isEmpty()) {

            ImmutableList.Builder<TermType> defaultTypeBuilder = ImmutableList.builder();

            RelationID tableId = relation2Predicate.createRelationFromPredicateName(metadata.getQuotedIDFactory(), predicate);
            Optional<RelationDefinition> td = Optional.ofNullable(metadata.getRelation(tableId));

            IntStream.range(0, predicate.getArity())
                    .forEach(i -> {

                        if(td.isPresent()) {
                            Attribute attribute = td.get().getAttribute(i+1);

                            //get type from metadata
                            defaultTypeBuilder.add(attribute.getTermType());
                        }
                        else{
                            defaultTypeBuilder.add(literalType);
                        }});
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
    private ImmutableMultimap<Integer, TermType> collectProposedCastTypes(
            Collection<CQIE> samePredicateRules, ImmutableMap<CQIE, ImmutableList<Optional<TermType>>> termTypeMap,
            Map<Predicate, ImmutableList<TermType>> alreadyKnownCastTypes) {

        ImmutableMultimap.Builder<Integer, TermType> indexedCastTypeBuilder = ImmutableMultimap.builder();

        int arity = samePredicateRules.iterator().next().getHead().getTerms().size();

        /*
         * For each rule...
         */
        samePredicateRules
                .forEach(rule -> {
                    List<Term> headArguments = rule.getHead().getTerms();
                    ImmutableList<Optional<TermType>> termTypes = termTypeMap.get(rule);

                    IntStream.range(0, arity)
                            .forEach(i -> {

                                TermType type = termTypes.get(i)
                                        /*
                                         * If not defined, extracts the cast type of the variable by looking at its defining
                                         * data atom (normally intensional)
                                         */
                                        .orElseGet(() -> getCastTypeFromSubRule(
                                                immutabilityTools.convertIntoImmutableTerm(headArguments.get(i)),
                                                extractDataAtoms(rule.getBody()).collect(ImmutableCollectors.toList()),
                                                alreadyKnownCastTypes));

                                indexedCastTypeBuilder.put(i, type);
                            });
                });

        return indexedCastTypeBuilder.build();

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
    private TermType getCastTypeFromSubRule(
            ImmutableTerm term,
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
                                .map(types -> types.get(index)).orElseThrow(() -> new IllegalStateException("No type could be inferred for " + term));

                    }
                }
            }

            throw new IllegalStateException("Unbounded variable: " + variable);
        }
        else if (term instanceof ImmutableExpression) {
            ImmutableExpression expression = (ImmutableExpression) term;
            ImmutableList<Optional<TermType>> argumentTypes = expression.getTerms().stream()
                    .map(t -> getCastTypeFromSubRule(t, bodyDataAtoms, alreadyKnownCastTypes))
                    .map(Optional::of)
                    .collect(ImmutableCollectors.toList());

            return expression.getOptionalTermType(argumentTypes)
                    .orElseThrow(() -> new IllegalStateException("No type could be inferred for " + term));
        }
        else if (term instanceof Constant) {
            return ((Constant) term).getType();
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            Predicate functionSymbol = ((ImmutableFunctionalTerm) term).getFunctionSymbol();
            if (functionSymbol instanceof DatatypePredicate)
                return functionSymbol.getExpectedBaseType(0);
        }

        throw new IllegalStateException("Could not determine the type of " + term);
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
     * TODO: refactor
     *
     */
    private static TermType unifyCastTypes(TermType type1, TermType type2) {
        // TODO: the common denominator is not the right mechanism for casting
        return type1.getCommonDenominator(type2);
    }
}
