package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

public class BinaryArithmeticLexicalFunctionSymbolImpl extends FunctionSymbolImpl{

    private final String dbOperationName;
    private final DBTermType dbStringType;
    private final TypeFactory typeFactory;

    protected BinaryArithmeticLexicalFunctionSymbolImpl(String dbOperationName, DBTermType dbStringType,
                                                        MetaRDFTermType metaRDFTermType, TypeFactory typeFactory) {
        super("LEXICAL_BINARY_" + dbOperationName, ImmutableList.of(dbStringType, dbStringType, metaRDFTermType, metaRDFTermType, metaRDFTermType));
        this.dbOperationName = dbOperationName;
        this.dbStringType = dbStringType;
        this.typeFactory = typeFactory;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Could be inferred after simplification
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbStringType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableList<ImmutableTerm> typeTerms = ImmutableList.of(newTerms.get(2), newTerms.get(3), newTerms.get(4));

        if (typeTerms.stream().allMatch(t -> t instanceof RDFTermTypeConstant)) {
            ImmutableList<RDFTermType> rdfTypeTerms = typeTerms.stream()
                    .map(t -> ((RDFTermTypeConstant) t).getRDFTermType())
                    .collect(ImmutableCollectors.toList());

            if (rdfTypeTerms.stream().allMatch(t -> t.isA(typeFactory.getAbstractOntopNumericDatatype()))) {
                return getNumericLexicalTerm(newTerms, termFactory, rdfTypeTerms.get(2));
            } else if (rdfTypeTerms.stream().anyMatch(t -> t.isA(typeFactory.getAbstractOntopTemporalDatatype()))) {
                return getTemporalLexicalTerm(newTerms.subList(0,2), rdfTypeTerms, termFactory);
            } else {
                return termFactory.getNullConstant();
            }
        } else if (typeTerms.stream().anyMatch(t -> t instanceof ImmutableFunctionalTerm)) {
            ImmutableFunctionalTerm functionalTerm = typeTerms.stream()
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm) t)
                    .findAny()
                    .get();

            Optional<ImmutableTerm> liftedTerm = tryPushingDownFunctionalTerm(functionalTerm, newTerms, termFactory, variableNullability)
                    .or(() -> liftRDFTypeFunctionSymbol(functionalTerm, newTerms, termFactory, variableNullability));

            return liftedTerm
                    .orElseGet(() -> super.tryToLiftMagicNumbers(newTerms, termFactory, variableNullability, false)
                    .orElseGet(() -> super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability)));
        } else {
            return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
        }
    }

    private Optional<ImmutableTerm> tryPushingDownFunctionalTerm(ImmutableFunctionalTerm term, ImmutableList<ImmutableTerm> newTerms,
                                                                 TermFactory termFactory, VariableNullability variableNullability) {
        return Optional.of(term)
                .filter(t -> t.getFunctionSymbol() instanceof DBIfThenFunctionSymbol)
                .map(t -> ((DBIfThenFunctionSymbol) t.getFunctionSymbol())
                        .pushDownRegularFunctionalTerm(
                                termFactory.getImmutableFunctionalTerm(this, newTerms),
                                newTerms.indexOf(t),
                                termFactory))
                .map(t -> t.simplify(variableNullability));
    }

    private Optional<ImmutableTerm> liftRDFTypeFunctionSymbol(ImmutableFunctionalTerm term,
                                                ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                VariableNullability variableNullability) {
        if (!(term.getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol)) {
            return Optional.empty();
        }

        RDFTermTypeFunctionSymbol termTypeFunctionSymbol = (RDFTermTypeFunctionSymbol) term.getFunctionSymbol();
        int index = newTerms.indexOf(term);
        return Optional.of(termTypeFunctionSymbol.lift(
                term.getTerms(),
                c -> {
                        ImmutableList<ImmutableTerm> terms = IntStream.range(0, newTerms.size())
                            .mapToObj(i -> i == index ? c : newTerms.get(i))
                        .collect(ImmutableCollectors.toList());
                        return buildTermAfterEvaluation(terms, termFactory, variableNullability);
                        },
                termFactory)
                .simplify(variableNullability));
    }

    private ImmutableTerm getNumericLexicalTerm(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                RDFTermType rdfType) {
        DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());

        ImmutableFunctionalTerm numericTerm = termFactory.getDBBinaryNumericFunctionalTerm(
                dbOperationName, dbType,
                termFactory.getConversionFromRDFLexical2DB(dbType, newTerms.get(0), rdfType),
                termFactory.getConversionFromRDFLexical2DB(dbType, newTerms.get(1), rdfType));

        return termFactory.getConversion2RDFLexical(dbType, numericTerm, rdfType);
    }

    private ImmutableTerm getTemporalLexicalTerm(ImmutableList<ImmutableTerm> lexicalTerms, ImmutableList<RDFTermType> typeTerms,
        TermFactory termFactory) {
            ImmutableList<Integer> durationArgumentsIdx = IntStream.range(0, typeTerms.size() -1 )
                    .filter(i -> typeTerms.get(i).isA(typeFactory.getXsdDurationDatatype()))
                    .boxed()
                    .collect(ImmutableCollectors.toList());

            if (!(durationArgumentsIdx.stream().allMatch(i -> lexicalTerms.get(i) instanceof Constant))) {
                return termFactory.getNullConstant();
            }
            
            ImmutableList<DBTermType> dbTypes = typeTerms.stream()
                    .map(t -> t.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory()))
                    .collect(ImmutableCollectors.toList());

            return termFactory.getDBBinaryTemporalOperationFunctionalTerm(dbOperationName, lexicalTerms.get(0), lexicalTerms.get(1),
                    dbTypes.get(0), dbTypes.get(1),dbTypes.get(2));
    }
}
