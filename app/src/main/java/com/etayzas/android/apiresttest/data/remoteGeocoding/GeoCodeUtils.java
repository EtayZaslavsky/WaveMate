package com.etayzas.android.apiresttest.data.remoteGeocoding;

import com.etayzas.android.apiresttest.data.remote.RetrofitClient;

/**
 * Created by Ewise on 20/02/2018.
 */

public class GeoCodeUtils {
    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/";

    public static GeoCodeService getGeoCodeService() {
        return GeoCodeRetroClient.getClient(BASE_URL).create(GeoCodeService.class);
    }
}
