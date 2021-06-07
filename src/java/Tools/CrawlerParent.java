package Tools;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CrawlerParent {


    public static void toFiles(List<Object> obj, String filename) throws IOException {


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

        HashMap<String, Object> fields = getObjectProperties(objects.get(0));


        String[] field_return = new String[fields.size()];
        int f = 0;
        for (String s : fields.keySet()) {
            field_return[f] = s;
            f++;
        }
        writer.writeNext(field_return);
        csvRecords.add(field_return);

        for (Object object : objects) {
            HashMap<String, Object> properties = getObjectProperties(object);
            String[] prop_return = new String[properties.size()];
            int l = 0;
            for (Object s : fields.values()) {
                if (s == null) {
                    prop_return[l] = "";
                } else if (s.getClass().equals(new String().getClass())) {
                    prop_return[l] = (String) s;
                } else if (s.getClass().equals(new String[]{}.getClass())) {
                    StringBuilder sb = new StringBuilder();
                    for (String str : (String[]) s) {
                        sb.append(str + ", ");
                    }
                    prop_return[l] = sb.toString();
                } else {
                    prop_return[l] = "";
                }
                l++;
            }
            writer.writeNext(prop_return);
            csvRecords.add(prop_return);
        }

        writer.flush();


    }

    public static void toExcelFile(List<Object> objects, String filename) throws IOException {


        String string = new String();
        //Create blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet
        XSSFSheet spreadsheet = workbook.createSheet(filename);

        XSSFRow row;

        int counter = 1;

        row = spreadsheet.createRow(0);
        HashMap<String, Object> objectFieldNames = getObjectProperties(objects.get(0));
        int cellid = 0;
        for (String obje : objectFieldNames.keySet()) {
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(obje);
        }


        for (Object obj : objects) {
            row = spreadsheet.createRow(counter++);
            HashMap<String, Object> objectArr = getObjectProperties(obj);
            cellid = 0;

            for (Object obje : objectArr.values()) {
                Cell cell = row.createCell(cellid++);
                if (obje == null) {
                    cell.setCellValue("");
                } else if (obje.getClass().equals(new String().getClass())) {
                    cell.setCellValue((String) obje);
                } else if (obje.getClass().equals(new String[]{}.getClass())) {
                    StringBuilder sb = new StringBuilder();
                    for (String str : (String[]) obje) {
                        sb.append(str + ", ");
                    }
                    cell.setCellValue(sb.toString());
                } else {
                    cell.setCellValue("");
                }
            }
        }
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(new File("listen/" + filename + ".xlsx"));

        workbook.write(out);
        out.close();
    }

    private static HashMap<String, Object> getObjectProperties(Object object) {

        Class<?> cl = object.getClass();
        HashMap<String, Object> allObjects = getObjectFieldNames(cl);
        for (java.lang.reflect.Field f : cl.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                Object o = f.get(object);
                if (!f.getName().equals("this$0")) {
                    allObjects.put(f.getName(), o);
                    if (o.getClass().getPackage().getName().contains(object.getClass().getPackage().getName())) {
                        allObjects.putAll(getAllSubPropertiesOfAnObject(o));
                    }
                }
            } catch (Exception e) {
            }
        }
        return allObjects;
    }


    private static HashMap<String, Object> getAllSubPropertiesOfAnObject(Object obj) {

        //watch out for depth
        Class<?> cl = obj.getClass();
        HashMap<String, Object> allObjects = new HashMap<>();
        for (java.lang.reflect.Field f : cl.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                Object o = f.get(obj);
                if (!f.getName().equals("this$0")) {
                    allObjects.put(f.getName(), o);
                    if (o.getClass().getPackage().getName().contains(obj.getClass().getPackage().getName())) {
                        allObjects.putAll(getAllSubPropertiesOfAnObject(o));
                    }
                }
            } catch (Exception e) {
            }
        }
        return allObjects;
    }


    private static HashMap<String, Object> getObjectFieldNames(Class<?> object) {
        HashMap<String, Object> allObjects = new HashMap<>();
        for (java.lang.reflect.Field f : object.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                if (!f.getName().equals("this$0")) {
                    allObjects.put(f.getName().replace("__", "-").replace("_", " "), null);
                    if (f.getType().getPackage().getName().contains(object.getPackage().getName())) {
                        allObjects.putAll(getObjectFieldNames(f.getType()));
                    }
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


