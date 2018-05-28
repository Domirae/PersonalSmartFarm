package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class JoinActivity extends Activity {

    EditText id_input, password_input;
    // String id, password;// id_input, password_input의 text 저장
    String loginId,loginPwd;
    Button checkBtn, inputBtn ;
    ArrayList<String> array_id;
    String TAG= "JoinActivity2";
    SharedPreferences auto;
    SharedPreferences.Editor autoLogin;
    ListView list;
    private   static   final   String  SERVER_ADDRESS = "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com";  //서버 IP를 전역변수로..
    String  uri= "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com/Login/result.xml";  //원하는 링크 접속
    //String  tagname, content;  //xml의 태그와 내용을 담기 위한 변수

    @Override
    protected  void  onCreate(Bundle savedInstanceState) {
        super .onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        checkBtn=(Button)findViewById(R.id.checkBtn);  //로그인을 위한 버튼
        inputBtn=(Button)findViewById(R.id.inputBtn);  //아이디 생성을 위한 버튼

        clickListener click=  new  clickListener();

        checkBtn.setOnClickListener(click);  //리스너 등록
        inputBtn.setOnClickListener(click);

        array_id= new  ArrayList< String >();

        //-----------------------------------------------------------------

        id_input=(EditText)findViewById(R.id.id_input);
        password_input=(EditText)findViewById(R.id.password_input);

        // id = id_input.getText().toString();
        // password = password_input.getText().toString();

        auto = getSharedPreferences("auto",Activity.MODE_PRIVATE);
        //처음에는 SharedPreferences에 아무런 정보도 없으므로 값을 저장할 키들을 생성한다.
        // getString의 첫 번째 인자는 저장될 키, 두 번쨰 인자는 값입니다.
        // 첨엔 값이 없으므로 키값은 원하는 것으로 하시고 값을 null을 줍니다.
        autoLogin = auto.edit();

        loginId = auto.getString("inputId",null);
        loginPwd = auto.getString("inputPwd",null);


        // adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,array_id);
        //list=(ListView)findViewById(R.id.list);
        //list.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        String id = auto.getString("inputId","");
        String pwd = auto.getString("inputPwd","");
        if(loginId !=null && loginPwd != null) {
            if(loginId.equals(id) && loginPwd.equals(pwd)) {
                Toast.makeText(JoinActivity.this, id +"님 자동로그인 입니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                intent.putExtra("id", id);
                setResult(RESULT_OK,intent);
                finish();
            }
        }
    }

    Handler xmlHandler= new  Handler(){

        public void handleMessage(Message msg){
            if (msg.what==0){
                for ( int  i=0; i<array_id.size();i++){
                    if(array_id.get(i). toString (). equals (id_input.getText(). toString ())){

                        if(loginId == null && loginPwd == null) {

                            autoLogin.putString("inputId", id_input.getText().toString());
                            autoLogin.putString("inputPwd", password_input.getText().toString());

                            //꼭 commit()을 해줘야 값이 저장됩니다
                            autoLogin.commit();

                            String id = auto.getString("inputId","");

                            Intent join_intent = new Intent(getApplicationContext(), MainActivity.class);
                            join_intent.putExtra("id", id);
                            setResult(RESULT_OK, join_intent);
                            finish();


                            break;  //로그인이 성공하면 탈출
                        }
                    }
                    else{

                    }
                }
            } else if (msg.what==1){  //ID 입력하기
                //Log.d(TAG,"array_id"+array_id.get(0).toString());
                if (array_id.get(0).toString (). equals ( "1" )){  // php로 mysql에 ID와 PASSWOR 입력이 성공하면 1을 반환 하고 그 값을 xml에 저장한걸 검사.
                    //check.setText( "ID_INPUT success" );
                }else {
                    //check.setText( "ID_INPUT fail" );
                }
            }

            array_id.clear();  //저장된 id를 모두다 지워야 한다.
            id_input.setText( "" );  //편리하게 EditText를 리셋
            password_input.setText( "" );
        }

    };


    class clickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            switch(v.getId()){

                case R.id.checkBtn:
                    if(id_input.getText().toString().equals("") || password_input.getText().toString().equals("")){
                        //check.setText("login fail");
                        //EditText가 비워있으면 null 오류가 뜨기 때문에 리턴해줘서 다시 입력하게 해야한다.
                        return;
                    }

                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try{
                                String ret=""; //xml에서 받아온 TEXT를 임시로 받는 변수
                                String id=id_input.getText().toString(); //사용자가 입력한 id
                                String password=password_input.getText().toString(); //사용자가 입력한 password
                                String rss = SERVER_ADDRESS + "/Login/"; //서버의 rss 파일이 있는 위치 (xml파일)
                                String tagname=""; //xml의 태그네밈을 위한 변수
                                XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); //xmlPullparser를 위한 준비과정.
                                factory.setNamespaceAware(true); //xml의 네임페이스 허용
                                XmlPullParser xpp = factory.newPullParser();
                                URL url = new URL(SERVER_ADDRESS+"/Login/insert.php?"+"id="+id+"&password="+password);
                                url.openStream(); //서버에 있는 php파일에 id와 password를 인자로 입력하는 구문
                                URL server = new URL(rss + "insert.xml"); //php문이 실행이 되면 mysql과 연동되어 그 결과를 xml파일로 생성하는데 그 생성된 파일이 insert.xml 파일
                                InputStream is = server.openStream(); //xml파일연결
                                xpp.setInput(is, "euc-kr"); //엔코딩 방식 설정

                                int eventType = xpp.getEventType(); //이벤트 타입 얻어오기 예르들어 <start> 인지 </start> 인지 구분하기 위한.
                                while (eventType != XmlPullParser.END_DOCUMENT) {  // xml이 끝날때까지 계속 돌린다.
                                    if (eventType == XmlPullParser.START_TAG) {
                                        tagname=xpp.getName();  //태그를 받아온다.
                                    }
                                    else   if (eventType==XmlPullParser.TEXT){
                                        if (tagname. equals ("id")){
                                            ret=xpp.getText(); //id 태그에 해당되는 TEXT를 임시로 저장
                                        }
                                    } else   if (eventType==XmlPullParser.END_TAG){
                                        //태그가 닫히는 부분에서 임시 저장된 TEXT를 Array에 저장한다.
                                        tagname=xpp.getName();
                                        if (tagname. equals ( "id" )){
                                            Log.d(TAG,"run array_id add");
                                            array_id. add (ret);
                                        }
                                    }
                                    eventType = xpp.next();
                                }
                                //check.setText(array_id.get(0).toString());

                            } // try문 끝

                            catch (Exception e){
                                e.getMessage();
                            }  //catch문 끝
                            xmlHandler.sendEmptyMessage(0);
                        }}).start();  //start로 스레드 실행.

                    break ;  //btn스위치 끝

                case  R.id.inputBtn:  //ID를 새로 생성하기 위한 버튼
                    if (id_input.getText(). toString (). equals ( "" ) || password_input.getText(). toString (). equals ( "" )){
                       // check.setText( "ID_INPUT Fail" );
                        //EditText가 비워있으면 null 오류가 뜨기 때문에 리턴해줘서 다시 입력하게 해야한다.
                        return ;
                    }

                    new  Thread( new  Runnable(){
                        @Override
                        public   void  run() {
                            Log.d(TAG,"id_input: thread runnable");
                            try {
                                Log.d(TAG,"id_input : start try");
                                String  ret= "" ;  //xml에서 받아온 TEXT를 임시로 받는 변수
                                String  id=id_input.getText(). toString ();  //사용자가 입력한 id
                                String  password=password_input.getText(). toString ();  //사용자가 입력한 password
                                String  rss = SERVER_ADDRESS + "/Login/" ;  //서버의 rss 파일이 있는 위치 (xml파일)
                                String  tagname= "" ;  //xml의 태그네밈을 위한 변수

                                Log.d(TAG,"id_input: start try2");
                                URL url =  new  URL(SERVER_ADDRESS+ "/Login/input2.php?id="+id+ "&password="+password);

                                try{
                                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                    httpURLConnection.setReadTimeout(5000);
                                    httpURLConnection.setConnectTimeout(5000);// 서버의 사정으로 접속이 지연될 시 timeout 처리가 되도록 설정
                                    httpURLConnection.connect();

                                    int responseStatusCode = httpURLConnection.getResponseCode();
                                    Log.d(TAG, "response code - " + responseStatusCode);

                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  //xmlPullparser를 위한 준비과정.
                                factory.setNamespaceAware( true );  //xml의 네임페이스 허용
                                XmlPullParser xpp = factory.newPullParser();  //서버에 있는 php파일에 id와 password를 인자로 입력하는 구문

                                URL server =  new  URL(rss + "input.xml" );  //php문이 실행이 되면 mysql과 연동되어 그 결과를 xml파일로 생성하는데 그 생성된 파일이 insert.xml 파일
                                InputStream is = server.openStream();  //xml파일연결
                                xpp.setInput(is,  "euc-kr" );  //엔코딩 방식 설정

                                int  eventType = xpp.getEventType();  //이벤트 타입 얻어오기 예르들어 <start> 인지 </start> 인지 구분하기 위한.

                                Log.d(TAG,"id_input: start try4");
                                while (eventType != XmlPullParser.END_DOCUMENT) {  // xml이 끝날때까지 계속 돌린다.
                                    if (eventType == XmlPullParser.START_TAG) {
                                        tagname=xpp.getName();  //태그를 받아온다.
                                    }else if(eventType==XmlPullParser.TEXT){
                                        if (tagname. equals ( "RESULT" )){
                                            ret=xpp.getText();      //RESULT 태그에 해당되는 TEXT를 임시로 저장
                                        }
                                    } else if (eventType==XmlPullParser.END_TAG){
                                        //태그가 닫히는 부분에서 임시 저장된 TEXT를 Array에 저장한다.
                                        tagname=xpp.getName();
                                        if (tagname. equals ( "RESULT" )){
                                            Log.d(TAG,"run array_id add");
                                            array_id.add(ret);
                                        }
                                    }
                                    eventType = xpp.next();
                                }
                               // check.setText(array_id.get(0).toString());
                            } // try문 끝
                            catch (Exception e){
                                e.getMessage();
                            }  //catch문 끝
                            xmlHandler.sendEmptyMessage(1);  //ID_INPUT 처리를 위해 핸들러에게 msg에 1로 보냄
                        }}).start();  //start로 스레드 실행.
                    break ;
            } //스위치 끝

        }

    }

}
