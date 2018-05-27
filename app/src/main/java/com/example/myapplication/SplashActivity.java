package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by 조하영 on 2018-05-10.
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(2000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}
