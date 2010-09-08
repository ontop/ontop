package inf.unibz.it.ucq.typing;

public class UnknownXSDTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -417904340563168705L;

	
	public UnknownXSDTypeException() {
	}

	public UnknownXSDTypeException(String message) {
		super(message);

	}

	public UnknownXSDTypeException(Throwable cause) {
		super(cause);

	}

	public UnknownXSDTypeException(String message, Throwable cause) {
		super(message, cause);

	}
}
