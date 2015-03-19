package com.kd.filmstrip.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ImageDownloader extends AsyncTask<String, Void, Boolean>
{
	ProgressDialog pd;
	String url, file;
	Context mContext;
	public ImageDownloader(String downloadUrl, String imageName, Context context){
		url = downloadUrl;
		file = imageName;
		mContext = context;
	}
	@Override
	protected void onPreExecute(){
		pd = ProgressDialog.show(mContext, "Wait", "Downloading...");
	}
    @Override
    protected Boolean doInBackground(String... arg0) 
    {           
        downloadImagesToSdCard(url,file);
        return true;
    }

       public void downloadImagesToSdCard(String downloadUrl,String imageName)
        {
            try
            {
                URL url = new URL(downloadUrl); 
                /* making a directory in sdcard */
                String sdCard=Environment.getExternalStorageDirectory().toString();     
                File myDir = new File(sdCard+"/Pictures/Filmstrip/");

                /*  if specified not exist create new */
                if(!myDir.exists())
                {
                    myDir.mkdir();
                    Log.v("", "inside mkdir");
                }

                /* checks the file and if it already exist delete */
                String fname = imageName;
                File file = new File (myDir, fname);
                if (file.exists ()) 
                    file.delete (); 

                     /* Open a connection */
                URLConnection ucon = url.openConnection();
                InputStream inputStream = null;
                HttpURLConnection httpConn = (HttpURLConnection)ucon;
                httpConn.setRequestMethod("GET");
                httpConn.connect();

                  if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) 
                  {
                   inputStream = httpConn.getInputStream();
                  }

                FileOutputStream fos = new FileOutputStream(file);  
                int totalSize = httpConn.getContentLength();
                int downloadedSize = 0;   
                byte[] buffer = new byte[1024];
                int bufferLength = 0;
                while ( (bufferLength = inputStream.read(buffer)) >0 ) 
                {                 
                  fos.write(buffer, 0, bufferLength);                  
                  downloadedSize += bufferLength;                 
                  Log.i("Progress:","downloadedSize:"+downloadedSize+"totalSize:"+ totalSize) ;
                }   

                    fos.close();
                    Log.d("test", "Image Saved in sdcard..");                      
            }
            catch(IOException io)
            {                  
                 io.printStackTrace();
            }
            catch(Exception e)
            {                     
                e.printStackTrace();
            }
        }  
   		@Override
   		protected void onPostExecute(Boolean result) {
   			pd.dismiss();
     	   Toast.makeText(mContext, "Image Saved", Toast.LENGTH_SHORT);
        }
}
