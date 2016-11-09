import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.IteratorImpl;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import java.io.File;
import java.io.OutputStream;

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
}
