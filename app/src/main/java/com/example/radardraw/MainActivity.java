package com.example.radardraw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    ImageView iv1,iv2;
    FrameLayout fl;
    LayoutInflater layoutInflater;
    HttpURLConnection con;
    Handler mHnd = new Handler(new HDCbFun());
    String url,url2;
    int timeInt,checkTime1,checkTime2;
    int mode = 0;
    int datePosion = -1;
    List<String> dateList,timeList;
    Button bt;
    Spinner sp;
    ConstraintLayout cl;
    boolean spOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cl = findViewById(R.id.ConstraintLayout);
        tv = findViewById(R.id.textView);


        iv1 = findViewById(R.id.imageView);
        iv2 = findViewById(R.id.imageView2);
        fl = findViewById(R.id.framelayout_main);
        layoutInflater = getLayoutInflater();

        url = "http://crs.comm.yzu.edu.tw:8888/Basement/"; //存放現有資料的網站

        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fl.removeAllViews();
                dateList = new ArrayList<String>();
                mode = 1;
                Thread t1 = new Thread(new TRClass(url));
                View view = layoutInflater.inflate(R.layout.layout_base,null);
                fl.addView(view);
                bt = findViewById(R.id.button_base);
                sp =findViewById(R.id.spinner);

                EditText edHour = findViewById(R.id.editTextTime);
                edHour.setEnabled(false);
                EditText edMin = findViewById(R.id.editTextTime2);
                edMin.setEnabled(false);

                t1.start();

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

                edHour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("hour should be between 0 and 24")
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            edHour.requestFocus();
                                        }
                                    }).create().show();
                        }
                    }
                });

                edMin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("minute should be between 0 and 60")
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            edMin.requestFocus();
                                        }
                                    }).create().show();
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
                        if (string1.equals("") || string2.equals("")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("input time").setPositiveButton("ok",null).create().show();
                        }else {
                            if (Integer.valueOf(string1)<0 || Integer.valueOf(string1)>24){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("time error")
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                edHour.requestFocus();
                                            }
                                        }).create().show();
                            }else if ( Integer.valueOf(string2)<0 || Integer.valueOf(string2)>60){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("time error")
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                edMin.requestFocus();
                                            }
                                        }).create().show();
                            }else {
                                timeInt = Integer.valueOf(string1+string2);
                                checkTime1 = checkTime(timeInt);
                                checkTime2 = checkTime(timeInt+5);
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
        });

        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fl.removeAllViews();
                mode = 2;
                tv.setText("Local mode");

                View view = layoutInflater.inflate(R.layout.layout_local,null);
                fl.addView(view);

                EditText edMs = findViewById(R.id.editTextNumber);
                EditText edData = findViewById(R.id.editTextNumber2);

                bt = findViewById(R.id.button_local);
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String msStr = edMs.getText().toString();
                        String dataStr = edData.getText().toString();

                        if (msStr.equals("") || dataStr.equals("")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("input the time")
                                    .setPositiveButton("ok",null)
                                    .create().show();
                        }else {
                            double msInt = Integer.valueOf(msStr);
                            double dataInt = Integer.valueOf(dataStr);
                            double waitSec = dataInt*msInt/1000;

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("It would take about "+(int)msInt+"ms * "+(int)dataInt+" = "+waitSec+" second for a picture")
                                    .setNegativeButton("cancel",null)
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(MainActivity.this,DrawLocalActivity.class);
                                            intent.putExtra("ms",msInt);
                                            intent.putExtra("data",dataInt);
                                            startActivity(intent);
                                        }
                                    }).create().show();
                        }
                    }
                });
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
            intent.putExtra("mode",1);
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

    public void edTextCheck(String tineStr,int t1,int t2){

    }

    public int checkTime(int time){

        if (String.valueOf(time).length()>4){
            return -1;
        }else{
            int min = time%100;
            int hour = time/100;
            if (min>60){
                min = min-60;
                hour = hour+1;
            }

            if (hour>23){
                return -1;
            }else {
                return hour*100+min;
            }
        }
    }
}