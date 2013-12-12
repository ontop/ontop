package it.unibz.krdb.obda.exception;

import java.io.IOException;

public class UnsupportedTagException extends IOException {

	private static final long serialVersionUID = 1L;

	private String tagName;
	
	public UnsupportedTagException(String tagName) {
		super();
		this.tagName = tagName;
	}
	
	@Override
    public String getMessage() {
		return "The tag " + tagName + " is no longer supported. You may safely remove the content from the file.";
	}
}
