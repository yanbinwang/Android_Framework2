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
package com.yanzhenjie.durban.callback;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.yanzhenjie.durban.model.ExifInfo;

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
public interface BitmapLoadCallback {

    void onSuccessfully(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo);

    void onFailure();

}