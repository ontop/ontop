/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * This class represents the literal of date-time value.
 */
public class DateTimeLiteral extends Literal {

	private static final long serialVersionUID = -2040969206308713701L;

	private final String[] formatStrings = {
		"yyyy-MM-dd HH:mm:ss.SSS",
		"yyyy-MM-dd HH:mm:ss",
		"yyyy-MM-dd"
	};
	
	/**
	 * The date-time value.
	 */
	protected Timestamp value;

	public DateTimeLiteral(String value) {
		
		for (String formatString : formatStrings)
	    {
	        try
	        {
	        	SimpleDateFormat format = new SimpleDateFormat(formatString);
	        	long miliseconds = format.parse(value).getTime();
	    		set(new Timestamp(miliseconds));
	            break;
	        }
	        catch (ParseException e) { }
	    }
	}

	public void set(Timestamp value) {
		this.value = value;
	}

	public Timestamp get() {
		return value;
	}

	@Override
	public String toString() {
		return get().toString();
	}
}
