package com.ktc.launcher.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Toast工具类
 * 
 */
public class ToastUtils {
	private static Toast mToast = null;

	/**
	 * Toast发送消息，默认Toast.LENGTH_SHORT
	 * 
	 */
	public static void showMessageShort(Context context, String msg) {
		showToast(context, msg, Toast.LENGTH_SHORT);
	}

	/**
	 * Toast发送消息，Toast.LENGTH_LONG
	 */
	public static void showMessageLong(Context context, String msg) {
		showToast(context, msg, Toast.LENGTH_LONG);
	}

	/**
	 * Toast发送消息，默认Toast.LENGTH_SHORT
	 * 
	 */

	public static void showToast(Context context, String text, int duration) {
		if (mToast == null) {
			mToast = Toast.makeText(context, text, duration);
		} else {
			mToast.setText(text);
			mToast.setDuration(duration);
		}
		int heightFromBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
				context.getResources().getDisplayMetrics());
		mToast.setGravity(Gravity.BOTTOM, 0, heightFromBottom);
		mToast.show();
	}

	/**
	 * 关闭当前Toast
	 * 
	 */
	public static void cancelCurrentToast() {
		if (mToast != null) {
			mToast.cancel();
		}
	}
}
