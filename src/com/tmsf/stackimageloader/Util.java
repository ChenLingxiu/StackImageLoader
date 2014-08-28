package com.tmsf.stackimageloader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;

import android.text.TextUtils;
import android.util.Log;

public class Util {

	private static String tag = "Util";
	private static String FILES_DIR;

	public static String Obj2Params(Object obj) {
		try {
			StringBuilder ret = new StringBuilder(256);
			Field[] fields = obj.getClass().getDeclaredFields();
			String and = "&", eq = "=";
			int count = 0;
			for (Field f : fields) {
				f.setAccessible(true);

				String val = (String) f.get(obj);
				if (TextUtils.isEmpty(val))
					continue;
				count++;
				if (count != 1)
					ret.append(and);
				ret.append(f.getName());
				ret.append(eq);
				ret.append(URLEncoder.encode(val, "UTF-8"));
			}
			return ret.toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";

	}

	public static <T> T ObjAddObj(T target, T obj2) {
		try {
			Field[] fields = target.getClass().getDeclaredFields();
			for (Field f : fields) {
				f.setAccessible(true);
				String val = (String) f.get(obj2);
				if (val == null)
					continue;
				f.set(target, val);
			}
			return target;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;

	}

	public static String generateParams(HashMap<String, String> map) {
		StringBuilder ret = new StringBuilder();
		int i = 0;
		String and = "&";
		String equals = "=";
		for (Entry item : map.entrySet()) {
			if (i == 0) {
				// ret.append("?");
			} else {
				ret.append(and);
			}
			ret.append(item.getKey());
			ret.append(equals);
			ret.append(item.getValue());
			i++;
		}
		return ret.toString();

	}

	public static byte[] getByteViaNet(String url) {
		HttpURLConnection urlConnection = null;
		InputStream in = null;
		try {

			URL mUrl = new URL(url);
			urlConnection = (HttpURLConnection) mUrl.openConnection();
			urlConnection
					.addRequestProperty(
							"Accept",
							"image/jpeg,image/png,image/gif, image/x-xbitmap,image/pjpeg, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-shockwave-flash, */*");
			urlConnection.setUseCaches(false);
			urlConnection.addRequestProperty("User-Agent", "Android Linux");
			urlConnection.setRequestMethod("GET");
			urlConnection.setReadTimeout(4000);
			in = urlConnection.getInputStream();
			return readStream(in);
		} catch (Exception e) {
			urlConnection.disconnect();
			return null;
		} finally {
			urlConnection.disconnect();
		}

	}

	public static byte[] readStream(InputStream in)
			throws InterruptedException, IOException {
		byte[] buffer = new byte[8196 * 4];
		int length;
		ByteArrayOutputStream ret = new ByteArrayOutputStream();
		try {

			while ((length = in.read(buffer)) != -1) {
				ret.write(buffer, 0, length);

			}
		} finally {
			ret.close();
			in.close();
		}
		return ret.toByteArray();
	}

	public static String AssembleParams(java.util.Map<String, String> rawParams) {
		StringBuilder pathBuilder = new StringBuilder();
		for (java.util.Map.Entry<String, String> entry : rawParams.entrySet()) {

			pathBuilder.append(entry.getKey()).append("=")

			.append(entry.getValue()).append("&");

		}
		pathBuilder.deleteCharAt(pathBuilder.length() - 1);
		return pathBuilder.toString();
	}

	public static String getStringViaNet(String mUrl,
			java.util.Map<String, String> rawParams) {
		// TODO Auto-generated method stub
		String ret = "fail";
		URL url;
		HttpURLConnection urlConnection = null;
		DataOutputStream out = null;
		BufferedReader in = null;
		try {
			long startTime = System.currentTimeMillis();
			url = new URL(mUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);
			urlConnection.addRequestProperty("User-Agent", "Android Linux");
			urlConnection.setRequestMethod("POST");
			out = new DataOutputStream(urlConnection.getOutputStream());
			out.writeBytes(AssembleParams(rawParams));
			out.flush();
			in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream(), "UTF-8"), 8196 * 4);
			if (urlConnection.getResponseCode() != 200)
				return "fail";

			ret = readStream(in);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // flush and close
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			urlConnection.disconnect();
		}

		return ret;
	}

	interface MyCallBack {
		public void onDone(String result);
	}

	static class MyCallBackC implements MyCallBack {
		public boolean CANCELED = false;

		public void onDone(String result) {
		}
	}

	public static String getStringViaNet(MyCallBackC cbk, String... params) {
		// TODO Auto-generated method stub
		String ret = "fail";
		URL url;
		HttpURLConnection urlConnection = null;
		DataOutputStream out = null;
		BufferedReader in = null;
		if (cbk.CANCELED) {
			cbk.onDone("canceled");
			return "canceled";
		}
		try {
			Log.e(tag, "action :" + params[0]);
			long startTime = System.currentTimeMillis();
			url = new URL(params[0]);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setConnectTimeout(4000);
			urlConnection.addRequestProperty("User-Agent", "Android Linux");
			urlConnection.setRequestMethod("POST");
			if (cbk.CANCELED) {
				cbk.onDone("canceled");
				return "canceled";
			}
			Log.e(tag, "params :" + params[1]);
			out = new DataOutputStream(urlConnection.getOutputStream());
			out.writeBytes(params[1]);
			out.flush();
			if (cbk.CANCELED) {
				cbk.onDone("canceled");
				return "canceled";
			}
			in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream(), "UTF-8"), 8196 * 4);
			Log.e(tag, "onPostInput :"
					+ (System.currentTimeMillis() - startTime));
			if (cbk.CANCELED) {
				cbk.onDone("canceled");
				return "canceled";
			}
			if (urlConnection.getResponseCode() != 200)
				return "fail";
			if (cbk.CANCELED) {
				cbk.onDone("canceled");
				return "canceled";
			}
			ret = readStream(cbk, in);
			Log.e(tag, "onPostReadStream :"
					+ (System.currentTimeMillis() - startTime));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // flush and close
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			urlConnection.disconnect();
		}

		return ret;
	}

	public static String readStream(MyCallBackC cbk, BufferedReader in) {
		StringBuffer ret = new StringBuffer(8196);
		String v;
		try {
			while ((v = in.readLine()) != null) {
				if (cbk.CANCELED) {
					cbk.onDone("canceled");
					return "canceled";
				}
				ret.append(v);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret.toString();
	}

	public static String readStream(BufferedReader in) {
		StringBuffer ret = new StringBuffer(8196);
		String v;
		try {
			while ((v = in.readLine()) != null) {
				ret.append(v);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret.toString();
	}

	static final int GIF = 10, PNG = 11, JPEG = 12, UNKNOWN = 13;

	public static int getImageType(byte abyte0[]) {
		int imagetype;
		// use byte so we got the type
		if (abyte0.length < 10)
			imagetype = UNKNOWN;
		else if (abyte0[0] == 71 && abyte0[1] == 73 && abyte0[2] == 70)
			imagetype = GIF;
		else if (abyte0[1] == 80 && abyte0[2] == 78 && abyte0[3] == 71)
			imagetype = PNG;
		else if (abyte0[6] == 74 && abyte0[7] == 70 && abyte0[8] == 73
				&& abyte0[9] == 70)
			imagetype = JPEG;
		else
			imagetype = UNKNOWN;
		return imagetype;
	}

	public static String getFilesDir() {
		if (Util.FILES_DIR == null)
			Util.FILES_DIR = TApplication.getInstance().getFilesDir()
					.toString();
		return Util.FILES_DIR;
	}
}
