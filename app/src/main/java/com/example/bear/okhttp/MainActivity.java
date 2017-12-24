package com.example.bear.okhttp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        try {
            //X.509证书生成器
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //存储证书的keyStore
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            //从assets读取本地证书
            InputStream cis =getAssets().open("dpdj"); //没有证书的话，应该是都信任；一旦有了证书，就需要匹配，不匹配会报错。
            //在内存中生成证书，并放到KeyStore里
            keyStore.setCertificateEntry("12306", cf.generateCertificate(cis));
            cis.close();
            //创建SSLContext对象，上下文环境为TLS
            SSLContext sslContext = SSLContext.getInstance("TLS");
            //创建TrustManager，用于验证服务器返回的证书是否可信
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            //构建完整的SSLContext
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            sslContext.init(null, trustManagers, new SecureRandom());
            //将SocketFactory给okHttp，用于创建SSLSocket使用
            builder.sslSocketFactory(sslContext.getSocketFactory());
            builder.sslSocketFactory(sslContext.getSocketFactory());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        //get请求
        //https://core.91bcl.com/auth/credentials
        //https://apic.dpdaojia.com
        Request request = new Request.Builder().get().url("https://apic.dpdaojia.com").build();
        Call call = builder.build().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("xiong", "Exception is \n"+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("xiong", "onResponse() is \n" + response.body().string());
            }
        });
    }
}
