/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jsoup.select.Elements;
/**
 *
 * @author maryan
 */
public class ProccessingData {
    private final String PAGE_EXT = ".page";
    private final String TMPL_EXT = ".tmpl";
    private final ExecutorService executor;
    
    public ProccessingData(List<String> links, File outputFolder) {
        
        final int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        
        int i = 0;
        
        for (String link : links) {
            
            i++;
            String articleName = "article" + i + "Title" + TMPL_EXT;
            String contentName = "article" + i + TMPL_EXT;
            String articleMetaTags = "article" + i + "MetaTag" + TMPL_EXT;
            String articleStructureName = "article" + i + PAGE_EXT;
            String articleHeader = "<h2 class=\"featurette-heading\">$!{file_articles_article" + i + "Title}</h2>";
            String structure = "$!{file_structure_top}\n" +
                                "  $!{file_articles_article" + i + "MetaTag}\n" +
                                "   $!{file_structure_titleOpen}\n" +
                                "       $!{file_articles_article" + i + "Title}\n" +
                                "   $!{file_structure_titleColse}\n" + 
                                "  $!{file_structure_fulltop}\n" +
                                "    $!{file_articles_article" + i + "}\n" +
                                "$!{file_structure_bottom}";
            executor.execute(() -> {
                
                String threadName = Thread.currentThread().getName();
                System.out.println("Hello " + threadName);

                try {

                    Document doc = Jsoup.connect(link).timeout(5000).get();
                    String title = doc.getElementsByTag("title").text().replaceAll(" - Лео творит!", "");
                    Elements metaTags = doc.getElementsByAttributeValue("property", "article:tag");
                    Element content = doc.getElementsByClass("entry-content").get(0);

                    content.getElementsByTag("div").remove();
                    content.getElementsByAttributeValue("name", "cutid1").remove();
                    content.getElementsByAttributeValue("name", "cutid1-end").remove();
                    
                   
                    
//                    content.getElementsByTag("br").remove();
//                    content.prepend("<p>");
//                            
//                            for(Element el : content.children()){
//                                System.out.println("el-"+el +"  " + el.val());
//                                if(el.tag().toString() == "img") {
//                                    el.before("</p>");
//                                    el.after("<p>");
//                                    
//                                }
//                                
//                            }
                    
                    try (PrintWriter out = new PrintWriter(outputFolder + File.separator + articleMetaTags)) {
                        out.println(metaTags);
                    }
                    try (PrintWriter out = new PrintWriter(outputFolder + File.separator + articleName)) {
                        out.println(title);
                    }
                    try (PrintWriter out = new PrintWriter(outputFolder + File.separator + contentName)) {
                        out.println(articleHeader);
                        out.println(content.html());
                    }
                    try (PrintWriter out = new PrintWriter(outputFolder + File.separator + articleStructureName)) {
                        out.println(structure);
                    }

                } catch (IOException ex) {
                    System.err.println("Error: " + ex);    
                }
            });
        }
        shutdown();       
    }
    
       
    public final void shutdown() {
        executor.shutdown(); 
        try {
            executor.shutdown();
            executor.awaitTermination(15, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
//            System.err.println("tasks interrupted");
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdownNow();
            System.out.println("Scraping finished");
        }
    }
}
