// SPDX-FileCopyrightText: 2024 Carlos Rica ( jasampler AT gmail DOT com )
// SPDX-License-Identifier: GPL-3.0-or-later
package urlparamiterator;

/**
 * Iterator to traverse and optionally modify
 * the query parameters of a given URL like
 * <code>http://site/path&quest;key1=value1&amp;key2=value2</code>.
 *
 * <p>Notes:<ul>
 * <li>This utility does not decode/encode the keys/values so that must be
 * done by the user for getKey()/getValue() and insertXxx() when appropriate.
 * <li>Parameter keys cannot be null, but parameter values will be null when
 * the value separator '=' is not found.
 * <li>If the last parameter is reached, calling again to next() has no effect.
 * <li>Insertions/removals do not affect the iteration order of the initial URL.
 * <li>Each parameter added using any insertion method will be concatenated
 * <i>after</i> the previous parameters added with the same method.
 * <li>Inserting a parameter with a null key will throw a NullPointerException.
 * </ul>
 *
 * <p>Examples:<pre>
 * URLParamIterator it = new URLParamIterator("AA?k=v&amp;k=&amp;k&amp;=&amp;");
 * while (it.next()) {
 *     System.out.println(it.getKey() + "=" + it.getValue());
 * }
 * //prints the lines: k=v, k=, k=null, =, =null
 *
 * URLParamIterator it = new URLParamIterator("http://site?c=2&amp;d");
 * while (it.next()) {
 *     if (it.isFirst()) {
 *         it.insertBefore("b", "1"); //inserts a parameter BEFORE current one
 *     }
 *     if (it.getKey().equals("d") &amp;&amp; ! "3".equals(it.getValue())) {
 *         //to modify a parameter it must be removed and reinserted:
 *         it.remove();
 *         it.insertBefore("d", "3"); //insertAfter() could be used too
 *     }
 *     if (it.isLast()) {
 *         it.insertAfter("e", "4"); //inserts a parameter AFTER current one
 *     }
 * }
 * it.insertFirst("a", "0"); //inserts a parameter at the beginning
 * it.insertLast("f", "5"); //inserts a parameter at the end
 * //it.toString(): "http://site?a=0&amp;b=1&amp;c=2&amp;d=3&amp;e=4&amp;f=5"
 * </pre>
 */
public class URLParamIterator {

	private static final char QUERY_SEP = '?';
	private static final char FRAGMENT_SEP = '#';
	private static final char VALUE_SEP = '=';
	//buffer indexes:
	private static final int ACCUM = 0; //parameters already accumulated
	private static final int BEFORE = 1; //pending to add to ACCUM
	private static final int AFTER = 2; //pending to add to ACCUM
	private static final int FIRST = 3; //added only in toString()
	private static final int LAST = 4; //added only in toString()

	/**
	 * Initial URL supplied for iterating its query parameters.
	 */
	public final String iniURL;

	/**
	 * Separator of the parameters, '&amp;' by default.
	 */
	public final char paramSep;

	/**
	 * Index of the query separator '&quest;'
	 * or the same as {@link #endParams}
	 * if no query separator '&quest;' is found.
	 */
	public final int startParams;

	/**
	 * Index of the fragment separator '#' or the length of the URL
	 * if no fragment separator '#' is found.
	 */
	public final int endParams;

	/** Index of the separator before the current parameter
	 * (it will be at the initial separator '&quest;' in first parameter)
	 * or -1 if next() was not called yet or the URL has no parameters. */
	private int start;

	/** Index after the end of the current parameter
	 * (it will be at the end of the value when a value exists)
	 * or -1 if next() was not called yet or the URL has no parameters. */
	private int end;

	/** Index after the end of the key of the current parameter
	 * (it will be at the value separator '=' when this separator exists)
	 * or -1 if next() was not called yet or the URL has no parameters. */
	private int endKey;

	private String keyCache;
	private String valueCache;
	private String strCache; //cache for toString()
	private StringBuilder[] buffers; //buffers to save modifications
	private LastModParam mp; //instanced only if a parameter was modified

	/** Saves the limits and removed flag of the last modified position. */
	private static class LastModParam {
		public int start; //start of last modified param not accumulated
		public int end; //end of last modified param not accumulated
		public boolean removed; //last param not accumulated was removed
	}

	/**
	 * Creates an iterator to traverse and optionally modify
	 * the query parameters of a given URL like
	 * <code>http://site/path&quest;key1=value1&amp;key2=value2</code>.
	 *
	 * @throws NullPointerException if the given URL is null
	 */
	public URLParamIterator(String iniURL) {
		this(iniURL, '&'); //default parameter separator
	}

