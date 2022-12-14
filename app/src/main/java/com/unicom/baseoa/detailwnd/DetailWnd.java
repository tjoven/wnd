package com.unicom.baseoa.detailwnd;
 
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.speech.setting.TtsSettings;
import com.iflytek.sunflower.FlowerCollector;
import com.iflytek.voicedemo.TtsDemo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.unicom.baseoa.detailwnd.DocDown.OnDocDownListener;
import com.unicom.baseoa.imgcache.ImgCache;
import com.unicom.baseoa.util.CrashHandler;
import com.unicom.baseoa.util.Utility;
import com.unicom.sywq.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.FrameLayout;
import android.widget.Toast;
import unigo.utility.Worker;
 

public class DetailWnd extends Activity implements OnDocDownListener{
	private static String TAG = TtsDemo.class.getSimpleName(); 	
	
	private ProgressDialog pd=null;
	
	private String mFileName = null;
	private String mFilePath = null;
	private MediaPlayer mPlayer = null;
	private boolean downMP3flag=true;
	private WebView webview = null;
    private FrameLayout mLayout;    // ???????????????????????????
    private View mCustomView;	//???????????????????????????View
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    //??????
    private FrameLayout fullscreenContainer;
    /** ?????????????????? */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    
    private WebChromeClient.CustomViewCallback customViewCallback;
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		Toast.makeText(BrowserWnd.this, "?????????",
//				Toast.LENGTH_LONG).show();
		//???title  
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//	           // ???????????????
//	            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//	          //  ???????????????
//	          //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//			}
		//??????  
//		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,    
//		              WindowManager.LayoutParams. FLAG_FULLSCREEN);   
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN ,    
//	              WindowManager.LayoutParams. FLAG_LAYOUT_IN_SCREEN);   
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.detail_wnd);
		webview=(WebView) this.findViewById(R.id.detail_webview);
		//webview = (WebView) this.findViewById(R.id.detail_webview);
		initWebView(webview);
		mLayout=(FrameLayout) findViewById(R.id.fl_video);
		CrashHandler crashHandler = CrashHandler.getInstance();  
		crashHandler.init(this);
		// ?????????????????????
//		showTip("// ?????????????????????");
					mTts = SpeechSynthesizer.createSynthesizer(DetailWnd.this, mTtsInitListener);
					mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
					mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
					
		//?????????
    	pd=new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("???????????????????????????...");
		
 		String newurl = this.getIntent().getStringExtra("newUrl");
//		webview.loadUrl(newurl);
 		String packageName = null;
		try{
 		PackageManager pm = this.getPackageManager();
		PackageInfo pi = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
		packageName = pi.packageName;
		}catch(Exception e){
			
		}
		webview.loadUrl( "file:///data/data/" + packageName
				+ "/files/wap_html/"+newurl );
		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(webview.getVisibility()==0){
				String url=webview.getUrl();
				if(url.endsWith(".png")||url.endsWith(".jpg")||url.endsWith(".JPEG")||url.endsWith(".jpeg")||url.endsWith(".PNG")||url.endsWith(".JPG")){
					if(webview.canGoBack()){
						 
						webview.goBack();
						return true;
					}
				}
				 
				finish();
			}else{
				return super.onKeyDown(keyCode, event);
			}
        } 
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onResume() {
		//????????????????????????
		FlowerCollector.onResume(DetailWnd.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}
 
	@SuppressLint({ "NewApi", "NewApi" })
	private void initWebView(WebView webView){
		
    	
    	WebSettings setings = webView.getSettings();
    	setings.setTextZoom(100);
    	webView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                return true;
            }
        });
    	/*this.webView.setScrollbarFadingEnabled(false);
    	this.webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);//???????????????
    	//Horizontal???????????????Vertical????????????
    	webView.setHorizontalScrollBarEnabled(false);*/
     	//webView.setVerticalScrollBarEnabled(true);   
    	
    	//setings.setAllowFileAccess(true);//??????????????????????????????
    	//??????????????????
    	setings.setBuiltInZoomControls(true); //??????????????????
    	setings.setBuiltInZoomControls(true);//??????????????????
    	//setings.setLightTouchEnabled(true);
    	setings.setUseWideViewPort(true); //?????????????????????????????????view
    	setings.setLoadWithOverviewMode(true);//????????????????????????
		//this.webView.setWebChromeClient(new WebChromeClient());
		setings.setJavaScriptEnabled(true);// ??????WebSetting?????????????????????Javascript?????????
		webView.requestFocus();//?????????????????????
		//??????
		WebView.setWebContentsDebuggingEnabled(true);//????????????
		 setings.setPluginState(WebSettings.PluginState.ON);
		 setings.setLoadsImagesAutomatically(true);
		 setings.setDomStorageEnabled(true);
		 
