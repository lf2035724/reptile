/**
 * Created by li on 2016-11-10.
 */
public class TestEndPage {
    public static void main(String[] args) {
        HTMLCacher hTMLCacher = new HTMLCacher();
        String href = "http://cn.pharmacyonline.com.au/acoe/swisse.html/?is_in_stock=0&p=5&is_in_stock=0";
        System.out.println(hTMLCacher.isEndPage(href));
    }
}
