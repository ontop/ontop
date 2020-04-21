package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.*;

public class OracleSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String getTopNSQL(String sqlString, int top) {
		return String.format("SELECT * FROM (%s) WHERE ROWNUM <= %d", sqlString, top);
	}



	public static final int NAME_MAX_LENGTH = 30;
	/**
	 * If the name (of a variable/view) needs to be shortcut, length of the number
	 * introduced.
	 */
	public static final int NAME_NUMBER_LENGTH = 3;
	public static final int NAME_MAX_NUMBER = (int)Math.pow(10, NAME_NUMBER_LENGTH);


	/**
	 * Makes sure the view or variable name never exceeds the max length supported by Oracle.
	 *
	 * Strategy: shortens the intermediateName and introduces a number to avoid conflict with
	 * similar names.
	 */

	@Override
	public String nameTopVariable(String identifier, Set<String> used) {

		int length = identifier.length();
		if (length <= NAME_MAX_LENGTH)
			return identifier;

		String shortened = identifier.substring(0, NAME_MAX_LENGTH - NAME_NUMBER_LENGTH);

		// Naive implementation
		for (int i = 0; i < NAME_MAX_NUMBER; i++) {
			String replacement = shortened + i;
			if (!used.contains(replacement))
				return replacement;
		}

		// TODO: find a better exception
		throw new RuntimeException("Impossible to create a new variable/view " + shortened
				+ "???" + " : already " + NAME_MAX_NUMBER + " of them.");
	}
}
