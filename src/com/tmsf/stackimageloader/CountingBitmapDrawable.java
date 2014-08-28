/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tmsf.stackimageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

/**
 * A BitmapDrawable that keeps track of whether it is being displayed or cached.
 * When the drawable is no longer being displayed or cached,
 * {@link Bitmap#recycle() recycle()} will be called on this drawable's bitmap.
 */
public class CountingBitmapDrawable extends BitmapDrawable {
	static final String tag = "RecyclingBitmapDrawable";
	public int mDisplayRefCount = 0;
	private boolean mHasBeenDisplayed;
	private String url;
	public CountingBitmapDrawable(Resources res, Bitmap bitmap, String url) {
		super(res, bitmap);
		this.url = url;
	}

	/**
	 * Notify the drawable that the displayed state has changed. Internally a
	 * count is kept so that the drawable knows when it is no longer being
	 * displayed.
	 * 
	 * @param isDisplayed
	 *            - Whether the drawable is being displayed or not
	 */
	public void setIsDisplayed(boolean isDisplayed) {
		synchronized (this) {
			if (isDisplayed) {
				mDisplayRefCount++;
				mHasBeenDisplayed = true;
			} else {
				mDisplayRefCount--;
			}
			Log.e(tag, "display count=" + mDisplayRefCount);
		}
	};

	private synchronized void checkState() {
		// If the drawable cache and display ref counts = 0, and this drawable
		// has been displayed, then recycle
		if (mDisplayRefCount <= 0 && mHasBeenDisplayed
				&& !PicManagerList.getmInstance().isInCache(url)
				&& hasValidBitmap()) {
			getBitmap().recycle();
		}
	}

	private synchronized boolean hasValidBitmap() {
		Bitmap bitmap = getBitmap();
		return bitmap != null && !bitmap.isRecycled();
	}

}
