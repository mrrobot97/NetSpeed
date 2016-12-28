package me.mrrobot97.netspeed;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by mrrobot on 16/12/28.
 */

public class SharedPreferencesUtils {
    public static String SHARED_FILE_NAME="spfs";


    //sharedpreferences util
    public static void putToSpfs(Context context, String key, Object value){
        SharedPreferences.Editor editor=context.getSharedPreferences(SHARED_FILE_NAME,Context.MODE_PRIVATE).edit();
        if (value instanceof Integer){
            editor.putInt(key, (Integer) value);
        }else if(value instanceof String){
            editor.putString(key, (String) value);
        }else if(value instanceof Boolean){
            editor.putBoolean(key, (Boolean) value);
        }else if(value instanceof Long){
            editor.putLong(key, (Long) value);
        }else if(value instanceof Float){
            editor.putFloat(key, (Float) value);
        }else if(value instanceof Set){
            editor.putStringSet(key, (Set<String>) value);
        }
        editor.apply();
    }

    public static Object getFromSpfs(Context context,String key,Object defVal){
        SharedPreferences spfs=context.getSharedPreferences(SHARED_FILE_NAME,Context.MODE_PRIVATE);
        if (defVal instanceof Integer){
            return spfs.getInt(key, (Integer) defVal);
        }else if(defVal instanceof String){
            return spfs.getString(key, (String) defVal);
        }else if(defVal instanceof Boolean){
            return spfs.getBoolean(key, (Boolean) defVal);
        }else if(defVal instanceof Long){
            return spfs.getLong(key, (Long) defVal);
        }else if(defVal instanceof Float){
            return spfs.getFloat(key, (Float) defVal);
        }else if(defVal instanceof Set){
            return spfs.getStringSet(key, (Set<String>) defVal);
        }
        return null;
    }

    public static void removeInSpfs(Context context,String key){
        SharedPreferences.Editor editor=context.getSharedPreferences(SHARED_FILE_NAME,Context.MODE_PRIVATE).edit();
        editor.remove(key);
        editor.apply();
    }

    public static void clearSpfs(Context context){
        SharedPreferences.Editor editor=context.getSharedPreferences(SHARED_FILE_NAME,Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

}