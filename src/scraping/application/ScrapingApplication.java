/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.cli.ParseException;



/**
 *
 * @author maryan
 */
public class ScrapingApplication {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws org.apache.commons.cli.ParseException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {

        String[] myArgs = {"-f", "links.txt", "2"};

        CLParser parser = new CLParser(myArgs);
        new ProccessingData(parser.getLinks());    
        ScrapingApplication b = new ScrapingApplication();
        File files = b.getFile("site-name");
        File[] f = files.listFiles();
        for (File ff: f) {
            System.out.println(ff.getPath());
        }
        
    }
    
    public File getFile(String fileName) {
	ClassLoader classLoader = getClass().getClassLoader();
	File file = new File(classLoader.getResource(fileName).getPath());
        System.out.println(file.getAbsolutePath());
	return file;
  }
}
