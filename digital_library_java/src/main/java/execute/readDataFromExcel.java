package execute;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.Data;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class readDataFromExcel {

    public List<Data> readBooksFromExcelFile(String excelFilePath) throws IOException {
        // 1. Create Array list name listBooks.
        List<Data> listBooks = new ArrayList<Data>();
        // 2. Generate InputStream to read excel File.
        FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet firstSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = firstSheet.iterator();

        // 3.Use while loop to run row by row.
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();
            Iterator<Cell> cellIterator = nextRow.cellIterator();
            Data data = new Data();// call model data
            while (cellIterator.hasNext()) {
                Cell nextCell = cellIterator.next();
                int columnIndex = nextCell.getColumnIndex();
                switch (columnIndex) {
                    case 1: // case 1 related to Title
                        data.setTitle((String) getCellValue(nextCell));
                        break;
                    case 2: // case 2 related to Author
                        data.setAuthor((String) getCellValue(nextCell));
                        break;
                    case 3: // case 3 related to Id
                        double b = (Double) getCellValue(nextCell);
                        String bd = String.valueOf(b);
                        data.setbID(bd);
                        break;
                    case 4: // case 4 related to Year of Publishing
                        double y = (Double) getCellValue(nextCell);
                        String yd = String.valueOf(y);
                        data.setYear(yd);

                        break;
                    case 5: // case 5 related to Publisher
                        data.setPublisher((String) getCellValue(nextCell));
                        break;
                    case 6: // case 6 related to Collection
                        data.setCollection((String) getCellValue(nextCell));
                        break;
                    case 7: // case 7 related to Type of book
                        data.setType((String) getCellValue(nextCell));
                        break;
                    case 8: // case 8 related to Quantity of the book
                        double q = (Double) getCellValue(nextCell);
                        String qd = String.valueOf(q);
                        data.setQty(qd);
                        break;
                }
            }
            listBooks.add(data);
        }
        // 9.Close Stream
        workbook.close();
        inputStream.close();
        return listBooks;
    }

    // Get Cell Value
    @SuppressWarnings("deprecation")

    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();

            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();

            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
        }
        return null;
    }

    public List<Data> executeReadExcel() throws IOException {
        String excelFilePath = "/Users/victornguyen/Sites/BookDigital/src/main/resources/BookData.xlsx";
        readDataFromExcel read = new readDataFromExcel();
        List<Data> list = read.readBooksFromExcelFile(excelFilePath);
        return list;
    }

    public static void main(String[] args) throws IOException {
        String excelFilePath = "src/main/resources/BookData.xlsx";
        readDataFromExcel read = new readDataFromExcel();
        List<Data> list = read.readBooksFromExcelFile(excelFilePath);

        System.out.println("Read data from excel successfully");
        for (Data data : list) {
            String title = data.getTitle();
            String author = data.getAuthor();
            String bID = data.getbID().replace(".0", "");
            String qty = data.getQty().replace(".0", "");
            String type = data.getType();
            String year = data.getYear().replace(".0", "");
            String collection = data.getCollection();
            String publisher = data.getPublisher();

            System.out.println("ID:" + bID + "--Collect: " + collection);
        }
        /*
         * try{ FileOutputStream outputStream = new
         * FileOutputStream("D:/Studying/thesis/workspace/textData3.txt");
         * OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,
         * "UTF-16"); BufferedWriter bufferedWriter = new
         * BufferedWriter(outputStreamWriter);
         *
         * for (Data data : list) { String title = data.getTitle(); String author =
         * data.getAuthor(); String bID = data.getbID().replace(".0", ""); String qty =
         * data.getQty().replace(".0",""); String type = data.getType(); String year =
         * data.getYear().replace(".0", ""); String collection = data.getCollection();
         * String publisher = data.getPublisher();
         *
         * bufferedWriter.write(bID+" - "+title+" - "+author+" - "+collection+
         * " - "+publisher+" - "+type+" - " +year+" - "+qty); bufferedWriter.newLine();
         *
         *
         * System.out.println("ID:"+bID+"--Title : "+title+"--Author:"+author+
         * "--Collection:"+collection+
         * "--Publisher:"+publisher+"--Type:"+type+"--Year:"+year+"--Qty:"+qty); }
         * System.out.println("Succsessfully"); bufferedWriter.close(); }catch
         * (IOException e){ e.printStackTrace(); }
         */
    }
}
