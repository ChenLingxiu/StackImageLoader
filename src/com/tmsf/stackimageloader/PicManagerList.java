package com.tmsf.stackimageloader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Stack;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

public class PicManagerList implements IPicManageList {

	final static int MSG_DOWNLOAD = 1;
	final static int MSG_STOP = 3;
	final static int MSG_DOWNLOAD_DONE = 4;
	final static int MSG_DOWNLOAD_FAIL = 5;
	boolean LOADFROMNET_IDLE = true;
	final static String tag = "PicManagerList";
	private static PicManagerList mInstance;

	private Stack<ViewRef> stack = new Stack<ViewRef>();

	private HashMap<String, ViewRef> queueNetTaskHelperMap = new HashMap<String, ViewRef>();
	private Bitmap bitMapSendHelper;
	private int bitmapTypeHelper;
	private ViewRef currentViewRef;

	private LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(
			computeProperMaxsize()) {

		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getHeight() * value.getRowBytes();
		}

		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			// TODO Auto-generated method stub
			super.entryRemoved(evicted, key, oldValue, newValue);
			// 已经从linkedhashmap中删除引用，应此不需要做什么

		}
	};
	private LoadFromNetHandler loadFromNetHandler;

	class LoadFromNetHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_DOWNLOAD:
				byte[] ret = Util.getByteViaNet((String) msg.obj);
				if (ret == null) {
					UIHandler.obtainMessage(MSG_DOWNLOAD_FAIL).sendToTarget();
					break;
				}
				bitmapTypeHelper = Util.getImageType(ret);
				bitMapSendHelper = processByte(ret);
				if (bitMapSendHelper == null) {
					UIHandler.obtainMessage(MSG_DOWNLOAD_FAIL).sendToTarget();
					break;
				}
				String url=currentViewRef.url;
				Bitmap bitmap=bitMapSendHelper;
				UIHandler.obtainMessage(MSG_DOWNLOAD_DONE).sendToTarget();
				saveToCache(url, bitmap);
				saveToSd(url, bitmap);
				break;
			case MSG_STOP:
				getLooper().quit();
				break;
			default:
				break;
			}

		}

		public LoadFromNetHandler(Looper looper) {
			super(looper);
		}

	}

	private Handler UIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_DOWNLOAD_DONE:
				if (bitMapSendHelper == null)
					break;
				if (currentViewRef == null)
					break;
				if (currentViewRef.url != null & currentViewRef.view != null
						&& currentViewRef.url.equals(currentViewRef.view.url))
					currentViewRef.view.setImageBitmap(processBitmap(
							bitMapSendHelper, currentViewRef.view));
				break;
			case MSG_DOWNLOAD_FAIL:

				break;
			default:
				break;
			}

			clearRefToJunkBitmap();
			// 最终，去stack取新的任务下载
			LOADFROMNET_IDLE = true;
			tryPopSend();
		}

	};

	public Bitmap loadFromCache(String url) {
		return lruCache.get(url);
	}

	public Bitmap loadFromSd(String url) {
		// TODO Auto-generated method stub
		String urlToSdPath = urlToSdPath(url);
		if (!new File(urlToSdPath).exists())
			return null;

		return BitmapFactory.decodeFile(urlToSdPath);
	}

	public void loadFromNet(String url, Picture view) {
		if (loadFromNetHandler == null) {
			HandlerThread handlerThread = new HandlerThread("imageViaNetLoader");
			handlerThread.start();
			loadFromNetHandler = new LoadFromNetHandler(
					handlerThread.getLooper());
		}
		queueNetTask(url, view);
		tryPopSend();

	}

	public void queueNetTask(String url, Picture view) {

		ViewRef viewRef = queueNetTaskHelperMap.get(url);
		if (viewRef == null) {
			viewRef = new ViewRef(view, url);
			queueNetTaskHelperMap.put(url, viewRef);
			stack.push(viewRef);
		} else {
			// queueNetTaskHelperMap 一个nettask的生命周期（从加入任务 到 显示完成为止）
			// currentViewRef生命周期 （下载开始到显示结束）
			if (currentViewRef != null && url.equals(currentViewRef.url))
				// 不在stack中，但作为下载显示任务在进行中
				return;
			else {
				long currentTimeMillis = System.currentTimeMillis();
				stack.remove(viewRef);
				Log.e(tag, "remove cost time = "
						+ (System.currentTimeMillis() - currentTimeMillis));

				stack.push(viewRef);
			}
		}

	}

	public void tryPopSend() {
		if (LOADFROMNET_IDLE && stack.size() > 0) {
			LOADFROMNET_IDLE = false;
			ViewRef pop = stack.pop();
			currentViewRef = pop;
			loadFromNetHandler.obtainMessage(MSG_DOWNLOAD, pop.url)
					.sendToTarget();
		}
	}

	public void showImage(String url, Picture view, int tempPicResId) {

		Bitmap loadFromMap = loadFromCache(url);
		if (loadFromMap == null) {
			Bitmap loadFromSd = loadFromSd(url);
			if (loadFromSd == null) {
				view.setImageResource(tempPicResId);
				loadFromNet(url, view);
			} else {
				saveToCache(url, loadFromSd);
				view.setImageBitmap(processBitmap(loadFromSd, view));
			}
		} else {
			view.setImageBitmap(processBitmap(loadFromMap, view));
		}
	}

	public static PicManagerList getmInstance() {
		if (Looper.getMainLooper() != Looper.myLooper())
			throw new RuntimeException("can't instansiate outside UI thread");
		if (mInstance == null) {
			mInstance = new PicManagerList();
		}
		return mInstance;
	}

	public void saveToCache(String url, Bitmap bitmap) {
		lruCache.put(url, bitmap);
	}

	public void saveToSd(String url, Bitmap bitmap) {
		try {
			if (bitmapTypeHelper == Util.PNG)
				bitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(
						new File(urlToSdPath(url))));
			if (bitmapTypeHelper == Util.JPEG)
				bitmap.compress(CompressFormat.JPEG, 60, new FileOutputStream(
						new File(urlToSdPath(url))));
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public Bitmap processBitmap(Bitmap bitmap, Picture view) {
		if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
			bitmap = Bitmap.createScaledBitmap(bitmap, view.getWidth(),
					view.getHeight(), true);
		}
		return bitmap;
	}

	public int computeProperMaxsize() {
		double memClass = ((ActivityManager) TApplication.getInstance()
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

		// Use 1/16 th of the available memory for this memory cache.

		double mCacheSize = (memClass / 16 < 1) ? (memClass / 16) : 1;
		return (int) (mCacheSize * 1024 * 1024);
	}

	public Bitmap processByte(byte[] Byte) {
		return BitmapFactory.decodeByteArray(Byte, 0, Byte.length);
	}

	public String urlToSdPath(String url) {
		int lastIndexOf = url.lastIndexOf("/");
		if (lastIndexOf == -1)
			return null;
		return Util.getFilesDir()
				+ MD5Util.getMD5String(url.substring(lastIndexOf));
	}

	class ViewRef {
		Picture view;
		String url;

		public Picture getView() {
			return view;
		}

		public void setView(Picture view) {
			this.view = view;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public ViewRef(Picture view, String url) {
			super();
			this.view = view;
			this.url = url;
		}

	}

	public void exit() {
		loadFromNetHandler.sendMessageAtFrontOfQueue(loadFromNetHandler
				.obtainMessage(MSG_STOP));
	}

	public void clearStack() {
		stack.removeAllElements();
	}

	public void clearCache() {
		lruCache.evictAll();
	}

	public void clearStackAndCache() {
		clearStack();
		clearCache();
	}

	public void clearRefToJunkBitmap() {
		// TODO Auto-generated method stub
		bitMapSendHelper = null;
		queueNetTaskHelperMap.remove(currentViewRef);
		currentViewRef.view = null;
		currentViewRef = null;
	}

	public boolean isInCache(String url) {
		if (lruCache.get(url) != null)
			return true;
		return false;
	}

}
