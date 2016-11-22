import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yangpy on 2016/11/14.
 */
public class ProductCacher {

    public static final String DETAIL_URL_FILE_PATH = "C:/Users/yangpy/Desktop/chuang/detailUrl.xls";

    public static final String PREFIX_ADD_CAR = "http://cn.pharmacyonline.com.au/checkout/cart/addCartAjax/uenc/,/product/";

    public static  final  String TO_DELETE_STRING = "delete\\/id\\/";

    public static final String TO_DELETE_STRING_POSTFIX = "\\/uenc\\/,";

    public static final String PREFIX_DELTE_CAR = "http://cn.pharmacyonline.com.au/cart/item/delete/id/";

    public static final String POSTFIX_DELETE_CAR = "/uenc/";

    public static final String CAR_URL = "http://cn.pharmacyonline.com.au/checkout/cart/";

    public static  final  String WEIGHT_ID_TAG = "total_weight";

    public static final  String PRODUCT_INFO_URL = "http://cn.pharmacyonline.com.au/cart/item/getInfo";

    public static HttpClientContext httpClientContext= null;

    public static CookieStore cookieStore = null;

    public static final String ALL_PRODUCT_INFO_FILE = "C:/Users/yangpy/Desktop/chuang/productInfo.xls";


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

    public void readAllProductInfoToExcel(File file,int urlColumnIndex) throws Exception {
        List<String> list = readExcel(file,urlColumnIndex);
        if(list == null || list.size()<1){
            System.out.println("没有读取到导航文件链接地址");
            return;
        }
        ProductEntity productEntity = null;
        List<ProductEntity> resultList = new ArrayList<ProductEntity>();
        String url = null;
        for(int i=0;i<30;i++){
            url = list.get(i);
            productEntity = readProductInfo(url);
            if(productEntity == null){
                System.out.println("商品不存在!"+list.get(i));
            }
            resultList.add(productEntity);
        }
        writeExcel(resultList);
    }

    public List<String> readExcel(File file , int urlColumnIndex) throws IOException, BiffException {
        List list = new ArrayList();
        Workbook rwb = null;
        Cell cell = null;
        InputStream stream = new FileInputStream(file);
        rwb = Workbook.getWorkbook(stream);
        Sheet sheet = rwb.getSheet(0);
        for(int i=1; i<sheet.getRows(); i++){
            cell = sheet.getCell(urlColumnIndex,i);
            list.add(cell.getContents());
        }
        System.out.println("读取到条数:"+list.size());
        return list;
    }

