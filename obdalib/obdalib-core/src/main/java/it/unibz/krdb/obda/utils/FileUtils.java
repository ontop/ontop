package it.unibz.krdb.obda.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
	
	public static void copy(String fromFileName, String toFileName) throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists()) {
			throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
		}
		if (!fromFile.isFile()) {
			throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
		}
		if (!fromFile.canRead()) {
			throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);
		}
		if (toFile.isDirectory()) {
			toFile = new File(toFile, fromFile.getName());
		}
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = from.read(buffer)) != -1) {
				to.write(buffer, 0, bytesRead); // write
			}
		} finally {
			if (from != null) {
				try {
					from.close();
				} catch (IOException e) {
					;
				}
			}
			if (to != null) {
				try {
					to.close();
				} catch (IOException e) {
					;
				}
			}
		}
	}
}
