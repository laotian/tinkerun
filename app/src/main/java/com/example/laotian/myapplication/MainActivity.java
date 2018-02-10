package com.example.laotian.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

//    @BindView(R.id.tvName)
//    TextView tvName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public void show(View view){
//        tvName.setText("hello,,you");
       int a=100;
        Toast.makeText(this,getResources().getText(R.string.zy),Toast.LENGTH_LONG).show();
    }

}
