package it.unibz.krdb.obda.owlapi3;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class OWLResultSetWriter {

	public static void writeCSV(List<String[]> tabularData, Writer writer) throws IOException {

		// Print the CSV content
		for (String[] rows : tabularData) {
			StringBuilder line = new StringBuilder();
			boolean needComma = false;
			for (int i = 0; i < rows.length; i++) {
				if (needComma) {
					line.append(",");
				}
				line.append(rows[i]);
				needComma = true;
			}			
			writer.write(line + "\n");
			writer.flush();
		}
		writer.close();
	}
}
