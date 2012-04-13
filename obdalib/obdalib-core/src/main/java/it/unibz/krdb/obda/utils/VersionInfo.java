package it.unibz.krdb.obda.utils;

public class VersionInfo {
	
	private static VersionInfo instance;

	private String version;

	private VersionInfo() {
		String v = VersionInfo.class.getPackage().getImplementationVersion();
		if (v != null) {
			version = v;
		} else {
			version = "[Not Released]";
		}
	}

	public synchronized static VersionInfo getVersionInfo() {
		if (instance == null) {
			instance = new VersionInfo();
		}
		return instance;
	}

	/**
	 * Gets a string that contains the version of this build. This is generated
	 * from the manifest of the jar that this class is packaged in.
	 * 
	 * @return The version info string (if available).
	 */
	public String getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return "OBDAlib API version " + version;
	}
}
