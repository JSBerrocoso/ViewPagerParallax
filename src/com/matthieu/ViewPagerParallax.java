package com.matthieu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class ViewPagerParallax extends ViewPager {
    int background_id =-1;
    int background_saved_id =-1;
    int saved_width=-1, saved_height=-1, saved_max_num_pages =-1;
    Bitmap saved_bitmap;

    int max_num_pages=0;
    int imageHeight;
    int imageWidth;
    float zoom_level;

    float overlap_level;

    Rect r = new Rect();

    private final static String TAG="ViewPagerParallax";

    public ViewPagerParallax(Context context) {
        super(context);
    }

    public ViewPagerParallax(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint({"NewApi"})
    private void set_new_background() {
        if (background_id == -1)
            return;

        if (max_num_pages == 0)
            return;

        if (getWidth()==0 || getHeight()==0)
            return;

        if ((saved_height == getHeight()) && (saved_width == getWidth()) &&
                (background_saved_id==background_id) &&
                (saved_max_num_pages == max_num_pages))
            return;

        InputStream is;

        try {
            is = getContext().getResources().openRawResource(background_id);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            imageHeight = options.outHeight;
            imageWidth = options.outWidth;

            zoom_level = ((float) imageHeight) / getHeight();  // we are always in 'fitY' mode
            is.reset();

            overlap_level = (imageWidth / zoom_level - getWidth() * max_num_pages) / (max_num_pages - 1);
            saved_bitmap = BitmapFactory.decodeStream(is);

            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot decode: " + e.getMessage());
            background_id = -1;
            return;
        }

        saved_height = getHeight();
        saved_width = getWidth();
        background_saved_id = background_id;
        saved_max_num_pages = max_num_pages;
    }

    int current_position=0;
    float current_offset=0.0f;

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        current_position = position;
        current_offset = offset;
    }

    Rect src = new Rect(), dst = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // maybe we could get the current position from the getScrollX instead?
        src.set((int) (((current_position + current_offset) * getWidth() + overlap_level * (current_position + current_offset)) * zoom_level), 0,
                (int) (((current_position + current_offset + 1) * getWidth() + overlap_level * current_position) * zoom_level), imageHeight);

        dst.set(getScrollX(), 0,
                (int) (getScrollX() + canvas.getWidth() - overlap_level * current_offset), canvas.getHeight());

        canvas.drawBitmap(saved_bitmap, src, dst, null);
    }

    public void set_max_pages(int num_max_pages) {
        max_num_pages = num_max_pages;
        set_new_background();
    }

    public void setBackgroundAsset(int res_id) {
        background_id = res_id;
        set_new_background();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        set_new_background();
    }
}
