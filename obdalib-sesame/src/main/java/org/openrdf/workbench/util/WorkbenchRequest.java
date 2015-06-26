/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.workbench.util;

import static java.net.URLDecoder.decode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.exceptions.BadRequestException;

/**
 * Request wrapper used by {@link org.openrdf.workbench.base
 * TransformationServlet}.
 */
public class WorkbenchRequest extends HttpServletRequestWrapper {

	private static final String UTF_8 = "UTF-8";

	private Map<String, String> parameters;

	private final Map<String, String> defaults;

	private InputStream content;

	private String contentFileName;

	private final ValueDecoder decoder;

	/**
	 * Wrap a request with an object aware of the current repository and
	 * application defaults.
	 * 
	 * @param repository
	 *        currently connected repository
	 * @param request
	 *        current request
	 * @param defaults
	 *        application default parameter values
	 * @throws RepositoryException
	 *         if there is an issue retrieving the parameter map
	 * @throws IOException
	 *         if there is an issue retrieving the parameter map
	 * @throws FileUploadException
	 *         if there is an issue retrieving the parameter map
	 */
	public WorkbenchRequest(Repository repository, HttpServletRequest request, Map<String, String> defaults)
		throws RepositoryException, IOException, FileUploadException
	{
		super(request);
		this.defaults = defaults;
		this.decoder = new ValueDecoder(repository, (repository == null) ? ValueFactoryImpl.getInstance()
				: repository.getValueFactory());
		String url = request.getRequestURL().toString();
		if (ServletFileUpload.isMultipartContent(this)) {
			parameters = getMultipartParameterMap();
		}
		else if (request.getQueryString() == null && url.contains(";")) {
			parameters = getUrlParameterMap(url);
		}
	}

	/**
	 * Get the content of any uploaded file that is part of this request.
	 * 
	 * @return the uploaded file contents, or null if not applicable
	 */
	public InputStream getContentParameter() {
		return content;
	}

	/**
	 * Get the name of any uploaded file that is part of this request.
	 * 
	 * @return the uploaded file name, or null if not applicable
	 */
	public String getContentFileName() {
		return contentFileName;
	}

	/***
	 * Get the integer value associated with the given parameter name. Internally
	 * uses getParameter(String), so looks in this order: 1. the query parameters
	 * that were parsed at construction, using the last value if multiple exist.
	 * 2. Request cookies. 3. The defaults.
	 * 
	 * @return the value of the parameter, or zero if it is not present
	 * @throws BadRequestException
	 *         if the parameter is present but does not parse as an integer
	 */
	public int getInt(String name)
		throws BadRequestException
	{
		int result = 0;
		String limit = getParameter(name);
		if (limit != null && !limit.isEmpty()) {
			try {
				result = Integer.parseInt(limit);
			}
			catch (NumberFormatException exc) {
				throw new BadRequestException(exc.getMessage(), exc);
			}
		}
		return result;
	}

	@Override
	public String getParameter(String name) {
		String result = null;
		if (parameters != null && parameters.containsKey(name)) {
			result = parameters.get(name);
		}
		else {
			String[] values = super.getParameterValues(name);
			if (values != null && values.length > 0) {
				// use the last one as it may be appended in JavaScript
				result = values[values.length - 1];
			}
			else {
				result = getCookie(name);
				if (result == null && defaults != null && defaults.containsKey(name)) {
					result = defaults.get(name);
				}
			}
		}
		return result;
	}

