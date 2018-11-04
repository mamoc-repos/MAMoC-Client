package uk.ac.st_andrews.cs.mamoc_client.Profilers;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        NetworkType.UNKNOWN, NetworkType.WIFI, NetworkType.CELLULAR_4G, NetworkType.CELLULAR_3G,
        NetworkType.CELLULAR_UNKNOWN
})

@Retention(RetentionPolicy.CLASS)
public @interface NetworkType {
    int UNKNOWN = 0;
    int WIFI = 1;
    int CELLULAR_3G = 2;
    int CELLULAR_4G = 3;
    int CELLULAR_UNKNOWN = 4;
}
