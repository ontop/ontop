package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;
import java.util.function.Function;


/**
 * This function mirrors UnaryLatelyTypedFunctionSymbolImpl, but applies to 2 terms rather than one.
 * @see UnaryLatelyTypedFunctionSymbolImpl
 *
 * This function symbol takes "two" lexical terms and a meta RDF term type term as input.
 *
 * Once the concrete RDF term type is determined, looks for its closest DB type
 * and then selects the corresponding DB function symbol to use (let's call it f).
 *
 * Then the lexical term (l) is transformed into a natural one (n).
 *
 * By default, returns f(n) but further transformations can be applied over it.
 *
 */
public class BinaryLatelyTypedFunctionSymbolImpl extends FunctionSymbolImpl {

    private final DBTermType targetType;
    private final Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct;

    protected BinaryLatelyTypedFunctionSymbolImpl(DBTermType dbStringType0, DBTermType dbStringType1,
                                                  MetaRDFTermType metaRDFTermType0, MetaRDFTermType metaRDFTermType1,
                                                  DBTermType targetType,
                                                  Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        super("BINARY_LATELY_TYPE_" + dbFunctionSymbolFct, ImmutableList.of(dbStringType0, dbStringType1,
                metaRDFTermType0, metaRDFTermType1));
        this.targetType = targetType;
        this.dbFunctionSymbolFct = dbFunctionSymbolFct;
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
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        // Mixing of term types allowed e.g. date and datetime for ofn time functions
        ImmutableTerm rdfTypeTerm0 = newTerms.get(2);
        ImmutableTerm rdfTypeTerm1 = newTerms.get(3);
        if (rdfTypeTerm0 instanceof RDFTermTypeConstant && rdfTypeTerm1 instanceof RDFTermTypeConstant) {
            RDFTermType rdfType0 = ((RDFTermTypeConstant) rdfTypeTerm0).getRDFTermType();
            RDFTermType rdfType1 = ((RDFTermTypeConstant) rdfTypeTerm1).getRDFTermType();
            DBTermType dbType0 = rdfType0.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());
            DBTermType dbType1 = rdfType1.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());

            Optional<DBFunctionSymbol> subFunctionSymbol0 = dbFunctionSymbolFct.apply(dbType0);
            Optional<DBFunctionSymbol> subFunctionSymbol1 = dbFunctionSymbolFct.apply(dbType1);

            // Irrelevant datatype -> stops the simplification.
            // The functional term should be eliminated by other means (otherwise the query will fail).
            if (!subFunctionSymbol0.isPresent() || !subFunctionSymbol1.isPresent())
                return termFactory.getImmutableFunctionalTerm(this, newTerms);

            ImmutableFunctionalTerm dbTerm = termFactory.getImmutableFunctionalTerm(
                    // Both DATE and DATETIME will yield the same function symbol
                    subFunctionSymbol0.get(),
                    termFactory.getConversionFromRDFLexical2DB(dbType0, newTerms.get(0), rdfType0),
                    termFactory.getConversionFromRDFLexical2DB(dbType1, newTerms.get(1), rdfType1));

            return transformNaturalDBTerm(dbTerm, dbType0, dbType1, rdfType0, rdfType1, termFactory)
                    .simplify(variableNullability);
        }
        else
            // Tries to lift the DB case of the rdf type term if there is any
            // If the first term is of the correct datatype try to lift the second
            return Optional.of(rdfTypeTerm0 instanceof RDFTermTypeConstant ? rdfTypeTerm1 : rdfTypeTerm0)
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm)t)
                    .filter(t -> t.getFunctionSymbol() instanceof DBIfThenFunctionSymbol)
                    .map(t -> ((DBIfThenFunctionSymbol) t.getFunctionSymbol())
                            .pushDownRegularFunctionalTerm(
                                    termFactory.getImmutableFunctionalTerm(this, newTerms),
                                    newTerms.indexOf(t),
                                    termFactory))
                    .map(t -> t.simplify(variableNullability))
                    // Tries to lift magic numbers
                    .orElseGet(() -> tryToLiftMagicNumbers(newTerms, termFactory, variableNullability, false)
                            .orElseGet(() -> super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability)));
    }

    /**
     * By default, returns the natural DB term
     */
    protected ImmutableTerm transformNaturalDBTerm(ImmutableFunctionalTerm dbTerm,
                                                   DBTermType inputDBType0, DBTermType inputDBType1,
                                                   RDFTermType rdfType0, RDFTermType rdfType1,
                                                   TermFactory termFactory) {
        return dbTerm;
    }
}

