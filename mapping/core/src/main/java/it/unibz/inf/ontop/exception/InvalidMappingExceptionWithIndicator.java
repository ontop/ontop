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

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.util.List;

/**
 * This happens if users insert an invalid mapping, i.e., one or more conditions below are occurred:
 * <ul>
 * <li>The mapping id is empty,</li>
 * <li>The target query is empty,</li>
 * <li>The source query is empty,</li>
 * <li>The predicates in the target query are not declared in the file head,</li>
 * <li>The parser fails to process the target query.</li>
 * </ul>
 */
public class InvalidMappingExceptionWithIndicator extends InvalidMappingException {

    private static final long serialVersionUID = 1L;

    public static final int MAPPING_ID_IS_BLANK = 1;
    public static final int TARGET_QUERY_IS_BLANK = 2;
    public static final int SOURCE_QUERY_IS_BLANK = 3;
    public static final int UNKNOWN_PREDICATE_IN_TARGET_QUERY = 4;
    public static final int ERROR_PARSING_TARGET_QUERY = 5;
    
    public InvalidMappingExceptionWithIndicator(List<Indicator> indicators) {
        super(buildMessage(indicators));
    }

    private static  String buildMessage(List<Indicator> indicators) {
        StringBuilder sb = new StringBuilder();
        if (!indicators.isEmpty()) {
            sb.append("\n");
            sb.append("The syntax of the mapping is invalid (and therefore cannot be processed). Problems: \n\n");
            for (Indicator indicator : indicators) {
                int lineNumber = indicator.getLineNumber();
                String mappingId = "";
                
                switch (indicator.getReason()) {
                case MAPPING_ID_IS_BLANK:
                    sb.append(String.format("Line %d: Mapping ID is missing\n\n", lineNumber));
                    break;
                case TARGET_QUERY_IS_BLANK:
                    mappingId = (String) indicator.getHint();
                    if (!mappingId.isEmpty()) {
                        sb.append(String.format("MappingId = '%s'\n", mappingId));
                    }
                    sb.append(String.format("Line %d: Target is missing\n\n", lineNumber));
                    break;
                case SOURCE_QUERY_IS_BLANK:
                    mappingId = (String) indicator.getHint();
                    if (!mappingId.isEmpty()) {
                        sb.append(String.format("MappingId = '%s'\n", mappingId));
                    }
                    sb.append(String.format("Line %d: Source query is missing\n\n", lineNumber));
                    break;
                case UNKNOWN_PREDICATE_IN_TARGET_QUERY:
                    Object[] hints1 = (Object[]) indicator.getHint();
                    mappingId = (String) hints1[0];
                    if (!mappingId.isEmpty()) {
                        sb.append(String.format("MappingId = '%s'\n", mappingId));
                    }
                    @SuppressWarnings("unchecked")
                    List<Predicate> unknownPredicates = (List<Predicate>) hints1[1];
                    sb.append(String.format("Line %d: %s (unknown predicate)\n\n", lineNumber, unknownPredicates));
                    break;
                case ERROR_PARSING_TARGET_QUERY:
                    String[] hints2 = (String[]) indicator.getHint();
                    mappingId = hints2[0];
                    if (!mappingId.isEmpty()) {
                        sb.append(String.format("MappingId = '%s'\n", mappingId));
                    }
                    String targetString = hints2[1];
                    sb.append(String.format("Line %d: Invalid target: '%s'\n", lineNumber, targetString));
                    String exceptions = hints2[2];
                    sb.append(String.format("Debug information\n%s\n", exceptions));
                    break;
                }
            }
        }
        return sb.toString();
    }
}
