package com.wikia.app.TvWikia.db;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class EpisodeParser {
	
	private static final String ns = null;
	
	public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException{
		try{
			//Instantiate the pull parser
			XmlPullParser parser = Xml.newPullParser();
			
			//Turn off namespacing
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			
			//Attach the input stream
			parser.setInput(in, null);
			parser.nextTag();
			
			//Start parsing
			return readFeed(parser);
		} finally{
			in.close();
		}
	}

	private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
		List<Entry> entries = new ArrayList<Entry>();
		//Set 'Data' as the overall tag
		parser.require(XmlPullParser.START_TAG, ns, "Data");
		while(parser.next() != XmlPullParser.END_TAG){
			//Loop until a start tag is read
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			//Get the name of the element
			String name = parser.getName();
			if(name.equals("Episode")){
				//If it's an Episode, parse it
				entries.add(readEntry(parser));
			} else{
				skip(parser);
			}
		}
		return entries;
	}
	
	//Entry class to hold episode information
	public static class Entry {
		public final int season;
		public final int episode;
		public final String title;
		public final String airdate;

		private Entry(int season, int episode, String title, String airdate){
			this.season = season;
			this.episode = episode;
			this.title = title;
			this.airdate = airdate;
		}
	}

	// Parses the contents of an entry. If it encounters a seaason, episode, title, or airdate tag,
	// hands them off to their respective "read" methods for processing. Otherwise, skips the tag.
	private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
	    parser.require(XmlPullParser.START_TAG, ns, "Episode");
	    int season = -1;
	    int episode = -1;
	    String title = null;
	    String airdate = null;
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        if (name.equals("SeasonNumber")) {
	            season = readSeason(parser);
	        } else if (name.equals("EpisodeNumber")) {
	            episode = readEpisode(parser);
	        } else if (name.equals("EpisodeName")) {
	            title = readTitle(parser);
	        } else if (name.equals("FirstAired")) {
	            airdate = readAirdate(parser);
	        } else {
	            skip(parser);
	        }
	    }
	    return new Entry(season, episode, title, airdate);
	}


	// Processes SeasonNumber tags in the feed.
	private int readSeason(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "SeasonNumber");
	    int season = readNumber(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "SeasonNumber");
	    return season;
	}
	// Processes FirstAired tags in the feed.
	private int readEpisode(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "EpisodeNumber");
	    int episode = readNumber(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "EpisodeNumber");
	    return episode;
	}
	// For the tags EpisodeName and FirstAired, extracts their text values.
	private int readNumber(XmlPullParser parser) throws IOException, XmlPullParserException {
	    int result = -1;
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = Integer.parseInt(parser.getText());
	        parser.nextTag();
	    }
	    return result;
	}
	// Processes EpisodeName tags in the feed.
	private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "EpisodeName");
	    String title = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "EpisodeName");
	    return title;
	}
	// Processes FirstAired tags in the feed.
	private String readAirdate(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "FirstAired");
	    String title = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "FirstAired");
	    return title;
	}
	// For the tags EpisodeName and FirstAired, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	}
}

