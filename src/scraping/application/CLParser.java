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
    private boolean enableTeg = false;
    private boolean enableLinkGeneration = false;
    
    public CLParser(String[] args) throws ParseException, IOException {
        this.filesOption();
        this.tegOption();
        this.linksOption();
        
        try {
            
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("f")) {
                this.proccessFilesOption(commandLine);
            }
            if (commandLine.hasOption("t")) {
                this.enableTeg = true;
            }
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
        options.addOption(Option.builder("f")
                .numberOfArgs(2)
                .argName("in-file output folder")
                .required()
                .desc("required two value")
                .build());
    }
    
    private void tegOption() {
        options.addOption("t", false, "enable teg");
    }
    
    private void linksOption() {
        options.addOption("l", false, "enable links generation");
    }
    
    private void proccessFilesOption(CommandLine cl) throws FileNotFoundException, IOException {
        File resources = new File(cl.getOptionValues("f")[0]);
        outputFolder = new File(cl.getOptionValues("f")[1]);

        if (!outputFolder.isDirectory()) {
            outputFolder.mkdir();
        }

        BufferedReader read = new BufferedReader(new FileReader(resources));

        for (String link; (link = read.readLine()) != null;) {
            this.links.add(link);
        }
    }
    
    public boolean enableTeg() {
        return this.enableTeg;
    }
    
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
