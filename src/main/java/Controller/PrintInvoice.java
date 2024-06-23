package Controller;

/**
 *
 * @author M AYAN LAPTOP
 */
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrintInvoice {
    public static void print(String filePath) {
        File file = new File(filePath);
        Desktop desktop = Desktop.getDesktop();
        if (file.exists()) {
            try {
                desktop.print(file);
            } catch (IOException ex) {
                Logger.getLogger(PrintInvoice.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("The file does not exist.");
        }
    }
}

