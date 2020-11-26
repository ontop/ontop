package it.unibz.inf.ontop.utils;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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


import java.util.Collection;

/**
 * A utility class for URI and BNode templates
 *
 * @author xiao
 */
public class ObjectTemplates {

    private static final String PLACE_HOLDER = "{}";
    private static final int PLACE_HOLDER_LENGTH = PLACE_HOLDER.length();

    /**
     * This method instantiates the input uri template by arguments
     * <p>
     * Example:
     * <p>
     * If {@code args = ["A", 1]}, then
     * <p>
     * {@code  URITemplates.format("http://example.org/{}/{}", args)}
     * results {@code "http://example.org/A/1" }
     */
    public static String format(String iriOrBnodeTemplate, Collection<?> args) {
        StringBuilder sb = new StringBuilder();
        int beginIndex = 0;
        for (Object arg : args) {
            int endIndex = iriOrBnodeTemplate.indexOf(PLACE_HOLDER, beginIndex);
            if (endIndex == -1)
                throw new IllegalArgumentException("the number of place holders should be equal to the number of other terms.");

            sb.append(iriOrBnodeTemplate.subSequence(beginIndex, endIndex)).append(arg);
            beginIndex = endIndex + PLACE_HOLDER_LENGTH;
        }
        int endIndex = iriOrBnodeTemplate.indexOf(PLACE_HOLDER, beginIndex);
        if (endIndex != -1)
            throw new IllegalArgumentException("the number of place holders should be equal to the number of other terms.");

        sb.append(iriOrBnodeTemplate.substring(beginIndex));
        return sb.toString();
    }



}
