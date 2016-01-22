package com.jj.ifriendidentification;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.shelwee.update.UpdateHelper;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends BaseActivity {
	
    String ret = null;
    String message = null;
    String skey = null;
    String wxsid = null;
    String wxuin = null;
    String pass_ticket = null;
    String isgrayscale = null;
    String DEVICE_ID = "e1615250492";
    String baseUrl = null;
    CUser userMe = null;
    String roomname = null;
    int nextUserIndex = 0;
    List<CUser> userList = null;
    List<CUser> delList = null;
    Map<String,String> baseRequest;
    BasicCookieStore httpCookieStore;
    int MAX_GROUP_NUM = 30; // 每组人数
    int SEARCH_INTERVAL = 30 * 1000;
    
    String boadcastName = "boadcastName";
    private LocalReceiver localReceiver = new LocalReceiver();
    
	private void loadBroadcastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(boadcastName);
		BaseApplication.getBroadcastManager().registerReceiver(localReceiver, intentFilter);
	}
    
	public class LocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
				String message = arg1.getStringExtra("message");
				MainActivity.this.onReceiveBroadCast(message);
		}
	}
	
	private void onReceiveBroadCast(String message) {
		TextView view = (TextView)this.findViewById(R.id.dcmText);
		String text = view.getText().toString() + "\n" + message;
		view.setText(text);
		
		final ScrollView scrollView = (ScrollView)this.findViewById(R.id.dcmScrollView);
		scrollView.post(new Runnable() {
		    @Override
		    public void run() {
		    	scrollView.fullScroll(ScrollView.FOCUS_DOWN);
		    }
		});
	}
	
	private void showMessageToTextView(String message) {
		Intent intent = new Intent();
		intent.setAction(boadcastName);
		intent.putExtra("message", message);
		BaseApplication.getBroadcastManager().sendBroadcast(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		loadBroadcastReceiver();
		checkVersion();
		//getUUID();
		letusgo();
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	private void checkVersion() {
		String updateUrl = "http://youjian.ren/ID_0036_Offical/version.json";
		String apkUrl = "http://youjian.ren/ID_0036_Offical/iPicpic_%s_0001.apk";
		UpdateHelper updateHelper = new UpdateHelper.Builder(this)
				.checkUrl(updateUrl, apkUrl).isAutoInstall(true).build();
		updateHelper.check();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void letusgo() {
		new Thread(new Runnable() {
			 
			@Override
			public void run() {
				getUUID();
			}
		}).start();
	}
	
	protected void getQR() {
		new Thread(new Runnable() {
			 
			@Override
			public void run() {
				getQRNow();
			}
		}).start();
	}
	
	private String uuid;
	
	protected void getQRNow() {
		String httpUrl = "https://login.weixin.qq.com/qrcode/" + uuid + "?t=webwx";
		
		ImageView view = (ImageView)this.findViewById(R.id.imageview);
		Picasso.with(this).load(httpUrl).into(view);
	}
	
	protected void getUUID() {
//		Map<String,String> query = new HashMap<String,String>();
//		query.put("appid", "wx782c26e4c19acffb");
//		query.put("fun", "new");
//		query.put("lang", "zh_CN");
//		getWebServiceJson("tag_get_uuid", "http://login.weixin.qq.com/jslogin", query);
		
		String resultData = "";
		String httpUrl = "https://login.weixin.qq.com/jslogin?appid=wx782c26e4c19acffb&fun=new&lang=zh_CN";
		
		
		try
		{
			URL url = new URL(httpUrl);  
			//使用HttpURLConnection打开连接
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestProperty("Connection", "close");
			urlConn.setRequestProperty("User-Agent", "");
			urlConn.setRequestProperty("Accept-Encoding", "");
			//urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X; en-us) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53");
			//得到读取的内容(流)
			InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
			// 为输出创建BufferedReader
			BufferedReader buffer = new BufferedReader(in);
			String inputLine = null;
			//使用循环来读取获得的数据
			while (((inputLine = buffer.readLine()) != null))
			{
				//我们在每一行后面加上一个"\n"来换行
				resultData += inputLine + "\n";

			}	
			
			if ( !resultData.equals("") )
			{
				//mTextView.setText(resultData);
    	    	String pattern="\"";
    	    	Pattern pat = Pattern.compile(pattern);
    	    	final String[] rs = pat.split(resultData);
    	    	
				Log.d("debug", rs[1]);
				uuid = rs[1];
				
				showMessageToTextView("\n生成二维码成功！");
				MobclickAgent.onEvent(MainActivity.this, "getuuidsuccess");
				
				getQR();
				scanLogin();
			}
			else
			{
				//mTextView.setText("读取的内容为NULL");
			}
			//关闭InputStreamReader
			in.close();
			//关闭http连接
			urlConn.disconnect();
			//设置显示取得的内容

		}
		catch (IOException e)
		{
			//Log.e(DEBUG_TAG, "IOException");
		}
	}
	

	protected void onWebServiceSuccess(ResponseInfo<String> responseInfo, String tag) {
		if (tag.equals("tag_get_uuid")) {
			String data = responseInfo.result;
			Log.d(tag, data);
		}
	}
	
	protected void scanLoginLater() {
		new Handler().postDelayed(new Runnable(){    
		    public void run() { scanLogin(); } }, 500); 
	}
	
	protected void scanLogin() {
		String queryString = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login";
		queryString += "?";
		queryString += "tid=1";
		queryString += "&";
		queryString += "uuid=";
		queryString += uuid;
		
		//unused
		queryString += "&";
		queryString += "unused=";
		queryString += UUID.randomUUID().toString();
		
    	HttpUtils http = new HttpUtils();
    	//http.configTimeout(3000);
    	http.send(HttpRequest.HttpMethod.GET, queryString, null, new RequestCallBack<String>() {
            
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String data = responseInfo.result;
				Log.d("debug", data);
				
				//window.code=200;
				//window.redirect_uri="https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=A_Tk_tZY10TJ4VJyd_5kGTlX@qrticket_0&uuid=QenRNAMohA==&lang=zh_CN&scan=1453008076";
				
    	    	String pattern=";";
    	    	Pattern pat = Pattern.compile(pattern);
    	    	String[] rs = pat.split(data);
    	    	
    	    	String pattern2="=";
    	    	Pattern pat2 = Pattern.compile(pattern2);
    	    	String[] code2 = pat2.split(rs[0]);
    	    	String result2 = code2[1];
    	    	if(result2.equals("200")) {
    	    		showMessageToTextView("等待成功");
    	    		MobclickAgent.onEvent(MainActivity.this, "scanLoginSuccess");
    	    		//登陆成功
    	    		Log.d("debug", "login success");
    	    		
    	    		//window.redirect_uri="https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=AShzcdwzwi6dTfVE2xFk9xAt@qrticket_0&uuid=Afex387l_Q==&lang=zh_CN&scan=1453011562"
        	    	String pattern3="\"";
        	    	Pattern pat3 = Pattern.compile(pattern3);
        	    	String[] code3 = pat3.split(rs[1]);
    	    		String redirect = code3[1];
    	    		redirect += "&fun=new";
    	    		
    	    		int end = redirect.lastIndexOf('/');
    	    		baseUrl = redirect.substring(0, end);
    	    		getBaseInfo(redirect);
    	    	}
    	    	else {
    	    		showMessageToTextView("等待扫码");
    	    		//扫码结束
    	    		Log.d("debug", "not login yet");
    	    		scanLoginLater();	
    	    	}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				scanLoginLater();
			}
    	});
	}
	
	protected void getProfile() throws UnsupportedEncodingException {
		String u = baseUrl + "/webwxinit";
		u += "?" + "pass_ticket=" + pass_ticket;
		u += "&" + "skey=" + skey;
		
    	RequestParams params = new RequestParams();
    	String json = String.format("{\"BaseRequest\":{\"Uin\":%1$s,\"Sid\":\"%2$s\",\"Skey\":\"%3$s\",\"DeviceID\":\"%4$s\"}}", wxuin, wxsid,skey,DEVICE_ID);
    	params.setBodyEntity(new StringEntity(json));
    	params.setContentType("application/json");
    	
    	HttpUtils http = new HttpUtils();
    	http.configCookieStore(httpCookieStore);
    	http.send(HttpRequest.HttpMethod.POST, u, params, new RequestCallBack<String>() {
            
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String data = responseInfo.result;
				Log.d("debug", data);
				
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(data);
					JSONObject user = (JSONObject) jsonObject.opt("User");
					userMe = new CUser(user);
					
					showMessageToTextView("Profile success");
					MobclickAgent.onEvent(MainActivity.this, "ProfileSuccess");
					getContact();
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				Log.d("debug", msg);
			}
    	});
	}
	
	protected void getContact() {
		String u = baseUrl + "/webwxgetcontact";

    	RequestParams params = new RequestParams();
    	params.addBodyParameter("pass_ticket", pass_ticket);
    	params.addBodyParameter("skey", skey);
    	
    	HttpUtils http = new HttpUtils();
    	http.configCookieStore(httpCookieStore);
    	http.send(HttpRequest.HttpMethod.POST, u, params, new RequestCallBack<String>() {
            
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String data = responseInfo.result;
				Log.d("debug", data);
				
				try {
					JSONObject jsonObject = new JSONObject(data);
					JSONArray jsonArrays = jsonObject.optJSONArray("MemberList");
					userList = new ArrayList<CUser>();
					for (int i = 0; i < jsonArrays.length(); i++) {
						JSONObject obj = (JSONObject) jsonArrays.get(i);
						CUser user = new CUser(obj);
						if(user.isValid(userMe))
							userList.add(user);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				showMessageToTextView("Contact Info");
				MobclickAgent.onEvent(MainActivity.this, "ContactSuccess");
				checkWhoDeletedMe();
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				Log.d("debug", msg);
			}
    	});
	}
	
	private void checkWhoDeletedMe() {
		String tip = "开始检查:你一共有" + userList.size() + "个好友";
		showMessageToTextView(tip);
		createOrAdd(0);
	}
	
	private void createOrAdd(int startIndex) {
		try {
			if(startIndex==0)
				creteRoom();
			else
				add2Room(startIndex);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void creteRoom() throws UnsupportedEncodingException {
		String u = baseUrl + "/webwxcreatechatroom";
		u += "?" + "pass_ticket=" + pass_ticket;

		int memberCount = Math.min(MAX_GROUP_NUM, userList.size());
		nextUserIndex = memberCount;
		
    	String memberList = "[";
    	for(int i=0; i<memberCount; i++) {
    		if(i>0) memberList += ",";
    		memberList += "{\"UserName\":\"";
    		memberList += userList.get(i).UserName;
    		memberList += "\"}";
    	}
    	memberList += "]";
		
    	String json = String.format("{\"BaseRequest\":{\"Uin\":%1$s,\"Sid\":\"%2$s\",\"Skey\":\"%3$s\",\"DeviceID\":\"%4$s\"},\"MemberCount\":%5$d,\"Topic\":\"\",\"MemberList\":%6$s}", 
    			wxuin, wxsid,skey,DEVICE_ID, memberCount, memberList);
    	
    	RequestParams params = new RequestParams();
    	params.setBodyEntity(new StringEntity(json));
    	params.setContentType("application/json");
    	
    	HttpUtils http = new HttpUtils();
    	http.configCookieStore(httpCookieStore);
    	http.send(HttpRequest.HttpMethod.POST, u, params, new RequestCallBack<String>() {
            
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String data = responseInfo.result;
				Log.d("debug", data);

				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(data);
					JSONObject base = (JSONObject) jsonObject.opt("BaseResponse");
					String ret = base.getString("Ret");
					String ErrMsg = base.getString("ErrMsg");
					
					if(ret.equals("0")) {
						delList = new ArrayList<CUser>();
						roomname = jsonObject.getString("ChatRoomName");
						
						int badCount = 0;
						String badGay = "";
						String allGay = "";
						JSONArray MemberList = (JSONArray)jsonObject.opt("MemberList");
						for(int i=0; i<MemberList.length(); i++) {
							CUser user = new CUser((JSONObject)MemberList.get(i));
							int index = userList.indexOf(user);
							if(i!=0) allGay += ",";
							allGay += userList.get(index).UserName;
							
							String MemberStatus = ((JSONObject)MemberList.get(i)).getString("MemberStatus");
							if(MemberStatus.equals("4")) {
								badGay += (" " + userList.get(index).NickName);
								delList.add(userList.get(index));
							}
						}
						
						//showMessageToTextView("创建聊天室成功");
						MobclickAgent.onEvent(MainActivity.this, "CreateRommSuccess");
						showMessageToTextView(String.format("成功扫描%1$d到%2$d号好友!", 1, nextUserIndex));
						showMessageToTextView(String.format("扫描到%1$d个损友!%2$s", delList.size(), badGay));
						
						if(nextUserIndex<userList.size()) {
							//add2RoomLater(nextUserIndex);
							removeFromRoomLater(allGay);
						}
						else {
							removeFromRoomLater(null);
							printDelList();
						}
					}
					else {
						Log.d("Debug", ErrMsg);
						showMessageToTextView(ErrMsg);
					}
				} catch (JSONException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					showMessageToTextView(e.toString());
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				Log.d("debug", msg);
				showMessageToTextView(msg);
			}
    	});
	}
	
	private void printDelList() {
		MobclickAgent.onEvent(MainActivity.this, "printDelList");
		showMessageToTextView("------------扫描结束----------------");
		for(int i=0; i<delList.size(); i++) {
			CUser baduser = delList.get(i);
			showMessageToTextView(baduser.NickName);
		}
		showMessageToTextView("----------------------------");
	}
	
	private void add2RoomLater(final int startIndex) throws UnsupportedEncodingException {
		showMessageToTextView(String.format("等待%d秒后发起下一波操作...", SEARCH_INTERVAL*1000));
		
		new Handler().postDelayed(new Runnable(){    
		    public void run() { 
		    	try {
					add2Room(startIndex);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} } }, SEARCH_INTERVAL); 
	}
	
	private void add2Room(int startIndex) throws UnsupportedEncodingException {
		String u = baseUrl + "/webwxupdatechatroom";
		u += "?" + "pass_ticket=" + pass_ticket;
		u += "&" + "fun=" + "addmember";

		final int memberCount = Math.min(MAX_GROUP_NUM, userList.size());
    	String memberList = "\"";
    	for(int i=0; i<memberCount; i++) {
    		if(i>0) memberList += ",";
    		memberList += userList.get(i+nextUserIndex).UserName;
    	}
    	memberList += "\"";
    	
    	nextUserIndex += memberCount;
		
    	String json = String.format("{\"BaseRequest\":{\"Uin\":%1$s,\"Sid\":\"%2$s\",\"Skey\":\"%3$s\",\"DeviceID\":\"%4$s\"},\"ChatRoomName\":\"%5$s\",\"AddMemberList\":%6$s}", 
    			wxuin, wxsid,skey,DEVICE_ID, roomname, memberList);
    	
    	RequestParams params = new RequestParams();
    	params.setBodyEntity(new StringEntity(json));
    	params.setContentType("application/json");
    	
    	HttpUtils http = new HttpUtils();
    	http.configCookieStore(httpCookieStore);
    	http.send(HttpRequest.HttpMethod.POST, u, params, new RequestCallBack<String>() {
            
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String data = responseInfo.result;
				Log.d("debug", data);

				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(data);
					JSONObject base = (JSONObject) jsonObject.opt("BaseResponse");
					String ret = base.getString("Ret");
					String ErrMsg = base.getString("ErrMsg");
					
					if(ret.equals("0")) {
						int badCount = 0;
						String badGay = "";
						String allGay = "";
						JSONArray MemberList = (JSONArray)jsonObject.opt("MemberList");
						for(int i=0; i<MemberList.length(); i++) {
							CUser user = new CUser((JSONObject)MemberList.get(i));
							int index = userList.indexOf(user);
							if(i!=0) allGay += ",";
							allGay += userList.get(index).UserName;
							
							String MemberStatus = ((JSONObject)MemberList.get(i)).getString("MemberStatus");
							if(MemberStatus.equals("4")) {
								badCount++;
								badGay += (" " + userList.get(index).NickName);
								delList.add(userList.get(index));
							}
						}
						
						//showMessageToTextView("添加联系人进聊天室成功");
						MobclickAgent.onEvent(MainActivity.this, "AddRommSuccess");
						showMessageToTextView(String.format("成功扫描%1$d到%2$d号好友!", nextUserIndex-memberCount, nextUserIndex));
						showMessageToTextView(String.format("扫描到%1$d个损友!%2$s", badCount, badGay));
						
						if(nextUserIndex<userList.size()) {
							//add2RoomLater(nextUserIndex);
							removeFromRoomLater(allGay);
						}
						else {
							printDelList();
							removeFromRoomLater(null);
						}
					}
					else {
						Log.d("Debug", ErrMsg);
						showMessageToTextView(ErrMsg);
					}
				} catch (JSONException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch bloc
					MobclickAgent.onEvent(MainActivity.this, "AddRommFail");
					showMessageToTextView(e.toString());
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				Log.d("debug", msg);
			}
    	});
	}
	
	private void removeFromRoomLater(final String allGay) throws UnsupportedEncodingException {
		//showMessageToTextView("等待15秒后发起下一波...");
		
		new Handler().postDelayed(new Runnable(){    
		    public void run() { 
		    	try {
		    		removeFromRoom(allGay);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} } }, SEARCH_INTERVAL); 
	}
	
	private void removeFromRoom(String allGay) throws UnsupportedEncodingException {
		showMessageToTextView("开始删除");
		
		String u = baseUrl + "/webwxupdatechatroom";
		u += "?" + "pass_ticket=" + pass_ticket;
		u += "&" + "fun=" + "delmember";

    	String memberList = "\"";
    	if(allGay!=null)
    		memberList += allGay;
    	else {
        	for(int i=0; i<userList.size(); i++) {
        		if(i>0) memberList += ",";
        		memberList += userList.get(i).UserName;
        	}
    	}
    	memberList += "\"";

    	String json = String.format("{\"BaseRequest\":{\"Uin\":%1$s,\"Sid\":\"%2$s\",\"Skey\":\"%3$s\",\"DeviceID\":\"%4$s\"},\"ChatRoomName\":\"%5$s\",\"DelMemberList\":%6$s}", 
    			wxuin, wxsid,skey,DEVICE_ID, roomname, memberList);
    	
    	RequestParams params = new RequestParams();
    	params.setBodyEntity(new StringEntity(json));
    	params.setContentType("application/json");
    	
    	HttpUtils http = new HttpUtils();
    	http.configCookieStore(httpCookieStore);
    	http.send(HttpRequest.HttpMethod.POST, u, params, new RequestCallBack<String>() {
            
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String data = responseInfo.result;
				Log.d("debug", data);

				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(data);
					JSONObject base = (JSONObject) jsonObject.opt("BaseResponse");
					String ret = base.getString("Ret");
					String ErrMsg = base.getString("ErrMsg");
					
					if(ret.equals("0")) {
//						JSONArray MemberList = (JSONArray)jsonObject.opt("MemberList");
//						for(int i=0; i<MemberList.length(); i++) {
//							String MemberStatus = ((JSONObject)MemberList.get(i)).getString("MemberStatus");
//							if(MemberStatus.equals("4")) {
//								CUser user = new CUser((JSONObject)MemberList.get(i));
//								int index = userList.indexOf(user);
//								delList.add(userList.get(index));
//							}
//						}
//						
						MobclickAgent.onEvent(MainActivity.this, "RemoveRommSuccess");
						showMessageToTextView("清理聊天室成功");
						
						if(nextUserIndex<userList.size())
							add2RoomLater(nextUserIndex);
						else
							printDelList();
					}
					else {
						showMessageToTextView("删除失败2");
						showMessageToTextView(ErrMsg);
						Log.d("Debug", ErrMsg);
					}
				} catch (JSONException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					showMessageToTextView("删除失败3");
					showMessageToTextView(e.toString());
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				Log.d("debug", msg);
				showMessageToTextView("删除失败");
				showMessageToTextView(msg);
			}
    	});
	}

	protected void getBaseInfo(String redirect) {
    	HttpUtils http = new HttpUtils();
    	httpCookieStore = new BasicCookieStore();
    	http.configCookieStore(httpCookieStore);
    	http.send(HttpRequest.HttpMethod.GET, redirect, null, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
//				<error>
//				<ret>0</ret>
//				<message>OK</message>
//				<skey>@crypt_2004667e_3d8f6c81559bf74b194c67298a06a720</skey>
//				<wxsid>S+X4kRkFiAgneMfT</wxsid>
//				<wxuin>1797983605</wxuin>
//				<pass_ticket>JQiRQIb5aMHZyzNY3eDKeccugafxc%2B3IHK4VqkLlPXxuhAYGfzgXNsYolZxKh6Ms</pass_ticket>
//				<isgrayscale>1</isgrayscale>
//				</error>
				
//	    		String cookie = httpCookieStore.toString();
//	    		Log.d("cookie", cookie);
				
				String data = responseInfo.result;
				Log.d("debug", data);
				
				try {
					XmlPullParser pullParser = Xml.newPullParser();// 也可以用该方法创建解析器
					InputStream in_nocode = new ByteArrayInputStream(data.getBytes());
					pullParser.setInput(in_nocode, "UTF-8");
					in_nocode.close();
					
			        //产生事件
			        int event = pullParser.getEventType();
			        //对事件进行判断
			        while (event != XmlPullParser.END_DOCUMENT) {
			            switch (event) {
			            case XmlPullParser.START_TAG:
			                if("ret".equals(pullParser.getName())) ret = pullParser.nextText();
			                if("message".equals(pullParser.getName())) message = pullParser.nextText();
			                if("skey".equals(pullParser.getName())) skey = pullParser.nextText();
			                if("wxuin".equals(pullParser.getName())) wxuin = pullParser.nextText();
			                if("wxsid".equals(pullParser.getName())) wxsid = pullParser.nextText();
			                if("pass_ticket".equals(pullParser.getName())) pass_ticket = pullParser.nextText();
			                if("isgrayscale".equals(pullParser.getName())) isgrayscale = pullParser.nextText();
			                break;
			            }
			            
			            //_baseRequest = { Uin: parseInt(wxuin), Sid: wxsid, Skey: skey, DeviceID: DEVICE_ID }
			    		baseRequest = new HashMap<String,String>();
			    		baseRequest.put("Uin", wxuin);
			    		baseRequest.put("Sid", wxsid);
			    		baseRequest.put("Skey", skey);
			    		baseRequest.put("DeviceID", DEVICE_ID);
			            
			            event = pullParser.next();//进入到后面的节点,触发case XmlPullParser.START_TAG事件，利用循环解析
			        }
			        
			        showMessageToTextView("BaseInfo success");
			        MobclickAgent.onEvent(MainActivity.this, "BaseInfoSuccess");
			        
			        getProfile();
			        //getContact();

				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // 设置Pull解析器要解析的数据
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				// TODO Auto-generated method stub
			}
    	});
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}
}
