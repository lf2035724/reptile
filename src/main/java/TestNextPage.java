/**
 * Created by yangpy on 2016/11/11.
 */
public class TestNextPage {
    public static void main(String[] args) throws InterruptedException {
        HTMLCacher htmlCacher = new HTMLCacher();
        System.out.println(htmlCacher.isEndPage("http://cn.pharmacyonline.com.au/baby-552/babyformula.html/?is_in_stock=0&p=4&is_in_stock=0"));
    }
}
