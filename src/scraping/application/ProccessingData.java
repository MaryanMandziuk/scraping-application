/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author maryan
 */
public class ProccessingData {
    private final String PAGE_EXT = ".page";
    private final String TMPL_EXT = ".tmpl";
    private final ExecutorService executor;
    
    public ProccessingData(List<String> links) {
        
        final int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        
        int i = 0;
        
        for (String link : links) {
            
            i++;
            String articleName = "articleTitle" + i + TMPL_EXT;
            String contentName = "article" + i + TMPL_EXT;
            String articleStructureName = "article" + i + PAGE_EXT;
            String articleHeader = "<h2 class=\"featurette-heading\">$file_articles_article"+i+"Title</h2>";
            String structure = "$file_structure_top\n" +
                                "  $file_articles_articleTitle" + i + "\n" +
                                "  $file_structure_fulltop\n" +
                                "    $file_articles_article" + i + "\n" +
                                "$file_structure_bottom";
            executor.execute(() -> {
                
                String threadName = Thread.currentThread().getName();
                System.out.println("Hello " + threadName);

                try {

                    Document doc = Jsoup.connect(link).timeout(5000).get();
                    String title = doc.getElementsByTag("title").text().replaceAll(" - Лео творит!", "");
                    Element content = doc.getElementsByClass("entry-content").get(0);

                    content.getElementsByTag("div").remove();
                    
                    try (PrintWriter out = new PrintWriter(articleName)) {
                        out.println(title);
                    }
                    try (PrintWriter out = new PrintWriter(contentName)) {
                        out.println(articleHeader);
                        out.println(content.html());
                    }
                    try (PrintWriter out = new PrintWriter(articleStructureName)) {
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
//            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdownNow();
            System.out.println("Scraping finished");
        }
    }
}
