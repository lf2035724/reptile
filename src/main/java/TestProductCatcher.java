/**
 * Created by yangpy on 2016/11/14.
 */
public class TestProductCatcher {
    public static final String BRAND_MAIN_URL = "http://cn.pharmacyonline.com.au/brands";
    public static void main(String[] args) throws Exception {
        ProductCacher productCacher = new ProductCacher();
        productCacher.getProductDtailsUrl(BRAND_MAIN_URL);
    }
}
