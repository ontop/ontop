package it.unibz.inf.ontop.model.type.lexical;

import it.unibz.inf.ontop.model.type.DBTermType;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;

import java.util.Optional;

public class DefaultLexicalSpaces {

   public static LexicalSpace getDefaultSpace(DBTermType.Category category) {
        switch (category) {
            case STRING:
                return DefaultLexicalSpaces::everything;
            case INTEGER:
                return DefaultLexicalSpaces::isValidInteger;
            case DECIMAL:
                return DefaultLexicalSpaces::isValidDecimal;
            case FLOAT_DOUBLE:
                return DefaultLexicalSpaces::isValidDouble;
            case BOOLEAN:
            case DATETIME:
            case OTHER:
            default:
                return DefaultLexicalSpaces::unknown;
        }
    }

    public static Optional<Boolean> everything(String lexicalValue) {
        return Optional.of(true);
    }

    public static Optional<Boolean> isValidInteger(String lexicalValue) {
        return Optional.of(XMLDatatypeUtil.isValidInteger(lexicalValue));
    }

    public static Optional<Boolean> isValidDecimal(String lexicalValue) {
        if (XMLDatatypeUtil.isValidDecimal(lexicalValue))
            return Optional.of(true);

        // Not sure
        if (XMLDatatypeUtil.isValidDouble(lexicalValue))
            return Optional.empty();

        // TODO: tolerate scientific notation for numbers beyond the bounds of xsd:double
        return Optional.of(false);
    }

    public static Optional<Boolean> isValidDouble(String lexicalValue) {
        return Optional.of(XMLDatatypeUtil.isValidDouble(lexicalValue));
    }

    public static Optional<Boolean> unknown(String lexicalValue) {
        return Optional.empty();
    }

}
