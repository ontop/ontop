/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.codec.DatalogConjunctiveQueryXMLCodec;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class QueryPainter {

	private Vector<ColorTask> tasks = null;
	private boolean alreadyColoring = false;
	private OBDAPreferences pref = null;
	private final OBDAModel apic;

	private boolean isValidQuery = false;

	final DatalogConjunctiveQueryXMLCodec c;
	private SimpleAttributeSet black;
	private SimpleAttributeSet brackets;
	private SimpleAttributeSet functor;
	private SimpleAttributeSet parameters;
	private SimpleAttributeSet dataProp;
	private SimpleAttributeSet objectProp;
	private SimpleAttributeSet clazz;
	private SimpleAttributeSet variable;

	public QueryPainter(OBDAModel apic, OBDAPreferences pref) {
		this.apic = apic;
		this.pref = pref;
		c = new DatalogConjunctiveQueryXMLCodec(apic);

		tasks = new Vector<ColorTask>();
		
		boolean useDefault = new Boolean(pref.get(OBDAPreferences.USE_DEAFAULT).toString());

		black = new SimpleAttributeSet();
		black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);
		if (!useDefault) {
			black.addAttribute(StyleConstants.FontConstants.Family, "SansSerif");
		}

		brackets = new SimpleAttributeSet();
		brackets.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLACK);
		if (!useDefault) {
			brackets.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
			brackets.addAttribute(StyleConstants.FontConstants.Family, "SansSerif");
		}

		functor = new SimpleAttributeSet();
		Color c_func = new Color(Integer.parseInt(pref.get(OBDAPreferences.FUCNTOR_COLOR).toString()));
		functor.addAttribute(StyleConstants.CharacterConstants.Foreground, c_func);
		if (!useDefault) {
			Boolean bold = new Boolean(pref.get(OBDAPreferences.OBDAPREFS_ISBOLD).toString());
			functor.addAttribute(StyleConstants.CharacterConstants.Bold, bold);
			functor.addAttribute(StyleConstants.FontConstants.Family, pref.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString());
			functor.addAttribute(StyleConstants.FontConstants.FontSize,
					Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString()));
		}

		Boolean bold = new Boolean(pref.get(OBDAPreferences.OBDAPREFS_ISBOLD).toString());
		String font = pref.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString();
		Integer size = Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString());

		parameters = new SimpleAttributeSet();
		Color c_para = new Color(Integer.parseInt(pref.get(OBDAPreferences.PARAMETER_COLOR).toString()));
		parameters.addAttribute(StyleConstants.CharacterConstants.Foreground, c_para);
		if (!useDefault) {
			parameters.addAttribute(StyleConstants.FontConstants.Family, font);
			parameters.addAttribute(StyleConstants.CharacterConstants.Bold, bold);
			parameters.addAttribute(StyleConstants.FontConstants.FontSize, size);
		}

		 dataProp = new SimpleAttributeSet();
		Color c_dp = new Color(Integer.parseInt(pref.get(OBDAPreferences.DATAPROPERTY_COLOR).toString()));
		dataProp.addAttribute(StyleConstants.CharacterConstants.Foreground, c_dp);
		if (!useDefault) {
			dataProp.addAttribute(StyleConstants.FontConstants.Family, font);
			dataProp.addAttribute(StyleConstants.CharacterConstants.Bold, bold);
			dataProp.addAttribute(StyleConstants.FontConstants.FontSize, size);
		}

		 objectProp = new SimpleAttributeSet();
		Color c_op = new Color(Integer.parseInt(pref.get(OBDAPreferences.OBJECTPROPTERTY_COLOR).toString()));
		objectProp.addAttribute(StyleConstants.CharacterConstants.Foreground, c_op);
		if (!useDefault) {
			objectProp.addAttribute(StyleConstants.FontConstants.Family, font);
			objectProp.addAttribute(StyleConstants.CharacterConstants.Bold, bold);
			objectProp.addAttribute(StyleConstants.FontConstants.FontSize, size);
		}

		 clazz = new SimpleAttributeSet();
		Color c_clazz = new Color(Integer.parseInt(pref.get(OBDAPreferences.CLASS_COLOR).toString()));
		clazz.addAttribute(StyleConstants.CharacterConstants.Foreground, c_clazz);
		if (!useDefault) {
			clazz.addAttribute(StyleConstants.FontConstants.Family, font);
			clazz.addAttribute(StyleConstants.CharacterConstants.Bold, bold);
			clazz.addAttribute(StyleConstants.FontConstants.FontSize, size);
		}

		variable = new SimpleAttributeSet();
		Color c_var = new Color(Integer.parseInt(pref.get(OBDAPreferences.FUCNTOR_COLOR).toString()));
		variable.addAttribute(StyleConstants.CharacterConstants.Foreground, c_var);
		if (!useDefault) {
			variable.addAttribute(StyleConstants.FontConstants.Family, font);
			variable.addAttribute(StyleConstants.CharacterConstants.Bold, bold);
			variable.addAttribute(StyleConstants.FontConstants.FontSize, size);
		}

	}

	public boolean isAlreadyColoring() {

		return alreadyColoring;
	}

	public void startRecoloring(final MappingStyledDocument doc) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!alreadyColoring)
					recolorQuery(doc);
			}
		});
	}

	public void doRecoloring(MappingStyledDocument doc) {
		recolorQuery(doc);
	}

	private void recolorQuery(MappingStyledDocument doc) {
		alreadyColoring = true;
		String input = null;
		boolean invalid = false;
		CQIE query = null;

		PrefixManager man = apic.getPrefixManager();
		// EntityNameRenderer erenderer = new EntityNameRenderer();


		try {
			input = doc.getText(0, doc.getLength());
			query = c.decode(input);
			if (query == null) {
				invalid = true;
			}
			// checkValidityOfConjunctiveQuery(query);
		} catch (Exception e) {
			invalid = true;
		}

		if (invalid) {

			try {
				doc.removeDocumentListener(doc);
				doc.setCharacterAttributes(0, doc.getLength(), black, true);
				doc.addDocumentListener(doc);
			} catch (Exception e) {
				System.err.print("Unexcpected error: " + e.getMessage());
				e.printStackTrace(System.err);
			}

		} else {
			CQIE current_query = query;
			try {

				doc.removeDocumentListener(doc);
				input = doc.getText(0, doc.getLength());

				int pos = input.indexOf("(", 0);
				while (pos != -1) {
					doc.setCharacterAttributes(pos, 1, brackets, false);
					pos = input.indexOf("(", pos + 1);
				}
				pos = input.indexOf(")", 0);
				while (pos != -1) {
					doc.setCharacterAttributes(pos, 1, brackets, false);
					pos = input.indexOf(")", pos + 1);
				}
				pos = input.indexOf(".", 0);
				while (pos != -1) {
					doc.setCharacterAttributes(pos, 1, black, false);
					pos = input.indexOf(".", pos + 1);
				}
				pos = input.indexOf(",", 0);
				while (pos != -1) {
					doc.setCharacterAttributes(pos, 1, black, false);
					pos = input.indexOf(",", pos + 1);
				}
				pos = input.indexOf(":", 0);
				while (pos != -1) {
					doc.setCharacterAttributes(pos, 1, black, false);
					pos = input.indexOf(":", pos + 1);
				}

				List<Atom> atoms = current_query.getBody();

				Iterator<Atom> it = atoms.iterator();
				while (it.hasNext()) {
					Atom a1 = it.next();
					if (!(a1 instanceof Atom))
						continue;
					Atom at = (Atom) a1;
					int arity = at.getArity();
					if (arity == 1) { // concept query atom

						String name = man.getShortForm(at.getPredicate().toString());
						// int in = input.indexOf(name);
						// setCharacterAttributes(in, name.length(), yellow,
						// false);
						ColorTask t1 = new ColorTask(name, clazz);
						tasks.add(t1);

						List<Term> terms = at.getTerms();
						Term t = terms.get(0);

						if (t instanceof FunctionalTermImpl) {

							FunctionalTermImpl f = (FunctionalTermImpl) t;
							String function = man.getShortForm(f.getFunctionSymbol().toString());

							List<Term> para = f.getTerms();
							Iterator<Term> para_it = para.iterator();
							while (para_it.hasNext()) {

								Term p = para_it.next();
								// TODO NOT SAFE!
								String str = "$" + p.toString();
								ColorTask task2 = new ColorTask(str, variable);
								tasks.add(task2);
							}

							ColorTask task1 = new ColorTask(function, functor);
							tasks.add(task1);

						} else if (t instanceof VariableImpl) {

							VariableImpl v = (VariableImpl) t;
							String str = "$" + v.getName();
							ColorTask task = new ColorTask(str, variable);
							tasks.add(task);

						}

					} else if (arity == 2) { // binary query atom

						String name = man.getShortForm(at.getPredicate().toString());
						// int in = input.indexOf(name);

						List<Term> terms = at.getTerms();

						if (terms.get(0) instanceof FunctionalTermImpl) {

							FunctionalTermImpl f = (FunctionalTermImpl) terms.get(0);
							String function = man.getShortForm(f.toString());
							List<Term> para = f.getTerms();
							Iterator<Term> para_it = para.iterator();
							while (para_it.hasNext()) {

								VariableImpl p = (VariableImpl) para_it.next();
								String str = "$" + p.getName();

								ColorTask task2 = new ColorTask(str, variable);
								tasks.add(task2);

							}
							ColorTask task1 = new ColorTask(function, functor);
							tasks.add(task1);

						} else if (terms.get(0) instanceof VariableImpl) {

							VariableImpl v = (VariableImpl) terms.get(0);
							String str = "$" + v.getName();
							ColorTask task = new ColorTask(str, variable);
							tasks.add(task);

						}
						if (terms.get(1) instanceof FunctionalTermImpl) {

							FunctionalTermImpl f = (FunctionalTermImpl) terms.get(1);
							String function = man.getShortForm(f.getFunctionSymbol().toString());
							//
							List<Term> para = f.getTerms();
							Iterator<Term> para_it = para.iterator();
							while (para_it.hasNext()) {

								VariableImpl p = (VariableImpl) para_it.next();
								String str = "$" + p.getName();

								ColorTask task2 = new ColorTask(str, variable);
								tasks.add(task2);
							}

							ColorTask task = new ColorTask(name, objectProp);
							tasks.add(task);
							ColorTask task1 = new ColorTask(function, functor);
							tasks.add(task1);

						} else if (terms.get(1) instanceof VariableImpl) {

							VariableImpl v = (VariableImpl) terms.get(1);
							String str = "$" + v.getName();
							ColorTask task = new ColorTask(name, dataProp);
							tasks.add(task);
							ColorTask task2 = new ColorTask(str, variable);
							tasks.add(task2);

						}
					} else {
						throw new RuntimeException("Unexpected parameter");
					}
				}

				ColorTask[] taskArray = order(tasks);

				for (int i = 0; i < taskArray.length; i++) {
					if (taskArray[i].text != null) {
						int index = input.indexOf(taskArray[i].text, 0);
						while (index != -1) {
							doc.setCharacterAttributes(index, taskArray[i].text.length(), taskArray[i].set, false);
							index = input.indexOf(taskArray[i].text, index + 1);
						}
					}
				}

				tasks.clear();

				doc.addDocumentListener(doc);

			} catch (Exception e) {
				System.err.print("Unexcpected error: " + e.getMessage());
				e.printStackTrace(System.err);
				doc.addDocumentListener(doc);
			}
		}
		setValidQuery(!invalid);
		alreadyColoring = false;
	}

	private ColorTask[] order(Vector<ColorTask> v) {

		ColorTask[] s = new ColorTask[v.size()];
		ColorTask[] result = v.toArray(s);

		for (int i = 1; i < result.length; i++) {
			int j = i;
			ColorTask str = result[i];
			int B = result[i].text.length();
			while ((j > 0) && (result[j - 1].text != null) && (result[j - 1].text.length() > B)) {
				result[j] = result[j - 1];
				j--;
			}
			result[j] = str;
		}

		return result;
	}

	public void setValidQuery(boolean isValidQuery) {
		this.isValidQuery = isValidQuery;
	}

	public boolean isValidQuery() {
		return isValidQuery;
	}

	class ColorTask {

		public String text = null;
		public SimpleAttributeSet set = null;

		public ColorTask(String s, SimpleAttributeSet sas) {

			text = s;
			set = sas;
		}
	}

}
