package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivilegedExceptionAction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    public static final int REQUST_CODE_MENU1 = 100;
    public static final int REQUST_CODE_MENU2 = 200;
    public static final int REQUST_CODE_MENU3 = 300;
    public static final int REQUST_CODE_MENU4 = 400;
    public static final String KEY_CONNECTIONS = "KEY_CONNECTIONS";

    TextView date, foreGrowth_tv,login_text;
    Button registerBtn;
    Button calendarBtn;
    Button chatBtn;
    Button joinBtn;
    Button logoutBtn;
    Button finishBtn;
    Button tem;
    Button hum;

    String sensor_tem, sensor_hum;
    String vetName;
    String vetDate="null";
    String growth_span;

    TextView todayWea,todayTem,todayHum,todayRain;
    Handler handler = new Handler();

    String id; //사용자 id
    User app_user; //사용자 객체

    SharedPreferences pref;
    Gson gson; //share에 객체를 저장하기 위한 gson
    private boolean saveLoginData;
    ProgressDialog pDialog;

    private static String urlStr = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=63&gridy=123";
    private   static   final   String  SERVER_ADDRESS = "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com";  //서버 IP를 전역변수로..
    String  uri= "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com/Login/result.xml";  //원하는 링크 접속

    final static String TAG = "MAIN_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("pref",Activity.MODE_PRIVATE);
        final String rcv_uid = pref.getString("uid","null");

        if(rcv_uid!=null){
            //유지되고 있는 부분이 있다면.
            SharedPreferences pref2 = getSharedPreferences("pref2",Activity.MODE_PRIVATE);
            String json = pref2.getString("app-user", "");
            gson = new GsonBuilder().create();
            app_user = gson.fromJson(json, User.class);//변환
            resume();
        }

        registerBtn=(Button)findViewById(R.id.button);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("pref",Activity.MODE_PRIVATE);
                final String rcv_uid = pref.getString("uid","null");

                if(rcv_uid.equals("null")){
                    showMessage_login();
                }else{
                    Intent intent=new Intent(getApplicationContext(),RegActivity.class);
                    intent.putExtra("app_user",app_user);
                    startActivityForResult(intent, REQUST_CODE_MENU1);
                }
            }
        });

        calendarBtn = (Button)findViewById(R.id.button3);
        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("pref",Activity.MODE_PRIVATE);
                final String rcv_uid = pref.getString("uid","null");

                if(app_user.getGrowth_span()==null && registerBtn.getText().equals("작물등록")){
                    if(rcv_uid.equals("null")){
                        Log.d(TAG,"미로그인_ 로그인 안내창");
                        showMessage_login();
                    }else{
                        Log.d(TAG,"로그인 ok_재배채소가 없음");
                        showMessage();
                    }
                }else{
                    Intent intent = new Intent(getApplicationContext(),CalendarActivity.class);
                    Log.d(TAG,"캘린더 입장 전 상태 확인"+app_user.isToday_register());
                    intent.putExtra("app_user",app_user);
                    startActivityForResult(intent,REQUST_CODE_MENU2);
                }
            }
        });

        finishBtn=(Button)findViewById(R.id.button2);
        finishBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("pref",Activity.MODE_PRIVATE);
                final String rcv_uid = pref.getString("uid","null");
                Log.v(TAG,registerBtn.getText().toString());

                if(!registerBtn.getText().toString().equals("작물등록")){
                    Log.v(TAG,"재배완료 버튼_정상작동");
                    showFinishMsg();
                }else if(registerBtn.getText().toString().equals("작물등록")){
                    Log.v(TAG,"현재 사용자 id"+rcv_uid);
                    if(rcv_uid.equals("null")){
                        showMessage_login();
                    }else{
                        showMessage();
                    }
                }
            }
        });

        tem = (Button)findViewById(R.id.imageButton3);
        hum = (Button)findViewById(R.id.imageButton4);

        chatBtn = (Button)findViewById(R.id.button5);
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                startActivityForResult(intent,REQUST_CODE_MENU3);
            }
        });

        date=(TextView)findViewById(R.id.foreGrowth_tv);
        if(vetDate.equals("null")){
            date.setText("예상 재배기간:");
        }

        foreGrowth_tv = (TextView)findViewById(R.id.foreGrowth_tv);

        //기상청 RSS 파싱
        todayWea = (TextView)findViewById(R.id.todayWea_tv);
        todayTem = (TextView)findViewById(R.id.todayTem_tv);
        todayHum = (TextView)findViewById(R.id.todayHum_tv);
        todayRain = (TextView)findViewById(R.id.todayRain_tv);

        ConnectThread thread = new ConnectThread(urlStr);
        thread.start();

        joinBtn=(Button)findViewById(R.id.button6);
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),JoinActivity.class);
                startActivityForResult(intent, REQUST_CODE_MENU4);
            }
        });


        logoutBtn = (Button)findViewById(R.id.button7);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor autoLogin = auto.edit();
                //editor.clear()는 auto에 들어있는 모든 정보를 기기에서 지웁니다.
                autoLogin.clear();
                autoLogin.commit();

                SharedPreferences pref=getSharedPreferences("pref", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor=pref.edit();
                editor.remove("uid");
                editor.clear();
                editor.commit();
                editor.commit();

                SharedPreferences pref2=getSharedPreferences("pref2", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor2=pref2.edit();
                editor2.clear();
                editor2.commit();
                editor2.remove("app-user");
                editor2.commit();

                //객체 초기화 하기
                app_user = null;

                //화면 정보 초기화 하기
                id=null;
                registerBtn.setText("작물등록");
                foreGrowth_tv.setText("예상 재배기간:");
                joinBtn.setText("LOGIN");
                vetDate = "null";
                login_text.setText("로그인을 해주세요");
                Toast.makeText(MainActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
            }
        });

        login_text = (TextView)findViewById(R.id.login_text);

        if(rcv_uid!=null){
            app_user = new User(rcv_uid);
            Thread thread1 = create_InitialValue();
            thread1.start();
            try{
                thread1.join();
            }catch (InterruptedException ie){
                ie.getMessage();
            }
            resume();
        }
    }

    private void showMessage_login() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("안내");
        alertDialogBuilder
                .setMessage("로그인 먼저 해주세요")
                .setIcon(android.R.drawable.ic_dialog_alert);

        alertDialogBuilder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent=new Intent(getApplicationContext(),JoinActivity.class);
                startActivityForResult(intent, REQUST_CODE_MENU4);
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    private void showMessage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("안내");
        alertDialogBuilder
                .setMessage("재배할 채소를 먼저 선택해주세요")
                .setIcon(android.R.drawable.ic_dialog_alert);

        alertDialogBuilder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), RegActivity.class);
                intent.putExtra("app_user",app_user);
                startActivityForResult(intent, REQUST_CODE_MENU1);
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }


    private void showFinishMsg() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("알림");
        alertDialogBuilder
                .setMessage("정말 작물재배를 완료하시겠습니까?")
                .setIcon(android.R.drawable.ic_dialog_alert);

        //김: 재배확인 버튼 누르면 재배작물 정보
        alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteThread dt = new deleteThread();
                dt.start();

                registerBtn.setText("작물등록");
                foreGrowth_tv.setText("예상 재배기간:");

                app_user.setStart_date(null);
                app_user.setFruit(null);
                app_user.setGrowth_span(null);
            }
        });

        alertDialogBuilder.setNegativeButton("아니오",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public class deleteThread extends Thread{

        public void run(){
            try{
                Log.d(TAG,app_user.getId());
                URL url = new URL(SERVER_ADDRESS+"/Login/deleteUserInfor.php?"+"id="+app_user.getId());
                url.openStream(); //서버에 있는 php파일에 id와 password를 인자로 입력하는 구문
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUST_CODE_MENU1) {
            if (resultCode == RESULT_OK) {
                app_user = (User)data.getSerializableExtra("app_user");
                date.setText("예상 재배기간 :"+app_user.getGrowth_span()+"일");
                registerBtn.setText(app_user.getFruit());
            }
        }else if(requestCode==REQUST_CODE_MENU4){
            if(resultCode==RESULT_OK){

                id=data.getExtras().getString("id");
                joinBtn.setText(id);

                SharedPreferences pref=getSharedPreferences("pref", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor=pref.edit();
                editor.putString("uid", id);
                editor.commit();

                //새로운 객체가 생성된 것이다. 따라서 정보를 얻어온다.
                app_user = new User(id);
                Thread initialValue = create_InitialValue();
                initialValue.start();

                try{
                    initialValue.join(); //스레드가 끝날때까지 대기한다.
                }catch (InterruptedException ie){
                    ie.getMessage();
                }
                resume();
            }
        }else if(requestCode == REQUST_CODE_MENU3){
            if(resultCode==RESULT_OK) {
                app_user = (User)data.getSerializableExtra("app_user");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"pause실행");
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreState();
        Log.d(TAG,"resume 실행");

//        pDialog=new ProgressDialog(MainActivity.this);
//        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        pDialog.setMessage("데이터 동기화 중..");
//        pDialog.show();

        resume();
    }

    protected void saveState(){
        SharedPreferences pref2=getSharedPreferences("pref2", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=pref2.edit();

        gson = new GsonBuilder().create();
        String json = gson.toJson(app_user,User.class);//Json으로 변환
        editor.putString("app-user", json); //Json으로 변환한 객체를 저장한다.
        editor.commit();
    }

    protected void restoreState(){
        SharedPreferences pref=getSharedPreferences("pref",Activity.MODE_PRIVATE);
        String rcv_uid=pref.getString("uid","null");

        if(rcv_uid.equals("null")){
            login_text.setText("로그인을 해주세요");
        }else{
            login_text.setText(rcv_uid+"님 환영합니다.");

        }
    }

    protected void clear(){
        SharedPreferences pref = getSharedPreferences("pref2",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        editor.clear();
        editor.commit();
    }


    Handler xmlHandler= new  Handler(){
        public void handleMessage(Message msg){
            SharedPreferences pref=getSharedPreferences("pref",Activity.MODE_PRIVATE);
            final String rcv_uid=pref.getString("uid","null");
            if(rcv_uid.equals("null")){
                joinBtn.setText("LOGIN");
            }else{
                joinBtn.setText(rcv_uid);
            }

            if (msg.what==0){
                if(app_user.getFruit()!=null){
                    registerBtn.setText(app_user.getFruit());
                    if(app_user.getGrowth_span()!=null){
                        date.setText("예상 재배기간 :"+app_user.getGrowth_span()+"일");
                        vetDate=app_user.getGrowth_span();
                    }
                }else{
                    registerBtn.setText("작물등록");
                }
            }else if(msg.what==1){
                tem.setText(sensor_tem+"C");
                hum.setText(sensor_hum+"%");
            }else if(msg.what ==2){
                date.setText("예상 재배기간 :"+app_user.getGrowth_span()+"일");
            }

            //pDialog.dismiss();
        }

    };


    private void savePreferences(String key){
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("key", key);
        editor.commit();
    }

    private void resume(){
        SharedPreferences pref=getSharedPreferences("pref",Activity.MODE_PRIVATE);
        final String rcv_uid=pref.getString("uid","null");

        //만약에 객체에 값이 있으면 새롭게 객체를 생성하지 않도록 설정을 해볼까...
        if(rcv_uid!=null){

            new Thread(new Runnable(){
                String tems;
                String hums;

                @Override
                public synchronized void run() {

                    try{
                        if(app_user.getFruit()!=null) {
                            Log.d("next", "접근을 했습니다.");
                            String vegeName = registerBtn.getText().toString();
                            String ret3 = ""; //xml에서 받아온 TEXT를 임시로 받는 변수
                            String rss3 = SERVER_ADDRESS + "/Login/"; //서버의 rss 파일이 있는 위치 (xml파일)
                            String tagname3 = ""; //xml의 태그네밈을 위한 변수

                            XmlPullParserFactory factory3 = XmlPullParserFactory.newInstance(); //xmlPullparser를 위한 준비과정.
                            factory3.setNamespaceAware(true); //xml의 네임페이스 허용
                            XmlPullParser xpp3 = factory3.newPullParser();
                            URL url3 = new URL(SERVER_ADDRESS + "/Login/getVegeGrowthDay.php?name=" + app_user.getFruit());
                            url3.openStream(); //김: 서버에 있는 php파일에 vegeName을 넣는다.

                            URL server3 = new URL(rss3 + "vegeGrowthDay.xml"); //php문이 실행이 되면 mysql과 연동되어 그 결과를 xml파일로 생성하는데 그 생성된 파일이 insert.xml 파일
                            InputStream is3 = server3.openStream(); //xml파일연결
                            xpp3.setInput(is3, "utf-8"); //인코딩 방식 설정

                            int eventType3 = xpp3.getEventType(); //이벤트 타입 얻어오기 예를 들어 <start> 인지 </start> 인지 구분하기 위한.
                            while (eventType3 != XmlPullParser.END_DOCUMENT) {  // xml이 끝날때까지 계속 돌린다.
                                if (eventType3 == XmlPullParser.START_TAG) {
                                    tagname3 = xpp3.getName();  //태그를 받아온다.
                                } else if (eventType3 == XmlPullParser.TEXT) { // 김: 이벤트타입이 태그가 아니라면
                                    if (tagname3.equals("growth_span")) {
                                        growth_span = xpp3.getText(); //김: 문자열로 저장된 생장기간을 숫자로 변환
                                        app_user.setGrowth_span(growth_span);
                                    }
                                } else if (eventType3 == XmlPullParser.END_TAG) {
                                    //태그가 닫히는 부분에서 임시 저장된 TEXT를 Array에 저장한다.
                                    tagname3 = xpp3.getName();
                                    if (tagname3.equals("growth_span")) {
                                    }
                                }
                                eventType3 = xpp3.next();
                            }
                            xmlHandler.sendEmptyMessage(2);
                        }

                        if(app_user.isToday_register()==true){

                            String ret2 = ""; //xml에서 받아온 TEXT를 임시로 받는 변수
                            String rss2 = "http://192.168.0.132/sensorDB/"; //서버의 rss 파일이 있는 위치 (xml파일)
                            String tagname2 = ""; //xml의 태그네밈을 위한 변수

                            XmlPullParserFactory factory2 = XmlPullParserFactory.newInstance(); //xmlPullparser를 위한 준비과정.
                            factory2.setNamespaceAware(true); //xml의 네임페이스 허용
                            XmlPullParser xpp2 = factory2.newPullParser();

                            try {
                                URL url2 = new URL("http://192.168.0.132/sensorDB/getReDHTData.php");
                                url2.openStream(); //김: 서버에 있는 php파일에 vegeName을 넣는다.
                            }catch (Exception e){
                                Log.v("sensor_tag",""+e.toString());
                            }

                            URL server2 = new URL(rss2 + "reDHTData.xml"); //php문이 실행이 되면 mysql과 연동되어 그 결과를 xml파일로 생성하는데 그 생성된 파일이 insert.xml 파일
                            InputStream is2 = server2.openStream(); //xml파일연결
                            xpp2.setInput(is2, "utf-8"); //인코딩 방식 설정
                            int eventType2 = xpp2.getEventType(); //이벤트 타입 얻어오기 예를 들어 <start> 인지 </start> 인지 구분하기 위한.
                            while (eventType2 != XmlPullParser.END_DOCUMENT) {  // xml이 끝날때까지 계속 돌린다.
                                if (eventType2 == XmlPullParser.START_TAG) {
                                    tagname2 = xpp2.getName();  //태그를 받아온다.
                                } else if (eventType2 == XmlPullParser.TEXT) { // 김: 이벤트타입이 태그가 아니라면
                                    if (tagname2.equals("tem")) {
                                        tems =xpp2.getText();
                                    }else if(tagname2.equals("hum")){
                                        hums = xpp2.getText();
                                    }
                                } else if (eventType2 == XmlPullParser.END_TAG) {
                                    //태그가 닫히는 부분에서 임시 저장된 TEXT를 Array에 저장한다.
                                    tagname2 = xpp2.getName();
                                    if(tagname2.equals("tem")){
                                        sensor_tem=tems;
                                    }else if(tagname2.equals("hum")){
                                        sensor_hum=hums;
                                    }
                                }
                                eventType2 = xpp2.next();
                            }
                            xmlHandler.sendEmptyMessage(1);
                        }



                    } // try문 끝
                    catch (Exception e){
                        e.getMessage();
                    }  //catch문 끝

                }}).start();  //start로 스레드 실행.
        }

    }

    //서버에서 사용자의 정보를 가져와야할 때 쓸 것!
    public Thread create_InitialValue(){

        return new Thread(new Runnable() {
            String fruit;
            String growth_start;

            @Override
            public void run() {
                try{
                    SharedPreferences pref = getSharedPreferences("pref",Activity.MODE_PRIVATE);
                    final String rcv_uid = pref.getString("uid","null");

                    String ret=""; //xml에서 받아온 TEXT를 임시로 받는 변수
                    String rss = SERVER_ADDRESS + "/Login/"; //서버의 rss 파일이 있는 위치 (xml파일)
                    String tagname=""; //xml의 태그네밈을 위한 변수

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); //xmlPullparser를 위한 준비과정.
                    factory.setNamespaceAware(true); //xml의 네임페이스 허용
                    XmlPullParser xpp = factory.newPullParser();

                    URL url = new URL(SERVER_ADDRESS+"/Login/getUserInfor.php?"+"id="+rcv_uid);
                    url.openStream(); //서버에 있는 php파일에 id와 password를 인자로 입력하는 구문

                    URL server = new URL(rss + "userInfor.xml"); //php문이 실행이 되면 mysql과 연동되어 그 결과를 xml파일로 생성하는데 그 생성된 파일이 insert.xml 파일

                    InputStream is = server.openStream(); //xml파일연결
                    xpp.setInput(is, "utf-8"); //인코딩 방식 설정

                    int eventType = xpp.getEventType(); //이벤트 타입 얻어오기 예를 들어 <start> 인지 </start> 인지 구분하기 위한.

                    while (eventType != XmlPullParser.END_DOCUMENT) {  // xml이 끝날때까지 계속 돌린다.
                        Log.d("fruit", "접근");
                        if (eventType == XmlPullParser.START_TAG) {
                            tagname = xpp.getName();  //태그를 받아온다.
                            Log.d("tag", tagname);
                        } else if (eventType == XmlPullParser.TEXT) {
                            if (tagname.equals("fruit")) {
                                fruit = xpp.getText(); //fruit 태그에 해당되는 TEXT를 임시로 저장
                                Log.d("fruit", fruit);
                            } else if (tagname.equals("growth_start")) {
                                growth_start = xpp.getText();
                            }
                        } else if (eventType == XmlPullParser.END_TAG) {
                            //태그가 닫히는 부분에서 임시 저장된 TEXT를 Array에 저장한다.
                            tagname = xpp.getName();
                            if (tagname.equals("fruit")) {
                                app_user.setFruit(fruit);
                            } else if (tagname.equals("growth_start")) {
                                app_user.setStart_date(growth_start);
                            }
                        }
                        eventType = xpp.next();
                    }
                }catch (Exception e){
                    e.getMessage();
                }//catch문 끝

                xmlHandler.sendEmptyMessage(0);
            }
        });
    }


    // 기상청 날씨 정보 추출 함수
    private String loadKmaData(String page) throws Exception {

        URL url = new URL(page);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        Log.d("connection", urlConnection.toString());

        if (urlConnection == null)
            return null;

        urlConnection.setConnectTimeout(10000); //최대 10초 대기
        urlConnection.setUseCaches(false); //매번 서버에서 읽어올 수 있도록 캐시 설정 off

        int resCode = urlConnection.getResponseCode();

        StringBuilder sb = new StringBuilder(); //고속 문자열 결합체 이용

        InputStream inputStream = urlConnection.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        //한줄씩 읽어오기
        BufferedReader br = new BufferedReader(isr);
        while (true) {
            String line = br.readLine();//웹페이지의 html 코드 읽어오기
            if (line == null)
                break;//스트림이 끝나면 null리턴
            sb.append(line + "\n");
        }

        br.close();
        urlConnection.disconnect();

        return sb.toString();
    }

    //날찌 관련 스레드
    class ConnectThread extends Thread{

        String urlStr;

        public ConnectThread(String inStr){
            this.urlStr=inStr;
        }

        @Override
        public void run() {
            try{
                final String html = loadKmaData(urlStr);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.v("connect_thread","run");
                            //Dom 파싱
                            ByteArrayInputStream bai = new ByteArrayInputStream(html.getBytes());
                            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); // 응답 결과물 처리를 위해 Doc Bulider 객체 생성
                            DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
                            Document parse = builder.parse(bai);//DOM 파서

                            //태그 검색
                            NodeList datas = parse.getElementsByTagName("data");
                            String result = "";
                            String day = ""; //오늘 날짜
                            String hour = ""; //동네 예보 3시간 단위
                            String sky = ""; //하늘 상태
                            String temp = ""; //현재 시간 온도
                            String r12 = "";// 12시간 예상 강수량
                            String reh = ""; //습도

                            //data 태그를 순차적으로 접근 --> Dom의 특징
                            for (int idx = 0; idx < datas.getLength(); idx++) {
                                Node node = datas.item(idx);//data 태그 추출
                                int childLength = node.getChildNodes().getLength();

                                //자식 태그 목록
                                NodeList childNodes = node.getChildNodes();
                                for (int childIdx = 0; childIdx < childLength; childIdx++) {
                                    Node childNode = childNodes.item(childIdx);
                                    int count = 0;
                                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                        count++;
                                        //태그인 경우만 처리
                                        //금일,내일,모레 구분(시간정보 포함)
                                        if (childNode.getNodeName().equals("day")) {
                                            int su = Integer.parseInt(childNode.getFirstChild().getNodeValue());
                                            switch (su) {
                                                case 0:
                                                    day = "금일";
                                                    break;
                                                case 1:
                                                    day = "내일";
                                                    break;
                                                case 2:
                                                    day = "모레";
                                                    break;
                                            }
                                        } else if (childNode.getNodeName().equals("hour")) {
                                            hour = childNode.getFirstChild().getNodeValue();
                                            //하늘상태코드 분석
                                        } else if (childNode.getNodeName().equals("wfKor")) {
                                            sky = childNode.getFirstChild().getNodeValue();
                                        } else if (childNode.getNodeName().equals("temp")) {
                                            temp = childNode.getFirstChild().getNodeValue();
                                        } else if (childNode.getNodeName().equals("r12")) {
                                            r12 = childNode.getFirstChild().getNodeValue();
                                        } else if (childNode.getNodeName().equals("reh")) {
                                            reh = childNode.getFirstChild().getNodeValue();
                                        }
                                    }
                                }//end 안쪽 for문
                            }//end 바깥쪽 for문
                            todayWea.setText(" 오늘 날씨: " + sky);
                            todayTem.setText(" 현재시간 온도: " + temp+"℃");
                            todayRain.setText(" 예상 강수량: " + r12+"mm");
                            todayHum.setText(" 오늘 습도: " + reh+"%");
                        } catch (Exception e) {
                            todayWea.setText("날씨 파싱 오류:" + e.getMessage());
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsStrting = sw.toString();

                            Log.e("StackTrace", exceptionAsStrting);
                        }
                    }
                });

            }catch (Exception e){

            }
        }
    }
}