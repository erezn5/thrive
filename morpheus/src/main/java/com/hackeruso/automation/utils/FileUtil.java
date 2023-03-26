package com.hackeruso.automation.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public final class FileUtil {

    public static Properties createPropertiesFromResource(Class clazz, String relativePath){
        try(InputStream ips = clazz.getClassLoader().getResourceAsStream(relativePath)){
            Properties prop = new Properties();
            prop.load(ips);
            return prop;
        }catch (IOException e){
            System.err.printf("Failed to convert resource %s + stream to properties, cause: %s%n", relativePath, e.getMessage());
            return null;
        }
    }

    public static long getFileSizeAsKiloByte(File file){
        return file.length()/1024;
    }

    public static boolean createNewFile(File file){
        if(file.exists()){
            Log.i("file=[%s] already exist", file.getAbsolutePath());
            return true;
        }else{
            try{
                if(file.createNewFile()){
                    Log.i("file=[%s] created successfully" , file.getAbsolutePath());
                    return true;
                }else{
                    Log.e("failed to create file=[%s]", file.getAbsolutePath());
                }
            }catch (IOException e){
                Log.e(e, "error occur creating file=[%s]", file.getAbsolutePath());
            }
        }
        return false;
    }

    private FileUtil(){}

    public static String getFile(String path){
        return new File(System.getProperty("user.dir")).getParent() + path;
    }

    public static String getAbsoluteFilePath(String path){
        return new File(System.getProperty("user.dir")) + path;
    }

    public static void createFolder(File folder , boolean recursive){
        if(folder.exists() && folder.isDirectory()){
            Log.info(folder.getName() + " directory already exist");
        }else if((recursive ? folder.mkdirs() : folder.mkdir())){
            Log.info(folder.getName() + " directory created successfully");
        }else{
            Log.error("failed to create '" + folder.getName() + "' directory");
        }
    }

    public static void writeToFile(String filePath, List<String> lines) {
        try(FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
            for(String line : lines){
                bufferedWriter.write(line);
                bufferedWriter.write('\n');
            }
            bufferedWriter.flush();
        }catch (Exception e){
            Log.e(e ,"failed write to file=[%s]" , filePath);
        }
    }

    public static String  handlePDFFile(String filePath) throws IOException {
        File file = new File(filePath);
        PDDocument doc = PDDocument.load(file);
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        String text = pdfTextStripper.getText(doc);
        doc.close();
        return text;
    }

    public static List<String> listFilesForFolder(final File folder) {
        List<String> fileArr = new ArrayList<>();

        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                fileArr.add(fileEntry.getName());
                System.out.println(fileEntry.getName());
            }
        }

        return fileArr;
    }
}
