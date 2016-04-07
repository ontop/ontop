package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeException;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.*;

public class TermTypeInferenceTools {

    /**
     * This table is not "ground truth" and deserve to be discussed (is it good enough or not?)
     */
    private static final ImmutableTable<COL_TYPE, COL_TYPE, COL_TYPE> DATATYPE_DENOMINATORS = generateDatatypeDenominators();

    private static ImmutableTable<COL_TYPE, COL_TYPE, COL_TYPE> generateDatatypeDenominators() {

        ImmutableTable.Builder<COL_TYPE, COL_TYPE, COL_TYPE> tableBuilder = ImmutableTable.<COL_TYPE, COL_TYPE, COL_TYPE>builder()
                // Base COL_TYPES
                .put(LITERAL, LITERAL, LITERAL)
                .put(OBJECT, OBJECT, OBJECT)
                .put(BNODE, BNODE, BNODE)
                .put(NULL, NULL, NULL)
                .put(UNSUPPORTED, UNSUPPORTED, UNSUPPORTED);

        // Child: Parent
        Map<COL_TYPE, COL_TYPE> datatypeHierarchy = ImmutableMap.<COL_TYPE, COL_TYPE>builder()
                .put(LITERAL_LANG, LITERAL)
                .put(STRING, LITERAL)
                .put(DATE, LITERAL)
                .put(DATETIME, LITERAL)
                .put(DATETIME_STAMP, DATETIME)
                .put(TIME, LITERAL)
                .put(YEAR, LITERAL)
                .put(DOUBLE, LITERAL)
                .put(FLOAT, DOUBLE) // Type promotion (https://www.w3.org/TR/xpath20/#dt-type-promotion)
                .put(DECIMAL, FLOAT) // Type promotion
                .put(INTEGER, DECIMAL) // Subtype substitution (https://www.w3.org/TR/xpath20/#dt-subtype-substitution)
                .put(LONG, INTEGER) // Subtype substitution
                .put(INT, LONG) // Subtype substitution
                .put(NON_NEGATIVE_INTEGER, INTEGER) // Subtype substitution
                .put(POSITIVE_INTEGER, NON_NEGATIVE_INTEGER) // Subtype substitution
                .put(NON_POSITIVE_INTEGER, INTEGER) // Subtype substitution
                .put(NEGATIVE_INTEGER, NON_POSITIVE_INTEGER) // Subtype substitution
                .put(UNSIGNED_INT, NON_NEGATIVE_INTEGER) // Subtype substitution
                .build();

        datatypeHierarchy.forEach((child, parent) -> {
            tableBuilder.put(child, child, child);

            // Non-final
            COL_TYPE ancestor = parent;
            // Transitive closure
            while (ancestor != null) {
                tableBuilder.put(child, ancestor, ancestor);
                tableBuilder.put(ancestor, child, ancestor);
                ancestor = datatypeHierarchy.get(ancestor);
            }
        });

        return tableBuilder.build();
    }


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
            return Optional.ofNullable(DATATYPE_DENOMINATORS.get(type1, type2));
        }
    }

    /**
     * TODO: simplify this method
     */
    public static Optional<TermType> inferType(Term term) throws TermTypeException {
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