//		   setings.setMediaPlaybackRequiresUserGesture(false);
		    setings.setAllowFileAccessFromFileURLs(true);
		    setings.setAllowContentAccess(true);

		 
		//??????????????????
		setings.setCacheMode(setings.LOAD_NO_CACHE); 
		//??????????????????java?????????????????????javascript??????
		webView.addJavascriptInterface(this, "hosturl");
		webView.addJavascriptInterface(this, "username");
		//this.webView.addJavascriptInterface(this, "getUsername"); 
		webView.addJavascriptInterface(this, "navigation");//???????????????
		webView.addJavascriptInterface(this, "loadurl");
		webView.addJavascriptInterface(this, "urlpara");
		webView.addJavascriptInterface(this, "webview");
		try{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT<=17){
				webView.removeJavascriptInterface("searchBoxJavaBridge_");
				webView.removeJavascriptInterface("accessibility");
				webView.removeJavascriptInterface("accessibilityTraversal");
			}
		}catch(Exception e){
			
		}
		
         /**?????????????????????
          *  NORMAL???????????????????????????????????????
          *  SINGLE_COLUMN????????????????????????WebView???????????????????????????
          *  NARROW_COLUMNS???????????????????????????????????????????????????????????????
          */
		setings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		//webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		//???????????????
		setings.setSavePassword(false);
		
	    // this.webView.setInitialScale(67);//?????????????????????
		//this.webView.setInitialScale(100);//?????????????????????
		//????????????????????????
		setings.setDefaultZoom(ZoomDensity.MEDIUM);//????????????????????????
//		this.webView.addJavascriptInterface(new GetContactInfo(this), "android");
		
		webView.clearCache(true);
		webView.setDownloadListener(downloadListener);
		webView.setWebViewClient(webViewClient);
		webView.setWebChromeClient(webChromeClient);
   }
 
	@Override
	 public void onPause(){
		//????????????????????????
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(DetailWnd.this);
        super.onPause();
	}
	
