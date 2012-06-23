package com.polution.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;

public class FileOperations {

	public static String DATABASE_FILE_NAME = "database.xml";
	
	public static void saveFile(Context context,String filename, String content){
		

		try{
			FileOutputStream fOut = context.openFileOutput(filename,
			                Context.MODE_WORLD_READABLE);
			OutputStreamWriter osw = new OutputStreamWriter(fOut); 
			
			// Write the string to the file
			osw.write(content);
			/* ensure that everything is
			* really written out and close */
			osw.flush();
			osw.close();
		}catch(Exception e){
			System.err.println("File write error in " + DATABASE_FILE_NAME);
		}
		
	}


	public static String readFile(Context context){
		String fileContent = "";
		
		try{
				FileInputStream fIn = context.openFileInput(DATABASE_FILE_NAME);
		        InputStreamReader isr = new InputStreamReader(fIn);
		        /* Prepare a char-Array that will
		         * hold the chars we read back in. */
		        char[] inputBuffer = new char[1000];
		        // Fill the Buffer with data from the file
		        
		        while(isr.read(inputBuffer) != -1){
			        // Transform the chars to a String
			        fileContent = fileContent + new String(inputBuffer);
		        }
		        
			} catch (IOException ioe) {
			        ioe.printStackTrace();
			}
		
		return fileContent;
	}
	
	public static void saveFileToSDCard(String filename, String content){
		
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		FileOutputStream fos;
		byte[] data = content.getBytes();
		try {
		    fos = new FileOutputStream(file);
		    fos.write(data);
		    fos.flush();
		    fos.close();
		} catch (FileNotFoundException e) {
		    // handle exception
		} catch (IOException e) {
		    // handle exception
		}
	}
	
	public static InputStream readFile(Context context,String filename){

		try{
				FileInputStream fIn = context.openFileInput(filename);
		        /* Prepare a char-Array that will
		         * hold the chars we read back in. */
		      
		        return fIn;
		        
			} catch (IOException ioe) {
			        ioe.printStackTrace();
			}
		
		return null;
	}
}
