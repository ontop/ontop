package it.unibz.krdb.obda.protege4.utils;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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

public interface OBDAProgressListener {

	public void actionCanceled() throws Exception;

	/**
	 * Set to true if the user has requested the operation to be cancelled, e.g.
	 * by clicking "Cancel"
	 * 
	 * @return
	 */
	public boolean isCancelled();

	/**
	 * Set to true if the user has been shown an error message of the exception leading to 
	 * unsuccessful result. This is used to avoid multiple error messages, and to avoid passing on
	 * null result values
	 * 
	 * @return
	 */
	public boolean isErrorShown();
	
}
