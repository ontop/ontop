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
		public void useDefaultPreferencesChanged(String preference, String useDefault);
	} 
	
	public class MappingManagerPreferences{
		
		private List<MappingManagerPreferenceChangeListener> listener = null;
		
		public static final String CLASS_COLOR = "class_Color";
		public static final String DATAPROPERTY_COLOR = "dataProperty_Color";
		public static final String VARIABLE_COLOR = "variable_Color";
		public static final String PARAMETER_COLOR = "parameter_Color";
		public static final String FUCNTOR_COLOR = "functor_Color";
		public static final String MAPPING_BODY_COLOR = "mapping_body_Color";
		public static final String MAPPING_ID_COLOR = "mapping_id_Color";
		public static final String OBJECTPROPTERTY_COLOR = "objectproperty_Color";
		
		public static final String OBDAPREFS_FONTFAMILY = "dependencies_Fontfamily";
		public static final String OBDAPREFS_FONTSIZE = "dependencies_Fontsize";
		public static final String OBDAPREFS_ISBOLD = "dependencies_isBold";
		
		public static final String ADD_MAPPING = "add.Mapping";
		public static final String DELETE_MAPPING = "delete.Mapping";
		public static final String EDIT_HEAD = "edit.Mapping.Head";
		public static final String EDIT_BODY = "edit.Mapping.Body";
		public static final String EDIT_ID = "edit.Mapping.id";
		public static final String USE_DEAFAULT = "use.default";
		
		private HashMap<String, Color> colorMap = null;
		private HashMap<String, String> fontMap = null;
		private HashMap<String, Integer> sizeMap = null;
		private HashMap<String, Boolean> isBoldMap = null;
		private HashMap<String, String> shortCuts = null;
		private boolean useDefault = true;
		
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
			colorMap.put(MAPPING_BODY_COLOR, Color.BLACK);
			colorMap.put(MAPPING_ID_COLOR, Color.BLACK);

			fontMap.put(OBDAPREFS_FONTFAMILY, "SansSerif");
			sizeMap.put(OBDAPREFS_FONTSIZE, new Integer(15));
			isBoldMap.put(OBDAPREFS_ISBOLD,new Boolean("false"));
			
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
		
		private void fireDefaultPreferencesChanged(String pref, String shortCut){
			Iterator<MappingManagerPreferenceChangeListener> it = listener.iterator();
			while(it.hasNext()){
				MappingManagerPreferenceChangeListener l = it.next();
				l.useDefaultPreferencesChanged(pref, shortCut);
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
		
		public boolean getUseDefault(){
			return useDefault;
		}
		
		public void setUseDefault(boolean b){
			useDefault=b;
			if(useDefault){
				fireDefaultPreferencesChanged(USE_DEAFAULT, "true");
			}else{
				fireDefaultPreferencesChanged(USE_DEAFAULT, "false");
			}
		}
	}

	
}
