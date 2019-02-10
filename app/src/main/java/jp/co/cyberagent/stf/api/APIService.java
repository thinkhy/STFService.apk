package jp.co.cyberagent.stf.api;

import jp.co.cyberagent.stf.api.domain.ActionInfo;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {

    @GET("action/{serial_of_android_device}")
    Call<ActionInfo> getAction(@Path("serial_of_android_device") String serial);

    @DELETE("action")
    Call<Boolean> removeAction(@Query("ID") String id);
}
