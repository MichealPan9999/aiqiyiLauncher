package com.ktc.launcher;

import java.io.File;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Application;

public class MyApplication extends Application{
 
	public void onCreate() {
		File cacheDir = StorageUtils.getOwnCacheDirectory(this, "imageloader/Cache");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
			.memoryCacheExtraOptions(480, 800)
			.threadPoolSize(3)
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.denyCacheImageMultipleSizesInMemory()
			.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
			.memoryCacheSize(2 * 1024 * 1024)
			.discCacheSize(50 * 1024 * 1024)
			.diskCacheFileNameGenerator(new Md5FileNameGenerator())
			.diskCacheFileCount(100)
			.discCache(new UnlimitedDiskCache(cacheDir){})
			.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
			.imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000))
			.writeDebugLogs()
			.build();
		ImageLoader.getInstance().init(config);
			
	}
}
