package com.puckowski.launcher5;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
	private final int ICON_DRAWABLE_SIZE = 72;
	private final double TEXT_RADIUS_SCALAR = 1.334;
	
	private Context mContext;
    private Rect mIconBounds;

    public ApplicationsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
        super(context, 0, apps);
        
        mContext = context;
        mIconBounds = new Rect();
    }
    
    private int getPixelsFromDp(float dp) {
	    return (int) Math.ceil(dp * mContext.getResources().getDisplayMetrics().density);
	}
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ApplicationInfo info = ((MainActivity) mContext).getApplications().get(position);
        
        if(convertView == null) {
            final LayoutInflater inflater = ((MainActivity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.app_list_icon, parent, false);
        }

        final TextView textView = (TextView) convertView.findViewById(R.id.appIconView);
        Drawable icon = info.icon;
        
        if(!info.filtered) {
            int width = getPixelsFromDp(ICON_DRAWABLE_SIZE);  
            width = (int) (width - getAdjustedTextHeight(textView));
            int height = width;

            final int iconWidth = icon.getIntrinsicWidth();
            final int iconHeight = icon.getIntrinsicHeight();

            final float ratio = (float) iconWidth / iconHeight;

            if(iconWidth > iconHeight) {
                height = (int) (width / ratio);
            }
            else if(iconHeight > iconWidth) {
                width = (int) (height * ratio);
            }

            final Bitmap.Config bitmapConfig = (icon.getOpacity() != PixelFormat.OPAQUE ?
                        Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            
            final Bitmap iconBitmap = Bitmap.createBitmap(width, height, bitmapConfig);
            final Canvas canvas = new Canvas(iconBitmap);

            canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));

            mIconBounds.set(icon.getBounds());

            icon.setBounds(0, 0, width, height);
            icon.draw(canvas);
            icon.setBounds(mIconBounds);
            
            icon = info.icon = new BitmapDrawable(mContext.getResources(), iconBitmap);
            info.filtered = true;
        }
        
        textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        textView.setText(info.title);

        return convertView;
    }
    
    private int getAdjustedTextHeight(TextView textView) {    	
    	return (int) (textView.getTextSize() * TEXT_RADIUS_SCALAR);
    }
}