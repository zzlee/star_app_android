package com.gabriel.pgap22.star;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import com.google.android.gcm.*;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.phonegap.plugins.GCM.GCMPlugin;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.*;
import android.app.*;



public class GCMIntentService extends GCMBaseIntentService {

  public static final String ME="GCMReceiver";
  public static String msgToJS = "";
  //Jean
  private static final String serverUrl = "http://www.feltmeng.idv.tw/members/device_tokens";
  private static final int MAX_ATTEMPTS = 5;
  private static final int BACKOFF_MILLI_SECONDS = 2000;
  private static final Random random = new Random();

  public GCMIntentService() {
    super("GCMIntentService");
  }
  private static final String TAG = "GCMIntentService";

  @Override
  public void onRegistered(Context context, String regId) {

	    Log.v(ME + ":onRegistered", "Registration ID arrived!");
	    Log.v(ME + ":onRegistered", regId);

	    JSONObject json;
	    Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        
	    try{
	      json = new JSONObject().put("event", "registered");
	      json.put("regid", regId);

	      Log.v(ME + ":onRegisterd", json.toString());

	      // Send this JSON data to the JavaScript application above EVENT should be set to the msg type
	      // In this case this is the registration ID
	      GCMPlugin.sendJavascript( json );
//	      post(serverUrl, params);

	    }
	    catch( JSONException e)
	    {
	      // No message to the user is sent, JSON failed
	      Log.e(ME + ":onRegisterd", "JSON exception");
	    }
	    
  }

  @Override
  public void onUnregistered(Context context, String regId) {
    Log.d(TAG, "onUnregistered - regId: " + regId);
  }

  @Override
  protected void onMessage(Context context, Intent intent) {
    Log.d(TAG, "onMessage - context: " + context);

    // Extract the payload from the message
    Bundle extras = intent.getExtras();
    if (extras != null) {
      try{
        Log.v(ME + ":onMessage extras ", extras.getString("message"));

        JSONObject json;
        json = new JSONObject().put("event", "message");


        // My application on my host server sends back to "EXTRAS" variables message and msgcnt
        // Depending on how you build your server app you can specify what variables you want to send
        //
        String title = extras.getString("title");
        String message = extras.getString("message");
        
        json.put("title", title);
        json.put("message", message);
        json.put("msgcnt", extras.getString("msgcnt"));

        /* Status Bar Notification */
        /*
        Notification notif = new Notification(android.R.drawable.btn_star_big_on, message, System.currentTimeMillis());
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.defaults |= Notification.DEFAULT_SOUND;
        notif.defaults |= Notification.DEFAULT_VIBRATE;*/
        
        NotificationCompat.Builder notiBuilder = 
        		new NotificationCompat.Builder(this)
        		.setSmallIcon(R.drawable.icon_notification)
        		.setContentTitle(title)
        		.setContentText(message)
        		.setAutoCancel(true)
        		.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        		.setTicker("上大螢幕");
        
        
        
        Intent notificationIntent = new Intent(context, Star_gabriel_pgap22.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Star_gabriel_pgap22.class);
        stackBuilder.addNextIntent(notificationIntent);
        
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notiBuilder.setContentIntent(resultPendingIntent);
        
        Notification notif = notiBuilder.build();
        
        notif.setLatestEventInfo(context, title, message, resultPendingIntent);
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
        mNotificationManager.notify(1, notif);
        /* End of Status Bar Notification*/
          
        
        Log.v(ME + ":onMessage ", json.toString());

        GCMPlugin.sendJavascript( json );
        // Send the MESSAGE to the Javascript application
      }
      catch( JSONException e)
      {
        Log.e(ME + ":onMessage", "JSON exception");
      }        	
    }


  }

  public void onError(Context context, String errorId) {
    Log.e(TAG, "onError - errorId: " + errorId);
  }
  
  //Deprecated
  private static void post(String endpoint, Map<String, String> params) throws IOException {
  	Log.v(TAG, "Post the regId to Server.");
      URL url;
      try {
          url = new URL(endpoint);
      } catch (MalformedURLException e) {
          throw new IllegalArgumentException("invalid url: " + endpoint);
      }
      StringBuilder bodyBuilder = new StringBuilder();
      Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
      // constructs the POST body using the parameters
      while (iterator.hasNext()) {
          Entry<String, String> param = iterator.next();
          bodyBuilder.append(param.getKey()).append('=')
                  .append(param.getValue());
          if (iterator.hasNext()) {
              bodyBuilder.append('&');
          }
      }
      String body = bodyBuilder.toString();
      Log.v(TAG, "Posting '" + body + "' to " + url);
      byte[] bytes = body.getBytes();
      HttpURLConnection conn = null;
      try {
          conn = (HttpURLConnection) url.openConnection();
          conn.setDoOutput(true);
          conn.setUseCaches(false);
          conn.setFixedLengthStreamingMode(bytes.length);
          conn.setRequestMethod("POST");
          conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
//          conn.setRequestProperty("Content-Type","application/json");
          // post the request
          OutputStream out = conn.getOutputStream();
          out.write(bytes);
          out.close();
          // handle the response
          int status = conn.getResponseCode();
          if (status != 200) {
            throw new IOException("Post failed with error code " + status);
          }
      } finally {
          if (conn != null) {
              conn.disconnect();
          }
      }
    }



}

