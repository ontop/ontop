package it.unibz.inf.ontop.model;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
* The Predicate class currently represents (1) first-order predicts, (2) function symbols, and
 * (3) logical operators (e.g. join, left join)
 *
 */
public interface Predicate {

	enum COL_TYPE {
		
		UNSUPPORTED (-1, "UNSUPPORTED"), // created only in SesameRDFIterator, ignored by SI and exceptions in all other cases
		NULL (0, "NULL"),
		OBJECT (1, "OBJECT"),
		BNODE (2, "BNODE"),
		LITERAL (3, "LITERAL"),
		LITERAL_LANG (-3, "LITERAL_LANG"), // not to be mapped from code // BC: Why not?
		INTEGER (4, "INTEGER"),
		DECIMAL (5, "DECIMAL"),
		DOUBLE (6, "DOUBLE"),
		STRING (7, "STRING"),
		DATETIME (8, "DATETIME"),
		BOOLEAN (9, "BOOLEAN"),
		DATE (10, "DATE"),
		TIME (11, "TIME"),
		YEAR (12, "YEAR"),
		LONG (13, "LONG"),
		FLOAT (14, "FLOAT"),
		NEGATIVE_INTEGER (15, "NEGATIVE_INTEGER"),
		NON_NEGATIVE_INTEGER (16, "NON_NEGATIVE_INTEGER"),
		POSITIVE_INTEGER (17, "POSITIVE_INTEGER"),
		NON_POSITIVE_INTEGER (18, "NON_POSITIVE_INTEGER"),
		INT (19, "INT"),
		UNSIGNED_INT (20, "UNSIGNED_INT"),
		DATETIME_STAMP (21, "DATETIME_STAMP");

		private static final ImmutableMap<Integer, COL_TYPE> CODE_TO_TYPE_MAP;
		
		static {
			ImmutableMap.Builder<Integer, COL_TYPE> mapBuilder = ImmutableMap.builder();
			for (COL_TYPE type : COL_TYPE.values()) {
				// ignore UNSUPPORTED (but not LITERAL_LANG anymore)
				if (type.code != -1)
					mapBuilder.put(type.code, type);
			}
			CODE_TO_TYPE_MAP = mapBuilder.build();
		}

		public static final ImmutableSet<COL_TYPE> INTEGER_TYPES = ImmutableSet.of(
				INTEGER, LONG, INT, NEGATIVE_INTEGER, NON_NEGATIVE_INTEGER, POSITIVE_INTEGER, NON_POSITIVE_INTEGER,
				UNSIGNED_INT);

		public static final ImmutableSet<COL_TYPE> NUMERIC_TYPES = ImmutableSet.of(
				DOUBLE, FLOAT, DECIMAL, INTEGER, LONG, INT, NEGATIVE_INTEGER, NON_NEGATIVE_INTEGER,
				POSITIVE_INTEGER, NON_POSITIVE_INTEGER, UNSIGNED_INT);
		
		private final int code;
		private final String label;

		// private constructor
		private COL_TYPE(int code, String label) {
			this.code = code;
			this.label = label;
		}
		
		public int getQuestCode() {
			return code;
		}

		@Override
		public String toString() {
			return label;
		}
		
		public static COL_TYPE getQuestType(int code) {
			return CODE_TO_TYPE_MAP.get(code);
		}
  };

  
  
    
	/**
	 * Get the name of the predicate. In practice, the predicate name is
	 * constructed as a URI to indicate a unique resource.
	 * 
	 * @return the resource identifier (URI).
	 */
    String getName();

	/**
	 * Get the number of elements of the predicate.
	 * 
	 * @return an integer number.
	 */
    int getArity();

	/***
	 * Returns the typing of the component given by component. Types can be
	 * "Object" or "Literal", defined by the inner enumerator {@Code Predicate.COL_TYPE}
	 * 
	 * @param column
	 */
    COL_TYPE getType(int column);


	boolean isClass();

	boolean isObjectProperty();

	boolean isAnnotationProperty();

	boolean isDataProperty();
	
	
	
	boolean isTriplePredicate();

//  boolean isAggregationPredicate();
}
