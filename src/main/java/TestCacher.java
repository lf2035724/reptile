import org.htmlparser.Node;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by li on 2016-11-1.
 */
public class TestCacher {

    public static final String ORI_IMG_PATH = "C:/Users/yangpy/Desktop/chuang/oriImg/";

    public static void main(String[] args) throws  Exception {
//        HTMLCacher HtmlCacher = new HTMLCacher();
//        String url = "http://cn.pharmacyonline.com.au/brands";
//        NodeList nodeList = HtmlCacher.getNodeList(url, "div", "class", "brands-wrap");
//        NodeList nodeListA = new NodeList();
//        AndFilter andFilter = new AndFilter(new TagNameFilter("a"),new HasAttributeFilter("href"));
//        SimpleNodeIterator iterator = nodeList.elements();
//       while (iterator.hasMoreNodes()){
//           iterator.nextNode().collectInto(nodeListA,andFilter);
//       }
//        ExcelHandller excelHandller = new ExcelHandller();
//        File file = new File("C:/Users/li/Desktop/study/testWrite.xls");
//        file.createNewFile();
//        OutputStream os = new FileOutputStream(file);
//        excelHandller.writeExcel(os,nodeListA);
             //   HTMLCacher HtmlCacher = new HTMLCacher();
        //HtmlCacher.getImage("http://7xihp5.com2.z0.glb.qiniucdn.com/media/catalog/product/8/8/887910/antipodes-juliet-skin-brightening-gel-cleanser-200ml.jpg",ORI_IMG_PATH,"123.jpg");
      // String imageName =  HtmlCacher.getImageName("http://7xihp5.com2.z0.glb.qiniucdn.com/media/catalog/product/8/8/887910/antipodes-juliet-scleanser-200ml.jpg?imageMogr2/thumbnail/90x90");
      //  System.out.println(imageName);
    }
}
