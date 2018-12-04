package com.rk.bluetooth_send;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created for BlueToothPrint
 */
public class BlueToothPrint_new {
    private static boolean receiver = false;
    private final BluetoothChatService mChatService;
    Set<BluetoothDevice> pairedDevices;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Activity context;
    static int exception_count = 0;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;
    private AlertDialog alertDialog;
    private ArrayList<BluetoothDevice> devicelist = new ArrayList<BluetoothDevice>();
    private ArrayList<BluetoothDevice> unBondedDevicelist = new ArrayList<BluetoothDevice>();

    private BluetoothConnector_new blue;
    static boolean exception = false;
    SharedPreferences sharedPrefs;
    private ConnectRunnable connector;
    static BlueToothPrint_new bluetoothnew;
    private ArrayList<String> discoveredDevicesAdapter = new ArrayList<>();
    private Connectivity callback;
    private boolean secure = true;
    //    private boolean tamillang = false;
    View layout = null;

    public static BlueToothPrint_new getinstance(Activity activity) {
        if (bluetoothnew == null) {
            bluetoothnew = new BlueToothPrint_new(activity);
        }
        return bluetoothnew;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    Log.e("bluetoothActionA", "" + deviceName + "--->" + deviceAddress);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Log.e("bluetoothActionC", "" + action);
                    layout = dialog(devicelist, unBondedDevicelist, R.layout.bluetooth_device_alert, context.getResources().getString(R.string.title_bluetooth_devices));
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        unBondedDevicelist.add(device);
                        Log.e("unBondedDevicelist",""+unBondedDevicelist.size());
                        showDevice(device, layout);
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                        if (discoveredDevicesAdapter.size() == 0) {
//                            discoveredDevicesAdapter.add(getString(R.string.none_found));
//                        }
                    /*if (unBondedDevicelist != null && unBondedDevicelist.size() > 0)
                        dialog(devicelist, unBondedDevicelist, R.layout.bluetooth_device_alert, context.getResources().getString(R.string.title_bluetooth_devices));
                    else {
                        Toast.makeText(context, getstring(R.string.pair_device), Toast.LENGTH_SHORT).show();
                    }*/
                }
            } catch (Exception e) {
                System.out.println("Broadcast Error : " + e.toString());
                Log.e("Broadcast Error : " , e.toString());
            } finally {
//                ((BillSuccessActivity) context).enableButton();
            }
        }
    };
    private final BroadcastReceiver bluetoothreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
//                        Toast.makeText(context, "Bluetooth turned  on", Toast.LENGTH_SHORT).show();
//
//                        if (exception)
//                        connect();
                        exception_count += 1;
                        break;
                    case BluetoothAdapter.STATE_OFF:
