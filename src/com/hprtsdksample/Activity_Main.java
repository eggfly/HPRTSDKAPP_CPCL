package com.hprtsdksample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.relinker.ReLinker;
import com.hprtsdksample.cpcl.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import HPRTAndroidSDK.HPRTPrinterHelper;
import HPRTAndroidSDK.IPort;
import HPRTAndroidSDK.PublicFunction;

public class Activity_Main extends Activity 
{
	private Context thisCon=null;
	private BluetoothAdapter mBluetoothAdapter;
	private PublicFunction PFun=null;
	private PublicAction PAct=null;
	
	private Button btnWIFI=null;
	private Button btnBT=null;
	private Button btnUSB=null;
	
	private Spinner spnPrinterList=null;
	private TextView txtTips=null;
	private Button btnOpenCashDrawer=null;
	private Button btnSampleReceipt=null;	
	private Button btn1DBarcodes=null;
	private Button btnQRCode=null;
	private Button btnPDF417=null;
	private Button btnCut=null;
//	private Button btnPageMode=null;
	private Button btnImageManage=null;
	private Button btnGetRemainingPower=null;
	
	private EditText edtTimes=null;
	
	private ArrayAdapter arrPrinterList; 
	private static HPRTPrinterHelper HPRTPrinter=new HPRTPrinterHelper();
	private String ConnectType="";
	private String PrinterName="";
	private String PortParam="";
	
