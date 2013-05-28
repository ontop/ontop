package it.unibz.krdb.obda.owlapi3.bootstrapping;

import it.unibz.krdb.obda.model.OBDAModel;

import java.io.File;

public class DirectMappingBootstrapperCMD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// check argument correctness
		if (args.length != 4 && args.length != 5) {
			System.out.println("Usage:");
			System.out
					.println(" DirectMappingBootstrapperCMD jdbc_url username password driver [outputfile]");
			System.out.println("");
			System.out.println(" jdbc_url   The jdbc url path");
			System.out.println(" username    The database username");
			System.out.println(" password     The database password");
			System.out.println(" driver     The jdbc driver class name");
			System.out
					.println(" outputfile The full path to the obda output file");
			System.out.println("");
			return;
		}

		// get parameter values
		String url = args[0].trim();
		String user = args[1].trim();
		String passw = args[2].trim();
		String driver = args[3].trim();
		String outfile = null;
		if (args.length == 5)
			outfile = args[4].trim();
		try {
			DirectMappingBootstrapper dm = new DirectMappingBootstrapper(url,
					user, passw, driver);
			if (outfile != null) {
				File f = new File(outfile);
				dm.getMappings(outfile);
			} else {
				OBDAModel model = dm.getMappings();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
