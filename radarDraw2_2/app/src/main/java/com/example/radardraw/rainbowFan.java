package com.example.radardraw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class rainbowFan extends View {

    String scvPath;
    List<String> dataSp;


    public rainbowFan(Context context) {
        super(context);
    }
    public rainbowFan(Context context,String path){
        super(context);
        scvPath = path;
    }

    public void setDataSp(String scvPath){

        dataSp = new ArrayList<String>();
        String dataStr;
        try {
            InputStream is = getContext().openFileInput(scvPath);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));

            while ((dataStr=in.readLine())!=null){
                dataSp.add(dataStr);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.reverse(dataSp);
    }

    //set color
    public static List<Integer> intervalColors(float angleFrom, float angleTo, int n) {
        float angleRange = angleTo - angleFrom;
        float stepAngle = angleRange / n;

        List<Integer> colors =  new ArrayList<Integer>();;
        for (int i = 0; i < n; i++) {
            float angle = angleFrom + i*stepAngle;
            float hsv[] = new float[] { angle, 1f, 1f };
            colors.add(Color.HSVToColor(hsv));

        }
        Collections.reverse(colors);
        return colors;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        setDataSp(scvPath);
        List<Integer>  colors = intervalColors(0, 240, 512);
        Paint c;
        RectF rect;
        Bitmap bitmap = Bitmap.createBitmap(1550,1050,Bitmap.Config.RGB_565);
        Canvas bitmapCanvas = new Canvas(bitmap);

        for (int a = 0;a < 64;a++){
            String[] col = dataSp.get(a).split(",");
            for (int i = 0;i < 48;i++){
                int huecolor;
                huecolor = (int) Double.parseDouble(col[i]);
                if(huecolor>=512){
                    huecolor=511;
                }
                c = new Paint();
                c.setAntiAlias(true);
                c.setColor(colors.get(huecolor));
                rect = new RectF(50+11*a, (float) (50+7.5*a),1550-11*a, (float) (1050-7.5*a));
                bitmapCanvas.drawArc(rect, (float) (240+1.25*i), (float) 1.25,true,c);
                Log.d("KanCa",":"+a+"-"+i);
            }
        }
        canvas.drawBitmap(bitmap,0,0,null);
    }
}
