package com.gornostaev.recognize.file_image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;

//этот класс нужен для работы с Uri ссылками и получения
//картинок по ним
public class ImageGetter {
    private static final String TAG = "imageGetterLogs";
    private Activity mActivity;
    //максимальный размер картинки
    private final int IMAGE_MAX_SIZE = 1920;

    public ImageGetter(Activity activity) {
        mActivity = activity;
    }

    //запуск новой активности для выбора нужного файла-картинки
    //request_code - код для запуска новой активности
    public void performFileSearch(int request_code) {

        //ACTION_GET_CONTENT т.к. нужен не сам файл, а его копия
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        //файл должен быть открываемым
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        //файл должен быть картинкой
        intent.setType("image/*");

        //запускаем новую активность
        mActivity.startActivityForResult(intent, request_code);
    }

    //получаем Bitmap по ссылке URI
    public Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                mActivity.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        //строим Bitmap
        //и уменьшаем размер входной фотографии. это необходимо для адекватной работы
        //Cloud Vision Api, да и работает быстрее и занимает меньше памяти
        Bitmap image = decodeFile(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    //производит масштабирование Bitmap
    public Bitmap scaleBitmap(Bitmap input) {

        int start_width = input.getWidth();
        int start_height = input.getHeight();
        //получаем хороший масштаб, кратный двойке
        int scale = getScale(start_width, start_height);
        //масштабируем
        return Bitmap.createScaledBitmap(input, start_width / scale, start_height / scale, false);
    }

    //умная функция для получения хорошего масштаба картинок
    private int getScale(int startWidth, int startHeight) {
        int scale = 1;
        if (startHeight > IMAGE_MAX_SIZE || startWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(startHeight, startWidth)) / Math.log(0.5)));
        }
        return scale;
    }

    //получаем уменьшенный битмап, используя FileDescriptor
    private Bitmap decodeFile(FileDescriptor f) {
        Bitmap b = null;
        //сначала получаем характеристики изображения, не подгружая его непосрежственно
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(f, null, o);
        //теперь получаем хороший масштаб, полагаясь на полученные параметры изображения
        int scale = getScale(o.outWidth, o.outHeight);
        //...и подгружаем изображение с масштабированными характеристиками
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        b = BitmapFactory.decodeFileDescriptor(f, null, o2);
        return b;
    }

    //функция для получения base64 строки из bitmap
    public String getBase64String(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

}
