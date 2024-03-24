// SPDX-FileCopyrightText: 2024 Carlos Rica ( jasampler AT gmail DOT com )
// SPDX-License-Identifier: GPL-3.0-or-later
package urlparamiterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

/**
 * Test class for {@link URLParamIterator}.
 */
public class URLParamIteratorTest {

	@Test
	public void multipleParametersURL() {
		URLParamIterator it = new URLParamIterator(
				"http://path?b&c&&a&ccc=&&b=&a=&a&c&=&bb&c&=0&a&b&c=3&&a=1&b=2&aa=111&bbb=22");
		ArrayList<String> keys = new ArrayList<>();
		ArrayList<String> values = new ArrayList<>();
		while (it.next()) {
			if (keys.size() == 0) {
				assertTrue(it.isFirst());
			} else {
				assertFalse(it.isFirst());
			}
			if (keys.size() == 21) {
				assertTrue(it.isLast());
			} else {
				assertFalse(it.isLast());
			}
			keys.add(it.getKey());
			values.add(it.getValue());
		}
		assertFalse(it.next());
		assertFalse(it.next());
		assertEquals(Arrays.asList("b", "c", "",  "a", "ccc","",  "b","a","a", "c", "","bb","c", "", "a", "b", "c","",  "a","b","aa", "bbb"), keys);
		assertEquals(Arrays.asList(null,null,null,null,"",   null,"", "", null,null,"",null,null,"0",null,null,"3",null,"1","2","111","22"), values);
	}

	@Test
	public void oneParameterWithValueURL() {
		URLParamIterator it = new URLParamIterator("http://path?key=val");
		assertTrue(it.next());
		assertEquals("key", it.getKey());
		assertTrue(it.isFirst());
		assertTrue(it.isLast());
		assertEquals("val", it.getValue());
		assertFalse(it.next());
		assertFalse(it.next());
	}

	@Test
	public void oneParameterWithEmptyValueURL() {
		URLParamIterator it = new URLParamIterator("http://path?key=");
		assertTrue(it.next());
		assertEquals("key", it.getKey());
		assertTrue(it.isFirst());
		assertTrue(it.isLast());
		assertEquals("", it.getValue());
		assertFalse(it.next());
		assertFalse(it.next());
	}

	@Test
	public void oneParameterWithoutValueURL() {
		URLParamIterator it = new URLParamIterator("http://path?key");
		assertTrue(it.next());
		assertEquals("key", it.getKey());
		assertTrue(it.isFirst());
		assertTrue(it.isLast());
		assertFalse(it.next());
		assertFalse(it.next());
	}

	@Test
	public void oneEmptyParameterURL() {
		URLParamIterator it = new URLParamIterator("http://path?");
		assertTrue(it.next());
		assertEquals("", it.getKey());
		assertTrue(it.isFirst());
		assertTrue(it.isLast());
		assertFalse(it.next());
		assertFalse(it.next());
	}

	@Test
	public void oneParameterWithEmptyKeyURL() {
		URLParamIterator it = new URLParamIterator("http://path?=val");
		assertTrue(it.next());
		assertEquals("", it.getKey());
		assertTrue(it.isFirst());
		assertTrue(it.isLast());
		assertEquals("val", it.getValue());
		assertFalse(it.next());
		assertFalse(it.next());
	}

	@Test
	public void oneParameterWithEmptyKeyAndEmptyValueURL() {
		URLParamIterator it = new URLParamIterator("http://path?=");
		assertTrue(it.next());
		assertEquals("", it.getKey());
		assertTrue(it.isFirst());
		assertTrue(it.isLast());
		assertEquals("", it.getValue());
		assertFalse(it.next());
		assertFalse(it.next());
	}

	@Test
	public void zeroParametersURL() {
		URLParamIterator it = new URLParamIterator("http://path");
		assertFalse(it.next());
		assertNull(it.getKey());
		assertFalse(it.next());
		assertFalse(it.next());
	}

	@Test(expected = NullPointerException.class)
	public void nullURL() {
		new URLParamIterator(null);
	}

