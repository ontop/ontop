/*
   Copyright (c) 2013 3 Round Stones Inc, Some Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.callimachusproject.io;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.LinkedList;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleUtil;
import org.openrdf.rio.turtle.TurtleWriter;

public class TurtleStreamWriter extends TurtleWriter {
	private final Literal TRUE = ValueFactoryImpl.getInstance().createLiteral(true);
	private final Literal FALSE = ValueFactoryImpl.getInstance().createLiteral(false);
	private final Relativizer base;
	private final Deque<Resource> stack = new LinkedList<Resource>();

	/**
	 * Creates a new TurtleWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the Turtle document to.
	 * @throws URISyntaxException 
	 */
	public TurtleStreamWriter(OutputStream out, String systemId) throws URISyntaxException {
		this(new OutputStreamWriter(out, Charset.forName("UTF-8")), systemId);
	}

	/**
	 * Creates a new TurtleWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the Turtle document to.
	 * @throws URISyntaxException 
	 */
	public TurtleStreamWriter(Writer writer, String systemId) throws URISyntaxException {
		super(writer);
		this.writer.setIndentationString("    ");
		this.base = new Relativizer(systemId);
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		try {
			Resource subj = st.getSubject();
			if (isHanging() && subj.equals(stack.peekLast())) {
				// blank subject
				lastWrittenSubject = subj;
				writer.write("[");
				if (st.getPredicate().equals(RDF.TYPE)) {
					writer.write(" ");
				} else {
					writer.writeEOL();
				}
				writer.increaseIndentation();

				// Write new predicate
				writePredicate(st.getPredicate());
				writer.write(" ");
				lastWrittenPredicate = st.getPredicate();
				writeValue(st.getObject());
			} else if (!subj.equals(lastWrittenSubject) && stack.contains(subj)) {
				closeHangingResource();
				while (!subj.equals(stack.peekLast())) {
					stack.pollLast();
					writer.writeEOL();
					writer.decreaseIndentation();
					writer.write("]");
					lastWrittenSubject = stack.peekLast();
					lastWrittenPredicate = null;
				}
				super.handleStatement(st);
			} else {
				closeHangingResource();
				super.handleStatement(st);
			}
		} catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	protected void writeValue(Value obj) throws IOException {
		if (obj instanceof BNode && !obj.equals(stack.peekLast())) {
			stack.addLast((BNode) obj);
		} else {
			super.writeValue(obj);
		}
	}

	@Override
	protected void closePreviousStatement() throws IOException {
		closeHangingResource();
		while (stack.pollLast() != null) {
			writer.writeEOL();
			writer.decreaseIndentation();
			writer.write("]");
			lastWrittenSubject = stack.peekLast();
			lastWrittenPredicate = null;
		}
		super.closePreviousStatement();
	}

	private void closeHangingResource() throws IOException {
		if (isHanging()) {
			super.writeValue(stack.pollLast());
		}
	}

	private boolean isHanging() {
		return !stack.isEmpty() && lastWrittenSubject != null && !lastWrittenSubject.equals(stack.peekLast());
	}

	@Override
	protected void writeURI(URI uri) throws IOException {
		String uriString = uri.toString();
		
		// Try to find a prefix for the URI's namespace
		String prefix = null;
		
		int splitIdx = TurtleUtil.findURISplitIndex(uriString);
		if (splitIdx > 0) {
			String namespace = uriString.substring(0, splitIdx);
			prefix = namespaceTable.get(namespace);
		}
		
		if (prefix != null) {
			// Namespace is mapped to a prefix; write abbreviated URI
			writer.write(prefix);
			writer.write(":");
			writer.write(uriString.substring(splitIdx));
		}
		else {
			// Write full URI
			writer.write("<");
			writer.write(TurtleUtil.encodeURIString(base.relativize(uriString)));
			writer.write(">");
		}
	}

	@Override
	protected void writeLiteral(Literal lit) throws IOException {
		if (lit.equals(TRUE)) {
			writer.write("true");
		} else if (lit.equals(FALSE)) {
			writer.write("false");
		} else if (XMLSchema.INTEGER.equals(lit.getDatatype())) {
			writer.write(lit.getLabel());
		} else if (XMLSchema.DECIMAL.equals(lit.getDatatype())) {
			writer.write(lit.getLabel());
		} else if (XMLSchema.DOUBLE.equals(lit.getDatatype())) {
			writer.write(lit.getLabel());
		} else if (XMLSchema.STRING.equals(lit.getDatatype())) {
			String label = lit.getLabel();
			
			if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
				// Write label as long string
				writer.write("\"\"\"");
				writer.write(TurtleUtil.encodeLongString(label));
				writer.write("\"\"\"");
			}
			else {
				// Write label as normal string
				writer.write("\"");
				writer.write(TurtleUtil.encodeString(label));
				writer.write("\"");
			}
			
			if (lit.getLanguage() != null) {
				// Append the literal's language
				writer.write("@");
				writer.write(lit.getLanguage());
			}
		} else {
			super.writeLiteral(lit);
		}
	}
	
}