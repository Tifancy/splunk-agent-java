package com.splunk.javaagent.trace;

/*
 * Copyright 2014 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FilterListItem represents elements of whitelists and blacklists, containing
 * a fully qualified class name (possibly including wildcards) and an optional
 * method name.
 *
 * You will generally create one by calling the parse method with a String
 * argument and use it as an immutable value thereafter.
 */
public class FilterListItem {

	String className;
	String methodName;

	public FilterListItem(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
	}

    public FilterListItem(String className) {
        this.className = className;
        this.methodName = null;
    }

	public String getClassName() {
		return className;
	}
	public String getMethodName() {
		return methodName;
	}

    public boolean hasMethodName() {
        return methodName != null;
    }

    /**
     * Parse a FilterListItem out of a string.
     *
     * The expected format is:
     *
     *     some.package.ClassName[:methodName]
     *
     * where methodName is optional. So a FilterListItem representing the class java.util.Arrays would
     * be represented as <tt>java.util.Arrays</tt>, while the method asList on that class would be
     * <tt>java.util.Arrays:asList</tt>. You can also use wildcards, as in <tt>java.util.*</tt>.
     *
     * @param s The String to parse from.
     * @return A FilterListItem parsed from s.
     */
    public static FilterListItem parse(String s) throws ParseException {
        if (s.isEmpty())
            throw new ParseException("FilterListItem.parse cannot accept empty strings.", 0);

        String[] components = s.split(":");

        if (components.length == 0)
            throw new ParseException("No components found in string to parse.", 0);
        if (components.length > 2)
            throw new ParseException("FilterListItem.parse expected at most 2 components separated by a " +
                    "colon (found " + components.length + " in \"" + s + "\"", 0);

        String className = components[0];

        // Check that className has only valid characters.
        Pattern p = Pattern.compile("[^A-Za-z0-9-_\\.*]");
        Matcher m = p.matcher(className);
        if (m.find())
            throw new ParseException("Found illegal character in class name \"" + className +
                    "\" at offset " + m.start(), m.start());

        if (components.length == 2) {
            // We have a methodName as well.
            String methodName = components[1];

            p = Pattern.compile("[^A-Za-z0-9-_]");
            m = p.matcher(methodName);

            if (m.find())
                throw new ParseException("Found illegal character in method name \"" + methodName +
                        "\" at offset " + m.start(), m.start());

            return new FilterListItem(className, methodName);
        } else {
            // We have only a className.
            return new FilterListItem(className);
        }
    }

    public static List<FilterListItem> parseMany(String s) throws ParseException {
        List<FilterListItem> l = new ArrayList<FilterListItem>();

        if (s.isEmpty())
            return l;

        for (String entry : s.split(",")) {
            l.add(FilterListItem.parse(entry));
        }
        return l;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FilterListItem)) {
            return false;
        } else {
            FilterListItem that = (FilterListItem)other;
            if (!that.getClassName().equals(getClassName()))
                return false;
            if (that.hasMethodName() != hasMethodName())
                return false;
            if (hasMethodName() && !(that.getMethodName().equals(getMethodName()))) {
                return false;
            }
            return true;
        }
    }
}
