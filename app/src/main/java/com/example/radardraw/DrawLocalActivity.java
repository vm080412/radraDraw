package com.example.radardraw;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DrawLocalActivity extends AppCompatActivity {

    int [][] heatMapCatch = new int[64][48];
    int dataPerPic, secPerData,counter = 0;
    Thread t1,t2;
    TextView tv;
    Button bt;
    Intent it;
    FrameLayout layout;
    boolean drawDone = true;
    boolean stopR = false;
    Handler handler = new Handler(new HDCB());
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmssSS");

    private UsbManager manager;
    private UsbSerialPort uartPort, dataPort;
    private UsbSerialDriver driver;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private enum UsbPermission { Unknown, Requested, Granted, Denied };
    private UsbPermission usbPermission = UsbPermission.Unknown;
    //
    private static final String ACTION_USB_PERMISSION = BuildConfig.APPLICATION_ID+".USB_PERMISSION";
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    PendingIntent permissionIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_local);

        mmWave m1843=new mmWave();
        bt = findViewById(R.id.button3);
        it = this.getIntent();
        tv = findViewById(R.id.textView7);
        layout = findViewById(R.id.FrameLayout2);
        stopR = false;
        drawDone = true;

        secPerData = (int) it.getDoubleExtra("ms",200);
        dataPerPic = (int) it.getDoubleExtra("data",5);

        //--- 1. Register the USB permission receiver with USB PERMISSION
        manager = (UsbManager) getSystemService(USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        //--- 2. Find the devices available and request permission
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            manager.requestPermission(device, permissionIntent);
        }
        //--- 3. Find the corresponding device driver
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }
        driver = availableDrivers.get(0);
        connection = manager.openDevice(driver.getDevice());
        //--- 4. Deal with the USB Permission
        if(connection == null && usbPermission == UsbPermission.Unknown && !manager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            manager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(connection == null) {
            if (!manager.hasPermission(driver.getDevice()))
                Log.d("Kan:","connection failed: permission denied");
            else
                Log.d("Kan:","connection failed: open failed");
            return;
        }

        dataPort = driver.getPorts().get(1); //--- Data Port
        while (!dataPort.isOpen()){
            try {
                dataPort.open(connection);
                dataPort.setParameters(921600,8, UsbSerialPort.STOPBITS_1,UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Log.d("Kan:Data port open error: ",e.getMessage());
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                m1843.parseODPacket(dataPort, getApplicationContext());
            }
        };

        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (!stopR){
                    Log.d("kanRa",m1843.recordGo+"");
                    if (m1843.recordGo && drawDone){ // i = record data number
                        drawDone = false;
                        handler.obtainMessage(3,m1843.getODHeatMapRecord())
                                .sendToTarget();

                    }
                }
            }
        };
        
        t1 = new Thread(runnable);
        t2 = new Thread(runnable2);
        t1.start();
        t2.start();

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1843.setPackEndLoop(true);
                stopR = true;
                finish();
            }
        });
    }

    class HDCB implements Handler.Callback{
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (counter == 0){
                heatMapInit(heatMapCatch);
                heatMapPlus(heatMapCatch,(int[][]) msg.obj);
                counter++;
            }else if(counter == dataPerPic-1){
                heatMapPlus(heatMapCatch,(int[][]) msg.obj);
                heatMapDev(heatMapCatch);

                rainbowFan rainbowFan = new rainbowFan(DrawLocalActivity.this,heatMapCatch);
                layout.addView(rainbowFan);
                counter = 0;
            }else {
                heatMapPlus(heatMapCatch,(int[][]) msg.obj);
                counter++;
            }

            try {
                Thread.sleep(secPerData);
                drawDone = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return true;
        }
    }

    private void heatMapInit(int[][] array){
        for (int a = 0;a < 64;a++){
            for (int i = 0;i < 48;i++){
                array[a][i] = 0;
            }
        }
    }
    private void heatMapPlus(int[][] array,int[][] plusArray){
        for (int a = 0;a < 64;a++){
            for (int i = 0;i < 48;i++){
                array[a][i] += plusArray[a][i];
            }
        }
    }
    private void heatMapDev(int[][] array){
        for (int a = 0;a < 64;a++){
            for (int i = 0;i < 48;i++){
                array[a][i] = array[a][i]/dataPerPic;
            }
        }
    }
    private void recordData(int[][] array){  // to check data
        Date date = new Date();
        String strD = sdf.format(date);
        String dataPath = getApplicationContext().getFilesDir()+"/"+strD+"+"+counter+".csv";
        File newIFile=new File(dataPath);
        try {
            newIFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(newIFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            for (int a = 0;a < 64;a++){
                String str = "";
                for (int b =0;b < 48;b++){
                    if (b == 47){
                        str = str+array[a][b];
                    }else {
                        str = str+array[a][b]+",";
                    }
                }
                osw.write(str);
                osw.write("\r\n");
            }
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
