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

import java.awt.Font;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBDAPreferences extends Properties{
	
	/**
	 * serial id
	 */
	private static final long serialVersionUID = 8038468716158271480L;
	
	private static final String	DEFAULT_PROPERTIESFILE	= "default.properties";
	
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
		
	private List<OBDAPreferenceChangeListener> listeners = null;
	
	private Properties preferences = null;
	
	private Logger				log						= LoggerFactory.getLogger(OBDAPreferences.class);
	
	public OBDAPreferences(){
		super();
		listeners = new ArrayList<OBDAPreferenceChangeListener>();
		setDefaultValues();
	}
	
	public String getOBDAPreference(String key){
		return preferences.getProperty(key);
	}
	
	private void setDefaultValues(){
		try {
			InputStream in = OBDAPreferences.class.getResourceAsStream(DEFAULT_PROPERTIESFILE);
			this.load(in);
		} catch (Exception e) {
			log.error("Error reading default properties for resoner.");
			log.debug(e.getMessage(), e);
		}			
	}
	
	@Override
	public synchronized Object setProperty(String key, String value) {
		Object o =  super.setProperty(key, value);
		firePreferenceChanged();
		return o;
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		// TODO Auto-generated method stub
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
