package com.mycompany.twitter4jfetch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClient {

    private String CONSUMER_KEY;
    private String CONSUMER_KEY_SECRET;

    /////////////////////////////
    // Term Categories
    protected static Map<String, Tweetails> termCollection = new HashMap<String, Tweetails>();
    protected static Map<String, Tweetails> people = new HashMap<String, Tweetails>();
    protected static Map<String, Tweetails> hashtags = new HashMap<String, Tweetails>();
    protected static Map<String, Tweetails> positiveWords = new HashMap<String, Tweetails>();
    protected static Map<String, Tweetails> negativeWords = new HashMap<String, Tweetails>();

    protected static List<Status> tweetsForTimeScan = new ArrayList<Status>();
    //
    //////////////////////////////

    //private static ArrayList<String> input = new ArrayList<String>();
    private static Map<String, Date> input = new HashMap<String, Date>();

    protected static File stopWordFile = new File("stopWords.csv");
    protected static List<String> stopWords = new ArrayList();
    protected static File positiveFile = new File("positive.csv");
    protected static List<String> positiveRef = new ArrayList();
    protected static File negativeFile = new File("negative.csv");
    protected static List<String> negativeRef = new ArrayList();

    public TwitterClient(String KEY, String KEY_SECRET) {
        CONSUMER_KEY = KEY;
        CONSUMER_KEY_SECRET = KEY_SECRET;
    }

    public static void readFiles() throws FileNotFoundException, IOException {
        // Reading in word files to clean and categorise data
        BufferedReader swBR = new BufferedReader(new FileReader(stopWordFile));
        BufferedReader pwBR = new BufferedReader(new FileReader(positiveFile));
        BufferedReader nwBR = new BufferedReader(new FileReader(negativeFile));

        String line = null;
        while ((line = swBR.readLine()) != null) {
            String[] sw = line.split(",");
            for (int i = 0; i < sw.length; i++) {
                stopWords.add(sw[i]);
            }
        }

        while ((line = pwBR.readLine()) != null) {
            String[] sw = line.split(",");
            for (int i = 0; i < sw.length; i++) {
                positiveRef.add(sw[i]);
            }
        }

        while ((line = nwBR.readLine()) != null) {
            String[] sw = line.split(",");
            for (int i = 0; i < sw.length; i++) {
                negativeRef.add(sw[i]);
            }
        }
    }

    public static void renderTweetsAfterTime(List<Status> tweetsIn) {
        //Map<String, Tweetails> output = new HashMap<String, Tweetails>();
        Map<Integer, Integer> tweetOutput = new HashMap<Integer, Integer>();

        Date dateIn = new Date(2014, 11, 24); // User input
        Date dateBefore = new Date(2014,11,25);
        
        for (Status tweet : tweetsIn) {
            int tweetDay = tweet.getCreatedAt().getHours();
            
            Integer tweetHour = tweet.getCreatedAt().getHours();
            Integer tweetCount = tweetOutput.get(tweetHour);
            
            System.out.print(tweetHour);
           // if (tweet.getCreatedAt().after(dateIn) && tweet.getCreatedAt().before(dateBefore)) {
            if (tweet.getCreatedAt().compareTo(dateBefore) < 0 && tweet.getCreatedAt().compareTo(dateIn) > 0) {
                System.out.print("**********************************");
                System.out.print("Yeah dara, it got to this line 3");
                System.out.print("********************************");

                if (tweetCount == null) {
                    tweetOutput.put(tweetHour, 1);
                    System.out.println("Added tweet to hour " + tweetHour);
                } else {
                    tweetOutput.put(tweetHour, tweetCount + 1);
                    System.out.println("Added tweet to hour " + tweetHour);
                }
            }
        }

        //Iterator<Map.Entry<Integer, Integer>> outputIterator = Map.Entry
        System.out.println();
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
        // Gather x amount of tweets
        //query.setCount(5000);
        QueryResult result = twitter.search(query);

        while (result.hasNext()) {
            for (Status status : result.getTweets()) {
                System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText() + " (" + status.getCreatedAt().toString() + ")");
                System.out.println("Retweets: " + status.getRetweetCount() + " | Favourites: " + status.getFavoriteCount());

                if (status.getGeoLocation() != null) {
                    System.out.println("Geo Location:");
                    System.out.println("(" + status.getGeoLocation().getLatitude() + "," + status.getGeoLocation().getLongitude() + ")");
                }

                // Feed tweets and username into map
                tweetsForTimeScan.add(status);
                input.put(status.getText(), status.getCreatedAt());
            }

            Query qr = result.nextQuery();
            result = twitter.search(qr);
        }

    }

    public static void renderWordFreqTable() {
        Map<String, Tweetails> output = new HashMap<String, Tweetails>();
        Iterator<Map.Entry<String, Date>> inputIterator = input.entrySet().iterator();

        while (inputIterator.hasNext()) {
            Map.Entry<String, Date> tweet = inputIterator.next();
            String[] words = tweet.getKey().trim().split("\\s+");

            Date tweetDate = tweet.getValue();

            for (String word : words) {
                Integer terms = null;

                Tweetails tweetDetails = output.get(word);
                if (tweetDetails != null) {
                    terms = tweetDetails.getOccourences();
                }

                ArrayList<Date> listToAdd = new ArrayList<Date>();
                if (terms == null) {
                    listToAdd.add(tweetDate);
                    output.put(word, new Tweetails(1, listToAdd));
                } else {
                    listToAdd = tweetDetails.getTweetTimes();
                    listToAdd.add(tweetDate);
                    output.put(word, new Tweetails(terms + 1, listToAdd));
                }
            }
        }

        Iterator<Map.Entry<String, Tweetails>> tempIterator = output.entrySet().iterator();
        while (tempIterator.hasNext()) {
            Map.Entry<String, Tweetails> entry = tempIterator.next();
            System.out.println(entry.getKey() + " - [" + entry.getValue().occourences + "]");
        }

        System.out.println("Starting removeStopWords()");
        termCollection = removeStopWords(output);
        System.out.println("Starting populateTermCategories()");
        populateTermCategories(termCollection);
        System.out.println("Starting orderTerms()");
        orderTerms(termCollection);
        System.out.println("Starting displayCategories()");
        displayCategories();

        renderTweetsAfterTime(tweetsForTimeScan);

        System.out.println(positiveWords.size() + " Positive Terms:");
        Iterator<Map.Entry<String, Tweetails>> positiveIterator = positiveWords.entrySet().iterator();
        Iterator<Map.Entry<String, Tweetails>> negativeIterator = negativeWords.entrySet().iterator();
        while (positiveIterator.hasNext()) {
            Map.Entry<String, Tweetails> entry = positiveIterator.next();
            System.out.print(entry.getKey() + " - [" + entry.getValue().occourences + "] (");
            for (Date time : entry.getValue().getTweetTimes()) {
                System.out.print(time + ", ");
            }
            System.out.println(")");
        }
        System.out.println(negativeWords.size() + " Negative Terms:");
        while (negativeIterator.hasNext()) {
            Map.Entry<String, Tweetails> entry = negativeIterator.next();
            System.out.print(entry.getKey() + " - [" + entry.getValue().occourences + "] (");
            for (Date time : entry.getValue().getTweetTimes()) {
                System.out.print(time + ", ");
            }
            System.out.println(")");
        }

    }

    public static void populateTermCategories(Map termMap) {
        Iterator<Map.Entry<String, Tweetails>> rPTIterator = termMap.entrySet().iterator();

        while (rPTIterator.hasNext()) {
            Map.Entry<String, Tweetails> entry = rPTIterator.next();
            String key = entry.getKey();
            Integer value = entry.getValue().occourences;
            ArrayList<Date> tweetDates = entry.getValue().tweetTime;
            // Gather People
            if (key.startsWith("@") || key.startsWith(".@")) {
                people.put(key, new Tweetails(value, tweetDates));
                rPTIterator.remove();
            }
            // Gather Hashtags
            if (key.startsWith("#")) {
                hashtags.put(key, new Tweetails(value, tweetDates));
                rPTIterator.remove();
            }
            // Gather positive terms
            if (positiveRef.contains(key.toLowerCase())) {
                positiveWords.put(key, new Tweetails(value, tweetDates));
            }
            // Gather negative terms
            if (negativeRef.contains(key.toLowerCase())) {
                negativeWords.put(key, new Tweetails(value, tweetDates));
            }
        }
    }

    public static void displayCategories() {

        Iterator<Map.Entry<String, Tweetails>> peopleIterator = people.entrySet().iterator();
        Iterator<Map.Entry<String, Tweetails>> topicIterator = hashtags.entrySet().iterator();
        Iterator<Map.Entry<String, Tweetails>> positiveIterator = positiveWords.entrySet().iterator();
        Iterator<Map.Entry<String, Tweetails>> negativeIterator = negativeWords.entrySet().iterator();

        System.out.println("\n\n*People*\t\t*Topic*\t\t*Positive*\t\t*Negative*");
        while (peopleIterator.hasNext() || topicIterator.hasNext() || positiveIterator.hasNext() || negativeIterator.hasNext()) {
            if (peopleIterator.hasNext()) {
                Map.Entry<String, Tweetails> personEntry = peopleIterator.next();
                System.out.print(personEntry.getKey());
            } else {
                System.out.print("\t\t");
            }
            if (topicIterator.hasNext()) {
                Map.Entry<String, Tweetails> topicEntry = topicIterator.next();
                System.out.print("\t\t" + topicEntry.getKey());
            } else {
                System.out.print("\t\t");
            }
            if (positiveIterator.hasNext()) {
                Map.Entry<String, Tweetails> positiveEntry = positiveIterator.next();
                System.out.print("\t\t" + positiveEntry.getKey());
            } else {
                System.out.print("\t\t");
            }
            if (negativeIterator.hasNext()) {
                Map.Entry<String, Tweetails> negativeEntry = negativeIterator.next();
                System.out.print("\t\t" + negativeEntry.getKey() + "\n");
            } else {
                System.out.print("\n");
            }
        }
    }

    public static Map<String, Tweetails> removeStopWords(Map<String, Tweetails> toRemoveFrom) {
        Iterator<Map.Entry<String, Tweetails>> tTFIterator = toRemoveFrom.entrySet().iterator();

        while (tTFIterator.hasNext()) {
            Map.Entry<String, Tweetails> entry = tTFIterator.next();
            String key = entry.getKey();
            if (stopWords.contains(key.toLowerCase()) || key.startsWith("http") || key.equals("RT")) {
                tTFIterator.remove();
            }
        }
        //Map<String, Integer> toReturn = toRemoveFrom;
        return toRemoveFrom;
    }

    public static Map getTermsOver(int n, Map map) {
        System.out.println("Terms over " + n + ":\n");

        Iterator<Map.Entry<String, Tweetails>> entryIter = map.entrySet().iterator();
        Map<String, Tweetails> toReturn = new HashMap<String, Tweetails>();
        //System.out.println("Terms over 100:\n");

        while (entryIter.hasNext()) {
            Map.Entry<String, Tweetails> entry = entryIter.next();
            String key = entry.getKey();
            int value = entry.getValue().occourences;

            //System.out.println(value);
            if (value > n) { //If term is mentioned more than 100 times
                System.out.println(key + ": " + value);
                toReturn.put(key, new Tweetails(value, entry.getValue().tweetTime));
            }
        }

        return toReturn;
    }

    public static Map orderTerms(Map<String, Tweetails> unsortedMap) {
        TreeMap<String, Tweetails> treeMap = new TreeMap<String, Tweetails>(new MapComparator(unsortedMap));
        treeMap.putAll(unsortedMap);

        Iterator<Map.Entry<String, Tweetails>> mapIterator = treeMap.entrySet().iterator();

        System.out.println("Terms:");
        while (mapIterator.hasNext()) {
            Map.Entry<String, Tweetails> entry = mapIterator.next();
            System.out.print(entry.getKey() + "[" + entry.getValue().occourences + "] ("); // + ")");
            for (Date time : entry.getValue().getTweetTimes()) {
                System.out.print(time + ", ");
            }
            System.out.println(")");
        }

        return treeMap;
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

class MapComparator implements Comparator<String> {

    Map<String, Tweetails> base;

    public MapComparator(Map<String, Tweetails> base) {
        this.base = base;
    }

    @Override
    public int compare(String a, String b) {
        if (base.get(a).occourences >= base.get(b).occourences) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }

}

class Tweetails { // Tweet details huehue

    public Integer occourences;
    public ArrayList<Date> tweetTime;

    Tweetails(Integer occ, ArrayList<Date> tt) {
        this.occourences = occ;
        this.tweetTime = tt;
    }

    public Integer getOccourences() {
        return this.occourences;
    }

    public ArrayList<Date> getTweetTimes() {
        return this.tweetTime;
    }
}
