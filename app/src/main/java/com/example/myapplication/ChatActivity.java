package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class ChatActivity extends AppCompatActivity implements AIListener{
    AIService aiService;
    //TextView chatView;
    ListView listView;
    public static final int RECORD_REQUST_CODE =101;
    boolean side =true;

    //채팅 내용을 표시하는 어댑터
    ChatArrayAdapter chatArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();

        setContentView(R.layout.activity_chat); // 인플레이션

        chatArrayAdapter = new ChatArrayAdapter(this.getApplicationContext(),R.layout.activity_chat_array_adapter);
        listView = (ListView)findViewById(R.id.chatlistView);
        listView.setAdapter(chatArrayAdapter);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if(permission != PackageManager.PERMISSION_GRANTED){
           // Log.i(TAG,"Permission to record denied");
            makeRequest();
        }

        final AIConfiguration config = new AIConfiguration("facfb8939d3f44639522f5112e9beab4",
                AIConfiguration.SupportedLanguages.Korean,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this,config);
        aiService.setListener(this);

        //채팅 내용 띄우기
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount()-1);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RECORD_REQUST_CODE:{
                if(grantResults.length ==0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                  Log.i("user_denied","Permisson has been denied by user");
                }else{
                    Log.i("user_denied","Permisson has been granted by user");
                }
            }
        }
    }

    protected void makeRequest(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},RECORD_REQUST_CODE);
    }

    public void buttonClicked(View view){
        aiService.startListening();
    }

    @Override
    public void onResult(AIResponse result) {
        Log.d("anu", result.toString());
        //Result result1 = result.getResult();
        //chatView.setText("Query"+result1.getResolvedQuery()+" action:"+result1.getAction());

        Result result1 = result.getResult();
        String parameterString = "";
        if (result1.getParameters() != null && !result1.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result1.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        // Show results in TextView.
//        chatView.setText("Query:" + result1.getResolvedQuery() +
//                "\nAction: " + result1.getFulfillment().getSpeech()+
//                "\nParameters: " + parameterString);


        Log.d("test","result는 발생했습니다.");
        sendChatMessage(result1.getResolvedQuery());
        sendChatMessage(result1.getFulfillment().getSpeech());
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    private boolean sendChatMessage(String meg){
        chatArrayAdapter.add(new ChatMessage(side,meg));
        side = !side;
        return true;
    }

}


