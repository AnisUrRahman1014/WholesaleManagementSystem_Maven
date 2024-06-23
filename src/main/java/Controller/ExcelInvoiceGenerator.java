/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author M AYAN LAPTOP
 */
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import Model.Bill;
import Model.BillItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExcelInvoiceGenerator {
    public static void generateExcel(Bill bill){
        String excelFilePath = System.getProperty("user.dir").concat("\\assets\\invoice2.xlsx");
        System.out.println(excelFilePath);
        
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
             
            Sheet sheet1 = workbook.getSheetAt(0);
            Sheet sheet2 = workbook.getSheetAt(1);

            populateSheet(sheet1, bill);
            populateSheet(sheet2, bill);
            
            // Adjust print settings for both sheets
            setPrintSettings(sheet1);
            setPrintSettings(sheet2);
            
            
            // Create directory if it doesn't exist
            Path invoicesDir = Paths.get(System.getProperty("user.home"), "Documents", "WsMS Invoices");
            if (!Files.exists(invoicesDir)) {
                Files.createDirectories(invoicesDir);
            }
            
            // Save the updated file
            String fileName=bill.getBillId().concat(".xlsx");
            try (FileOutputStream fos = new FileOutputStream(invoicesDir.resolve(fileName).toFile())) {
                workbook.write(fos);
            }
            PrintInvoice.print(invoicesDir.resolve(fileName).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void copyRow(Row srcRow, Row destRow) {
        destRow.setHeight(srcRow.getHeight());

        for (int i = srcRow.getFirstCellNum(); i < srcRow.getLastCellNum(); i++) {
            Cell oldCell = srcRow.getCell(i);
            Cell newCell = destRow.createCell(i);

            if (oldCell == null) {
                newCell = null;
                continue;
            }

            // Copy style from old cell and apply to new cell
            CellStyle newCellStyle = destRow.getSheet().getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCellStyle.setBorderBottom(BorderStyle.THIN);
            newCellStyle.setBorderLeft(BorderStyle.THIN);
            newCellStyle.setBorderRight(BorderStyle.THIN);
            newCell.setCellStyle(newCellStyle);
        }
    }
    
    private static void populateSheet(Sheet sheet, Bill bill){
        Row row;
        Cell cell;
        // BILL ID
        row = sheet.getRow(3);
        cell = row.getCell(5);
        cell.setCellValue(bill.getBillId());

        // BILL DATE
        row = sheet.getRow(4);
        cell = row.getCell(5);
        cell.setCellValue(bill.getDate().toString());

        // CUSTOMER NAME
        row = sheet.getRow(6);
        cell = row.getCell(5);
        cell.setCellValue(bill.getCustomer().getName());

        // CUSTOMER ADDRESS
        row = sheet.getRow(8);
        cell = row.getCell(5);
        cell.setCellValue(bill.getCustomer().getAddress());

        // CUSTOMER PHONE
        row = sheet.getRow(10);
        cell = row.getCell(5);
        cell.setCellValue(bill.getCustomer().getContact());
        int currentRow = 13;
        int currentCol = 0;
        Row templateRow = sheet.getRow(13);
        for(BillItem item: bill.getBillItems()){
            sheet.shiftRows(currentRow+1, sheet.getLastRowNum(), 1);
            row = sheet.createRow(currentRow+1);
            copyRow(templateRow, row);
            for(int col=currentCol;col<=6;col++){
                cell = row.getCell(col);
                switch(col){
                    case 0 -> cell.setCellValue(item.getProduct().getProdID());
                    case 1 -> cell.setCellValue(item.getProduct().getProdName());
                    case 2 -> cell.setCellValue(item.getQuantity());
                    case 3 -> cell.setCellValue(item.getRatePerUnit());
                    case 4 -> cell.setCellValue(item.getDiscount());
                    case 5 -> {
                        if(cell == null){
                            cell = row.createCell(col);
                        }
                        cell.setCellValue(item.getTotal());
                    }
                }
            }
            currentRow++;
        }
        sheet.removeRow(sheet.getRow(13)); //index of the first row if receipt being used as the sample template
        sheet.shiftRows(14,sheet.getLastRowNum(),-1);
        currentRow++;
        // TOTAL BILL
        row = sheet.getRow(currentRow);
        cell = row.getCell(1);
        cell.setCellValue(bill.getTotalBill());

//            currentRow++;
        // TOTAL BILL
        row = sheet.getRow(currentRow);
        cell = row.getCell(3);
        cell.setCellValue(bill.getTax());

//            currentRow++;
        // TOTAL Discount
        row = sheet.getRow(currentRow);
        cell = row.getCell(5);
        cell.setCellValue(bill.getDiscountOnTotal());

        currentRow++;
        currentRow++;
        System.out.println(currentRow);
        // TOTAL FINAL BILL
        row = sheet.getRow(currentRow);
        cell = row.getCell(1);
        cell.setCellValue(bill.getTotalFinalBill());


//            currentRow++;
        // DEPOSIT
        row = sheet.getRow(currentRow);
        cell = row.getCell(3);
        cell.setCellValue(bill.getDeposit());

//            currentRow++;
        // CREDIT
        row = sheet.getRow(currentRow);
        cell = row.getCell(5);
        cell.setCellValue(bill.getCredit());
    }
    
    private static void setPrintSettings(Sheet sheet) {
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setAutobreaks(true);
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitHeight((short) 1);
        sheet.getPrintSetup().setFitWidth((short) 1);
    }
}

