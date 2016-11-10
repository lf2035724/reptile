import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
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
            System.out.println(content);
            return content;
        }
    }

    public NodeList getNodeList(String url, String tagName, String attributeName, String attributeValue)
            throws Exception {
        Parser parser = new Parser(url);
        AndFilter andFilter = new AndFilter(
                new TagNameFilter(tagName),
                new HasAttributeFilter(attributeName, attributeValue)
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

    public void getImage(String url, String filePath, String fileName) throws Exception {
        FileOutputStream fo = null;
        InputStream ins = null;
        URL uri = null;
        try {
            uri = new URL(url);
            ins = uri.openStream();
            fo = new FileOutputStream(new File(filePath + fileName));
            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = ins.read(buf, 0, buf.length)) != -1) {
                fo.write(buf, 0, length);
            }
            ins.close();
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
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

    public boolean isEndPage(String href) {
        if (StringUtils.isEmpty(href)) {
            System.out.println("警告！地址为空！");
        }
        String html = null;
        try {
            html = getHTMLContent(href);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NodeList nodeList = null;
        String linkhref = null;
        if (html.indexOf("下一页") > 1) {
            try {
                nodeList = this.getNodeList(href, "a", "&p=");
            } catch (Exception e) {
                e.printStackTrace();
            }
            SimpleNodeIterator sni = null;
            LinkTag linkTag = null;
            if (nodeList != null) {
                sni = nodeList.elements();
                while (sni.hasMoreNodes()) {
                    linkTag = (LinkTag) sni.nextNode();
                    if (linkTag.getLinkText() != null && linkTag.getLinkText().indexOf("下一页") >= 0) {
                        break;
                    }
                }
                if (linkTag == null) {
                    System.out.println("没有找到下一页对应的node");
                    return false;
                } else {
                    linkhref = linkTag.getLink();
                    System.out.println("111111111111111");
                    System.out.println(linkTag.getLinkText());
                    System.out.println(linkhref);
                }
            }
        } else {
            System.out.println("没有找到下一页对应的node2");
            return false;
        }
        int attrIndex = linkhref.indexOf("&p=");
        if (attrIndex < 1) {
            System.out.println("根据href没有找到&p=");
        }
        String tempStr = linkhref.substring(attrIndex + 1, linkhref.length());
        attrIndex = tempStr.indexOf("&");
        tempStr = tempStr.substring(0, attrIndex);
        if (href.indexOf(tempStr) > 0) {
            return true;
        } else {
            return false;
        }
    }
}
