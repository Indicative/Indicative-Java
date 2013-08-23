/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * A fairly basic REST client for posting events to the Indicative Endpoint.
 *
 * Nonblocking, asynchronous... Shouldn't break your app.
 *
 * Usage: Indicative.event("your-Api-Key",
 * "Registration").uniqueId("user47").addProperty("name","value").done();
 *
 * Note: You MUST call done() at the end...
 *
 */
public class Indicative {

    private static final Logger LOG = Logger.getLogger(Indicative.class.getName());
    /*
     * The number of threads to use when POSTing to the endpoint
     */
    private static final int THREADS = 5;
    private static ExecutorService pool = Executors.newFixedThreadPool(THREADS);
    private static final String REST_ENDPOINT_URL = "http://api.skunkalytics.com/service/event";
    /*
     * Enable this to see some basic details printed to the default logger
     */
    private static final boolean DEBUG = false;

    private static class PostThread implements Runnable {

        Indicative event;

        private PostThread(Indicative event) {
            this.event = event;
        }

        @Override
        public void run() {
            StringBuilder json = new StringBuilder();

            json.append("{ ");
            json.append("\"projectId\" : \"").append(escape(event.apiKey)).append("\", ");
            if (event.eventUniqueId != null) {
                json.append("\"eventUniqueId\" :  \"").append(escape(event.eventUniqueId)).append("\", ");
            }
            json.append("\"eventName\" : \"").append(escape(event.eventName)).append("\", ");
            json.append("\"eventTime\" : ").append(event.eventTime).append(", ");
            json.append("\"properties\" : { ");

            if (event.properties != null) {
                Iterator<Entry<String, String>> it = event.properties.entrySet().iterator();
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

            sendPost(json.toString());
        }

        private void sendPost(String body) {
            HttpURLConnection con = null;
            DataOutputStream wr = null;
            try {
                URL url = new URL(REST_ENDPOINT_URL);
                con = (HttpURLConnection) url.openConnection();

                //add reuqest header
                con.setRequestMethod("POST");
                con.addRequestProperty("Content-Type", "application/json");
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

    public void done() {
        pool.execute(new PostThread(this));
    }

    public static Indicative event(String apiKey, String eventName) {
        return new Indicative(apiKey, eventName);
    }

    protected Indicative(String apiKey, String eventName) {
        this.apiKey = apiKey;
        this.eventName = eventName;
        this.eventTime = System.currentTimeMillis();

    }

    public Indicative addEventTime(long eventTime) {
        this.eventTime = eventTime;
        return this;
    }

    public Indicative addProperty(String name, String value) {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        properties.put(name, value);
        return this;
    }

    protected Indicative uniqueId(String eventUniqueId) {
        this.eventUniqueId = eventUniqueId;
        return this;
    }
    String apiKey;
    String eventName;
    long eventTime;
    String eventUniqueId;
    Map<String, String> properties;

    /**
     * " => \" , \ => \\
     *
     * @param s
     * @return
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
}
