package com.kaltura.playkit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @hide
 */

public class Utils {
    private static final String TAG = "Utils";

    public static String readAssetToString(Context context, String asset) {
        try {
            InputStream assetStream = context.getAssets().open(asset);
            return fullyReadInputStream(assetStream, 1024 * 1024).toString();
        } catch (IOException e) {
            Log.e(TAG, "Failed reading asset " + asset, e);
            return null;
        }
    }

    @NonNull
    public static ByteArrayOutputStream fullyReadInputStream(InputStream inputStream, int byteLimit) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte data[] = new byte[1024];
        int count;

        while ((count = inputStream.read(data)) != -1) {
            int maxCount = byteLimit - bos.size();
            if (count > maxCount) {
                bos.write(data, 0, maxCount);
                break;
            } else {
                bos.write(data, 0, count);
            }
        }
        bos.flush();
        bos.close();
        inputStream.close();
        return bos;
    }

    public static boolean isNullOrEmpty(final Collection<?> c) {
        return c == null || c.isEmpty();
    }


    public static <E extends Enum<E>> E byValue(Class<E> EClass, String enumValue) {
        return byValue(EClass, enumValue, null);
    }

    public static <E extends Enum<E>> E byValue(Class<E> EClass, String enumValue, E defaultOption) {

        try {
            return enumValue != null ? Enum.valueOf(EClass, enumValue) : defaultOption;

        } catch (IllegalArgumentException | NullPointerException e) {
            return defaultOption;
        }
    }

    public static Bundle mapToBundle(Map<String, ? extends Serializable> input) {
        Bundle output = new Bundle();
        for (String key : input.keySet()) {
            output.putSerializable(key, input.get(key));
        }
        return output;
    }

    public static <T extends Serializable> Map<String, T> bundleToMap(Bundle input, Class<T> c) {
        Map<String, T> output = new HashMap<String, T>();
        for (String key : input.keySet()) {
            output.put(key, c.cast(input.getParcelable(key)));
        }
        return output;
    }

}