//                        Toast.makeText(context, "Bluetooth turned  off", Toast.LENGTH_SHORT).show();
                        try {
                            Thread.sleep(3000);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        mBluetoothAdapter.enable();
                        break;
                }
            }
        }
    };
    private String printerDeviceAddress = "printeraddress";

    public BlueToothPrint_new(Activity context) {
        exception_count = 0;
        this.context = context;
        sharedPrefs = context.getSharedPreferences("FPS_POS",
                MODE_PRIVATE);
        exception = false;
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                while (!mBluetoothAdapter.isEnabled()) {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        if (!receiver) {
            receiver = true;
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(bluetoothreceiver, filter);

        }
        create_handler();
        mChatService = new BluetoothChatService(context, mHandler);
    }

    private void create_handler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                            case BluetoothChatService.STATE_NONE:
                                break;
                        }
                        break;
                    case Constants.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        Log.e("Received", readMessage);
                        callback.message("To : " + readMessage);
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        if (null != context) {
                            Toast.makeText(context, "Connected to "
                                    + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        }
                        callback.connected();
                        break;
                    case Constants.MESSAGE_TOAST:
                        if (null != context) {
                            Toast.makeText(context, msg.getData().getString(Constants.TOAST),
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
    }

    private void getBondeddevicelist() {
//        mBluetoothAdapter.startDiscovery();
        pairedDevices = mBluetoothAdapter.getBondedDevices();
//        mBluetoothAdapter.cancelDiscovery();
        devicelist.clear();
        for (BluetoothDevice device : pairedDevices) {
            devicelist.add(device);
        }
//        if (devicelist != null && devicelist.size() > 0)
//            dialog(devicelist, unBondedDevicelist, R.layout.bluetooth_device_alert, context.getResources().getString(R.string.title_bluetooth_devices));
//        else {
//            Toast.makeText(context, getstring(R.string.pair_device), Toast.LENGTH_SHORT).show();
//        }
    }
//    private void findBT() {
//        try {
////            Log.e("findBT", "findBT()");
////            Log.e("bluetooth", "" + mBluetoothAdapter.toString());
//            //            Log.e("getAddress", "" + device.getAddress());
////            Log.e("name", "" + device.getName());
////            Log.e("desc content", "" + device.describeContents());
////            Log.e("fetchuuid", "" + device.fetchUuidsWithSdp());
////            Log.e("getbondstate", "" + device.getBondState());
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
////                Log.e("type", "" + device.getType());
////            }
////            Log.e("uuids", "" + device.getUuids());
////                    break;
////            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
////            intial_receiver();
////            context.registerReceiver(mReceiver, filter);
//            Log.e("BluetoothpairedDevices", "" + pairedDevices);
//            //int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
//            //String mDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
//            // Log.e("pairedDevices.getClass", ""+pairedDevices.getClass().toString());
//            if (pairedDevices.size() > 0) {
//            } else {
//                Log.e("BluetoothpairedDevices", "no paired devices...");
////                printDialogStatus(context.getString(R.string.no_printer));
////                ((BillSuccessActivity) context).enableButton2();
//            }
//        } catch (Exception e) {
//            Log.e("findBT Error", e.toString(), e);
//        } finally {
////            ((BillSuccessActivity) context).enableButton();
//        }
//    }

//    public void printCall() {
//        try {
//            findBT();
//            /*openBT();//socket connection
//            BluetoothPrintData();*/
//        } catch (Exception e) {
//            Log.e("Print Call", e.toString(), e);
//        } finally {
////            ((BillSuccessActivity) context).enableButton();
//        }
//    }

    private String getstring(int i) {
        return context.getResources().getString(i);
    }

    // Tries to open a connection to the bluetooth printer device
    public void connect() {
//        if (connector != null) {
//            connector.cancel();
//            connector = null;
//        }
//        connector = new ConnectRunnable();
//        connector.start();
        mChatService.connect(mmDevice, secure);
    }

    public void opendialog() {
        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
        String device_address = sharedPrefs.getString(printerDeviceAddress, "");
        if (!device_address.isEmpty()) {
//            if (mBluetoothAdapter == null)
//                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mmDevice == null || !mmDevice.getAddress().equals(device_address)) {
                mmDevice = mBluetoothAdapter.getRemoteDevice(device_address);
            }
            connect();
        } else {
            getBondeddevicelist();
            getUnbond_deviceList();

        }
    }

    private void getUnbond_deviceList() {

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
//        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
//        context.registerReceiver(mReceiver, filter);
    }

    public void disconnect() {
        if (mChatService != null) {
            mChatService.stop();
        }
    }


    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            callback.message("From : " + message);
        }
    }

    public void start() {
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start(secure);
            }
        }
    }

    public void setCallBack(Connectivity callback) {
        this.callback = callback;
    }

    private class ConnectRunnable extends Thread {
        public void run() {
            try {
                if (mmSocket == null || !mmSocket.isConnected()) {
                    blue = new BluetoothConnector_new(context, mmDevice, true, mBluetoothAdapter, null);
                    mmSocket = blue.connect();
                }
                if (mmSocket != null) {
                    exception = false;
                    mmOutputStream = mmSocket.getOutputStream();
                    mmInputStream = mmSocket.getInputStream();

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, context.getResources().getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("Tag", "bluetooth connected");
                } else {
                    exception = true;
//                    Toast.makeText(context, context.getResources().getString(R.string.waittoast), Toast.LENGTH_SHORT).show();
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, context.getResources().getString(R.string.no_device), Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("Tag", "bluetooth not connected");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Tag", "bluetooth not connected");
            }
        }

        public void cancel() {
//            try {
//                if (mmSocket != null)
//                    mmSocket.close();
//            } catch (IOException e) {
//                Log.d("TAG", "Canceled connection", e);
//            }
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            if (mmSocket != null)
                mmSocket.close();
            Log.v("TAG", "SockectClosed");
        } catch (IOException ex) {
            Log.d("TAG", "Could not close exisiting socket", ex);
        }
    }

    public void IntentPrint(String txtValue) {
        try {
//            Thread.sleep(2000);
            Log.e("txtValue", txtValue);
            byte[] buffer = txtValue.getBytes();
            Log.e("buffer", "" + buffer);
            byte[] PrintHeader = {(byte) 0x55, 0, 0};
            Log.e("PrintHeader", "" + PrintHeader);
            PrintHeader[2] = (byte) buffer.length;
            Log.e("PrintHeader[2]", "" + PrintHeader[2]);
            if (PrintHeader.length > 128) {
                String value = "\nValue is more than 128 size\n";
            } else {
//                Set font size here
//                byte[] arrayOfByte1 = { 27, 33, 0 };
//                byte[] format = { 27, 33, 0 };
//                format[2] = ((byte)(0x1 | arrayOfByte1[2]));
//                mmOutputStream.write(format);
                for (int i = 4; i <= PrintHeader.length - 1; i++) {
                    //Log.e("PrintHeader.length", ""+PrintHeader.length);
                    if (mmOutputStream != null) {
                        mmOutputStream.write(PrintHeader[i]);
                        // Log.e("1* ", "PrintHeader[i] = "+PrintHeader[i]);
                    }
                }
                for (int i = 4; i <= buffer.length - 1; i++) {
                    if (mmOutputStream != null) {
                        //  byte[] format = { 27, 33, 0 };
                        // Bold
                        //format[2] = ((byte)(0x8 | PrintHeader[2]));
                        //Small
                        // format[2] = ((byte)(0x1 | PrintHeader[2]));
                        //UnderLine
                        //format[2] = ((byte)(0x80 | PrintHeader[2]));
                        ////  byte[] format = {15, 33, 35 }; // manipulate your font size in the second parameter
                        //  byte[] center =  { 0x1b, 'a', 0x01 }; // center alignment
                        //  mmOutputStream.write(format);
                        //  mmOutputStream.write(center);
                        // out.write(str.getBytes(),0,str.getBytes().length);
                        mmOutputStream.write(buffer[i]);
                        // Log.e("2* ", "buffer[i] = "+buffer[i]);
                    }
                }
//                if (mmOutputStream != null)
//                    mmOutputStream.flush();
//                printDialogStatus(context.getString(R.string.print_success));
//                if (mReceiver != null)
//                    context.unregisterReceiver(mReceiver);
//                if (mmOutputStream != null)
//                    mmOutputStream.close();
//                if (mmInputStream != null)
//                    mmInputStream.close();
//                mmSocket.close();
//                blue.bluetoothSocket.close();
                try {
                    if (receiver) {
                        context.unregisterReceiver(bluetoothreceiver);
                        receiver = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    receiver = false;
                }
            }
        } catch (Exception e) {
            Log.e("errorPrint", e.toString(), e);
        } finally {
//            ((BillSuccessActivity) context).enableButton();
        }
    }

    private String pad(String value, int length, String with) {
        StringBuilder result = new StringBuilder(length);
        // Pre-fill a String value
        result.append(fill(Math.max(0, length - value.length()), with));
        result.append(value);
        return result.toString();
    }
//    private void printDialogStatus(final String printerStatus) {
//        context.runOnUiThread(new Runnable() {
//            public void run() {
//                Util.messageBar(context, printerStatus);
//                Log.e("Local class", context.getLocalClassName());
//                if (context.getLocalClassName().contains("BillSuccessActivity")) {
//                    ((BillSuccessActivity) context).enableButtonAfterPrint();
//                }
//            }
//        });
//    }

    private String fill(int length, String with) {
        StringBuilder sb = new StringBuilder(length);
        while (sb.length() < length) {
            sb.append(with);
        }
        return sb.toString();
    }
//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            try {
//                String action = intent.getAction();
//                // When discovery finds a device
//                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
//                    // Get the BluetoothDevice object from the Intent
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    String deviceName = device.getName();
//                    String deviceAddress = device.getAddress();
//                    Log.e("bluetoothActionA", "" + deviceName + "--->" + deviceAddress);
//                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                    Log.e("bluetoothActionB", "" + action);
//                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                    Log.e("bluetoothActionC", "" + action);
//                }
//            } catch (Exception e) {
//                System.out.println("Broadcast Error : " + e.toString());
//            } finally {
////                ((BillSuccessActivity) context).enableButton();
//            }
//        }
//    };

    private View dialog(final ArrayList<BluetoothDevice> bondedList, ArrayList<BluetoothDevice> unbondedList, int cus_layout, String title) {
        inflater = (LayoutInflater)
                context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        builder = new AlertDialog.Builder(context);
        View layout = inflater.inflate(cus_layout,
                null);
        LinearLayout llay = (LinearLayout) layout.findViewById(R.id.container);
        ScrollView sv = (ScrollView) layout.findViewById(R.id.scrollView);
        final ArrayList<BluetoothDevice> totalList = new ArrayList<>();
        if (bondedList.size() > 0)
            totalList.addAll(bondedList);
        if (unbondedList.size() > 0)
            totalList.addAll(unbondedList);

        for (int i = 0; i < totalList.size(); i++) {
            LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutparams.setMargins(10, 10, 10, 10);
            TextView txt = new TextView(context);
            txt.setLayoutParams(layoutparams);
            txt.setText(totalList.get(i).getName() + "\n" + totalList.get(i).getAddress());
            txt.setTextSize(20);
            txt.setTag(i);
            txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mmDevice = totalList.get((int) v.getTag());
//                    sharedPrefs.edit().putString(printerDeviceAddress, mmDevice.getAddress()).commit();
                    connect();//socket connection
//                        Log.e("socket status",""+Boolean.toString(blue.success));
////                        mmSocket=blue.bluetoothSocket.getOutputStream();
//                        while (!blue.success) {
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            Log.e("socket status",""+Boolean.toString(blue.success));
//                            if (blue.success)
//                                break;
//                        }
//                    mBluetoothAdapter.disable();
                    alertDialog.hide();
                }
            });
            llay.addView(txt);
        }
