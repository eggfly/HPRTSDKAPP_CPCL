package com.hprtsdksample;

import android.content.Context;

public class PublicAction
{	
	private Context context=null;	
	public PublicAction()
	{
		
	}
	public PublicAction(Context con)
	{
		context = con;
	}
//	public String LanguageEncode()
//	{
//		try
//		{
//			PublicFunction PFun=new PublicFunction(context);
//			String sLanguage=PFun.ReadSharedPreferencesData("Codepage").split(",")[1].toString();
//			String sLEncode="gb2312";
//			int intLanguageNum=0;
//			
//			sLEncode=PFun.getLanguageEncode(sLanguage);		
//			intLanguageNum= PFun.getCodePageIndex(sLanguage);	
//			
//			HPRTPrinterHelper.SetCharacterSet((byte)intLanguageNum);
//			HPRTPrinterHelper.LanguageEncode=sLEncode;
//		
//			return sLEncode;
//		}
//		catch(Exception e)
//		{			
//			Log.e("HPRTSDKSample", (new StringBuilder("PublicAction --> AfterPrintAction ")).append(e.getMessage()).toString());
//			return "";
//		}
//	}
}