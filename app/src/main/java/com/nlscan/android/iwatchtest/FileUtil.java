package com.nlscan.android.iwatchtest;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtil {

    public static void writeText(String fileName, String content){
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        try {
            fos = new FileOutputStream(new File(fileName));
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);
            bw.write(content);
        } catch (IOException e) {
            e.printStackTrace();

        }
        finally {
            closeStream(bw);
            closeStream(osw);
            closeStream(fos);
        }


    }


    /**
     * close stream
     * @param closeable
     */
    private static void closeStream(Closeable closeable){
        if (closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