    public ProductEntity readProductInfo(String detailUrl) throws Exception {
        if(detailUrl == null){
            System.out.println("产品详情url为空");
            return null;
        }
        Node node = null;
        Node node2 = null;
        NodeList nodeList = null;
        NodeList nodeListSecond  = null;
        NodeList nodeListThird  = null;
        HTMLCacher htmlCacher = new HTMLCacher();
        Parser parser = new Parser(detailUrl);
        nodeList = htmlCacher.getNodeList(parser,"div","class","product-name");
        if (nodeList == null || nodeList.size()<1) {
            System.out.println("没有找到产品名称");
            return null;
        }
        String PriceNow = null;
        String PriceWas = null;
        String PriceSingle = null;
        ProductEntity productEntity = new ProductEntity();
        TagNameFilter tagNameFilter = new TagNameFilter("h1");
        nodeList = nodeList.extractAllNodesThatMatch(tagNameFilter,true);
        productEntity.setProductChineseName(getInfoByNodeList(nodeList));
        parser = new Parser(detailUrl);
        nodeList = htmlCacher.getNodeList(parser, "div", "class", "DetailPriceContain clearfix");
        AndFilter andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "PriceNow")
        );
        nodeList = nodeList.extractAllNodesThatMatch(andFilter,true);
        PriceNow = getInfoByNodeList(nodeList);
        if(PriceNow!=null){
            PriceNow = PriceNow.substring(PriceNow.indexOf("AU$")+"AU$".length());
            PriceNow = StringUtils.strip(PriceNow);
        }
        productEntity.setNowAmount(PriceNow);
        parser = new Parser(detailUrl);
        nodeList = htmlCacher.getNodeList(parser, "div", "class", "DetailPriceContain clearfix");
        andFilter = new AndFilter(
                new TagNameFilter("p"),
                new HasAttributeFilter("class", "PriceWas")
        );
        nodeList = nodeList.extractAllNodesThatMatch(andFilter,true);
        PriceWas = getInfoByNodeList(nodeList);
        if(PriceWas!=null){
            PriceWas = PriceWas.substring(PriceWas.indexOf("AU$")+"AU$".length());
            PriceWas = StringUtils.strip(PriceWas);
        }
        productEntity.setOriginAmount(PriceWas);
        if(PriceNow==null&&PriceWas==null){
            parser = new Parser(detailUrl);
            nodeList = htmlCacher.getNodeList(parser, "div", "class", "DetailNoDis PriceNow last_price_sing");
            nodeList = nodeList.extractAllNodesThatMatch(new TagNameFilter("span"), true);
            PriceSingle = getInfoByNodeList(nodeList);
            if(PriceSingle!=null){
                PriceSingle = PriceSingle.substring(PriceSingle.indexOf("AU$")+"AU$".length());
                PriceSingle = StringUtils.strip(PriceSingle);
            }
            productEntity.setOriginAmount(PriceSingle);
            productEntity.setNowAmount(PriceSingle);
            if (PriceSingle== null){
                System.out.println("警告，没有找到价格！！！");
            }
        }
        productEntity.setProductId(detailUrl.substring(detailUrl.indexOf(".html")-7,detailUrl.indexOf(".html")));
        productEntity.setWeight(getWeight(productEntity.getProductId().substring(1,productEntity.getProductId().length())));
        if(!StringUtils.isEmpty(productEntity.getProductChineseName())){
            String parten = "[\\u4e00-\\u9fa5]+";
            Pattern pattern = Pattern.compile(parten);
            Matcher matcher = pattern.matcher(productEntity.getProductChineseName());
            matcher.find();
            productEntity.setBrandEnglishName(StringUtils.trim(productEntity.getProductChineseName().substring(0,matcher.start())));
        }
        parser = new Parser(detailUrl);
        nodeList =  htmlCacher.getNodeList(parser,"div","class","std");
        if(nodeList == null || nodeList.size()<1){
            System.out.println("详细说明不存在！"+detailUrl);
        }else {
            int tempIndex = 0;
            if(nodeList.elementAt(0).getChildren()==null||nodeList.elementAt(0).getChildren().size()==1){
                productEntity.setProductDescribe(StringUtils.trim(nodeList.elementAt(0).toPlainTextString()));
            }else{
                SimpleNodeIterator iterator = nodeList.elementAt(0).getChildren().elements();
                boolean saveFlag = false;
                StringBuffer sb = new StringBuffer();
                while (iterator.hasMoreNodes()){
                    node = iterator.nextNode();
                    if(node instanceof Div && tempIndex==0){
                        break;
                    }
                    if(node instanceof Div && tempIndex!=0){
                        break;
                    }
                    if(node instanceof ParagraphTag){
                        if(sb.length()<1){
                            sb.append(node.toPlainTextString());
                        }else{
                            sb.append("*" + node.toPlainTextString().replaceAll("\\*",""));
                        }
                    }
                    tempIndex ++;
                }
                productEntity.setProductDescribe(sb.toString());
            }
        }
        parser = new Parser(detailUrl);
        nodeList =  htmlCacher.getNodeList(parser,"div","class","box-collateral box-description");
        andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "desc-title")
        );
        nodeList = nodeList.extractAllNodesThatMatch(andFilter,true);
        SimpleNodeIterator iterator = nodeList.elements();
        int index;
        while (iterator.hasMoreNodes()){
            index = 0;
            node = iterator.nextNode();
            node2 = node;
            while (true){
                node = node.getNextSibling();
                if(node == null ){
                    break;
                }
                if(node instanceof Div){
                    break;
                }
                if(node instanceof ParagraphTag){
                    TagNode node3 = (TagNode) node;
                    if(node2.toPlainTextString().indexOf("产品介绍：") >= 0){
                        if( index == 0){
                            productEntity.setProductDescribe(node3.toPlainTextString());
                        }
                        if(node3.toPlainTextString().indexOf("规格：") >= 0){
                            String temp = node3.toPlainTextString().substring(node3.toPlainTextString().indexOf("规格：")+"规格：".length());
                            productEntity.setUnitContent(StringUtils.trim(temp));
                        }
                        if(node3.toPlainTextString().indexOf("容量：") >= 0){
                            String temp = node3.toPlainTextString().substring(node3.toPlainTextString().indexOf("容量：")+"容量：".length());
                            productEntity.setUnitContent(StringUtils.trim(temp));
                        }
                        if(node3.toPlainTextString().indexOf("品牌：") >= 0){
                            String parten = "[\\u4e00-\\u9fa5]+";
                            String parten2 = "[a-z0-9A-Z]*";
                            Pattern pattern = Pattern.compile(parten);
                            String temp = node3.toPlainTextString().substring(node3.toPlainTextString().indexOf("品牌：")+"品牌：".length());
                            Matcher matcher = pattern.matcher(temp);
                            if(matcher.find()){
                                temp = temp.replaceAll(parten2,"");
                                temp = temp.replaceAll("/","");
                                productEntity.setBrandChineseName(StringUtils.trim(temp));
                            }
                        }
                        if(node3.toPlainTextString().indexOf("产地：") >= 0){
                            String temp = node3.toPlainTextString().substring(node3.toPlainTextString().indexOf("产地：")+"产地：".length());
                            productEntity.setProductingArea(StringUtils.trim(temp));
                        }
                    }
                    if(node2.toPlainTextString().indexOf("产品特点：") >= 0){
                        StringBuffer sb = new StringBuffer();
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setCharacteristic(sb.toString());
                    }

                    if(node2.toPlainTextString().indexOf("产品功能：") >= 0){
                        StringBuffer sb = new StringBuffer();
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setCharacteristic(sb.toString());
                    }

                    if(node2.toPlainTextString().indexOf("功能概述：") >= 0){
                        StringBuffer sb = new StringBuffer();
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setFunctionDescripe(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("主要成份：") >= 0){
                        StringBuffer sb = new StringBuffer();
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setMainContent(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("适用人群：") >= 0){
                        StringBuffer sb = new StringBuffer();
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setIntendedFor(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("使用方法：") >= 0){
                        StringBuffer sb = new StringBuffer();
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setUsageMethod(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("注意事项：") >= 0){
                        StringBuffer sb = new StringBuffer();
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setAttention(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("Warnings:") >= 0){
                        StringBuffer sb = new StringBuffer();
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setAttention(sb.toString());
                    }
                    index ++;
                    continue;
                }else{
                    continue;
                }
            }
        }
        parser = new Parser(detailUrl);
        nodeList = htmlCacher.getNodeList(parser, "div", "class", "product-extend-specification product-extend");
        if(nodeList!=null&&nodeList.size()>0){
            nodeList = nodeList.extractAllNodesThatMatch(new TagNameFilter("td"), true);
        }
        int index2 = 0;
        if(nodeList!=null&&nodeList.size()>0){
            iterator = nodeList.elements();
            while (iterator.hasMoreNodes()){
                TagNode node4= (TagNode)iterator.nextNode();
                if(index2 == 1){
                    productEntity.setProductEnglishName(node4.toPlainTextString());
                    index2++;
                }
                if(node4.toPlainTextString().indexOf("英文名称") >= 0){
                    index2=1;
                }
            }
        }
        System.out.println(productEntity);
        return productEntity;
    }

    public void writeExcel(List<ProductEntity> entityList){
        File file = new File(ALL_PRODUCT_INFO_FILE);
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
        WritableWorkbook wwb = null;
        WritableSheet ws = null;
        try
        {
            wwb = Workbook.createWorkbook(os);
            ws = wwb.createSheet("sheet",0);
            ProductEntity entity = null;
            for(int i=0;i<entityList.size();i++){
                entity = entityList.get(i);
                if(entity==null){
                    System.out.println("产品实体空！");
                    continue;
                }
                Field [] fields = entity.getClass().getDeclaredFields();
                for(int j=0;j < fields.length;j++){
                    Field f = fields[j];
                    f.setAccessible(true);
                    Object val = f.get(entity);
                    ws.addCell(new Label(j,i,String.valueOf(val)));
                }
            }
            wwb.write();
            wwb.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getWeight(String productId) throws Exception {
        if(productId == null){
            System.out.println("产品ID不能为空");
            return null;
        }
        if(productId.length()==7){
            productId.substring(1,productId.length());
        }
        String weight = null;
        String deleteId = null;
        HTMLCacher htmlCacher = new HTMLCacher();
        String url = PREFIX_ADD_CAR+productId;
        //System.out.println(url);
        BufferedReader in = null;
        String content = null;
        CloseableHttpClient client = null;
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("qty","1");
        HttpPost request = postForm(url,paramMap);
        try {
            client = HttpClients.createDefault();
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
        int deleteIndex ;
        if(content!=null){
            deleteIndex = content.indexOf(TO_DELETE_STRING);
            deleteId = content.substring(deleteIndex + TO_DELETE_STRING.length(), content.indexOf(TO_DELETE_STRING_POSTFIX));
         //   System.out.println(deleteId);
        }else {
            System.out.println("没有找到删除ID");
            return null;
        }
        paramMap = new HashMap<String, String>();
        paramMap.put("ShippingMethod","tablerate");
        HttpPost requestGetInfo = postForm(PRODUCT_INFO_URL,paramMap);
        try {
            client = HttpClients.createDefault();
            HttpResponse response = client.execute(requestGetInfo,httpClientContext);
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
       // System.out.println(content);
        if(content.indexOf("\"WeightTotal\":")>0){
            content = content.substring(content.indexOf("\"WeightTotal\":")
                    +"\"WeightTotal\":".length(),content.indexOf("\"ItemSubtotal\"")-1);
        }
        weight = content;
        //System.out.println(PREFIX_DELTE_CAR+deleteId);
        HttpGet httpGet = new HttpGet(PREFIX_DELTE_CAR+deleteId);
       // System.out.println(PREFIX_DELTE_CAR+deleteId);
        try {
            HttpResponse response = client.execute(httpGet, httpClientContext);
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
        //System.out.println(content);
        return weight;
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

    public String replaceTag(String oriString){
        if(StringUtils.isEmpty(oriString)){
            System.out.println("要替换的源字符串为空！");
            return null;
        }
        if(oriString.indexOf("○") >= 0){
            oriString = oriString.replaceAll("○","");
        }
        if(oriString.indexOf("　") >= 0){
            oriString = oriString.replaceAll("　","");
        }

        return StringUtils.trim(oriString);
    }

    public static void setContext() {
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
        cookieStore = new BasicCookieStore();
        Header [] header = null;
        header = httpResponse.getHeaders("Set-Cookie");
        HeaderElement [] headerElement = header[0].getElements();
        BasicClientCookie cookie = null;
        for(int i=0;i<header.length;i++){
            for(int j=0;j<header[i].getElements().length;j++){
                if(j%2==0){
                    cookie = new BasicClientCookie(header[i].getElements()[j].getName(),
                            header[i].getElements()[j].getValue());
                    cookie.setDomain("cn.pharmacyonline.com.au");
                    cookie.setPath("/");
                    //System.out.println(header[i].getElements()[j].getName());
                   //System.out.println(header[i].getElements()[j].getValue());
                    cookieStore.addCookie(cookie);
                }

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

    private static HttpPost postForm(String url, Map<String, String> params){

        HttpPost httpost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList <NameValuePair>();

        Set<String> keySet = params.keySet();
        for(String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key)));
        }

        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return httpost;
    }

    public String getInfoByNodeList(NodeList nodeList){
        if(nodeList == null || nodeList.size()<1){
            System.out.println("根据nodelist获得信息时nodelist为空");
            return null;
        }
        TagNode tagNode = (TagNode)nodeList.elementAt(0);
        return StringUtils.trim(tagNode.toPlainTextString());
    }
}
