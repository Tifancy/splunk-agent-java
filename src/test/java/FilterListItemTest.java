import com.splunk.javaagent.trace.FilterListItem;
import org.junit.Assert;
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
public class FilterListItemTest {
    public void assertRaises(Class c, Runnable r) {

    }

    @Test(expected=ParseException.class)
    public void withInvalidClassAndMethod() throws ParseException {
        FilterListItem.parse("com.splunk.!abc:boris");
    }

    @Test(expected=ParseException.class)
    public void withInvalidMethod() throws ParseException {
        FilterListItem.parse("com.splunk.abc:!boris??");
    }

    @Test(expected=ParseException.class)
    public void withInvalidClassAlone() throws ParseException {
        FilterListItem.parse("com.splunk.!abc");
    }

    @Test
    public void withClassAlone() throws ParseException {
        FilterListItem f = FilterListItem.parse("com.splunk.dev.MyClass");
        Assert.assertEquals("com.splunk.dev.MyClass", f.getClassName());
        Assert.assertFalse(f.hasMethodName());
        Assert.assertNull(f.getMethodName());
    }

    @Test
    public void withWildcardClassAlone() throws ParseException {
        FilterListItem f = FilterListItem.parse("com.splunk.dev.*");
        Assert.assertEquals("com.splunk.dev.*", f.getClassName());
        Assert.assertFalse(f.hasMethodName());
        Assert.assertNull(f.getMethodName());
    }

    @Test
    public void withClassAndMethod() throws ParseException {
        FilterListItem f = FilterListItem.parse("com.splunk.dev.MyClass:myMethod");
        Assert.assertEquals("com.splunk.dev.MyClass", f.getClassName());
        Assert.assertTrue(f.hasMethodName());
        Assert.assertEquals("myMethod", f.getMethodName());
    }

    @Test
    public void withWildcardClassAndMethod() throws ParseException {
        FilterListItem f = FilterListItem.parse("com.splunk.dev.*:myMethod");
        Assert.assertEquals("com.splunk.dev.*", f.getClassName());
        Assert.assertTrue(f.hasMethodName());
        Assert.assertEquals("myMethod", f.getMethodName());
    }
}
