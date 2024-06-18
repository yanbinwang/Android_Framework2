/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.durban.util;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.yanzhenjie.durban.error.StorageError;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Create by Yan Zhenjie on 2017/5/23.
 */
public class FileUtils {

    private static Random random = new Random();

    private FileUtils() {
    }

    public static void validateDirectory(String path) throws StorageError {
        File file = new File(path);
        try {
            if (file.isFile())
                file.delete();
            if (!file.exists())
                file.mkdirs();
        } catch (Exception e) {
            throw new StorageError("Directory creation failed.");
        }
    }

    public static String randomImageName(Bitmap.CompressFormat format) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmSSS", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate) + random.nextInt(9000) + "." + format;
    }

    public static void copyFile(@NonNull String pathFrom, @NonNull String pathTo) throws StorageError {
        try {
            FileInputStream inputStream = new FileInputStream(pathFrom);
            FileOutputStream outputStream = new FileOutputStream(pathTo);
            int len;
            byte[] buffer = new byte[2048];
            while ((len = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, len);
        } catch (IOException e) {
            throw new StorageError(e);
        }
    }

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        }
    }

}