//            }
//        }
        LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutparams.setMargins(10, 10, 10, 10);
        TextView txt1 = new TextView(context);
        txt1.setLayoutParams(layoutparams);
        txt1.setText("UnPaired Devices");
        txt1.setTypeface(Typeface.DEFAULT_BOLD);
        txt1.setTextSize(25);
        llay.addView(txt1);
        sv.invalidate();
        sv.requestLayout();
        TextView tv = (TextView) layout.findViewById(R.id.title);
        tv.setText(title);
        builder.setView(layout);
        alertDialog = builder.create();
//        alertDialog.setCancelable(false);
        alertDialog.show();
        return layout;
    }

    private View showDevice(final BluetoothDevice device, View layout) {
        LinearLayout llay = (LinearLayout) layout.findViewById(R.id.container);
        ScrollView sv = (ScrollView) layout.findViewById(R.id.scrollView);
        LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutparams.setMargins(10, 10, 10, 10);
        TextView txt = new TextView(context);
        txt.setLayoutParams(layoutparams);
        txt.setText(device.getName() + "\n" + device.getAddress());
        txt.setTextSize(20);
//        txt.setTag(devicelist.size() + unBondedDevicelist.size());
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmDevice = device;
                alertDialog.hide();
            }
        });
        llay.addView(txt);
        sv.invalidate();
        sv.requestLayout();
        builder.setView(layout);
//        alertDialog = builder.create();
//        alertDialog.setCancelable(false);
//        alertDialog.show();
        return layout;
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == MY_BLUETOOTH_ENABLE_REQUEST_ID) {
//            if (resultCode == RESULT_OK) {
//                // Request granted - bluetooth is turning on...
//            }
//            if (resultCode == RESULT_CANCELED) {
//                // Request denied by user, or an error was encountered while
//                // attempting to enable bluetooth
//            }
//        }
//    }

    private Handler mHandler = null;
}