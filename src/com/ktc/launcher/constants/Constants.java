package com.ktc.launcher.constants;

public interface Constants {    
    public interface Config {
        public static final boolean DEBUG = true;
    }
    public static final String TAG="Launch";    
    public static final String SAVE_APPLIST="iappwidget_applist";    
    public static final String APPUPDATA_ACTION="com.ktc.launcher.ui.iappwidget.appupdata_action"; 
    public static final int SETSOURCE_MS=0x002;
    public static final int DEBUGLEVEL=0;  //0不输出日志  7输出全部
    
    public static final String[] miniAppPackageName={
        "com.mstar.tv.tvplayer.ui",     //1 TV Plater
        "com.jrm.localmm",     //2
        "com.android.browser", 		//3
        "com.android.tv.settings", //4
        "",						//5 
        "",						//6 
        "",                    //7
        "",                    //8
        ""                     //9
    };
}
