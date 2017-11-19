package com.tinkerun.io;

import com.tencent.tinker.build.util.TypedValue;
import com.tencent.tinker.build.util.Utils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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


    public enum ZipEntryDiff{
        ADD,DELETE,MODIFY,SAME
    }

    public interface ZipEntryVisitor{
        void onVisit(ZipEntryDiff zipEntryDiff, String entryName, InputStream entryStream);
    }

    /**
     * 检查
     * @param oldApk
     * @param newApk
     * @param zipEntryVisitor
     */
    public static void walkResource(File oldApk, File newApk, HashSet<Pattern> patterns, ZipEntryVisitor zipEntryVisitor){
        ZipFile zipOld,zipNew;
        try{
            zipOld=new ZipFile(oldApk);
            zipNew=new ZipFile(newApk);
            Enumeration enumeration=zipNew.entries();
            while(enumeration.hasMoreElements()){
                ZipEntry zipEntry=(ZipEntry)enumeration.nextElement();
                String entryName=zipEntry.getName();
                if(!zipEntry.isDirectory() && Utils.checkFileInPattern(patterns, entryName)){
                    ZipEntry zipEntryOld=zipOld.getEntry(entryName);
                    if(zipEntryOld==null){
                        zipEntryVisitor.onVisit(ZipEntryDiff.ADD,entryName,zipNew.getInputStream(zipEntry));
                    }else if(zipEntry.getSize()==zipEntryOld.getSize() && zipEntry.getCrc()==zipEntryOld.getCrc()){
                        zipEntryVisitor.onVisit(ZipEntryDiff.SAME,entryName,null);
                    }else{
                        zipEntryVisitor.onVisit(ZipEntryDiff.MODIFY,entryName,zipNew.getInputStream(zipEntry));
                    }
                }
            }
            //ZipEntryDiff.Delete没有检查
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveStream(InputStream inputStream,String filePath,String entryName){
        File file = new File(filePath + File.separator +entryName);

        File parentFile = file.getParentFile();
        if (parentFile != null && (!parentFile.exists())) {
            parentFile.mkdirs();
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis=new BufferedInputStream(inputStream);
        try {
            try {
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos, TypedValue.BUFFER_SIZE);

                byte[] buf = new byte[TypedValue.BUFFER_SIZE];
                int len;
                while ((len = bis.read(buf, 0, TypedValue.BUFFER_SIZE)) != -1) {
                    fos.write(buf, 0, len);
                }
            } finally {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            }
        }catch (IOException ex){

        }
    }



 }