	private UsbManager mUsbManager=null;	
	private UsbDevice device=null;
	private static final String ACTION_USB_PERMISSION = "com.HPRTSDKSample";
	private PendingIntent mPermissionIntent=null;
	private static IPort Printer=null;
	private Handler handler;
	private ProgressDialog dialog;
	public static String paper="0"; 
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		try
		{
			thisCon=this.getApplicationContext();
			
			btnWIFI = (Button) findViewById(R.id.btnWIFI);
			btnUSB = (Button) findViewById(R.id.btnUSB);
			btnBT = (Button) findViewById(R.id.btnBT);
			
			//edtTimes = (EditText) findViewById(R.id.edtTimes);
			
			spnPrinterList = (Spinner) findViewById(R.id.spn_printer_list);	
			txtTips = (TextView) findViewById(R.id.txtTips);
			btnSampleReceipt = (Button) findViewById(R.id.btnSampleReceipt);
			btnOpenCashDrawer = (Button) findViewById(R.id.btnOpenCashDrawer);
			btn1DBarcodes = (Button) findViewById(R.id.btn1DBarcodes);
			btnQRCode = (Button) findViewById(R.id.btnQRCode);
			btnPDF417 = (Button) findViewById(R.id.btnPDF417);
			btnCut = (Button) findViewById(R.id.btnCut);
			btnImageManage = (Button) findViewById(R.id.btnImageManage);
			btnGetRemainingPower = (Button) findViewById(R.id.btnGetRemainingPower);
			btnGetStatus = (Button) findViewById(R.id.btnGetStatus);
					
			mPermissionIntent = PendingIntent.getBroadcast(thisCon, 0, new Intent(ACTION_USB_PERMISSION), 0);
	        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			thisCon.registerReceiver(mUsbReceiver, filter);
			
			PFun=new PublicFunction(thisCon);
			PAct=new PublicAction(thisCon);
			InitSetting();
			InitCombox();
			this.spnPrinterList.setOnItemSelectedListener(new OnItemSelectedPrinter());
			//Enable Bluetooth
			EnableBluetooth();
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					super.handleMessage(msg);
					if (msg.what==1) {
						Toast.makeText(thisCon, "succeed", Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}else {
						Toast.makeText(thisCon, "failure", Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
				}
			};
		}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onCreate ")).append(e.getMessage()).toString());
		}
	}
	
	private void InitSetting()
	{
		String paper = PFun.ReadSharedPreferencesData("papertype");
		if (!"".equals(paper)) {
			Activity_Main.paper=paper;
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String paper = PFun.ReadSharedPreferencesData("papertype");
		if (!"".equals(paper)) {
			Activity_Main.paper=paper;
		}
		String[] arrpaper = getResources().getStringArray(R.array.activity_main_papertype);
		if ("1".equals(Activity_Main.paper)) {
			btnOpenCashDrawer.setText(getResources().getString(R.string.activity_esc_function_btnopencashdrawer)+":"+arrpaper[1]);
		}else {
			btnOpenCashDrawer.setText(getResources().getString(R.string.activity_esc_function_btnopencashdrawer)+":"+arrpaper[0]);
		}
	}
	//add printer list
	private void InitCombox()
	{
		try
		{
			arrPrinterList = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
			String strSDKType=thisCon.getString(com.hprtsdksample.cpcl.R.string.sdk_type);
			if(strSDKType.equals("all"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_cpcl, R.layout.item_printlist);
			if(strSDKType.equals("hprt"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_hprt, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mkt"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mkt, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mprint"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mprint, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("sycrown"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_sycrown, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mgpos"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mgpos, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("ds"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_ds, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("cst"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_cst, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("other"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_other, android.R.layout.simple_spinner_item);
			arrPrinterList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			PrinterName=arrPrinterList.getItem(0).toString();
			spnPrinterList.setAdapter(arrPrinterList);
		}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> InitCombox ")).append(e.getMessage()).toString());
		}
	}
	
	private class OnItemSelectedPrinter implements OnItemSelectedListener
	{				
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{

			PrinterName=arrPrinterList.getItem(arg2).toString();
			HPRTPrinter=new HPRTPrinterHelper(thisCon,PrinterName);
			CapturePrinterFunction();
	//		GetPrinterProperty();
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// TODO Auto-generated method stub			
		}
	}
	
	//EnableBluetooth
	private boolean EnableBluetooth()
    {
        boolean bRet = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null)
        {
            if(mBluetoothAdapter.isEnabled())
                return true;
            mBluetoothAdapter.enable();
            try 
    		{
    			Thread.sleep(500);
    		} 
    		catch (InterruptedException e) 
    		{			
    			e.printStackTrace();
    		}
            if(!mBluetoothAdapter.isEnabled())
            {
                bRet = true;
                Log.d("PRTLIB", "BTO_EnableBluetooth --> Open OK");
            }
        } 
        else
        {
        	Log.d("HPRTSDKSample", (new StringBuilder("Activity_Main --> EnableBluetooth ").append("Bluetooth Adapter is null.")).toString());
        }
        return bRet;
    }
	
	//call back by scan bluetooth printer
	@Override  
  	protected void onActivityResult(int requestCode, int resultCode, final Intent data)  
  	{  
  		try
  		{  		
  			String strIsConnected;
	  		switch(resultCode)
	  		{
	  			case HPRTPrinterHelper.ACTIVITY_CONNECT_BT:		
	  				String strBTAddress="";
	  				strIsConnected=data.getExtras().getString("is_connected");
	  	        	if (strIsConnected.equals("NO"))
	  	        	{
	  	        		txtTips.setText(thisCon.getString(R.string.activity_main_scan_error));	  	        		
  	                	return;
	  	        	}
	  	        	else
	  	        	{	  	        		
	  						txtTips.setText(thisCon.getString(R.string.activity_main_connected));
	  					return;
	  	        	}		  	        	
	  			case HPRTPrinterHelper.ACTIVITY_CONNECT_WIFI:		
	  				String strIPAddress="";
	  				String strPort="";
	  				strIsConnected=data.getExtras().getString("is_connected");
	  	        	if (strIsConnected.equals("NO"))
	  	        	{
	  	        		txtTips.setText(thisCon.getString(R.string.activity_main_scan_error));	  	        		
  	                	return;
	  	        	}
	  	        	else
	  	        	{	  	        		
	  	        		strIPAddress=data.getExtras().getString("IPAddress");
	  	        		strPort=data.getExtras().getString("Port");
	  	        		if(strIPAddress==null || !strIPAddress.contains("."))	  					
	  						return;	  						  					
	  	        		HPRTPrinter=new HPRTPrinterHelper(thisCon,spnPrinterList.getSelectedItem().toString().trim());
	  					if(HPRTPrinterHelper.PortOpen("WiFi,"+strIPAddress+","+strPort)!=0)	  						  						
	  						txtTips.setText(thisCon.getString(R.string.activity_main_connecterr));	  	                	
	  					else
	  						txtTips.setText(thisCon.getString(R.string.activity_main_connected));
	  					return;
	  	        	}		  	        	
	  			case HPRTPrinterHelper.ACTIVITY_IMAGE_FILE:	  				
//	  		    	PAct.LanguageEncode();
	  				dialog = new ProgressDialog(Activity_Main.this);
					dialog.setMessage("Printing.....");
					dialog.setProgress(100);
					dialog.show();
		  				new Thread(){
		  					public void run() {
		  						try {
	  				String strImageFile=data.getExtras().getString("FilePath");
	  				Bitmap bmp=BitmapFactory.decodeFile(strImageFile);
	  				int height = bmp.getHeight();
	  				System.err.println("height:"+height);
	  		    	HPRTPrinterHelper.printAreaSize("0", "200", "200", "" + height, "1");
	  		    	int a=HPRTPrinterHelper.Expanded("0","0",strImageFile);
	  		    	if ("1".equals(Activity_Main.paper)) {
	  					HPRTPrinterHelper.Form();
	  				}
	  		    	HPRTPrinterHelper.Print();
	  		    	if (a>0) {
						handler.sendEmptyMessage(1);
					}else {
						handler.sendEmptyMessage(0);
					}
						}catch (Exception e) {
						handler.sendEmptyMessage(0);
					}
				}
			}.start();
	  				return;
	  			case HPRTPrinterHelper.ACTIVITY_PRNFILE:	  				
	  				String strPRNFile=data.getExtras().getString("FilePath");
	  				HPRTPrinterHelper.PrintBinaryFile(strPRNFile);  					  				
	  				
	  				/*String strPRNFile=data.getExtras().getString("FilePath");	  					  				
	  				byte[] bR=new byte[1];
	  				byte[] bW=new byte[3];
	  				bW[0]=0x10;bW[1]=0x04;bW[2]=0x02;
	  				for(int i=0;i<Integer.parseInt(edtTimes.getText().toString());i++)
	  				{
	  					HPRTPrinterHelper.PrintBinaryFile(strPRNFile);
	  					HPRTPrinterHelper.DirectIO(bW, null, 0);
	  					HPRTPrinterHelper.DirectIO(null, bR, 1);	  						
	  				}*/
	  				return;
  			}
  		}
  		catch(Exception e)
  		{
  			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onActivityResult ")).append(e.getMessage()).toString());
  		}
        super.onActivityResult(requestCode, resultCode, data);  
  	} 
	
	@SuppressLint("NewApi")
	public void onClickConnect(View view) 
	{		
    	if (!checkClick.isClickEvent()) return;
    	
    	try
    	{
	    	if(HPRTPrinter!=null)
			{					
	    		HPRTPrinterHelper.PortClose();
			}
			
	    	if(view.getId()==R.id.btnBT)
	    	{
                if (Build.VERSION.SDK_INT >= 23) {
                    //校验是否已具有模糊定位权限
                    if (ContextCompat.checkSelfPermission(Activity_Main.this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Activity_Main.this,
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                100);
                    } else {
                        //具有权限
                        ConnectType="Bluetooth";
                        Intent serverIntent = new Intent(thisCon,Activity_DeviceList.class);
                        startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);
                        return;
                    }
                } else {
                    //系统不高于6.0直接执行
                    ConnectType="Bluetooth";
                    Intent serverIntent = new Intent(thisCon,Activity_DeviceList.class);
                    startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);
                }
	    	}
	    	else if(view.getId()==R.id.btnWIFI)
	    	{	    		
	    		ConnectType="WiFi";
	    		Intent serverIntent = new Intent(thisCon,Activity_Wifi.class);
				serverIntent.putExtra("PN", PrinterName); 
				startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_WIFI);				
				return;	
	    	}
	    	else if(view.getId()==R.id.btnUSB)
	    	{
	    		ConnectType="USB";							
				HPRTPrinter=new HPRTPrinterHelper(thisCon,arrPrinterList.getItem(spnPrinterList.getSelectedItemPosition()).toString());					
				//USB not need call "iniPort"				
				mUsbManager = (UsbManager) thisCon.getSystemService(Context.USB_SERVICE);
				Toast.makeText(thisCon, ":"+mUsbManager.toString(), Toast.LENGTH_SHORT).show();
		  		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();  
		  		Toast.makeText(thisCon, "deviceList:"+deviceList.size(), Toast.LENGTH_SHORT).show();
		  		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		  		
		  		boolean HavePrinter=false;		  
		  		while(deviceIterator.hasNext())
		  		{
		  		    device = deviceIterator.next();
		  		    int count = device.getInterfaceCount();
		  		  Toast.makeText(thisCon, "count:"+count, Toast.LENGTH_SHORT).show();
		  		    for (int i = 0; i < count; i++) 
		  	        {
		  		    	UsbInterface intf = device.getInterface(i); 
		  		    	Toast.makeText(thisCon, ""+intf.getInterfaceClass(), Toast.LENGTH_SHORT).show();
		  	            if (intf.getInterfaceClass() == 7)
		  	            {
		  	            	HavePrinter=true;
		  	            	mUsbManager.requestPermission(device, mPermissionIntent);		  	            	
		  	            }
		  	        }
		  		}
		  		if(!HavePrinter)
		  			txtTips.setText(thisCon.getString(R.string.activity_main_connect_usb_printer));	
	    	}
    	}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickConnect "+ConnectType)).append(e.getMessage()).toString());
		}
	}
		   			
	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
	    public void onReceive(Context context, Intent intent) 
	    {
	    	try
	    	{
		        String action = intent.getAction();	       
		        //Toast.makeText(thisCon, "now:"+System.currentTimeMillis(), Toast.LENGTH_LONG).show();
		        //HPRTPrinterHelper.WriteLog("1.txt", "fds");
		        if (ACTION_USB_PERMISSION.equals(action))
		        {
			        synchronized (this) 
			        {		        	
			            device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
				        {			 
				        	if(HPRTPrinterHelper.PortOpen(device)!=0)
							{					
				        		HPRTPrinter=null;
								txtTips.setText(thisCon.getString(R.string.activity_main_connecterr));												
			                	return;
							}
				        	else
				        		txtTips.setText(thisCon.getString(R.string.activity_main_connected));
				        		
				        }		
				        else
				        {			        	
				        	return;
				        }
			        }
			    }
		        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
		        {
		            device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		            if (device != null) 
		            {	                	            	
						HPRTPrinterHelper.PortClose();					
		            }
		        }	    
	    	} 
	    	catch (Exception e) 
	    	{
	    		Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> mUsbReceiver ")).append(e.getMessage()).toString());
	    	}
		}
	};
	private Button btnGetStatus;
	
	public void onClickClose(View view) 
	{
    	if (!checkClick.isClickEvent()) return;
    	
    	try
    	{
	    	if(HPRTPrinter!=null)
			{					
	    		HPRTPrinterHelper.PortClose();
			}
			this.txtTips.setText(R.string.activity_main_tips);
			return;	
    	}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickClose ")).append(e.getMessage()).toString());
		}
    }
	
	public void onClickbtnSetting(View view) 
	{
    	if (!checkClick.isClickEvent()) return;
    	
    	try
    	{
    		Intent myIntent = new Intent(this, Activity_Setting.class);
    		startActivityForResult(myIntent, HPRTPrinterHelper.ACTIVITY_IMAGE_FILE);
        	startActivityFromChild(this, myIntent, 0);	
    	}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickClose ")).append(e.getMessage()).toString());
		}
    }
	
	public void onClickDo(View view) 
	{
		if (!checkClick.isClickEvent()) return;
		
		if(!HPRTPrinterHelper.IsOpened())
		{
			Toast.makeText(thisCon, thisCon.getText(R.string.activity_main_tips), Toast.LENGTH_SHORT).show();				
			return;
		}
		String paper = PFun.ReadSharedPreferencesData("papertype");
		if (!"".equals(paper)) {
			Activity_Main.paper=paper;
		}
		if (view.getId()==R.id.btnOpenCashDrawer) {
			paperAlertDialog();
		}   	    	
    	if(view.getId()==R.id.btnGetStatus)
    	{
    		Intent myIntent = new Intent(this, Activity_Status.class);
        	startActivityFromChild(this, myIntent, 0);
    	}
    	else if(view.getId()==R.id.btnSampleReceipt)
    	{
    		PrintSampleReceipt();
    	}
    	else if(view.getId()==R.id.btn1DBarcodes)
    	{
    		Intent myIntent = new Intent(this, Activity_1DBarcodes.class);    		
        	startActivityFromChild(this, myIntent, 0);
    	}
    	else if(view.getId()==R.id.btnTextFormat)
    	{
    		Intent myIntent = new Intent(this, Activity_TextFormat.class);
        	startActivityFromChild(this, myIntent, 0);
    	}
    	else if(view.getId()==R.id.btnPrintImageFile)
    	{
    		Intent myIntent = new Intent(this, Activity_PRNFile.class); 
        	myIntent.putExtra("Folder", android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
        	myIntent.putExtra("FileFilter", "jpg,gif,png,");
        	startActivityForResult(myIntent, HPRTPrinterHelper.ACTIVITY_IMAGE_FILE);
    	}
    	else if(view.getId()==R.id.btnPrintPRNFile)
    	{
    		Intent myIntent = new Intent(this, Activity_PRNFile.class);    	
        	myIntent.putExtra("Folder", android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
        	myIntent.putExtra("FileFilter", "prn,");
        	startActivityForResult(myIntent, HPRTPrinterHelper.ACTIVITY_PRNFILE);
    	}
    	else if(view.getId()==R.id.btnQRCode)
    	{
    		Intent myIntent = new Intent(this, Activity_QRCode.class);
        	startActivityFromChild(this, myIntent, 0);
    	}    	
    	else if(view.getId()==R.id.btnPrintTestPage)
    	{
    		try {
    			HPRTPrinterHelper.printAreaSize("0", "200", "200", "1400", "1");
    			HPRTPrinterHelper.Align(HPRTPrinterHelper.CENTER);
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "5", getResources().getString(R.string.activity_test_page));
    			HPRTPrinterHelper.Align(HPRTPrinterHelper.LEFT);
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "50", "code128");
    			HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, "128", "2", "1", "50", "0", "80", true, "7", "0", "5", "123456789");
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "180", "UPCA");
    			HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, HPRTPrinterHelper.UPCA, "2", "1", "50", "0", "210", true, "7", "0", "5", "123456789012");
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "310", "UPCE");
    			HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, HPRTPrinterHelper.code128, "2", "1", "50", "0", "340", true, "7", "0", "5", "0234565687");
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "440", "EAN8");
    			HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, HPRTPrinterHelper.EAN8, "2", "1", "50", "0", "470", true, "7", "0", "5", "12345678");
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "570", "CODE93");
    			HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, HPRTPrinterHelper.code93, "2", "1", "50", "0", "600", true, "7", "0", "5", "123456789");
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "4", "0", "0", "700", "CODE39");
    			HPRTPrinterHelper.Barcode(HPRTPrinterHelper.BARCODE, HPRTPrinterHelper.code39, "2", "1", "50", "0", "730", true, "7", "0", "5", "123456789");
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "8", "0", "0", "830", getResources().getString(R.string.activity_esc_function_btnqrcode));
    			HPRTPrinterHelper.PrintQR(HPRTPrinterHelper.BARCODE, "0", "870", "4", "6", "ABC123");
    			HPRTPrinterHelper.PrintQR(HPRTPrinterHelper.BARCODE, "150", "870", "4", "6", "ABC123");
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "8", "0", "0", "1000", getResources().getString(R.string.activity_test_line));
    			HPRTPrinterHelper.Line("0", "1030", "400", "1030", "1");
    			HPRTPrinterHelper.Text(HPRTPrinterHelper.TEXT, "8", "0", "0", "1050", getResources().getString(R.string.activity_test_box));
    			HPRTPrinterHelper.Box("0", "1080", "400", "1300", "1");
    			if ("1".equals(Activity_Main.paper)) {
    				HPRTPrinterHelper.Form();
    			}
  		    	HPRTPrinterHelper.Print();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickWIFI ")).append(e.getMessage()).toString());
			}
    	}else if (view.getId()==R.id.btnExpress){
            AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Main.this);
            builder.setIcon(R.drawable.ic_launcher);
            final String[] cities = getResources().getStringArray(R.array.activity_main_express);
            builder.setItems(cities, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which){
                        //申通
                        case 0:
                            STexpress();
                            break;
                        //中通
                        case 1:
                            ZTexpress();
                            break;
                        //天天
                        case 2:
                            TTexpress();
                            break;
                        default:
                            break;
                    }
                }
            });
            builder.show();
        }
    }

    private void TTexpress() {
        try
        {
            HashMap<String, String> pum=new HashMap<String, String>();
            pum.put("[Referred]", "蒙 锡林郭勒盟");
            pum.put("[City]", "锡林郭勒盟 包");
            pum.put("[Number]", "108");
            pum.put("[Receiver]", "渝州");
            pum.put("[Receiver_Phone]", "15182429075");
            pum.put("[Receiver_address1]", "内蒙古自治区 锡林郭勒盟 正黄旗 解放东路与");//收件人地址第一行
            pum.put("[Receiver_address2]", "外滩路交叉口62号静安中学静安小区10栋2单元");//收件人第二行（若是没有，赋值""）
            pum.put("[Receiver_address3]", "1706室");//收件人第三行（若是没有，赋值""）
            pum.put("[Sender]", "洲瑜");
            pum.put("[Sender_Phone]", "13682429075");
            pum.put("[Sender_address1]", "浙江省 杭州市 滨江区 滨盛路1505号1706室信息部,滨盛路1505号滨盛");//寄件人地址第一行
            pum.put("[Sender_address2]", "滨盛路1505号1706室信息部");//寄件人第二行（若是没有，赋值""）
            pum.put("[Barcode]", "998016450402");
            pum.put("[Waybill]", "运单号：998016450402");
            pum.put("[Product_types]", "数码产品");
            pum.put("[Quantity]", "数量：22");
            pum.put("[Weight]", "重量：22.66KG");
            Set<String> keySet = pum.keySet();
            Iterator<String> iterator = keySet.iterator();
            InputStream afis =this.getResources().getAssets().open("TTKD.txt");//打印模版放在assets文件夹里
            String path = new String(InputStreamToByte(afis ),"utf-8");//打印模版以utf-8无bom格式保存
            while (iterator.hasNext()) {
                String string = (String)iterator.next();
                path = path.replace(string, pum.get(string));
            }
            HPRTPrinterHelper.printText(path);
            if ("1".equals(Activity_Main.paper)) {
                HPRTPrinterHelper.Form();
            }
            HPRTPrinterHelper.Print();
        }
        catch(Exception e)
        {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
        }
    }

    private void ZTexpress() {
        try
        {
            HashMap<String, String> pum=new HashMap<String, String>();
            pum.put("[payment]", "18");
            pum.put("[remark]", "上海");
            pum.put("[Barcode]", "376714121");
            pum.put("[orderCodeNumber]", "100");
            pum.put("[date]", "200");
            pum.put("[siteName]", "上海 上海市 长宁区");
            pum.put("[Receiver]", "申大通");
            pum.put("[Receiver_Phone]", "13826514987");
            pum.put("[Receiver_address]", "上海市宝山区共和新路47");
            pum.put("[Sender]", "快小宝");
            pum.put("[Sender_Phone]", "13826514987");
            pum.put("[Sender_address]", "上海市长宁区北曜路1178号（鑫达商务楼）");
            pum.put("[goodName1]", "鞋子");
            pum.put("[goodName2]", "衬衫");
            pum.put("[wight]", "10kg");
            pum.put("[price]", "200");
            pum.put("[payment]", "18");
            pum.put("[orderCode]", "12345");
            pum.put("[goodName]", "帽子");
            pum.put("[nowDate]", "2017.3.13");
            Set<String> keySet = pum.keySet();
            Iterator<String> iterator = keySet.iterator();
            InputStream afis =this.getResources().getAssets().open("ZhongTong.txt");//打印模版放在assets文件夹里
            String path = new String(InputStreamToByte(afis ),"utf-8");//打印模版以utf-8无bom格式保存
            while (iterator.hasNext()) {
                String string = (String)iterator.next();
                path = path.replace(string, pum.get(string));
            }
            HPRTPrinterHelper.printText(path);
            if ("1".equals(Activity_Main.paper)) {
                HPRTPrinterHelper.Form();
            }
            HPRTPrinterHelper.Print();
        }
        catch(Exception e)
        {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
        }
    }

    private void STexpress() {
        try
        {
            HashMap<String, String> pum=new HashMap<String, String>();
            pum.put("[barcode]", "363604310467");
            pum.put("[distributing]", "上海 上海市 长宁区");
            pum.put("[receiver_name]", "申大通");
            pum.put("[receiver_phone]", "13826514987");
            pum.put("[receiver_address1]", "上海市宝山区共和新路4719弄共");
            pum.put("[receiver_address2]", "和小区12号306室");//收件人地址第一行
            pum.put("[sender_name]", "快小宝");//收件人第二行（若是没有，赋值""）
            pum.put("[sender_phone]", "13826514987");//收件人第三行（若是没有，赋值""）
            pum.put("[sender_address1]", "上海市长宁区北曜路1178号（鑫达商务楼）");
            pum.put("[sender_address2]", "1号楼305室");
            Set<String> keySet = pum.keySet();
            Iterator<String> iterator = keySet.iterator();
            InputStream afis =this.getResources().getAssets().open("STO_CPCL.txt");//打印模版放在assets文件夹里
            String path = new String(InputStreamToByte(afis ),"utf-8");//打印模版以utf-8无bom格式保存
            while (iterator.hasNext()) {
                String string = (String)iterator.next();
                path = path.replace(string, pum.get(string));
            }
            HPRTPrinterHelper.printText(path);
            InputStream inbmp =this.getResources().getAssets().open("logo_sto_print1.png");
            Bitmap bitmap = BitmapFactory.decodeStream(inbmp);
            InputStream inbmp2 =this.getResources().getAssets().open("logo_sto_print2.png");
            Bitmap bitmap2 = BitmapFactory.decodeStream(inbmp2);
            HPRTPrinterHelper.Expanded("10", "20", bitmap,(byte)0);//向打印机发送LOGO
            HPRTPrinterHelper.Expanded("10", "712", bitmap2,(byte)0);//向打印机发送LOGO
            HPRTPrinterHelper.Expanded("10", "1016", bitmap2,(byte)0);//向打印机发送LOGO
            if ("1".equals(Activity_Main.paper)) {
                HPRTPrinterHelper.Form();
            }
            HPRTPrinterHelper.Print();
        }
        catch(Exception e)
        {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
        }
    }
    private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }
    private void paperAlertDialog(){
		
		final String[] papertype = getResources().getStringArray(R.array.activity_main_papertype);
		Builder builder = new AlertDialog.Builder(Activity_Main.this);
		 builder.setIcon(R.drawable.ic_launcher).setTitle(getResources().getString(R.string.activity_esc_function_btnopencashdrawer))
         .setItems(papertype, new OnClickListener() {

             @Override
             public void onClick(DialogInterface dialog, int which) {
            	 switch (which) {
				case 1:
					try {

						HPRTPrinterHelper.papertype_CPCL(1);
						PFun.WriteSharedPreferencesData("papertype", "1");
						btnOpenCashDrawer.setText(getResources().getString(R.string.activity_esc_function_btnopencashdrawer)+":"+papertype[1]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 0:
					try {

						HPRTPrinterHelper.papertype_CPCL(0);
						PFun.WriteSharedPreferencesData("papertype", "0");
						btnOpenCashDrawer.setText(getResources().getString(R.string.activity_esc_function_btnopencashdrawer)+":"+papertype[0]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
             }
         });
		 builder.create().show();
	}
	private void CapturePrinterFunction()
	{
		try
		{
			int[] propType=new int[1];
			byte[] Value=new byte[500];
			int[] DataLen=new int[1];
			String strValue="";
			boolean isCheck=false;
			if (PrinterName.equals("HM-T300")|PrinterName.equals("HM-A300")|PrinterName.equals("108B")|PrinterName.equals("R42")|PrinterName.equals("106B")) {
				btnCut.setVisibility(View.GONE);		
				btnOpenCashDrawer.setVisibility(View.VISIBLE);		
				btn1DBarcodes.setVisibility(View.VISIBLE);		
				btnQRCode.setVisibility(View.VISIBLE);		
//				btnPageMode.setVisibility(View.GONE);
				btnPDF417.setVisibility(View.GONE);		
				btnGetRemainingPower.setVisibility(View.GONE);		
				btnWIFI.setVisibility(View.VISIBLE);		
//				btnUSB.setVisibility(View.VISIBLE);		
				btnBT.setVisibility(View.VISIBLE);	
				btnSampleReceipt.setVisibility(View.VISIBLE);	
				btnGetStatus.setVisibility(View.VISIBLE);
			}
		}
		catch(Exception e)
		{
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
		}
	}
	

	
	private void PrintSampleReceipt()
	{
		try
		{
            String[] ReceiptLines = getResources().getStringArray(R.array.activity_main_sample_2inch_receipt);
            HPRTPrinterHelper.LanguageEncode="GBK";
            HPRTPrinterHelper.RowSetX("200");//设置X坐标
            HPRTPrinterHelper.Setlp("5","2","32");//5:字体这个是默认值。2：字体大小。32：设置的整行的行高。
            HPRTPrinterHelper.RowSetBold("2");//字体加粗2倍
            HPRTPrinterHelper.PrintData(ReceiptLines[0]+"\r\n");//小票内容
            HPRTPrinterHelper.RowSetBold("1");//关闭加粗
            HPRTPrinterHelper.RowSetX("100");
            HPRTPrinterHelper.Setlp("5","2","32");
            HPRTPrinterHelper.RowSetBold("2");
            HPRTPrinterHelper.PrintData(ReceiptLines[1]+"\r\n");
            HPRTPrinterHelper.RowSetBold("1");//关闭加粗
            HPRTPrinterHelper.RowSetX("100");
            for (int i = 2; i < ReceiptLines.length; i++) {
                HPRTPrinterHelper.Setlp("5","0","32");
                HPRTPrinterHelper.PrintData(ReceiptLines[i]+"\r\n");
            }
            HPRTPrinterHelper.RowSetX("0");
		}
		catch(Exception e)
		{
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
		}
	}
	
}
