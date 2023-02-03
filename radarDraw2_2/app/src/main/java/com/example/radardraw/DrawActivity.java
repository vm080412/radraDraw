package com.example.radardraw;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

public class DrawActivity extends AppCompatActivity {
    Button bt;
    TextView tv;
    Spinner sp;
    FrameLayout layout;
    Intent dit;
    List<String> timeList;
    ArrayAdapter<String> arrAd;
    Handler mHnd = new Handler(new HDCbFun());
    Thread t1;
    boolean spOn = false;
    int counter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        bt = findViewById(R.id.button2);
        layout = findViewById(R.id.frameLayout);
        tv = findViewById(R.id.textView3);
        sp = findViewById(R.id.spinner3);
        dit = this.getIntent();
        timeList = dit.getStringArrayListExtra("time");
        counter = 0;

        arrAd = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,timeList);
        sp.setAdapter(arrAd);
        sp.setEnabled(true);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.removeAllViews();
            }
        });

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spOn){
                    sp.setEnabled(false);
                    layout.removeAllViews();
                    tv.setText("wait");
                    t1 = new Thread(new TRClass3(position));
                    t1.start();
                }else {
                    spOn = true;
                }

                /*sp.setEnabled(false);
                rainbowFan = new rainbowFan(DrawActivity.this,timeList.get(position));
                layout.addView(rainbowFan);*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    class HDCbFun implements Handler.Callback{

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            tv.setText("ok");
            sp.setEnabled(true);
            layout.addView((View)msg.obj);
            tv.setText("choose");
            return true;
        }
    }

    class TRClass3 implements Runnable{
        private int position;
        TRClass3(int position){
            this.position = position;
        }

        @Override
        public void run() {
            rainbowFan rainbowFan = new rainbowFan(DrawActivity.this,timeList.get(position));
            mHnd.obtainMessage(2,rainbowFan)
                    .sendToTarget();
        }
    }
}
