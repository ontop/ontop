package it.unibz.krdb.obda.exception;

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