	/**
	 * Creates an iterator to traverse and optionally modify
	 * the query parameters of a given URL but using
	 * the given parameter separator instead of the default '&amp;'.
	 *
	 * @throws NullPointerException if the given URL is null
	 */
	public URLParamIterator(String iniURL, char paramSep) {
		if (iniURL == null) {
			throw new NullPointerException("URL must not be null");
		}
		this.iniURL = strCache = iniURL;
		this.paramSep = paramSep;
		endParams = substrIndexOf(iniURL, 0, iniURL.length(),
								FRAGMENT_SEP);
		startParams = substrIndexOf(iniURL, 0, endParams, QUERY_SEP);
		start = end = endKey = -1;
	}

	/**
	 * Searches for the next parameter in the URL, and if it is found
	 * returns true saving its key and position, otherwise returns false
	 * not changing the previously saved key and position.
	 */
	public boolean next() {
		//gets the index of ? or previous_& or endParams:
		int idx = start == -1 ? startParams : end;
		if (idx < endParams) {
			start = idx;
			end = substrIndexOf(iniURL, idx + 1, endParams,
								paramSep);
			endKey = substrIndexOf(iniURL, idx + 1, end, VALUE_SEP);
			keyCache = valueCache = null;
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the current parameter is the first.
	 */
	public boolean isFirst() {
		return start == startParams;
	}

	/**
	 * Returns true if the current parameter is the last.
	 */
	public boolean isLast() {
		return end == endParams;
	}

	/**
	 * Returns the key of the current parameter
	 * (it can be the empty string "")
	 * or null if next() was not called yet or the URL has no parameters.
	 */
	public String getKey() {
		if (keyCache == null && start != -1) {
			keyCache = substr(iniURL, start + 1, endKey);
		}
		return keyCache;
	}

	/**
	 * Returns the value of the current parameter
	 * or null if no value separator '=' was found,
	 * next() was not called yet or the URL has no parameters.
	 */
	public String getValue() {
		if (valueCache == null && endKey != end) {
			valueCache = substr(iniURL, endKey + 1, end);
		}
		return valueCache;
	}

	/** Provided correct indexes returns "" if the substring is empty. */
	private static String substr(String str, int beginIndex, int endIndex) {
		return beginIndex < endIndex ?
				str.substring(beginIndex, endIndex) : "";
	}

	/** Returns the index of the first occurrence of the given character
	 * in the given substring or the given end index if it is not found. */
	private static int substrIndexOf(String str,
			int beginIndex, int endIndex, char ch) {
		int index = str.indexOf(ch, beginIndex);
		return index != -1 && index < endIndex ? index : endIndex;
	}

	/** Throws an exception if the given parameter key is null. */
	private static void requireNonNullKey(String paramKey) {
		if (paramKey == null) {
			throw new NullPointerException(
					"Parameter key must not be null");
		}
	}

	/**
	 * Removes the current parameter from the modified URL.
	 *
	 * @throws IllegalStateException if no parameter is currently selected
	 */
	public void remove() {
		prepareParamToModify();
		mp.removed = true;
	}

	/**
	 * Inserts the given parameter key and value in the modified URL
	 * <i>before</i> the position of the current parameter,
	 * and after all the previous parameters inserted in the same position.
	 *
	 * @throws IllegalStateException if no parameter is currently selected
	 * @throws NullPointerException if the given parameter key is null
	 */
	public void insertBefore(String paramKey, String paramValue) {
		requireNonNullKey(paramKey);
		prepareParamToModify();
		appendParamToBuffer(BEFORE, paramKey, paramValue);
	}

	/**
	 * Inserts the given parameter key and value in the modified URL
	 * <i>after</i> the position of the current parameter,
	 * but after all the previous parameters inserted in the same position.
	 *
	 * @throws IllegalStateException if no parameter is currently selected
	 * @throws NullPointerException if the given parameter key is null
	 */
	public void insertAfter(String paramKey, String paramValue) {
		requireNonNullKey(paramKey);
		prepareParamToModify();
		appendParamToBuffer(AFTER, paramKey, paramValue);
	}

	/**
	 * Inserts the given parameter key and value in the modified URL
	 * <i>before</i> all the other parameters,
	 * but after all the previous parameters inserted in the same position.
	 *
	 * @throws NullPointerException if the given parameter key is null
	 */
	public void insertFirst(String paramKey, String paramValue) {
		requireNonNullKey(paramKey);
		appendParamToBuffer(FIRST, paramKey, paramValue);
		strCache = null;
	}

	/**
	 * Inserts the given parameter key and value in the modified URL
	 * <i>after</i> all the other parameters,
	 * and after all the previous parameters inserted in the same position.
	 *
	 * @throws NullPointerException if the given parameter key is null
	 */
	public void insertLast(String paramKey, String paramValue) {
		requireNonNullKey(paramKey);
		appendParamToBuffer(LAST, paramKey, paramValue);
		strCache = null;
	}

	/**
	 * Returns the original URL possibly modified by calls to
	 * {@link #insertBefore(String, String)},
	 * {@link #insertAfter(String, String)},
	 * {@link #insertFirst(String, String)},
	 * {@link #insertLast(String, String)}
	 * or {@link #remove()}.
	 */
	@Override
	public String toString() {
		if (strCache != null) {
			return strCache;
		}
		StringBuilder result = new StringBuilder();
		appendBufferWithPath(result, FIRST);
		if (mp != null) {
			appendBufferWithPath(result, ACCUM);
			appendBufferWithPath(result, BEFORE);
			if (! mp.removed) {
				appendPathOrSep(result)
					.append(iniURL, mp.start + 1, mp.end);
			}
			appendBufferWithPath(result, AFTER);
			if (mp.end < endParams) {
				appendPathOrSep(result)
					.append(iniURL, mp.end + 1, endParams);
			}
		}
		appendBufferWithPath(result, LAST);
		if (endParams < iniURL.length()) {
			result.append(iniURL, endParams, iniURL.length());
		}
		strCache = result.toString();
		return strCache;
	}

	/** Common operations for remove(), insertBefore() and insertAfter(). */
	private void prepareParamToModify() {
		if (start == -1) {
			throw new IllegalStateException(
					"No parameter selected");
		}
		if (mp == null) {
			//if the modified parameter is not at the beginning:
			if (startParams < start) {
				getBuffer(ACCUM).append(paramSep)
					.append(iniURL, startParams + 1, start);
			}
			mp = new LastModParam();
		} else if (mp.start < start) {
			//accumulates the last modified parameter:
			accumulateBufferAndClean(BEFORE);
			if (mp.removed) {
				mp.removed = false;
			} else {
				getBuffer(ACCUM).append(paramSep)
					.append(iniURL, mp.start + 1, mp.end);
			}
			accumulateBufferAndClean(AFTER);
			//accumulates the next unmodified parameters, if any:
			if (mp.end < start) {
				getBuffer(ACCUM).append(paramSep)
					.append(iniURL, mp.end + 1, start);
			}
		}
		mp.start = start;
		mp.end = end;
		strCache = null;
	}

	/** Returns true if the specified buffer has parameters in it. */
	private boolean isBufferNotEmpty(int bufIdx) {
		if (buffers == null) {
			return false;
		}
		StringBuilder buf = buffers[bufIdx];
		return buf != null && buf.length() != 0;
	}

	/** Instances and returns the specified buffer to modify it. */
	private StringBuilder getBuffer(int bufIdx) {
		if (buffers == null) {
			buffers = new StringBuilder[5];
		}
		StringBuilder buf = buffers[bufIdx];
		if (buf == null) {
			buf = buffers[bufIdx] = new StringBuilder();
		}
		return buf;
	}

	/** Appends the given key and value to the specified buffer
	 * but always inserting the parameter separator before. */
	private void appendParamToBuffer(int bufIdx, String paramKey,
					String paramValue) {
		StringBuilder buf = getBuffer(bufIdx);
		buf.append(paramSep).append(paramKey);
		if (paramValue != null) {
			buf.append(VALUE_SEP).append(paramValue);
		}
	}

	/** Appends the specified buffer to the ACCUM buffer and cleans it. */
	private void accumulateBufferAndClean(int bufIdx) {
		if (isBufferNotEmpty(bufIdx)) {
			StringBuilder buf = getBuffer(bufIdx);
			getBuffer(ACCUM).append(buf);
			buf.setLength(0);
		}
	}

	/** Appends the given buffer to the result but inserting before
	 * the path of the URL if the result is empty. */
	private void appendBufferWithPath(StringBuilder result, int bufIdx) {
		if (isBufferNotEmpty(bufIdx)) {
			StringBuilder buf = getBuffer(bufIdx);
			appendPathOrSep(result).append(buf, 1, buf.length());
		}
	}

	/** If the given buffer is empty then appends the path of the URL,
	 * otherwise appends the parameter separator. */
	private StringBuilder appendPathOrSep(StringBuilder result) {
		if (result.length() == 0) {
			return result.append(iniURL, 0, startParams)
							.append(QUERY_SEP);
		}
		return result.append(paramSep);
	}

}

