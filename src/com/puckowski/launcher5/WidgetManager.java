package com.puckowski.launcher5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class WidgetManager {
	public final int APPWIDGET_HOST_ID        = 3001;
	public final int REQUEST_PICK_APPWIDGET   = 3002;
	public final int REQUEST_CREATE_APPWIDGET = 3003;
	
	private Activity mContext;
	
	private AppWidgetHost mAppWidgetHost;
	private AppWidgetManager mAppWidgetManager;
	private ArrayList<Integer> mAppWidgetIdArray;
	
	public WidgetManager(MainActivity context) {
		mContext = context;
		
		mAppWidgetHost = new AppWidgetHost(context, APPWIDGET_HOST_ID);
		mAppWidgetManager = AppWidgetManager.getInstance(context);	
		mAppWidgetIdArray = new ArrayList<Integer>();
	}
	
	public AppWidgetHost getHost() {
		return mAppWidgetHost;
	}
	
	public AppWidgetManager getManager() {
		return mAppWidgetManager;
	}
	
	public void selectWidget() {
		int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
		
		Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		
		addEmptyData(intent);
		
		mContext.startActivityForResult(intent, REQUEST_PICK_APPWIDGET);
	}
	
	public void addEmptyData(Intent intent) {
		ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
		intent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
		
		ArrayList<Bundle> customExtras = new ArrayList<Bundle>();
		intent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
	}
	
	public void readWidgetData() {
		FileInputStream idInputStream;
		
		try {
			idInputStream = mContext.openFileInput(mContext.getString(R.string.appwidget_id_file));
			
			BufferedReader idReader = new BufferedReader(new InputStreamReader(idInputStream));
            
		    String line = null; 
		    try {
			    while((line = idReader.readLine()) != null) {
			        mAppWidgetIdArray.add(Integer.parseInt(line));
			    }
			    
			    idInputStream.close();
		    }
		    catch(IOException ioException) { 
		    	ioException.printStackTrace();
		    }
		}
		catch(FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
			return;
		}
	}
	
	public void reinstateSavedWidgets(LinearLayout widgetLayout) {		
		AppWidgetHostView appWidgetHostView = null;
		
		for(int i = 0; i < mAppWidgetIdArray.size(); i++) {
			try {
				int appWidgetId = mAppWidgetIdArray.get(i);
				 
				appWidgetHostView = loadSavedWidget(appWidgetId);
			}
			catch(Exception exception) { 
				exception.printStackTrace();
				appWidgetHostView = null;
			}
			
			if(appWidgetHostView != null && widgetLayout != null) {
				widgetLayout.addView(appWidgetHostView);
			}
		}
	}
	
	public AppWidgetHostView loadSavedWidget(int appWidgetId) {   
		int newAppWidgetId = mAppWidgetHost.allocateAppWidgetId();
		newAppWidgetId = appWidgetId;
		
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(newAppWidgetId);
				
		AppWidgetHostView appWidgetHostView = mAppWidgetHost.createView(mContext, newAppWidgetId, appWidgetInfo);
		appWidgetHostView.setAppWidget(newAppWidgetId, appWidgetInfo);
		
		return appWidgetHostView;
	}
	
	public void saveWidgetData() {
		File file = new File(mContext.getFilesDir(), mContext.getString(R.string.appwidget_id_file));
		
		if(file.canWrite() == false) {
			return;
		}
		
		try {
			FileOutputStream idOutputStream = mContext.openFileOutput(mContext.getString(R.string.appwidget_id_file), Context.MODE_PRIVATE);		
			BufferedWriter idWriter = new BufferedWriter(new OutputStreamWriter(idOutputStream));
			
			for(int i = 0; i < mAppWidgetIdArray.size(); i++) {
				idWriter.write(mAppWidgetIdArray.get(i) + "\n");
			}
			
			idWriter.close();
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}  
	}
	
	public void createWidget(Intent intent, LinearLayout widgetLayout) {
		Bundle extras = intent.getExtras();
		
		int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
		
		AppWidgetHostView appWidgetHostView = mAppWidgetHost.createView(mContext, appWidgetId, appWidgetInfo);
		appWidgetHostView.setAppWidget(appWidgetId, appWidgetInfo);
			
		mAppWidgetIdArray.add(appWidgetId);
		widgetLayout.addView(appWidgetHostView);
	}
	
	public void configureWidget(Intent intent, LinearLayout widgetLayout) {
		Bundle extras = intent.getExtras();
		
		int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
		
		if(appWidgetInfo.configure != null) {
			intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			
			intent.setComponent(appWidgetInfo.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			
			mContext.startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
		} 
		else {
			createWidget(intent, widgetLayout);
		}
	}
	
	public void removeWidget(LinearLayout widgetLayout) { 
		int appWidgetCount = mAppWidgetIdArray.size();
		
		if(appWidgetCount == 0) {
			return;
		}
		
		appWidgetCount--;
		
		widgetLayout.removeViewAt(appWidgetCount);
		mAppWidgetIdArray.remove(appWidgetCount);
	}
}