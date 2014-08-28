package com.tmsf.stackimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class Picture extends ImageView {
	public String url;
	private String tag = "Pic";
	private Context cxt;

	public Picture(Context context, AttributeSet attrs) {
		super(context, attrs);
		cxt = context;
	}

	public Picture(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		cxt = context;
	}

	public Picture(Context context) {
		super(context);
		cxt = context;
	}

	public void setImageURL(String url) {
		if (!url.equals(this.url)) {
			this.url = url;
			PicManagerList.getmInstance().showImage(url, this,
					R.drawable.loading_pic);
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		// 注意优化 当有相同的drawable 将不会updatedrawable
		setImageDrawable(new CountingBitmapDrawable(cxt.getResources(), bm,
				this.url));
	}

	@Override
	public void setImageResource(int resId) {
		// TODO Auto-generated method stub
		Drawable pre = getDrawable();
		super.setImageResource(resId);
		if (pre instanceof CountingBitmapDrawable) {
			((CountingBitmapDrawable) pre).setIsDisplayed(false);
		}
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		Drawable pre = getDrawable();
		super.setImageDrawable(drawable);
		if (drawable instanceof CountingBitmapDrawable) {
			((CountingBitmapDrawable) drawable).setIsDisplayed(true);
		}
		if (pre instanceof CountingBitmapDrawable) {
			((CountingBitmapDrawable) pre).setIsDisplayed(false);
		}

	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	protected void onAttachedToWindow() {
		Log.e(tag, "onAttachedToWindow");
		super.onAttachedToWindow();
	}

}
