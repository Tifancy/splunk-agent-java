package com.splunk.javaagent.trace;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void equalsWorksWithClassAlone() {
        Assert.assertEquals(new FilterListItem("abc"), new FilterListItem("abc"));
    }

    @Test
    public void equalsWorksWithClassAndMethod() {
        Assert.assertEquals(new FilterListItem("abc", "def"), new FilterListItem("abc", "def"));
    }

    @Test
    public void equalsFailsWithDifferentClass() {
        Assert.assertNotEquals(new FilterListItem("abc", "def"), new ArrayList<Integer>());
    }

    @Test
    public void equalsFailsWithDifferentClassname() {
        Assert.assertNotEquals(new FilterListItem("abc", "def"), new FilterListItem("qef", "def"));
    }

    @Test
    public void equalsFailsWithDifferentMethod() {
        Assert.assertNotEquals(new FilterListItem("abc", "def"), new FilterListItem("abc", "qef"));
    }

    @Test
    public void equalsFailsWithAndWithoutMethod() {
        Assert.assertNotEquals(new FilterListItem("abc", "def"), new FilterListItem("abc"));
    }

    @Test
    public void listOfNone() throws ParseException {
        FilterListItem[] expected = new FilterListItem[] {};
        FilterListItem[] found = FilterListItem.parseMany("").toArray(new FilterListItem[] {});
        Assert.assertArrayEquals(expected, found);
    }

    @Test
    public void listOfNull() throws ParseException {
        FilterListItem[] expected = new FilterListItem[] {};
        FilterListItem[] found = FilterListItem.parseMany(null).toArray(new FilterListItem[]{});
        Assert.assertArrayEquals(expected, found);
    }

    @Test
    public void listOfOne() throws ParseException {
        FilterListItem[] expected = new FilterListItem[] {
                new FilterListItem("com.splunk.dev.MyClass")
        };

        FilterListItem[] found = FilterListItem.parseMany("com.splunk.dev.MyClass").toArray(new FilterListItem[] {});

        Assert.assertArrayEquals(expected, found);
    }

    @Test
    public void listOfMany() throws ParseException {
        FilterListItem[] expected = new FilterListItem[] {
                new FilterListItem("com.splunk.dev.MyClass"),
                new FilterListItem("com.boris.*"),
                new FilterListItem("com.agent.Meep", "myMethod")
        };

        FilterListItem[] found = FilterListItem.parseMany(
                "com.splunk.dev.MyClass,com.boris.*,com.agent.Meep:myMethod"
        ).toArray(new FilterListItem[] {});

        Assert.assertArrayEquals(expected, found);
    }

    @Test
    public void listOfOneInvalid() throws ParseException {

    }

    @Test
    public void listOfManyWithOneInvalid() throws ParseException {

    }
}
