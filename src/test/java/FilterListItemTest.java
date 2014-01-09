import com.splunk.javaagent.trace.FilterListItem;
import junit.framework.TestCase;
import org.junit.Test;

import java.text.ParseException;

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
public class FilterListItemTest extends TestCase {
    public void assertRaises(Class c, Runnable r) {

    }

    @Test(expected=ParseException.class)
    public void testWithInvalidClassAndMethod() throws ParseException {
        FilterListItem.parse("com.splunk.!abc:boris");
    }

    @Test(expected=ParseException.class)
    public void testWithInvalidMethod() throws ParseException {
        FilterListItem.parse("com.splunk.abc:!boris??");
    }

    @Test
    public void testWithInvalidClassAlone() throws ParseException {
        FilterListItem.parse("com.splunk.!abc");
    }

    @Test
    public void testWithClassAlone() {
        FilterListItem f = new FilterListItem("com.splunk.dev.MyClass");
        assertEquals("com.splunk.dev.MyClass", f.getClassName());
        assertFalse(f.hasMethodName());
        assertNull(f.getMethodName());
    }

    @Test
    public void testWithWildcardClassAlone() {
        FilterListItem f = new FilterListItem("com.splunk.dev.*");
        assertEquals("com.splunk.dev.*", f.getClassName());
        assertFalse(f.hasMethodName());
        assertNull(f.getMethodName());
    }

    @Test
    public void testWithClassAndMethod() {
        FilterListItem f = new FilterListItem("com.splunk.dev.MyClass:myMethod");
        assertEquals("com.splunk.dev.MyClass", f.getClassName());
        assertTrue(f.hasMethodName());
        assertEquals("myMethod", f.getMethodName());
    }

    @Test
    public void testWithWildcardClassAndMethod() {
        FilterListItem f = new FilterListItem("com.splunk.dev.*:myMethod");
        assertEquals("com.splunk.dev.*", f.getClassName());
        assertTrue(f.hasMethodName());
        assertEquals("myMethod", f.getMethodName());
    }
}
