package com.honeywell.usbakerydex;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.p000v4.app.NotificationCompat.Builder;
import android.support.p000v4.view.MotionEventCompat;
import android.util.Log;
import com.honeywell.dexjar.CDEX;
import com.honeywell.dexjar.CommunicationMethod;
import com.honeywell.dexjar.IDEX;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

public class MyService extends Service {
    private static final String CALLER_APP_ID = "callerAppID";
    /* access modifiers changed from: private */
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /* access modifiers changed from: private */
    public static final UUID DEX_CHARACTERISTIC_DATA = UUID.fromString("F000C0E1-0451-4000-B000-000000000000");
    /* access modifiers changed from: private */
    public static final UUID DEX_SERVICE_SPP = UUID.fromString("F000C0E0-0451-4000-B000-000000000000");
    private final String BATTERY_PRE = "battery:";
    /* access modifiers changed from: private */
    public final StringBuilder DataReadOperationSync = new StringBuilder();
    private final int INDEX_FIRMWARE = 2;
    private final String MODULE_SPI_PARAM_APP_START_INTENT_PARAMS_JSON = "JSON";
    private final String TAG = "DEX";
    private boolean bDexInited = false;
    private String blebatteryinfo = null;
    private String blerevisioninfo = null;
    /* access modifiers changed from: private */
    public CallMe callMe = null;
    /* access modifiers changed from: private */
    public BluetoothGattCharacteristic charac = null;
    /* access modifiers changed from: private */
    public CommunicationMethod commMethod = CommunicationMethod.NONE;
    /* access modifiers changed from: private */
    public String config = "";
    /* access modifiers changed from: private */
    public String connectedHandleAddress = null;
    private ServiceConnection connection;
    private String dexDir = "";
    /* access modifiers changed from: private */
    public DEXThread dexThread = null;
    /* access modifiers changed from: private */
    public int evtSrcId = 0;
    /* access modifiers changed from: private */
    public String instanceName = "";
    /* access modifiers changed from: private */
    public JSONObject jsonObject;
    String jsonString = "";
    private LeScanCallback mBLEScanCallback = new LeScanCallback() {
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            new Thread(new Runnable() {
                public void run() {
                    String address = bluetoothDevice.getAddress();
                    if (MyService.this.connectedHandleAddress != null && MyService.this.connectedHandleAddress.equalsIgnoreCase(address)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Scan Callback - Device address: ");
                        sb.append(address);
                        Log.i("DEX", sb.toString());
                        MyService.this.mBluetoothDevice = bluetoothDevice;
                        Log.i("DEX", "Scan Callback - Set Gatt connection");
                        MyService.this.mConnectedGatt = MyService.this.mBluetoothDevice.connectGatt(MyService.this.mContext, false, MyService.this.mGattCallback);
                        if (MyService.this.mConnectedGatt == null) {
                            Log.d("DEX", "connectGatt FAILURE");
                        } else {
                            Log.d("DEX", "connectGatt SUCCESS");
                        }
                    }
                }
            }).start();
        }
    };
    private BluetoothAdapter mBluetoothAdapter;
    /* access modifiers changed from: private */
    public BluetoothDevice mBluetoothDevice;
    /* access modifiers changed from: private */
    public boolean mConnected = false;
    /* access modifiers changed from: private */
    public BluetoothGatt mConnectedGatt = null;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            super.onConnectionStateChange(bluetoothGatt, i, i2);
            Log.i("DEX", String.format("BluetoothGatt.onConnectionStateChanged() - status: %d  newState : %d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)}));
            if (i == 0 && i2 == 2) {
                Log.i("DEX", "********** GATT CONNECTED **********");
                Log.d("DEX", "call gatt.discoverServices()");
                bluetoothGatt.discoverServices();
            }
        }

        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            super.onServicesDiscovered(bluetoothGatt, i);
            Log.i("DEX", "BluetoothGatt.onServicesDiscovered");
            if (MyService.this.charac == null) {
                int i2 = 0;
                while (true) {
                    if (i2 >= 4) {
                        break;
                    } else if (MyService.this.mConnectedGatt.getService(MyService.DEX_SERVICE_SPP) != null) {
                        MyService.this.charac = bluetoothGatt.getService(MyService.DEX_SERVICE_SPP).getCharacteristic(MyService.DEX_CHARACTERISTIC_DATA);
                        break;
                    } else {
                        i2++;
                        MyService.this.SleepThreadForTime(5000);
                        Log.i("DEX", "Error in BluetoothGattCharacteristic retry");
                    }
                }
            }
            if (MyService.this.charac == null) {
                Log.i("DEX", "Error in creation of BluetoothGattCharacteristic object\n");
                return;
            }
            Log.i("DEX", "++++ set mConnected = true +++++");
            MyService.this.mConnected = true;
            if (bluetoothGatt.setCharacteristicNotification(MyService.this.charac, true)) {
                Log.i("DEX", "++ set characteristic success\n");
            } else {
                Log.i("DEX", "++ set characteristic failed\n");
            }
            BluetoothGattDescriptor descriptor = MyService.this.charac.getDescriptor(MyService.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }

        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, i);
        }

        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, i);
        }

        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            super.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
            if (MyService.DEX_CHARACTERISTIC_DATA.equals(bluetoothGattCharacteristic.getUuid())) {
                synchronized (MyService.this.DataReadOperationSync) {
                    byte[] value = bluetoothGattCharacteristic.getValue();
                    MyService.this.objDex.WriteDataToDEXFromBLE(value);
                    for (byte GetAsciiCode : value) {
                        String GetAsciiCode2 = MyService.this.GetAsciiCode(GetAsciiCode, 1);
                        if (!GetAsciiCode2.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("SF->DSD:");
                            sb.append(GetAsciiCode2);
                            Log.d("DEX", sb.toString());
                        }
                    }
                }
            }
        }

        public void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            super.onDescriptorRead(bluetoothGatt, bluetoothGattDescriptor, i);
        }

        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            super.onDescriptorWrite(bluetoothGatt, bluetoothGattDescriptor, i);
        }

        public void onReliableWriteCompleted(BluetoothGatt bluetoothGatt, int i) {
            super.onReliableWriteCompleted(bluetoothGatt, i);
        }

        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int i, int i2) {
            super.onReadRemoteRssi(bluetoothGatt, i, i2);
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        BluetoothDevice device;

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
                Log.i("DEX", "onReceive: ACTION_PAIRING_REQUEST");
            } else if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
                Log.i("DEX", "onReceive: ACTION_BOND_STATE_CHANGED");
                this.device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            } else if ("android.bluetooth.device.action.ACL_CONNECTED".equals(action)) {
                Log.i("DEX", "onReceive: ACTION_ACL_CONNECTED");
            }
        }
    };
    /* access modifiers changed from: private */
    public Messenger messenger = null;
    /* access modifiers changed from: private */
    public long nErrorCode = 0;
    /* access modifiers changed from: private */
    public IDEX objDex;
    private Intent paramsIntent = null;
    private String pin = null;
    /* access modifiers changed from: private */
    public String portName = "";
    private Messenger replyTo = null;
    /* access modifiers changed from: private */
    public String strErrorMsg = "";
    private int synchType = 0;

    private class CallMe implements ConnectionEvent {
        /* access modifiers changed from: private */

        /* renamed from: cn */
        public ConnectionNotifier f13cn;

        private CallMe() {
            this.f13cn = new ConnectionNotifier(this);
        }

        public void connectionEvent() {
            Log.i("DEX", String.format("***** in connectionEvent() *****\nerror: %d\n message: %s", new Object[]{Long.valueOf(MyService.this.nErrorCode), MyService.this.strErrorMsg}));
            MyService.this.doSendMessage(MyService.this.nErrorCode, MyService.this.strErrorMsg);
        }
    }

    public interface ConnectionEvent {
        void connectionEvent();
    }

    private class ConnectionNotifier {
        /* access modifiers changed from: private */
        public boolean bConnected;

        /* renamed from: ce */
        private ConnectionEvent f14ce;

        private ConnectionNotifier(ConnectionEvent connectionEvent) {
            this.f14ce = connectionEvent;
            this.bConnected = false;
        }

        /* access modifiers changed from: private */
        public void doWork() {
            long j;
            String str = "";
            Log.i("DEX", "connectionNotifier.doWork()");
            if (this.bConnected) {
                Log.i("DEX", "message handler isConnected");
                Log.i("DEX", String.format("doWork() - evtSrcId = %d", new Object[]{Integer.valueOf(MyService.this.evtSrcId)}));
                Log.i("DEX", "Initialization - checking for connection...");
                Log.i("DEX", String.format("Initialization - deal with content: '%s'", new Object[]{str}));
                if (!str.isEmpty()) {
                    for (int i = 0; i <= 2; i++) {
                        MyService.this.dealWithContent(str, i);
                    }
                } else if (!MyService.this.SetMACAddressFromPairedDevice()) {
                    MyService.this.nErrorCode = 9000;
                    MyService.this.strErrorMsg = "DEX Adapter Error: Check pairing";
                    this.f14ce.connectionEvent();
                    return;
                }
                Log.i("DEX", String.format("post dealWithContent(): '%s'", new Object[]{MyService.this.connectedHandleAddress}));
                Log.i("DEX", String.format("Call DexInitialization(%s, %s)", new Object[]{MyService.this.instanceName, MyService.this.config}));
                long DexInitialization = MyService.this.objDex.DexInitialization(MyService.this.instanceName, MyService.this.config);
                Log.i("DEX", String.format("DEXInitialization() result: %d", new Object[]{Long.valueOf(DexInitialization)}));
                if (DexInitialization != 0) {
                    MyService.this.nErrorCode = DexInitialization;
                    MyService.this.strErrorMsg = "DEXInitialization() error";
                    this.f14ce.connectionEvent();
                    return;
                }
                Log.d("DEX", "SetCommunicationMethod()");
                long SetCommunicationMethod = MyService.this.objDex.SetCommunicationMethod(MyService.this.commMethod, MyService.this.portName);
                if (SetCommunicationMethod != 0) {
                    Log.i("DEX", String.format("DEX SetCommunicationMethod() error: %d", new Object[]{Long.valueOf(SetCommunicationMethod)}));
                    MyService.this.nErrorCode = SetCommunicationMethod;
                    MyService.this.strErrorMsg = "SetCommunicationMethod error";
                    this.f14ce.connectionEvent();
                    return;
                }
                Log.i("DEX", "Initialization Process");
                MyService.this.strErrorMsg = "Initialization completed.";
                try {
                    String access$1200 = MyService.this.getIgnoreCase(MyService.this.jsonObject, "config");
                    if (access$1200 != null) {
                        long access$1300 = MyService.this.SetConfigurationOptions(MyService.this.jsonObject.getJSONObject(access$1200));
                        if (access$1300 != 0) {
                            Log.i("DEX", String.format("SetConfigurationOptions() error: %d", new Object[]{Long.valueOf(access$1300)}));
                            MyService.this.nErrorCode = access$1300;
                            MyService.this.strErrorMsg = "SetConfigurationOptions error";
                            this.f14ce.connectionEvent();
                            return;
                        }
                    }
                    if (MyService.this.evtSrcId == 7363 || MyService.this.evtSrcId == 7328) {
                        Log.d("DEX", "Defining & Starting DEXThread");
                        MyService.this.dexThread = new DEXThread();
                        MyService.this.dexThread.setPriority(10);
                        MyService.this.dexThread.start();
                        long currentTimeMillis = System.currentTimeMillis();
                        while (true) {
                            int currentTimeMillis2 = (int) ((System.currentTimeMillis() - currentTimeMillis) / 1000);
                            if (!MyService.this.dexThread.bHasStarted) {
                                if (currentTimeMillis2 > 5) {
                                    Log.i("DEX", "Waiting for dexThread.start()");
                                    break;
                                }
                            } else {
                                Log.i("DEX", "dexThread has started");
                                break;
                            }
                        }
                    }
                    if (MyService.this.evtSrcId == 2573) {
                        Log.i("DEX", "Close Process");
                        this.f14ce.connectionEvent();
                        return;
                    }
                    if (MyService.this.mBluetoothDevice == null) {
                        Log.i("DEX", "Initialization - mBluetoothDevice is null.");
                    } else if (MyService.this.mConnectedGatt == null) {
                        Log.i("DEX", "Initialization - mConnectedGatt is null.");
                    } else {
                        Log.d("DEX", String.format("Gatt Connection State: %s", new Object[]{Integer.valueOf(MyService.this.mConnectedGatt.getConnectionState(MyService.this.mBluetoothDevice))}));
                    }
                    if (MyService.this.WaitForBLEConnection() != 0) {
                        MyService.this.nErrorCode = 9000;
                        MyService.this.strErrorMsg = "DEX Adapter Error: Press power button";
                        this.f14ce.connectionEvent();
                    } else if (MyService.this.evtSrcId == 7363) {
                        if (MyService.this.mConnectedGatt == null || MyService.this.charac == null) {
                            if (MyService.this.mConnectedGatt == null) {
                                Log.e("DEX", "mConnectedGatt is null.");
                            } else if (MyService.this.charac == null) {
                                Log.e("DEX", "charac is null.");
                            }
                            Log.e("DEX", "NOT CONNECTED - cannot send.");
                        } else {
                            Log.i("DEX", "PROCESS_SEND - connected, starting DEX process");
                            try {
                                if (MyService.this.getIgnoreCase(MyService.this.jsonObject, "transaction") != null) {
                                    Log.i("DEX", "Has transaction - call WriteInvoices()");
                                    j = MyService.this.nErrorCode = MyService.this.WriteTransaction(MyService.this.jsonObject.getJSONObject("transaction"));
                                } else {
                                    j = MyService.this.nErrorCode = -1;
                                    MyService.this.strErrorMsg = "No transaction found";
                                }
                                synchronized (MyService.this.dexThread) {
                                    if (j == 0) {
                                        try {
                                            Log.i("DEX", "SendDexData()");
                                            j = MyService.this.objDex.SendDexData();
                                        } catch (Exception e) {
                                            String str2 = "DEX";
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("Exception occurred during SendDexData: ");
                                            sb.append(e.getMessage());
                                            Log.i(str2, sb.toString());
                                        }
                                        if (j != 0) {
                                            StringBuilder sb2 = new StringBuilder();
                                            sb2.append("SendDexData failed - Error Code: ");
                                            sb2.append(j);
                                            Log.d("DEX", sb2.toString());
                                            MyService.this.nErrorCode = j;
                                            MyService.this.strErrorMsg = MyService.this.objDex.GetErrorMessage();
                                            Log.d("DEX", String.format("SendDexData failed - Error Msg: %s", new Object[]{MyService.this.strErrorMsg}));
                                        } else {
                                            StringBuilder sb3 = new StringBuilder();
                                            sb3.append("SendDexData completed: ");
                                            sb3.append(j);
                                            Log.d("DEX", sb3.toString());
                                            MyService.this.nErrorCode = j;
                                            MyService.this.strErrorMsg = "Invoice(s) sent.";
                                        }
                                    }
                                    if (j == 0) {
                                        this.f14ce.connectionEvent();
                                    } else {
                                        MyService.this.CheckResult("SendDexData(): ", j);
                                    }
                                }
                            } catch (Exception e2) {
                                StringBuilder sb4 = new StringBuilder();
                                sb4.append("Exception in doDEX().ProcessSend: ");
                                sb4.append(e2.toString());
                                Log.e("DEX", sb4.toString());
                                MyService.this.nErrorCode = -1;
                                MyService.this.strErrorMsg = "Exception in ProcessSend()";
                                this.f14ce.connectionEvent();
                            }
                        }
                    } else if (MyService.this.evtSrcId == 7328) {
                        Log.i("DEX", "PROCESS_RECEIVE - checking for connection...");
                        if (!MyService.this.mConnected) {
                            Log.i("DEX", "connectionEvent(): PROCESS_RECEIVE - BLEAdapter not connected");
                            this.f14ce.connectionEvent();
                        } else if (MyService.this.mConnectedGatt == null || MyService.this.charac == null) {
                            if (MyService.this.mConnectedGatt == null) {
                                Log.e("DEX", "mConnectedGatt is null.");
                            } else if (MyService.this.charac == null) {
                                Log.e("DEX", "charac is null.");
                            }
                            Log.e("DEX", "NOT CONNECTED - cannot send.");
                        } else {
                            Log.i("DEX", "PROCESS_RECEIVE - connected, starting DEX process");
                            try {
                                synchronized (MyService.this.dexThread) {
                                    StringBuilder sb5 = new StringBuilder();
                                    Log.d("DEX", "ReceiveDexData()");
                                    long ReceiveDexData = MyService.this.objDex.ReceiveDexData(sb5);
                                    if (ReceiveDexData == 0) {
                                        String sb6 = sb5.toString();
                                        StringBuilder sb7 = new StringBuilder();
                                        sb7.append("JSON Data:\n");
                                        sb7.append(sb6);
                                        sb7.append("\n");
                                        Log.d("DEX", sb7.toString());
                                        MyService.this.strErrorMsg = sb5.toString();
                                    } else {
                                        Log.d("DEX", "GetErrorMessage()");
                                        if (MyService.this.objDex == null) {
                                            Log.d("DEX", "objDex is null - 10");
                                        }
                                        String GetErrorMessage = MyService.this.objDex.GetErrorMessage();
                                        StringBuilder sb8 = new StringBuilder();
                                        sb8.append("Error: ");
                                        sb8.append(ReceiveDexData);
                                        sb8.append("\n");
                                        sb8.append(GetErrorMessage);
                                        sb8.append("\n");
                                        Log.d("DEX", sb8.toString());
                                        MyService.this.strErrorMsg = MyService.this.objDex.GetErrorMessage();
                                    }
                                    if (ReceiveDexData == 0) {
                                        this.f14ce.connectionEvent();
                                    } else {
                                        MyService.this.CheckResult("ReceiveDexData(): ", ReceiveDexData);
                                    }
                                }
                            } catch (Exception e3) {
                                StringBuilder sb9 = new StringBuilder();
                                sb9.append("Exception in Process RECV: ");
                                sb9.append(e3.toString());
                                Log.e("DEX", sb9.toString());
                                MyService.this.nErrorCode = -1;
                                MyService.this.strErrorMsg = "Exception in Process RECV";
                                this.f14ce.connectionEvent();
                            }
                        }
                    } else {
                        MyService.this.nErrorCode = 0;
                        MyService.this.strErrorMsg = "Initialization completed.";
                        this.f14ce.connectionEvent();
                    }
                } catch (Exception e4) {
                    StringBuilder sb10 = new StringBuilder();
                    sb10.append("Exception in SetConfigurationOptions(): ");
                    sb10.append(e4.toString());
                    Log.e("DEX", sb10.toString());
                    MyService.this.nErrorCode = -1;
                    MyService.this.strErrorMsg = "Exception in SetConfigurationOptions()";
                    this.f14ce.connectionEvent();
                }
            }
        }
    }

    private class DEXThread extends Thread {
        public volatile boolean bHasStarted;
        private volatile boolean bIsRunning;
        private volatile int nLastOp;
        private volatile int nOp;

        private DEXThread() {
            this.bHasStarted = false;
            this.bIsRunning = true;
            this.nOp = -1;
            this.nLastOp = -1;
        }

        public void run() {
            while (this.bIsRunning) {
                try {
                    if (this.bHasStarted && MyService.this.objDex != null) {
                        this.nOp = MyService.this.objDex.GetNextBLEOperation();
                    }
                    switch (this.nOp) {
                        case 1:
                            Log.d("DEX", "DEXInitialization operation");
                            break;
                        case 2:
                            Log.d("DEX", "DEXUninitialization operation");
                            break;
                        case 3:
                            byte[] SendDataToBLEFromDEX = MyService.this.objDex.SendDataToBLEFromDEX();
                            if (MyService.this.mConnectedGatt == null) {
                                Log.d("DEX", "mConnectedGatt==null!");
                                break;
                            } else {
                                if (MyService.this.charac == null) {
                                    int i = 0;
                                    while (true) {
                                        if (i < 4) {
                                            if (MyService.this.mConnectedGatt.getService(MyService.DEX_SERVICE_SPP) != null) {
                                                MyService.this.charac = MyService.this.mConnectedGatt.getService(MyService.DEX_SERVICE_SPP).getCharacteristic(MyService.DEX_CHARACTERISTIC_DATA);
                                            } else {
                                                i++;
                                                MyService.this.SleepThreadForTime(5000);
                                                Log.d("DEX", "Error in BluetoothGattCharacteristic retry");
                                            }
                                        }
                                    }
                                    Log.d("DEX", "Error in BluetoothGattCharacteristic");
                                }
                                if (MyService.this.charac != null) {
                                    synchronized (MyService.this.mConnectedGatt) {
                                        try {
                                            MyService.this.charac.setValue(SendDataToBLEFromDEX);
                                            if (!MyService.this.mConnectedGatt.writeCharacteristic(MyService.this.charac)) {
                                                Log.d("DEX", "++ send data failed!");
                                            } else {
                                                String GetAsciiCode = MyService.this.GetAsciiCode(SendDataToBLEFromDEX[0], 1);
                                                if (!GetAsciiCode.isEmpty()) {
                                                    StringBuilder sb = new StringBuilder();
                                                    sb.append("DSD->SF:");
                                                    sb.append(GetAsciiCode);
                                                    Log.d("DEX", sb.toString());
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        case 4:
                            synchronized (MyService.this.DataReadOperationSync) {
                                MyService.this.objDex.ReceiveDataFromBLE();
                            }
                            break;
                        default:
                            this.bHasStarted = true;
                            break;
                    }
                    if (this.bHasStarted && MyService.this.objDex != null) {
                        MyService.this.SleepThreadForTime(5);
                        MyService.this.objDex.FinishCurrentBLEOperation();
                    }
                } catch (Exception e2) {
                    Log.d("DEX", String.format("Last operation: %d", new Object[]{Integer.valueOf(this.nOp)}));
                    Log.d("DEX", String.format("DEXThread Exception caught: %s", new Object[]{e2.getMessage()}));
                    Log.d("DEX", String.format("Stack Trace: %s", new Object[]{e2.toString()}));
                    return;
                }
            }
        }
    }

    private static class IncomingHandler extends Handler {
        public void handleMessage(Message message) {
        }

        private IncomingHandler() {
        }
    }

    private class RemoteServiceConnection implements ServiceConnection {
        private RemoteServiceConnection() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("DEX", "onServiceConnected()");
            MyService.this.messenger = new Messenger(iBinder);
            MyService.this.callMe.f13cn.bConnected = true;
            MyService.this.callMe.f13cn.doWork();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            MyService.this.messenger = null;
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    /* access modifiers changed from: private */
    public long WaitForBLEConnection() {
        Log.i("DEX", "WaitForBLEConnection() - scanBLEDevice start");
        long currentTimeMillis = System.currentTimeMillis();
        scanBLEDevice(true);
        Log.i("DEX", "WaitForBLEConnection()");
        while (true) {
            int currentTimeMillis2 = (int) ((System.currentTimeMillis() - currentTimeMillis) / 1000);
            if (!this.mConnected) {
                if (currentTimeMillis2 > 10) {
                    Log.i("DEX", "WaitForBLEConnection - No Connection made!");
                    break;
                }
            } else {
                Log.i("DEX", "WaitForBLEConnection - Connection made!");
                break;
            }
        }
        scanBLEDevice(false);
        Log.i("DEX", "WaitForBLEConnection() - scanBLEDevice end");
        if (this.mConnected) {
            this.strErrorMsg = "BLE Adapter Connected";
            return 0;
        }
        this.strErrorMsg = "BLE Adapter NOT Connected";
        return -1;
    }

    /* access modifiers changed from: private */
    public String getIgnoreCase(JSONObject jSONObject, String str) {
        Iterator keys = jSONObject.keys();
        while (keys.hasNext()) {
            String str2 = (String) keys.next();
            if (str2.equalsIgnoreCase(str)) {
                return str2;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public long SetConfigurationOptions(JSONObject jSONObject) {
        long j;
        try {
            String ignoreCase = getIgnoreCase(jSONObject, "transmissioncontrolnumber");
            if (ignoreCase != null) {
                Log.i("DEX", String.format("Transmission Control Number: %d", new Object[]{Long.valueOf(jSONObject.getLong(ignoreCase))}));
                j = this.objDex.SetConfigTransmissionControlNumber(jSONObject.getLong(ignoreCase));
            } else {
                j = 0;
            }
            if (j == 0) {
                String ignoreCase2 = getIgnoreCase(jSONObject, "transactionsetcontrolnumber");
                if (ignoreCase2 != null) {
                    Log.i("DEX", String.format("Transaction Control Number: %d", new Object[]{Long.valueOf(jSONObject.getLong(ignoreCase2))}));
                    j = this.objDex.SetConfigTransactionSetControlNumber(jSONObject.getLong(ignoreCase2));
                }
            }
            if (j == 0) {
                String ignoreCase3 = getIgnoreCase(jSONObject, "testindicator");
                if (ignoreCase3 != null) {
                    Log.i("DEX", String.format("TestIndicator: %s", new Object[]{jSONObject.getString(ignoreCase3)}));
                    j = this.objDex.SetConfigTestIndicator(jSONObject.getString(ignoreCase3));
                }
            }
            if (j == 0) {
                String ignoreCase4 = getIgnoreCase(jSONObject, "supplier");
                if (ignoreCase4 != null) {
                    Log.i("DEX", "Call SetSupplier()");
                    j = SetSupplierValues(jSONObject.getJSONObject(ignoreCase4));
                }
            }
            if (j != 0) {
                return j;
            }
            String ignoreCase5 = getIgnoreCase(jSONObject, "retailer");
            if (ignoreCase5 == null) {
                return j;
            }
            Log.i("DEX", "Call SetRetailer()");
            return SetRetailerValues(jSONObject.getJSONObject(ignoreCase5));
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in SetConfigurationOptions(): ");
            sb.append(e.toString());
            Log.e("DEX", sb.toString());
            return -1;
        }
    }

    private long SetSupplierValues(JSONObject jSONObject) {
        String str = "";
        String str2 = "";
        String str3 = "";
        long j = 0;
        try {
            String ignoreCase = getIgnoreCase(jSONObject, "communicationsid");
            if (ignoreCase != null) {
                str = jSONObject.getString(ignoreCase);
            }
            String str4 = str;
            String ignoreCase2 = getIgnoreCase(jSONObject, "dunsnumber");
            if (ignoreCase2 != null) {
                str2 = jSONObject.getString(ignoreCase2);
            }
            String str5 = str2;
            String ignoreCase3 = getIgnoreCase(jSONObject, "location");
            if (ignoreCase3 != null) {
                str3 = jSONObject.getString(ignoreCase3);
            }
            String str6 = str3;
            String ignoreCase4 = getIgnoreCase(jSONObject, "signaturekey");
            if (ignoreCase4 != null) {
                j = jSONObject.getLong(ignoreCase4);
            }
            long j2 = j;
            Log.i("DEX", String.format("SetSupplier(%s, %s, %s, %d)", new Object[]{str4, str5, str6, Long.valueOf(j2)}));
            return this.objDex.SetSupplier(str4, str5, str6, j2);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in SetSupplierValues(): ");
            sb.append(e.toString());
            Log.e("DEX", sb.toString());
            return -1;
        }
    }

    private long SetRetailerValues(JSONObject jSONObject) {
        String str = "";
        String str2 = "";
        String str3 = "";
        String str4 = "";
        try {
            String ignoreCase = getIgnoreCase(jSONObject, "communicationsid");
            if (ignoreCase != null) {
                str = jSONObject.getString(ignoreCase);
            }
            String ignoreCase2 = getIgnoreCase(jSONObject, "dunsnumber");
            if (ignoreCase2 != null) {
                str2 = jSONObject.getString(ignoreCase2);
            }
            String ignoreCase3 = getIgnoreCase(jSONObject, "location");
            if (ignoreCase3 != null) {
                str3 = jSONObject.getString(ignoreCase3);
            }
            String ignoreCase4 = getIgnoreCase(jSONObject, "dexversion");
            if (ignoreCase4 != null) {
                str4 = jSONObject.getString(ignoreCase4);
            }
            Log.i("DEX", String.format("SetRetailer(%s, %s, %s, %s)", new Object[]{str, str2, str3, str4}));
            return this.objDex.SetRetailer(str, str2, str3, str4);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in LogRetailerValues(): ");
            sb.append(e.toString());
            Log.e("DEX", sb.toString());
            return -1;
        }
    }

    private void SetUpBLEInterface(JSONObject jSONObject) {
        Log.i("DEX", "SetBLEInterface()");
        try {
            if (getIgnoreCase(jSONObject, "inifile") != null) {
                String FindDexDirectory = FindDexDirectory();
                if (FindDexDirectory != null && FindDexDirectory.length() > 0 && DirectoryExists(FindDexDirectory)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.dexDir);
                    sb.append(File.separator);
                    sb.append("config.ini");
                    this.config = sb.toString();
                    Log.i("DEX", String.format("inifile: %s", new Object[]{this.config}));
                }
            }
            String ignoreCase = getIgnoreCase(jSONObject, "instancename");
            if (ignoreCase != null) {
                this.instanceName = jSONObject.getString(ignoreCase);
                Log.i("DEX", String.format("instanceName: %s", new Object[]{this.instanceName}));
            }
            String ignoreCase2 = getIgnoreCase(jSONObject, "commmethod");
            if (ignoreCase2 == null) {
                return;
            }
            if (jSONObject.getString(ignoreCase2).equalsIgnoreCase("rs232")) {
                this.commMethod = CommunicationMethod.RS232;
                String ignoreCase3 = getIgnoreCase(jSONObject, "port");
                if (ignoreCase3 != null) {
                    this.portName = jSONObject.getString(ignoreCase3);
                    Log.i("DEX", String.format("init: port: %s", new Object[]{this.portName}));
                }
            } else if (jSONObject.getString(ignoreCase2).equalsIgnoreCase("btle")) {
                this.commMethod = CommunicationMethod.BTLE;
                Log.i("DEX", "init: commMethod: CommunicationMethod.BTLE");
                this.portName = "";
                Log.i("DEX", String.format("init: portName: %s", new Object[]{this.portName}));
            }
        } catch (Exception e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Exception in SetUpBLEInterface(): ");
            sb2.append(e.toString());
            Log.e("DEX", sb2.toString());
        }
    }

    /* access modifiers changed from: private */
    public long WriteTransaction(JSONObject jSONObject) {
        long j;
        long j2;
        long j3;
        Log.i("DEX", "WriteInvoices() - start");
        synchronized (this) {
            j = -1;
            try {
                String ignoreCase = getIgnoreCase(jSONObject, "dxs");
                if (ignoreCase != null) {
                    Log.i("DEX", "WriteDXSSegment()");
                    JSONObject jSONObject2 = jSONObject.getJSONObject(ignoreCase);
                    if (!jSONObject2.has("01") || jSONObject2.getString("01").isEmpty()) {
                        Log.i("DEX", "BeginTransaction() - no elements");
                        j2 = this.objDex.BeginTransactionSet();
                    } else {
                        j2 = this.objDex.WriteDXSSegment(jSONObject2.getString("01"), jSONObject2.getString("02"), jSONObject2.getString("03"), (!jSONObject2.has("04") || jSONObject2.getString("04").isEmpty()) ? 0 : jSONObject2.getLong("04"), (!jSONObject2.has("05") || jSONObject2.getString("05").isEmpty()) ? null : jSONObject2.getString("05"), (!jSONObject2.has("06") || jSONObject2.getString("06").isEmpty()) ? null : jSONObject2.getString("06"));
                    }
                } else {
                    Log.i("DEX", "BeginTransaction() - no DXS");
                    j2 = this.objDex.BeginTransactionSet();
                }
                if (j2 != 0) {
                    Log.d("DEX", String.format("BeginTransaction() error: %d", new Object[]{Long.valueOf(j2)}));
                    return j2;
                }
                String ignoreCase2 = getIgnoreCase(jSONObject, "invoices");
                if (ignoreCase2 != null) {
                    JSONObject jSONObject3 = jSONObject.getJSONObject(ignoreCase2);
                    Iterator keys = jSONObject3.keys();
                    while (keys.hasNext()) {
                        String str = (String) keys.next();
                        Log.i("DEX", String.format("WriteAnInvoice: %s: ", new Object[]{str}));
                        j2 = WriteAnInvoice(jSONObject3.getJSONObject(str));
                        if (j2 != 0) {
                            break;
                        }
                    }
                    if (j2 != 0) {
                        Log.d("DEX", String.format("WriteAnInvoice() error: %d", new Object[]{Long.valueOf(j2)}));
                        return j2;
                    }
                }
                String ignoreCase3 = getIgnoreCase(jSONObject, "dxe");
                if (ignoreCase3 != null) {
                    Log.i("DEX", "WriteDXESegment()");
                    JSONObject jSONObject4 = jSONObject.getJSONObject(ignoreCase3);
                    if (!jSONObject4.has("01") || jSONObject4.getString("01").isEmpty()) {
                        Log.i("DEX", "EndTransactionSet() - no elements");
                        j3 = this.objDex.EndTransactionSet();
                    } else {
                        j3 = this.objDex.WriteDXESegment(jSONObject4.getLong("01"));
                    }
                } else {
                    Log.i("DEX", "EndTransactionSet() - no DXE");
                    j3 = this.objDex.EndTransactionSet();
                }
                if (j3 != 0) {
                    Log.d("DEX", String.format("WriteDEXSegment()/EndTransactionSet() error: %d", new Object[]{Long.valueOf(j3)}));
                }
                j = j3;
            } catch (Exception e) {
                this.nErrorCode = -1;
                StringBuilder sb = new StringBuilder();
                sb.append("Exception in WriteInvoices(): ");
                sb.append(e.toString());
                this.strErrorMsg = sb.toString();
                Log.e("DEX", this.strErrorMsg);
            }
        }
        return j;
    }

    private long WriteAnInvoice(JSONObject jSONObject) {
        long j;
        long j2;
        JSONObject jSONObject2 = jSONObject;
        String str = "";
        try {
            String ignoreCase = getIgnoreCase(jSONObject2, "st");
            if (ignoreCase != null) {
                JSONObject jSONObject3 = jSONObject2.getJSONObject(ignoreCase);
                Log.i("DEX", "WriteSTSegment()");
                long WriteSTSegment = this.objDex.WriteSTSegment(jSONObject3.getString("01"), jSONObject3.has("02") ? jSONObject3.getLong("02") : 0);
                if (WriteSTSegment != 0) {
                    Log.d("DEX", String.format("WriteSTSegment() error: %d", new Object[]{Long.valueOf(WriteSTSegment)}));
                    return WriteSTSegment;
                }
                str = jSONObject3.getString("01");
            }
            String ignoreCase2 = getIgnoreCase(jSONObject2, "g82");
            if (ignoreCase2 != null) {
                JSONObject jSONObject4 = jSONObject2.getJSONObject(ignoreCase2);
                Log.i("DEX", "WriteG82Segment()");
                long WriteG82Segment = this.objDex.WriteG82Segment(jSONObject4.getString("01"), jSONObject4.getString("02"), (!jSONObject4.has("03") || jSONObject4.getString("03").isEmpty()) ? "" : jSONObject4.getString("03"), (!jSONObject4.has("04") || jSONObject4.getString("04").isEmpty()) ? "" : jSONObject4.getString("04"), (!jSONObject4.has("05") || jSONObject4.getString("05").isEmpty()) ? "" : jSONObject4.getString("05"), (!jSONObject4.has("06") || jSONObject4.getString("06").isEmpty()) ? "" : jSONObject4.getString("06"), jSONObject4.getString("07"), (!jSONObject4.has("08") || jSONObject4.getString("08").isEmpty()) ? "" : jSONObject4.getString("08"), (!jSONObject4.has("09") || jSONObject4.getString("09").isEmpty()) ? "" : jSONObject4.getString("09"), (!jSONObject4.has("10") || jSONObject4.getString("10").isEmpty()) ? "" : jSONObject4.getString("10"), (!jSONObject4.has("11") || jSONObject4.getString("11").isEmpty()) ? "" : jSONObject4.getString("11"), (!jSONObject4.has("12") || jSONObject4.getString("12").isEmpty()) ? "" : jSONObject4.getString("12"));
                if (WriteG82Segment != 0) {
                    Log.d("DEX", String.format("WriteG82Segment() error: %d", new Object[]{Long.valueOf(WriteG82Segment)}));
                    return WriteG82Segment;
                }
            }
            String ignoreCase3 = getIgnoreCase(jSONObject2, "g87");
            if (ignoreCase3 != null) {
                JSONObject jSONObject5 = jSONObject2.getJSONObject(ignoreCase3);
                Log.i("DEX", "WriteG87Segment()");
                long WriteG87Segment = this.objDex.WriteG87Segment(jSONObject5.getString("01"), jSONObject5.getString("02"), jSONObject5.getString("03"), jSONObject5.getString("04"), jSONObject5.getInt("05"), (!jSONObject5.has("06") || jSONObject5.getString("06").isEmpty()) ? "" : jSONObject5.getString("06"));
                if (WriteG87Segment != 0) {
                    Log.d("DEX", String.format("WriteG87Segment() error: %d", new Object[]{Long.valueOf(WriteG87Segment)}));
                    return WriteG87Segment;
                }
            }
            String ignoreCase4 = getIgnoreCase(jSONObject2, "n9");
            if (ignoreCase4 != null) {
                JSONObject jSONObject6 = jSONObject2.getJSONObject(ignoreCase4);
                Log.i("DEX", "WriteN9Segment()");
                long WriteN9Segment = this.objDex.WriteN9Segment(jSONObject6.getString("01"), (!jSONObject6.has("02") || jSONObject6.getString("02").isEmpty()) ? "" : jSONObject6.getString("02"), (!jSONObject6.has("03") || jSONObject6.getString("03").isEmpty()) ? "" : jSONObject6.getString("03"), (!jSONObject6.has("04") || jSONObject6.getString("04").isEmpty()) ? "" : jSONObject6.getString("04"), (!jSONObject6.has("05") || jSONObject6.getString("05").isEmpty()) ? "" : jSONObject6.getString("05"), (!jSONObject6.has("06") || jSONObject6.getString("06").isEmpty()) ? "" : jSONObject6.getString("06"));
                if (WriteN9Segment != 0) {
                    Log.d("DEX", String.format("WriteN9Segment() error: %d", new Object[]{Long.valueOf(WriteN9Segment)}));
                    return WriteN9Segment;
                }
            }
            String ignoreCase5 = getIgnoreCase(jSONObject2, "mtx");
            if (ignoreCase5 != null) {
                Log.i("DEX", "WriteInvoiceMTX()");
                long WriteMessageText = WriteMessageText(jSONObject2.getJSONObject(ignoreCase5));
                if (WriteMessageText != 0) {
                    Log.d("DEX", String.format("WriteInvoiceMTX() error: %d", new Object[]{Long.valueOf(WriteMessageText)}));
                    return WriteMessageText;
                }
            }
            String ignoreCase6 = getIgnoreCase(jSONObject2, "items");
            if (ignoreCase6 != null) {
                Log.i("DEX", "WriteItemDetails()");
                long WriteItemDetails = WriteItemDetails(jSONObject2.getJSONObject(ignoreCase6), str);
                if (WriteItemDetails != 0) {
                    Log.d("DEX", String.format("WriteItemDetails() error: %d", new Object[]{Long.valueOf(WriteItemDetails)}));
                    return WriteItemDetails;
                }
            }
            String ignoreCase7 = getIgnoreCase(jSONObject2, "adjustments");
            if (ignoreCase7 != null) {
                Log.i("DEX", "WriteAdjustments() - Invoice Level");
                long WriteAdjustments = WriteAdjustments(jSONObject2.getJSONObject(ignoreCase7), str);
                if (WriteAdjustments != 0) {
                    Log.d("DEX", String.format("WriteAdjustments() - Invoice Level: %d", new Object[]{Long.valueOf(WriteAdjustments)}));
                    return WriteAdjustments;
                }
            }
            String ignoreCase8 = getIgnoreCase(jSONObject2, "g84");
            if (ignoreCase8 != null) {
                Log.i("DEX", "WriteG84Segment():");
                JSONObject jSONObject7 = jSONObject2.getJSONObject(ignoreCase8);
                long WriteG84Segment = this.objDex.WriteG84Segment(jSONObject7.getDouble("01"), jSONObject7.has("02") ? jSONObject7.getDouble("02") : 0.0d, jSONObject7.has("03") ? jSONObject7.getDouble("03") : 0.0d);
                if (WriteG84Segment != 0) {
                    Log.d("DEX", String.format("WriteG84Segment(): %d", new Object[]{Long.valueOf(WriteG84Segment)}));
                    return WriteG84Segment;
                }
            }
            String ignoreCase9 = getIgnoreCase(jSONObject2, "g86");
            long j3 = 1;
            if (ignoreCase9 != null) {
                Log.i("DEX", "WriteG86Segment()");
                JSONObject jSONObject8 = jSONObject2.getJSONObject(ignoreCase9);
                IDEX idex = this.objDex;
                if (jSONObject8.has("01") && !jSONObject8.getString("01").isEmpty()) {
                    j3 = jSONObject8.getLong("01");
                }
                j2 = idex.WriteG86Segment(j3, (!jSONObject8.has("02") || jSONObject8.getString("02").isEmpty()) ? "" : jSONObject8.getString("02"));
            } else {
                Log.i("DEX", "WriteG86Segment - empty");
                if (this.objDex == null) {
                    Log.d("DEX", "objDex is null - 28");
                }
                j2 = this.objDex.WriteG86Segment(1, "");
            }
            if (j2 != 0) {
                Log.d("DEX", String.format("WriteG86Segment(): %d", new Object[]{Long.valueOf(j2)}));
                return j2;
            }
            Log.i("DEX", "WriteG85Segment()");
            if (this.objDex == null) {
                Log.d("DEX", "objDex is null - 29");
            }
            long WriteG85Segment = this.objDex.WriteG85Segment();
            if (WriteG85Segment != 0) {
                Log.d("DEX", String.format("WriteG85Segment(): %d", new Object[]{Long.valueOf(WriteG85Segment)}));
                return WriteG85Segment;
            }
            String ignoreCase10 = getIgnoreCase(jSONObject2, "se");
            if (ignoreCase10 != null) {
                Log.i("DEX", "WriteSESegment()");
                JSONObject jSONObject9 = jSONObject2.getJSONObject(ignoreCase10);
                j = this.objDex.WriteSESegment(jSONObject9.has("01") ? jSONObject9.getLong("01") : 0);
            } else {
                Log.i("DEX", "WriteSESegment - empty");
                j = this.objDex.WriteSESegment(0);
            }
            if (j != 0) {
                Log.d("DEX", String.format("WriteSESegment(): %d", new Object[]{Long.valueOf(j)}));
            }
            return j;
        } catch (Exception e) {
            j = -1;
            this.nErrorCode = -1;
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in WriteInvoices(): ");
            sb.append(e.toString());
            this.strErrorMsg = sb.toString();
            Log.e("DEX", this.strErrorMsg);
        }
    }

    private long WriteMessageText(JSONObject jSONObject) {
        try {
            Iterator keys = jSONObject.keys();
            ArrayList arrayList = new ArrayList();
            while (keys.hasNext()) {
                arrayList.add(Integer.valueOf(Integer.parseInt((String) keys.next())));
            }
            Collections.sort(arrayList);
            if (arrayList.size() <= 0) {
                return 0;
            }
            long j = 0;
            for (int i = 0; i < arrayList.size(); i++) {
                Iterator keys2 = jSONObject.keys();
                while (true) {
                    if (!keys2.hasNext()) {
                        break;
                    }
                    String format = String.format(Locale.US, "%d", new Object[]{Integer.valueOf(((Integer) arrayList.get(i)).intValue())});
                    JSONObject jSONObject2 = jSONObject.getJSONObject(format);
                    Log.i("DEX", String.format("WriteMessageText() - key: %s", new Object[]{format}));
                    j = this.objDex.WriteMTXSegment((!jSONObject2.has("01") || jSONObject2.getString("01").isEmpty()) ? "" : jSONObject2.getString("01"), (!jSONObject2.has("02") || jSONObject2.getString("02").isEmpty()) ? "" : jSONObject2.getString("02"), (!jSONObject2.has("03") || jSONObject2.getString("03").isEmpty()) ? "" : jSONObject2.getString("03"));
                    if (j != 0) {
                        Log.d("DEX", String.format("WriteMessageText(): %d", new Object[]{Long.valueOf(j)}));
                        break;
                    }
                }
            }
            return j;
        } catch (Exception e) {
            this.nErrorCode = -1;
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in WriteMessageText(): ");
            sb.append(e.toString());
            this.strErrorMsg = sb.toString();
            Log.e("DEX", this.strErrorMsg);
            return -1;
        }
    }

    private long WriteItemDetails(JSONObject jSONObject, String str) {
        long j;
        long j2;
        JSONObject jSONObject2;
        String str2 = str;
        try {
            Iterator keys = jSONObject.keys();
            ArrayList arrayList = new ArrayList();
            while (keys.hasNext()) {
                arrayList.add(Integer.valueOf(Integer.parseInt((String) keys.next())));
            }
            Collections.sort(arrayList);
            j = 0;
            if (arrayList.size() > 0) {
                Log.i("DEX", "WriteLSSegment()");
                long WriteLoopStart = this.objDex.WriteLoopStart();
                if (WriteLoopStart != 0) {
                    Log.d("DEX", String.format("WriteLSSegment() error: %d", new Object[]{Long.valueOf(WriteLoopStart)}));
                    return WriteLoopStart;
                }
                for (int i = 0; i < arrayList.size(); i++) {
                    int intValue = ((Integer) arrayList.get(i)).intValue();
                    String format = String.format(Locale.US, "%d", new Object[]{Integer.valueOf(intValue)});
                    JSONObject jSONObject3 = jSONObject.getJSONObject(format);
                    if (str2.equalsIgnoreCase("894")) {
                        Log.i("DEX", String.format("WriteG83Segment - SellByEach() - Sequence Number: %s", new Object[]{format}));
                        jSONObject2 = jSONObject3;
                        j2 = this.objDex.SellByEach(intValue, jSONObject3.getDouble("02"), (!jSONObject3.has("04") || jSONObject3.getString("04").isEmpty()) ? "" : jSONObject3.getString("04"), (!jSONObject3.has("08") || jSONObject3.getString("08").isEmpty()) ? 0.0d : jSONObject3.getDouble("08"), (!jSONObject3.has("10") || jSONObject3.getString("10").isEmpty()) ? "" : jSONObject3.getString("10"), (!jSONObject3.has("06") || jSONObject3.getString("06").isEmpty()) ? "" : jSONObject3.getString("06"));
                    } else {
                        jSONObject2 = jSONObject3;
                        if (jSONObject2.length() <= 3) {
                            Log.i("DEX", String.format("WriteG89Segment - AdjustItem() - Sequence Number: %s", new Object[]{format}));
                            j2 = this.objDex.AdjustItem(intValue, (!jSONObject2.has("02") || jSONObject2.getString("02").isEmpty()) ? -1.0d : jSONObject2.getDouble("02"), (!jSONObject2.has("08") || jSONObject2.getString("08").isEmpty()) ? -1.0d : jSONObject2.getDouble("08"));
                        } else {
                            Log.i("DEX", String.format("WriteG89Segment() - Sequence Number: %s", new Object[]{format}));
                            j2 = this.objDex.WriteG89Segment(intValue, (!jSONObject2.has("02") || jSONObject2.getString("02").isEmpty()) ? -1.0d : jSONObject2.getDouble("02"), (!jSONObject2.has("03") || jSONObject2.getString("03").isEmpty()) ? "" : jSONObject2.getString("03"), (!jSONObject2.has("04") || jSONObject2.getString("04").isEmpty()) ? "" : jSONObject2.getString("04"), (!jSONObject2.has("05") || jSONObject2.getString("05").isEmpty()) ? "" : jSONObject2.getString("05"), (!jSONObject2.has("06") || jSONObject2.getString("06").isEmpty()) ? "" : jSONObject2.getString("06"), (!jSONObject2.has("07") || jSONObject2.getString("07").isEmpty()) ? "" : jSONObject2.getString("07"), (!jSONObject2.has("08") || jSONObject2.getString("08").isEmpty()) ? -1.0d : jSONObject2.getDouble("08"), (!jSONObject2.has("09") || jSONObject2.getString("09").isEmpty()) ? 0 : jSONObject2.getInt("09"), (!jSONObject2.has("10") || jSONObject2.getString("10").isEmpty()) ? 0 : jSONObject2.getInt("10"), (!jSONObject2.has("11") || jSONObject2.getString("11").isEmpty()) ? "" : jSONObject2.getString("11"), (!jSONObject2.has("12") || jSONObject2.getString("12").isEmpty()) ? "" : jSONObject2.getString("12"));
                        }
                    }
                    if (j2 != 0) {
                        Log.d("DEX", String.format("WriteG83Segment()/WriteG89Segment() error: %d", new Object[]{Long.valueOf(j2)}));
                        return j2;
                    }
                    String ignoreCase = getIgnoreCase(jSONObject2, "adjustments");
                    if (ignoreCase != null) {
                        Log.d("DEX", "WriteAdjustments() - Item Level");
                        long WriteAdjustments = WriteAdjustments(jSONObject2.getJSONObject(ignoreCase), str2);
                        if (WriteAdjustments != 0) {
                            Log.d("DEX", String.format("WriteAdjustments() - Item Level error: %d", new Object[]{Long.valueOf(WriteAdjustments)}));
                            return WriteAdjustments;
                        }
                    }
                }
                if (this.objDex == null) {
                    Log.d("DEX", "objDex is null - 35");
                }
                Log.d("DEX", "WriteLESegment()");
                j = this.objDex.WriteLoopEnd();
                if (j != 0) {
                    Log.i("DEX", String.format("WriteLESegment() error: %d", new Object[]{Long.valueOf(j)}));
                }
            }
        } catch (Exception e) {
            j = -1;
            this.nErrorCode = -1;
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in WriteItemDetails(): ");
            sb.append(e.toString());
            this.strErrorMsg = sb.toString();
            Log.e("DEX", this.strErrorMsg);
        }
        return j;
    }

    private long WriteAdjustments(JSONObject jSONObject, String str) {
        long j;
        try {
            Iterator keys = jSONObject.keys();
            j = 0;
            boolean z = false;
            while (keys.hasNext()) {
                JSONObject jSONObject2 = jSONObject.getJSONObject((String) keys.next());
                if (str.equalsIgnoreCase("895") && !z) {
                    Log.i("DEX", "WriteG72Segment() - REMOVE");
                    j = this.objDex.WriteG72Segment("96", "12", "REMOVE", "", 0.0d, 0.0d, "", 0.0d, 0.0d, 0.0d, "");
                    if (j != 0) {
                        Log.d("DEX", String.format("WriteG72Segment() - REMOVE - error: %d", new Object[]{Long.valueOf(j)}));
                        return j;
                    }
                    z = true;
                }
                double d = 0.0d;
                if (jSONObject2.has("05") && !jSONObject2.getString("05").isEmpty()) {
                    Log.i("DEX", "WriteG72Segment() - AllowanceOrChargeByRate");
                    IDEX idex = this.objDex;
                    String string = jSONObject2.getString("01");
                    String string2 = jSONObject2.getString("02");
                    String string3 = (!jSONObject2.has("03") || jSONObject2.getString("03").isEmpty()) ? "" : jSONObject2.getString("03");
                    String string4 = (!jSONObject2.has("04") || jSONObject2.getString("04").isEmpty()) ? "" : jSONObject2.getString("04");
                    double d2 = (!jSONObject2.has("05") || jSONObject2.getString("05").isEmpty()) ? 0.0d : jSONObject2.getDouble("05");
                    if (jSONObject2.has("06") && !jSONObject2.getString("06").isEmpty()) {
                        d = jSONObject2.getDouble("06");
                    }
                    j = idex.AllowanceOrChargeByRate(string, string2, string3, string4, d2, d, (!jSONObject2.has("07") || jSONObject2.getString("07").isEmpty()) ? "" : jSONObject2.getString("07"), (!jSONObject2.has("11") || jSONObject2.getString("11").isEmpty()) ? "" : jSONObject2.getString("11"));
                } else if (jSONObject2.has("08") && !jSONObject2.getString("08").isEmpty()) {
                    Log.i("DEX", "WriteG72Segment() - AllowanceOrChargeByTotal");
                    IDEX idex2 = this.objDex;
                    String string5 = jSONObject2.getString("01");
                    String string6 = jSONObject2.getString("02");
                    String string7 = (!jSONObject2.has("03") || jSONObject2.getString("03").isEmpty()) ? "" : jSONObject2.getString("03");
                    String string8 = (!jSONObject2.has("04") || jSONObject2.getString("04").isEmpty()) ? "" : jSONObject2.getString("04");
                    if (jSONObject2.has("08") && !jSONObject2.getString("08").isEmpty()) {
                        d = jSONObject2.getDouble("08");
                    }
                    j = idex2.AllowanceOrChargeByTotal(string5, string6, string7, string8, d, (!jSONObject2.has("11") || jSONObject2.getString("11").isEmpty()) ? "" : jSONObject2.getString("11"));
                } else if (jSONObject2.has("09") && !jSONObject2.getString("09").isEmpty() && jSONObject2.has("10") && !jSONObject2.getString("10").isEmpty()) {
                    Log.i("DEX", "WriteG72Segment() - AllowanceOrChargeByPercent");
                    IDEX idex3 = this.objDex;
                    String string9 = jSONObject2.getString("01");
                    String string10 = jSONObject2.getString("02");
                    String string11 = (!jSONObject2.has("03") || jSONObject2.getString("03").isEmpty()) ? "" : jSONObject2.getString("03");
                    String string12 = (!jSONObject2.has("04") || jSONObject2.getString("04").isEmpty()) ? "" : jSONObject2.getString("04");
                    double d3 = (!jSONObject2.has("09") || jSONObject2.getString("09").isEmpty()) ? 0.0d : jSONObject2.getDouble("09");
                    if (jSONObject2.has("10") && !jSONObject2.getString("10").isEmpty()) {
                        d = jSONObject2.getDouble("10");
                    }
                    j = idex3.AllowanceOrChargeByPercent(string9, string10, string11, string12, d3, d, (!jSONObject2.has("11") || jSONObject2.getString("11").isEmpty()) ? "" : jSONObject2.getString("11"));
                }
                if (j != 0) {
                    Log.i("DEX", String.format("WriteG72Segment() - error: %d", new Object[]{Long.valueOf(j)}));
                }
            }
        } catch (Exception e) {
            j = -1;
            this.nErrorCode = -1;
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in WriteAdjustments(): ");
            sb.append(e.toString());
            this.strErrorMsg = sb.toString();
            Log.e("DEX", this.strErrorMsg);
        }
        return j;
    }

    public void onCreate() {
        super.onCreate();
        Log.i("DEX", "bDexInit = false");
        this.bDexInited = false;
        startForeground(1, new Builder(this, App.CHANNEL_ID).setContentTitle("Honeywell DEX Service").setContentText("Running...").setSmallIcon(C0200R.C0201drawable.ic_android).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0)).build());
        Log.i("DEX", "MyService.onCreate()");
        this.mBluetoothAdapter = null;
        this.mContext = this;
        Log.i("DEX", "RemoteServiceConnection");
        this.connection = new RemoteServiceConnection();
        Log.i("DEX", "CallMe");
        this.callMe = new CallMe();
        Log.i("DEX", "Messenger");
        this.replyTo = new Messenger(new IncomingHandler());
        Log.d("DEX", "MyService:Perform FileCheck()");
        FileCheck();
        Log.d("DEX", "MyService:FileCheck() Complete - wait 5");
        try {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            if (bluetoothManager != null) {
                this.mBluetoothAdapter = bluetoothManager.getAdapter();
                if (this.mBluetoothAdapter == null) {
                    Log.d("DEX", "Initialize Bluetooth adapter failed!");
                    return;
                }
                IntentFilter intentFilter = new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED");
                intentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
                intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
                registerReceiver(this.mReceiver, intentFilter);
            }
        } catch (NullPointerException unused) {
            Log.e("DEX", "NullPointerException - BluetootManager.getAdapter()");
        }
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        Log.i("DEX", "MyService.onStartCommand()");
        this.objDex = new CDEX();
        Log.i("DEX", "Call setDexParams()");
        setDexParams(intent, i2);
        Log.i("DEX", "return START_NOT_STICKY");
        return 2;
    }

    /* access modifiers changed from: private */
    public void CheckResult(String str, long j) {
        Log.i("DEX", "Check Result()");
        if (j != 0) {
            Log.i("DEX", "Check Result() - isError");
            if (this.nErrorCode == 0) {
                this.nErrorCode = j;
                if (this.objDex != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(str);
                    sb.append(" error: ");
                    sb.append(this.objDex.GetErrorMessage());
                    this.strErrorMsg = sb.toString();
                } else {
                    Log.d("DEX", "objDex is null - 37");
                }
                Log.i("DEX", String.format("CheckResult - set error values:\nError: %d\nMessage: %s", new Object[]{Long.valueOf(this.nErrorCode), this.strErrorMsg}));
            }
            if (this.nErrorCode != 0 && this.callMe.f13cn.bConnected) {
                Log.i("DEX", "Call connectionEvent()");
                this.callMe.connectionEvent();
            }
        }
    }

    private void setDexParams(Intent intent, int i) {
        Log.i("DEX", "setDexParams()");
        String str = "DEX";
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("MyService: check intent.hasExtras('JSON') - ");
            sb.append(intent.hasExtra("JSON"));
            Log.d(str, sb.toString());
            if (intent.hasExtra("JSON")) {
                this.jsonString = intent.getStringExtra("JSON");
                if (this.jsonString != null && !this.jsonString.isEmpty()) {
                    this.jsonObject = new JSONObject(this.jsonString);
                    String ignoreCase = getIgnoreCase(this.jsonObject, "initialization");
                    if (ignoreCase != null) {
                        JSONObject jSONObject = this.jsonObject.getJSONObject(ignoreCase);
                        String ignoreCase2 = getIgnoreCase(jSONObject, "evtSrcId");
                        if (ignoreCase2 != null) {
                            this.evtSrcId = jSONObject.getInt(ignoreCase2);
                            Log.i("DEX", String.format("evtSrcId: %d", new Object[]{Integer.valueOf(this.evtSrcId)}));
                        }
                        String ignoreCase3 = getIgnoreCase(jSONObject, "synchType");
                        if (ignoreCase3 != null) {
                            this.synchType = jSONObject.getInt(ignoreCase3);
                            Log.i("DEX", String.format("synchType: %d", new Object[]{Integer.valueOf(this.synchType)}));
                        }
                        Log.i("DEX", "SEtUpBLEDevice");
                        SetUpBLEInterface(jSONObject);
                    }
                    String stringExtra = intent.getStringExtra(CALLER_APP_ID);
                    Log.i("DEX", String.format("callerPackageName: %s", new Object[]{stringExtra}));
                    Intent intent2 = new Intent("com.movilizer.client.android.EXT_EVENT");
                    Log.i("DEX", "set externalIntent");
                    intent2.setPackage(stringExtra);
                    Log.i("DEX", "externalIntent.SetPackage");
                    intent2.putExtra("evtSrcId", Integer.valueOf(this.evtSrcId));
                    Log.i("DEX", "externalIntent.putExtra");
                    Log.i("DEX", "call bindService");
                    bindService(intent2, this.connection, 1);
                    this.bDexInited = true;
                }
            }
        } catch (JSONException e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("JSONException in setDexParams(): ");
            sb2.append(e.toString());
            Log.e("DEX", sb2.toString());
        } catch (Exception e2) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Exception in setDexParams(): ");
            sb3.append(e2.toString());
            Log.e("DEX", sb3.toString());
        }
        Log.i("DEX", "end DexSetParms");
    }

    public void onDestroy() {
        Log.d("DEX", "releaseResources() - sleep 1");
        releaseResources();
    }

    private void releaseResources() {
        Log.d("DEX", "releaseResources()");
        try {
            Log.i("DEX", "Unregister BT BroadcastReceiver");
            if (this.mConnectedGatt != null) {
                Log.d("DEX", "Release mConnectedGatt");
                this.mConnectedGatt.disconnect();
                this.mConnectedGatt.close();
                this.mConnectedGatt = null;
            } else {
                Log.d("DEX", "mConnectedGatt is null");
            }
            if (this.objDex != null) {
                Log.d("DEX", "Release objDex");
                this.objDex.DexTerminate();
                this.objDex = null;
            } else {
                Log.d("DEX", "objDex is null - 38");
            }
            if (this.mBluetoothAdapter != null) {
                Log.d("DEX", "Release mBlueToothAdapter");
                this.mBluetoothAdapter = null;
            } else {
                Log.d("DEX", "mBlueToothAdapter is null");
            }
            if (this.mBluetoothDevice != null) {
                Log.d("DEX", "Release mBlueToothDevice");
                this.mBluetoothDevice = null;
            }
            if (this.callMe != null) {
                Log.d("DEX", "Release callMe");
                this.callMe = null;
            } else {
                Log.d("DEX", "callMe is null");
            }
            if (this.replyTo != null) {
                Log.d("DEX", "Release replyTo");
                this.replyTo = null;
            } else {
                Log.d("DEX", "replyTo is null");
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception: ");
            sb.append(e.toString());
            Log.e("DEX", sb.toString());
        }
        try {
            if (this.connection != null) {
                Log.e("DEX", "Unbinding the service - connection is not null");
                unbindService(this.connection);
            } else {
                Log.e("DEX", "connection is null");
            }
        } catch (IllegalArgumentException unused) {
        } catch (Exception e2) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Exception while unbinding the service: ");
            sb2.append(e2.toString());
            Log.e("DEX", sb2.toString());
        }
        Log.d("DEX", "super onDestroy");
        super.onDestroy();
        Log.d("DEX", "onDestroy Completed");
    }

    public void doSendMessage(long j, String str) {
        JSONObject jSONObject = new JSONObject();
        try {
            Log.i("DEX", String.format("doSendMEssage() result values:\nresult = %d\nmessage = %s", new Object[]{Long.valueOf(j), str}));
            jSONObject.put("result", j);
            jSONObject.put("message", str);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in doSendMessage(): ");
            sb.append(e.getMessage());
            Log.i("DEX", sb.toString());
        }
        Message obtain = Message.obtain();
        Log.i("DEX", String.format("doSendMEssage() values:\nevtSrcId = %d\nsynchType = %d\n%s", new Object[]{Integer.valueOf(this.evtSrcId), Integer.valueOf(this.synchType), jSONObject.toString()}));
        Bundle bundle = new Bundle();
        bundle.putString("JSON", jSONObject.toString());
        obtain.what = this.evtSrcId;
        obtain.arg1 = (int) System.currentTimeMillis();
        obtain.arg2 = this.synchType;
        obtain.replyTo = this.replyTo;
        obtain.setData(bundle);
        try {
            SleepThreadForTime(1500);
            Log.i("DEX", "SendMesssage()");
            this.messenger.send(obtain);
        } catch (RemoteException e2) {
            String str2 = "DEX";
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Exception in doSendMessage(): ");
            sb2.append(e2.getMessage());
            Log.i(str2, sb2.toString());
        } catch (Throwable th) {
            Log.i("DEX", "StopSelf()");
            stopSelf();
            throw th;
        }
        Log.i("DEX", "StopSelf()");
        stopSelf();
    }

    private void ProcessBTLEConnectionString(String str) {
        if (!str.isEmpty()) {
            String substring = str.substring(4, 16);
            StringBuilder sb = new StringBuilder();
            sb.append(substring.substring(10));
            sb.append(":");
            sb.append(substring.substring(8, 10));
            sb.append(":");
            sb.append(substring.substring(6, 8));
            sb.append(":");
            sb.append(substring.substring(4, 6));
            sb.append(":");
            sb.append(substring.substring(2, 4));
            sb.append(":");
            sb.append(substring.substring(0, 2));
            this.connectedHandleAddress = sb.toString();
            int lastIndexOf = str.lastIndexOf(36);
            if (lastIndexOf >= 0) {
                int indexOf = str.toLowerCase().indexOf("battery:");
                if (indexOf > 0) {
                    this.pin = str.substring(lastIndexOf + 1, indexOf);
                } else {
                    this.pin = str.substring(lastIndexOf + 1);
                }
            }
        } else {
            Log.i("DEX", "content is empty");
        }
    }

    /* access modifiers changed from: private */
    public void dealWithContent(String str, int i) {
        switch (i) {
            case 0:
                ProcessBTLEConnectionString(str);
                Log.i("DEX", String.format("MAC Address: %s, PIN: %s)", new Object[]{this.connectedHandleAddress, this.pin}));
                return;
            case 1:
                int indexOf = str.toLowerCase().indexOf("battery:");
                if (indexOf >= 0) {
                    int indexOf2 = str.toLowerCase().indexOf("rev:");
                    if (indexOf2 > 0) {
                        this.blebatteryinfo = str.substring(indexOf + "battery:".length(), indexOf2);
                    } else {
                        this.blebatteryinfo = str.substring(indexOf + "battery:".length());
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Battery: ");
                sb.append(this.blebatteryinfo);
                Log.i("DEX", sb.toString());
                return;
            case 2:
                int indexOf3 = str.toLowerCase().indexOf("rev:");
                if (indexOf3 >= 0) {
                    this.blerevisioninfo = str.substring(indexOf3 + "rev:".length());
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append("REV: ");
                sb2.append(this.blerevisioninfo);
                Log.i("DEX", sb2.toString());
                return;
            default:
                return;
        }
    }

    private void scanBLEDevice(boolean z) {
        if (z) {
            Log.i("DEX", ">>BLE scan start");
            if (!AlreadyPaired()) {
                this.mBluetoothAdapter.startLeScan(this.mBLEScanCallback);
                return;
            }
            return;
        }
        Log.i("DEX", ">>BLE stop scan");
        this.mBluetoothAdapter.stopLeScan(this.mBLEScanCallback);
    }

    /* access modifiers changed from: private */
    public boolean SetMACAddressFromPairedDevice() {
        boolean z;
        Log.d("DEX", "SetMACAddressFromPairedDevice() - start");
        Set<BluetoothDevice> bondedDevices = this.mBluetoothAdapter.getBondedDevices();
        int i = 0;
        for (BluetoothDevice name : bondedDevices) {
            Log.d("DEX", String.format("Device name: %s", new Object[]{name.getName()}));
            i++;
        }
        Log.d("DEX", String.format("Number of connected devices: %d", new Object[]{Integer.valueOf(i)}));
        if (i == 0) {
            return false;
        }
        Iterator it = bondedDevices.iterator();
        while (true) {
            if (!it.hasNext()) {
                z = false;
                break;
            }
            BluetoothDevice bluetoothDevice = (BluetoothDevice) it.next();
            String name2 = bluetoothDevice.getName();
            Log.d("DEX", String.format("Connected Device: %s", new Object[]{name2}));
            if (name2.equalsIgnoreCase("DEXAdapter")) {
                String address = bluetoothDevice.getAddress();
                Log.d("DEX", String.format("   device address: '%s'", new Object[]{address}));
                if (!address.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(address.substring(0, 2));
                    sb.append(":");
                    sb.append(address.substring(3, 5));
                    sb.append(":");
                    sb.append(address.substring(6, 8));
                    sb.append(":");
                    sb.append(address.substring(9, 11));
                    sb.append(":");
                    sb.append(address.substring(12, 14));
                    sb.append(":");
                    sb.append(address.substring(15));
                    this.connectedHandleAddress = sb.toString();
                    z = true;
                    break;
                }
            }
        }
        Log.d("DEX", String.format("MAC Address assigned: '%s'", new Object[]{this.connectedHandleAddress}));
        return z;
    }

    private boolean AlreadyPaired() {
        boolean z;
        Log.d("DEX", "AlreadyPaired() - start");
        Iterator it = this.mBluetoothAdapter.getBondedDevices().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            BluetoothDevice bluetoothDevice = (BluetoothDevice) it.next();
            String address = bluetoothDevice.getAddress();
            Log.d("DEX", String.format("Device Address: %s\nconnected Handle Address: %s", new Object[]{address, this.connectedHandleAddress}));
            if (this.connectedHandleAddress.equalsIgnoreCase(address)) {
                Log.d("DEX", "***** Device is already paired! *****");
                this.mBluetoothDevice = bluetoothDevice;
                this.mConnectedGatt = this.mBluetoothDevice.connectGatt(this.mContext, false, this.mGattCallback);
                break;
            }
        }
        if (this.mConnectedGatt != null) {
            Log.d("DEX", "Checking for BluetoothGattCharacteristic assignment.");
            int i = 0;
            while (true) {
                if (i >= 3) {
                    break;
                } else if (this.mConnected) {
                    z = true;
                    break;
                } else {
                    SleepThreadForTime(4000);
                    i++;
                }
            }
            Log.d("DEX", String.format("AlreadyPaired() - result: %s", new Object[]{Boolean.valueOf(z)}));
            return z;
        }
        z = false;
        Log.d("DEX", String.format("AlreadyPaired() - result: %s", new Object[]{Boolean.valueOf(z)}));
        return z;
    }

    /* access modifiers changed from: protected */
    public void SleepThreadForTime(int i) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.US);
        if (i >= 1000) {
            try {
                Log.d("DEX", String.format("Waiting %d milliseconds - %s", new Object[]{Integer.valueOf(i), simpleDateFormat.format(new Date())}));
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("SleepThreadForTime() Exception: ");
                sb.append(e.getMessage());
                Log.d("DEX", sb.toString());
                return;
            }
        }
        do {
        } while (System.currentTimeMillis() - System.currentTimeMillis() < ((long) i));
    }

    public String GetAsciiCode(int i, int i2) {
        if (i == 10) {
            return "LF ";
        }
        if (i == 13) {
            return "CR ";
        }
        if (i == 16) {
            return "DLE";
        }
        switch (i) {
            case 1:
                return "SOH";
            case 2:
                return "STX";
            case 3:
                return "ETX";
            case 4:
                return "EOT";
            case 5:
                return "ENQ";
            case 6:
                return "ACK";
            default:
                switch (i) {
                    case MotionEventCompat.AXIS_WHEEL /*21*/:
                        return "NAK";
                    case MotionEventCompat.AXIS_GAS /*22*/:
                        return "SYN";
                    case MotionEventCompat.AXIS_BRAKE /*23*/:
                        return "ETB";
                    default:
                        if (i > 31 && i < 127) {
                            return i2 == 0 ? "" : Character.toString((char) i);
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("???: ");
                        sb.append(i);
                        return sb.toString();
                }
        }
    }

    private void FileCheck() {
        try {
            Log.d("DEX", "FileCheck: FindDexDirectory");
            String FindDexDirectory = FindDexDirectory();
            if (FindDexDirectory != null && FindDexDirectory.length() > 0) {
                Log.d("DEX", String.format("Dex Directory Name: %s", new Object[]{FindDexDirectory}));
                if (!DirectoryExists(FindDexDirectory)) {
                    CopyAssetFiles(this.dexDir);
                } else {
                    CheckAssetFiles(this.dexDir);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0069  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean DirectoryExists(String r7) {
        /*
            r6 = this;
            java.lang.String r0 = "DEX"
            java.lang.String r1 = "Checking for Directory: %s"
            r2 = 1
            java.lang.Object[] r3 = new java.lang.Object[r2]
            r4 = 0
            r3[r4] = r7
            java.lang.String r1 = java.lang.String.format(r1, r3)
            android.util.Log.d(r0, r1)
            java.io.File r0 = new java.io.File
            r0.<init>(r7)
            boolean r7 = r0.exists()
            if (r7 == 0) goto L_0x0066
            java.lang.String r7 = "DEX"
            java.lang.String r1 = "%s exists"
            java.lang.Object[] r3 = new java.lang.Object[r2]
            r3[r4] = r0
            java.lang.String r1 = java.lang.String.format(r1, r3)
            android.util.Log.d(r7, r1)
            boolean r7 = r0.isDirectory()
            if (r7 == 0) goto L_0x0042
            java.lang.String r7 = "DEX"
            java.lang.String r1 = "%s is a directory."
            java.lang.Object[] r3 = new java.lang.Object[r2]
            r3[r4] = r0
            java.lang.String r1 = java.lang.String.format(r1, r3)
            android.util.Log.d(r7, r1)
            r7 = 1
            goto L_0x0067
        L_0x0042:
            java.lang.String r7 = "DEX"
            java.lang.String r1 = "Deleting %s."
            java.lang.Object[] r3 = new java.lang.Object[r2]
            r3[r4] = r0
            java.lang.String r1 = java.lang.String.format(r1, r3)
            android.util.Log.d(r7, r1)
            boolean r7 = r0.delete()
            if (r7 != 0) goto L_0x0066
            java.lang.String r7 = "DEX"
            java.lang.String r1 = "Deleting %s FAILED!"
            java.lang.Object[] r3 = new java.lang.Object[r2]
            r3[r4] = r0
            java.lang.String r1 = java.lang.String.format(r1, r3)
            android.util.Log.d(r7, r1)
        L_0x0066:
            r7 = 0
        L_0x0067:
            if (r7 != 0) goto L_0x008d
            java.lang.String r1 = "DEX"
            java.lang.String r3 = "Creating directory: %s."
            java.lang.Object[] r5 = new java.lang.Object[r2]
            r5[r4] = r0
            java.lang.String r3 = java.lang.String.format(r3, r5)
            android.util.Log.d(r1, r3)
            boolean r1 = r0.mkdirs()
            if (r1 != 0) goto L_0x008d
            java.lang.String r1 = "DEX"
            java.lang.String r3 = "%s - Directory not created."
            java.lang.Object[] r5 = new java.lang.Object[r2]
            r5[r4] = r0
            java.lang.String r3 = java.lang.String.format(r3, r5)
            android.util.Log.d(r1, r3)
        L_0x008d:
            java.lang.String r1 = "%s"
            java.lang.Object[] r3 = new java.lang.Object[r2]
            r3[r4] = r0
            java.lang.String r0 = java.lang.String.format(r1, r3)
            r6.dexDir = r0
            java.lang.String r0 = "DEX"
            java.lang.String r1 = "DEX Directory name is: %s."
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r3 = r6.dexDir
            r2[r4] = r3
            java.lang.String r1 = java.lang.String.format(r1, r2)
            android.util.Log.d(r0, r1)
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.honeywell.usbakerydex.MyService.DirectoryExists(java.lang.String):boolean");
    }

    private String FindDexDirectory() {
        String str = "";
        try {
            AssetManager assets = getAssets();
            String[] list = assets.list("");
            if (list == null || list.length <= 0) {
                return str;
            }
            int length = list.length;
            int i = 0;
            while (i < length) {
                String str2 = list[i];
                if (str2.equalsIgnoreCase("config.ini")) {
                    Log.d("DEX", "Found the config.ini");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assets.open(str2)));
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            return str;
                        }
                        String[] split = readLine.split("=");
                        if (split.length == 2 && split[0].equalsIgnoreCase("DEX_DIRECTORY")) {
                            Log.d("DEX", String.format("DEX_DIRECTORY = %s", new Object[]{split[1]}));
                            return split[1];
                        }
                    }
                } else {
                    i++;
                }
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }

    private void CopyAssetFiles(String str) {
        Log.d("DEX", String.format("CopyAssetFiles to: %s", new Object[]{str}));
        try {
            AssetManager assets = getAssets();
            String[] list = assets.list("");
            if (list != null && list.length > 0) {
                for (String str2 : list) {
                    Log.d("DEX", String.format("CopyAssetFiles: Copying file: %s", new Object[]{str2}));
                    try {
                        InputStream open = assets.open(str2);
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(str, str2));
                        CopyFile(open, fileOutputStream);
                        open.close();
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        StringBuilder sb = new StringBuilder();
                        sb.append("file://");
                        sb.append(str);
                        sb.append(File.separator);
                        sb.append(str2);
                        sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse(sb.toString())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    private void CheckAssetFiles(String str) {
        Log.d("DEX", String.format("CheckAssetFiles: Check all asset files present in  %s", new Object[]{str}));
        try {
            AssetManager assets = getAssets();
            String[] list = assets.list("");
            if (list != null && list.length > 0) {
                for (String str2 : list) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(str);
                    sb.append(File.separator);
                    sb.append(str2);
                    if (!new File(sb.toString()).exists()) {
                        Log.d("DEX", String.format("CheckAssetFiles: Copying file: %s", new Object[]{str2}));
                        try {
                            InputStream open = assets.open(str2);
                            FileOutputStream fileOutputStream = new FileOutputStream(new File(str, str2));
                            CopyFile(open, fileOutputStream);
                            open.close();
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("file://");
                            sb2.append(str);
                            sb2.append(File.separator);
                            sb2.append(str2);
                            sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse(sb2.toString())));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    private void CopyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[1024];
        while (true) {
            int read = inputStream.read(bArr);
            if (read != -1) {
                outputStream.write(bArr, 0, read);
            } else {
                return;
            }
        }
    }
}
