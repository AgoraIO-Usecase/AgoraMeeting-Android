package io.agora.meeting.http;

import io.agora.meeting.http.been.AppVersionResp;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Description:
 *
 * @author xcz
 * @since 3/1/21
 */
public interface SystemService {


    @GET("/scenario/meeting/apps/{appId}/v2/appVersion")
    Call<AppVersionResp> checkVersion(
            @Path("appId") String appId,
            @Query("osType") int osType,
            @Query("terminalType") int terminalType,
            @Query("appVersion") String appVersion);

}
