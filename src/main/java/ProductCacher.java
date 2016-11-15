import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangpy on 2016/11/14.
 */
public class ProductCacher {

    public static final String DETAIL_URL_FILE_PATH = "C:/Users/yangpy/Desktop/chuang/detailUrl.xls";


    public void getProductDtailsUrl(String brandMainUrl) throws Exception {
        if(brandMainUrl == null){
            System.out.println("品牌主页连接为空！");
        }
        List<String> columnName = new ArrayList<String>();
        columnName.add("brandName");
        columnName.add("productDetailUrl");
        HTMLCacher htmlCacher = new HTMLCacher();
        NodeList nodeList =  htmlCacher.getNodeList(brandMainUrl,"div","class","brands-wrap");
        AndFilter andFilter = new AndFilter(
                new TagNameFilter("a"),
                new HasAttributeFilter("href")
        );
        nodeList = nodeList.extractAllNodesThatMatch(andFilter,true);
        LinkTag linkTag = null;
        if(nodeList==null || nodeList.size() < 1){
            System.out.println("没有解析到产品信息！");
          return;
        }
        SimpleNodeIterator simpleNodeIterator = nodeList.elements();
        SimpleNodeIterator productNodeIterator = null;
        String productUrl = null;
        String brandName = null;
        String brandUrl = null;
        Parser parserProduct = null;
        NodeList productNodeList = null;
        LinkTag productLinkTag = null;
        List<String> tempList = new ArrayList<String>();
        Map<String,List<String>> resultMap = new HashMap<String,List<String>>();
        while (simpleNodeIterator.hasMoreNodes()){
            linkTag = (LinkTag)simpleNodeIterator.nextNode();
            brandUrl = linkTag.getLink();
            brandName= linkTag.getLinkText();
            System.out.println(brandName);
            productNodeList =  htmlCacher.getNodeList(brandUrl,"a","class","ProductImg");
            if(productNodeList == null || productNodeList.size() < 1){
                System.out.println("没有找到产品列表数据");
                continue;
            }
            productNodeIterator = productNodeList.elements();
            while (productNodeIterator.hasMoreNodes()){
                productLinkTag = (LinkTag) productNodeIterator.nextNode();
                tempList.add(productLinkTag.getLink());
            }
            while (!htmlCacher.isEndPage(brandUrl)){
                productNodeList =  htmlCacher.getNodeList(brandUrl,"a","class","next_jump");
                if(productNodeList == null || productNodeList.size()<1){
                    System.out.println("警告！下一页按钮居然是空！");
                    break;
                }
                productLinkTag = (LinkTag) productNodeList.elements().nextNode();
                brandUrl = productLinkTag.getLink();
                productNodeList = htmlCacher.getNodeList(brandUrl,"a","class","ProductImg");
                if(productNodeList == null || productNodeList.size()<1){
                    System.out.println("下一页居然没有产品？");
                    break;
                }
                productNodeIterator = productNodeList.elements();
                while (productNodeIterator.hasMoreNodes()){
                    productLinkTag = (LinkTag) productNodeIterator.nextNode();
                    tempList.add(productLinkTag.getLink());
                }
            }
            resultMap.put(brandName,tempList);
            System.out.println("品牌："+brandName+"共有"+tempList.size()+"个产品");
            tempList.clear();
        }
        writeExcel(DETAIL_URL_FILE_PATH,columnName,resultMap);

    }

    public String getNextPageUrl(HTMLCacher htmlCacher ,Parser parser) throws Exception {
        NodeList nodeList = null;
        SimpleNodeIterator simpleNodeIterator = null;
        nodeList = htmlCacher.getNodeList(parser,"a","class","next_jump");
        if (nodeList == null || nodeList.size() < 1){
            System.out.println("警告，给定页面没有下一页URL:");
            System.out.println(parser.getURL());
            return null;
        }
        simpleNodeIterator = nodeList.elements();
        LinkTag linkTag = null;
        while (simpleNodeIterator.hasMoreNodes()){
            linkTag  = (LinkTag)simpleNodeIterator.nextNode();
            return linkTag.getLink();
        }
        return null;
    }

    private void writeExcel(String filePath,List<String> columnNames,Map<String,List<String>> rowValue){
        ExcelHandller excelHandller = new ExcelHandller();
        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        excelHandller.writeExcel(os,columnNames,rowValue);
    }
}
