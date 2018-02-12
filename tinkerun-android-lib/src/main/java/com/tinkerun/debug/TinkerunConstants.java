package com.tinkerun.debug;

import android.util.Log;

import com.tencent.tinker.loader.shareutil.ShareConstants;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianlupan on 2018/2/12.
 */

public class TinkerunConstants {

    /**
     * 获取加载失败时，errorCode对应的错误码解释，如ERROR_LOAD_DISABLE
     * @param errorCode
     * @return
     */
    public static String getLoadErrorCode(int errorCode){
        return formatList(getCodes(errorCode,"ERROR_LOAD_PATCH_"));
    }

    public static String getPackageCheckErrorCode(int errorCode){
        return formatList(getCodes(errorCode,"ERROR_PACKAGE_"));
    }

    public static String getError(int errorCode,String prefix){
        return formatList(getCodes(errorCode,prefix));
    }

    private  static  <T> String  formatList(List<T> list){
        if(list==null || list.size()==0){
            return "";
        }
        String splitor=",";
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<list.size();i++){
            builder.append(list.get(i));
            if(i<list.size()-1){
               builder.append(splitor);
            }
        }
        return builder.toString();
    }

    private static List<String> getCodes(int value, String fieldPrefix)  {
        List<String> result = new ArrayList<>();
        if (fieldPrefix != null && fieldPrefix.length() > 0) {
            for (Field field : ShareConstants.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (field.getName().startsWith(fieldPrefix)) {
                        try {
                            if(field.getInt(ShareConstants.class)==value){
                                result.add(field.getName());
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return result;
    }

    private static List<String> getCodes(String value, String fieldPrefix)  {
        List<String> result = new ArrayList<>();
        if (fieldPrefix != null && fieldPrefix.length() > 0) {
            for (Field field : ShareConstants.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (field.getName().startsWith(fieldPrefix)) {
                        try {
                            if(value.equals(field.get(ShareConstants.class))){
                                result.add(field.getName());
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return result;
    }


}