	@Test
	public void modifyURL() {
		URLParamIterator it = new URLParamIterator("http://path?k=v&key1=val1&key2=val2&key3=val3#fragment");
		assertTrue(it.next()); //k=v
		it.remove();
		assertEquals("http://path?key1=val1&key2=val2&key3=val3#fragment", it + "");
		it.insertFirst("key", "val");
		assertEquals("http://path?key=val&key1=val1&key2=val2&key3=val3#fragment", it + "");
		it.insertFirst("key0", "val0");
		assertEquals("http://path?key=val&key0=val0&key1=val1&key2=val2&key3=val3#fragment", it + "");
		it.insertLast("key4", "");
		assertEquals("http://path?key=val&key0=val0&key1=val1&key2=val2&key3=val3&key4=#fragment", it + "");
		it.insertLast("key5", null);
		assertEquals("http://path?key=val&key0=val0&key1=val1&key2=val2&key3=val3&key4=&key5#fragment", it + "");
		it.insertLast("key6", "val6");
		assertEquals("http://path?key=val&key0=val0&key1=val1&key2=val2&key3=val3&key4=&key5&key6=val6#fragment", it + "");
		assertTrue(it.next()); //key1
		it.remove();
		it.insertBefore("key1b", "val1b");
		assertEquals("http://path?key=val&key0=val0&key1b=val1b&key2=val2&key3=val3&key4=&key5&key6=val6#fragment", it + "");
		it.insertAfter(it.getKey(), it.getValue());
		assertEquals("http://path?key=val&key0=val0&key1b=val1b&key1=val1&key2=val2&key3=val3&key4=&key5&key6=val6#fragment", it + "");
		it.insertAfter("key1a", "val1a");
		assertEquals("http://path?key=val&key0=val0&key1b=val1b&key1=val1&key1a=val1a&key2=val2&key3=val3&key4=&key5&key6=val6#fragment", it + "");
		assertTrue(it.next()); //key2
		assertEquals("http://path?key=val&key0=val0&key1b=val1b&key1=val1&key1a=val1a&key2=val2&key3=val3&key4=&key5&key6=val6#fragment", it + "");
		assertTrue(it.next()); //key3
		it.remove();
		assertEquals("http://path?key=val&key0=val0&key1b=val1b&key1=val1&key1a=val1a&key2=val2&key4=&key5&key6=val6#fragment", it + "");
		assertFalse(it.next()); //key3
		assertEquals("http://path?key=val&key0=val0&key1b=val1b&key1=val1&key1a=val1a&key2=val2&key4=&key5&key6=val6#fragment", it + "");
		assertFalse(it.next()); //key3
		assertEquals("http://path?key=val&key0=val0&key1b=val1b&key1=val1&key1a=val1a&key2=val2&key4=&key5&key6=val6#fragment", it + "");
	}

	@Test(expected = IllegalStateException.class)
	public void removeNoParameterSelected() {
		URLParamIterator it = new URLParamIterator("http://path?a=1");
		it.remove();
	}

	@Test(expected = IllegalStateException.class)
	public void insertBeforeNoParameterSelected() {
		URLParamIterator it = new URLParamIterator("http://path?a=1");
		it.insertBefore("v", "k");
	}

	@Test(expected = IllegalStateException.class)
	public void insertAfterNoParameterSelected() {
		URLParamIterator it = new URLParamIterator("http://path?a=1");
		it.insertBefore("v", "k");
	}

	@Test(expected = NullPointerException.class)
	public void insertBeforeNullKey() {
		URLParamIterator it = new URLParamIterator("http://path?a=1");
		assertTrue(it.next());
		it.insertBefore(null, "");
	}

	@Test(expected = NullPointerException.class)
	public void insertAfterNullKey() {
		URLParamIterator it = new URLParamIterator("http://path?a=1");
		assertTrue(it.next());
		it.insertAfter(null, "");
	}

	@Test(expected = NullPointerException.class)
	public void insertFirstNullKey() {
		URLParamIterator it = new URLParamIterator("http://path");
		it.insertFirst(null, "");
	}

	@Test(expected = NullPointerException.class)
	public void insertLastNullKey() {
		URLParamIterator it = new URLParamIterator("http://path");
		it.insertLast(null, "");
	}

	@Test
	public void modifyURLSkipFirstParamsAndRemove() {
		URLParamIterator it = new URLParamIterator("http://path?a=1&b=2&c=3&d=4#fragment");
		assertTrue(it.next()); //1
		assertTrue(it.next()); //2
		assertTrue(it.next()); //3
		it.remove();
		assertEquals("http://path?a=1&b=2&d=4#fragment", it + "");
		it.insertAfter("ca", "3a");
		assertEquals("http://path?a=1&b=2&ca=3a&d=4#fragment", it + "");
		it.insertBefore("cb", "3b");
		assertEquals("http://path?a=1&b=2&cb=3b&ca=3a&d=4#fragment", it + "");
	}

	@Test
	public void modifyURLSkipFirstParamsAndInsertBefore() {
		URLParamIterator it = new URLParamIterator("http://path?a=1&b=2&c=3&d=4#fragment");
		assertTrue(it.next()); //1
		assertTrue(it.next()); //2
		assertTrue(it.next()); //3
		it.insertBefore("cb", "3b");
		assertEquals("http://path?a=1&b=2&cb=3b&c=3&d=4#fragment", it + "");
		it.insertAfter("ca", "3a");
		assertEquals("http://path?a=1&b=2&cb=3b&c=3&ca=3a&d=4#fragment", it + "");
		it.remove();
		assertEquals("http://path?a=1&b=2&cb=3b&ca=3a&d=4#fragment", it + "");
	}

