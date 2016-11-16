import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2016-11-2.
 */
public class ExcelHandller {

    public void writeExcelBrand(OutputStream os, NodeList nodeList)
    {
        try
        {
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet ws = wwb.createSheet("product",0);
            Label label = new Label(0,0,"product_name");
            Label label2 = new Label(1,0,"href");
            ws.addCell(label);
            ws.addCell(label2);
            SimpleNodeIterator iterator =  nodeList.elements();
            LinkTag linkTag = null;
            int y = 1;
        while (iterator.hasMoreNodes()){
            linkTag = (LinkTag)iterator.nextNode();
            Label labels = new Label(0,y,linkTag.getLinkText());
            Label labels1 = new Label(1,y,linkTag.getLink());
            ws.addCell(labels);
            ws.addCell(labels1);
            y++;
        }
            wwb.write();
            wwb.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeExcel(OutputStream os
            , List<String> nameList, Map<String,List<String>> valueMap)
    {
        WritableWorkbook wwb = null;
        WritableSheet ws = null;
        try
        {
            wwb = Workbook.createWorkbook(os);
            ws = wwb.createSheet("sheet",0);
            Label label = null;
            Label label2 = null;
            Iterator iterator = null;
            for(int i=0;i<nameList.size();i++){
                label =  new Label(i,0,nameList.get(i));
                ws.addCell(label);
                for(int j = 0;j<valueMap.get(nameList.get(i)).size();j++){
                    label2 = new Label(i,j+1,(String)valueMap.get(nameList.get(i)).get(j));
                    ws.addCell(label2);
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

    public void writeExcelByMap(OutputStream os,Map<String, List<String>> valueMap)
    {
        WritableWorkbook wwb = null;
        WritableSheet ws = null;
        try
        {
            wwb = Workbook.createWorkbook(os);
            ws = wwb.createSheet("sheet",0);
            Label label = null;
            Label label2 = null;
            Iterator iterator = null;
            iterator = valueMap.keySet().iterator();
            System.out.println("收到写入数据:"+valueMap.keySet().size());
            String key = null;
            List<String> tempList = null;
            int count = 0;
            while (iterator.hasNext()){
                key = (String)iterator.next();
                tempList = valueMap.get(key);
                for(int j = 0;j<tempList.size();j++){
                      label = new Label(0,count,key);
                      label2 = new Label(1,count,tempList.get(j));
                      ws.addCell(label);
                      ws.addCell(label2);
                    count++;
                 }
                System.out.println("写入key:"+key+"数量:"+tempList.size());
            }
            System.out.println("总条数："+count);
            wwb.write();
            wwb.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
