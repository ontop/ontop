package it.unibz.krdb.obda.parser;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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
