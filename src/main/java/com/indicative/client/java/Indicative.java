package com.indicative.client.java;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A REST client for posting events to the Indicative Endpoint.
 *
 * Usage:
 * Indicative.apiKey("Your-Api-Key-Goes-Here");
 * Indicative.event("Registration").uniqueId("user47").addProperty("name","value").done();
 *
 * Note: You MUST call done() at the end...
 *
 */
public class Indicative {

    /**
     * The API key associated with your project. Use different API keys for your
     * development and production environments.
     */
    private static String API_KEY = "Your-Api-Key-Goes-Here";
    
    /**
     * Enable this to see some basic details printed to the default logger
     */
    private static final boolean DEBUG = false;

    /**
     * A class used to asynchronously post events to the Indicative API
     * endpoint.
     */
    private static class PostThread implements Runnable {

        Indicative event;

        /**
         * A constructor that sets the event for the PostThread to send.
         *
         * @param event The event to send to the Indicative API endpoint.
         */
        private PostThread(Indicative event) {
            this.event = event;
        }

        /**
         * Creates a JSON representation of the event and sends it to the
         * Indicative API endpoint.
         */
        @Override
        public void run() {
            sendPost(event.toJson());
        }

        /**
         * Sends the event to the Indicative API endpoint via an HTTP POST.
         *
         * @param body
         */
        private void sendPost(String body) {
            HttpURLConnection con = null;
            DataOutputStream wr = null;
            try {
                URL url = new URL(REST_ENDPOINT_URL);
                con = (HttpURLConnection) url.openConnection();

                // Add request header
                con.setRequestMethod("POST");
                con.setRequestProperty("Accept-Charset", "UTF-8");
                con.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes("UTF-8").length));

                // Send post request
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setInstanceFollowRedirects(false);
                con.setUseCaches(false);

                wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(body);
                wr.flush();
                wr.close();
                if (DEBUG) {
                    LOG.info(body);
                }
                int responseCode = con.getResponseCode();
                if (DEBUG) {
                    LOG.log(Level.INFO, "Response Code : {0}", responseCode);
                }
                if (responseCode != 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();
                    LOG.log(Level.SEVERE, response.toString());

                }
            } catch (MalformedURLException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                if (con != null) {
                    try {
                        con.disconnect();
                    } catch (Exception ex) {
                    }
                }
                if (wr != null) {
                    try {
                        wr.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    /**
     * Creates a new Thread to asynchronously post the event. This MUST be
     * called once you're done building the event. Otherwise, the event will not
     * be submitted to the API.
     */
    public void done() {
        pool.execute(new PostThread(this));
    }

    public static void apiKey(String apiKey) {
        Indicative.API_KEY = apiKey;
    }
    
    /**
     * Instantiates a new Indicative object and initializes it with the name of
     * your event.
     *
     * @param eventName The name of your event.
     * @return The newly created Indicative object.
     */
    public static Indicative event(String eventName) {
        return new Indicative(eventName);
    }
    
    /**
     * A constructor that sets the initial values for the Indicative object's
     * apiKey, eventName, and eventTime fields.
     *
     * @param eventName The name of your event.
     */
    protected Indicative(String eventName) {
        this(eventName, null);
    }

    /**
     * A constructor that sets the initial values for the Indicative object's
     * apiKey, eventName, and eventTime fields.
     *
     * @param eventName The name of your event.
     * @param apiKey The apiKey you want to use
     */
    protected Indicative(String eventName, String apiKey) {
        if (apiKey == null) {
            this.apiKey = API_KEY;
        } else {
            this.apiKey = apiKey;
        }
        
        this.eventName = eventName;
        this.eventTime = System.currentTimeMillis();

    }
    
    /**
     * Sets the Indicative object's eventTime field.
     *
     * @param eventTime The UNIX timestamp (in milliseconds) when your event
     * occurred.
     * @return The modified Indicative object.
     */
    public Indicative addEventTime(long eventTime) {
        this.eventTime = eventTime;
        return this;
    }

    /**
     * Adds a property name/value pair to the Indicative object's Map of
     * properties.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     * @return The modified Indicative object.
     */
    public Indicative addProperty(String name, String value) {
        properties.put(name, value);
        return this;
    }

    /**
     * Adds a property name/value pair to the Indicative object's Map of
     * properties.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     * @return The modified Indicative object.
     */
    public Indicative addProperty(String name, int value) {
        return addProperty(name, String.valueOf((Object) value));
    }

    /**
     * Adds a property name/value pair to the Indicative object's Map of
     * properties.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     * @return The modified Indicative object.
     */
    public Indicative addProperty(String name, long value) {
        return addProperty(name, String.valueOf((Object) value));
    }

    /**
     * Adds a property name/value pair to the Indicative object's Map of
     * properties.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     * @return The modified Indicative object.
     */
    public Indicative addProperty(String name, float value) {
        return addProperty(name, String.valueOf((Object) value));
    }

    /**
     * Adds a property name/value pair to the Indicative object's Map of
     * properties.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     * @return The modified Indicative object.
     */
    public Indicative addProperty(String name, double value) {
        return addProperty(name, String.valueOf((Object) value));
    }

    /**
     * Adds a property name/value pair to the Indicative object's Map of
     * properties.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     * @return The modified Indicative object.
     */
    public Indicative addProperty(String name, boolean value) {
        return addProperty(name, String.valueOf((Object) value));
    }

    /**
     * Adds a Map of property name/value pairs to the Indicative object's Map of
     * properties.
     *
     * @param propertyMap A map of Strings representing property names and
     * values.
     * @return The modified Indicative object.
     */
    public Indicative addProperties(Map<String, String> propertyMap) {
        if (propertyMap != null) {
            properties.putAll(propertyMap);
        }
        return this;
    }

    /**
     * Adds the user's unique identifier to the Indicative object.
     *
     * @param eventUniqueId The unique identifier for the user associated with
     * the event.
     * @return The modified Indicative object.
     */
    public Indicative uniqueId(String eventUniqueId) {
        this.eventUniqueId = eventUniqueId;
        return this;
    }
    
    String apiKey;
    String eventName;
    long eventTime;
    String eventUniqueId;
    Map<String, String> properties = new HashMap<String, String>();

    /**
     * Serializes the event to a JSON String.
     *
     * @return The JSON representation of the event.
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();

        json.append("{ ");
        json.append("\"apiKey\" : \"").append(escape(this.apiKey)).append("\", ");
        if (this.eventUniqueId != null) {
            json.append("\"eventUniqueId\" : \"").append(escape(this.eventUniqueId)).append("\", ");
        }
        json.append("\"eventName\" : \"").append(escape(this.eventName)).append("\", ");
        json.append("\"eventTime\" : ").append(this.eventTime).append(", ");
        json.append("\"properties\" : { ");

        if (this.properties != null) {
            Iterator<Entry<String, String>> it = this.properties.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> property = it.next();
                json.append("\"").append(escape(property.getKey())).append("\" : \"").append(escape(property.getValue())).append("\"");
                if (it.hasNext()) {
                    json.append(",");
                }

            }
        }
        json.append(" }");
        json.append("}");

        return json.toString();
    }

    /**
     * Escapes special characters in a String with a backslash character for use
     * in the JSON representation of an event. For example: the character "
     * becomes \" and \ becomes \\
     *
     * @param s The original String.
     * @return The String with its special characters escaped.
     */
    public static String escape(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\u0085': // Next Line
                    sb.append("\\u0085");
                    break;
                case '\u2028': // Line Separator
                    sb.append("\\u2028");
                    break;
                case '\u2029': // Paragraph Separator
                    sb.append("\\u2029");
                    break;
                default:
                    if (ch >= '\u0000' && ch <= '\u001F') {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
        }//for
        return sb.toString();
    }
    
    /**
     * An example of the kind of method you should create to easily add groups
     * of properties to every event.  This method should take as a parameter an 
     * Object representing your user, and add certain properties based on that users's
     * attributes.
     */
    
    /**
     public Indicative addCommonProperties(UsersEntity user){
        properties.add("Gender", user.getGender()); 
        properties.add("Age", user.getAge());
     
        return this; 
     }
     
     */
    
    private static final Logger LOG = Logger.getLogger(Indicative.class.getName());
    /**
     * The number of threads to use when POSTing to the endpoint
     */
    private static final int THREADS = 5;
    private static ExecutorService pool = Executors.newFixedThreadPool(THREADS);
    private static final String REST_ENDPOINT_URL = "https://api.indicative.com/service/event";
}
