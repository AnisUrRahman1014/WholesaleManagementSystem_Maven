package Controller;

/**
 *
 * @author Anis Ur Rahman
 */
import Model.Bill;
import Model.BillItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WordInvoiceGenerator {
    
    public static void generateWordReceipt(Bill bill) {
        // Add the logo (Placeholder for logo implementation)
        try (XWPFDocument document = new XWPFDocument()) {
            // Add the logo (Placeholder for logo implementation)
            XWPFParagraph logoParagraph = document.createParagraph();
            XWPFRun logoRun = logoParagraph.createRun();
            logoRun.setText("LOGO");
            // Add image to logoRun here if you have an image file
            // Add barcode image (Placeholder for barcode implementation)
            XWPFParagraph barcodeParagraph = document.createParagraph();
            XWPFRun barcodeRun = barcodeParagraph.createRun();
            barcodeRun.setText("BARCODE");
            // Add image to barcodeRun here if you have an image file
            // Add Invoice #
            XWPFParagraph invoiceNumberParagraph = document.createParagraph();
            invoiceNumberParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun invoiceNumberRun = invoiceNumberParagraph.createRun();
            invoiceNumberRun.setText("Invoice #: " + bill.getBillId());
            // Add Invoice Date
            XWPFParagraph invoiceDateParagraph = document.createParagraph();
            invoiceDateParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun invoiceDateRun = invoiceDateParagraph.createRun();
            invoiceDateRun.setText("Invoice Date: " + bill.getDate());
            // Add Office details
            XWPFParagraph officeParagraph = document.createParagraph();
            XWPFRun officeRun = officeParagraph.createRun();
            officeRun.setText("Office: Near Sadar Police Station, Sadar Chowk, Gujrat");
            // Add WhatsApp logo and number
            XWPFParagraph whatsappParagraph = document.createParagraph();
            XWPFRun whatsappRun = whatsappParagraph.createRun();
            whatsappRun.setText("WhatsApp: +92 300 096 5242");
            // Add image to whatsappRun here if you have an image file
            // Create table for bill details
            XWPFTable table = document.createTable();
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("Item");
            headerRow.addNewTableCell().setText("Quantity");
            headerRow.addNewTableCell().setText("Price");
            headerRow.addNewTableCell().setText("Total");
            for (BillItem item : bill.getBillItems()) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(item.getProduct().getProdName());
                row.getCell(1).setText(String.valueOf(item.getQuantity()));
                row.getCell(2).setText(String.valueOf(item.getRatePerUnit()));
                row.getCell(3).setText(String.valueOf(item.getTotal()));
            }
            // Write the document to file
            try (FileOutputStream out = new FileOutputStream("Invoice.docx")) {
                document.write(out);
            }
        } catch (IOException ex) {
            Logger.getLogger(WordInvoiceGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

