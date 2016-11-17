/**
 * Created by yangpy on 2016/11/14.
 */
public class TestProductCatcher {

    public static final String BRAND_MAIN_URL = "http://cn.pharmacyonline.com.au/brands";

    public static void main(String[] args) throws Exception {
        ProductCacher productCacher = new ProductCacher();
        //productCacher.getProductDtailsUrl(BRAND_MAIN_URL);//所有产品链接
        String productDeleteId = productCacher.addToCar("109046");
        System.out.println(productDeleteId);
        //System.out.println(productCacher.addToCar("110298"));
        //String temp = productCacher.getWeight();
        //System.out.println(temp);
        //System.out.println(productCacher.removeFromCar(temp));
    }
}
