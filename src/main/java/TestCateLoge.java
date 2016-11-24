import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.File;
import java.io.IOException;

/**
 * Created by li on 2016-11-12.
 */
public class TestCateLoge {

    public static final String FIRST_CATELOG_URL = "http://cn.pharmacyonline.com.au/";
    public static final String SECOND_CATELOG_URL = "http://cn.pharmacyonline.com.au/";

    public static void main(String[] args) throws Exception {
       CatelogCatcher catelogCatcher = new CatelogCatcher();
        //catelogCatcher.getAllFirstCatelog(FIRST_CATELOG_URL);
        //catelogCatcher.getAllSecondCatelog(SECOND_CATELOG_URL);
        //catelogCatcher.getAllThirdCatelog(FIRST_CATELOG_URL);
        //File file = new File(CatelogCatcher.SECOND_CATELOG_FILE_PATH);
        //catelogCatcher.getFourCatelog(file,2,0,4,false);
        File file = new File(CatelogCatcher.THIRD_CATELOG_FILE_PATH);
        catelogCatcher.getFourCatelog(file,2,0,4,true);
    }
}
