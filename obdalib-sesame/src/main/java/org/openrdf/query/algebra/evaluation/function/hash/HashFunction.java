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
package org.openrdf.query.algebra.evaluation.function.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * Abstract hash function
 * 
 * @author jeen
 */
public abstract class HashFunction implements Function {

	protected String hash(String text, String algorithm)
		throws NoSuchAlgorithmException
	{
		byte[] hash = MessageDigest.getInstance(algorithm).digest(text.getBytes());
		BigInteger bi = new BigInteger(1, hash);
		String result = bi.toString(16);
		if (result.length() % 2 != 0) {
			return "0" + result;
		}
		return result;
	}

	public abstract Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException;

}
