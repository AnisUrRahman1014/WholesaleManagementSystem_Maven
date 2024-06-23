package Controller;

/**
 *
 * @author Anis Ur Rahman
 */
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
public class ManagementSystemCPU {
    PrinterJob job=null;
    public static Dimension getScreenSize(){
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height-10;
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        Dimension screenSize = new Dimension(screenWidth, screenHeight);
        return screenSize;
    }
    
    private String[] splitString(String input, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = input.split("\\s+");
        StringBuilder currentLine = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; i++) {
            if (currentLine.length() + words[i].length() + 1 <= maxLength) {
                currentLine.append(" ").append(words[i]);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(words[i]);
            }
        }

        lines.add(currentLine.toString());

        return lines.toArray(new String[0]);
    }

//    private void printPanel(String orderID, String billType, String contact){
//        job = PrinterJob.getPrinterJob();
//        java.sql.Date sqlDate= new java.sql.Date(new java.util.Date().getTime());
//        job.setJobName(sqlDate+" _"+String.valueOf(new java.util.Date().getTime())); 
//        PageFormat page=job.defaultPage();
//        Paper paper = new Paper();
//        paper.setImageableArea(0, 0, 200.0, orderItems.size()*500.0);
//        page.setPaper(paper);
//        job.setPrintable((Graphics pg, PageFormat pf, int pageNum) -> {  
//            if(pageNum > 0){
//                return Printable.NO_SUCH_PAGE;
//            }
//            Graphics2D g2 = (Graphics2D)pg;
//            g2.translate(pf.getImageableX(), pf.getImageableY());
//            g2.scale(0.9,0.9);
//            int y1 = 20;
//            int yShift=10;
//            int headerRectHeight=15;
//            g2.setFont(new Font("Century Gothic",Font.BOLD,12));
//            g2.drawString("INVOICE", 0, y1);
//            y1 += yShift;
//            g2.setFont(new Font("Century Gothic",Font.BOLD,10));
//            g2.drawString("RM Fast Food Corner", 0, y1);
//            y1 += yShift;
//            g2.setFont(new Font("Century Gothic",Font.PLAIN,9));
//            g2.drawString("JalalPur Road, Gujrat", 0, y1);
//            y1 += yShift;
//            g2.drawString("+92 300 627 9757", 0, y1);
//            y1 += yShift;
//            g2.drawLine(0, y1, 240, y1);
//            y1 += headerRectHeight;
//            g2.drawString("Order ID: "+orderID, 0, y1); y1 += yShift;
//            g2.drawString("Order Type: "+billType, 0, y1); y1 += yShift;
//            g2.drawString("Customer: "+contact, 0, y1);           
//            y1 += headerRectHeight;
//            g2.drawLine(0, y1, 240, y1);
//            y1 += headerRectHeight;
//            g2.drawString("Name", 0, y1);
//            g2.drawString("Quantity", 100, y1);
//            g2.drawString("Price", 165, y1);
//            y1 += yShift;
//            g2.drawLine(0, y1, 240, y1);
//            y1 += headerRectHeight;
//            for (OrderItem i : orderItems) {
//                String itemString = i.toString();
//                int itemStringStart = 0;
//                int quantityStringStart = 100;
//
//                // Split the itemString into lines if it's too long
//                String[] lines = splitString(itemString, 20);
//                int indexCount=0;
//                for (String line : lines) {
//                    g2.drawString(line, itemStringStart, y1);
//                    indexCount++;
//                    if(indexCount<lines.length){
//                        y1 += yShift;
//                    }                    
//                }
//                g2.drawString(String.valueOf(i.getQuantity()), quantityStringStart, y1);
//                g2.drawString("x " + String.valueOf(i.getUnitPrice()), quantityStringStart+10, y1);
//                g2.drawString(String.valueOf(i.getTotalPrice()), 165, y1);
//                y1 += yShift+10;
//            }
//            g2.drawLine(0, y1, 240, y1);
//            y1 += headerRectHeight;
//            g2.drawLine(0, y1, 240, y1);
//            y1 += headerRectHeight;
//            g2.drawString("Total Amount", 0, y1);
//            g2.drawString(":", 75, y1);
//            g2.drawString(totalPriceField.getText(), 165, y1);
//            y1 += yShift;
//            g2.drawLine(0, y1, 240, y1);
//            y1 += headerRectHeight;
//            g2.drawString("Date", 0, y1);
//            g2.drawString(":", 65, y1);
//            g2.drawString(String.valueOf(LocalDate.now()), 135, y1);
//            y1 += yShift;
//            g2.drawString("*******************************************************************", 0, y1);
//            y1 += yShift;
//            g2.drawString("THANK YOU SO MUCH", 45, y1);
//            return Printable.PAGE_EXISTS;
//        },page);
//        try{
//                PrintService printService=job.getPrintService();
//                if(printService==null)
//                {
//                    job.printDialog();
//                    printService = job.getPrintService();
//                }
//                if(printService!=null) {
//                    
//                    job.print();
//                }
//        } catch (PrinterException ex) {
//            ex.printStackTrace();
//        }
//    }
    
    public static void print(Object content){
        
    }
}
