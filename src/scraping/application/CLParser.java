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
/**
 * Class for working with command line
 * @author maryan
 */
public class CLParser {
    
    private CommandLineParser parser = new DefaultParser();
    private Options options = new Options();
    private List<String> links = new ArrayList<>();
    private File outputFolder;
//    private boolean enableTag = false;
    private boolean enableLinkGeneration = false;
    
    public CLParser(String[] args) throws ParseException, IOException {
        this.filesOption();
//        this.tagOption();
        this.linksOption();
        this.outputFolderOption();
        try {
            
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("f")) {
                this.proccessOutputFolderOption(commandLine);
            }
            if (commandLine.hasOption("i")) {
                this.proccessFileLinkOption(commandLine);
            }
//            if (commandLine.hasOption("t")) {
//                this.enableTag = true;
//            }
            if (commandLine.hasOption("l")) {
                this.enableLinkGeneration = true;
            }
            
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("scraping application", options);
            throw ex;
        }
    }
    
    private void filesOption() {
        options.addOption(Option.builder("i")
                .numberOfArgs(1)
                .argName("in-file")
                .desc("required one value")
                .build());
    }
    
    private void outputFolderOption() {
        options.addOption(Option.builder("f")
                .numberOfArgs(1)
                .argName("in-file")
                .required()
                .desc("required one value")
                .build());
    }
     
//    private void tagOption() {
//        options.addOption("t", false, "enable tag");
//    }
    
    private void linksOption() {
        options.addOption("l", false, "enable links generation");
    }
    
    private void proccessOutputFolderOption(CommandLine cl) throws FileNotFoundException, IOException {
        outputFolder = new File(cl.getOptionValue("f"));

        if (!outputFolder.isDirectory()) {
            outputFolder.mkdir();
        }
    }
    
    private void proccessFileLinkOption(CommandLine cl) throws FileNotFoundException, IOException {
        File resources = new File(cl.getOptionValue("i"));
        BufferedReader read = new BufferedReader(new FileReader(resources));

        for (String link; (link = read.readLine()) != null;) {
            this.links.add(link);
        }
    }
    
//    public boolean enableTag() {
//        return this.enableTag;
//    }
    
    public List getLinks() {
        return this.links;
    }
    
    public File getOutputFolder() {
        return outputFolder;
    }
    
    public boolean getLinkGen() {
        return enableLinkGeneration;
    }
}
