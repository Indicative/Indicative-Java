/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indicative.client.java;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for Indicative's Java client.
 */
public class IndicativeTest {
    
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void addingStringPropertyValueThrowsNoException() {
        Indicative.event("Registration").addProperty("String property", "String");
    }

    @Test
    public void addingIntPropertyValueThrowsNoException() {
        Indicative.event("Registration").addProperty("int property", 23);
    }

    @Test
    public void addingLongPropertyValueThrowsNoException() {
        Indicative.event("Registration").addProperty("long property", 23l);
    }

    @Test
    public void addingFloatPropertyValueThrowsNoException() {
        Indicative.event("Registration").addProperty("float property", 23.0f);
    }

    @Test
    public void addingDoublePropertyValueThrowsNoException() {
        Indicative.event("Registration").addProperty("double property", 23.0d);
    }

    @Test
    public void addingBooleanPropertyValueThrowsNoException() {
        Indicative.event("Registration").addProperty("boolean property", true);
    }

    @Test
    public void addingNullPropertyValueThrowsNoException() {
        Indicative.event("Registration").addProperty("null property", null);
    }

    @Test
    public void addingNullPropertyMapThrowsNoException() {
        Indicative.event("Registration").addProperties(null);
    }

    @Test
    public void addingNullEventNameThrowsNoException() {
        Indicative.event(null);
    }

    @Test
    public void addingNullUniqueIdThrowsNoException() {
        Indicative.event("Registration").uniqueId(null);
    }

    @Test
    public void testJsonSerialization() throws Exception {
        assertThat("Event is correctly serialized to JSON",
                Indicative.event("Registration").uniqueId("user47").addEventTime(1377647905000l).addProperty("name", "value").toJson(),
                is(equalTo("{ \"apiKey\" : \"Your-Api-Key-Goes-Here\", \"eventUniqueId\" : \"user47\", \"eventName\" : \"Registration\", \"eventTime\" : 1377647905000, \"properties\" : { \"name\" : \"value\" }}")));
    }
    
    @Test
    public void testEscapingCharacters() throws Exception {
        assertThat("Special characters are correctly escaped",
                Indicative.event("EventNameWithBackspace\b").uniqueId("uniqueIdWithNewLineChar\n").addEventTime(1377647905000l).addProperty("name", "valueWithTabChar\t").toJson(),
                is(equalTo("{ \"apiKey\" : \"Your-Api-Key-Goes-Here\", \"eventUniqueId\" : \"uniqueIdWithNewLineChar\\n\", \"eventName\" : \"EventNameWithBackspace\\b\", \"eventTime\" : 1377647905000, \"properties\" : { \"name\" : \"valueWithTabChar\\t\" }}")));
    }
}
