/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author maryan
 */
public class ScrapingApplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        
        options.addOption(Option.builder("f")
                .numberOfArgs(2)
                .argName("in-file output folder")
                .required()
                .desc("required two val")
                .build());
        
        List<String> links = new ArrayList<>();
        
        String[] myArgs = {"-f", "links.txt", "2"};
        
        try {
            CommandLine commandLine = parser.parse(options, myArgs);
            
            if (commandLine.hasOption("f")) {
                File resources = new File(commandLine.getOptionValues("f")[0]);
                File outputFolder = new File(commandLine.getOptionValues("f")[1]);
                
                if (!outputFolder.isDirectory()) {
                    outputFolder.mkdir();
                }
                
                BufferedReader read = new BufferedReader(new FileReader(resources));
                
                for (String link; (link = read.readLine()) != null;) {
                    links.add(link);
                }
                
            }
            
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("scraping application", options);
        }
        
        for (String link : links) {
            try {
                Document doc = Jsoup.connect(link).timeout(1000).get();
                System.out.println("Link=" + link);                
                System.out.println(doc.html());
            } catch (IOException ex) {
                System.err.println("Error: " + ex);
                
            }
        }
    }
    
}
