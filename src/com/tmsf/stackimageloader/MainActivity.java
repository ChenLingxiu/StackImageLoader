package com.tmsf.stackimageloader;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Picture img = (Picture) findViewById(R.id.img);
		Picture img2 = (Picture) findViewById(R.id.img2);
		Picture img3 = (Picture) findViewById(R.id.img3);
		img.setImageURL("http://b.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=30461668ab014c080d3620f76b12696d/d4628535e5dde711498dd837a5efce1b9c166190.jpg");
		img2.setImageURL("http://b.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=8d24b3b6e9f81a4c323fe49bb6430b3c/4034970a304e251f14dde53aa586c9177e3e53cd.jpg");
		img3.setImageURL("http://c.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=815f359bfc039245b5b8e95de6fdcfa7/54fbb2fb43166d220ddbd6c2462309f79052d23c.jpg");

	}

}
