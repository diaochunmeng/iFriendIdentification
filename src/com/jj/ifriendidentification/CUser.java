package com.jj.ifriendidentification;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

public class CUser {
	public String UserName;
	private String Uin;
	public String NickName;
	private String VerifyFlag;
	private String HeadImgUrl;
	//private int ContactFlag;
	private String[] SPEC_USERS = {"newsapp", "fmessage", "filehelper", "weibo", "qqmail", "tmessage", "qmessage", "qqsync", "floatbottle", "lbsapp", "shakeapp", "medianote", "qqfriend", "readerapp", "blogapp",
            "facebookapp", "masssendapp", "meishiapp", "feedsapp", "voip", "blogappweixin", "weixin", "brandsessionholder", "weixinreminder", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c",
            "officialaccounts", "notification_messages", "wxitil", "userexperience_alarm" };
	
	public CUser() { super(); }

	public CUser(JSONObject json) throws JSONException {
		Uin = json.getString("Uin");
		UserName = json.getString("UserName");
		NickName = json.getString("NickName");
		VerifyFlag = json.optString("VerifyFlag");
		HeadImgUrl = json.optString("HeadImgUrl");
		//ContactFlag = json.getInt("ContactFlag");
	}
	
	public boolean isValid(CUser me) {
//        return v["VerifyFlag"] == 0 // 个人账号
//                && !_.includes(SPEC_USERS, v["UserName"]) // 特殊账号
//                && !/@@/.test(v["UserName"]) // 群聊
//                && v["UserName"] !== profile["UserName"] // 自己

		boolean res = VerifyFlag.equals("0") && !Arrays.asList(SPEC_USERS).contains(UserName) && !UserName.equals(me.UserName) /*&& (ContactFlag==3)*/;
		return res;
	}
	
	@Override
	public boolean equals(Object obj) {
		CUser user = (CUser)obj;
		return user.UserName.equals(this.UserName);
	}
}


