/**
 * Created by li on 2016-11-10.
 */
public class TestEndPage {
    public static void main(String[] args) throws InterruptedException {
        HTMLCacher hTMLCacher = new HTMLCacher();
        String href = "http://cn.pharmacyonline.com.au/acoe/bbox.html/?p=3";
        System.out.println(hTMLCacher.isEndPage(href));
    }
}
