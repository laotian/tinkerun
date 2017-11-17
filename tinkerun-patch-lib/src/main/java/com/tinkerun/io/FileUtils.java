package com.tinkerun.io;

import java.io.File;
import java.util.List;

/**
 * Created by tianlupan on 2017/11/17.
 */

public class FileUtils {

    /**
     * 递归查找 dir 中修改时间小于 lastModified
     * @param lastModified
     * @param dir
     * @return
     */
    public static void getOlderThan(long lastModified,File dir,List<File> result){
        System.out.println("getOlder than dir="+dir);
        if(!dir.exists()){
            System.out.println("dir="+dir+" not exits");
            return;
        }
        for(File file:dir.listFiles()){
            if(file.getName().equals("..") || file.getName().equals(".")) continue;
            if(file.isDirectory()){
                getOlderThan(lastModified,file,result);
            }else if(file.lastModified()<lastModified) {
                result.add(file);
            }
        }


    }
 }
