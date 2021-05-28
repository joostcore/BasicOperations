package Tools;

import Tools.exceptions.EndOfEntries;
import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrawlerParent {


    public static void toFiles(List<Object> obj,String filename) throws IOException {


        toJsonFile(obj, filename);
        toCsvFile(obj, filename);
        toExcelFile(obj, filename);


    }


    public static void toJsonFile(List<Object> objects, String filename) throws IOException {

        Gson gson = new Gson();
        String output = gson.toJson(objects);
        stringToFile(output, "listen/" + filename + ".txt");

    }

    public static void toCsvFile(List<Object> objects, String filename) throws IOException {

        List<String[]> csvRecords = new ArrayList<>();


        CSVWriter writer = new CSVWriter(new FileWriter("listen/" + filename + ".csv"),
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        List<String> fields = getObjectFieldNames(objects.get(0));

        String[] field_return = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            field_return[i] = fields.get(i) != null ? (String) fields.get(i) : "";
        }
        writer.writeNext(field_return);
        csvRecords.add(field_return);

        for (Object object : objects) {
            List<Object> properties = getObjectProperties(object);
            String[] prop_return = new String[properties.size()];
            for (int i = 0; i < properties.size(); i++) {
                prop_return[i] = properties.get(i) != null ? (String) properties.get(i) : "";
            }

            writer.writeNext(prop_return);
            csvRecords.add(prop_return);

        }

        writer.flush();


    }

    public static void toExcelFile(List<Object> objects, String filename) throws IOException {


        //Create blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet
        XSSFSheet spreadsheet = workbook.createSheet(filename);

        XSSFRow row;

        int counter = 1;

        row = spreadsheet.createRow(0);
        List<String> objectFieldNames = getObjectFieldNames(objects.get(0));
        int cellid = 0;
        for (String obje : objectFieldNames) {
            Cell cell = row.createCell(cellid++);
            cell.setCellValue((String) obje);
        }


        for (Object obj : objects) {
            row = spreadsheet.createRow(counter++);
            List<Object> objectArr = getObjectProperties(obj);
            cellid = 0;

            for (Object obje : objectArr) {
                Cell cell = row.createCell(cellid++);
                cell.setCellValue((String) obje);
            }
        }
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(new File("listen/" + filename + ".xlsx"));

        workbook.write(out);
        out.close();
    }

    public static List<Object> getObjectProperties(Object object) {

        Class<?> cl = object.getClass();
        List<Object> allObjects = new ArrayList<Object>();
        for (java.lang.reflect.Field f : cl.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                Object o = f.get(object);
                if (!f.getName().equals("this$0")) {
                    allObjects.add(o);
                }
            } catch (Exception e) {
            }
        }
        return allObjects;
    }

    public static List<String> getObjectFieldNames(Object object) {

        Class<?> cl = object.getClass();
        List<String> allObjects = new ArrayList<String>();
        for (java.lang.reflect.Field f : cl.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                if (!f.getName().equals("this$0")) {
                    allObjects.add(f.getName().replace("__","-").replace("_"," "));
                }
            } catch (Exception e) {
            }
        }
        return allObjects;
    }

    public static void stringToFile(String string, String filename) throws IOException {

        FileOutputStream fos = new FileOutputStream(new File(filename));
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        //convert string to byte array
        byte[] bytes = string.getBytes();
        //write byte array to file
        bos.write(bytes);
        bos.close();
        fos.close();

    }
}


