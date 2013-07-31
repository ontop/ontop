/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.utils;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OBDAPreferences extends Properties{
	
	private static final long serialVersionUID = 8038468716158271480L;
	
	public static final String RESULTSET_OPTION = "resultset.option";
	public static final String JODS_RESULTSET_FETCHSIZE = "resultset.fetchsize";
	
	public static final String CLASS_COLOR = "class_Color";	
	public static final String DATAPROPERTY_COLOR = "dataProperty_Color";
	public static final String VARIABLE_COLOR = "variable_Color";
	public static final String PARAMETER_COLOR = "parameter_Color";
	public static final String FUCNTOR_COLOR = "functor_Color";
	public static final String MAPPING_BODY_COLOR = "mapping_body_Color";
	public static final String MAPPING_ID_COLOR = "mapping_id_Color";
	public static final String OBJECTPROPTERTY_COLOR = "objectproperty_Color";
		
	public static final String OBDAPREFS_FONTFAMILY = "fontfamily";
	public static final String OBDAPREFS_FONTSIZE = "fontsize";
	public static final String OBDAPREFS_ISBOLD = "isBold";
		
	public static final String ADD_MAPPING = "add.Mapping";
	public static final String DELETE_MAPPING = "delete.Mapping";
	public static final String EDIT_HEAD = "edit.Mapping.Head";
	public static final String EDIT_BODY = "edit.Mapping.Body";
	public static final String EDIT_ID = "edit.Mapping.id";
	public static final String USE_DEAFAULT = "use.default.fontsettings";
	
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
		
	private List<OBDAPreferenceChangeListener> listeners = new ArrayList<OBDAPreferenceChangeListener>();

	private Properties preferences = null;

	public OBDAPreferences(){
		super();
	}
	
	public String getOBDAPreference(String key){
		return preferences.getProperty(key);
	}
	
	@Override
	public synchronized Object setProperty(String key, String value) {
		Object o =  super.setProperty(key, value);
		firePreferenceChanged();
		return o;
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		Object o =  super.put(key, value);
		firePreferenceChanged();
		return o;
	}

	@Override
	public synchronized void putAll(Map<? extends Object, ? extends Object> t) {
		super.putAll(t);
		firePreferenceChanged();
	}

	public void registerPreferenceChangedListener(OBDAPreferenceChangeListener li){
		listeners.add(li);
	}
		
	public void removePreferenceChangedListener(OBDAPreferenceChangeListener li){
		listeners.remove(li);
	}
	
	private void firePreferenceChanged(){
		for(OBDAPreferenceChangeListener li : listeners){
			li.preferenceChanged();
		}
	}
	
	public List<String> getOBDAPreferenceKeys(){
		ArrayList<String> keys = new ArrayList<String>();
		keys.add(JODS_RESULTSET_FETCHSIZE);
		keys.add(CLASS_COLOR);
		keys.add(DATAPROPERTY_COLOR);
		keys.add(VARIABLE_COLOR);
		keys.add(PARAMETER_COLOR);
		keys.add(FUCNTOR_COLOR);
		keys.add(MAPPING_BODY_COLOR);
		keys.add(MAPPING_ID_COLOR);
		keys.add(OBJECTPROPTERTY_COLOR);
		keys.add(OBDAPREFS_FONTFAMILY);
		keys.add(OBDAPREFS_FONTSIZE);
		keys.add(OBDAPREFS_ISBOLD);
		keys.add(ADD_MAPPING);
		keys.add(DELETE_MAPPING);
		keys.add(EDIT_BODY);
		keys.add(EDIT_HEAD);
		keys.add(EDIT_ID);
		keys.add(USE_DEAFAULT);
		return keys;
	}
}
