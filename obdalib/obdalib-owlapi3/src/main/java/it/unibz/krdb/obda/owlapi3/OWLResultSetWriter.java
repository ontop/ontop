package it.unibz.krdb.obda.owlapi3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OWLResultSetWriter {

	public static void writeCSV(List<String[]> tabularData, String fileLocation) throws IOException {

		File output = new File(fileLocation);
		if (fileLocation.lastIndexOf(".") == -1) { // without extension
			output = new File(fileLocation + ".csv");
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(output, false));
		
		// Print the CSV content
		for (String[] rows : tabularData) {
			StringBuffer line = new StringBuffer();
			boolean needComma = false;
			for (int i = 0; i < rows.length; i++) {
				if (needComma) {
					line.append(",");
				}
				line.append(rows[i]);
				needComma = true;
			}			
			writer.write(line + "\n");
		}
		writer.flush();
		writer.close();
	}
}
