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
package inf.unibz.it.obda.gui.swing.preferences;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class OBDAPreferences {
	
	public static final String RESULTSET_OPTION = "resultset_option";
	
	private static OBDAPreferences instance = null;
	private static MappingManagerPreferences mmpInstance = null;
	
	public static final String JODS_RESULTSET_FETCHSIZE = "jods.resultset.fetchsize";
	
	private HashMap<String, Object> obdaPref = null;
	
	public OBDAPreferences(){
		obdaPref = new HashMap<String, Object>();
		setDefaultValues();
	}
	
	public Object getOBDAPreference(String key){
		return obdaPref.get(key);
	}
	
	private void setDefaultValues(){
		obdaPref.put(JODS_RESULTSET_FETCHSIZE, 100);
	}
	
	public MappingManagerPreferences getMappingsPreference(){
		if(mmpInstance == null){
			mmpInstance = new MappingManagerPreferences();
		}
		return mmpInstance;
	}
	
	public interface MappingManagerPreferenceChangeListener{
		
		public void colorPeferenceChanged(String preference, Color col);
		public void fontSizePreferenceChanged(String preference, int size);
		public void fontFamilyPreferenceChanged(String preference, String font);
		public void isBoldPreferenceChanged(String preference, Boolean isBold);
		public void shortCutChanged(String preference, String shortcut);
	} 
	
	public class MappingManagerPreferences{
		
		private List<MappingManagerPreferenceChangeListener> listener = null;
		
		public static final String CLASS_COLOR = "class_Color";
		public static final String CLASS_FONTFAMILY = "class_Fontfamily";
		public static final String CLASS_FONTSIZE = "class_Fontsize";
		public static final String CLASS_ISBOLD = "class_isBold";
		
		public static final String DATAPROPERTY_COLOR = "dataProperty_Color";
		public static final String DATAPROPERTY_FONTFAMILY = "dataProperty_Fontfamily";
		public static final String DATAPROPERTY_FONTSIZE = "dataProperty_Fontsize";
		public static final String DATAPROPERTY_ISBOLD = "dataProperty_isBold";
		
		public static final String VARIABLE_COLOR = "variable_Color";
		public static final String VARIABLE_FONTFAMILY = "variable_Fontfamily";
		public static final String VARIABLE_FONTSIZE = "variable_Fontsize";
		public static final String VARIABLE_ISBOLD = "variable_isBold";
		
		public static final String PARAMETER_COLOR = "parameter_Color";
		public static final String PARAMETER_FONTFAMILY = "parameter_Fontfamily";
		public static final String PARAMETER_FONTSIZE = "parameter_Fontsize";
		public static final String PARAMETER_ISBOLD = "parameter_isBold";
		
		public static final String FUCNTOR_COLOR = "functor_Color";
		public static final String FUCNTOR_FONTFAMILY = "functor_Fontfamily";
		public static final String FUCNTOR_FONTSIZE = "functor_Fontsize";
		public static final String FUCNTOR_ISBOLD = "functor_isBold";
		
		public static final String MAPPING_BODY_COLOR = "mapping_body_Color";
		public static final String MAPPING_BODY_FONTFAMILY = "mapping_body_Fontfamily";
		public static final String MAPPING_BODY_FONTSIZE = "mapping_body_Fontsize";
		public static final String MAPPING_BODY_ISBOLD = "mapping_body_isBold";
		
		public static final String MAPPING_ID_COLOR = "mapping_id_Color";
		public static final String MAPPING_ID_FONTFAMILY = "mapping_id_Fontfamily";
		public static final String MAPPING_ID_FONTSIZE = "mapping_id_Fontsize";
		public static final String MAPPING_ID_ISBOLD = "mapping_id_isBold";
		
		public static final String OBJECTPROPTERTY_COLOR = "objectproperty_Color";
		public static final String OBJECTPROPTERTY_FONTFAMILY = "objectproperty_Fontfamily";
		public static final String OBJECTPROPTERTY_FONTSIZE = "objectproperty_Fontsize";
		public static final String OBJECTPROPTERTY_ISBOLD = "objectproperty_isBold";
		
		public static final String INVALIDQUERY_COLOR = "invalidquery_Color";
		public static final String INVALIDQUERY_FONTFAMILY = "invalidquery_Fontfamily";
		public static final String INVALIDQUERY_FONTSIZE = "invalidquery_Fontsize";
		public static final String INVALIDQUERY_ISBOLD = "invalidquery_isBold";
		
		public static final String DEPENDENCIES_COLOR = "dependencies_Color";
		public static final String DEPENDENCIES_FONTFAMILY = "dependencies_Fontfamily";
		public static final String DEPENDENCIES_FONTSIZE = "dependencies_Fontsize";
		public static final String DEPENDENCIES_ISBOLD = "dependencies_isBold";
		
		public static final String ADD_MAPPING = "add.Mapping";
		public static final String DELETE_MAPPING = "delete.Mapping";
		public static final String EDIT_HEAD = "edit.Mapping.Head";
		public static final String EDIT_BODY = "edit.Mapping.Body";
		public static final String EDIT_ID = "edit.Mapping.id";
		
		private HashMap<String, Color> colorMap = null;
		private HashMap<String, String> fontMap = null;
		private HashMap<String, Integer> sizeMap = null;
		private HashMap<String, Boolean> isBoldMap = null;
		private HashMap<String, String> shortCuts = null;
		
		private MappingManagerPreferences(){
			colorMap = new HashMap<String,Color>();
			fontMap = new HashMap<String, String>();
			sizeMap = new HashMap<String, Integer>();
			isBoldMap = new HashMap<String, Boolean>();
			listener = new Vector<MappingManagerPreferenceChangeListener>();
			shortCuts = new HashMap<String, String>();
			setDefaultValues();
		}
		
		private void setDefaultValues(){
			colorMap.put(CLASS_COLOR, Color.YELLOW.darker());
			colorMap.put(DATAPROPERTY_COLOR, new Color(50, 149, 85));
			colorMap.put(OBJECTPROPTERTY_COLOR, Color.BLUE);
			colorMap.put(VARIABLE_COLOR, Color.GRAY);
			colorMap.put(PARAMETER_COLOR, Color.BLACK);
			colorMap.put(FUCNTOR_COLOR, Color.BLACK);
			colorMap.put(INVALIDQUERY_COLOR, Color.RED);
			colorMap.put(MAPPING_BODY_COLOR, Color.BLACK);
			colorMap.put(MAPPING_ID_COLOR, Color.BLACK);
			colorMap.put(DEPENDENCIES_COLOR, Color.BLACK);
			
			fontMap.put(CLASS_FONTFAMILY, "SansSerif");
			fontMap.put(DATAPROPERTY_FONTFAMILY, "SansSerif");
			fontMap.put(OBJECTPROPTERTY_FONTFAMILY, "SansSerif");
			fontMap.put(VARIABLE_FONTFAMILY, "Monospaced");
			fontMap.put(PARAMETER_FONTFAMILY, "Monospaced");
			fontMap.put(FUCNTOR_FONTFAMILY, "SansSerif");
			fontMap.put(MAPPING_BODY_FONTFAMILY,"Monospaced");
			fontMap.put(MAPPING_ID_FONTFAMILY,"SansSerif");
			fontMap.put(INVALIDQUERY_FONTFAMILY,"SansSerif");
			fontMap.put(DEPENDENCIES_FONTFAMILY, "SansSerif");
			
			sizeMap.put(CLASS_FONTSIZE, new Integer(18));
			sizeMap.put(DATAPROPERTY_FONTSIZE, new Integer(18));
			sizeMap.put(OBJECTPROPTERTY_FONTSIZE, new Integer(18));
			sizeMap.put(VARIABLE_FONTSIZE, new Integer(18));
			sizeMap.put(PARAMETER_FONTSIZE, new Integer(18));
			sizeMap.put(FUCNTOR_FONTSIZE, new Integer(18));
			sizeMap.put(MAPPING_BODY_FONTSIZE,new Integer(18));
			sizeMap.put(MAPPING_ID_FONTSIZE,new Integer(18));
			sizeMap.put(INVALIDQUERY_FONTSIZE,new Integer(18));
			sizeMap.put(DEPENDENCIES_FONTSIZE,new Integer(14));
			
			isBoldMap.put(CLASS_ISBOLD, new Boolean("true"));
			isBoldMap.put(DATAPROPERTY_ISBOLD, new Boolean("true"));
			isBoldMap.put(OBJECTPROPTERTY_ISBOLD, new Boolean("true"));
			isBoldMap.put(VARIABLE_ISBOLD, new Boolean("false"));
			isBoldMap.put(PARAMETER_ISBOLD, new Boolean("false"));
			isBoldMap.put(FUCNTOR_ISBOLD, new Boolean("true"));
			isBoldMap.put(MAPPING_BODY_ISBOLD,new Boolean("false"));
			isBoldMap.put(MAPPING_ID_ISBOLD,new Boolean("false"));
			isBoldMap.put(INVALIDQUERY_ISBOLD,new Boolean("false"));
			isBoldMap.put(DEPENDENCIES_ISBOLD,new Boolean("false"));
			
			shortCuts.put(ADD_MAPPING, "ctrl pressed A");
			shortCuts.put(DELETE_MAPPING, "ctrl pressed D");
			shortCuts.put(EDIT_BODY, "ctrl pressed B");
			shortCuts.put(EDIT_HEAD, "ctrl pressed H");
			shortCuts.put(EDIT_ID, "ctrl pressed I");
			
		}
		
		public int size = 12;
		public int style = 0;
		public String type = "Arial";
		public Font font = new Font(type, style, size);
		
		public int body_maxWight = 600;
		public int body_maxHeight = 35;
		public int body_minWight = 500;
		public int body_minHeight = 30;
		
		public int head_maxWight = 600;
		public int head_maxHeight = 35;
		public int head_minWight = 500;
		public int head_minHeight = 30;
		
		public int getFontSize(String key){
			Integer size = sizeMap.get(key);
			if(size == null){
				try {
					throw new Exception("FAILURE: No entry for "+ key);
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			}else{
				return size;
			}
		}
		
		public String getFontFamily(String key){
			String family = fontMap.get(key);
			if(family == null){
				try {
					throw new Exception("FAILURE: No entry for "+ key);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}else{
				return family;
			}
		}
		
		public Color getColor(String key){
			Color color = colorMap.get(key);
			if(color == null){
				try {
					throw new Exception("FAILURE: No entry for "+ key);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}else{
				return color;
			}
		}
		
		public Boolean isBold(String key){
			Boolean isBold = isBoldMap.get(key);
			if(isBold == null){
				try {
					throw new Exception("FAILURE: No entry for "+ key);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}else{
				return isBold;
			}
		}
		
		public void setFontSize(String key, int value){
			int previousValue = sizeMap.put(key, new Integer(value));
			if(value != previousValue){
				fireFontSizeChangedEvent(key, value);
			}
		}
		
		public void setColor(String key, Color value){
			Color previousValue = colorMap.put(key, value);
			if(!value.equals(previousValue)){
				fireColorChangedEvent(key, value);
			}
		}
		
		public void setFontFamily(String key, String value){
			String previousValue = fontMap.put(key, value);
			if(!value.equals(previousValue)){
				fireFontFamilyChangedEvent(key, value);
			}
		}
		
		public void setIsBold(String key, Boolean value){
			Boolean previousValue =	isBoldMap.put(key, value);
			if(!value.equals(previousValue)){
				fireIsBoldChangedEvent(key, value);
			}
			
		}
		
		private void fireColorChangedEvent(String pref, Color col){
			
			Iterator<MappingManagerPreferenceChangeListener> it = listener.iterator();
			while(it.hasNext()){
				MappingManagerPreferenceChangeListener l = it.next();
				l.colorPeferenceChanged(pref, col);
			}
		}
		
		private void fireFontSizeChangedEvent(String pref, int f){
		
			Iterator<MappingManagerPreferenceChangeListener> it = listener.iterator();
			while(it.hasNext()){
				MappingManagerPreferenceChangeListener l = it.next();
				l.fontSizePreferenceChanged(pref, f);
			}
		}
		
		private void fireFontFamilyChangedEvent(String pref, String font){

			Iterator<MappingManagerPreferenceChangeListener> it = listener.iterator();
			while(it.hasNext()){
				MappingManagerPreferenceChangeListener l = it.next();
				l.fontFamilyPreferenceChanged(pref, font);
			}
		}
		
		private void fireIsBoldChangedEvent(String pref, Boolean isBold){
			Iterator<MappingManagerPreferenceChangeListener> it = listener.iterator();
			while(it.hasNext()){
				MappingManagerPreferenceChangeListener l = it.next();
				l.isBoldPreferenceChanged(pref, isBold);
			}
		}
		
		private void fireShortCutChangedEvent(String pref, String shortCut){
			Iterator<MappingManagerPreferenceChangeListener> it = listener.iterator();
			while(it.hasNext()){
				MappingManagerPreferenceChangeListener l = it.next();
				l.shortCutChanged(pref, shortCut);
			}
		}
		
		public void registerPreferenceChangedListener(MappingManagerPreferenceChangeListener li){
			listener.add(li);
		}
		
		public void removePreferenceChangedListener(MappingManagerPreferenceChangeListener li){
			listener.remove(li);
		}
		
		public String getShortCut(String key){
			String shortcut = shortCuts.get(key);
			if(shortcut == null){
				try {
					throw new Exception("FAILURE: No entry for "+ key);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}else{
				return shortcut;
			}
		}
		
		public void setShortcut(String key, String value){
			String previousValue =shortCuts.put(key, value);
			if(!value.equals(previousValue)){
				fireShortCutChangedEvent(key, value);
			}
		}
	}

}
