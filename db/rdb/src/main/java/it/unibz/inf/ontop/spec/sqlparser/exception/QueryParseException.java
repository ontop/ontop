package it.unibz.inf.ontop.spec.sqlparser.exception;

public class QueryParseException extends Exception {
    private final String sql;
    private final String originalMessage;

    public QueryParseException(String sql, String message, String originalMessage) {
        super(message);
        this.sql = sql;
        this.originalMessage = originalMessage;
    }

    public String getSQL() { return sql; }

    public String getOriginalMessage() { return originalMessage; }
}

