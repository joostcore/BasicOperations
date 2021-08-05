package Tools;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
                CSVWriter.NO_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);


        List<FieldHolder> fields = getObjectProperties(objects.get(0));


        String[] field_return = new String[fields.size()];
        int f = 0;
        for (FieldHolder s : fields) {
            field_return[f] = s.getName();
            f++;
        }
        writer.writeNext(field_return);
        csvRecords.add(field_return);

        for (Object object : objects) {
            List<FieldHolder> properties = getObjectProperties(object);
            String[] prop_return = new String[properties.size()];
            int l = 0;
            for (FieldHolder fieldHolder : properties) {
                Object s = fieldHolder.getObject();
                if (s == null) {
                    prop_return[l] = "";
                } else if (s.getClass().equals(new String().getClass())) {
                    String string = (String) s;
                    prop_return[l] = string.replace(";",",");
                } else if (s.getClass().equals(new String[]{}.getClass())) {
                    StringBuilder sb = new StringBuilder();
                    for (String str : (String[]) s) {
                        sb.append(str.replace(";",",") + ", ");
                    }
                    prop_return[l] = sb.toString();
                } else if (s.getClass().isArray()) {
                    prop_return[l] = Arrays.deepToString((Object[]) s).replace(";",",");
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

        int counter = 0;
        row = spreadsheet.createRow(counter);
        List<FieldHolder> objectFieldNames = getObjectProperties(objects.get(0));
        int cellid = 0;
        for (FieldHolder obje : objectFieldNames) {
            Cell cell = row.createCell(cellid++);
            cell.setCellValue(obje.getName());
        }
        counter++;
        for (Object obj : objects) {
            row = spreadsheet.createRow(counter++);
            List<FieldHolder> objectArr = getObjectProperties(obj);
            cellid = 0;

            for (FieldHolder fieldHolder : objectArr) {
                Object obje = fieldHolder.getObject();
                Cell cell = row.createCell(cellid++);
                if (obje == null) {
                    cell.setCellValue(" ");
                } else if (obje.getClass().equals(new String().getClass())) {
                    cell.setCellValue((String) obje);
                } else if (obje.getClass().equals(new String[]{}.getClass())) {
                    StringBuilder sb = new StringBuilder();
                    for (String str : (String[]) obje) {
                        sb.append(str + ", ");
                    }
                    cell.setCellValue(sb.toString());
                } else if (obje.getClass().isArray()) {
                    cell.setCellValue(Arrays.deepToString((Object[]) obje));
                } else {
                    cell.setCellValue(" ");
                }
            }
        }
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(new File("listen/" + filename + ".xlsx"));

        workbook.write(out);
        out.close();
    }

    public static List<FieldHolder> getObjectProperties(Object object) {

        Class<?> cl = object.getClass();
        List<FieldHolder> allObjects = new ArrayList<>();
        for (java.lang.reflect.Field f : cl.getDeclaredFields()) {
            try {
                synchronized (f) {
                    f.setAccessible(true);
                    if (!Modifier.isTransient(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                        if (object == null) {
                            allObjects.add(new FieldHolder(f.getName(), null));
                            if (f.getType().isArray()) {

                            } else {
                                if (f.getType().getPackage().getName().contains(cl.getPackage().getName())) {
                                    allObjects.addAll(getAllSubPropertiesOfAnObject(null, f.getType()));
                                }
                            }
                        } else {
                            Object o = f.get(object);
                            if (!f.getName().equals("this$0") && !Modifier.isTransient(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                                allObjects.add(new FieldHolder(f.getName(), o));
                                if (f.getType().isArray()) {

                                } else {
                                    if (f.getType().getPackage().getName().contains(object.getClass().getPackage().getName())) {
                                        allObjects.addAll(getAllSubPropertiesOfAnObject(o, f.getType()));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("FieldName :" + f.getName());
                System.out.println("FieldName :" + f.getType().isArray());
                System.out.println("FieldName :" + f.getType().getComponentType());
                System.out.println("FieldName :" + f.getType().getComponentType().getPackage().getName());

                System.exit(1);
            }
        }
        return allObjects;
    }

    private static List<FieldHolder> getAllSubPropertiesOfAnObject(Object object, Class<?> type) {

        //watch out for depth
        Class<?> cl = type;
        List<FieldHolder> allObjects = new ArrayList<>();
        for (java.lang.reflect.Field f : cl.getDeclaredFields()) {
            try {
                synchronized (f) {
                    f.setAccessible(true);

                    if (!Modifier.isTransient(f.getModifiers())&& !Modifier.isPrivate(f.getModifiers())) {
                        if (object == null) {
                            allObjects.add(new FieldHolder(f.getName(), null));
                            if (f.getType().isArray()) {

                            } else {
                                if (f.getType().getPackage().getName().contains(cl.getPackage().getName())) {
                                    allObjects.addAll(getAllSubPropertiesOfAnObject(null, f.getType()));
                                }
                            }
                        } else {
                            Object o = f.get(object);
                            if (!f.getName().equals("this$0") && !Modifier.isTransient(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                                allObjects.add(new FieldHolder(f.getName(), o));
                                if (f.getType().isArray()) {

                                } else {
                                    if (f.getType().getPackage().getName().contains(object.getClass().getPackage().getName())) {
                                        allObjects.addAll(getAllSubPropertiesOfAnObject(o, f.getType()));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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


