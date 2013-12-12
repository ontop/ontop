package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
	 * Map of entries that have an alternative name.
	 */
	private HashMap<String, Integer> log = new HashMap<String, Integer>();

	/**
	 * Map of alternative names.
	 */
	private HashMap<Integer, String> master = new HashMap<Integer, String>();

	/**
	 * Set with all unsafe names, names with multiple columns (not qualified)
	 * use for throwing exception.
	 */
	private HashSet<String> unsafeEntries = new HashSet<String>();
	
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
				putEntry(entries[i], index);
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
		if (entry == null || reference == null) {
			return;
		}
		Integer index = getEntry(reference);
		if (index != null) {
			putAliasEntry(entry, index);
		}
	}

	/**
	 * Returns the alternative name for the given entry.
	 */
	public String lookup(String entry) {
		if (exist(entry)) {
			Integer index = getEntry(entry);
			return retrieve(index);
		}
		return null;
	}

	/**
	 * Removes the given entry from the lookup table. This action follows the
	 * removal of the alternative name, if necessary.
	 */
	public void remove(String entry) {
		removeEntry(entry);
		unregister();
	}

	/**
	 * Removes more than one entry from the lookup table. This action follows
	 * the removal of the alternative name, if necessary.
	 */
	public void remove(String[] entries) {
		for (int i = 0; i < entries.length; i++) {
			removeEntry(entries[i]);
		}
		unregister();
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
	 */
	public boolean asEqualTo(String entry, String reference) {
		if (!exist(entry) || !exist(reference)) {
			return false;
		}
		String name = lookup(reference);
		Integer index = getEntry(entry);
		update(index, name);

		return true;
	}

	@Override
	public String toString() {
		final String printFormat = "%s --> %s";

		String str = "";
		for (String entry : log.keySet()) {
			String name = lookup(entry);
			str += String.format(printFormat, entry, name);
			str += "\n";
		}
		return str;
	}

	/*
	 * Utility method to check if the entry exists already in the table or not.
	 * Input string will be written in lower case.
	 */
	private boolean exist(String entry) {
		final String sourceEntry = entry.toLowerCase();
		return (log.containsKey(trim(sourceEntry)) || log.containsKey(sourceEntry));
	}

	private String trim(String string) {
		
		while (string.startsWith("\"") && string.endsWith("\"")) {
			
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}
	/*
	 * Utility method to add an entry in the lookup table. Input string will be
	 * written in lower case.
	 */
	private void putEntry(String entry, Integer index) {
		final String insertedEntry = entry.toLowerCase();
		/*
		 * looking for repeated entries, if they exists they are unsafe
		 * (generally unqualified names) and they are marked as unsafe.
		 */
		boolean isExist = log.containsKey(insertedEntry);
		
		if (!ambiguous(insertedEntry)) {
			if (!isExist) {
				log.put(insertedEntry, index);
			} else {
				if (!identical(insertedEntry, index)) {
					// Add the entry to unsafe entries list if the new entry is ambiguous.
					unsafeEntries.add(insertedEntry);
					log.remove(insertedEntry);
				}
			}
		}
	}
	
	/*
	 * Utility method to add an alias entry in the lookup table. Input string will be
	 * written in lower case.
	 */
	private void putAliasEntry(String entry, Integer index) {
		final String insertedEntry = entry.toLowerCase();
		/*
		 * looking for repeated entries, if they exists they are unsafe
		 * (generally unqualified names) and they are marked as unsafe.
		 */
		boolean isExist = log.containsKey(insertedEntry);
		
		if (!isExist) {
			log.put(insertedEntry, index);
		} else {
			if (!identical(insertedEntry, index)) {
				// Add the entry to unsafe entries list if the new entry is ambiguous.
				unsafeEntries.add(insertedEntry);
				log.remove(insertedEntry);
			}
		}
	}

	private boolean ambiguous(String entry) {
		return unsafeEntries.contains(entry);
	}

	/*
	 * Checks if the already existed entry is actually identical entry by checking also its index.
	 */
	private boolean identical(String insertedEntry, Integer index) {
		return (index == log.get(insertedEntry)) ? true : false;
	}

	/*
	 * Utility method to get an entry from the lookup table. Input string will
	 * be written in lower case.
	 */
	private Integer getEntry(String entry) {
		return log.get(trim(entry.toLowerCase()));
	}

	/*
	 * Utility method to remove an entry from the lookup table. Input string
	 * will be written in lower case.
	 */
	private void removeEntry(String entry) {
		final String sourceEntry = entry.toLowerCase();
		log.remove(sourceEntry);
	}

	/*
	 * Retrieves the alternative name given the index number
	 */
	private String retrieve(int index) {
		return master.get(index);
	}

	/*
	 * Changes the alternative in the given index number
	 */
	private void update(int index, String value) {
		master.put(index, value);
	}

	/*
	 * Assigns the newly added entry to an alternative name.
	 */
	private void register(int index) {
		if (!master.containsKey(index)) {
			String name = String.format(DEFAULT_NAME_FORMAT, index);
			master.put(index, name);
		}
	}

	/*
	 * Removes the alternative name if the index is no longer available in the
	 * lookup table.
	 */
	private void unregister() {
		Set<Integer> set = new HashSet<Integer>();
		Collections.addAll(set, log.values().toArray(new Integer[0]));
		Integer[] logIndex = set.toArray(new Integer[0]);
		Integer[] masterIndex = master.keySet().toArray(new Integer[0]);

		for (int i = 0; i < masterIndex.length; i++) {
			boolean bExist = false;
			for (int j = 0; j < logIndex.length; j++) {
				if (masterIndex[i] == logIndex[j]) {
					bExist = true;
					break;
				}
			}
			if (!bExist) {
				master.remove(masterIndex[i]);
			}
		}
	}
}
