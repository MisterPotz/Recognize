package com.gornostaev.recognize.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.CONNECTIVITY_SERVICE;

//этот класс нужен для получения информации о доступности сети
public class ConnectivityStatus {
    //храним контекст нужной активности
    private Context context;

    //интерфейс для обратной связи с активностью при изменении состояния интернета
    public interface OnInternetStateChangeListener {
        void onChange(boolean connectionIsActive);
    }

    private OnInternetStateChangeListener listener;
    private ConnectivityManager.NetworkCallback networkCallback;

    //соединение с интернетом просто включено (но еще неизвестно, работает ли
    //оно на самом деле
    private boolean connectionExists;

    //активное соединение с интернетом существует
    private boolean connectionActive;
    private final static String TAG = "Connectivity Status";
    //эта переменная нужна для хранения ссылки на асинхронную задачу
    //чтобы не было одновременно запущенных нескольких задач
    private CheckConnectionAsyncTask mTask;

    public boolean isConnectionActive() {
        return connectionActive;
    }

    //функция нужна для регистрации в ConnectivityManager нужного Callback
    public void performRegisterCallback() {
        //получаем ConnectivityManager
        final ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext()
                .getSystemService(CONNECTIVITY_SERVICE);
        //объявляем переменную, которая будет определять отслеживаемую сеть
        NetworkRequest request;
        //создаем билдер и указываем что целевая сеть должна использовать интернет
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        //получаем требования для сети
        request = builder.build();
        //если колбэк был ранее создан, сразу прикрепляем его
        if (networkCallback != null) {
            connectivityManager.registerNetworkCallback(request, networkCallback);
            return;
        }
        //создаем новый callback
        networkCallback =
                new ConnectivityManager.NetworkCallback() {
                    //при доступности Network
                    @Override
                    public void onAvailable(Network network) {
                        //super.onAvailable(network);
                        connectionExists = true;
                        connectionActive = true;
                        //при доступности сети ставим, будто она доступна и реально работает
                        //но когда будет нажата кнопка запроса - будет реальная проверка доступности
                        //функцией updateConnection()
                        listener.onChange(connectionActive);
                        //updateConnection();
                        Log.d(TAG, "Connection exists: " + connectionExists);
                    }

                    //при потере соединения Network
                    @Override
                    public void onLost(Network network) {
                        //super.onLost(network);
                        connectionActive = false;
                        connectionExists = false;
                        //работаем с интерфейсом, и указываем проблемы с соединением
                        listener.onChange(connectionActive);
                        Log.d(TAG, "Network disconnected " + network.toString());
                    }
                };
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private Context getBaseContext() {
        return context;
    }

    //функция нужна для открепления Callback
    public void performUnregisterCallback() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext()
                .getSystemService(CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    //передаем контекст активности, чтобы получить сервис ConnectivityManager
    public ConnectivityStatus(Context base, OnInternetStateChangeListener listener) {
        context = base;
        //изначально считаем, что соединение в порядке
        connectionActive = true;
        connectionExists = true;
        //регистрируем слушателя интернета в ConnectivityManager
        performRegisterCallback();
        this.listener = listener;
        mTask = null;
        updateConnection();//при инициализации сразу проводим проверку на интернет
    }

    //проверка при каждом нажатии на кнопку
    //и отмена предыдущего AsyncTask, если такой был (на всякий случай)
    public void updateConnection() {
        //проверяем, не была ли уже запущена эта задача
        if (mTask != null) {
            return;
        }
        //запускаем асинхр. задачу для определения активности соединения (то есть
        //что соединение реально есть)
        mTask = new CheckConnectionAsyncTask();
        mTask.execute(connectionExists);
    }

    //асинк таск для отслеживания реального состояния интернета (посылает запрос на
    //какой-нибудь доступный сервер)
    public class CheckConnectionAsyncTask extends AsyncTask<Boolean, Integer, Boolean> {
        //при получении результата
        @Override
        protected void onPostExecute(Boolean result) {
            //устанавливаем результат проверки
            connectionActive = result;
            Log.d(TAG, "Connection is active: " + connectionActive);
            //вызываем колбэк для работы с интерфейсом
            listener.onChange(connectionActive);
            //убираем ссылку на эту задачу в переменной, которая эту ссылку хранит,
            //после выполнения задачи (чтобы можно было запустить новую задачу)
            mTask = null;
        }

        @Override
        protected Boolean doInBackground(Boolean... parameter) {
            //первый параметр - существует ли соединение вообще
            //если оно не существует - то дальше активность соединения проверять не нужно
            if (!parameter[0]) {
                return false;
            }
            return pingServer();
        }
    }

    //функция для пинга какого-нибудь сервера
    private boolean pingServer() {
        try {
            HttpURLConnection urlc = (HttpURLConnection)
                    (new URL("http://clients3.google.com/generate_204")
                            .openConnection());
            urlc.setRequestProperty("User-Agent", "Android");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000);
            urlc.connect();
            return (urlc.getResponseCode() == 204 &&
                    urlc.getContentLength() == 0);
        } catch (IOException e) {
            Log.e(TAG, "Error checking internet connection", e);
        }
        return false;
    }
}