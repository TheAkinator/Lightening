package com.example.lightening;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;

import java.util.UUID;

/**
 * Created by Dave Smith 
 * Double Encore, Inc.
 * MainActivity
 */

public class MainActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "BluetoothGattActivity";

    private static final String DEVICE_NAME = "IDAC BLE";

    /* Humidity Service */
    private static final UUID IDAC = UUID.fromString("12345678-8000-0080-5F9B-34FB00000000");
    
    /* Barometric Pressure Service */
    private static final UUID CHANGE_IDAC = UUID.fromString("12345679-0800-0008-05F9-B34FB0000000");
    private static final UUID MOTOR_VALUE = UUID.fromString("12345677-0800-0008-05F9-B34FB0000000");
    /* Client Configuration Descriptor */
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002903-0000-1000-8000-00805f9b34fb");


    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;

    private BluetoothGatt mConnectedGatt;
    private TextView led, seek;
    private Button highAlert, midAlert, noAlert;
    private ProgressDialog mProgress;
    private BluetoothDevice device;
    private BluetoothGatt myGatt;
    private SeekBar seekBar;
    private int IDACValue;
    
    private int mState = 1;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        setProgressBarIndeterminate(true);

        /*
         * We are going to display the results in some text fields
         */
        led = (TextView) findViewById(R.id.bluetooth);
        seek = (TextView) findViewById(R.id.seek);
        highAlert = (Button) findViewById(R.id.highAlert);
        midAlert = (Button) findViewById(R.id.midAlert);
        noAlert = (Button) findViewById(R.id.noAlert);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        
        
        /*
         * Bluetooth in Android 4.3 is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();

        /*
         * A progress dialog will be needed while the connection process is
         * taking place
         */
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        
        highAlert.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mState = 2;
				setAlertLevel(myGatt);
				readAlertLevel(myGatt);
				noAlert.setClickable(true);
				highAlert.setClickable(false);
			}
		});

        midAlert.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mState = 1;
				setAlertLevel(myGatt);
				readAlertLevel(myGatt);
			}
		});

        noAlert.setOnClickListener(new OnClickListener() {
	
        	@Override
			public void onClick(View v) {
				mState = 0;
				setAlertLevel(myGatt);
				readAlertLevel(myGatt);
				highAlert.setClickable(true);
				noAlert.setClickable(false);
		}
        });
        
        seek.setText(seekBar.getProgress() +"/"+ seekBar.getMax());
        
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progress = 0;
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				seek.setText(progress + "/" + seekBar.getMax());
				
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progresValue,
					boolean fromUser) {
				progress = progresValue;
				seek.setText(seekBar.getProgress() +"/"+ seekBar.getMax());
				
				IDACValue = (int) ( (seekBar.getProgress())*2.55 );
				    
				 
				 BluetoothGattCharacteristic characteristic;
				 characteristic = myGatt.getService(IDAC)
	                        .getCharacteristic(CHANGE_IDAC);
	                characteristic.setValue(new byte[] {(byte) IDACValue});
				
	                myGatt.writeCharacteristic(characteristic);
			}
		});
        
        highAlert.setClickable(false);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        //Make sure dialog is hidden
        mProgress.dismiss();
        //Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }
    

    @Override
    protected void onStop() {
        super.onStop();
        //Disconnect from any active tag connection
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.main, menu);
        //Add any device elements we've discovered to the overflow menu
        for (int i=0; i < mDevices.size(); i++) {
        	
            BluetoothDevice device = mDevices.valueAt(i);
            menu.add(0, mDevices.keyAt(i), 0, device.getName());

        }

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                mDevices.clear();
                startScan();
                return true;
            default:
                //Obtain the discovered device to connect with
                 device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to "+device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                //Display progress UI
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to "+device.getName()+"..."));
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         */
        if (DEVICE_NAME.equals(device.getName())) {
            mDevices.put(device.hashCode(), device);
            //Update the overflow menu
            invalidateOptionsMenu();

            
        }
    }
    
    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };
    
    private void startScan() {
        mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);

        mHandler.postDelayed(mStopRunnable, 2500);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);
    }
    
    private void setAlertLevel(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic;
        
        switch (mState) {
            case 0:
                Log.d(TAG, "Enabling pressure cal");
                characteristic = gatt.getService(IDAC)
                        .getCharacteristic(MOTOR_VALUE);
                characteristic.setValue(new byte[] {0x01});
                
                //updateLedValues(characteristic);
                break;
                
            case 1:
                Log.d(TAG, "Enabling pressure cal");
                characteristic = gatt.getService(IDAC)
                        .getCharacteristic(CHANGE_IDAC);
                
                characteristic.setValue(new byte[] {(byte) 0x01});
                
                //updateLedValues(characteristic);
                break;
                
            case 2:
                Log.d(TAG, "Enabling pressure cal");
                characteristic = gatt.getService(IDAC)
                        .getCharacteristic(MOTOR_VALUE);
                characteristic.setValue(new byte[] {(byte) 0x02});
                //updateLedValues(characteristic);
                break;
               
           
            default:
                mHandler.sendEmptyMessage(MSG_DISMISS);
                Log.i(TAG, "All Sensors Enabled");
                return;
        }
        
        
    

        gatt.writeCharacteristic(characteristic);
    }
    
    private void readAlertLevel(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic;
    
                characteristic = gatt.getService(IDAC)
                        .getCharacteristic(CHANGE_IDAC);
                
                int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
     		   
                led.setText(String.format("%d", value));                

 

        gatt.readCharacteristic(characteristic);
    }
   
   /*
    * In this callback, we've created a bit of a state machine to enforce that only
    * one characteristic be read or written at a time until all of our sensors
    * are enabled and we are registered to get notifications.
    */
   private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

       /* State Machine Tracking */
         

       @Override
       public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
           //After writing the enable flag, next we read the initial value

    	   //advance();
    	   
    		mHandler.sendEmptyMessage(MSG_DISMISS);
            Log.i(TAG, "All Sensors Enabled");
   			

           //enableNextSensor(gatt);
       }
       
       @Override
       public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    	   //updateLedValues(characteristic);
    	   mHandler.sendEmptyMessage(MSG_DISMISS);
           Log.i(TAG, "All Sensors Enabled");
       }
       
       @Override
       public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
           /*
            * After notifications are enabled, all updates from the device on characteristic
            * value changes will be posted here.  Similar to read, we hand these up to the
            * UI thread to update the display.
            */
           if (IDAC.equals(characteristic.getUuid())) {
               mHandler.sendMessage(Message.obtain(null, MSG_ALERT_LEVEL, characteristic));
           }

       }

       @Override
       public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
           //Once notifications are enabled, we move to the next sensor and start over with enable
          
           
           
       }
       
       @Override
       public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
           Log.d(TAG, "Connection State Change: "+status+" -> "+connectionState(newState));
           if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
               /*
                * Once successfully connected, we must next discover all the services on the
                * device before we can read and write their characteristics.
                */
               gatt.discoverServices();

               mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
           } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
               /*
                * If at any point we disconnect, send a message to clear the weather values
                * out of the UI
                */
               mHandler.sendEmptyMessage(MSG_CLEAR);
           } else if (status != BluetoothGatt.GATT_SUCCESS) {
               /*
                * If there is a failure at any stage, simply disconnect
                */
               gatt.disconnect();
           }
       }

       @Override
       public void onServicesDiscovered(BluetoothGatt gatt, int status) {
           Log.d(TAG, "Services Discovered: "+status);
           mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling psoc..."));
           /*
            * With services discovered, we are going to reset our state machine and start
            * working through the sensors we need to enable
            */
          // reset();
           //mProgress.dismiss();
           //readAlertLevel(myGatt);
           setAlertLevel(gatt);
           myGatt = gatt;
           
       }
       
      

       @Override
       public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
           Log.d(TAG, "Remote RSSI: "+rssi);
       }
       
       private String connectionState(int status) {
           switch (status) {
               case BluetoothProfile.STATE_CONNECTED:
                   return "Connected";
               case BluetoothProfile.STATE_DISCONNECTED:
                   return "Disconnected";
               case BluetoothProfile.STATE_CONNECTING:
                   return "Connecting";
               case BluetoothProfile.STATE_DISCONNECTING:
                   return "Disconnecting";
               default:
                   return String.valueOf(status);
           }
       }
       
   };


      
       
       
       /*
        * We have a Handler to process event results on the main thread
        */
       private static final int MSG_ALERT_LEVEL = 101;
       private static final int MSG_PROGRESS = 201;
       private static final int MSG_DISMISS = 202;
       private static final int MSG_CLEAR = 301;
       private Handler mHandler = new Handler() {
           @Override
           public void handleMessage(Message msg) {
               BluetoothGattCharacteristic characteristic;
               switch (msg.what) {
                   case MSG_ALERT_LEVEL:
                       characteristic = (BluetoothGattCharacteristic) msg.obj;
                       if (characteristic.getValue() == null) {
                           Log.w(TAG, "Error obtaining led");
                           return;
                       }
                       updateLedValues(characteristic);
                       break;
                   case MSG_PROGRESS:
                       mProgress.setMessage((String) msg.obj);
                       if (!mProgress.isShowing()) {
                           mProgress.show();
                       }
                       break;
                   case MSG_DISMISS:
                       mProgress.hide();
                       break;
                   case MSG_CLEAR:
                       led.setText("");
                       break;
               }
           }
       };
       
       private void updateLedValues(BluetoothGattCharacteristic characteristic) {
           int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        		   
           led.setText(String.format("%d", value));
       }
       

}