//	private void initWebViewNew(WebView wvPlayer){
////		wvPlayer = (WebView) findViewById(R.id.wv_main);
//		    WebSettings settings = wvPlayer.getSettings();
//		    settings.setJavaScriptEnabled(true);
//		    settings.setUserAgentString("FortuneAndroidInnerWebPlayer V1.0");
//		    settings.setLoadsImagesAutomatically(true);
//		    settings.setUseWideViewPort(true);
//		    settings.setLoadWithOverviewMode(true);
//		    settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
////		    settings.setPluginState(WebSettings.PluginState.ON);
//		    settings.setDomStorageEnabled(true);
////		    settings.setMediaPlaybackRequiresUserGesture(false);
////		    settings.setAllowFileAccessFromFileURLs(true);
////		    settings.setAllowContentAccess(true);
//		    wvPlayer.clearCache(true);
//		    wvPlayer.setWebViewClient(webViewClient);
//		    wvPlayer.setWebChromeClient(webChromeClient);
//		}

	private WebViewClient webViewClient = new WebViewClient() {
		 public boolean shouldOverrideUrlLoading(final WebView view, String url) 
		 {
			 Log.e("DetailWnd--","?????????shouldOverrideUrlLoading");
//		      if (url.startsWith("sms:") || url.startsWith("tel:")) { 
//	                Intent intent = new Intent(Intent.ACTION_VIEW,
//	                        Uri.parse(url)); 
//	                startActivity(intent); 
//	            }else {
//	            	view.loadUrl(url);//????????????
//	            }
//            return true;   
			 
			 if(url.indexOf(".do?")>0){
					String str = DetailWnd.this.getString(R.string.SETTING_INFOS);
					SharedPreferences settings = DetailWnd.this.getSharedPreferences(str, 0);
				    url =   url+ "&sys_token_khd=" + settings.getString("sys_token_khd", "");
				}
				 Pattern p = Pattern.compile("/\\d{22}\\.[A-Za-z0-9]{1,4}$"); 
				 Matcher m = p.matcher(url); 
				 boolean b = m.find();
				 Log.v("shouldOverrideUrlLoading", b+"===="+url);
			      if (url.startsWith("sms:") || url.startsWith("tel:")) { 
		                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url)); 
		                startActivity(intent); 
		            }else{

			    		if(b||url.endsWith(".mp4") || url.endsWith(".MP4")||url.endsWith(".flv") || url.endsWith(".FLV")||ImgCache.isImgHttpUrl(url)){
		            		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  
		            			Toast t=Toast.makeText(DetailWnd.this, "???SD??????SD???????????????["+Environment.getExternalStorageState()+"]", Toast.LENGTH_LONG);  
		            			//t.setGravity(Gravity.CENTER, 0, 0);  
		            			t.show();  
		            		}else{
		            			//?????????
		            			if(!ImgCache.isImgHttpUrl(url)){
		            				pd.setMessage("???????????????????????????...");
		            				pd.setCancelable(false);
		            				pd.show();
		            			}
		            			final String url2=url;
		            			new Thread() {
		           				 @Override
		           				 public void run() {
			           					Uri uri = Uri.parse(url2);
				                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
				                        String fileName=Uri.decode(intent.getDataString());
				                        System.out.println("fileName==="+fileName);
			           					 Worker worker=new Worker(1);
			           					 DocDown HttpDocDown=new DocDown(DetailWnd.this,url2,fileName);
			           					 worker.doWork(HttpDocDown);
		           				     }
		           				 }.start();
		            		}
		            	}else if(url.endsWith(".m3u8")){
		            		Uri uri = Uri.parse(url);
		                   Intent intent = new Intent(Intent.ACTION_VIEW,uri);
		                   startActivity(intent);
		            	}else if(url.endsWith("&sysbrowser")){//???????????????????????????
		            		url=url.substring(0, url.indexOf("&sysbrowser"));
		            		Uri uri = Uri.parse(url);
			                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
			                startActivity(intent);
			            }else {
		            		view.loadUrl(url);//????????????
		            	}
		            }
	             return true;   
			 
        }//??????????????????,???webview??????
		 
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				//handler.sendEmptyMessage(0);
				Log.e("DetailWnd--","?????????url="+url);
				Log.v("debug", url);
				super.onPageStarted(view, url, favicon);

			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				 if(url.endsWith(".mp4") || url.endsWith(".MP4"))
				 {  
				     Intent i = new Intent(Intent.ACTION_VIEW);  
					 i.setDataAndType(Uri.parse(url),"video/mp4");
					 startActivity(i); 
				} 
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				if(failingUrl.contains("?")&&!failingUrl.startsWith("http://")){
				String[] temp = failingUrl.split("\\?");
			 
				view.loadUrl(temp[0]);
				}else{
			 
				Toast.makeText(DetailWnd.this, "?????????????????????", Toast.LENGTH_LONG).show();
				}
			}	
			
	};
	 	
