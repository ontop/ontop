package it.unibz.inf.ontop.ctables;

import javax.annotation.Nullable;

public class CTablesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CTablesException(@Nullable final String message) {
        super(message);
    }

    public CTablesException(@Nullable final Throwable cause) {
        super(cause);
    }

    public CTablesException(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }

}
