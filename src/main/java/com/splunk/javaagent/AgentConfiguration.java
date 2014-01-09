package com.splunk.javaagent;

import com.splunk.javaagent.trace.FilterListItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Filter;

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
public class AgentConfiguration extends Properties {
    public void load(InputStream stream) throws IOException {
        super.load(stream);
        try {
            processFields();
        } catch (ParseException pe) {
            throw new IOException(pe.getMessage());
        }
    }

    public void load(Reader reader) throws IOException {
        super.load(reader);
        try {
            processFields();
        } catch (ParseException pe) {
            throw new IOException(pe.getMessage());
        }
    }

    private boolean strictlyParseBoolean(String s) throws ParseException {
        String t = s.toLowerCase();
        if (t.equals("true"))
            return true;
        else if (t.equals("false"))
            return false;
        else
            throw new ParseException("Could not parse a boolean from \"" + s +
                    "\" (should have been true or false)", 0);
    }

    private void processFields() throws ParseException {
        List<FilterListItem> ls;

        ls = FilterListItem.parseMany((String)get("trace.whitelist"));
        put("trace.whitelist", ls);

        ls = FilterListItem.parseMany((String)get("trace.blacklist"));
        put("trace.blacklist", ls);

        HashMap<String, String> userEventTags = new HashMap<String, String>();
        if (!getProperty("agent.userEventTags", "").equals("")) {
            for (String s : getProperty("agent.userEventTags", "").split(",")) {
                String[] components = s.split("=", 2);
                if (components.length < 2)
                    throw new ParseException("In agent.userEventTags, \"" + s + "\" is not of the form key=value", 0);
                userEventTags.put(components[0], components[1]);
            }
        }
        put("agent.userEventTags", userEventTags);

        put("trace.classLoaded", strictlyParseBoolean(getProperty("trace.classLoaded", "true")));
        put("trace.methodEntered", strictlyParseBoolean(getProperty("trace.methodEntered", "true")));
        put("trace.methodExited", strictlyParseBoolean(getProperty("trace.methodExited", "true")));
        put("trace.errors", strictlyParseBoolean(getProperty("trace.errors", "true")));
    }

    public String getAppName() {
        return getProperty("agent.app.name", "");
    }

    public String getAppInstance() {
        return getProperty("agent.app.instance", "");
    }

    public List<FilterListItem> getBlacklist() {
        return (List<FilterListItem>)get("trace.blacklist");
    }

    public List<FilterListItem> getWhitelist() {
        return (List<FilterListItem>)get("trace.whitelist");
    }

    public Map<String, String> getUserEventTags() {
        return (Map<String, String>)get("agent.userEventTags");
    }

    public boolean traceClassLoaded() {
        return (Boolean)get("trace.classLoaded");
    }

    public boolean traceErrors() {
        return (Boolean)get("trace.errors");
    }

    public boolean traceMethodEntered() {
        return (Boolean)get("trace.methodEntered");
    }

    public boolean traceMethodExited() {
        return (Boolean)get("trace.methodExited");
    }
}
