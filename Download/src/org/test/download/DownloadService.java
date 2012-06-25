package org.test.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ProgressBar;

public class DownloadService extends Service {
	String downloadUrl,fileName;
	LocalBroadcastManager mLocalBroadcastManager;
	ProgressBar progressBar;
	File sdCard = Environment.getExternalStorageDirectory();
	File dir = new File (sdCard.getAbsolutePath() + "/org.test.download/");
	double fileSize = 0;
	DownloadAsyncTask dat;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public DownloadService(String url,Context c, ProgressBar pBar){
		downloadUrl = url;
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(c);
		progressBar = pBar;
		dat = new DownloadAsyncTask();
		dat.execute(new String[]{downloadUrl});
		
	}
	
	private boolean checkDirs(){
		if(!dir.exists()){
			return dir.mkdirs();
		}
		return true;
	}
	private long isIncomplete(){
		File from = new File(dir,fileName+"-incomplete");
		if(from.exists()){
			Log.d("status","download is incomplete, filesize:" + from.length());
			return from.length();
		}
		return 0;
	}
	public void cancel(){
		dat.cancel(true);
	}
	public class DownloadAsyncTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
				fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")+1);
				if(!checkDirs()){
					return "Making directories failed!";
				}
				try {
					byte[] buffer = new byte[5000];
			        int bufferLength = 0;
			        int percentage = 0;
			        double downloadedSize = 0;
					URL url = new URL(downloadUrl);
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			        urlConnection.setRequestMethod("GET");
			        urlConnection.setConnectTimeout(10000);
			        urlConnection.setReadTimeout(10000);
			        Log.d("status","ReadTimeOut: "+urlConnection.getReadTimeout() + "ConnectTimeOut: "+urlConnection.getConnectTimeout());
			        long downloaded = isIncomplete();
			        if(downloaded > 0){
			        	urlConnection.setRequestProperty("Range", "bytes="+(downloaded)+"-");
			        	downloadedSize = downloaded;
			        	fileSize = downloaded;
			        }
			        urlConnection.setDoOutput(true);
			        urlConnection.connect();
			        fileSize += urlConnection.getContentLength();
			        FileOutputStream fos = new FileOutputStream(new File(dir,fileName+"-incomplete"),true);
			        InputStream inputStream = urlConnection.getInputStream();
			        while ( (bufferLength = inputStream.read(buffer)) > 0 ) 
		            {
			        	if(isCancelled()){
			        		break;
			        	}
			            fos.write(buffer, 0, bufferLength);
			            downloadedSize += bufferLength;
			            percentage = (int) ((downloadedSize / fileSize) * 100);
			            publishProgress(percentage);
			            //Log.d("status","downloading: " + downloadedSize+"/"+fileSize+" ("+percentage+"%)");
		            }
			        fos.close();
			        urlConnection.disconnect();
				} catch (Exception e) {
					Log.e("Download Failed","Error: " + e.getMessage());
				}
				if(isCancelled()){
	        		return "Download cancelled!";
	        	}
			return "Download complete";
		}
		@Override
		protected void onProgressUpdate(Integer... values){
			super.onProgressUpdate(values[0]);
			if(progressBar != null){
				progressBar.setProgress(values[0]);
			}else{
				Log.w("status", "ProgressBar is null, please supply one!");
			}
		}
		
		@Override
		protected void onPreExecute(){
			mLocalBroadcastManager.sendBroadcast(new Intent("org.test.download.DOWNLOAD_STARTED"));
		}
		
		@Override
		protected void onPostExecute(String str){
			File from = new File(dir,fileName+"-incomplete");
			File to = new File(dir,fileName);
			from.renameTo(to);
			mLocalBroadcastManager.sendBroadcast(new Intent("org.test.download.DOWNLOAD_FINISHED"));
		}
		
		@Override
		protected void onCancelled(){
			mLocalBroadcastManager.sendBroadcast(new Intent("org.test.download.DOWNLOAD_CANCELLED"));
		}
		
	}

}
