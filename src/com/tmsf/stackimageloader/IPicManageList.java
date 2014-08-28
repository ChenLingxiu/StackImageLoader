package com.tmsf.stackimageloader;

import android.graphics.Bitmap;

public interface IPicManageList {
	
	public void loadFromNet(String url,Picture view);
	public Bitmap loadFromCache(String url);
	public Bitmap loadFromSd(String url);
	public void tryPopSend();
	public void showImage(String url,Picture view,int tempPicResId);
	public void queueNetTask(String url,Picture view);
	public void saveToCache(String url,Bitmap bitmap);
	public void saveToSd(String url,Bitmap bitmap);
	public String urlToSdPath(String url);
	public void exit();
	public void clearStack(); 
	public void clearCache(); 
	public void clearStackAndCache(); 
	public Bitmap processBitmap(Bitmap bitmap,Picture view);
	public Bitmap processByte(byte[] Byte);
	public int computeProperMaxsize();
	public void clearRefToJunkBitmap();
	public boolean isInCache(String url);
}
