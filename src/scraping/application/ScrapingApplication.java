/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

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

//        String[] myArgs = {"-f", "links.txt", "site", "-t", "-l"};
        
        CLParser parser = new CLParser(args );
        ProccessingData obj = new ProccessingData(parser.getLinks(), parser.getOutputFolder(),
                parser.enableTeg(), parser.getLinkGen());
        obj.proccessLinks();
        
        
        
    }
}
