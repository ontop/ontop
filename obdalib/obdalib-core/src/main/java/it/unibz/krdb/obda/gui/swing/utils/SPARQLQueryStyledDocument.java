/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.utils.OBDAPreferences;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

// import edu.stanford.smi.protegex.owl.model.OWLModel;

// import com.hp.hpl.jena.query.*;

// import com.hp.hpl.jena.graph.query.Query;

public class SPARQLQueryStyledDocument extends DefaultStyledDocument implements DocumentListener {

	private static final long serialVersionUID = -4291908267565566128L;

	private boolean alreadyColoring = false;
	public StyleContext context = null;
	SPARQLQueryStyledDocument myself = this;
	public SimpleAttributeSet default_style = null;

//	private OBDAPreferences pref = null;

	// private GetDefaultSPARQLPrefixAction _getPrefixAction = null;

	// SPARQLParser parser = null;

	public SPARQLQueryStyledDocument(StyleContext context) {
		super(context);

		// default_style = context.getStyle(StyleContext.DEFAULT_STYLE);
		// StyleConstants.setForeground(default_style, Color.BLACK);
		// StyleConstants.setFontFamily(default_style, "Dialog");
		// StyleConstants.setFontSize(default_style, 12);

		// try {
		// insertString(0, "SELECT $x WHERE { $x  }", default_style);
		// } catch (BadLocationException e) {
		// e.printStackTrace(System.err);
		// }
		addDocumentListener(this);
	}

	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		// recolorQuery();
	}

	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		if (alreadyColoring)
			return;
		recolorQuery();
	}

	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		if (alreadyColoring)
			return;
		recolorQuery();
	}

	private void recolorQuery() {

		if (alreadyColoring)
			return;

		alreadyColoring = true;
		String input = null;
		boolean invalid = false;
		Query query = null;

		try {
			input = getText(0, getLength());
			query = QueryFactory.create(input);

		} catch (Exception e) {
			invalid = true;
			// System.out.println(e.getMessage());
		}

		if ((invalid) || (!(query.isSelectType() || query.isAskType()))) {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						removeDocumentListener(myself);
//						SimpleAttributeSet black = new SimpleAttributeSet();
//						black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);
						
						default_style = new SimpleAttributeSet();
						StyleConstants.setForeground(default_style, Color.BLACK);
						// if(!pref.getUseDefault()){
						StyleConstants.setFontFamily(default_style, "Dialog");
						StyleConstants.setFontSize(default_style, 14);
						
						// if(!pref.getUseDefault()){
						// black.addAttribute(StyleConstants.FontConstants.Family,
						// pref.getFontFamily(MappingManagerPreferences.OBDAPREFS_FONTFAMILY));
						// black.addAttribute(StyleConstants.CharacterConstants.Bold,
						// pref.isBold(MappingManagerPreferences.OBDAPREFS_ISBOLD));
						// black.addAttribute(StyleConstants.FontConstants.FontSize,
						// pref.getFontSize(MappingManagerPreferences.OBDAPREFS_FONTSIZE));
						// setCharacterAttributes(0, getLength(), black, true);
						// }
						setCharacterAttributes(0, getLength(), default_style, true);
						addDocumentListener(myself);

					} catch (Exception e) {
						System.err.print("Unexcpected error: " + e.getMessage());
						e.printStackTrace(System.err);
					}
				}
			});
		} else {
			final Query current_query = query;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						// boolean useDefault = new
						// Boolean(pref.get(OBDAPreferences.USE_DEAFAULT).toString());
						removeDocumentListener(myself);
						String input = getText(0, getLength());

						default_style = new SimpleAttributeSet();
						StyleConstants.setForeground(default_style, Color.BLACK);
						// if(!pref.getUseDefault()){
						StyleConstants.setFontFamily(default_style, "Dialog");
						StyleConstants.setFontSize(default_style, 14);

						SimpleAttributeSet black = new SimpleAttributeSet();
						black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);
						// if(!useDefault){
						// black.addAttribute(StyleConstants.FontConstants.Family,
						// "SansSerif" );
						// }

						SimpleAttributeSet bracket_styles = new SimpleAttributeSet();
						bracket_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLACK);
						// if(!useDefault){
						// bracket_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// Boolean.TRUE);
						// bracket_styles.addAttribute(StyleConstants.FontConstants.Family,
						// "SansSerif" );
						// }

						SimpleAttributeSet functor = new SimpleAttributeSet();

						SimpleAttributeSet parameters = new SimpleAttributeSet();

						SimpleAttributeSet predicates_styles = new SimpleAttributeSet();
						Color c_pred = new Color(41, 119, 167);
						predicates_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, c_pred);
						predicates_styles.addAttribute(StyleConstants.CharacterConstants.Bold, true);
						
						// if(!useDefault){
						// predicates_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// pref.isBold(MappingManagerPreferences.OBDAPREFS_ISBOLD));
						// predicates_styles.addAttribute(StyleConstants.FontConstants.Family,
						// pref.getFontFamily(MappingManagerPreferences.OBDAPREFS_FONTFAMILY));
						// predicates_styles.addAttribute(StyleConstants.FontConstants.FontSize,
						// pref.getFontSize(MappingManagerPreferences.OBDAPREFS_FONTSIZE)
						// );
						// }

						SimpleAttributeSet classes_styles = new SimpleAttributeSet();
						Color c_clazz = new Color(199, 155, 41);
						classes_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, c_clazz);
						classes_styles.addAttribute(StyleConstants.CharacterConstants.Bold, true);
						// if(!useDefault){
						// classes_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// pref.isBold(MappingManagerPreferences.OBDAPREFS_ISBOLD));
						// classes_styles.addAttribute(StyleConstants.FontConstants.Family,
						// pref.getFontFamily(MappingManagerPreferences.OBDAPREFS_FONTFAMILY));
						// classes_styles.addAttribute(StyleConstants.FontConstants.FontSize,
						// pref.getFontSize(MappingManagerPreferences.OBDAPREFS_FONTSIZE)
						// );
						// }

						SimpleAttributeSet variables_styles = new SimpleAttributeSet();

						// Reseting styes

						setCharacterAttributes(0, SPARQLQueryStyledDocument.this.getLength(), default_style, true);

						int pos = input.indexOf("{", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, 1, bracket_styles, false);
							pos = input.indexOf("{", pos + 1);
						}
						pos = input.indexOf("}", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, 1, bracket_styles, false);
							pos = input.indexOf("}", pos + 1);
						}
						pos = input.indexOf(".", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, 1, black, false);
							pos = input.indexOf(".", pos + 1);
						}
						pos = input.indexOf("*", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, 1, variables_styles, false);
							pos = input.indexOf(".", pos + 1);
						}

						pos = input.indexOf("rdf:type", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, "rdf:type".length(), black, false);
							pos = input.indexOf("rdf:type", pos + 1);
						}

						List sel_vars = current_query.getResultVars();
						List sel_uris = current_query.getResultURIs();
						ArrayList<Node_URI> predicates = new ArrayList<Node_URI>();
						ArrayList<String> concepts = new ArrayList<String>();
						ArrayList<Node_Literal> constants = new ArrayList<Node_Literal>();

						com.hp.hpl.jena.sparql.syntax.Element pattern = current_query.getQueryPattern();
						ElementGroup group = (ElementGroup) pattern;
						List list = group.getElements();

						for (int k = 0; k < list.size(); k++) {

							ElementGroup current_group = null;
							ElementTriplesBlock triplesBock = null;
							if (list.get(k) instanceof ElementGroup) {
								current_group = (ElementGroup) list.get(k);
								triplesBock = (ElementTriplesBlock) current_group.getElements().get(0);
							} else if (list.get(k) instanceof ElementTriplesBlock) {
								// current_group = (ElementGroup) list.get(k);
								triplesBock = (ElementTriplesBlock) list.get(0);
							} else if (list.get(k) instanceof ElementFilter) {
								continue;
							}

							BasicPattern triples = triplesBock.getPattern();
							for (int i = 0; i < triples.size(); i++) {
								Triple triple = triples.get(i);
								Node o = triple.getObject();
								Node p = triple.getPredicate();
								Node s = triple.getSubject();

								if (p instanceof Node_URI) {
									Node_URI predicate = (Node_URI) p;
									if (predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
										if (o instanceof Node_Literal) {
											Node_Literal lit = (Node_Literal) o;
											concepts.add(lit.getLiteralValue().toString());
										} else if (o instanceof Node_URI) {
											Node_URI uri = (Node_URI) o;
											String localname = uri.getLocalName();
											concepts.add(localname);
										} else {
											// attributes.addAttribute(StyleConstants.CharacterConstants.Foreground,
											// Color.RED);
											return;
										}
									} else {
										predicates.add((Node_URI) p);
										if (o instanceof Node_Literal) {
											constants.add((Node_Literal) o);
										} else if (o instanceof Var) {
											sel_vars.add(((Var) o).getName());
										}
									}
									if (s instanceof Node_Literal) {
										constants.add((Node_Literal) s);
									} else if (s instanceof Var) {
										sel_vars.add(((Var) s).getName());
									}
								}
							}
						}

						Iterator var_it = sel_vars.iterator();
						while (var_it.hasNext()) {
							String var = (String) var_it.next();
							int x = input.indexOf(var, 0);
							while (x != -1) {
								if ((input.charAt(x - 1) == '?') || (input.charAt(x - 1) == '$')) {
									setCharacterAttributes(x - 1, var.length() + 1, variables_styles, false);
								}
								x = input.indexOf(var.toString(), x + 1);
							}
						}

						Iterator<Node_URI> pred_it = predicates.iterator();
						while (pred_it.hasNext()) {
							Node_URI pred = pred_it.next();
							int x = input.indexOf(pred.getLocalName().toString(), 0);
							while (x != -1) {
								if (input.charAt(x - 1) == ':') {
									setCharacterAttributes(x, pred.getLocalName().toString().length(), predicates_styles, false);
									int b = 1;
									char ch = input.charAt(x - b);
									while (ch != ' ') {
										setCharacterAttributes(x - b, pred.getLocalName().toString().length() + b, predicates_styles, false);
										b++;
										ch = input.charAt(x - b);
									}
								}
								x = input.indexOf(pred.getLocalName().toString(), x + 1);
							}
						}
						//
						Iterator<Node_Literal> const_it = constants.iterator();
						while (const_it.hasNext()) {
							Node_Literal constant = const_it.next();
							int x = input.indexOf(constant.getLiteralValue().toString(), 0);
							while (x != -1) {
								if ((input.charAt(x - 1) == '\'') || (input.charAt(x - 1) == '\"')) {
									setCharacterAttributes(x, constant.getLiteralValue().toString().length(), black, false);
								}
								x = input.indexOf(constant.getLiteralValue().toString(), x + 1);
							}
						}
						//
						Iterator<String> classes_it = concepts.iterator();
						while (classes_it.hasNext()) {
							String concept = classes_it.next();
							int x = input.indexOf(concept, 0);
							while (x != -1) {
								try {
									if ((input.charAt(x - 1) == '\'') || (input.charAt(x - 1) == '\"')) {
										setCharacterAttributes(x, concept.length(), classes_styles, false);
									} else if (input.charAt(x - 1) == ':') {
										setCharacterAttributes(x, concept.length(), classes_styles, false);
										int b = 1;
										char ch = input.charAt(x - b);
										while (ch != ' ') {
											setCharacterAttributes(x - b, concept.length() + b, classes_styles, false);
											b++;
											ch = input.charAt(x - b);
										}
									}
									x = input.indexOf(concept, x + 1);
								} catch (StringIndexOutOfBoundsException e) {
									throw e;
									// return;
								}
							}
						}

						pos = input.indexOf("rdf:type", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, "rdf:type".length(), black, false);
							pos = input.indexOf("rdf:type", pos + 1);
						}
						//
						// pos = input.indexOf("SELECT", 0);
						// setCharacterAttributes(pos, "SELECT".length(),
						// keyword_styles, false);
						// pos = input.indexOf("WHERE", 0);
						// setCharacterAttributes(pos, "WHERE".length(),
						// keyword_styles, false);

						addDocumentListener(myself);

					} catch (Exception e) {
						addDocumentListener(myself);
						alreadyColoring = false;
					}
				}
			});
		}
		alreadyColoring = false;
	}

	/**
	 * Creates the SPARQL PREFIX and BASE declarations for a given OWLModel.
	 * This string can be used to start a new query.
	 * 
	 * @param owlModel
	 *            the OWLModel to get the prefixes of
	 * @return a prefix declaration string
	 */
	// private String createPrefixDeclarations(OWLModel owlModel) {
	// String queryString = "";
	// String defaultNamespace =
	// owlModel.getNamespaceManager().getDefaultNamespace();
	// if (defaultNamespace.endsWith("#")) {
	// queryString += "BASE <" + defaultNamespace.substring(0,
	// defaultNamespace.length() - 1) + ">\n";
	// } else {
	// queryString += "BASE <" + defaultNamespace + ">\n";
	// }
	// queryString += "PREFIX : <" + defaultNamespace + ">\n";
	// Iterator prefixes =
	// owlModel.getNamespaceManager().getPrefixes().iterator();
	// while (prefixes.hasNext()) {
	// String prefix = (String) prefixes.next();
	// String namespace =
	// owlModel.getNamespaceManager().getNamespaceForPrefix(prefix);
	// queryString += "PREFIX " + prefix + ": <" + namespace + ">\n";
	// }
	// return queryString;
	// }
}
