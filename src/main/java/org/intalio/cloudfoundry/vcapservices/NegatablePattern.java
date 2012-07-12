/*
 * Copyright (c) 2011 Intalio Inc
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.intalio.cloudfoundry.vcapservices;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intalio.cloudfoundry.vcapservices.impl.VCapService;

/**
 * Simple wrapper class for a regexp pattern to keep track of 
 * whether we want a match or a mismatch
 */
public class NegatablePattern {
	
	private final Pattern _pattern;
	private final boolean _isNegated;

	/**
	 * @param stringOrRegexpStr If it starts and ends with '/' then makes this string into a Regexp Pattern;
	 * Other wrap it into a regexp for an exact match. If it starts with '!' make it a negated pattern.
	 * @return
	 */
	private static NegatablePattern asRegexp(String stringOrRegexpStr) {
		stringOrRegexpStr = VCapService.resolvePropertyValue(stringOrRegexpStr);
		boolean isNegated = false;
		if (stringOrRegexpStr.startsWith("!")) {
			isNegated = true;
			stringOrRegexpStr = stringOrRegexpStr.substring(1);
		}
		if (stringOrRegexpStr.startsWith("/") && stringOrRegexpStr.endsWith("/")) {
			String regex = stringOrRegexpStr.substring(1, stringOrRegexpStr.length() -1);
			return new NegatablePattern(Pattern.compile(regex), isNegated);
		} else {
			return new NegatablePattern(Pattern.compile(Pattern.quote(stringOrRegexpStr)), isNegated);
		}
	}

	/**
	 * Resolves a system-property or env-variable with the ${KEY,defult-value} notation.
	 * If the string starts with a '!' the pattern is negated.
	 * If the remaining of the string starts and finishes with '/' then make a regexp out of it.
	 * Otherwise makes a literal match regexp.
	 * @param stringOrRegexpStr
	 */
	public NegatablePattern(String stringOrRegexpStr) {
		stringOrRegexpStr = VCapService.resolvePropertyValue(stringOrRegexpStr);
		if (stringOrRegexpStr.startsWith("!")) {
			_isNegated = true;
			stringOrRegexpStr = stringOrRegexpStr.substring(1);
		} else {
			_isNegated = false;
		}
		if (stringOrRegexpStr.startsWith("/") && stringOrRegexpStr.endsWith("/")) {
			String regex = stringOrRegexpStr.substring(1, stringOrRegexpStr.length() -1);
			_pattern = Pattern.compile(regex);
		} else {
			_pattern = Pattern.compile(Pattern.quote(stringOrRegexpStr));
		}
	}
	
	public NegatablePattern(Pattern pattern, boolean isNegated) {
		_pattern = pattern;
		_isNegated = isNegated;
	}
	
	public boolean matches(String input) {
		Matcher m = _pattern.matcher(input);
		if (m.matches()) return !_isNegated;
		else return _isNegated;
	}
	
	public String toString() {
		return "/" + _pattern.toString() + (_isNegated ? "/ negated" : "/");
	}
}
