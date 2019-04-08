package com.gornostaev.recognize;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gornostaev.recognize.connection.ConnectivityStatus;
import com.gornostaev.recognize.connection.NetworkService;
import com.gornostaev.recognize.connection.request.FullRequest;
import com.gornostaev.recognize.connection.response.FullResponse;
import com.gornostaev.recognize.file_image.ImageGetter;
import com.gornostaev.recognize.label_list.ContainerImageLabels;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private LinearLayout url_elements_holder;
    private LinearLayout img_holder;
    private EditText edt_link;
    private Button btn_see_results;
    //private ImageButton btn_link_clear, btn_link_insert;
    private ImageView img_input;
    private ImageGetter mImageGetter;
    private TextView txt_loading;
    private TextView txt_no_internet;
    private static final int READ_REQUEST_CODE = 42;
    private String imgToAnalyze_base64;
    //эта переменная нужна для работы с буфером обмена
    private ClipboardManager mClipBoardManager;
    private ConnectivityStatus connectivityStatus;
    private static final String TAG = "myLogs";
    //API_KEY находится в gradle.properties, в целях
    //безопасности этот файл был скрыт из контроля версий
    private static final String API_KEY = BuildConfig.API_KEY;
    private TextView txtSomeInfo;
    //храним экземпляр, чтобы не инициализировать новые задачи
    //при повторном нажатии кнопки
    private SendRequestTask sendRequestTask = null;
    //так же и с GetImageTask
    private GetImageTask getImageTask = null;
    //переменная для хранения результата запроса в Cloud Vision API
    //т.е. очки и названия
    private ContainerImageLabels containerImageLabels;

    private void setElementsThatAreKept() {
        //получаем сервис clipboard
        mClipBoardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        txt_no_internet = (TextView) findViewById(R.id.main_txt_no_internet);
        txt_loading = (TextView) findViewById(R.id.main_txt_loading);
        edt_link = (EditText) findViewById(R.id.main_edt_url);
        url_elements_holder = (LinearLayout) findViewById(R.id.main_url_elements_holder);
        img_holder = findViewById(R.id.main_img_holder);
        //настройка кнопки ввода и поиска картинки по ссылке
        img_input = (ImageView) findViewById(R.id.main_img_input);
        btn_see_results = (Button) findViewById(R.id.main_btn_see_results);
        btn_see_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLabelActivity(containerImageLabels);
            }
        });
    }

    private void setBtnOnDeviceFind() {
        Button btn_find = (Button) findViewById(R.id.main_btn_file);
        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //скрываем элементы для ссылочной загрузки картинки
                updateUrlVisibility(false);

                //запускаем активность Storage Access Framework для поиска
                //нужного файла и передаем код запуска
                mImageGetter.performFileSearch(READ_REQUEST_CODE);
            }
        });
    }

    //настройка кнопки открытия пункта меню с вводом ссылки и прочих элементов,
    //выпадающих при выборе способа загрузки изображения "Ссылка"
    private void setBtnsUrlInput() {
        Button btn_link = (Button) findViewById(R.id.main_btn_url);
        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //скрываем элементы для ссылочного поиска, если они уже были показаны
                //если скрыты, то показываем
                updateUrlVisibility(url_elements_holder.getVisibility() != View.VISIBLE);
            }
        });
        //очистка поля ввода
        ImageButton btn_link_clear = (ImageButton) findViewById(R.id.main_btn_uri_clear);
        btn_link_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //очищаем поле ввода при нажатии на кнопку "стереть"
                edt_link.setText("");
            }
        });
        //вставить в поле ввода из буффера обмена
        ImageButton btn_link_insert = (ImageButton) findViewById(R.id.main_btn_uri_copy);
        btn_link_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mClipBoardManager.hasPrimaryClip()) {
                    Snackbar.make(edt_link, R.string.buffer_empty, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    return;
                }
                //получаем данные из буфера обмена
                ClipData clipData = mClipBoardManager.getPrimaryClip();
                //устанавливаем на ввод текст из буфера обмена
                edt_link.setText(clipData.getItemAt(0).getText().toString());
            }
        });
        Button btn_link_enter = (Button) findViewById(R.id.main_btn_uri_apply);
        btn_link_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = edt_link.getText().toString();
                //проверяем интернет
                connectivityStatus.updateConnection();
                if (url.equals("")) {
                    Snackbar.make(edt_link, R.string.empty_request, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else
                    //если соединение считается активным - отправляем запрос на получение картинки
                    if (connectivityStatus.isConnectionActive())
                        getImageAtUrl(url);
            }
        });
    }

    //кнопка отправки запроса в CloudApi
    private void setBtnCloudApiRequest() {
        txtSomeInfo = (TextView) findViewById(R.id.some_additional_info);
        txtSomeInfo.setText("");
        Button btn_send_request = (Button) findViewById(R.id.main_btn_send_request);
        //устанавливаем слушатель, отвечающий за действия при нажатии на кнопку обработки
        btn_send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //создаем запрос
                if (sendRequestTask != null)
                    return;
                //если соединение не считается активным, то просто
                //уходим отсюда, т.к. все действия по уведомлению пользователя
                //об интернете уже должны были быть произведены
                if (!connectivityStatus.isConnectionActive()) {
                    return;
                }
                if (imgToAnalyze_base64 == null) {
                    return;
                }
                sendRequestTask = new SendRequestTask();
                sendRequestTask.execute(imgToAnalyze_base64);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //настраиваем все элементы, которые хранятся
        setElementsThatAreKept();

        //настриваем кнопку "найти на устройстве" и её слушателя
        setBtnOnDeviceFind();
        setBtnsUrlInput();
        setBtnCloudApiRequest();
        //настраиваем ImageGetter, нужен для получения картинок и простой работы с ними
        mImageGetter = new ImageGetter(this);
        imgToAnalyze_base64 = null;
        containerImageLabels = null;
        setListenerForInternet();
        updateUrlVisibility(false);
    }

    private void setListenerForInternet() {
        //заготавливаем слушатель для обратной связи и получения уведомлений
        //об изменении состояния интернета
        ConnectivityStatus.OnInternetStateChangeListener listener = new
                ConnectivityStatus.OnInternetStateChangeListener() {
                    @Override
                    public void onChange(final boolean connectionIsActive) {
                        //запуск в главном потоке необходим, т.к. иначе выдается ошибка
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setInternetState(connectionIsActive);
                            }
                        });
                    }
                };
        //устанавливаем слушатель состояния интернета
        connectivityStatus = new ConnectivityStatus(this, listener);
        //регистрируем колбэк
        connectivityStatus.performRegisterCallback();
    }

    //отображает нужное сообщение о состоянии интернета в зависимости
    //от его состояния
    private void setInternetState(boolean connectionIsActive) {
        if (connectionIsActive) {
            //если соединение оказалось активным - убираем
            //надпись про проблемы с соединением
            showNoInternet(false);
        } else {
            //если все наоборот, пишем, что все плохо
            showNoInternet(true);
        }
    }

    //скрывает или показывает элементы, связанные с загрузкой картинки из ссылки
    private void updateUrlVisibility(boolean setVisible) {
        url_elements_holder.setVisibility(setVisible ? View.VISIBLE : View.GONE);
    }

    //фунция для старта новой активности
    //принимает полученные данные от Cloud Vision API
    private void startLabelActivity(ContainerImageLabels containerImageLabels) {
        Intent intent = new Intent(getBaseContext(), LabelScoreActivity.class);
        Bundle data = new Bundle();
        data.putParcelable("image_labels", containerImageLabels);
        intent.putExtras(data);
        startActivity(intent);
    }

    //загружает картинку и показывает её
    public void loadImage(Bitmap image) {
        img_input.setImageBitmap(image);

    }

    public void showNoInternet(boolean isNoInternet) {
        txt_no_internet.setVisibility(isNoInternet ? View.VISIBLE : View.GONE);
    }

    public void showLoading(boolean isLoading) {
        txt_loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    public void showImage(boolean isShown) {
        // img_input.setVisibility(isShown ? View.VISIBLE : View.GONE);
        img_holder.setVisibility(isShown ? View.VISIBLE : View.GONE);
    }

    private void showResults(boolean isShown) {
        btn_see_results.setVisibility(isShown ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        //сравниваем код запроса с нашим
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // заранее объявляем uri
            Uri uri = null;
            //если вернувшиеся данные есть
            if (resultData != null) {
                //получаем uri
                uri = resultData.getData();
                if (getImageTask != null) {
                    //убираем старую задачу (вдруг кто-то такой быстрый
                    //что успел нажать кнопку нового поиска картинки
                    //до того, как старая задача успела завершиться?)
                    getImageTask.cancel(true);
                    getImageTask = new GetImageTask();
                } else {
                    getImageTask = new GetImageTask();
                }
                //отдаем задачу обновления картинки AsyncTask'у
                getImageTask.execute(uri);
            }
        }
    }

    //эта вещь нужна для работы с Picasso
    //формирует запрос на асинхронный поиск картинки по указанной ссылке
    com.squareup.picasso.Target mTarget = new com.squareup.picasso.Target() {
        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            showLoading(false);
            showImage(true);
            Bitmap img_to_analyze = mImageGetter.scaleBitmap(bitmap);
            imgToAnalyze_base64 = mImageGetter.getBase64String(img_to_analyze);
            img_input.setImageBitmap(img_to_analyze);
            //скрываем кнопку "увидеть результаты", т.к. картинка новая
            //а пользователь еще не нажал кнопку анализа
            showResults(false);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            Log.d(TAG, "Image not loaded from URL " + e.toString());
            if (!connectivityStatus.isConnectionActive()) {
                showNoInternet(true);
            } else {
                Snackbar.make(edt_link, R.string.picture_not_found, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                showLoading(false);
            }

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            showLoading(true);
        }
    };


    private void getImageAtUrl(String url) {
        Picasso.get()
                .load(url)
                .into(mTarget);

    }


    //эта задача нужна для формирования запроса в Google Cloud Vision API
    //и последующая его передача асинхронной функции Retrofit
    private class SendRequestTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onProgressUpdate(Integer... values) {
            showLoading(true);
        }

        @Override
        protected void onPostExecute(Boolean fullResponseCall) {
            //очищаем ссылку на асинхронную задачу, чтобы можно было создать новую
            sendRequestTask = null;
            //не делаем showLoading(false), т.к. в doInBackground
            //отправили запрос, который при возвращении вызовет onResponse или onFailure
            //где будет убран символ загрузки
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            publishProgress();
            //получаем изображение в base64
            String base64 = strings[0];
            //получаем полный java файл для формирования json запроса
            FullRequest fullRequest = FullRequest.buildFullRequest(base64);
            NetworkService.getInstance()
                    .getCloudVisionApi()
                    .postData(API_KEY, fullRequest).enqueue(new Callback<FullResponse>() {
                @Override
                public void onResponse(@NonNull Call<FullResponse> call, @NonNull Response<FullResponse> response) {
                    FullResponse fullResponse = response.body();
                    getResponseAndSave(fullResponse);
                }

                @Override
                public void onFailure(@NonNull Call<FullResponse> call, @NonNull Throwable t) {
                    txtSomeInfo.setText("");
                    showLoading(false);
                    t.printStackTrace();
                }
            });
            return false;
        }

        //получаем fullResponse и производим с ним все необходимые действия:
        //переводим его в наш класс и передаем этот класс в новую активность
        protected void getResponseAndSave(FullResponse fullResponse) {
            if (fullResponse == null) {
                txtSomeInfo.setText(R.string.cloud_api_error);
                showLoading(false);
                return;
            }
            //программа попадает в эту область кода если все вызовы завершены успешно
            //значит результат гарантированно есть и можно показать кнопку
            //результатов
            showResults(true);
            //создаем массив контейнеров метки и очков
            containerImageLabels =
                    new ContainerImageLabels(fullResponse);
            txtSomeInfo.setText("");
            //стартуем новую активность с результатами
            showLoading(false);
            startLabelActivity(containerImageLabels);
        }
    }

    //нужен для передачи результата из АсинкТаска после его выполнения
    private class OutputContainer {
        Bitmap image;
        String base64;

        public Bitmap getImage() {
            return image;
        }

        public OutputContainer setImage(Bitmap image) {
            this.image = image;
            return this;
        }

        public String getBase64() {
            return base64;
        }

        public OutputContainer setBase64(String base64) {
            this.base64 = base64;
            return this;
        }
    }

    //AsyncTask для получения картинки по uri
    private class GetImageTask extends AsyncTask<Uri, Integer, OutputContainer> {
        @Override
        protected void onProgressUpdate(Integer... progress) {
            //устанавливаем текст загрузки
            showLoading(true);
            showImage(false);
        }

        //при получении картинки вставляем её
        @Override
        protected void onPostExecute(OutputContainer result) {
            if (result != null) {
                Log.d(TAG, "Image found, trying to paint...");
                showImage(true);
                showLoading(false);
                //показываем картинку
                loadImage(result.getImage());
                imgToAnalyze_base64 = result.getBase64();
                Log.d(TAG, "base64: " + imgToAnalyze_base64.substring(0, 130));
                showResults(false);
            }
            //очищаем ссылку на выполненную задачу
            getImageTask = null;
        }


        //передаем Uri на  картинку, находим картинку и отдаем её
        @Override
        protected OutputContainer doInBackground(Uri... parameter) {
            Uri uri = parameter[0];
            return getFullImageInfo(uri);
        }

        //возвращает обработанный Bitmap и его base64 код
        protected OutputContainer getFullImageInfo(Uri uri) {
            Log.d(TAG, "Uri: " + uri.toString());
            //публикуем некоторый прогресс
            publishProgress();
            Bitmap image = null;
            String base64 = null;
            try {
                //получаем картинку Bitmap
                image = mImageGetter.getBitmapFromUri(uri);
                base64 = mImageGetter.getBase64String(image);
            } catch (IOException e) {
                Log.d(TAG, "Image was not painted");
            }
            //возвращаем картинку
            return new OutputContainer().setBase64(base64).setImage(image);
        }
    }


    //на случай, если активность будет уничтожена, нужно отменить все запросы
    @Override
    public void onDestroy() {
        //открепить задачу прослушивания интернета
        connectivityStatus.performUnregisterCallback();
        if (sendRequestTask != null) sendRequestTask.cancel(true);
        //отменить запрос пикассо для поиска картинки по ссылке
        Picasso.get().cancelRequest(mTarget);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //закрепить задачу прослушивания интернета
        connectivityStatus.performRegisterCallback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //открепить задачу прослушивания интернета
        connectivityStatus.performUnregisterCallback();
        if (sendRequestTask != null) sendRequestTask.cancel(true);
        //отменить запрос поиска картинки по ссылке
        Picasso.get().cancelRequest(mTarget);

    }

}
