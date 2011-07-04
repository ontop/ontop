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
package it.unibz.krdb.obda.utils;


import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class OBDAPreferences{
	
	/**
	 * serial id
	 */
	private static final long serialVersionUID = 8038468716158271480L;
	
	
	public static final String RESULTSET_OPTION = "resultset_option";
	public static final String JODS_RESULTSET_FETCHSIZE = "jods.resultset.fetchsize";
	
	public static final String CLASS_COLOR = "class_Color";
	public static final String CLASS_FONT = "class_font";
	public static final String CLASS_FONTSIZE = "class_fontsize";
	public static final String CLASS_ISBOLD = "class_isbold";
		
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
	
	private OBDAPreferencePersistanceManager persistanceManager = null;
	
	private HashMap<String, Object> obdaPref = null;
	private List<OBDAPreferenceChangeListener> listeners = null;
	
	private Properties preferences = null;
	
	public OBDAPreferences(OBDAPreferencePersistanceManager pm){
		persistanceManager = pm;
		preferences = new Properties();
		listeners = new ArrayList<OBDAPreferenceChangeListener>();
		setDefaultValues();
		loadPreferences();
	}
	
	public String getOBDAPreference(String key){
		return preferences.getProperty(key);
	}
	
	private void loadPreferences(){
		
		String value = persistanceManager.loadPreference(CLASS_COLOR);
		if(value != null){
			preferences.setProperty(CLASS_COLOR, value);
		}
		
		value = persistanceManager.loadPreference(DATAPROPERTY_COLOR);
		if(value != null){
			preferences.setProperty(DATAPROPERTY_COLOR, value);
		}
		
		value = persistanceManager.loadPreference(OBJECTPROPTERTY_COLOR);
		if(value != null){
			preferences.setProperty(OBJECTPROPTERTY_COLOR, value);
		}
		
		value = persistanceManager.loadPreference(VARIABLE_COLOR);
		if(value != null){
			preferences.setProperty(VARIABLE_COLOR, value);
		}
		
		value = persistanceManager.loadPreference(PARAMETER_COLOR);
		if(value != null){
			preferences.setProperty(PARAMETER_COLOR, value);
		}
		
		value = persistanceManager.loadPreference(FUCNTOR_COLOR);
		if(value != null){
			preferences.setProperty(FUCNTOR_COLOR, value);
		}
		
		value = persistanceManager.loadPreference(MAPPING_BODY_COLOR);
		if(value != null){
			preferences.setProperty(MAPPING_BODY_COLOR, value);
		}
		
		value = persistanceManager.loadPreference(MAPPING_ID_COLOR);
		if(value != null){
			preferences.setProperty(MAPPING_ID_COLOR,value);
		}
		
		value = persistanceManager.loadPreference(OBDAPREFS_FONTFAMILY);
		if(value != null){
			preferences.setProperty(OBDAPREFS_FONTFAMILY, value);
		}
		
		value = persistanceManager.loadPreference(OBDAPREFS_FONTSIZE);
		if(value != null){
			preferences.setProperty(OBDAPREFS_FONTSIZE,value);
		}
		
		value = persistanceManager.loadPreference(OBDAPREFS_ISBOLD);
		if(value != null){
			preferences.setProperty(OBDAPREFS_ISBOLD, value);
		}
		
		value = persistanceManager.loadPreference(ADD_MAPPING);
		if(value != null){
			preferences.setProperty(ADD_MAPPING, value);
		}
		
		value = persistanceManager.loadPreference(DELETE_MAPPING);
		if(value != null){
			preferences.setProperty(DELETE_MAPPING, value);
		}
		
		value = persistanceManager.loadPreference(EDIT_BODY);
		if(value != null){
			preferences.setProperty(EDIT_BODY, value);
		}
		
		value = persistanceManager.loadPreference(EDIT_HEAD);
		if(value != null){
			preferences.setProperty(EDIT_HEAD, value);
		}
		
		value = persistanceManager.loadPreference(EDIT_ID);
		if(value != null){
			preferences.setProperty(EDIT_ID, value);
		}
		
		value = persistanceManager.loadPreference(USE_DEAFAULT);
		if(value != null){
			preferences.setProperty(USE_DEAFAULT, value);
		}
		
		value = persistanceManager.loadPreference(JODS_RESULTSET_FETCHSIZE);
		if(value != null){
			preferences.setProperty(JODS_RESULTSET_FETCHSIZE, value);
		}
	}
	
	private void setDefaultValues(){
		
		Integer rgb = Color.YELLOW.darker().getRGB();
		preferences.setProperty(CLASS_COLOR, rgb.toString());
		rgb = new Color(50, 149, 85).getRGB();
		preferences.setProperty(DATAPROPERTY_COLOR, rgb.toString());
		rgb = Color.BLUE.getRGB();
		preferences.setProperty(OBJECTPROPTERTY_COLOR,rgb.toString());
		rgb = Color.GRAY.getRGB();
		preferences.setProperty(VARIABLE_COLOR, rgb.toString());
		rgb = Color.BLACK.getRGB();
		preferences.setProperty(PARAMETER_COLOR, rgb.toString());
		rgb = Color.BLACK.getRGB();
		preferences.setProperty(FUCNTOR_COLOR, rgb.toString());
		rgb = Color.BLACK.getRGB();
		preferences.setProperty(MAPPING_BODY_COLOR, rgb.toString());
		rgb = Color.BLACK.getRGB();
		preferences.setProperty(MAPPING_ID_COLOR, rgb.toString());

		preferences.setProperty(OBDAPREFS_FONTFAMILY, "SansSerif");
		preferences.setProperty(OBDAPREFS_FONTSIZE, "15");
		preferences.setProperty(OBDAPREFS_ISBOLD, "false");
		
		preferences.setProperty(ADD_MAPPING, "ctrl pressed A");
		preferences.setProperty(DELETE_MAPPING, "ctrl pressed D");
		preferences.setProperty(EDIT_BODY, "ctrl pressed B");
		preferences.setProperty(EDIT_HEAD, "ctrl pressed H");
		preferences.setProperty(EDIT_ID, "ctrl pressed I");
		
		preferences.setProperty(USE_DEAFAULT, "false");
		preferences.setProperty(JODS_RESULTSET_FETCHSIZE, "100");
					
	}
		
	public int getFontSize(String key){
		String size = preferences.getProperty(key);
		if(size == null){
			try {
				throw new Exception("FAILURE: No entry for "+ key);
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}else{
			return Integer.parseInt(size);
		}
	}
		
	public String getFontFamily(String key){
		String family = preferences.getProperty(key);
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
		String rgb = preferences.getProperty(key);
		if(rgb == null){
			try {
				throw new Exception("FAILURE: No entry for "+ key);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else{
			Color color = new Color(Integer.parseInt(rgb));
			return color;
		}
	}
		
	public Boolean isBold(String key){
		String isBold = preferences.getProperty(key);
		if(isBold == null){
			return false;
		}else{
			return new Boolean(isBold);
		}
	}
			
	public String getShortCut(String key){
		String shortcut =preferences.getProperty(key);
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
	
	public void setPreference(String key, String newValue){
		preferences.setProperty(key, newValue);
		persistanceManager.storePreference(key, newValue);
		firePreferenceChanged(key, newValue);
	}
		
	
	
	public void registerPreferenceChangedListener(OBDAPreferenceChangeListener li){
			listeners.add(li);
	}
		
	public void removePreferenceChangedListener(OBDAPreferenceChangeListener li){
		listeners.remove(li);
	}
	
	private void firePreferenceChanged(String key, Object value){
		for(OBDAPreferenceChangeListener li : listeners){
			li.preferenceChanged(key, value);
		}
	}
	
}
