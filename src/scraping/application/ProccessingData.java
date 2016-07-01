/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.jsoup.select.Elements;
/**
 *
 * @author maryan
 */
public class ProccessingData {
    private final String PAGE_EXT = ".page";
    private final String TMPL_EXT = ".tmpl";
    private final ExecutorService executor;
    private final int WIDTH_PROPORTION = 9;
    private final int HEIGHT_PROPORTION = 7;
    
    public ProccessingData(List<String> links, File outputFolder) throws IOException {
        
        final int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        
        
        File imagesFolder = new File(outputFolder + File.separator + "images");
        if (!imagesFolder.exists()) {
            if (!imagesFolder.mkdir()) {
                System.err.println("Enable to create images folder");
            }
        }
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
                                "   $!{file_structure_titleClose}\n" + 
                                "  $!{file_structure_fulltop}\n" +
                                "    $!{file_articles_article" + i + "}\n" +
                                "$!{file_structure_bottom}";
            File destination = new File(imagesFolder + File.separator + "articleImage" + i);
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
                    content.getElementsByAttributeValue("class", "i-ljuser-userhead").remove();
                    content.getElementsByTag("img").attr("class", "img-responsive");

//                    getEnglishWords(content.text());
                    getFrequencyWords(content.text());
                    URL imageUrl = new URL(content.getElementsByTag("img").get(0).attr("abs:src"));
                    proccessImage(imageUrl, destination);
//                    FileUtils.copyURLToFile(imageUrl,  destination);
                    
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
    
    private int getProportion(int width, int height) {
        
        int a = width / WIDTH_PROPORTION;
        int b = height / HEIGHT_PROPORTION;    
        
        if ( a < b) {
            return a;
        }
        return b;
    }
    
    private void proccessImage(URL imageUrl, File destination) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageUrl);
        } catch (IOException e) {
            System.err.println("Error read image: " + e);
        }
        int p = getProportion(img.getWidth(), img.getHeight());
       
        img = img.getSubimage(0, 0, p * WIDTH_PROPORTION, p * HEIGHT_PROPORTION);
        
        try {
            
            ImageIO.write(img, "png", destination);
        } catch (IOException e) {
             System.err.println("Error write image: " + e);
        }
    }
           
    public final void shutdown() {
        executor.shutdown(); 
        try {
            executor.shutdown();
            executor.awaitTermination(55, TimeUnit.SECONDS);
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
    
    private String getEnglishWords(String text) {
        final String pattern = "([\\w-&&[^0-9]]+)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        StringBuilder s = new StringBuilder();
        while (m.find()) {
            String word = m.group();
            s.append(word);
            s.append(", ");
        }
        s.delete(s.length() - 2, s.length() -1);
        System.out.println("builder: " + s.toString());
        return "";
    }
    
    private String getFrequencyWords(String text) {
        Map<String, Integer> countWords = new HashMap<>();
        String[] arr = text.split("\\s");
        for (int i = 0; i < arr.length; i++) {
            if (countWords.containsKey(arr[i])) {
                countWords.replace(arr[i], countWords.get(arr[i]) + 1);
            } else {
                countWords.put(arr[i], 1);
            }
        }
        StringBuilder s = new StringBuilder();
        for( String key: countWords.keySet()) {
            if (countWords.get(key) > 2 && key.length() > 3) {
                s.append(key);
                s.append(", ");
            }
        }
//        s.delete(s.length() - 2, s.length() -1);
        System.out.println("builder: " + s.toString());
        return "";
    }
}
