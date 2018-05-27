package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage>{
    //들어오는 메세지를 관리하는 창
    List<ChatMessage> msgs = new ArrayList<ChatMessage>();
    TextView msgText;
    private LinearLayout singleMessageContainer;

    //레이아웃을 받아옵니다.
    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    //@Override
    public void add(ChatMessage object){
        msgs.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return msgs.size();
    }

    @Override
    public ChatMessage getItem(int index) {
        return (ChatMessage) msgs.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        Log.d("error","오류 발생 ");

        if (row == null) {
            Log.d("not_e","오류가 발생하지 않았습니다. ");
            // inflator를 생성하여, chatting_message.xml을 읽어서 View객체로 생성한다.
            //전체 화면이 아닌 부분화면을 위한 xml레이아웃을 메모리에 객체화 하기 위해서 별도의 인플레이션 객체를 이용
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);// 인플레이터를 얻어온다.
            row = inflater.inflate(R.layout.activity_chat_array_adapter, parent, false);
        }
        singleMessageContainer = (LinearLayout) row.findViewById(R.id.singleMessageContainer);
        // Array List에 들어 있는 채팅 문자열을 읽어
        ChatMessage msg = (ChatMessage) msgs.get(position);
        // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
        msgText = (TextView) row.findViewById(R.id.singleMessage);
        msgText.setText(msg.getMessage());
        msgText.setTextColor(Color.parseColor("#000000"));
        msgText.setBackgroundResource(msg.right ? R.drawable.bubble_a : R.drawable.bubble_b);
        singleMessageContainer.setGravity(msg.right ? Gravity.RIGHT : Gravity.LEFT);

        return row;
    }

}



