package com.sdk.game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


import com.gotye.api.GotyeAPI;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.widget.Toast;
import c.b.BP;
import c.b.PListener;
import c.b.QListener;

import cn.bmob.sms.BmobSMS;
import cn.bmob.sms.bean.BmobSmsState;
import cn.bmob.sms.exception.BmobException;
import cn.bmob.sms.listener.QuerySMSStateListener;
import cn.bmob.sms.listener.RequestSMSCodeListener;
import cn.bmob.sms.listener.VerifySMSCodeListener;


public class MainActivity extends UnityPlayerActivity  {

	// 此为测试Appid,请将Appid改成你自己的Bmob AppId
	String APPID = "729ebe6bbb6481cb2c98ad44bc009fc4";
	String APPID1= "6afa1c017b8248bdee8e19ebedf07a9d";
	// 此为支付插件的官方最新版本号,请在更新时留意更新说明
	int PLUGINVERSION = 7;

	public static MainActivity currentActivity;
	private static final String TAG = "test";
	Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Toast.makeText(
						MainActivity.this,
						"初始化开始",Toast.LENGTH_LONG).show();
			}
		});
		//setContentView(R.layout.activity_main);
		mContext = this;
		currentActivity=this;
		// 必须先初始化
		BP.init(this, APPID);
		BmobSMS.initialize(this,APPID);
		GotyeAPI.getInstance().init(getApplicationContext(), "0416ca45-7a00-4a68-8b13-75adbbeb8af9", GotyeAPI.SCENE_UNITY3D);
		final int pluginVersion = BP.getPluginVersion();
		if (pluginVersion < PLUGINVERSION) {// 为0说明未安装支付插件, 否则就是支付插件的版本低于官方最新版
			
			runOnUiThread(new Runnable(){
    			@Override
    			public void run() {
    				Toast.makeText(
    						MainActivity.this,
    						pluginVersion == 0 ? "监测到本机尚未安装支付插件,无法进行支付,请先安装插件(无流量消耗)"
    								: "监测到本机的支付插件不是最新版,最好进行更新,请先更新插件(无流量消耗)", 0).show();
    				installBmobPayPlugin("bp.db");
    			}
    		});
			
		}
	}

	public static MainActivity getInstance()  
    {  
        return currentActivity;  
    } 
	public void RestartApplication(){
		new Thread(){
			public void run(){
			Intent launch=getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
			launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(launch);  
			android.os.Process.killProcess(android.os.Process.myPid());
			}
			}.start();
			finish();
	}
	/**
	 * 调用支付
	 * 
	 * @param alipayOrWechatPay
	 *            支付类型，true为支付宝支付,false为微信支付
	 */
	 public void onStartIap(final String productId,final int money,final String productName, final String Dec,final String Data) {
			runOnUiThread(new Runnable(){
    			@Override
    			public void run() {
    				Toast.makeText(MainActivity.this, "查询订单……",
    						Toast.LENGTH_LONG).show();
    				
    				
    				BP.pay(productName, Dec,money*1.0f/100, true, new PListener() {

    					// 因为网络等原因,支付结果未知(小概率事件),出于保险起见稍后手动查询
    					@Override
    					public void unknow() {
    						Toast.makeText(MainActivity.this, "支付结果未知,请稍后手动查询", Toast.LENGTH_SHORT)
    								.show();
    						//tv.append(name + "'s pay status is unknow\n\n");
    						//hideDialog();
    						String json="Pay;-1;"+productId;
    	    	    		UnityPlayer.UnitySendMessage("Main Camera","OnCKGameSdkCallback", json);
    					}

    					// 支付成功,如果金额较大请手动查询确认
    					@Override
    					public void succeed() {
    						Toast.makeText(MainActivity.this, "支付成功!", Toast.LENGTH_SHORT).show();
    						//tv.append(name + "'s pay status is success\n\n");
    						//hideDialog();
    						String json="Pay;0;"+productId;
    	    	    		UnityPlayer.UnitySendMessage("Main Camera","OnCKGameSdkCallback", json);
    					}

    					// 无论成功与否,返回订单号
    					@Override
    					public void orderId(String orderId) {
    						// 此处应该保存订单号,比如保存进数据库等,以便以后查询
    						//order.setText(orderId);
    						//tv.append(name + "'s orderid is " + orderId + "\n\n");
    						//showDialog("获取订单成功!请等待跳转到支付页面~");
    						String json="Pay;-2;"+orderId;
    	    	    		UnityPlayer.UnitySendMessage("Main Camera","OnCKGameSdkCallback", json);
    					}

    					// 支付失败,原因可能是用户中断支付操作,也可能是网络原因
    					@Override
    					public void fail(int code, String reason) {

    						// 当code为-2,意味着用户中断了操作
    						// code为-3意味着没有安装BmobPlugin插件
    						if (code == -3) {
    							Toast.makeText(
    									MainActivity.this,
    									"监测到你尚未安装支付插件,无法进行支付,请先安装插件(已打包在本地,无流量消耗),安装结束后重新支付",
    									0).show();
    							installBmobPayPlugin("bp.db");
    						} else {
    							Toast.makeText(MainActivity.this, "支付中断!", Toast.LENGTH_SHORT)
    									.show();
    						}
    						String json="Pay;1;"+productId;
    	    	    		UnityPlayer.UnitySendMessage("Main Camera","OnCKGameSdkCallback", json);
    						//tv.append(name + "'s pay status is fail, error code is \n"
    						//		+ code + " ,reason is " + reason + "\n\n");
    						//hideDialog();
    					}
    				});
    				
    			}
    			
    			
    			
    		});
	 }
	
	
	  private String phoneNunber="";
	  /**********************************************************
	     * 短信验证码
	     * 1，获取验证码
	     * 2，验证
	     * 3，查询验证状态
	     ********************************************************/
	    //第一步：获取验证码
	    public void requestSendMsg(String phoneNum) {
	        phoneNunber = phoneNum;
	        onCoderReturn("获取 " + phoneNum+ " 的验证码,,,");
	        BmobSMS.requestSMSCode(MainActivity.this, phoneNum, "短信验证", new RequestSMSCodeListener() {
	            @Override
	            public void done(Integer smsId, BmobException ex) {
	                // TODO Auto-generated method stub
	                if (ex == null) {//验证码发送成功
	                    onCoderReturn("获取验证码OK：" + smsId);
	                }
	                else
	                {
	                	onCoderReturn(ex.toString());
	                }
	            }
	        });
	    }
	    //第二步：验证短信码
	    public void verifiyCoder(String coder)
	    {
	        onCoderReturn("开始验证:" + coder);
	        if(phoneNunber == "" || coder =="")
	        {
	            onCoderReturn("验证失败");
	            return;
	        }
	        BmobSMS.verifySmsCode(MainActivity.this, phoneNunber, coder, new VerifySMSCodeListener() {
	            @Override
	            public void done(BmobException ex) {
	                // TODO Auto-generated method stub
	                if (ex == null) {//短信验证码已验证成功
	                    onCoderReturn("SmsSuccess");
	                } else {
	                    //Log.i("bmob", "验证失败：code =" + ex.getErrorCode() + ",msg = " + ex.getLocalizedMessage());
	                    onCoderReturn("SmsFail");
	                }
	            }
	        });
	    }

	    //第三步：查询验证状态
	    public void queryState(String smsid)
	    {
	        BmobSMS.querySmsState(MainActivity.this, Integer.parseInt(smsid), new QuerySMSStateListener() {
	            @Override
	            public void done(BmobSmsState bmobSmsState, BmobException e) {
	                if (e == null) {
	                    //Log.i("bmob", "短信状态：" + bmobSmsState.getSmsState() + ",验证状态：" + bmobSmsState.getVerifyState());
	                    onCoderReturn(bmobSmsState.getSmsState() + " " + bmobSmsState.getVerifyState());
	                }
	            }
	        });
	    }

	    //状态返回Unity
	    public void onCoderReturn(String state )
	    {
	        String gameObjectName = "Main Camera";
	        String methodName = "OnCoderReturn";
	        String arg0 = state;
	        UnityPlayer.UnitySendMessage(gameObjectName, methodName, arg0);
	    }

	// 以下仅为控件操作，可以略过


	// 默认为0.02
	

	

	
	

	void installBmobPayPlugin(String fileName) {
		try {
			InputStream is = getAssets().open(fileName);
			File file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + fileName + ".apk");
			if (file.exists())
				file.delete();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] temp = new byte[1024];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			fos.close();
			is.close();

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.parse("file://" + file),
					"application/vnd.android.package-archive");
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
