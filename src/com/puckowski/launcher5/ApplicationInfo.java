package com.puckowski.launcher5;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class ApplicationInfo {
    public CharSequence title;
    public Intent intent;
    public Drawable icon;
    public boolean filtered;
    
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        intent.setComponent(className);
        intent.setFlags(launchFlags);
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(!(object instanceof ApplicationInfo)) {
            return false;
        }

        ApplicationInfo info = (ApplicationInfo) object;
        
        return title.equals(info.title) &&
                intent.getComponent().getClassName().equals(
                        info.intent.getComponent().getClassName());
    }
}
