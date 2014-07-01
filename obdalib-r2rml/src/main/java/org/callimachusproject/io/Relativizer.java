/*
   Copyright (c) 2011 3 Round Stones Inc, Some Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.callimachusproject.io;

import java.net.URI;
import java.net.URISyntaxException;

public class Relativizer {
	private final String baseURI;
	private final String authURI;
	private final String pathURI;
	private final String queryURI;
	private final String fragURI;
	private final String[] paths;

	public Relativizer(String baseURI) throws URISyntaxException {
		this.baseURI = baseURI;
		if (baseURI == null || java.net.URI.create(baseURI).isOpaque()) {
			authURI = null;
			pathURI = null;
			queryURI = null;
			fragURI = null;
			paths = new String[0];
		} else {
			java.net.URI parsed = URI.create(baseURI);
			paths = URI.create(baseURI).getPath().split("/", Integer.MAX_VALUE);
			String s = parsed.getScheme();
			String a = parsed.getAuthority();
			authURI = new URI(s, a, "/", null, null).toString();
			int path = baseURI.lastIndexOf('/');
			pathURI = baseURI.substring(0, path + 1);
			int query = baseURI.lastIndexOf('?');
			if (query < 0) {
				queryURI = baseURI + "?";
			} else {
				queryURI = baseURI.substring(0, query + 1);
			}
			int frag = baseURI.lastIndexOf('#');
			if (frag < 0) {
				fragURI = baseURI + "#";
			} else {
				fragURI = baseURI.substring(0, frag + 1);
			}
		}

	}

	public String relativize(String uri) {
		// identity URI reference
		if (uri.equals(baseURI) && uri.indexOf('#') < 0)
			return "";
		// opaque URI
		if (pathURI == null)
			return uri;
		// fragment URI reference
		if (uri.startsWith(fragURI))
			return uri.substring(fragURI.length() - 1);
		// query string URI reference
		if (uri.startsWith(queryURI))
			return uri.substring(queryURI.length() - 1);
		// last path segment
		if (uri.equals(pathURI))
			return ".";
		// within last path segment
		if (uri.startsWith(pathURI))
			return uri.substring(pathURI.length());
		// different scheme or authority
		if (!uri.startsWith(authURI))
			return uri;
		try {
			URI net = URI.create(uri);
			String path = relativizePath(net.getPath());
			URI ref = new URI(null, null, path, net.getQuery(),
					net.getFragment());
			return ref.toString();
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}

	private String relativizePath(String path) {
		assert path.charAt(0) == '/';
		String[] seg = path.split("/", Integer.MAX_VALUE);
		// first segment is empty string
		int diff = 1;
		while (diff < paths.length && diff < seg.length - 1
				&& paths[diff].equals(seg[diff])) {
			diff++;
		}
		if (diff < 2) // no path segments in common
			return path;
		StringBuilder sb = new StringBuilder();
		// last segment is empty or file name
		for (int i = diff; i < paths.length - 1; i++) {
			sb.append("../");
		}
		for (int i = diff; i < seg.length - 1; i++) {
			sb.append(seg[i]).append('/');
		}
		return sb.append(seg[seg.length - 1]).toString();
	}
}