package com.mycompany.twitter4jfetch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClient {

    private String CONSUMER_KEY;
    private String CONSUMER_KEY_SECRET;

    protected static Map<String, Integer> termCollection = new HashMap<String, Integer>();

    private static ArrayList<String> input = new ArrayList<String>();

    protected static File stopWordFile = new File("stopWords.csv");
    protected static List<String> stopWords = new ArrayList();

    public TwitterClient(String KEY, String KEY_SECRET) {
        CONSUMER_KEY = KEY;
        CONSUMER_KEY_SECRET = KEY_SECRET;
    }

    public static void FileReader(File toRead) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(toRead));

        if (toRead.getName().equals("stopWords.csv")) {
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] sw = line.split(",");
                for (int i = 0; i < sw.length; i++) {
                    stopWords.add(sw[i]);
                }
            }

            for (String s : stopWords) {

            }
        }
    }

    public void startSeachAPI(String qstr) throws TwitterException, IOException {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_KEY_SECRET)
                .setApplicationOnlyAuthEnabled(true);
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        OAuth2Token token = twitter.getOAuth2Token();
        if (token != null) {
            System.out.println("Token Type  : " + token.getTokenType());
            System.out.println("Access Token: " + token.getAccessToken());
        }
        Query query = new Query(qstr);
        query.setCount(1000);
        //long id = (long) 422204137108168705;
        //result.getSinceId();
        QueryResult result = twitter.search(query);
        for (Status status : result.getTweets()) {
            System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
            System.out.println("Retweets: " + status.getRetweetCount() + " | Favourites: " + status.getFavoriteCount());

            if (status.getGeoLocation() != null) {
                System.out.println("Geo Location:");
                System.out.println("(" + status.getGeoLocation().getLatitude() + "," + status.getGeoLocation().getLongitude() + ")");
            }

            // Feed tweets and username into map
            input.add(status.getText());
        }

    }

    public static void renderWordFreqTable() {
        Map<String, Integer> output = new HashMap<String, Integer>();

        for (String tweet : input) {
            String[] words = tweet.trim().split("\\s+");

            for (String word : words) {
                Integer terms = output.get(word);
                if (terms == null) {
                    // terms = new HashMap<String,Integer>();
                    output.put(word, 1);
                } else {
                    output.put(word, terms + 1);
                }
            }
        }
        
        termCollection = removeStopWords(output);
        //return output;
        //System.out.println("Unordered terms: \n" + output);
        //System.out.println("Ordered terms: \n" + orderTerms(output).toString());

    }

    public static Map<String, Integer> removeStopWords(Map toRemoveFrom) {
        Iterator<Map.Entry<String, Integer>> tTFIterator = toRemoveFrom.entrySet().iterator();

        while (tTFIterator.hasNext()) {
            Map.Entry<String, Integer> entry = tTFIterator.next();
            String key = entry.getKey();
                if (stopWords.contains(key.toLowerCase())) {
                    tTFIterator.remove();
                }
        }
        //Map<String, Integer> toReturn = toRemoveFrom;
        return toRemoveFrom;
    }

    public static Map getTermsOver(int n, Map map) {
        System.out.println("Terms over " + n + ":\n");

        Iterator<Map.Entry<String, Integer>> entryIter = map.entrySet().iterator();
        Map<String, Integer> toReturn = new HashMap<String, Integer>();
        //System.out.println("Terms over 100:\n");

        while (entryIter.hasNext()) {
            Map.Entry<String, Integer> entry = entryIter.next();
            String key = entry.getKey();
            int value = entry.getValue().intValue();

            //System.out.println(value);
            if (value > n) { //If term is mentioned more than 100 times
                System.out.println(key + ": " + value);
                toReturn.put(key, value);
            }
        }

        return toReturn;
    }

    public static Map orderTerms(Map terms) {

        // Divide keys and values
        List keys = new ArrayList(terms.keySet());
        List values = new ArrayList(terms.values());
        Map<String, Integer> toReturn = new HashMap<String, Integer>();

        // Sort lists of keys and values
        Collections.sort(keys);
        Collections.sort(values);

        Iterator valIt = values.iterator();
        while (valIt.hasNext()) {
            Object val = valIt.next();
            Iterator keyIt = keys.iterator();
            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String toCompare1 = terms.get(key).toString();
                String toCompare2 = val.toString();

                // Match key and value
                if (toCompare1.equals(toCompare2)) {
                    terms.remove(key);
                    keys.remove(key);
                    toReturn.put((String) key, (Integer) val);
                    break;
                }
            }

        }
        return toReturn;
    }

    public void getUserTimeLine(String user) throws TwitterException {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_KEY_SECRET)
                .setApplicationOnlyAuthEnabled(true);
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        OAuth2Token token = twitter.getOAuth2Token();
        if (token != null) {
            System.out.println("Token Type  : " + token.getTokenType());
            System.out.println("Access Token: " + token.getAccessToken());
        }
        List<Status> statuses;
        statuses = twitter.getUserTimeline(user);
        System.out.println("Showing @" + user + "'s user timeline.");
        for (Status status : statuses) {
            System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
        }
    }
}
