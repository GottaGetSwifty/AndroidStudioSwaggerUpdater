package updater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SwaggerUpdater {

    static final String tempFileName = "tempFile";
    static final String swaggerGenFilesFilePath = "/generated/android/src/main/java";
    public static void main(String[] args) throws Exception {

        String outputDir = args[0];
        String downloadUrl = args[1];
        try {
            byte[] downloadedBytes = downloadWithUrl(downloadUrl);
            File tempFile = Files.write(new File(tempFileName).toPath(), downloadedBytes).toFile();
            File destinationFile = new File(outputDir);
            unzipFiles(tempFile, destinationFile);
            copyFilesToDesiredPath(outputDir);
            tempFile.delete();
            deleteDir(new File(destinationFile, "generated"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyFilesToDesiredPath(String basePath) throws  IOException{

        File exportedFolder = new File(basePath + swaggerGenFilesFilePath);
        File desiredFolder = new File(basePath);
        mergeTwoDirectories(desiredFolder, exportedFolder);
    }


    private static byte[] downloadWithUrl(String url) throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().bytes();
    }

    private static void unzipFiles(File archive, File newFile) {
        try {
            ZipFile e = new ZipFile(archive);
            int entries = e.size();
            boolean total = false;
            Enumeration e1 = e.entries();

            while(e1.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)e1.nextElement();
                unzipEntry(e, entry, newFile);
            }

            e.close();
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {
        File outputFile = new File(outputDir, entry.getName());
        if(!outputFile.exists()) {
            if (entry.isDirectory()) {
                outputFile.mkdirs();
                return;
            }
            else {
                outputFile.createNewFile();
            }
        }
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }

    }

    public static int copy(BufferedInputStream in, BufferedOutputStream out) {
        byte[] buffer = new byte[1024];
        int count = 0;
        boolean n = false;

        try {
            int n1;
            while((n1 = in.read(buffer, 0, 1024)) != -1) {
                out.write(buffer, 0, n1);
                count += n1;
            }

            out.flush();
        } catch (IOException var18) {
            var18.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException var17) {
                var17.printStackTrace();
            }

            try {
                in.close();
            } catch (IOException var16) {
                var16.printStackTrace();
            }

        }

        return count;
    }

    public static void mergeTwoDirectories(File dir1, File dir2){
        String targetDirPath = dir1.getAbsolutePath();
        File[] files = dir2.listFiles();
        for (File file : files) {
            if(file.isDirectory()) {
                mergeTwoDirectories(new File(targetDirPath + File.separator + file.getName()), file);
            }
            file.renameTo(new File(targetDirPath + File.separator + file.getName()));
            System.out.println(file.getName() + " is moved!");
        }
    }

    static private void deleteDir(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteDir(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
}
