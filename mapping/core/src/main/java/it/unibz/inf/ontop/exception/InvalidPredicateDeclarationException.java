package it.unibz.inf.ontop.exception;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.model.predicate.Predicate;

import java.util.List;

/**
 * This happens if the declared predicates in the OBDA file do not match to the ones
 * specified in the source ontology.
 */
public class InvalidPredicateDeclarationException extends MappingException {

    private static final long serialVersionUID = 1L;

    public static final int UNKNOWN_PREDICATE = 1;


    public InvalidPredicateDeclarationException(List<Indicator> indicators) {
        super(buildMessage(indicators));
    }

    private static String buildMessage(List<Indicator> indicators) {
        StringBuilder sb = new StringBuilder();
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
        return sb.toString();
    }
}
