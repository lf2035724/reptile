import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2016-11-12.
 */
public class CatelogCatcher {

    public static final String FIRST_CATELOG_FILE_PATH = "C:/Users/yangpy/Desktop/chuang/fistCatelog.xls";

    public static final String SECOND_CATELOG_FILE_PATH = "C:/Users/yangpy/Desktop/chuang/secondCatelog.xls";

    public static final String THIRD_CATELOG_FILE_PATH = "C:/Users/yangpy/Desktop/chuang/thirdCatelog.xls";

    public static final String FOUR_CATELOG_FILE_PATH = "C:/Users/yangpy/Desktop/chuang/fourCatelog.xls";

    public static final String FIVE_CATELOG_FILE_PATH = "C:/Users/yangpy/Desktop/chuang/fiveCatelog.xls";


    public void getAllFirstCatelog(String mainUrl){
        HTMLCacher htmlCacher = new HTMLCacher();
        NodeList nodeList = null;
        try {
            nodeList = htmlCacher.getNodeList(mainUrl,"a","class","nav-top-title");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if(nodeList == null){
            System.out.println("根据mainUrl没有找到firstCatelogA");
            return;
        }
        SimpleNodeIterator simpleNodeIterator = nodeList.elements();
        Node node = null;
        LinkTag linkTag = null;
        String columnName1 = "catelogEnglishName";
        String columnName2 = "catelogChineseName";
        String columnName3 = "url";
        List<String> columnNames = new ArrayList<String>();
        columnNames.add(columnName1);
        columnNames.add(columnName2);
        columnNames.add(columnName3);
        List<String> catelogEnglishNameValues = new ArrayList<String>();
        List<String> columnNamesValues = new ArrayList<String>();
        List<String> urlValues = new ArrayList<String>();
        Map<String,List<String>> rowValue = new HashMap<String, List<String>>();
        rowValue.put(columnName1,catelogEnglishNameValues);
        rowValue.put(columnName2,columnNamesValues);
        rowValue.put(columnName3,urlValues);
        List tempList = null;
        String catelogEnglishName = "";
        String catelogChineseName = "";
        String url = "";
        while (simpleNodeIterator.hasMoreNodes()){
            linkTag = (LinkTag) simpleNodeIterator.nextNode();
            System.out.println(linkTag.getLink());
            url = linkTag.getLink();
            if(url.indexOf(".html")<1){
                continue;
            }
            catelogEnglishName = url.substring(url.indexOf(".com.au/")+".com.au/".length(),url.indexOf(".html"));
            catelogChineseName = linkTag.getChildren().elementAt(1).getFirstChild().getText();
            rowValue.get(columnName1).add(catelogEnglishName);
            rowValue.get(columnName2).add(catelogChineseName);
            url = url.substring(0,url.lastIndexOf(".html")+".html".length());
            rowValue.get(columnName3).add(url);
        }
        writeExcel(FIRST_CATELOG_FILE_PATH,columnNames,rowValue);
    }

    public void getAllSecondCatelog(String mainUrl){
        HTMLCacher htmlCacher = new HTMLCacher();
        NodeList nodeList = null;
        NodeList nodeListSecond = null;
        try {
            nodeList = htmlCacher.getNodeList(mainUrl,"a","class","nav-top-title");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if(nodeList == null){
            System.out.println("根据mainUrl没有找到firstCatelogA");
            return;
        }
        SimpleNodeIterator simpleNodeIterator = nodeList.elements();
        LinkTag linkTag = null;
        NodeList nodeListThird = null;
        String columnName1 = "catelogEnglishName";
        String columnName2 = "catelogChineseName";
        String columnName3 = "url";
        String columnName4 = "parentCatelogName";
        String columnName5 = "isEnd";
        List<String> columnNames = new ArrayList<String>();
        columnNames.add(columnName1);
        columnNames.add(columnName2);
        columnNames.add(columnName3);
        columnNames.add(columnName4);
        columnNames.add(columnName5);

        List<String> columnName1Values = new ArrayList<String>();
        List<String> columnName2Values = new ArrayList<String>();
        List<String> columnName3Values = new ArrayList<String>();
        List<String> columnName4Values = new ArrayList<String>();
        List<String> columnName5Values = new ArrayList<String>();

        Map<String,List<String>> rowValue = new HashMap<String, List<String>>();
        rowValue.put(columnName1,columnName1Values);
        rowValue.put(columnName2,columnName2Values);
        rowValue.put(columnName3,columnName3Values);
        rowValue.put(columnName4,columnName4Values);
        rowValue.put(columnName5,columnName5Values);

        String catelogEnglishNameFirst = "";
        String catelogEnglishNameSecond = "";
        String catelogChineseNameSecond = "";
        String urlFirst = "";
        String urlSecond = "";
        SimpleNodeIterator simpleNodeIteratorSecond = null;
        LinkTag linkTagSecond = null;
        Boolean isEnd = Boolean.FALSE;
        while (simpleNodeIterator.hasMoreNodes()){
            linkTag = (LinkTag) simpleNodeIterator.nextNode();
            System.out.println(linkTag.getText());
            urlFirst = linkTag.getLink();
            if(urlFirst.indexOf(".html")<1){
                continue;
            }
            catelogEnglishNameFirst = urlFirst.substring(urlFirst.indexOf(".com.au/")+".com.au/".length(),urlFirst.indexOf(".html"));
            try {
                nodeListSecond =  htmlCacher.getNodeList(urlFirst,"a","class","SecondMenuName");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
            if(nodeListSecond == null || nodeListSecond.size() < 1){
                System.out.println("根据mainUrl没有找到secondCatelogA");
                continue;
            }
            simpleNodeIteratorSecond = nodeListSecond.elements();
            while (simpleNodeIteratorSecond.hasMoreNodes()) {
                linkTagSecond = (LinkTag) simpleNodeIteratorSecond.nextNode();
                System.out.println(linkTagSecond.getText());
                urlSecond = linkTagSecond.getLink();
                catelogEnglishNameSecond = urlSecond.substring(urlSecond.lastIndexOf("/")+1,urlSecond.indexOf(".html"));
                catelogChineseNameSecond = linkTagSecond.getLinkText();
                try {//开始抓取3级菜单
                    nodeListThird = htmlCacher.getNodeList(urlSecond,"a","class","SecondMenuName");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                if(nodeListThird == null || nodeListThird.size() < 1){
                    System.out.println("当前只有二级分类，结束");
                    isEnd = Boolean.TRUE;
                }else{
                    isEnd = Boolean.FALSE;
                }
                urlSecond = urlSecond.substring(0,urlSecond.lastIndexOf(".html")+".html".length());
                rowValue.get(columnName1).add(catelogEnglishNameSecond);
                rowValue.get(columnName2).add(catelogChineseNameSecond);
                rowValue.get(columnName3).add(urlSecond);
                rowValue.get(columnName4).add(catelogEnglishNameFirst);
                rowValue.get(columnName5).add(isEnd.toString());
            }
        }
        writeExcel(SECOND_CATELOG_FILE_PATH,columnNames,rowValue);
    }

    public void getAllThirdCatelog(String mainUrl){
        HTMLCacher htmlCacher = new HTMLCacher();
        NodeList nodeList = null;
        NodeList nodeListSecond = null;
        NodeList nodeListThird = null;
        try {
            nodeList = htmlCacher.getNodeList(mainUrl,"a","class","nav-top-title");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if(nodeList == null){
            System.out.println("根据mainUrl没有找到firstCatelogA");
            return;
        }
        LinkTag linkTag = null;
        String columnName1 = "catelogEnglishName";
        String columnName2 = "catelogChineseName";
        String columnName3 = "url";
        String columnName4 = "parentCatelogName";
        List<String> columnNames = new ArrayList<String>();
        columnNames.add(columnName1);
        columnNames.add(columnName2);
        columnNames.add(columnName3);
        columnNames.add(columnName4);
        List<String> columnName1Values = new ArrayList<String>();
        List<String> columnName2Values = new ArrayList<String>();
        List<String> columnName3Values = new ArrayList<String>();
        List<String> columnName4Values = new ArrayList<String>();
        Map<String,List<String>> rowValue = new HashMap<String, List<String>>();
        rowValue.put(columnName1,columnName1Values);
        rowValue.put(columnName2,columnName2Values);
        rowValue.put(columnName3,columnName3Values);
        rowValue.put(columnName4,columnName4Values);

        String catelogEnglishNameFirst = "";
        String catelogEnglishNameSecond = "";
        String catelogEnglishNameThird = "";
        String catelogChineseNameThird = "";
        String urlFirst = "";
        String urlSecond = "";
        String urlThird = "";
        SimpleNodeIterator simpleNodeIteratorSecond = null;
        SimpleNodeIterator simpleNodeIteratorThird = null;
        LinkTag linkTagSecond = null;
        LinkTag linkTagThird = null;
        SimpleNodeIterator simpleNodeIterator = nodeList.elements();
        while (simpleNodeIterator.hasMoreNodes()){
            linkTag = (LinkTag) simpleNodeIterator.nextNode();
            System.out.println(linkTag.getText());
            urlFirst = linkTag.getLink();
            if(urlFirst.indexOf(".html")<1){
                continue;
            }
            catelogEnglishNameFirst = urlFirst.substring(urlFirst.indexOf(".com.au/")+".com.au/".length(),urlFirst.indexOf(".html"));
            System.out.println(catelogEnglishNameFirst);
            try {
                nodeListSecond =  htmlCacher.getNodeList(urlFirst,"a","class","SecondMenuName");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
            if(nodeListSecond == null || nodeListSecond.size() < 1){
                System.out.println("根据mainUrl没有找到secondCatelogA");
                continue;
            }
            simpleNodeIteratorSecond = nodeListSecond.elements();
            while (simpleNodeIteratorSecond.hasMoreNodes()) {
                linkTagSecond = (LinkTag) simpleNodeIteratorSecond.nextNode();
                System.out.println(linkTagSecond.getLinkText());
                urlSecond = linkTagSecond.getLink();
                catelogEnglishNameSecond = urlSecond.substring(urlSecond.lastIndexOf("/")+1,urlSecond.indexOf(".html"));
                try {//开始抓取3级菜单
                    nodeListThird = htmlCacher.getNodeList(urlSecond,"a","class","SecondMenuName");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                if(nodeListThird == null || nodeListThird.size() < 1){
                    System.out.println("当前分类只有二级分类");
                    continue;
                }else {
                    simpleNodeIteratorThird = nodeListThird.elements();
                    while (simpleNodeIteratorThird.hasMoreNodes()) {
                        linkTagThird = (LinkTag) simpleNodeIteratorThird.nextNode();
                        System.out.println(linkTagThird.getLinkText());
                        catelogChineseNameThird = linkTagThird.getLinkText();
                        urlThird = linkTagThird.getLink();
                        catelogEnglishNameThird = urlThird.substring(urlThird.lastIndexOf("/") + 1, urlThird.indexOf(".html"));
                        urlThird = urlThird.substring(0,urlThird.lastIndexOf(".html")+".html".length());
                        rowValue.get(columnName1).add(catelogEnglishNameThird);
                        rowValue.get(columnName2).add(catelogChineseNameThird);
                        rowValue.get(columnName3).add(urlThird);
                        rowValue.get(columnName4).add(catelogEnglishNameSecond);
                    }
                }
            }
        }
        writeExcel(THIRD_CATELOG_FILE_PATH,columnNames,rowValue);
    }

    public void getFourCatelog(File file,int urlColumnIndex,int cateLogeNameIndex,int isEndIndex,boolean isEndCatelogFile) throws Exception {
        List<String> list = readExcel(file,urlColumnIndex);
        Map<String,List<String>> resultMap = new HashMap<String, List<String>>();
        Parser parser = null;
        if(list == null || list.size()<1){
            System.out.println("没有读取到导航文件链接地址");
            return;
        }
        List<String> listName = readExcel(file,cateLogeNameIndex);
        HTMLCacher htmlCacher = new HTMLCacher();
        List<String> tempList = null;
        List<String> listIsEnd = null;
        if(!isEndCatelogFile){
            listIsEnd = readExcel(file,isEndIndex);
        }
        int count = 1;
        String nextPageUrl = null;
        for(int i = 0;i<list.size();i++){
            count = 1;
            if(!isEndCatelogFile&&Boolean.FALSE.toString().equals(listIsEnd.get(i))){
                continue;
            }
            parser = new Parser(list.get(i));
            tempList = (getAllProductByPageUrl(htmlCacher,parser));
            if(tempList==null||tempList.size()<1){
                continue;
            }
            nextPageUrl = getNextPageUrl(htmlCacher,new Parser(list.get(i)));
            while (!htmlCacher.isEndPage(nextPageUrl)){
                count ++;
                tempList.addAll(getAllProductByPageUrl(htmlCacher,new Parser(nextPageUrl)));
                nextPageUrl = getNextPageUrl(htmlCacher,new Parser(nextPageUrl));
            }
            if(StringUtils.isNotEmpty(nextPageUrl)&&htmlCacher.isEndPage(nextPageUrl)){
                count ++;
                tempList.addAll(getAllProductByPageUrl(htmlCacher,new Parser(nextPageUrl)));
            }
            System.out.println("到达最后一页,"+listName.get(i)+"共"+count+"页，产品数："+tempList.size());
            resultMap.put(listName.get(i),tempList);
            nextPageUrl = null;
        }
        System.out.println("产品关系记录共："+resultMap.size());
        if(isEndCatelogFile){
            writeExcelFour(FIVE_CATELOG_FILE_PATH,resultMap);
        }else{
            writeExcelFour(FOUR_CATELOG_FILE_PATH,resultMap);
        }
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

    private List<String> getAllProductByPageUrl(HTMLCacher htmlCacher,Parser parser){
        List<String> resultList = new ArrayList<String>();
        NodeList nodeListKey = null;
        SimpleNodeIterator simpleNodeIteratorKey = null;
        LinkTag nodeKey = null;
        String urlKey = null;
        try {
            nodeListKey =  htmlCacher.getNodeList(parser,"a","class","ProductImg");
        } catch (Exception e) {
            System.out.println(e.getMessage());
             return null;
        }
        if(nodeListKey == null || nodeListKey.size() < 1){
            System.out.println("根据Url没有找到产品记录Key");
           return null;
        }

        simpleNodeIteratorKey = nodeListKey.elements();
        while (simpleNodeIteratorKey.hasMoreNodes()){
            nodeKey = (LinkTag)simpleNodeIteratorKey.nextNode();
            urlKey = nodeKey.getLink();
            //urlKey = urlKey.substring(0,urlKey.lastIndexOf("/"));
            resultList.add(urlKey.substring(urlKey.lastIndexOf("/")+1,urlKey.indexOf(".html")));
        }
        return resultList;
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



    private void writeExcelFour(String filePath,Map<String,List<String>> rowValue){
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
}
