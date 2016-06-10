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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import static java.util.Locale.filter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        
        final String TMPL_EXT = ".tmpl";
        
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        
        options.addOption(Option.builder("f")
                .numberOfArgs(2)
                .argName("in-file output folder")
                .required()
                .desc("required two value")
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
            throw ex;
        }
        
        
        int i = 0;
        
        for (String link : links) {
            i++;
            try {
                Document doc = Jsoup.connect(link).timeout(1000).get();
                String title = doc.getElementsByTag("title").text().replaceAll(" - Лео творит!", "");
                Element content = doc.getElementsByClass("entry-content").get(0);
                
                
                content.getElementsByTag("div").remove();
 
                String articleName = "articleTitle" + Integer.toString(i) + TMPL_EXT;
                        
                try (PrintWriter out = new PrintWriter(articleName)) {
                    out.println(title);
                }
                
                String contentName = "article" + Integer.toString(i) + TMPL_EXT;
                        
               
                try (PrintWriter out = new PrintWriter(contentName)) {
                    out.println(content.html());
                }
                
            } catch (IOException ex) {
                System.err.println("Error: " + ex);
                
            }
        }
    }
    
}
