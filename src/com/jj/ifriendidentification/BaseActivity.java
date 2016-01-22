package com.jj.ifriendidentification;

import java.util.Map;

import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.Activity;
import android.util.Log;

public class BaseActivity extends Activity {
    protected void getWebServiceJson(final String tag, String path, Map<String,String> query) {
    	String queryString = "";
    	for (Map.Entry<String, String> entry:query.entrySet()) {
    		if(queryString!=null) queryString += "&";
    		queryString += (entry.getKey() + "=" + entry.getValue());
    	}
    	
    	String fullpath = path;
    	if(queryString.length()>0) {
        	fullpath += "?";
        	fullpath += queryString;
    	}
    	getWebServiceJson(tag, fullpath);
    }
    
	protected void getWebServiceJson(final String tag, String url) {
		this.simpleWebServiceJson(tag, HttpRequest.HttpMethod.GET, url, null);
	}
    
    protected void postWebServiceJson(final String tag, String path, Map<String,String> query) {
    	RequestParams params = new RequestParams();
    	for (Map.Entry<String, String> entry:query.entrySet()) {
    		Log.d("debug", entry.getKey());
    		Log.d("debug", entry.getValue());
    		params.addBodyParameter(entry.getKey(),entry.getValue());
    	}
    		
    	this.simpleWebServiceJson(tag, HttpRequest.HttpMethod.POST, path, params);
    }
	
    public void simpleWebServiceJson(final String tag, HttpMethod method, String queryString, RequestParams params) {
    	HttpUtils http = new HttpUtils("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X; en-us) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53");
    	//http.configUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X; en-us) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53");
    	http.send(method, queryString, params, new RequestCallBack<String>() {
            
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				onWebServiceSuccess(responseInfo, tag);
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				if(tag.length()>0) {
					httpFail(tag, new HttpException(error), msg);
				}
			}
    	});
    }

    protected void onWebServiceSuccess(ResponseInfo<String> responseInfo, String tag) {
		if(tag.length()>0) {
			try {
				String data = responseInfo.result;
				if (data != null) {
					JSONObject jsonObject = new JSONObject(data);
					parseSuccess(tag,jsonObject);
				} else {
					parseFail(tag);
				}
			} catch (Exception e) {
				httpFail(tag, new HttpException(e), e.toString());
			}
		}
    }
    
	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// http callback//////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	protected void parseSuccess(String tag, JSONObject json) throws JSONException, DbException{}
	protected void parseFail(String tag){}
	protected void httpFail(String tag, HttpException exception, String e){}
}
