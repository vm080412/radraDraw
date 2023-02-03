package com.example.radardraw;


public class getFromNet {

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
