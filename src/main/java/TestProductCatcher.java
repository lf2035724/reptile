import java.io.File;

/**
 * Created by yangpy on 2016/11/14.
 */
public class TestProductCatcher {

    public static final String BRAND_MAIN_URL = "http://cn.pharmacyonline.com.au/brands";

    public static final String ALL_PRODUCT_FILE_URL = "C:/Users/li/Desktop/study/aucross/data/detailUrl.xls";

    public static void main(String[] args) throws Exception {
        ProductCacher productCacher = new ProductCacher();
        //productCacher.getProductDtailsUrl(BRAND_MAIN_URL);//所有产品链接
        //String weight = productCacher.getWeight("107379");
//        productCacher.readProductInfo("http://cn.pharmacyonline.com.au/1104565.html");
        File file = new File(ALL_PRODUCT_FILE_URL);
        productCacher.readAllProductInfoToExcel(file,1);
        //ProductEntity productEntity = new ProductEntity();
       // productEntity.setSku("901096");
        //productCacher.getOriImg(new HTMLCacher(),"http://cn.pharmacyonline.com.au/1112266.html",productEntity);

    }
}
