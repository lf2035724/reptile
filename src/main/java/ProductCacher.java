import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.protocol.HttpContext;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * Created by yangpy on 2016/11/14.
 */
public class ProductCacher {

    public static final String DETAIL_URL_FILE_PATH = "C:/Users/yangpy/Desktop/chuang/detailUrl.xls";

    public static final String PREFIX_ADD_CAR = "http://cn.pharmacyonline.com.au/checkout/cart/addCartAjax/uenc/,/product/";

    public static  final  String TO_DELETE_STRING = "delete\\/id\\/";

    public static final String PREFIX_DELTE_CAR = "http://cn.pharmacyonline.com.au/cart/item/delete/id/";

    public static final String POSTFIX_DELETE_CAR = "/uenc/";

    public static final String CAR_URL = "http://cn.pharmacyonline.com.au/checkout/cart/";

    public static  final  String WEIGHT_ID_TAG = "total_weight";

    public static HttpClientContext httpClientContext= null;

    public static CookieStore cookieStore = null;

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
        List<String> tempList = null;
        Map<String,List<String>> resultMap = new HashMap<String,List<String>>();
        int count = 0;
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
            tempList = new ArrayList<String>();
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
        }
        writeExcel(DETAIL_URL_FILE_PATH,resultMap);

    }

    public ProductEntity readProductInfo(String detailUrl){
        return null;
    }

    public String addToCar(String productId) throws Exception {
        if(productId == null){
            System.out.println("产品ID不能为空");
            return null;
        }
        if(productId.length()==7){
            productId.substring(1,productId.length());
        }
        String deleteId = null;
        HTMLCacher htmlCacher = new HTMLCacher();
        String url = PREFIX_ADD_CAR+productId;
        System.out.println(url);
        BufferedReader in = null;
        String content = null;
        CloseableHttpClient client = null;
        HttpPost request = new HttpPost();
        try {
            client = HttpClients.createDefault();
            request.setURI(new URI(CAR_URL));
            HttpResponse response = client.execute(request);
            setCookieStore(response);
            setContext();
            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent(), "utf-8"));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            content = sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();// 最后要关闭BufferedReader
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(content);
        int deleteIndex ;
        if(content!=null){
            deleteIndex = content.indexOf(TO_DELETE_STRING);
            System.out.println(deleteIndex);
        }else {
            System.out.println("没有找到删除ID");
            return null;
        }
        if(deleteIndex > 0){
            deleteId = content.substring(deleteIndex+TO_DELETE_STRING.length()+7,content.length());
        }
        try {
            request.setURI(new URI(CAR_URL));
            HttpResponse response = client.execute(request, httpClientContext);
            //setCookieStore(response);
            //setContext();
            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent(), "utf-8"));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            content = sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();// 最后要关闭BufferedReader
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
       // System.out.println(content);
        System.out.println(httpClientContext.getCookieStore().getCookies());
        if (content!=null){
            System.out.println(content.indexOf(WEIGHT_ID_TAG));
        }

        return null;
    }

    public boolean removeFromCar(String deleteString) throws Exception {
        if(deleteString == null){
            System.out.println("deleteString为空");
            return false;
        }
        HTMLCacher htmlCacher = new HTMLCacher();
        String content = null;
        content = htmlCacher.getHTMLContent(PREFIX_DELTE_CAR+deleteString+POSTFIX_DELETE_CAR);
        System.out.println(content);
        if(content != null && content.indexOf("success")>0){
            return true;
        }else {
            return false;
        }
    }

    public String getWeight() throws Exception {
        HTMLCacher htmlCacher = new HTMLCacher();
        NodeList nodeList = htmlCacher.getNodeList(CAR_URL,"span","id","WEIGHT_ID_TAG");
        if (nodeList == null || nodeList.size() <1){
            System.out.println("购物车没有找到重量信息");
            return null;
        }
        SimpleNodeIterator iterator =  nodeList.elements();
        Node node = null;
        while (iterator.hasMoreNodes()){
            node = iterator.nextNode();
            System.out.println(node.toPlainTextString());
        }
        return node.toPlainTextString();
    }

    public static void setContext() {
        System.out.println("----setContext");
        httpClientContext = HttpClientContext.create();
        httpClientContext.setCookieStore(cookieStore);
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

    private void writeExcel(String filePath,Map<String,List<String>> rowValue){
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
        excelHandller.writeExcelByMap(os,rowValue);
    }

    public static void setCookieStore(HttpResponse httpResponse) {
        System.out.println("----setCookieStore");
        cookieStore = new BasicCookieStore();
        Header [] header = null;
        header = httpResponse.getHeaders("Set-Cookie");
        HeaderElement [] headerElement = header[0].getElements();
        BasicClientCookie cookie = null;
        for(int i=0;i<header.length;i++){
            for(int j=0;j<header[i].getElements().length;j++){
                cookie = new BasicClientCookie(header[i].getElements()[j].getName(),
                        header[i].getElements()[j].getValue());
                cookie.setDomain(".cn.pharmacyonline.com.au");
                cookie.setPath("/");
                    System.out.println(header[i].getElements()[j].getName());
                    System.out.println(header[i].getElements()[j].getValue());

                cookieStore.addCookie(cookie);
            }
        }

//        String JSESSIONID = setCookie.substring(setCookie.indexOf("frontend=")+"frontend=".length(),
//                setCookie.indexOf(";"));
//        System.out.println("JSESSIONID:" + JSESSIONID);
//        BasicClientCookie cookie = new BasicClientCookie("frontend",
//                JSESSIONID);
//        cookie.setDomain(".cn.pharmacyonline.com.au");
//        cookie.setPath("/");
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date());
//        cal.add(Calendar.DATE,+1);
//        cookie.setExpiryDate(cal.getTime());
        // cookie.setAttribute(ClientCookie.VERSION_ATTR, "0");
        // cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "127.0.0.1");
        // cookie.setAttribute(ClientCookie.PORT_ATTR, "8080");
        // cookie.setAttribute(ClientCookie.PATH_ATTR, "/CwlProWeb");
        //cookieStore.addCookie(cookie);
    }
}
