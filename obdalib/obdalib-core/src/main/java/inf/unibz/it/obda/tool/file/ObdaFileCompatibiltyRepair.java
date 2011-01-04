/*
 * @(#)QueryTranslatorTest 27/12/2010
 *
 * Copyright 2010 OBDA-API. All rights reserved.
 * Use is subject to license terms.
 */
package inf.unibz.it.obda.tool.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for transforming the old OBDA file to the new standard.
 *
 * @author Josef Hardi <josef.hardi@gmail.com>
 */
public class ObdaFileCompatibiltyRepair {

	private static final String PREFIXES =
		"<OBDA version=\"1.0\"\n" +
		"\txml:base=\"http://www.owl-ontologies.com/Ontology1207768242.owl#\"\n" +
		"\txmlns=\"http://www.owl-ontologies.com/Ontology1207768242.owl#\"\n" +
		"\txmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
		"\txmlns:owl2xml=\"http://www.w3.org/2006/12/owl2-xml#\"\n" +
		"\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
		"\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
		"\txmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
		"\txmlns:obdap=\"http://obda.org/mapping/predicates/>\"";

	public static void main(String[] args) {

		File file = new File(args[0]);

		if (file.isFile()) {
			repair(file);
		}
		else if (file.isDirectory()) {
			recurseDirectory(file);
		}
	}

	/* Go through the directory and find the list of files */
	private static void recurseDirectory(File file) {
		if (file.isFile()) {
			repair(file);
		}
		else {
			File[] list = file.listFiles();
		    for (int i = 0; i < list.length; i++)
		    	recurseDirectory(list[i]);
		}
	}



	/* Match and replace some text to the new standard. */
	private static void repair(File file) {

		Pattern p = Pattern.compile("rdf:type\\s+'(\\w+)'");
		Matcher m = null;

		if (isObdaFile(file)) {
			try {
				File backupFile = copyFile(file); // creates a backup file

				BufferedReader reader =
					new BufferedReader(new FileReader(backupFile));
				FileWriter writer = new FileWriter(file);

				String line = null;
				while((line = reader.readLine()) != null) {
					// For the case adding prefixes in the <OBDA> tag.
					if (line.contains("<OBDA")) {
						line = PREFIXES;
					}
					// For the case changing the class name in the mapping head.
					else if (line.contains("headclass=")) {
						line = line.replace(
							"inf.unibz.it.obda.api.domain.ucq.ConjunctiveQuery",
							"org.obda.query.domain.imp.CQIEImpl");
					}
					// For the case replacing the rdf:type string.
					else {
						m = p.matcher(line);
						while (m.find()) {
							line = m.replaceFirst("rdf:type :" + m.group(1));
							m = p.matcher(line);
						}
					}
					writer.write(line + "\n");
				}
				reader.close();
				writer.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/* Create a backup for the original file. */
	private static File copyFile(File file) throws IOException {
		String oldFileName = file.toString();
		String newFileName = oldFileName + ".orig";
		File newFile = new File(newFileName);

		FileReader in = new FileReader(file);
	    FileWriter out = new FileWriter(newFile);

	    int c;
	    while ((c = in.read()) != -1)
	      out.write(c);

	    in.close();
	    out.close();

	    return newFile;
	}

	/* Check whether the target file is an OBDA file. */
	private static boolean isObdaFile(File file) {
		String fileName = file.toString();
		String extension =
			fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());

		return extension.equals("obda");
	}
}
