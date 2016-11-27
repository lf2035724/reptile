import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.IteratorImpl;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by li on 2016-11-1.
 */
public class HTMLCacher {

    public String getHTMLContent(String url) throws Exception {
        BufferedReader in = null;
        String content = null;

        try {
            // 定义HttpClient
            HttpClient client = HttpClients.createDefault();
            // 实例化HTTP方法
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);
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
            return content;
        }
    }

    public String getHTMLContent(String url, HttpClient client, HttpContext context) throws Exception {
        BufferedReader in = null;
        String content = null;

        try {
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request,context);
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
            return content;
        }
    }

    public NodeList getNodeList(String url, String tagName, String attributeName, String attributeValue)
            throws Exception {
        Parser parser = null;
        for(int i=0;i<6;i++){
            try {
                parser = new Parser(url);
                if(parser == null){
                    Thread.sleep(2000);
                    continue;
                }
            }catch (Exception ex){
                    Thread.sleep(2000);
                    continue;
            }
        }
        AndFilter andFilter = new AndFilter(
                new TagNameFilter(tagName),
                new HasAttributeFilter(attributeName, attributeValue)
        );
        NodeList nodes = parser.parse(andFilter);
        return nodes;
    }

    public NodeList getNodeList(Parser parser, String tagName, String attributeName, String attributeValue)
            throws Exception {
        AndFilter andFilter = new AndFilter(
                new TagNameFilter(tagName),
                new HasAttributeFilter(attributeName, attributeValue)
        );
        NodeList nodes = parser.parse(andFilter);
        return nodes;
    }

    public NodeList getLinkNodeListByRegex(String url, String tagName, String attributeRegex)
            throws Exception {
        Parser parser = new Parser(url);
        AndFilter andFilter = new AndFilter(
                new TagNameFilter(tagName),
                new LinkRegexFilter(attributeRegex)
        );
        NodeList nodes = parser.parse(andFilter);
        return nodes;
    }

    public NodeList getNodeList(String url, String tagName, String htmlValue)
            throws Exception {
        Parser parser = new Parser(url);
        AndFilter andFilter = new AndFilter(
                new TagNameFilter(tagName),
                new LinkStringFilter(htmlValue)
        );
        NodeList nodes = parser.extractAllNodesThatMatch(andFilter);
        return nodes;
    }

    public void getImage(String url, String filePath, String fileName,boolean isLastImgUrl) throws Exception {
        FileOutputStream fo = null;
        InputStream ins = null;
        URL uri = null;
        for(int i=0;i<3;i++){
            if(!isLastImgUrl&&i<2){
                i++;
            }
            try {
                uri = new URL(url);
                ins = uri.openStream();
                fo = new FileOutputStream(new File(filePath + fileName));
                byte[] buf = new byte[1024];
                int length = 0;
                while ((length = ins.read(buf, 0, buf.length)) != -1) {
                    fo.write(buf, 0, length);
                }
                //Thread.sleep(1000);
                ins.close();
                fo.close();
            } catch (Exception e) {
                if(i==2&&!isLastImgUrl){
                    throw new Exception("图片不存在");
                }else if(i==2&&isLastImgUrl){
                    e.printStackTrace();
                }else{
                    System.out.println("图片不存在1秒后重试"+url);
                    Thread.sleep(1000);
                    continue;
                }
            } finally {
                if (ins != null) {
                    try {
                        ins.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (fo != null) {
                    try {
                        fo.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getImageName(String imageSrc) {
        if (imageSrc == null) {
            System.out.println("警告！图片地址为空！");
        }
        int pos = imageSrc.indexOf(".jpg");
        if (pos < 1) {
            System.out.println("警告！图片地址无法找到.jpg！");
        }
        imageSrc = imageSrc.substring(0, pos);
        int posStar = imageSrc.lastIndexOf("/");
        return imageSrc.substring(posStar + 1);
    }

    public boolean isEndPage(String href) throws InterruptedException {
        if(StringUtils.isEmpty(href)){
            System.out.println("传入的连接地址为空！是最后一页");
            return true;
        }
        String htmlContent = null;
        for(int i=0;i<6;i++){
            try {
                htmlContent = getHTMLContent(href);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(StringUtils.isEmpty(htmlContent)){
                Thread.sleep(2000);
                System.out.println("通过连接获取到html为空，2秒后再次尝试href:"+href);
            }else{
                break;
            }
        }
       int indexOfNextPage =  htmlContent.indexOf("下一页");
        String temp = null;
        if(indexOfNextPage>0){
            temp = htmlContent.substring(indexOfNextPage-40,indexOfNextPage);
            int tempIndex = temp.indexOf("p=");
            if(tempIndex>0){
                temp = temp.substring(tempIndex,temp.lastIndexOf("\">"));
                return href.indexOf(temp)>0? true : false;
            }else{
                System.out.println("下一页超链接没有找到p=关键字");
                return true;
            }
        }else{
            return true;
        }
    }


}
