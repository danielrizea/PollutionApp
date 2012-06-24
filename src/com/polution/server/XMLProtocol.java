package com.polution.server;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.polution.map.model.PollutionPoint;

public class XMLProtocol {

	public static final String POINT = "data_point";
	public static final String LAT = "lat";
	public static final String LON = "lon";
	public static final String CO = "CO";
	public static final String NO = "NO";
	public static final String AIR_Q = "AIR_Q";
	public static final String TIMESTAMP = "timestamp";
	
	
	public static String retrieveXMLData(List<PollutionPoint> points){

		    XmlSerializer serializer = Xml.newSerializer();
		    StringWriter writer = new StringWriter();
		    try {
		        serializer.setOutput(writer);
		        serializer.startDocument("UTF-8", true);
		        serializer.startTag("", "pollution_points");
		        serializer.attribute("", "number", String.valueOf(points.size()));
		        for (PollutionPoint p: points){
		            serializer.startTag("", POINT);
		            serializer.startTag("", LAT);
		            serializer.text(p.lat+"");
		            serializer.endTag("", LAT);
		            
		            serializer.startTag("", LON);
		            serializer.text(p.lon+"");
		            serializer.endTag("", LON);
		            
		            serializer.startTag("", CO);
		            serializer.text(p.sensor_1+"");
		            serializer.endTag("", CO);
		            
		            serializer.startTag("", CO);
		            serializer.text(p.sensor_2+"");
		            serializer.endTag("", CO);
		            
		            serializer.startTag("", AIR_Q);
		            serializer.text(p.sensor_3+"");
		            serializer.endTag("", AIR_Q);
		            
		            serializer.startTag("", TIMESTAMP);
		            serializer.text(p.timestamp+"");
		            serializer.endTag("", TIMESTAMP);
		            
		            serializer.endTag("", POINT);
		        }
		        serializer.endTag("", "pollution_points");
		        serializer.endDocument();
		        
		        System.out.println("XML :" + writer.toString());
		        return writer.toString();
		    } catch (Exception e) {
		        throw new RuntimeException(e);
		    } 
		}
	
	public static List<PollutionPoint> parseXMLDocument(InputStream inStream){
		
		ArrayList<PollutionPoint> points = new ArrayList<PollutionPoint>();
		
		 XmlPullParser parser = Xml.newPullParser();
	        try {
	            // auto-detect the encoding from the stream
	            parser.setInput(inStream, null);
	            int eventType = parser.getEventType();
	            PollutionPoint p = null;
	            boolean done = false;
	            while (eventType != XmlPullParser.END_DOCUMENT && !done){
	            	
	                String name = null;
	                switch (eventType){
	                    case XmlPullParser.START_DOCUMENT:
	                        //messages = new ArrayList<Message>();
	                        break;
	                    case XmlPullParser.START_TAG:
	                        name = parser.getName();
	                        if (name.equalsIgnoreCase(POINT)){
	                            p = new PollutionPoint();
	                        } else if (p != null){
	                            if (name.equalsIgnoreCase(LAT)){
	                                p.lat=(Float.parseFloat(parser.nextText()));
	                            } else if (name.equalsIgnoreCase(LON)){
	                                p.lon = Float.parseFloat(parser.nextText());
	                            } else if (name.equalsIgnoreCase(CO)){
	                                p.sensor_1 = Float.parseFloat(parser.nextText());
	                            } else if (name.equalsIgnoreCase(NO)){
	                            	p.sensor_2 = Float.parseFloat(parser.nextText());
	                            } else if (name.equalsIgnoreCase(AIR_Q)){
	                            	p.sensor_3 = Float.parseFloat(parser.nextText());
	                            } else if (name.equalsIgnoreCase(TIMESTAMP)){
	                                p.timestamp = Integer.parseInt(parser.nextText());
	                            }    
	                        }
	                        break;
	                    case XmlPullParser.END_TAG:
	                        name = parser.getName();
	                        if (name.equalsIgnoreCase(POINT) && 
	p != null){
	                            points.add(p);
	                        } else if (name.equalsIgnoreCase("pollution_points")){
	                            done = true;
	                        }
	                        break;
	                }
	                eventType = parser.next();
	            }
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	        
	        return points;
	}
		
}
