package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RegActivity extends AppCompatActivity {

    String etSearch;
    Button bSearch,bRegister,bBack;
    TextView tvResult,vetName;
    String fdate,name;
    String userName;
    String growth_start;

    //ScrollView scroll; //스크롤바 사용-> layout에서만 해도 되긴 하는데 이상함... 수정요함.
    public static final int REQUST_CODE_MENU2=101;

    //캘린더에 등록 여부를 묻는다.
    boolean today_register;

    //서버 통신을 위한 변수들 --> 연결하고자 하는 파일?
    private static final String TAG = "RegActivity";

    User app_user;

    //요청 url
    String url = "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com/Vetgetables/byVeg.php";
    String getAllVeNames_url = "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com/Vetgetables/getAllVeNames.php";
    String registerVegetable_url = "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com/Vetgetables/registerVegetable.php?name=";
    String registerUserveg_url = "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com/Vetgetables/insertRecord.php?name=";
    String existUserInfor = "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com/Vetgetables/existUserInfor.php?name=";

    //선택 상황을 위한 flag
    int flag  = 1;
    private static final String TAG_JSON="webnautes";
    private static final String TAG_NAME = "name";
    private static final String TAG_GROWTH_DAY = "growth-day";
    private static final String TAG_INFOR ="infor";

    //showresult 영역을 위한 전역 변수
    String mJsonString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        bSearch=(Button)findViewById(R.id.button1);
        bRegister=(Button)findViewById(R.id.button2);
        vetName=(TextView)findViewById(R.id.vet_name);

        //Main에서 전송한 객체를 전달받아 표시하도록 설정
        Intent intent = getIntent();
        app_user = (User)intent.getSerializableExtra("app_user");
        userName = app_user.getId();

        //등록된 fruit 정보의 유/무에 따라 처리를 다르게 할 것
        //이미 등록된 정보가 존재한다면
        if(app_user.getFruit()!=null){
            vetName.setText(app_user.getFruit());
            flag=3;
            Log.d("tasg","실행");
            GetData task_existUserInfor = new GetData();
            task_existUserInfor.execute(existUserInfor+app_user.getFruit());
        }

        //오늘 날짜를 받아서 저장한다.
        Date today_date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        growth_start = sdf.format(today_date);

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vetName!=null){
                    GetData task_registerUserveg= new GetData();
                    task_registerUserveg.execute(registerUserveg_url+vetName.getText().toString()+"&userid="+userName+"&growth_start="+growth_start);
                    showMessage_register();
                }
                else{
                    showMessage_information();
                }
            }
        });

        bBack=(Button)findViewById(R.id.backBtn);
        bBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvResult=(TextView)findViewById(R.id.textView);
        tvResult.setMovementMethod(new ScrollingMovementMethod());

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogRadio();
            }
        });
    }

    /**정보 유지 영역**/
    @Override
    protected void onPause() {
        super.onPause();
        //saveState();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //restoreState();
        //만약 정보가 리셋되어있다면 텍스트 역시 리셋
        if(app_user.getFruit()==null){

            vetName.setText("None");
            tvResult.setText("");
        }

    }

    protected void saveState(){
        SharedPreferences pref=getSharedPreferences("pref",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        editor.putString("searchFruit",vetName.getText().toString());
        editor.putString("searchInfor",tvResult.getText().toString());
        editor.commit();
    }

    protected void restoreState(){
        SharedPreferences pref=getSharedPreferences("pref",Activity.MODE_PRIVATE);
        if((pref!=null)&&(pref.contains("searchFruit"))&&(pref.contains("searchInfor"))){
            String searchFruit=pref.getString("searchFruit","");
            vetName.setText(searchFruit);
            String searchInfor=pref.getString("searchInfor","");
            tvResult.setText(searchInfor);
        }
    }


    //체크박스로 구성된 채소 선택
    private void DialogRadio(){
        //서버에 연결을 한 후 에 그냥 배열만 보낼까..? 아니면...?
        flag=1;
        GetData task_getAllVeNames = new GetData();
        task_getAllVeNames.execute(getAllVeNames_url);
    }

    /**다이얼로그 안내 부분 **/
    private void showMessage() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("안내");
        alertDialogBuilder
                .setMessage("채소를 선택해주세요")
                .setIcon(android.R.drawable.ic_dialog_alert);

        alertDialogBuilder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogRadio();
            }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

    }

    private void showMessage_information() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("안내");
        alertDialogBuilder
                .setMessage("채소 먼저 등록해주세요")
                .setIcon(android.R.drawable.ic_dialog_alert);

        alertDialogBuilder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogRadio();
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    //작물의 바로 등록 여부를 선택할 수 있도록
    private void showMessage_register(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("안내");
        alertDialogBuilder
                .setMessage("재배를 오늘부터 시작하시겠습니까?")
                .setIcon(android.R.drawable.ic_dialog_alert);

        //확인 버튼을 누르면 일정이 바로 캘린더에 적용되도록 함
        alertDialogBuilder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                app_user.setToday_register(true);
                finish_intent();
            }
        });

        //취소 버튼을 누르면 일정은 수동으로 적용하도록 함
        alertDialogBuilder.setNegativeButton("다음에", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                app_user.setToday_register(false);
                finish_intent();
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    //인텐트_finish 부분 함수로 설정
    public void finish_intent(){
        //Calendar,Main에 내용을 보낸다.
        Intent reg_intent=new Intent(getApplicationContext(),MainActivity.class);

        reg_intent.putExtra("date",fdate);
        reg_intent.putExtra("fruit",name);
        reg_intent.putExtra("reg",today_register);

        setResult(RESULT_OK,reg_intent);

        //토마토 설정후 토마토를 띄우게 하는 부분 --> 미해결 부분
        bSearch.setHint(bSearch.getText().toString());
        finish();
    }

    protected void clear(){
        SharedPreferences pref = getSharedPreferences("pref",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        editor.clear();
        editor.commit();
    }

    /**서버 연결 관련 부분 코드 **/

    //서버에서 데이터 가져 오는 코드
    private class GetData extends AsyncTask<String,Integer,String>{

        ProgressDialog progressDialog;
        String errorString = null;

        //백그라운드 작업 시작 전에 UI 작업을 진행한다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(RegActivity.this,"Please Wait",null,true,true);
        }

        //백그라운드 작업이 끝난 후 UI 작업
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss(); //작업이 시작하면 화면을 종료한다.

            if(s == null){
                tvResult.setText(errorString);
            }else{
                mJsonString = s;

                if(flag == 1){
                    showAllVeNames();
                }else if(flag == 2){
                    showSearchVegetable();
                }else if(flag ==3){
                    showExistUserInfor();
                }
            }
        }

        //백그라운드 작업을 진행한다. --> 제일 먼저 진행됨
        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];

            try{
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);// 서버의 사정으로 접속이 지연될 시 timeout 처리가 되도록 설정
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;

                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            }catch(Exception e){
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();
                return null;
            }
        }
    }

    private void showAllVeNames(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            String[] ve_names= new String[jsonArray.length()];

            for(int i=0;i<jsonArray.length();i++){
                JSONObject item = jsonArray.getJSONObject(i);
                ve_names[i] = item.getString(TAG_NAME);
                Log.d(TAG,ve_names[i]);
            }

            final CharSequence[] Vegetables = ve_names;
            final AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);

            alt_bld.setIcon(android.R.drawable.ic_dialog_info);
            alt_bld.setTitle("Select a Vegetables"); //타이틀

            // 리스트 버튼 눌렀을 때
            alt_bld.setSingleChoiceItems(Vegetables, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    etSearch=Vegetables[item].toString(); //선택한 아이템을 등록한다.
                }
            });

            //확인 버튼 눌렀을 때
            alt_bld.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //클릭했을 때 아무것도 클릭안했으면
                    flag=2;
                    if(etSearch!=null){
                        //해당 스트링을 받아온다.
                        String selectVe_name = etSearch;

                        //해당 스트링을  name = ''에 합쳐 get 방식으로 보낸다.
                        //테스크 객체 생성 후 보낸다.
                        GetData task_registerVegetable = new GetData();
                        task_registerVegetable.execute(registerVegetable_url+selectVe_name);

                        vetName.setText(etSearch);
                    }else{
                        showMessage();
                    }
                }
            });

            final AlertDialog alert = alt_bld.create();
            alert.show();

        } catch (JSONException e) {
            Log.d(TAG, "showAllVeNames : ", e);
        }
    }

    private void showSearchVegetable() {
        try {
            Log.d(TAG,"showSerachVegetable:"+mJsonString);
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String ve_name = item.getString(TAG_NAME);
                int growth_day = item.getInt(TAG_GROWTH_DAY);
                String infor = item.getString(TAG_INFOR);

                Log.d(TAG,"name"+name+"growth_day:"+growth_day);

                this.name = ve_name;
                fdate=""+growth_day;
                tvResult.setText(infor);
            }

        } catch (JSONException e) {
            Log.d(TAG, "showAllVeNames : ", e);
        }
    }

    private void registeruserVegetable(){
        try {
            Log.d(TAG,"registeruserVegetable:"+mJsonString);
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String ve_name = item.getString(TAG_NAME);
                int growth_day = item.getInt(TAG_GROWTH_DAY);
                String infor = item.getString(TAG_INFOR);

                Log.d(TAG,"name"+name+"growth_day:"+growth_day);

                this.name = ve_name;
                fdate=""+growth_day;
                tvResult.setText(infor);
            }

        } catch (JSONException e) {
            Log.d(TAG, "showAllVeNames : ", e);
        }
    }

    public void showExistUserInfor(){
        try{
        Log.d(TAG,"existUserInfor"+mJsonString);
        JSONObject jsonObject = new JSONObject(mJsonString);
        tvResult.setText(jsonObject.getString(TAG_JSON));

        }catch(JSONException e){

        }
    }

}
