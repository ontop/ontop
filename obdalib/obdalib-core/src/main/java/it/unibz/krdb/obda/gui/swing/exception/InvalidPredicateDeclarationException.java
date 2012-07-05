package it.unibz.krdb.obda.gui.swing.exception;

import it.unibz.krdb.obda.model.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * This happens if the declared predicates in the OBDA file do not match to the ones
 * specified in the source ontology.
 */
public class InvalidPredicateDeclarationException extends Exception {

    private static final long serialVersionUID = 1L;

    public static final int UNKNOWN_PREDICATE = 1;

    private List<Indicator> indicators = new ArrayList<Indicator>();

    public InvalidPredicateDeclarationException(Indicator indicator) {
        this.indicators.add(indicator);
    }

    public InvalidPredicateDeclarationException(List<Indicator> indicators) {
        this.indicators.addAll(indicators);
    }
    
    @Override
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        if (!indicators.isEmpty()) {
            sb.append("\n");
            sb.append("The plugin cannot load the OBDA model. (REASON: Unknown predicate declarations)\n");
            sb.append("Please make sure the following predicates match to the source ontology.\n\n");
            for (Indicator indicator : indicators) {
                int lineNumber = indicator.getLineNumber();
                int columnNumber = indicator.getColumnNumber();
                Predicate predicate = (Predicate) indicator.getHint();
                
                switch (indicator.getReason()) {
                case UNKNOWN_PREDICATE:
                    if (predicate.isClass()) {
                        sb.append(String.format("Line %d, Column %d: %s (unknown class)\n", lineNumber, columnNumber, predicate.getName()));
                    } else if (predicate.isObjectProperty()) {
                        sb.append(String.format("Line %d, Column %d: %s (unknown object property)\n", lineNumber, columnNumber, predicate.getName()));
                    } else if (predicate.isDataProperty()) {
                        sb.append(String.format("Line %d, Column %d: %s (unknown data property)\n", lineNumber, columnNumber, predicate.getName()));
                    }
                    break; // case break
                }
            }
        }
        String message = sb.toString();
        return message;
    }
}
