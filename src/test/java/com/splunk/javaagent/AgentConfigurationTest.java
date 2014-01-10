package com.splunk.javaagent;

import com.splunk.javaagent.AgentConfiguration;
import com.splunk.javaagent.trace.FilterListItem;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

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
public class AgentConfigurationTest {
    @Test
    public void noWhitelist() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("key=value"));

        Assert.assertArrayEquals(
                new FilterListItem[] {},
                properties.getWhitelist().toArray(new FilterListItem[] {})
        );
    }

    @Test
    public void oneEntryWhitelist() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.whitelist=com.example.nothing"));

        Assert.assertArrayEquals(
                new FilterListItem[] { new FilterListItem("com.example.nothing") },
                properties.getWhitelist().toArray(new FilterListItem[] {})
        );
    }

    @Test
    public void manyEntryWhitelist() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.whitelist=com.example.first,com.example.second," +
                "com.oracle.java.internal,com.boris.meep.*:hilda"));

        Assert.assertArrayEquals(
                new FilterListItem[] {
                        new FilterListItem("com.example.first"),
                        new FilterListItem("com.example.second"),
                        new FilterListItem("com.oracle.java.internal"),
                        new FilterListItem("com.boris.meep.*", "hilda")
                },
                properties.getWhitelist().toArray(new FilterListItem[] {})
        );
    }

    @Test(expected=IOException.class)
    public void invalidWhitelist() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.whitelist=!vasdfsf.?%!"));
    }


    // Blacklist
    @Test
    public void noBlacklist() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("key=value"));

        Assert.assertArrayEquals(
                new FilterListItem[] {},
                properties.getBlacklist().toArray(new FilterListItem[] {})
        );
    }

    @Test
    public void oneEntryBlacklist() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.blacklist=com.example.nothing"));

        Assert.assertArrayEquals(
                new FilterListItem[] { new FilterListItem("com.example.nothing") },
                properties.getBlacklist().toArray(new FilterListItem[] {})
        );
    }

    @Test
    public void manyEntryBlacklist() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.blacklist=com.example.first,com.example.second," +
                "com.oracle.java.internal,com.boris.meep.*:hilda"));

        Assert.assertArrayEquals(
                new FilterListItem[] {
                        new FilterListItem("com.example.first"),
                        new FilterListItem("com.example.second"),
                        new FilterListItem("com.oracle.java.internal"),
                        new FilterListItem("com.boris.meep.*", "hilda")
                },
                properties.getBlacklist().toArray(new FilterListItem[] {})
        );
    }

    @Test(expected=IOException.class)
    public void invalidBlacklist() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.blacklist=!43441aafsdf?"));
    }

    @Test
    public void missingAgentAppName() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("key=value"));

        Assert.assertEquals("", properties.getAppName());
    }

    @Test
    public void presentAgentAppName() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("agent.app.name=\u2314boris__ hi"));

        Assert.assertEquals("\u2314boris__ hi", properties.getAppName());
    }

    @Test
    public void missingAgentInstance() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("key=value"));

        Assert.assertEquals("", properties.getAppInstance());
    }

    @Test
    public void presentAgentInstance() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("agent.app.instance=\u2314boris__ hi"));

        Assert.assertEquals("\u2314boris__ hi", properties.getAppInstance());
    }

    @Test
    public void missingUserEventTags() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("key=value"));

        Assert.assertEquals(0, properties.getUserEventTags().size());
    }

    @Test
    public void oneUserEventTag() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("agent.userEventTags=boris=hilda"));

        Assert.assertEquals(1, properties.getUserEventTags().size());
        Assert.assertTrue(properties.getUserEventTags().containsKey("boris"));
        Assert.assertEquals("hilda", properties.getUserEventTags().get("boris"));
    }

    @Test
    public void manyUserEventTags() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("agent.userEventTags=boris=hilda,meep=\u2314!!ab,grok=pizza"));

        Assert.assertEquals(3, properties.getUserEventTags().size());

        Assert.assertTrue(properties.getUserEventTags().containsKey("boris"));
        Assert.assertEquals("hilda", properties.getUserEventTags().get("boris"));

        Assert.assertTrue(properties.getUserEventTags().containsKey("meep"));
        Assert.assertEquals("\u2314!!ab", properties.getUserEventTags().get("meep"));

        Assert.assertTrue(properties.getUserEventTags().containsKey("grok"));
        Assert.assertEquals("pizza", properties.getUserEventTags().get("grok"));
    }

    @Test
    public void missingClassLoaded() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("key=value"));

        Assert.assertTrue(properties.traceClassLoaded());
    }

    @Test
    public void classLoadedFalse() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.classLoaded=false"));

        Assert.assertFalse(properties.traceClassLoaded());
    }

    @Test
    public void classLoadedTrue() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.classLoaded=true"));

        Assert.assertTrue(properties.traceClassLoaded());
    }

    @Test(expected=IOException.class)
    public void classLoadedInvalid() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.classLoaded=%$#afaf a"));
    }

    @Test
    public void methodEnteredMissing() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("key=value"));

        Assert.assertTrue(properties.traceMethodEntered());
    }

    @Test
    public void methodEnteredTrue() throws  IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.methodEntered=true"));

        Assert.assertTrue(properties.traceMethodEntered());
    }

    @Test
    public void methodEnteredFalse() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.methodEntered=false"));

        Assert.assertFalse(properties.traceMethodEntered());
    }

    @Test(expected=IOException.class)
    public void methodEnteredInvalid() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.methodEntered=%#@A"));
    }


    @Test
    public void methodExitedMissing() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("key=value"));

        Assert.assertTrue(properties.traceMethodExited());
    }

    @Test
    public void methodExitedTrue() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.methodExited=true"));

        Assert.assertTrue(properties.traceMethodExited());
    }

    @Test
    public void methodExitedFalse() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.methodExited=false"));

        Assert.assertFalse(properties.traceMethodExited());
    }

    @Test(expected=IOException.class)
    public void methodExitedInvalid() throws IOException {
        AgentConfiguration properties = new AgentConfiguration();
        properties.load(new StringReader("trace.methodExited=%AF2!!"));
    }

    @Test
    public void traceErrorsMissing() throws IOException {
        AgentConfiguration configuration = new AgentConfiguration();
        configuration.load(new StringReader("key=value"));

        Assert.assertTrue(configuration.traceErrors());
    }

    @Test
    public void traceErrorsTrue() throws IOException {
        AgentConfiguration configuration = new AgentConfiguration();
        configuration.load(new StringReader("trace.errors=true"));

        Assert.assertTrue(configuration.traceErrors());
    }

    @Test
    public void traceErrorsFalse() throws IOException {
        AgentConfiguration configuration = new AgentConfiguration();
        configuration.load(new StringReader("trace.errors=false"));

        Assert.assertFalse(configuration.traceErrors());
    }

    @Test(expected=IOException.class)
    public void traceErrorsInvalid() throws IOException {
        AgentConfiguration configuration = new AgentConfiguration();
        configuration.load(new StringReader("trace.errors=$#!@$#@!"));
    }



}