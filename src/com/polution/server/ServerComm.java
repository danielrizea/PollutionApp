package com.polution.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.polution.map.model.PollutionPoint;

public class ServerComm {

	
	/*
	public static final String serverHttp = "http://danielrizea.ro/thesis/";

	public static final String uploadFileName = "upload_data.php";
	public static final String downloadFileName = "download_data.php";
	*/
	
	public static final String serverHttp = "http://danielrizea.ro/thesis/";

	public static final String uploadFileName = "upload_data";
	public static final String downloadFileName = "download_data.php";
	public static void uploadPoints(String xmlData){

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
		HttpConnectionParams.setSoTimeout(httpParameters, 15000);

		DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
		HttpPost httppost = new HttpPost(serverHttp+uploadFileName);

		//HttpGet httpGet = new HttpGet(serverHttp+uploadFileName);
		
		//BasicHttpParams params = new BasicHttpParams();
		//params.setParameter("data_points", xmlData);
		
		try {
		    // Add your data
		    
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		    nameValuePairs.add(new BasicNameValuePair("data_points", xmlData));
		    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
		    
		    //httpGet.setParams(params);
	           
		    // Execute HTTP Post Request
		    HttpResponse response = httpclient.execute(httppost);
		    //nameValuePairs.clear();
		    //nameValuePairs = null;

		    HttpEntity httpEntity = response.getEntity();
		    response = null;
		    Log.e("Server Response",EntityUtils.toString(httpEntity));
		} catch (IOException e) {
		    Log.e("IOException", " IOException ");
		    e.printStackTrace();
		} catch (Exception e){
		    Log.e("Exception", " Exception ");
		    e.printStackTrace();
		} finally {
		    httpParameters = null;
		    httppost = null;
		}

	}

	
	
	public static List<PollutionPoint> downloadPoints(){
		
		try{
			HttpGet httpget = new HttpGet(serverHttp+downloadFileName);  
	
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse resp = client.execute(httpget);
	
			StatusLine status = resp.getStatusLine();
			if (status.getStatusCode() != HttpStatus.SC_OK) {
			    Log.d("log", "HTTP error, invalid server status code: " + resp.getStatusLine());  
			}
	
			String xmlDATA = EntityUtils.toString(resp.getEntity());
			//System.out.println("DATA :" + xmlDATA);
			
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( xmlDATA.getBytes());
			
			
			List<PollutionPoint> points = XMLProtocol.parseXMLDocument(byteArrayInputStream);
			
			System.out.println("Size :" + points);
			return points;
			
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
		return null;
	}
}