private	WebChromeClient webChromeClient = new WebChromeClient(){
	
		
		public void onProgressChanged(WebView view,int progress){//??????????????????????????? 
        	if(progress==100){
       		//handler.sendEmptyMessage(1);//??????????????????,?????????????????????
       	}   
           super.onProgressChanged(view, progress);   
       } 
	    @Override
	    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
	        AlertDialog.Builder b2 = new AlertDialog.Builder(view.getContext())
	                .setTitle("??????").setMessage(message)
	                .setPositiveButton("??????",
	                        new AlertDialog.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialog,
	                                    int which) {
	                                result.confirm();
	                                // MyWebView.this.finish();
	                            }
	                        });

	        b2.setCancelable(false);
	        b2.create();
	        b2.show();
	        return true;
	    }
	    
	    //????????????????????????
        @Override
        public void onShowCustomView(final View view, CustomViewCallback callback) {
        	
            super.onShowCustomView(view, callback);
            //??????view ????????????????????????
            if (mCustomView != null) {
            	Log.e("DetailWnd--","?????????onShowCustomView null");
                callback.onCustomViewHidden();
                return;
            }
            Log.e("DetailWnd--","zhixingdaoonShowCustomView");

            mCustomView = view;
            mCustomView.setVisibility(View.VISIBLE);
            mCustomViewCallback = callback;
            mLayout.addView(mCustomView);
            mLayout.setVisibility(View.VISIBLE);
            mLayout.bringToFront();
//            view.post(new Runnable() {
//            	@Override
//            	public void run() {
//            		
//            		Log.e("DetailWnd--","??????"+view.getHeight()+"??????"+view.getWidth());
//                    if(view.getHeight()>=view.getWidth()) {
//                        //????????????
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                    }else {
//                        //????????????
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                    }
//            	}
//
//            });
            
            //????????????
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            
            if (mCustomView == null) {
            	Log.e("DetailWnd--","?????????onHideCustomView null");
                return;
            }
            Log.e("DetailWnd--","zhixingdaoonHideCustomView");            
            mCustomView.setVisibility(View.GONE);
            mLayout.removeView(mCustomView);
            mCustomView = null;
            mLayout.setVisibility(View.GONE);
            try {
                mCustomViewCallback.onCustomViewHidden();
            } catch (Exception e) {
            	Log.e("DetailWnd--", "yichang");
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//??????
        }
   } ;
   private void setStatusBarVisibility(boolean visible) {
       int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
       getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
   }
   /** ?????????????????? */
   static class FullscreenHolder extends FrameLayout {

       public FullscreenHolder(Context ctx) {
           super(ctx);
           setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
       }

       @Override
       public boolean onTouchEvent(MotionEvent evt) {
           return true;
       }
   }
   private void fullScreen() {
	    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    } else {
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	}

   
//   /**
//    * ?????????????????????
//    */
//   @Override
//   public void onConfigurationChanged(Configuration config) {
//       super.onConfigurationChanged(config);
//       switch (config.orientation) {
//           case Configuration.ORIENTATION_LANDSCAPE:
//               getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//               getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//               //mToolbar.setVisibility(View.GONE);
//               break;
//           case Configuration.ORIENTATION_PORTRAIT:
//               getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//               getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//               //mToolbar.setVisibility(View.VISIBLE);
//               break;
//       }
//   }
   
 private  DownloadListener downloadListener = new DownloadListener(){

		@Override
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype,
				long contentLength) {
			if(url.endsWith(".pdf") || url.endsWith(".ppt")
					|| url.endsWith(".docx") || url.endsWith(".xlsx")
					|| url.endsWith(".pptx") || url.endsWith(".doc")
					|| url.endsWith(".xls") || url.endsWith(".rar")
					|| url.endsWith(".jpg")|| url.endsWith(".png")
					|| url.endsWith(".zip")|| url.endsWith(".txt")
					|| url.endsWith(".mp4"))
			{
			  Log.e("DetailWnd--", "???????????????url=="+url);
			   //?????????????????????
              Uri uri = Uri.parse(url);
              Intent intent = new Intent(Intent.ACTION_VIEW,uri);
              startActivity(intent);
			}
		}};

		public void asyncHttpRequest(String url,String data,final String sys_timeStamp,final String callback){
			String str = DetailWnd.this.getString(R.string.SETTING_INFOS);
			SharedPreferences settings = DetailWnd.this.getSharedPreferences(str, 0);
			AsyncHttpClient client = new AsyncHttpClient(true,80,443);
			//????????????????????????cookie
	    	final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
	    	client.setCookieStore(myCookieStore);
	    	final String settingUrl = settings.getString("httpMain", ""); 
			url = settingUrl  + url+ "&sys_token_khd=" + settings.getString("sys_token_khd", "");
			//url = url+"&"+java.net.URLEncoder.encode(data);;
			//url = url.replaceAll(" ", "%20");
			System.out.println("11111111111="+url);
			String[] dataArr = data.split("&");
			//String[] dataArr ={};
			RequestParams params = new RequestParams();
			Map map=new HashMap();
			for(String param:dataArr){
				String key ="";
				String value = "";
				int index=param.indexOf("=");
				if(index==-1){
					continue;
				}
				key=param.substring(0, index);
				value=param.substring(index+1, param.length());
				Set set=new HashSet();
				 if(map.containsKey(key)){
					 set=(Set)map.get(key);
				 }
				set.add(value);
				map.put(key,set);
				params.put(key, set);
			}
            //url=java.net.URLEncoder.encode(url);
			client.post(url, params, new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			    	if(!"".equals(callback)){
			    		System.out.println("3333333333333333");
			    		String str="";
			    		if(response.indexOf("login.do?method=exit")>-1){
			    			str="{\"error\":\""+response+"\"}";
			    		}else{
			    			str=response;
			    		}
			    		final String response2=str;
//			    		if(DetailWnd.this.webview.isShown()){
			    			DetailWnd.this.runOnUiThread(new  Runnable(){
			    				public void run() { 
			    					DetailWnd.this.webview.loadUrl("javascript:"+callback+"("+response2+",'"+sys_timeStamp+"')");
			    				}
			    			});
			    			
//			    		}
//			    		if(DetailWnd.this.webView2.isShown()){
//			    			DetailWnd.this.runOnUiThread(new  Runnable(){
//			    				public void run() { 
//			    					DetailWnd.this.webView2.loadUrl("javascript:"+callback+"("+response2+",'"+sys_timeStamp+"')");
//			    				}
//			    				});
//			    			
//			    		}
			    	}
			    	
			    }
				@Override
				public void onFailure(Throwable error, String content)
				{
					System.out.println("444444444444444444444");
					if(DetailWnd.this.webview.isShown()){
						DetailWnd.this.runOnUiThread(new  Runnable(){
		    				public void run() { 
		    					DetailWnd.this.webview.loadUrl("javascript:exceptionCallBack()");
		    				}
		    				});
					}
//					if(DetailWnd.this.webView2.isShown()){
//						DetailWnd.this.runOnUiThread(new  Runnable(){
//		    				public void run() { 
//		    					DetailWnd.this.webView2.loadUrl("javascript:exceptionCallBack()");
//		    				}
//		    				});
//					}
					
					//Log.e(TAG , "onFailure error : " + error.toString() + "content : " + content);
				}
			});

		}
		
