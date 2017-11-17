package com.example.laotian.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void show(View view){

       int a=100;
        Toast.makeText(this,getResources().getText(R.string.zy),Toast.LENGTH_LONG).show();
        System.out.println("yes3332122222");
    }

}
