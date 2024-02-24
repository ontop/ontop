package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import org.apache.commons.rdf.api.IRI;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.*;

public class SPARQLCastFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFTermType targetType;
    private final DBTypeFactory dbTypeFactory;
    private final Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private final ImmutableList<String> booleanPattern = ImmutableList.of("true", "false", "t", "f", "1", "0");

    private final Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct;


    public SPARQLCastFunctionSymbolImpl(String functionSymbolName, IRI iriFunctionName, RDFDatatype targetDataType,
                                        TypeFactory typeFactory,
                                        Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        super(functionSymbolName, iriFunctionName, ImmutableList.of(typeFactory.getAbstractRDFTermType()));
        this.targetType = targetDataType;
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
        this.dbFunctionSymbolFct = dbFunctionSymbolFct;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {

        DBTermType targetTypeClosestDBType = targetType.getClosestDBType(dbTypeFactory);


        // CASE 1: Input is typed
        if ((typeTerms.size()>0) && (typeTerms.get(0) instanceof RDFTermTypeConstant)) {
            RDFTermTypeConstant inputRDFType = (RDFTermTypeConstant) typeTerms.get(0);

            // If no cast necessary, return term
            if (inputRDFType.equals(returnedTypeTerm)) {
                return subLexicalTerms.get(0);
            }

            // If constant is not consistent with expected output type, return NULL
            if (!(checkTermConversionConsistency(inputRDFType.getRDFTermType(), termFactory,
                    targetTypeClosestDBType, subLexicalTerms.get(0)))) {
                return termFactory.getNullConstant();
            }

            DBTermType inputDBType = inputRDFType.getRDFTermType().getClosestDBType(dbTypeFactory);

            Optional<DBFunctionSymbol> checkAndConvertFunctionSymbol = dbFunctionSymbolFct.apply(inputDBType);
            ImmutableTerm checkAndConvertTerm = checkAndConvertFunctionSymbol.isPresent()
                    ? termFactory.getImmutableFunctionalTerm(checkAndConvertFunctionSymbol.get(), subLexicalTerms.get(0))
                    : termFactory.getNullConstant();

            // Normalize the term to the expected type
            return termFactory.getConversion2RDFLexical(
                    targetTypeClosestDBType,
                    checkAndConvertTerm,
                    targetType);
        } else {
            // CASE 2: Input is not typed or variable, use STRING as default
            return termFactory.getImmutableFunctionalTerm(
                    dbFunctionSymbolFct.apply(dbTypeFactory.getDBStringType()).get(), subLexicalTerms.get(0));
        }
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(targetType);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    //TODO: Ideally this logic should pushed in the lower-level function symbols, that operates over DB terms.
    // This would maximize the chances for this logic to be used, because we may not have yet constants when reducing to
    // the SPARQL function into DB functions.
    /**
     * Checks for datatype compatibility, otherwise returns NULL
     * CASE 1: Conversion between datatypes not permitted e.g. integer to datetime
     * CASE 2: Conversion of particular values not permitted e.g. string "123a" to integer
     */
    protected boolean checkTermConversionConsistency(RDFTermType rdfTermType, TermFactory termFactory,
                                                     DBTermType targetType2, ImmutableTerm term) {

        DBTermType.Category inputType = rdfTermType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory())
                .getCategory();

        if (targetType2.getNaturalRDFDatatype().isPresent()) {
            switch (targetType2.getCategory()) {
                case DATE:
                    // Case 1: If concrete numeric, no conversion
                    if (rdfTermType instanceof ConcreteNumericRDFDatatype)
                        return false;
                    if (inputType.equals(STRING) && (term instanceof DBConstant)) {
                        try {
                            LocalDate.parse(((DBConstant) term).getValue());
                            return true;
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    }
                    break;
                case DATETIME:
                    if (rdfTermType instanceof ConcreteNumericRDFDatatype)
                        return false;
                    if (inputType.equals(STRING) && (term instanceof DBConstant)) {
                        try {
                            // Drop Z from datetime format
                            String targetDate = ((DBConstant) term).getValue().replace("Z", "");
                            if (targetDate.chars().filter(ch -> ch == ':').count() == 3) {
                                // Drop timezone; check only if valid datetime
                                LocalDateTime.parse(targetDate.substring(0, targetDate.length()-6));
                                // Check if valid timezone
                                // Imperfect solution since some timezone combinations might not exist in the real world
                                return (ImmutableList.of("00", "30", "45").contains(targetDate.substring(targetDate.length() - 2)))
                                        && (ImmutableList.of("-", "+").contains(String.valueOf(targetDate.charAt(targetDate.length() - 6))))
                                        && (Integer.parseInt(targetDate.substring(targetDate.length() - 5, targetDate.length() - 3)) < 15);
                            } else {
                                // Check if valid datetime
                                LocalDateTime.parse(targetDate);
                                return true;
                            }

                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    }
                    break;
                case FLOAT_DOUBLE:
                case DECIMAL:
                case INTEGER:
                    if (inputType.equals(DATE) || inputType.equals(DATETIME))
                        return false;
                    if (inputType.equals(STRING) && (term instanceof DBConstant)) {
                        if (term.isNull()) { return false; }
                        return numericPattern.matcher(((DBConstant) term).getValue()).matches();
                    }
                    break;
                case BOOLEAN:
                    if (inputType.equals(DATE) || inputType.equals(DATETIME))
                        return false;
                    if (inputType.equals(STRING) && (term instanceof DBConstant)) {
                        if (term.isNull()) { return false; }
                        return booleanPattern.contains(((DBConstant) term).getValue());
                    }
                    break;
            }
        }
        return true;
    }

}