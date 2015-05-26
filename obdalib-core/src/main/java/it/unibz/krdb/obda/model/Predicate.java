package it.unibz.krdb.obda.model;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
* The Predicate class currently represents (1) first-order predicts, (2) function symbols, and
 * (3) logical operators (e.g. join, left join)
 *
 */
public interface Predicate extends Cloneable, Serializable {

	public static enum COL_TYPE {
		
		UNSUPPORTED (-1), // created only in SesameRDFIterator, ignored by SI and exceptions in all other cases
		NULL (0),
		OBJECT (1),
		BNODE (2),
		LITERAL (3),
		LITERAL_LANG (-3), // not to be mapped from code
		INTEGER (4),
		DECIMAL (5),
		DOUBLE (6),
		STRING (7),
		DATETIME (8),
		BOOLEAN (9),
		DATE (10),
		TIME (11),
		YEAR (12),
		LONG (13),
		FLOAT (14),
		NEGATIVE_INTEGER (15),
		NON_NEGATIVE_INTEGER (16),
		POSITIVE_INTEGER (17),
		NON_POSITIVE_INTEGER (18),
		INT (19),
		UNSIGNED_INT (20),
		DATETIME_STAMP (21);
		
		private static final Map<Integer, Predicate.COL_TYPE> codeToTypeMap = new HashMap<>();
		
		static {
			for (COL_TYPE type : COL_TYPE.values()) {
				// ignore UNSUPPORTED and LITERAL_LANG
				if (type.code >= 0)
					codeToTypeMap.put(type.code, type);
			}
		}
		
		private final int code;
		
		// private constructor
		private COL_TYPE(int code) {
			this.code = code;
		}
		
		public int getQuestCode() {
			return code;
		}
		
		public static COL_TYPE getQuestType(int code) {
			return codeToTypeMap.get(code);
		}
  };

  
  
    
	/**
	 * Get the name of the predicate. In practice, the predicate name is
	 * constructed as a URI to indicate a unique resource.
	 * 
	 * @return the resource identifier (URI).
	 */
	public String getName();

	/**
	 * Get the number of elements of the predicate.
	 * 
	 * @return an integer number.
	 */
	public int getArity();

	/***
	 * Returns the typing of the component given by component. Types can be
	 * "Object" or "Literal", defined by the inner enumerator {@Code Predicate.COL_TYPE}
	 * 
	 * @param column
	 */
	public COL_TYPE getType(int column);

	/**
	 * Duplicate the object by performing a deep cloning.
	 * 
	 * @return the copy of the object.
	 */
	public Predicate clone();

	boolean isClass();

	boolean isObjectProperty();

	boolean isDataProperty();
	
	boolean isDataPredicate();
	
	boolean isBooleanPredicate();
	
	boolean isAlgebraPredicate();
	
	boolean isArithmeticPredicate();
	
	boolean isDataTypePredicate();

    boolean isStringOperationPredicate();

	boolean isTriplePredicate();
}
