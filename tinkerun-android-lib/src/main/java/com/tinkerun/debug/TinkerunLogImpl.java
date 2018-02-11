/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tinkerun.debug;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import com.tencent.tinker.lib.util.TinkerLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by laotian on 18/2/11
 * 收集Tinker/Tinkerun日志
 */
public class TinkerunLogImpl implements TinkerLog.TinkerLogImp {
    private static final String TAG = "Tinkerun";

    public static final int LEVEL_VERBOSE = 0;
    public static final int LEVEL_DEBUG   = 1;
    public static final int LEVEL_INFO    = 2;
    public static final int LEVEL_WARNING = 3;
    public static final int LEVEL_ERROR   = 4;
    public static final int LEVEL_NONE    = 5;
    private static int level = LEVEL_VERBOSE;
    private static final String logFileName="tinkerun.log";
    private static final int MESSAGE_LOG_APPEND=0x1;
    private static final int MESSAGE_READ=0x2;
    private static TinkerunLogImpl sInstance=new TinkerunLogImpl();
    private HandlerThread mHandlerThread=new HandlerThread("log-thread");
    private Handler mHandler;
    private  File logFile=null;
    private PrintStream printStream;

    public interface ReadCallback{
        void onGetResult(List<String> lines);
        void  onError(String error);
    }

    private TinkerunLogImpl(){
        mHandlerThread.start();
        mHandler=new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MESSAGE_LOG_APPEND:
                        appendLog((String)msg.obj);
                        break;
                    case MESSAGE_READ:
                        sendReadResult((ReadCallback)msg.obj);
                        break;
                }
            }
        };
    }

    private void sendReadResult(ReadCallback readCallback){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            List<String> lines=new ArrayList<>();
            while((line=reader.readLine())!=null){
                lines.add(line);
            }
            readCallback.onGetResult(lines);
        } catch (IOException e) {
            e.printStackTrace();
            readCallback.onError(e.getMessage());
        }

    }

    private void appendLog(String log){
        if(printStream!=null) {
            printStream.println(log);
        }
        Log.d(TAG,log);
    }

    public static TinkerunLogImpl getInstance(){
        return sInstance;
    }

    public void init(Context context){
        logFile=new File(context.getCacheDir(),logFileName);
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            printStream = new PrintStream(new FileOutputStream(logFile, true));
        }catch (IOException ex){
            printStream=null;
        }
    }

      @Override
    public void v(String s, String s1, Object... objects) {
        if (level <= LEVEL_VERBOSE) {
            final String log = objects == null ? s1 : String.format(s1, objects);
            log(s,log);
        }
    }

    @Override
    public void i(String s, String s1, Object... objects) {
        if (level <= LEVEL_INFO) {
            final String log = objects == null ? s1 : String.format(s1, objects);
            log(s,log);
        }
    }

    @Override
    public void w(String s, String s1, Object... objects) {
        if (level <= LEVEL_WARNING) {
            final String log = objects == null ? s1 : String.format(s1, objects);
            log(s, log);
        }
    }

    @Override
    public void d(String s, String s1, Object... objects) {
        if (level <= LEVEL_DEBUG) {
            final String log = objects == null ? s1 : String.format(s1, objects);
            log(s, log);
        }
    }

    @Override
    public void e(String s, String s1, Object... objects) {
        if (level <= LEVEL_ERROR) {
            final String log = objects == null ? s1 : String.format(s1, objects);
            log(s, log);
        }
    }

    @Override
    public void printErrStackTrace(String s, Throwable throwable, String s1, Object... objects) {
        String log = objects == null ? s1 : String.format(s1, objects);
        if (log == null) {
            log = "";
        }
        log = log + "  " + Log.getStackTraceString(throwable);
        log(s, log);
    }

    public void log(String tag,String log){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date= format.format(new Date());
        String formatLog=date+"   "+tag+":"+log;
        mHandler.obtainMessage(MESSAGE_LOG_APPEND,formatLog).sendToTarget();
    }

    public void readAllLogs(ReadCallback callback){
        mHandler.obtainMessage(MESSAGE_READ,callback).sendToTarget();
    }
}
