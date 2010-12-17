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
package inf.unibz.it.obda.gui.swing.mapping.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;
import inf.unibz.it.obda.codec.xml.DatalogConjunctiveQueryCodec;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;

import java.awt.Color;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.ObjectVariableImpl;
import org.obda.query.domain.imp.VariableImpl;

public class QueryPainter {

	private Vector<ColorTask> tasks = null;
	private boolean				alreadyColoring	= false;
	private MappingManagerPreferences pref= null;
	private final APIController	apic;

	public QueryPainter(APIController apic){
		this.apic = apic;

		tasks= new Vector<ColorTask>();
		pref =  OBDAPreferences.getOBDAPreferences().getMappingsPreference();
	}

	public boolean isAlreadyColoring(){

		return alreadyColoring;
	}

	public void startRecoloring(final MappingStyledDocument doc){

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
					recolorQuery(doc);
			}
		});
	}

	public void doRecoloring(MappingStyledDocument doc){

		recolorQuery(doc);
	}

	private void recolorQuery(MappingStyledDocument doc) {

		alreadyColoring = true;
		String input = null;
		boolean invalid = false;
		CQIE query = null;

		SimpleAttributeSet black = new SimpleAttributeSet();
		black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);
		black.addAttribute(StyleConstants.FontConstants.Family, "SansSerif" );

		SimpleAttributeSet brackets = new SimpleAttributeSet();
		brackets.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
		brackets.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLACK);
		brackets.addAttribute(StyleConstants.FontConstants.Family, "SansSerif" );

		SimpleAttributeSet functor = new SimpleAttributeSet();
		functor.addAttribute(StyleConstants.CharacterConstants.Bold, pref.isBold(MappingManagerPreferences.FUCNTOR_ISBOLD));
		functor.addAttribute(StyleConstants.CharacterConstants.Foreground, pref.getColor(MappingManagerPreferences.FUCNTOR_COLOR));
		functor.addAttribute(StyleConstants.FontConstants.Family, pref.getFontFamily(MappingManagerPreferences.FUCNTOR_FONTFAMILY) );
		functor.addAttribute(StyleConstants.FontConstants.FontSize, pref.getFontSize(MappingManagerPreferences.FUCNTOR_FONTSIZE));

		SimpleAttributeSet parameters = new SimpleAttributeSet();
		parameters.addAttribute(StyleConstants.FontConstants.Family, pref.getFontFamily(MappingManagerPreferences.PARAMETER_FONTFAMILY));
		parameters.addAttribute(StyleConstants.CharacterConstants.Foreground, pref.getColor(MappingManagerPreferences.PARAMETER_COLOR));
		parameters.addAttribute(StyleConstants.CharacterConstants.Bold, pref.isBold(MappingManagerPreferences.PARAMETER_ISBOLD));
		parameters.addAttribute(StyleConstants.FontConstants.FontSize, pref.getFontSize(MappingManagerPreferences.PARAMETER_FONTSIZE));

		SimpleAttributeSet dataProp = new SimpleAttributeSet();
		dataProp.addAttribute(StyleConstants.CharacterConstants.Bold, pref.isBold(MappingManagerPreferences.DATAPROPERTY_ISBOLD));
		dataProp.addAttribute(StyleConstants.CharacterConstants.Foreground, pref.getColor(MappingManagerPreferences.DATAPROPERTY_COLOR));
		dataProp.addAttribute(StyleConstants.FontConstants.Family, pref.getFontFamily(MappingManagerPreferences.DATAPROPERTY_FONTFAMILY) );
		dataProp.addAttribute(StyleConstants.FontConstants.FontSize, pref.getFontSize(MappingManagerPreferences.DATAPROPERTY_FONTSIZE) );

		SimpleAttributeSet objectProp = new SimpleAttributeSet();
		objectProp.addAttribute(StyleConstants.CharacterConstants.Bold, pref.isBold(MappingManagerPreferences.OBJECTPROPTERTY_ISBOLD));
		objectProp.addAttribute(StyleConstants.CharacterConstants.Foreground, pref.getColor(MappingManagerPreferences.OBJECTPROPTERTY_COLOR));
		objectProp.addAttribute(StyleConstants.FontConstants.Family, pref.getFontFamily(MappingManagerPreferences.OBJECTPROPTERTY_FONTFAMILY));
		objectProp.addAttribute(StyleConstants.FontConstants.FontSize, pref.getFontSize(MappingManagerPreferences.DATAPROPERTY_FONTSIZE) );

		SimpleAttributeSet clazz = new SimpleAttributeSet();
		clazz.addAttribute(StyleConstants.CharacterConstants.Bold, pref.isBold(MappingManagerPreferences.CLASS_ISBOLD));
		clazz.addAttribute(StyleConstants.CharacterConstants.Foreground, pref.getColor(MappingManagerPreferences.CLASS_COLOR));
		clazz.addAttribute(StyleConstants.FontConstants.Family, pref.getFontFamily(MappingManagerPreferences.CLASS_FONTFAMILY));
		clazz.addAttribute(StyleConstants.FontConstants.FontSize, pref.getFontSize(MappingManagerPreferences.CLASS_FONTSIZE) );


		SimpleAttributeSet variable = new SimpleAttributeSet();
		variable.addAttribute(StyleConstants.CharacterConstants.Bold, pref.isBold(MappingManagerPreferences.VARIABLE_ISBOLD));
		variable.addAttribute(StyleConstants.CharacterConstants.Foreground, pref.getColor(MappingManagerPreferences.VARIABLE_COLOR));
		variable.addAttribute(StyleConstants.FontConstants.Family, pref.getFontFamily(MappingManagerPreferences.VARIABLE_FONTFAMILY));
		variable.addAttribute(StyleConstants.FontConstants.FontSize, pref.getFontSize(MappingManagerPreferences.VARIABLE_FONTSIZE) );


		SimpleAttributeSet invalidQuery = new SimpleAttributeSet();
		invalidQuery.addAttribute(StyleConstants.CharacterConstants.Foreground, pref.getColor(MappingManagerPreferences.INVALIDQUERY_COLOR));
		invalidQuery.addAttribute(StyleConstants.FontConstants.Family, pref.getFontFamily(MappingManagerPreferences.INVALIDQUERY_FONTFAMILY) );
		invalidQuery.addAttribute(StyleConstants.FontSize, pref.getFontSize(MappingManagerPreferences.INVALIDQUERY_FONTSIZE));
		invalidQuery.addAttribute(StyleConstants.CharacterConstants.Bold, pref.isBold(MappingManagerPreferences.INVALIDQUERY_ISBOLD));


		try {
			input = doc.getText(0, doc.getLength());
			DatalogConjunctiveQueryCodec c = new DatalogConjunctiveQueryCodec(apic);
			query = c.decode(input);
			if(query == null){
				invalid = true;
			}
//			checkValidityOfConjunctiveQuery(query);

		} catch (Exception e) {

//			e.printStackTrace();
			invalid = true;
		}

		if (invalid) {

					try {
						doc.removeDocumentListener(doc);
						doc.setCharacterAttributes(0, doc.getLength(), invalidQuery, true);
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
						while (it.hasNext()){

							Atom at = it.next();
							int arity = at.getArity();
							if (arity == 1){  // concept query atom

								String name = apic.getEntityNameRenderer().getPredicateName(at);
//								int in = input.indexOf(name);
//								setCharacterAttributes(in, name.length(), yellow, false);
								ColorTask t1 = new ColorTask(name, clazz);
								tasks.add(t1);

								List<Term> terms = at.getTerms();
									Term t = terms.get(0);

									if(t instanceof ObjectVariableImpl){

										ObjectVariableImpl f = (ObjectVariableImpl) t;
										String function = apic.getEntityNameRenderer().getFunctionName(f);

										List<Term> para = f.getTerms();
										Iterator<Term> para_it = para.iterator();
										while (para_it.hasNext()){

											Term p = para_it.next();
											String str = "$"+p.getName();
											ColorTask task2 = new ColorTask(str, variable);
											tasks.add(task2);
										}

										ColorTask task1 = new ColorTask(function, functor);
										tasks.add(task1);


									}else if(t instanceof VariableImpl){

										VariableImpl v = (VariableImpl) t;
										String str = "$"+v.getName();
										ColorTask task = new ColorTask(str, variable);
										tasks.add(task);

									}

							} else if (arity == 2) {  // binary query atom

								String name = apic.getEntityNameRenderer().getPredicateName(at);
//								int in = input.indexOf(name);

								List<Term> terms = at.getTerms();

								if (terms.get(0) instanceof ObjectVariableImpl){

									ObjectVariableImpl f = (ObjectVariableImpl) terms.get(0);
									String function = apic.getEntityNameRenderer().getFunctionName(f);
									List<Term> para = f.getTerms();
									Iterator para_it = para.iterator();
									while (para_it.hasNext()){

										Term p = (VariableImpl)para_it.next();
										String str = "$"+p.getName();

										ColorTask task2 = new ColorTask(str, variable);
										tasks.add(task2);

									}
									ColorTask task1 = new ColorTask(function, functor);
									tasks.add(task1);

								}else if(terms.get(0) instanceof VariableImpl){

									VariableImpl v = (VariableImpl) terms.get(0);
									String str = "$"+v.getName();
									ColorTask task = new ColorTask(str, variable);
									tasks.add(task);

								}
								if (terms.get(1) instanceof ObjectVariableImpl){

									ObjectVariableImpl f = (ObjectVariableImpl) terms.get(1);
									String function = apic.getEntityNameRenderer().getFunctionName(f);
//
									List<Term> para = f.getTerms();
									Iterator<Term> para_it = para.iterator();
									while (para_it.hasNext()){

										Term p = para_it.next();
										String str = "$"+p.getName();

										ColorTask task2 = new ColorTask(str, variable);
										tasks.add(task2);
									}

									ColorTask task = new ColorTask(name, objectProp);
									tasks.add(task);
									ColorTask task1 = new ColorTask(function, functor);
									tasks.add(task1);

								}else if(terms.get(1) instanceof VariableImpl){

									VariableImpl v = (VariableImpl) terms.get(1);
									String str = "$"+v.getName();
									ColorTask task = new ColorTask(name, dataProp);
									tasks.add(task);
									ColorTask task2 = new ColorTask(str, variable);
									tasks.add(task2);

								}
							} else {
								// TODO Throw an exception.
							}
						}


						ColorTask [] taskArray = order(tasks);

						for(int i=0; i<taskArray.length; i++){
							if(taskArray[i].text != null){
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


		alreadyColoring = false;
	}

	private  ColorTask[] order(Vector<ColorTask> v){

		ColorTask[] s = new ColorTask[v.size()];
		ColorTask[] result = v.toArray(s);

		for (int i = 1; i < result.length; i++) {
			int j = i;
			ColorTask str = result[i];
			int B = result[i].text.length();
			while ((j > 0) && (result[j-1].text != null) && (result[j-1].text.length() > B)) {
				result[j] =result[j-1];
				j--;
			}
			result[j] = str;
		}

		return result;
	}

	private void checkValidityOfConjunctiveQuery(CQIE cq) throws Exception{
		List<Atom> atoms = cq.getBody();
		Iterator<Atom> it = atoms.iterator();
		APICoupler coup= apic.getCoupler();
		URI onto_uri = apic.getCurrentOntologyURI();
		while(it.hasNext()){
			Atom atom = it.next();
			int arity = atom.getArity();
			if (arity == 1) {  // concept query atom
				String name =apic.getEntityNameRenderer().getPredicateName(atom);
				boolean isConcept =coup.isNamedConcept(onto_uri,new URI(name));
				if(!isConcept){
					throw new Exception("Concept "+name+" not present in ontology.");
				}

			} else if (arity == 2) {  // binary query atom
				String name = apic.getEntityNameRenderer().getPredicateName(atom);
				List<Term> terms = atom.getTerms();
				Term t2 = terms.get(1);
				boolean found = false;
				if(t2 instanceof ObjectVariableImpl){
					found =coup.isObjectProperty(onto_uri,new URI(name));
				}else{
					found =coup.isDatatypeProperty(onto_uri,new URI(name));
				}
				if(!found){
					throw new Exception("Property "+name+" not present in ontology.");
				}
			}
		}
	}


	class ColorTask {

		public String text = null;
		public SimpleAttributeSet set = null;

		public ColorTask(String s, SimpleAttributeSet sas){

			text = s;
			set = sas;
		}
	}


}
