package com.tinkerun.debug;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.tinkerun.R;
import java.util.List;

/**
 * Created by tianlupan on 2018/2/11.
 */

public class LogActivity extends Activity {

    private ListView log_listView;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        log_listView=(ListView)findViewById(R.id.log_listView);
        readLogs();
    }


    private void readLogs(){
        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("正在加载日志..");
        progressDialog.setCancelable(true);
        progressDialog.show();

        TinkerunLogImpl.getInstance().readAllLogs(new TinkerunLogImpl.ReadCallback() {
            @Override
            public void onGetResult(final List<String> lines) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLogs(lines);
                    }
                });
            }

            @Override
            public void onError(final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LogActivity.this,"读取日志文件时发生错误:"+error,Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    private void setLogs(List<String> logs){
        dismissLoading();
        log_listView.setAdapter(new ArrayAdapter<String>(this,R.layout.item_log,R.id.log_text,logs));
    }

    private void dismissLoading(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoading();
    }
}
