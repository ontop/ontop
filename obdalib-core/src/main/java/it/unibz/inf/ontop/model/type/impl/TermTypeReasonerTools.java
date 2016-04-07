package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableTable;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeError;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.*;

public class TermTypeReasonerTools {

    private static final ImmutableTable<COL_TYPE, COL_TYPE, COL_TYPE> DATATYPE_UNIFY_TABLE =
            new ImmutableTable.Builder<COL_TYPE, COL_TYPE, COL_TYPE>()
                .put(INTEGER, DOUBLE, DOUBLE)
                .put(INTEGER, DECIMAL, DECIMAL)
                .put(INTEGER, INTEGER, INTEGER)
                //.put(COL_TYPE.INTEGER, COL_TYPE.REAL, COL_TYPE.REAL)
                .put(DECIMAL, DECIMAL, DECIMAL)
                .put(DECIMAL, DOUBLE, DOUBLE)
                //.put(COL_TYPE.DECIMAL, COL_TYPE.REAL, COL_TYPE.REAL)
                .put(DECIMAL, INTEGER, DECIMAL)

                .put(DOUBLE, DECIMAL, DOUBLE)
                .put(DOUBLE, DOUBLE, DOUBLE)
                //.put(COL_TYPE.DOUBLE, COL_TYPE.REAL, COL_TYPE.REAL)
                .put(DOUBLE, INTEGER, DOUBLE)

                //.put(COL_TYPE.REAL, COL_TYPE.DECIMAL, COL_TYPE.REAL)
                //.put(COL_TYPE.REAL, COL_TYPE.DOUBLE, COL_TYPE.REAL)
                //.put(COL_TYPE.REAL, COL_TYPE.REAL, COL_TYPE.REAL)
                //.put(COL_TYPE.REAL, COL_TYPE.INTEGER, COL_TYPE.REAL)

                // TODO: complete

                .build();

    /**
     * TODO: find a better name
     */
    public static Optional<COL_TYPE> getCommonDenominatorType(COL_TYPE type1, COL_TYPE type2) {
        if (type1 == null) {
            return Optional.ofNullable(type2);
        } else if (type2 == null) {
            return Optional.of(type1);
        }
        else {
            return Optional.ofNullable(DATATYPE_UNIFY_TABLE.get(type1, type2));
        }
    }

    /**
     * TODO: simplify this method
     */
    public static Optional<TermType> inferType(Term term) throws TermTypeError {
        if(term instanceof Function){
            Function f = (Function)term;
            Predicate typePred = f.getFunctionSymbol();

            /**
             * TODO: generalize this
             */
            if(f instanceof Expression) {
                return ((Expression) f).getOptionalTermType();
            }
            else if (f.isDataTypeFunction()){
                COL_TYPE colType = f.getFunctionSymbol().getType(0);
                /**
                 * Special case: langString WHERE the language tag is KNOWN
                 */
                if (colType == LITERAL_LANG) {
                    if (f.getTerms().size() != 2) {
                        throw new IllegalStateException("A lang literal function should have two arguments");
                    }
                    Term secondArgument = f.getTerms().get(1);
                    if (secondArgument instanceof Constant) {
                        return Optional.of(new TermTypeImpl(new LanguageTagImpl((
                                ((Constant)secondArgument).getValue()))));
                    }
                }
                return Optional.of(new TermTypeImpl(colType));

            } else if (typePred instanceof URITemplatePredicate) {
                return  Optional.of(new TermTypeImpl(OBJECT));
            } else if (typePred instanceof BNodePredicate){
                return Optional.of(new TermTypeImpl(BNODE));
            }
            else {
                throw new IllegalArgumentException("Unexpected functional term: " + term);
            }
        }
        else if(term instanceof Variable){
            return Optional.empty();
        } else if(term instanceof ValueConstant){
            /**
             * Deals with the ugly definition of the NULL constant.
             * COL_TYPE of NULL should be NULL!
             */
            if (term == OBDAVocabulary.NULL) {
                return Optional.of(new TermTypeImpl(NULL));
            }
            else {
                return Optional.of(new TermTypeImpl(((ValueConstant) term).getType()));
            }
        } else if(term instanceof URIConstant){
            return Optional.of(new TermTypeImpl(OBJECT));
        }
        else {
            throw new IllegalStateException("Unexpected term: " + term);
        }
    }
}
