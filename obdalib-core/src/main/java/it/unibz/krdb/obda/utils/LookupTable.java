package it.unibz.krdb.obda.utils;

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

import java.util.*;

/**
 * A utility class to map database column names to new variables. The map
 * ignores the letter case of the column names when retrieving the new
 * variables. To implement this, the HashMap will operate on one single case
 * (i.e., lower case). Thus, all input strings that arrive to methods related to
 * HashMap will always be overwritten to lower case.
 */
public class LookupTable {

	private static final String DEFAULT_NAME_FORMAT = "t%s"; // e.g., t1, t2,
	
	/**
	 * Map of variable names to the corresponding numbers
	 */
	private final Map<String, Integer> var2NumMap = new HashMap<>();

	/**
     * Map of numbers to their canonical variable names
     *
     * @see #add(String, int)
	 */
	private final Map<Integer, String> num2CanonicalVarMap = new HashMap<>();

	/**
	 * Set with all unsafe names, names with multiple columns (not qualified)
	 * use for throwing exception.
	 */
	private final Set<String> unsafeEntries = new HashSet<>();
	
	public LookupTable() {
		// NO-OP
	}

	/**
	 * Adds a string entry to this lookup table. A special index will be
	 * assigned to this entry which points to a alternative name defined by the
	 * system.
	 * 
	 * @param entry
	 *            Any string.
	 * @param index
	 *            The entry index.
	 */
	public void add(String entry, int index) {
		if (entry != null) {
			entry = getCanonicalForm(entry);
			putEntry(entry, index);
			register(index);
		}
	}

	/**
	 * Adds a collection of strings to this lookup table. All those strings will
	 * point to a same alternative name defined by the system.
	 * 
	 * @param entries
	 *            An array of strings.
	 * @param index
	 *            The entries index, all entries share the same index number.
	 */
	public void add(String[] entries, int index) {
		for (int i = 0; i < entries.length; i++) {
			if (entries[i] != null) {
				String entry = getCanonicalForm(entries[i]);
				putEntry(entry, index);
			}
		}
		register(index);
	}

	/**
	 * Adds a string entry to this lookup table with a reference of an existing
	 * entry. This method is used when users want to insert a new entry that has
	 * a similarity to the existing one, so both have a same alternative name.
	 * For example, consider the initial lookup table has:
	 * 
	 * <pre>
	 * "Employee.id" --> "t1"
	 * "Employee.name" --> "t2"
	 * </pre>
	 * 
	 * Calling the method add("id_number", "Employee.id") will give you the
	 * result:
	 * 
	 * <pre>
	 * "Employee.id" --> "t1"
	 * "Employee.name" --> "t2"
	 * "id_number" --> "t1"
	 * </pre>
	 * 
	 * @param entry
	 *            A new entry.
	 * @param reference
	 *            An entry that exists already in the lookup table. The method
	 *            will get its index and assign it to the new entry.
	 */
	public void add(String entry, String reference) {
		if (entry == null || reference == null) 
			throw new IllegalArgumentException();
		
		reference = getCanonicalForm(reference);
		Integer index = var2NumMap.get(reference);
		if (index != null) {
			entry = getCanonicalForm(entry);
			putAliasEntry(entry, index);
		}
		else 
			throw new IllegalArgumentException();
	}

	/**
	 * Returns the alternative name for the given entry.
	 */
	public String lookup(String entry) {
		entry = getCanonicalForm(entry);
		Integer index = var2NumMap.get(entry);
		if (index != null) 
			return num2CanonicalVarMap.get(index);
		
		return null;
	}

	/**
	 * Updates the alternative name for the given entry to be the same as the
	 * reference entry. The method returns false if either one or both entry
	 * values are not in the lookup table. For example, consider the initial
	 * lookup table has:
	 * 
	 * <pre>
	 * "Employee.id" --> "t1"
	 * "Employee.name" --> "t2"
	 * "Salary.pid" --> "t3"
	 * </pre>
	 * 
	 * Calling the method asEqualTo("Salary.pid", "Employee.id") will give you
	 * the result:
	 * 
	 * <pre>
	 * "Employee.id" --> "t1"
	 * "Employee.name" --> "t2"
	 * "Salary.pid" --> "t1"
	 * </pre>
	 * 
	 * TEST ONLY
	 * 
	 */
	public boolean asEqualTo(String entry, String reference) {
		entry = getCanonicalForm(entry);
		reference = getCanonicalForm(reference);

		Integer index = var2NumMap.get(entry);
		Integer referenceIndex = var2NumMap.get(reference);
		
		if ((index == null) || (referenceIndex == null)) 
			return false;
		
		String name = num2CanonicalVarMap.get(referenceIndex);
		num2CanonicalVarMap.put(index, name);

		return true;
	}
	