	private String getCookie(String name) {
		String result = null;
		Cookie[] cookies = getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (name.equals(cookie.getName())) {
					result = cookie.getValue();
					break;
				}
			}
		}
		return result;
	}

	@Override
	public String[] getParameterValues(String name) {
		return (parameters != null && parameters.containsKey(name)) ? new String[] { parameters.get(name) }
				: super.getParameterValues(name);
	}

	/**
	 * Returns whether a non-null, non-empty value is available for the given
	 * parameter name.
	 * 
	 * @param name
	 *        parameter name to check
	 * @return true if a non-null, non-empty value exists, false otherwise
	 */
	public boolean isParameterPresent(String name) {
		boolean result = false;
		if (parameters == null || parameters.get(name) == null) {
			String[] values = super.getParameterValues(name);
			if (values != null && values.length > 0) {
				// use the last one as it may be appended in JavaScript
				result = !values[values.length - 1].isEmpty();
			}
		}
		else {
			result = !parameters.get(name).isEmpty();
		}
		return result;
	}

	/**
	 * Returns a {@link org.openrdf.model.Resource} corresponding to the value of
	 * the given parameter name.
	 * 
	 * @param name
	 *        of parameter to retrieve resource from
	 * @return value corresponding to the given parameter name
	 * @throws BadRequestException
	 *         if a problem occurs parsing the parameter value
	 */
	public Resource getResource(String name)
		throws BadRequestException, RepositoryException
	{
		Value value = getValue(name);
		if (value == null || value instanceof Resource) {
			return (Resource)value;
		}
		throw new BadRequestException("Not a BNode or URI: " + value);
	}

	/**
	 * Gets a map of the all parameters with values, also caching them in this
	 * {@link WorkbenchRequest}.
	 * 
	 * @return a map of all parameters with values
	 */
	public Map<String, String> getSingleParameterMap() {
		@SuppressWarnings("unchecked")
		Map<String, String[]> map = super.getParameterMap();
		Map<String, String> parameters = new HashMap<String, String>(map.size());
		for (String name : map.keySet()) {
			if (isParameterPresent(name)) {
				parameters.put(name, getParameter(name));
			}
		}
		if (this.parameters != null) {
			parameters.putAll(this.parameters);
		}
		return parameters;
	}

	/**
	 * Gets the value of the 'type' parameter.
	 * 
	 * @return the value of the 'type' parameter
	 */
	public String getTypeParameter() {
		return getParameter("type");
	}

	/**
	 * Gets the URI referred to by the parameter value.
	 * 
	 * @param name
	 *        of the parameter to check
	 * @return the URI, or null if the parameter has no value, is only
	 *         whitespace, or equals "null"
	 * @throws BadRequestException
	 *         if the value doesn't parse as a URI
	 * @throws RepositoryException
	 *         if the name space prefix is not resolvable
	 */
	public URI getURI(String name)
		throws BadRequestException, RepositoryException
	{
		Value value = getValue(name);
		if (value == null || value instanceof URI) {
			return (URI)value;
		}
		throw new BadRequestException("Not a URI: " + value);
	}

	/**
	 * Gets the URL referred to by the parameter value.
	 * 
	 * @param name
	 *        of the parameter to check
	 * @return the URL
	 * @throws BadRequestException
	 *         if the value doesn't parse as a URL
	 */
	public URL getUrl(String name)
		throws BadRequestException
	{
		String url = getParameter(name);
		try {
			return new URL(url);
		}
		catch (MalformedURLException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
	}

	/**
	 * Gets the {@link org.openrdf.model.Value} referred to by the parameter
	 * value.
	 * 
	 * @param name
	 *        of the parameter to check
	 * @return the value, or null if the parameter has no value, is only
	 *         whitespace, or equals "null"
	 * @throws BadRequestException
	 *         if the value doesn't parse as a URI
	 * @throws RepositoryException
	 *         if any name space prefix is not resolvable
	 */
	public Value getValue(String name)
		throws BadRequestException, RepositoryException
	{
		return decoder.decodeValue(getParameter(name));
	}

	private String firstLine(FileItemStream item)
		throws IOException
	{
		InputStream stream = item.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			return reader.readLine();
		}
		finally {
			reader.close();
		}
	}

	private Map<String, String> getMultipartParameterMap()
		throws RepositoryException, IOException, FileUploadException
	{
		Map<String, String> parameters = new HashMap<String, String>();
		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator iter = upload.getItemIterator(this);
		while (iter.hasNext()) {
			FileItemStream item = iter.next();
			String name = item.getFieldName();
			if ("content".equals(name)) {
				content = item.openStream();
				contentFileName = item.getName();
				break;
			}
			else {
				parameters.put(name, firstLine(item));
			}
		}
		return parameters;
	}

	private Map<String, String> getUrlParameterMap(String url)
		throws UnsupportedEncodingException
	{
		String qry = url.substring(url.indexOf(';') + 1);
		Map<String, String> parameters = new HashMap<String, String>();
		for (String param : qry.split("&")) {
			int idx = param.indexOf('=');
			String name = decode(param.substring(0, idx), UTF_8);
			String value = decode(param.substring(idx + 1), UTF_8);
			parameters.put(name, value);
		}
		return parameters;
	}
}