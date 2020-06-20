package com.puckowski.launcher5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

public class MainActivity extends FragmentActivity
	implements SharedPreferences.OnSharedPreferenceChangeListener { 	
	private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
	
	private static Context mContext;
	 
	private static ArrayList<ApplicationInfo> mApplications;
	private static ApplicationsAdapter mApplicationsAdapter;
	
	private static GridView mAppListView;
	private static LinearLayout mWidgetLayout;
	
	private ViewPager mViewPager;
	private WidgetManager mWidgetManager;
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
							
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		
		setContentView(R.layout.activity_main);
		setWallpaper();
		
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.rootLayout);
	    mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mWidgetManager = new WidgetManager(this);
        
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(sharedPreferences, null);
		
		registerIntentReceivers();
		
		loadApplications(true);
		
		mWidgetManager.readWidgetData();
		mWidgetManager.reinstateSavedWidgets(mWidgetLayout);
	}
    
	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }
    
    @Override
	protected void onStart() {
		super.onStart();
		
		mWidgetManager.getHost().startListening();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mWidgetManager.saveWidgetData();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        
        loadApplications(false);
        bindApplications();
    }
	
	@Override
	protected void onStop() {
		super.onStop();
		
		mWidgetManager.saveWidgetData(); 
		mWidgetManager.getHost().stopListening();
	}
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        
        unregisterReceiver(mApplicationsReceiver);
        
        Runtime.getRuntime().gc();
        System.gc();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case R.id.addWidget:
				mWidgetManager.selectWidget();
				
				return true;
			case R.id.removeWidget:
				mWidgetManager.removeWidget(mWidgetLayout);
				
				return true;
		}
		
		return super.onOptionsItemSelected(menuItem);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent)  {
	    if(keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0) {
	    	mViewPager.setCurrentItem(0);
	    	
	        return true;
	    }

	    return super.onKeyDown(keyCode, keyEvent);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String preferenceArgument) {        	    		
	}
	
	public WidgetManager getWidgetManager() {
		return mWidgetManager;
	}
	
	public ArrayList<ApplicationInfo> getApplications() {
		return mApplications;
	}
	
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
		private final int FRAGMENT_COUNT = 2;
		
        public AppSectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int index) {
            switch(index) {
            	case 0:
                    Fragment widgetFragment = new WidgetFragment();
                    
                    return widgetFragment;
            	case 1:
            		Fragment appListFragment = new AppListFragment();
                    
                    return appListFragment;
                default:
                	return null;
            }
        }

        @Override
        public int getCount() {
            return FRAGMENT_COUNT;
        }
    }

    public static class WidgetFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        	View widgetView = layoutInflater.inflate(R.layout.widget_fragment, container, false);
        	mWidgetLayout = (LinearLayout) widgetView.findViewById(R.id.widget_grid);
        	
        	((MainActivity) mContext).getWidgetManager().reinstateSavedWidgets(mWidgetLayout);
        	
            return widgetView;
        }
    }
    
    public static class AppListFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        	View appListView = layoutInflater.inflate(R.layout.app_list_fragment, container, false);
            
        	mAppListView = (GridView) appListView.findViewById(R.id.all_apps);
        	mAppListView.setOnItemClickListener(new ApplicationLauncher());
        	
        	mAppListView.setAdapter(mApplicationsAdapter); 
            mAppListView.setSelection(0);
            
            return appListView;
        }
        
        private class ApplicationLauncher implements AdapterView.OnItemClickListener {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ApplicationInfo applicationInfo = (ApplicationInfo) adapterView.getItemAtPosition(position);         
                startActivity(applicationInfo.intent);
            }
        }
    }
    
	private void registerIntentReceivers() {    
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        
        registerReceiver(mApplicationsReceiver, filter);
    }
	
	private void loadApplications(boolean isLaunching) {
        if(isLaunching && mApplications != null) {
            return;
        }

        PackageManager packageManager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> applications = packageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(applications, new ResolveInfo.DisplayNameComparator(packageManager));

        if(applications != null) {
            final int applicationCount = applications.size();

            if(mApplications == null) {
                mApplications = new ArrayList<ApplicationInfo>(applicationCount);
            }
            
            mApplications.clear();

            for(int i = 0; i < applicationCount; i++) {
                ApplicationInfo application = new ApplicationInfo();
                ResolveInfo resolveInfo = applications.get(i);

                application.title = resolveInfo.loadLabel(packageManager);
                application.setActivity(new ComponentName(
                		resolveInfo.activityInfo.applicationInfo.packageName,
                		resolveInfo.activityInfo.name),
                        Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                application.icon = resolveInfo.activityInfo.loadIcon(packageManager);

                mApplications.add(application);
            }
        }
        
        if(mApplicationsAdapter == null) {
        	mApplicationsAdapter = new ApplicationsAdapter(this, mApplications);
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(resultCode == RESULT_OK) {
			if(requestCode == mWidgetManager.REQUEST_PICK_APPWIDGET) {
				mWidgetManager.configureWidget(intent, mWidgetLayout);
			} 
			else if(requestCode == mWidgetManager.REQUEST_CREATE_APPWIDGET) {
				mWidgetManager.createWidget(intent, mWidgetLayout);
			}
		} 
		else if(resultCode == RESULT_CANCELED && intent != null) {
			int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			
			if(appWidgetId != -1) {
				mWidgetManager.getHost().deleteAppWidgetId(appWidgetId);
			}
		}
	}
	
	private class ApplicationsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadApplications(false);
            bindApplications();
        }
    }
	
	private void bindApplications() {
		if(mAppListView != null) {
			mAppListView.setAdapter(new ApplicationsAdapter(this, mApplications));
        	mAppListView.setSelection(0);
		}
    }
	
	private void setWallpaper() {
		Drawable wallpaper = peekWallpaper();
            
		if(wallpaper != null) {
            getWindow().setBackgroundDrawable(new CroppedDrawable(wallpaper));
        }
    }
}
