package com.mycompany.twitter4jfetch;

import java.io.IOException;
import javax.swing.JOptionPane;
import twitter4j.TwitterException;

public class TwitterFetch {
	
	public static TwitterClient tw;
	
	public static void search(String qstr) {
		try {
			tw.startSeachAPI(qstr); 
			} catch (TwitterException e) {
				// TODO Auto-generated catch block 
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block 
				e.printStackTrace(); 
			}
	}
	
	public static void userTimeline(String user) {
		try {
			tw.getUserTimeLine(user); 
		} catch (TwitterException e) {
			// TODO Auto-generated catch block e.printStackTrace(); }
		}
	}
	
	/** * @param args */ 
	public static void main(String[] args) throws IOException {
                // FileReading
                TwitterClient.readFiles();
            
		tw = new TwitterClient(AccessTokens.consumerKey, AccessTokens.consumerKeySecret);
		search(JOptionPane.showInputDialog("Enter Query:"));
                
                System.out.println("************************************");
                TwitterClient.renderWordFreqTable();
          
                
                //TwitterClient.getTermsOver(0, TwitterClient.termCollection);

                

		//userTimeline("pwezem"); // only recent 200
	}
}
