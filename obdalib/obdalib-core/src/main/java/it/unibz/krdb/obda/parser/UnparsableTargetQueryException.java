package it.unibz.krdb.obda.parser;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UnparsableTargetQueryException extends IOException {

	private static final long serialVersionUID = 918867103710602963L;

	private Map<TargetQueryParser, TargetQueryParserException> exceptions;
	
	public UnparsableTargetQueryException(Map<TargetQueryParser, TargetQueryParserException> exceptions) {
		super("Could not parse target query from OBDA document.");
		this.exceptions = exceptions;
	}
	
	@Override
	public String getMessage() {
		StringBuilder msg = new StringBuilder();
        msg.append("Problem parsing in OBDA document.");
        msg.append("\n");
        msg.append("Could not load OBDA model. Either a suitable parser could not be found, or parsing failed. See parser logs below for explanation.\n");
        msg.append("The following parsers were tried:\n");
        int counter = 1;
        for (TargetQueryParser parser : exceptions.keySet()) {
            msg.append(counter);
            msg.append(") ");
            msg.append(parser.getClass().getSimpleName());
            msg.append("\n");
            counter++;
        }
        msg.append("\nDetailed logs:\n");
        for (TargetQueryParser parser : exceptions.keySet()) {
            Throwable exception = exceptions.get(parser);
            msg.append("--------------------------------------------------------------------------------\n");
            msg.append("Parser: ");
            msg.append(parser.getClass().getSimpleName());
            msg.append("\n");
            msg.append(exception.getMessage());
            msg.append("\n\n");
        }
        return msg.toString();
	}
}
