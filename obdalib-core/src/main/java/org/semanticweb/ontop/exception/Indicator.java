package org.semanticweb.ontop.exception;

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

/**
 * A utility class to store error indicators that are linked to the input file.
 * It is used to trace input mistakes when reading a file.
 */
public class Indicator {

    private int lineNumber;
    private int columnNumber;
    private Object hint;
    private int reason;

    /**
     * An error indicator that points to a given line number in the file. Users can
     * encode the reason and store the error hint.
     * 
     * @param lineNumber
     *          The line number that contains the error.
     * @param hint
     *          Part of the file that has the error.
     * @param reason
     *          The error category.
     */
    public Indicator(int lineNumber, Object hint, int reason) {
        this(lineNumber, -1, hint, reason);
    }

    /**
     * An error indicator that points to a given line number in the file. In addition,
     * users can specify the column number if the error is contained in a table list.
     * 
     * @param lineNumber
     *          The line number that contains the error.
     * @param columnNumber
     *          The column order that contains the error.
     * @param hint
     *          Part of the file that has the error.
     * @param reason
     *          The error category.
     */
    public Indicator(int lineNumber, int columnNumber, Object hint, int reason) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.hint = hint;
        this.reason = reason;
    }

    /**
     * Returns the line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the column number.
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Returns the error hint.
     */
    public Object getHint() {
        return hint;
    }

    /**
     * Returns the error category.
     */
    public int getReason() {
        return reason;
    }
}