//		public String getHostUrl(){
//			String url = Utility.iURL;
//			if(url!=null&&!url.startsWith("http://")){
//				url = "http://"+url;
//			}
//			return url;
//		}
		
		public String getHostUrl(){
			String url = Utility.iURL;
			if(url!=null&&!url.startsWith("https://")){
				url = "https://"+url;
			}
			return url;
		}
		
		public void viewDetail(String url){
			Intent intent = new Intent(this, DetailWnd.class);
			intent.putExtra("newUrl", url);
		    startActivity(intent);
		}
		
		public void closeDetail(){
			 finish();
		}





		@Override
		public void onDocDown(DocDown http) {
			if(http.getSucceed()==true){
				startWordFileIntent(http.getCardURL());
				pd.cancel();
				pd.dismiss();
			}else{
				Toast t=Toast.makeText(DetailWnd.this, http.getReason(), Toast.LENGTH_LONG);  
				t.setGravity(Gravity.CENTER, 0, 0);  
				t.show();
				pd.cancel();
				pd.dismiss();
				return; 
			}
		}
		
		//doc ????????????
				public void startWordFileIntent(String param){
					Intent intent = getFileIntent(param);
					try{
						if(downMP3flag){
							this.startActivity(intent);
						}else{
							startPlayServer();
						}
						downMP3flag=true;
					}catch(Exception e){
						String path = android.os.Environment.getExternalStorageDirectory().getPath();
						Toast.makeText(DetailWnd.this, "?????????????????????????????????????????????????????????????????????"+path+"/Undownload???????????????", Toast.LENGTH_LONG).show();  
					}
				}
				
				public Intent getFileIntent(String fileName) {
					Uri uri = Uri.fromFile(new File(fileName));
					String type = getMIMEType(fileName);
					Intent intent = new Intent();
					// Intent intent = new Intent("android.intent.action.VIEW");
					intent.setAction("android.intent.action.VIEW");
					intent.addCategory("android.intent.category.DEFAULT");
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setDataAndType(uri, type);
					return intent;
				}

				private String getMIMEType(String fName) {
					String type = "";

					String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

					/* ???????????????????????????MimeType */
					if (end.equals("pdf")) {
						type = "application/pdf";//
					} else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
							|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
						type = "audio/*";
					} else if (end.equals("3gp") || end.equals("mp4")||end.equals("swf")||end.equals("flv")) {
						type = "video/*";
					} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
							|| end.equals("jpeg") || end.equals("bmp")) {
						type = "image/*";
					} else if (end.contains("doc")) {
						type = "application/msword";
					} else if (end.contains("xls")) {
						type = "application/vnd.ms-excel";
					} else if (end.contains("ppt")) {
						type = "application/vnd.ms-powerpoint";
					} else {
						type = "*/*";
					}
					return type;
				}
				
				 //???????????????????????????
			    private void startPlayServer() {
			        mPlayer = new MediaPlayer();
			        String saveDir = Environment.getExternalStorageDirectory()+ "/Undownload/";
				    mFilePath = saveDir + mFileName;
			        try {
			        	//????????????????????????
			            mPlayer.setDataSource(mFilePath);
			            mPlayer.prepare();
			            //?????????
			            mPlayer.start();
			        } catch (IOException e) {
			        	e.printStackTrace();
			            
			        }
			    }
			    
			    
			    

				// ??????????????????
				private SpeechSynthesizer mTts;
			 
				// ????????????
				private int mPercentForBuffering = 0;
				// ????????????
				private int mPercentForPlaying = 0;
				 
				private Toast mToast;
				private SharedPreferences mSharedPreferences;
				
				LinkedList<String> textList = null;
				public void startSpeaking(String text){
					textList = new LinkedList<String>();
					if(text.length()>4000){
						textList.add(text.substring(0, 4000));
						textList.add(text.substring(4000));
					}else{
						textList.add(text);
					}
					if(textList.size()>0){
						startSpeaking(textList.getFirst(),0);
						textList.removeFirst();
					}
				}
				
				 
			 public void startSpeaking(String text,int flag){
//				 showTip(text.length()+"");
//				 Regex regex = new Regex("<.+?>", RegexOptions.IgnoreCase);
//			     string strOutput = regex.Replace(strHtml, "");//?????????"<"???">"???????????????
//			     strOutput = strOutput.Replace("<", "");
//			     strOutput = strOutput.Replace(">", "");
//			     strOutput = strOutput.Replace("&nbsp;", "");

				 
					 	// ?????????????????????????????????????????????
						FlowerCollector.onEvent(DetailWnd.this, "tts_play");
						
						// ????????????
						setParam();
						 if(null != mTts){
						int code = mTts.startSpeaking(text, mTtsListener);
			 
						
						if (code != ErrorCode.SUCCESS) {
//							showTip("??????????????????,?????????: " + code);
						}}
			 }
			 public void stopSpeaking(){
						 if(null != mTts){
							 
							 mTts.stopSpeaking();
						 }
			 }
			 public void pauseSpeaking(){
				 if(null != mTts){
						mTts.pauseSpeaking();
				 }
			 }
			 public void resumeSpeaking(){
				 if(null != mTts){
						mTts.resumeSpeaking();
				 }
					 
				}
				private int selectedNum = 0;
				 
				/**
				 * ??????????????????
				 */
				private InitListener mTtsInitListener = new InitListener() {
					@Override
					public void onInit(int code) {
//						Log.d(TAG, "InitListener init() code = " + code);
						if (code != ErrorCode.SUCCESS) {
//			        		showTip("???????????????,????????????"+code);
			        	} else {
							// ????????????????????????????????????startSpeaking??????
			        		// ????????????????????????onCreate???????????????????????????????????????????????????startSpeaking???????????????
			        		// ?????????????????????onCreate??????startSpeaking??????????????????
//			        		showTip("???????????????????????????!");
						}		
					}
				};

				/**
				 * ?????????????????????
				 */
				private SynthesizerListener mTtsListener = new SynthesizerListener() {
					
					@Override
					public void onSpeakBegin() {
//						showTip("????????????");
					}

					@Override
					public void onSpeakPaused() {
//						showTip("????????????");
					}

					@Override
					public void onSpeakResumed() {
//						showTip("????????????");
					}

					@Override
					public void onBufferProgress(int percent, int beginPos, int endPos,
							String info) {
						// ????????????
						mPercentForBuffering = percent;
						showTip(String.format(getString(R.string.tts_toast_format),mPercentForBuffering, mPercentForPlaying));
					}

					@Override
					public void onSpeakProgress(int percent, int beginPos, int endPos) {
						// ????????????
						mPercentForPlaying = percent;
						showTip(String.format(getString(R.string.tts_toast_format),
								mPercentForBuffering, mPercentForPlaying));
					}

					@Override
					public void onCompleted(SpeechError error) {
						if (error == null) {
//							showTip("????????????");
							if(textList!=null && textList.size()>0){
								startSpeaking(textList.getFirst(),0);
								textList.removeFirst();
							}else{
								DetailWnd.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										webview.loadUrl("javascript:onSpeakCompleted();");
									}
								});
							}
							
						} else if (error != null) {
//							showTip(error.getPlainDescription(true));
						}
					}

					@Override
					public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
						// ??????????????????????????????????????????id??????????????????????????????id??????????????????????????????????????????????????????????????????????????????
						// ??????????????????????????????id???null
							if (SpeechEvent.EVENT_SESSION_ID == eventType) {
								String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
								unigo.utility.Log.write(1, "kdxf_onEvent", "session id =" + sid , "");
							}
					}
				};

				private void showTip(final String str) {
					mToast.setText(str);
					DetailWnd.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							mToast.show();
							
						}
					});
				}

				/**
				 * ????????????
				 * @return
				 */
				private void setParam(){
					// ????????????
					mTts.setParameter(SpeechConstant.PARAMS, null);
					// ????????????
					String mEngineType = SpeechConstant.TYPE_CLOUD;

					// ????????????????????????????????????
					if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
						mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
						// ???????????????????????????
						mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
						//??????????????????
						mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
						//??????????????????
						mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
						//??????????????????
						mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
					}else {
						mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
						// ??????????????????????????? voicer???????????????????????????????????????????????????
						mTts.setParameter(SpeechConstant.VOICE_NAME, "");
						/**
						 * TODO ????????????????????????????????????????????????????????????????????????
						 * ??????????????????????????????????????????????????????????????????
						 */
					}
					//??????????????????????????????
					mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
					// ??????????????????????????????????????????????????????true
					mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

					// ???????????????????????????????????????????????????pcm???wav??????????????????sd????????????WRITE_EXTERNAL_STORAGE??????
					// ??????AUDIO_FORMAT??????????????????????????????????????????
					mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
					mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
				}
				
				@Override
				protected void onDestroy() {
					super.onDestroy();
					
					if( null != mTts ){
						mTts.stopSpeaking();
						// ?????????????????????
						mTts.destroy();
					}
			        //????????????cookie
//			        CookieSyncManager.createInstance(WebActivity.this);
//			        CookieManager cookieManager = CookieManager.getInstance();
//			        cookieManager.removeAllCookie();
//			        CookieSyncManager.getInstance().sync();
			        webview.setWebChromeClient(null);
			        webview.setWebViewClient(null);
			        webview.getSettings().setJavaScriptEnabled(false);
			        webview.clearCache(true);
			        webview.destroy();
				}
				
				 
				 
}
