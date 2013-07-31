/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.gui.swing.exception;

import it.unibz.krdb.obda.model.Predicate;

import java.util.ArrayList;
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
public class InvalidMappingException extends Exception {

    private static final long serialVersionUID = 1L;

    public static final int MAPPING_ID_IS_BLANK = 1;
    public static final int TARGET_QUERY_IS_BLANK = 2;
    public static final int SOURCE_QUERY_IS_BLANK = 3;
    public static final int UNKNOWN_PREDICATE_IN_TARGET_QUERY = 4;
    public static final int ERROR_PARSING_TARGET_QUERY = 5;
    
    private List<Indicator> indicators = new ArrayList<Indicator>();

    public InvalidMappingException(Indicator indicator) {
       this.indicators.add(indicator);
    }
    
    public InvalidMappingException(List<Indicator> indicators) {
        this.indicators.addAll(indicators);
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (!indicators.isEmpty()) {
            sb.append("\n");
            sb.append("The plugin cannot load the OBDA model: (REASON: Invalid mappings)\n");
            sb.append("Please fix the following mappings and reload the file.\n\n");
            for (Indicator indicator : indicators) {
                int lineNumber = indicator.getLineNumber();
                String mappingId = "";
                
                switch (indicator.getReason()) {
                case MAPPING_ID_IS_BLANK:
                    sb.append(String.format("Line %d: Invalid input: (mappingId = null)\n\n", lineNumber));
                    break;
                case TARGET_QUERY_IS_BLANK:
                    mappingId = (String) indicator.getHint();
                    if (!mappingId.isEmpty()) {
                        sb.append(String.format("MappingId = '%s'\n", mappingId));
                    }
                    sb.append(String.format("Line %d: Invalid input: (targetQuery = null)\n\n", lineNumber));
                    break;
                case SOURCE_QUERY_IS_BLANK:
                    mappingId = (String) indicator.getHint();
                    if (!mappingId.isEmpty()) {
                        sb.append(String.format("MappingId = '%s'\n", mappingId));
                    }
                    sb.append(String.format("Line %d: Invalid input: (sourceQuery = null)\n\n", lineNumber));
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
                    sb.append(String.format("Line %d: Cannot parse query: '%s'\n\n", lineNumber, targetString));
                    break;
                }
            }
        }
        String message = sb.toString();
        return message;
    }
}
