import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
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
import org.htmlparser.tags.ImageTag;
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

    public static final String ORI_IMG_PATH = "C:/Users/yangpy/Desktop/chuang/oriImg/";

    public static final String ALL_PRODUCT_FILE_URL = "C:/Users/yangpy/Desktop/chuang/detailUrl.xls";

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

    public void readAllProductInfoToExcel(int urlColumnIndex,int isOverFlagIndex) throws Exception {
        File file = new File(ALL_PRODUCT_FILE_URL);
        List<List> list = readExcel(file,urlColumnIndex,isOverFlagIndex);
        if(list == null || list.size()<1){
            System.out.println("没有读取到导航文件链接地址");
            return;
        }
        ProductEntity productEntity = null;
        List<List> resultList = new ArrayList<List>();
        List resultTemp = null;
        List<Integer> resultOver = new ArrayList<Integer>();
        String url = null;
        for(int i=0;i<list.size();i++){
            url = (String)list.get(i).get(0);
            productEntity = readProductInfo(url);
            if(productEntity == null){
                System.out.println("商品不存在!"+list.get(i));
            }
            resultTemp = new ArrayList();
            resultTemp.add(productEntity);
            resultTemp.add(list.get(i).get(1));
            resultList.add(resultTemp);
            resultOver.add((Integer) list.get(i).get(1));
            if(resultList.size()==10||i==list.size()-1){
                writeExcel(resultList);
                writeExcelOver(resultOver);
                resultList.clear();
                resultOver.clear();
            }
        }
    }

    public List<List> readExcel(File file , int urlColumnIndex, int isOverFlagIndex) throws IOException, BiffException {
        List list = new ArrayList();
        List list2 = null;
        Workbook rwb = null;
        Cell cell = null;
        Cell isOverCell = null;
        InputStream stream = new FileInputStream(file);
        rwb = Workbook.getWorkbook(stream);
        Sheet sheet = rwb.getSheet(0);
        for(int i=0; i<sheet.getRows(); i++){
            cell = sheet.getCell(urlColumnIndex,i);
            isOverCell = sheet.getCell(isOverFlagIndex,i);
            if(StringUtils.isEmpty(StringUtils.trim(isOverCell.getContents()))){
                list2 = new ArrayList();
                list2.add(cell.getContents());
                list2.add(i);
                list.add(list2);
            }
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
        NodeList nodeListTotal = null;
        HTMLCacher htmlCacher = new HTMLCacher();
        Parser parser = null;
        for(int i=0;i<3;i++){
            try {
                parser = new Parser(detailUrl);
                if(parser!=null){
                    break;
                }else {
                    System.out.println("没有获得到详细也信息1秒后重试"+detailUrl);
                    Thread.sleep(1000);
                }
            }catch (Exception ex){
                System.out.println("获得详细页信息出错，3秒后重试");
                Thread.sleep(3000);
            }
        }
        nodeListTotal = htmlCacher.getNodeList(parser,"div","class","product-view");
        AndFilter andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "product-name")
        );
        nodeList =nodeListTotal.extractAllNodesThatMatch(andFilter,true);
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
        andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "DetailSku")
        );
        nodeList = nodeListTotal.extractAllNodesThatMatch(andFilter,true);
        if (nodeList == null || nodeList.size()<1) {
            System.out.println("没有找sku");
            return null;
        }else{
            String sku = nodeList.elementAt(0).toPlainTextString();
            sku=sku.substring(sku.indexOf("SKU:")+"SKU:".length(),sku.length());
            productEntity.setSku(sku);
        }
        andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "DetailPriceContain clearfix")
        );
        nodeList = nodeListTotal.extractAllNodesThatMatch(andFilter, true);
        andFilter = new AndFilter(
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
        andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "DetailPriceContain clearfix")
        );
        nodeList = nodeListTotal.extractAllNodesThatMatch(andFilter,true);
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
            andFilter = new AndFilter(
                    new TagNameFilter("div"),
                    new HasAttributeFilter("class", "DetailNoDis PriceNow last_price_sing")
            );
            nodeList = nodeListTotal.extractAllNodesThatMatch(andFilter, true);
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
            if(matcher.find()){
                productEntity.setBrandEnglishName(StringUtils.trim(productEntity.getProductChineseName().substring(0,matcher.start())));
            }
        }
        andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "std")
        );
        nodeList = nodeListTotal.extractAllNodesThatMatch(andFilter, true);
        if(nodeList == null || nodeList.size()<1){
            System.out.println("详细说明不存在！"+detailUrl);
        }else {
            int tempIndex = 0;
            if(nodeList.elementAt(0).getChildren()==null||nodeList.elementAt(0).getChildren().size()==1){
                productEntity.setProductDescribe(StringUtils.trim(nodeList.elementAt(0).toPlainTextString()));
            }else{
                SimpleNodeIterator iterator = nodeList.elementAt(0).getChildren().elements();
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
        andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "box-collateral box-description")
        );
        nodeList =  nodeListTotal.extractAllNodesThatMatch(andFilter,true);
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
            StringBuffer sb = new StringBuffer();
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
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setCharacteristic(sb.toString());
                    }

                    if(node2.toPlainTextString().indexOf("产品功能：") >= 0){
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setFunctionDescripe(sb.toString());
                    }

                    if(node2.toPlainTextString().indexOf("功能概述：") >= 0){
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setFunctionDescripe(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("主要成份：") >= 0){
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setMainContent(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("适用人群：") >= 0){
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setIntendedFor(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("使用方法：") >= 0){
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setUsageMethod(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("注意事项：") >= 0){
                        if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))&&sb.length()<2){
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }else if(!StringUtils.isEmpty(StringUtils.trim(node3.toPlainTextString()))){
                            sb.append("*");
                            sb.append(replaceTag(node3.toPlainTextString()));
                        }
                        productEntity.setAttention(sb.toString());
                    }
                    if(node2.toPlainTextString().indexOf("Warnings:") >= 0){
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
        if(StringUtils.isEmpty(productEntity.getUnitContent())){
            String temp = productEntity.getUnitContent();
            String parten = "([0-9]+[g]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)|([0-9]+[m]{1}[l]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)([0-9]+[m]{1}[L]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)|([0-9]+[k]{1}[g]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)|([0-9]+[\\u7247]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)|([0-9]+[\\u7c92]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)|([0-9]+[\\u514b]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)|([Tablet]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)|([Tab]{1}[\\s]{1}[X]{1}[\\s]{1}[0-9]+)";
            String parten2 = "([0-9]+[\\u7247]{1})|([0-9]+[\\u7c92]{1})|([0-9]+[\\u514b]{1})|([0-9]+[g]{1})|([0-9]+[m]{1}[l]{1})|([0-9]+[m]{1}[L]{1})|([0-9]+[m]{1}[l]{1})|([0-9]+[k]{1}[g]{1})|([0-9]+[K]{1}[G]{1})|([0-9]+[L]{1})";
            Pattern pattern = Pattern.compile(parten);
            Matcher matcher = pattern.matcher(productEntity.getProductChineseName());
            if(matcher.find()){
                temp = productEntity.getProductChineseName().substring(matcher.start(),matcher.end());
            }else{
                pattern = Pattern.compile(parten2);
                matcher = pattern.matcher(productEntity.getProductChineseName());
                if (matcher.find()){
                    temp = productEntity.getProductChineseName().substring(matcher.start(),matcher.end());
                }
            }
            productEntity.setUnitContent(StringUtils.trim(temp));
        }
        andFilter = new AndFilter(
                new TagNameFilter("div"),
                new HasAttributeFilter("class", "product-extend-specification product-extend")
        );
        nodeList = nodeListTotal.extractAllNodesThatMatch(andFilter,true);
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
                if(index2 == 3){
                    productEntity.setBrandEnglishName(node4.toPlainTextString());
                    index2++;
                }
                if(node4.toPlainTextString().indexOf("英文名称") >= 0){
                    index2=1;
                }
                if(node4.toPlainTextString().indexOf("品牌") >= 0){
                    index2=3;
                }
            }
        }
        getOriImg(htmlCacher,nodeListTotal,productEntity);
        System.out.println(productEntity);
        return productEntity;
    }

    public void getOriImg(HTMLCacher htmlCacher,NodeList totalNodeList,ProductEntity productEntity) throws Exception {
        AndFilter andFilter = new AndFilter(
                new TagNameFilter("a"),
                new HasAttributeFilter("class", "cloud-zoom")
        );
        NodeList nodeList = totalNodeList.extractAllNodesThatMatch(andFilter,true);
        ImageTag imageTag = null;
        String imageUrl = null;
        if(nodeList!=null&&nodeList.size()>0){
            nodeList = nodeList.elementAt(0).getChildren();
            SimpleNodeIterator iterator3 = nodeList.elements();
            while (iterator3.hasMoreNodes()){
                Node node = iterator3.nextNode();
                if(node instanceof ImageTag){
                    imageTag = (ImageTag)node;
                    break;
                }
            }
        }
        if(imageTag== null){
            System.out.println("没有原图片？"+productEntity.getSku());
            return;
        }
        imageUrl = imageTag.getImageURL();
        String postfix = null;
        if(imageUrl.indexOf(".jpg")>0){
            imageUrl = imageUrl.substring(0,imageUrl.indexOf(".jpg")+".jpg".length());
            postfix = ".jpg";
        }else if(imageUrl.indexOf(".gif")>0){
            imageUrl = imageUrl.substring(0,imageUrl.indexOf(".gif")+".gif".length());
            postfix = ".gif";
        }else if(imageUrl.indexOf(".png")>0){
            imageUrl = imageUrl.substring(0,imageUrl.indexOf(".png")+".png".length());
            postfix = ".png";
        }else if(imageUrl.indexOf("?") > 0){
            imageUrl = imageUrl.substring(0,imageUrl.indexOf("?"));
        }else {
            imageUrl = imageUrl+".jpg";
        }
        String tempUrl = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.length());
        if(tempUrl.indexOf(productEntity.getSku()+postfix) >= 0){
            String tempUrl2 = imageUrl.substring(0,imageUrl.lastIndexOf("/")+1)+productEntity.getSku()+postfix;
            try {
                htmlCacher.getImage(tempUrl2,ORI_IMG_PATH,productEntity.getSku() + postfix,false);
            } catch (Exception e) {
                if("图片不存在".equals(e.getMessage())){
                    htmlCacher.getImage(imageUrl,ORI_IMG_PATH,productEntity.getSku() + postfix,true);
                }else {
                    e.printStackTrace();
                }
            }
        }else {
            htmlCacher.getImage(imageUrl,ORI_IMG_PATH,productEntity.getSku() + postfix,true);
        }
    }
    public void writeExcel(List<List> entityList) throws IOException, InterruptedException {
        File file = new File(ALL_PRODUCT_INFO_FILE);
        Workbook wb = null;
        WritableWorkbook wwb = null;
        WritableSheet ws = null;
        try
        {
            if(file.exists()){
                wb = Workbook.getWorkbook(file);
                wwb = Workbook.createWorkbook(file, wb);
                ws = wwb.getSheet(0);
            }else {
                wwb = Workbook.createWorkbook(file);
                ws = wwb.createSheet("sheet",0);
            }
            ProductEntity entity = null;
            for(int i=0;i<entityList.size();i++){
                entity = (ProductEntity)entityList.get(i).get(0);
                if(entity==null){
                    System.out.println("产品实体空！");
                    continue;
                }
                Field [] fields = entity.getClass().getDeclaredFields();
                for(int j=0;j < fields.length;j++){
                    Field f = fields[j];
                    f.setAccessible(true);
                    Object val = f.get(entity);
                    ws.addCell(new Label(j,(Integer)entityList.get(i).get(1),String.valueOf(val)));
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

    public void writeExcelOver(List<Integer> list) throws IOException, WriteException {
        File file = new File(ALL_PRODUCT_FILE_URL);
        Workbook wwb = null;
        WritableWorkbook wbook = null;
        WritableSheet ws = null;
        try
        {
            wwb = Workbook.getWorkbook(file);
            wbook = Workbook.createWorkbook(file, wwb);
            ws = wbook.getSheet(0);
            Integer rowIndex = null;
            for(int i=0;i<list.size();i++){
                rowIndex = list.get(i);
                if(rowIndex==null){
                    System.out.println("行号空！");
                    continue;
                }
                    ws.addCell(new Label(2,rowIndex,"1"));
            }
            wbook.write();
            wbook.close();
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
        for(int i=0;i<4;i++){
            try {
                client = HttpClients.createDefault();
                HttpResponse response = client.execute(request);
                if(response == null&& i<4){
                    continue;
                }
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
            } catch (Exception ex){
                continue;
            }finally {
                if (in != null) {
                    try {
                        in.close();// 最后要关闭BufferedReader
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        for(int i=0;i<4;i++){
            try {
                client = HttpClients.createDefault();
                HttpResponse response = client.execute(requestGetInfo,httpClientContext);
                if(response == null&&i<4){
                    Thread.sleep(1000);
                    continue;
                }
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
            } catch (Exception ex){
                continue;
            } finally{
                if (in != null) {
                    try {
                        in.close();// 最后要关闭BufferedReader
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        for(int i=0;i<4;i++){
            try {
                HttpResponse response = client.execute(httpGet, httpClientContext);
                if(response == null&& i<3){
                    continue;
                }
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
            } catch (Exception ex){
                continue;
            }finally {
                if (in != null) {
                    try {
                        in.close();// 最后要关闭BufferedReader
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
