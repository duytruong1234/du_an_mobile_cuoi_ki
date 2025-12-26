package vn.duytruong.appbandienthoai.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit instance;

    public static Retrofit getInstance(String baseUrl){
        if (instance == null){
            // Tạo logging interceptor để xem request/response
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor để disable cache - luôn lấy dữ liệu mới từ server
            Interceptor noCacheInterceptor = chain -> {
                Request request = chain.request().newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK) // Bắt buộc lấy từ network
                        .header("Cache-Control", "no-cache, no-store, must-revalidate")
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .build();
                return chain.proceed(request);
            };

            // Tạo OkHttpClient với timeout, logging và NO CACHE
            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(null) // Disable cache hoàn toàn
                    .addInterceptor(noCacheInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Cấu hình Gson lenient để chấp nhận JSON không chuẩn
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            instance = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return instance;
    }

    // Thêm phương thức để reset instance khi cần
    public static void resetInstance() {
        instance = null;
    }
}