	@Test
	public void modifyURLSkipFirstParamsAndInsertAfter() {
		URLParamIterator it = new URLParamIterator("http://path?a=1&b=2&c=3&d=4#fragment");
		assertTrue(it.next()); //1
		assertTrue(it.next()); //2
		assertTrue(it.next()); //3
		it.insertAfter("ca", "3a");
		assertEquals("http://path?a=1&b=2&c=3&ca=3a&d=4#fragment", it + "");
		it.insertBefore("cb", "3b");
		assertEquals("http://path?a=1&b=2&cb=3b&c=3&ca=3a&d=4#fragment", it + "");
		it.remove();
		assertEquals("http://path?a=1&b=2&cb=3b&ca=3a&d=4#fragment", it + "");
	}

	@Test
	public void modifyURLEmptyParams() {
		URLParamIterator it = new URLParamIterator("http://path?&a&b&c&#fragment");
		assertEquals("http://path?&a&b&c&#fragment", it + "");
		assertTrue(it.next());
		it.insertBefore("", null);
		assertEquals("http://path?&&a&b&c&#fragment", it + "");
		assertTrue(it.next());
		it.insertAfter("", null);
		assertEquals("http://path?&&a&&b&c&#fragment", it + "");
		it.insertFirst("", null);
		assertEquals("http://path?&&&a&&b&c&#fragment", it + "");
		it.insertLast("", null);
		assertEquals("http://path?&&&a&&b&c&&#fragment", it + "");
	}

	@Test
	public void modifyURLInsertFirstAndInsertLast() {
		URLParamIterator it = new URLParamIterator("http://path#fragment");
		assertEquals("http://path#fragment", it + "");
		it.insertLast("l", "1");
		assertEquals("http://path?l=1#fragment", it + "");
		it.insertFirst("f", "1");
		assertEquals("http://path?f=1&l=1#fragment", it + "");
		it.insertFirst("f", "2");
		assertEquals("http://path?f=1&f=2&l=1#fragment", it + "");
		it.insertLast("l", "2");
		assertEquals("http://path?f=1&f=2&l=1&l=2#fragment", it + "");
	}

	@Test
	public void removeNextParamsAndToString() {
		URLParamIterator it = new URLParamIterator("http://path?a=1&b=2&c=3&d=4#fragment");
		assertTrue(it.next()); //1
		it.remove();
		assertTrue(it.next()); //2
		assertTrue(it.next()); //3
		assertEquals("http://path?b=2&c=3&d=4#fragment", it + "");
	}

	@Test
	public void insertBeforeNextParamsAndToString() {
		URLParamIterator it = new URLParamIterator("http://path?a=1&b=2&c=3&d=4#fragment");
		assertTrue(it.next()); //1
		it.insertBefore("ab", "1b");
		assertTrue(it.next()); //2
		assertTrue(it.next()); //3
		assertEquals("http://path?ab=1b&a=1&b=2&c=3&d=4#fragment", it + "");
	}

	@Test
	public void insertAfterNextParamsAndToString() {
		URLParamIterator it = new URLParamIterator("http://path?a=1&b=2&c=3&d=4#fragment");
		assertTrue(it.next()); //1
		it.insertAfter("aa", "1a");
		assertTrue(it.next()); //2
		assertTrue(it.next()); //3
		assertEquals("http://path?a=1&aa=1a&b=2&c=3&d=4#fragment", it + "");
	}

	@Test
	public void example1() {
		URLParamIterator it = new URLParamIterator("AA?k=v&k=&k&=&");
		StringBuilder sb = new StringBuilder();
		while (it.next()) {
			sb.append(it.getKey() + "=" + it.getValue() + "\n");
		}
		assertEquals("k=v\nk=\nk=null\n=\n=null\n", sb.toString());
	}

	@Test
	public void example2() {
		URLParamIterator it = new URLParamIterator("http://site?c=2&d");
		while (it.next()) {
			if (it.isFirst()) {
				it.insertBefore("b", "1");
			}
			if (it.getKey().equals("d") && ! "3".equals(it.getValue())) {
				it.remove();
				it.insertAfter("d", "3");
			}
			if (it.isLast()) {
				it.insertAfter("e", "4");
			}
		}
		it.insertFirst("a", "0");
		it.insertLast("f", "5");
		assertEquals("http://site?a=0&b=1&c=2&d=3&e=4&f=5", it + "");
	}

	@Test
	public void changeParamSeparator() {
		URLParamIterator it = new URLParamIterator("AA?b=2;c=3;d=4#fragment", ';');
		StringBuilder sb = new StringBuilder();
		it.insertLast("k", "e");
		it.insertLast("v", "5");
		it.insertFirst("k", "a");
		it.insertFirst("v", "1");
		while (it.next()) {
			sb.append(it.getKey() + "=" + it.getValue() + "\n");
			it.insertAfter("v", it.getValue());
			it.insertBefore("k", it.getKey());
			it.remove();
		}
		assertEquals("b=2\nc=3\nd=4\n", sb.toString());
		assertEquals("AA?k=a;v=1;k=b;v=2;k=c;v=3;k=d;v=4;k=e;v=5#fragment", it + "");
	}

}

