package com.example.radardraw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tv;
    HttpURLConnection con;
    Handler mHnd = new Handler(new HDCbFun());
    String url,url2;
    int timeInt,checkTime1,checkTime2;
    int datePosion = -1;
    List<String> dateList,timeList;
    Button bt;
    Spinner sp,modeSp;
    EditText edTime, edHour,edMin;
    ConstraintLayout cl;

    boolean spOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cl = findViewById(R.id.ConstraintLayout);
        tv = findViewById(R.id.textView);
        sp = findViewById(R.id.spinner);
        sp.setEnabled(false);
        modeSp = findViewById(R.id.spinner2);

        edHour = findViewById(R.id.editTextTime2);
        edHour.setEnabled(false);
        edMin = findViewById(R.id.editTextTime);
        edMin.setEnabled(false);

        bt = findViewById(R.id.button);
        url = "http://crs.comm.yzu.edu.tw:8888/Basement/";
        modeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 1:
                        dateList = new ArrayList<String>();
                        Thread t1 = new Thread(new TRClass(url));
                        t1.start();
                        break;
                    case 2:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spOn){
                    datePosion = position;
                    url2 = url+dateList.get(position)+"/";
                    edHour.setEnabled(true);
                    edMin.setEnabled(true);
                    tv.setText(url2);
                }else {spOn = true;}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*edTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (!edTime.getText().toString().equals("")){
                        timeInt = Integer.valueOf(edTime.getText().toString());
                    }
                }
            }
        });*/

        edHour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String str = edHour.getText().toString();
                    if (str.equals("") == false){
                        int a = Integer.valueOf(str);
                        if ( a<0 || a>24){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("hour between 0 and 24")
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            edHour.requestFocus();
                                        }
                                    }).create().show();
                        }
                    }else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("hour between 0 and 24")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        edHour.requestFocus();
                                    }
                                }).create().show();
                    }
                }
            }
        });

        edMin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String str = edMin.getText().toString();
                    if (str.equals("") == false){
                        int a = Integer.valueOf(str);
                        if ( a<0 || a>60){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("minute between 0 and 60")
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            edMin.requestFocus();
                                        }
                                    }).create().show();
                        }
                    }else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("minute between 0 and 60")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        edMin.requestFocus();
                                    }
                                }).create().show();
                    }
                }
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string1,string2;
                string1 = edHour.getText().toString();
                string2 = edMin.getText().toString();

                if (string2.length()==1){
                    string2 = "0"+string2;
                }
                //edTime.clearFocus();
                if (string1.equals("") || string2.equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("input time").setPositiveButton("ok",null).create().show();
                }else {
                    if (Integer.valueOf(string1)<0 || Integer.valueOf(string1)>24){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("hour between 0 and 24")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        edHour.requestFocus();
                                    }
                                }).create().show();
                    }else if ( Integer.valueOf(string2)<0 || Integer.valueOf(string2)>60){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("minute between 0 and 60")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        edMin.requestFocus();
                                    }
                                }).create().show();
                    }else {
                        timeInt = Integer.valueOf(string1+string2);
                        checkTime1 = new getFromNet().checkTime(timeInt);
                        checkTime2 = new getFromNet().checkTime(timeInt+5);
                        if (checkTime1 != -1 && checkTime2 != -1){
                            tv.setText(checkTime1+"~~~"+checkTime2);
                            Thread t2 = new Thread(new TRClass2(url2));
                            t2.start();
                        }else {
                            tv.setText("time error"+timeInt);
                        }
                    }
                }
            }
        });
    }

    class TRClass implements Runnable{
        private String url;
        TRClass(String url){
            this.url = url;
        }

        @Override
        public void run() {
            String res = getDayList(url);
            mHnd.obtainMessage(1,res)
                    .sendToTarget();
        }
    }

    class TRClass2 implements Runnable{
        private String url;
        TRClass2(String url){
            this.url = url;
        }

        @Override
        public void run() {
            String res = getTimeData(url);
            downloadData();
            Intent intent = new Intent(MainActivity.this,DrawActivity.class);
            intent.putStringArrayListExtra("time", (ArrayList<String>) timeList);
            startActivity(intent);
            mHnd.obtainMessage(2,res)
                    .sendToTarget();
        }
    }

    class HDCbFun implements Handler.Callback{

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            ArrayAdapter<String> arrAd = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,dateList);
            sp.setAdapter(arrAd);
            sp.setEnabled(true);
            spOn = false;
            if (datePosion != -1){
                sp.setSelection(datePosion);
            }
            tv.setText("ok");
            Log.d("Kan","Ok");
            return true;
        }
    }

    private String getDayList(String strTxt){
        StringBuffer response;
        String dateStr;
        try {
            //
            URL obj = new URL(strTxt);
            con = (HttpURLConnection) obj.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            response = new StringBuffer();
            String inputLine;
            int inputLen;
            while ((inputLine=in.readLine())!=null){
                if (inputLine.contains("<tr><td><i class=\"icon icon-_blank\">")){
                    if (!inputLine.contains("<table>")){
                        inputLen = inputLine.length();
                        dateStr = inputLine.substring(inputLen-25,inputLen-15);
                        dateList.add(dateStr);
                        //Log.d("Kan",inputLine.toString());
                        //Log.d("Kan",dateStr);
                    }
                }
            }
            in.close();

            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            con.disconnect();
        }
    }

    private String getTimeData(String strTxt){
        StringBuffer response;
        String dateStr;
        int time;
        timeList = new ArrayList<String>();

        try {
            //
            URL obj = new URL(strTxt);
            con = (HttpURLConnection) obj.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            int inputLen;
            while ((inputLine=in.readLine())!=null){
                if (inputLine.contains("<tr><td><i class=\"icon icon-_page\">")){
                    if (!inputLine.contains("<table>")){
                        inputLen = inputLine.length();
                        dateStr = inputLine.substring(inputLen-31,inputLen-27);
                        time = Integer.valueOf(dateStr);
                        if (time>=checkTime1 && time < checkTime2){
                            timeList.add(inputLine.substring(inputLen-31,inputLen-14));
                        }
                    }
                }
            }
            in.close();

            return "response.toString()";
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            con.disconnect();
        }
    }

    private void downloadData(){
        String dateStr,urlStr;

        if (!timeList.isEmpty()){
            for (int i = 0;i < timeList.size();i++){
                urlStr = url2+timeList.get(i);
                String dataPath = getApplicationContext().getFilesDir()+"/"+timeList.get(i);
                Log.d("kanD",dataPath);
                try {
                    URL obj = new URL(urlStr);
                    con = (HttpURLConnection) obj.openConnection();
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));

                    File newIFile=new File(dataPath);
                    newIFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(newIFile);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);

                    String inputLine;
                    while ((inputLine=in.readLine())!=null){
                        osw.write(inputLine);
                        osw.write("\r\n");
                    }
                    osw.flush();
                    osw.close();

                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}