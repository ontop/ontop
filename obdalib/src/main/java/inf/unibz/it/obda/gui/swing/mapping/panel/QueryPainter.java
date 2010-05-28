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
import inf.unibz.it.obda.gui.swing.mapping.panel.MappingStyledDocument;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

import java.awt.Color;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class QueryPainter {
	
	private Vector<ColorTask> tasks = null;
	private boolean				alreadyColoring	= false;
	private MappingManagerPreferences pref= null;
	private APIController	apic;
	
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
		ConjunctiveQuery query = null;
		
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
			query = new ConjunctiveQuery(input,apic);
			checkValidityOfConjunctiveQuery(query);

		} catch (Exception e) {
			
//			e.printStackTrace();
			invalid = true;
		}
		
		if(invalid || !query.isInputQueryValid(apic)){
			
					try {
						doc.removeDocumentListener(doc);
						doc.setCharacterAttributes(0, doc.getLength(), invalidQuery, true);
						doc.addDocumentListener(doc);
					} catch (Exception e) {
						System.err.print("Unexcpected error: " + e.getMessage());
						e.printStackTrace(System.err);
					}
				
		}else {
					ConjunctiveQuery current_query = query;
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

						ArrayList<QueryAtom> atoms = current_query.getAtoms();
						
						Iterator it = atoms.iterator();
						while (it.hasNext()){
							
							QueryAtom at = (QueryAtom) it.next();
							
							if (at instanceof ConceptQueryAtom){
								
								ConceptQueryAtom a = (ConceptQueryAtom) at;
								String name = a.getName();
								int in = input.indexOf(name);
//								setCharacterAttributes(in, name.length(), yellow, false);
								ColorTask t1 = new ColorTask(name, clazz);
								tasks.add(t1);
								
								ArrayList<QueryTerm> terms = a.getTerms();				
									QueryTerm t = (QueryTerm) terms.get(0);
									
									if(t instanceof FunctionTerm){
										
										FunctionTerm f = (FunctionTerm) t;
										String function = f.getName();
										
										ArrayList<QueryTerm> para = f.getParameters();
										Iterator para_it = para.iterator();
										while (para_it.hasNext()){
											
											QueryTerm p = (VariableTerm)para_it.next();
											String str = p.toString();
											
											
											ColorTask task2 = new ColorTask(str, parameters);
											tasks.add(task2);
										}
										
										ColorTask task1 = new ColorTask(function, functor);
										tasks.add(task1);
										
										
									}else if(t instanceof VariableTerm){
										
										VariableTerm v = (VariableTerm) t;
										String str = v.toString();
										int j = input.indexOf(str);
//										setCharacterAttributes(j, str.length(), magenta, false);
										ColorTask task = new ColorTask(str, variable);
										tasks.add(task);
										
									}								

							}else if(at instanceof BinaryQueryAtom){
								
								BinaryQueryAtom a = (BinaryQueryAtom) at;
								String name = a.getName();
								int in = input.indexOf(name);
							
								ArrayList<QueryTerm> terms = a.getTerms();
								
								if (terms.get(0) instanceof FunctionTerm){
									
									FunctionTerm f = (FunctionTerm) terms.get(0);
									String function = f.getName();
									ArrayList<QueryTerm> para = f.getParameters();
									Iterator para_it = para.iterator();
									while (para_it.hasNext()){
										
										QueryTerm p = (VariableTerm)para_it.next();
										String str = p.toString();
										
										ColorTask task2 = new ColorTask(str, parameters);
										tasks.add(task2);
										
									}
									ColorTask task1 = new ColorTask(function, functor);
									tasks.add(task1);
									
								}else if(terms.get(0) instanceof VariableTerm){
									
									VariableTerm v = (VariableTerm) terms.get(0);
									String str = v.toString();
									int j = input.indexOf(str);
//									setCharacterAttributes(j, str.length(), magenta, false);
									ColorTask task = new ColorTask(str, variable);
									tasks.add(task);
								
								}
								if (terms.get(1) instanceof FunctionTerm){
									
									FunctionTerm f = (FunctionTerm) terms.get(1);
									String function = f.getName();
//									
									ArrayList<QueryTerm> para = f.getParameters();
									Iterator para_it = para.iterator();
									while (para_it.hasNext()){
										
										QueryTerm p = (VariableTerm)para_it.next();
										String str = p.toString();
										
										ColorTask task2 = new ColorTask(str, parameters);
										tasks.add(task2);
									}
									
									ColorTask task = new ColorTask(name, objectProp);
									tasks.add(task);
									ColorTask task1 = new ColorTask(function, functor);
									tasks.add(task1);
									
								}else if(terms.get(1) instanceof VariableTerm){
									
									VariableTerm v = (VariableTerm) terms.get(1);
									String str = v.toString();
									ColorTask task = new ColorTask(name, dataProp);
									tasks.add(task);
									ColorTask task2 = new ColorTask(str, variable);
									tasks.add(task2);
									
								}
							}
						}
						
						
						ColorTask [] taskArray = order(tasks);
						
						for(int i=0; i<taskArray.length; i++){
							
							int index = input.indexOf(taskArray[i].text, 0);
							while (index != -1) {
								doc.setCharacterAttributes(index, taskArray[i].text.length(), taskArray[i].set, false);
								index = input.indexOf(taskArray[i].text, index + 1);
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
			while ((j > 0) && (result[j-1].text.length() > B)) {
				result[j] =result[j-1];
				j--;
			}
			result[j] = str;
		}
			
		return result;	
	}
	
	private void checkValidityOfConjunctiveQuery(ConjunctiveQuery cq ) throws Exception{
		ArrayList<QueryAtom> atoms = cq.getAtoms();
		Iterator<QueryAtom> it = atoms.iterator();
		APICoupler coup= apic.getCoupler();
		URI onto_uri =apic.getCurrentOntologyURI();
		while(it.hasNext()){
			QueryAtom atom = it.next();
			if(atom instanceof ConceptQueryAtom){
				ConceptQueryAtom cqa = (ConceptQueryAtom) atom;
				String name = cqa.getName();
				boolean isConcept =coup.isNamedConcept(new URI(name));
				if(!isConcept){
					throw new Exception("Concept "+name+" not present in ontology.");
				}
				
			}else{
				BinaryQueryAtom bqa = (BinaryQueryAtom) atom;
				String name = bqa.getName();
				ArrayList<QueryTerm> terms = bqa.getTerms();
				QueryTerm t2 = terms.get(1);
				boolean found = false;
				if(t2 instanceof FunctionTerm){
					found =coup.isObjectProperty(new URI(name));
				}else{
					found =coup.isDatatypeProperty(new URI(name));
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
