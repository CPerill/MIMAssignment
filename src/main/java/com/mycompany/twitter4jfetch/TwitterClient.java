package com.mycompany.twitter4jfetch;

import java.util.List;
import java.io.IOException;

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
	
	public TwitterClient(String KEY, String KEY_SECRET){
		CONSUMER_KEY = KEY;
		CONSUMER_KEY_SECRET = KEY_SECRET;
	}
	
	public void startSeachAPI(String qstr) throws TwitterException, IOException{
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
	    }
		    
	}
	
	public void getUserTimeLine(String user) throws TwitterException{
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