	// ROMAN (23 Sep 2015): YET ANOTHER POINT OF CHOPPING THE NAMES
	// THIS DOES NOT WORK CORRECTLY WITH "A"."B"
	
	private static String getCanonicalForm(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
		}
		return s.toLowerCase();
	}
	

	private static final String printFormat = "%s --> %s\n";

	@Override
	public String toString() {

		String str = "";
		for (Map.Entry<String, Integer> entry : var2NumMap.entrySet()) {
			String name = num2CanonicalVarMap.get(entry.getValue());
			str += String.format(printFormat, entry.getKey(), name);
		}
		return str;
	}

	
	/*
	 * Utility method to add an entry in the lookup table. Input string will be
	 * written in lower case.
	 */
	private void putEntry(String entry, Integer index) {

		// looking for repeated entries, if they exists they are unsafe
		// (generally unqualified names) and they are marked as unsafe.
		
		if (!unsafeEntries.contains(entry)) {
			Integer entryIndex = var2NumMap.get(entry);
			
			if (entryIndex == null) {
				var2NumMap.put(entry, index);
			} 
			else {
				if (entryIndex != index) {
					// Add the entry to unsafe entries list if the new entry is ambiguous.
					unsafeEntries.add(entry);
//					var2NumMap.remove(insertedEntry);
				}
			}
		}
	}
	
	/*
	 * Utility method to add an alias entry in the lookup table. 
	 */
	private void putAliasEntry(String entry, Integer index) {
		/*
		 * looking for repeated entries, if they exists they are unsafe
		 * (generally unqualified names) and they are marked as unsafe.
		 */
		Integer entryIndex = var2NumMap.get(entry);
		
		if (entryIndex == null) {
			var2NumMap.put(entry, index);
		} 
		else {
			if (entryIndex != index) {
				// Add the entry to unsafe entries list if the new entry is ambiguous.
				unsafeEntries.add(entry);
				var2NumMap.remove(entry);
			}
		}
	}



	/*
	 * Assigns the newly added entry to an alternative name.
	 */
	private void register(int index) {
		if (!num2CanonicalVarMap.containsKey(index)) {
			String name = String.format(DEFAULT_NAME_FORMAT, index);
			num2CanonicalVarMap.put(index, name);
		}
		// else already registered -- not an error
	}

	//=================================================================================
	
	/**
	 * Removes the given entry from the lookup table. 
	 * This action is followed by the removal of the alternative name, if necessary.
	 * 
	 * NOTE: USED IN TESTS ONLY
	 */
	public void remove(String entry) {
		removeEntry(entry);
		unregister();
	}

	/**
	 * Removes more than one entry from the lookup table. 
	 * This action is followed by the removal of the alternative name, if necessary.
	 *
	 * NOTE: USED IN TESTS ONLY
	 */
	public void remove(String[] entries) {
		for (int i = 0; i < entries.length; i++) {
			removeEntry(entries[i]);
		}
		unregister();
	}

	
	/*
	 * Utility method to remove an entry from the lookup table. 
	 * 
	 * NOTE: USED IN TESTS ONLY
	 */
	private void removeEntry(String entry) {
		final String sourceEntry = getCanonicalForm(entry);
		var2NumMap.remove(sourceEntry);
	}

	
	/*
	 * Removes the alternative name if the index is no longer available in the
	 * lookup table.
	 * 
	 * NOTE: USED IN TESTS ONLY
	 */
	private void unregister() {
		Set<Integer> set = new HashSet<Integer>();
		Collections.addAll(set, var2NumMap.values().toArray(new Integer[0]));
		Integer[] logIndex = set.toArray(new Integer[0]);
		Integer[] masterIndex = num2CanonicalVarMap.keySet().toArray(new Integer[0]);

		for (int i = 0; i < masterIndex.length; i++) {
			boolean bExist = false;
			for (int j = 0; j < logIndex.length; j++) {
				if (masterIndex[i] == logIndex[j]) {
					bExist = true;
					break;
				}
			}
			if (!bExist) {
				num2CanonicalVarMap.remove(masterIndex[i]);
			}
		}
	}
}
