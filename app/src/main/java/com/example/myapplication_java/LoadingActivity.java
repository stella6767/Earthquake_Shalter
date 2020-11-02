package com.example.myapplication_java;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity {
    Context context = this;
    final String TAG = "LoadingActivity";
    static int count,count2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Shelter> shelters = xml_parse1();
                //ArrayList<Shelter> shelters2 = xml_parse2();

                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                intent.putExtra("shelter", shelters); //데이터 넘기는 사이즈가 제한되어있음...
                //intent.putExtra("shelter2", shelters2);
                startActivity(intent);
            }
        }).start();

    }
        private ArrayList<Shelter> xml_parse1() {
        ArrayList<Shelter> shelterList = new ArrayList<Shelter>();
        InputStream inputStream = getResources().openRawResource(R.raw.earth);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        XmlPullParserFactory xmlPullParserFactory = null;
        XmlPullParser xmlPullParser = null;

        try {
            xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(reader);

            Shelter shelter = null;
            int eventType = xmlPullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){ //원하는 태그만 파싱
                    case XmlPullParser.START_DOCUMENT:
                        Log.i(TAG, "xml START");
                        break;
                    case XmlPullParser.START_TAG:
                        String startTag = xmlPullParser.getName();
                        Log.i(TAG, "Start TAG :" + startTag);
                        if(startTag.equals("row")) {
                            shelter = new Shelter();
                            Log.d(TAG, "Shelter 추가");
                        }
                        else if(startTag.equals("vt_acmdfclty_nm")) {
                            shelter.setName(xmlPullParser.nextText());
                            Log.d(TAG, "이름"+ shelter.getName());
                        }
                        else if(startTag.equals("dtl_adres")) {
                            shelter.setAddress(xmlPullParser.nextText());
                            Log.d(TAG, "주소" + shelter.getAddress());
                        }
                        else if(startTag.equals("rdnmadr_cd")) { //도로명 주소로 구분
                            shelter.setNumber(this.count);
                            Log.d(TAG, String.valueOf(shelter.getNumber()));
                            this.count ++; //배열 인덱스 겸 객체 찾기라고 생각하셈
                            Log.d(TAG, "shelter 번호");
                        }
                        else if(startTag.equals("xcord")) { //double 형으로 안 받아지는디...
                            shelter.setX(Double.parseDouble(xmlPullParser.nextText()));
                            Log.d("x 값", String.valueOf(shelter.x));
                        }
                        else if(startTag.equals("ycord")) {
                            shelter.setY(Double.parseDouble(xmlPullParser.nextText()));
                            Log.d("y 값", String.valueOf(shelter.y));
                        }
                        else if(startTag.equals("fclty_ar")) {
                            shelter.setArea(Double.parseDouble(xmlPullParser.nextText()));
                            Log.d("시설면적 값", String.valueOf(shelter.area));
                        }
                        else if(startTag.equals("mngps_nm")) {
                            //if(xmlPullParser.nextText() == null)
                                //shelter.setPerson("없음");

                            shelter.setPerson(xmlPullParser.nextText());
                            Log.d("담당자: ", String.valueOf(shelter.getPerson()));
                        }
                        else if(startTag.equals("mngps_telno")) {
                            //if(check != null)  없음 구문을 어떻게 만들지......
                                shelter.setPhone(xmlPullParser.nextText());
                            //else shelter.setPhone("연락처 없음");
                            Log.d("담당자 연락처", String.valueOf(shelter.getPhone()));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String endTag = xmlPullParser.getName();
                        Log.i(TAG,"End TAG : "+ endTag);
                        if (endTag.equals("row")) {
                            shelterList.add(shelter); //대피소 추가
                        }
                        break;
                }
                try {
                    eventType = xmlPullParser.next();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try{
                if(reader !=null) reader.close();
                if(inputStreamReader !=null) inputStreamReader.close();
                if(inputStream !=null) inputStream.close();
            }catch(Exception e2){
                e2.printStackTrace();
            }
        }
        Log.d("배열 사이즈", "사이즈" + String.valueOf(shelterList.size())); //대략 2000개
            //이상이면 데이터를 짤라서 보내야 됨
        for (int i=0; i<shelterList.size(); i++) { //빈 문자열 체크해서 "없음" 문자열로 바꿔주는 함수 null 과 "" 은 다른개념
            if ((shelterList.get(i).getPerson() == "") || (shelterList.get(i).getPhone() == "")){ //null 개념 씨바 이해를 할 수가 없네
                shelterList.get(i).setPerson("없음");
                shelterList.get(i).setPhone("연락처 없음");
            Log.d("체크","체크값"+ shelterList.get(i).getPerson());}
        }


        return shelterList;
    }

   /* private ArrayList<Shelter> xml_parse2() {
        ArrayList<Shelter> shelterList = new ArrayList<Shelter>();
        InputStream inputStream = getResources().openRawResource(R.raw.earth2);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        XmlPullParserFactory xmlPullParserFactory = null;
        XmlPullParser xmlPullParser = null;

        try {
            xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(reader);

            Shelter shelter = null;
            int eventType = xmlPullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){ //원하는 태그만 파싱
                    case XmlPullParser.START_DOCUMENT:
                        Log.i(TAG, "xml START");
                        break;
                    case XmlPullParser.START_TAG:
                        String startTag = xmlPullParser.getName();
                        Log.i(TAG, "Start TAG :" + startTag);
                        if(startTag.equals("row")) {
                            shelter = new Shelter();
                            Log.d(TAG, "Shelter 추가");
                        }
                        else if(startTag.equals("vt_acmdfclty_nm")) {
                            shelter.setName(xmlPullParser.nextText());
                            Log.d(TAG, "이름"+ shelter.getName());
                        }
                        else if(startTag.equals("dtl_adres")) {
                            shelter.setAddress(xmlPullParser.nextText());
                            Log.d(TAG, "주소" + shelter.getAddress());
                        }
                        else if(startTag.equals("rdnmadr_cd")) { //도로명 주소로 구분
                            shelter.setNumber(this.count);
                            Log.d(TAG, String.valueOf(shelter.getNumber()));
                            this.count2 ++; //배열 인덱스 겸 객체 찾기라고 생각하셈
                            Log.d(TAG, "shelter 번호");
                        }
                        else if(startTag.equals("xcord")) { //double 형으로 안 받아지는디...
                            shelter.setX(Double.parseDouble(xmlPullParser.nextText()));
                            Log.d("x 값", String.valueOf(shelter.x));
                        }
                        else if(startTag.equals("ycord")) {
                            shelter.setY(Double.parseDouble(xmlPullParser.nextText()));
                            Log.d("y 값", String.valueOf(shelter.y));
                        }
                        else if(startTag.equals("fclty_ar")) {
                            shelter.setArea(Double.parseDouble(xmlPullParser.nextText()));
                            Log.d("시설면적 값", String.valueOf(shelter.area));
                        }
                        else if(startTag.equals("mngps_nm")) {
                            //if(xmlPullParser.nextText() == null)
                            //shelter.setPerson("없음");

                            shelter.setPerson(xmlPullParser.nextText());
                            Log.d("담당자: ", String.valueOf(shelter.getPerson()));
                        }
                        else if(startTag.equals("mngps_telno")) {
                            //if(check != null)  없음 구문을 어떻게 만들지......
                            shelter.setPhone(xmlPullParser.nextText());
                            //else shelter.setPhone("연락처 없음");
                            Log.d("담당자 연락처", String.valueOf(shelter.getPhone()));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String endTag = xmlPullParser.getName();
                        Log.i(TAG,"End TAG : "+ endTag);
                        if (endTag.equals("row")) {
                            shelterList.add(shelter); //대피소 추가
                        }
                        break;
                }
                try {
                    eventType = xmlPullParser.next();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try{
                if(reader !=null) reader.close();
                if(inputStreamReader !=null) inputStreamReader.close();
                if(inputStream !=null) inputStream.close();
            }catch(Exception e2){
                e2.printStackTrace();
            }
        }
        Log.d("배열 사이즈", "사이즈" + String.valueOf(shelterList.size())); //대략 2000개
        //이상이면 데이터를 짤라서 보내야 됨
        for (int i=0; i<shelterList.size(); i++) { //빈 문자열 체크해서 "없음" 문자열로 바꿔주는 함수 null 과 "" 은 다른개념
            if ((shelterList.get(i).getPerson() == "") || (shelterList.get(i).getPhone() == "")){ //null 개념 씨바 이해를 할 수가 없네
                shelterList.get(i).setPerson("없음");
                shelterList.get(i).setPhone("연락처 없음");
                Log.d("체크","체크값"+ shelterList.get(i).getPerson());}
        }


        return shelterList;
    }*/



}