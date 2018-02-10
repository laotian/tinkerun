package com.tinkerun.io;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by laotian on 2018/2/10.
 */

public class ReflectUtils {

    /**
     * 把属于同一class的全部field 从from 复制到 to
     * @param clz
     * @param from
     * @param to
     * @param <T>
     */
    public static <T> void copyFrom(Class<T> clz,T from,T to){
        for(Field field:clz.getDeclaredFields()){
            if((field.getModifiers() & Modifier.FINAL) ==0){
                field.setAccessible(true);
                try {
                    field.set(to,field.get(from));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
