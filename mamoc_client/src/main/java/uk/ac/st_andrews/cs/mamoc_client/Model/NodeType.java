package uk.ac.st_andrews.cs.mamoc_client.Model;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        NodeType.UNKNOWN,
        NodeType.NEARBY,
        NodeType.CLOUDLET,
        NodeType.CLOUD
})

@Retention(RetentionPolicy.CLASS)
public @interface NodeType {
    int UNKNOWN = 0;
    int NEARBY = 1;
    int CLOUDLET = 2;
    int CLOUD = 3;
}

