package com.com.clone_spotify.service;

import android.graphics.Bitmap;

public interface InitCallback {
    //notification에 띄울 bitmap 이미지 만들기
    void getBitmap(Bitmap bitmap);
}
