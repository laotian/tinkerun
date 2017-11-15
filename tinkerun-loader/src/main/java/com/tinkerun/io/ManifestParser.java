package com.tinkerun.io;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.Map;

/**
 * 用于解析 AndroidManifest 文件中meta-data标签的工具类
 */
public final class ManifestParser {

    static class MetaNode{
        private String key;
        private Object value;
        public MetaNode(String key, Object value){
            this.key=key;
            this.value=value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    public interface ManifestVisitor{
         boolean accept(MetaNode metaNode);
    }

    public static class MetaStringVisitor implements ManifestVisitor{

        private final String targetValue;
        private final boolean searchByMetaKey;
        private final Map<String,String> targetContainer;
        private final boolean match;

        /**
         * @param targetValue 目标metaNode的value值
         * @param targetContainer 所有和targetValue对应的key，存放到的容器
         * @param searchByMetaKey 如果选是，targetValue为meta-data 的key值，否则为value值
         * @param match 使用正则匹配方式
         */
        public MetaStringVisitor(String targetValue, Map<String,String> targetContainer, boolean searchByMetaKey, boolean match){
            this.targetValue=targetValue;
            this.targetContainer=targetContainer;
            this.searchByMetaKey=searchByMetaKey;
            this.match=match;
        }

        private boolean isTarget(String value){
            if(match){
                return value.matches(targetValue);
            }else{
                return targetValue.equals(value);
            }
        }

        @Override
        public final boolean accept(MetaNode metaNode) {
             String key=metaNode.getKey();
             Object value= metaNode.getValue();
            if(value instanceof String) {
                String manifestValue=(String)value;
                if (searchByMetaKey && isTarget(key)) {
                    if (acceptNode(key,manifestValue)) {
                        return true;
                    }
                } else if (!searchByMetaKey && isTarget(manifestValue)) {
                    if (acceptNode(key,manifestValue)) {
                        return true;
                    }
                }
            }
            return false;
        }

        protected boolean acceptNode(String key, String value){
            targetContainer.put(key,value);
            return false;
        }
    }

    public static void parse(Context context, ManifestVisitor manifestVisitor){
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                for (String key : appInfo.metaData.keySet()) {
                    Object value=appInfo.metaData.get(key);
                    MetaNode metaNode=new MetaNode(key,value);
                    if(manifestVisitor.accept(metaNode)) break;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Unable to find metadata", e);
        }
    }

}
