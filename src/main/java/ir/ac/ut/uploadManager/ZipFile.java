/*
 * Copyright 2020 mohammad.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.ac.ut.uploadManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author mohammad
 */
public class ZipFile {

    public static File zipifyFiles(Map<String, String> files) {
        try {
            String zipFile = "sources.zip";
            /*
			 * To create a zip file, use
			 * 
			 * ZipOutputStream(OutputStream out)
			 * constructor of ZipOutputStream class.
             */
            //create object of FileOutputStream
            File zip = new File(zipFile);
            FileOutputStream fout = new FileOutputStream(zip);
            ZipOutputStream zout = new ZipOutputStream(fout);
            zout.setMethod(8);
            zout.setLevel(5);
            //create object of ZipOutputStream from FileOutputStream
            for (String className : files.keySet()) {

                //create object of FileInputStream for source file

                /*
				 * To begin writing ZipEntry in the zip file, use
				 * 
				 * void putNextEntry(ZipEntry entry)
				 * method of ZipOutputStream class.
				 * 
				 * This method begins writing a new Zip entry to 
				 * the zip file and positions the stream to the start 
				 * of the entry data.
                 */
                zout.putNextEntry(new ZipEntry(className + ".java"));
                zout.setComment(className + ".java");
                /*
                 * After creating entry in the zip file, actually
                 * write the file.
                 */
                byte[] data = files.get(className).getBytes();
                zout.write(data);


                /*
				 * After writing the file to ZipOutputStream, use
				 * 
				 * void closeEntry() method of ZipOutputStream class to 
				 * close the current entry and position the stream to 
				 * write the next entry.
                 */
                zout.closeEntry();
                //close the InputStrea

            }
            zout.close();

            return zip;

        } catch (IOException ioe) {
            System.out.println("IOException :" + ioe);
        }
        return null;
    }
}
