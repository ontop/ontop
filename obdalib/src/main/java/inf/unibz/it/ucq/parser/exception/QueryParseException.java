package inf.unibz.it.ucq.parser.exception;

public class QueryParseException extends Exception {

	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6354414183809682360L;

	public QueryParseException() {
	}

	public QueryParseException(String message) {
		super(message);

	}

	public QueryParseException(Throwable cause) {
		super(cause);

	}

	public QueryParseException(String message, Throwable cause) {
		super(message, cause);

	}

}
