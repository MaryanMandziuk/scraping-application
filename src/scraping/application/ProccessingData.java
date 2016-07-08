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
import org.apache.commons.io.FileUtils;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
/**
 * Class for scarping web data
 * @author maryan
 */
public class ProccessingData {
    
    private final String PAGE_EXT = ".page";
    private final String TMPL_EXT = ".tmpl";
    private final ExecutorService executor;
    private final int WIDTH_PROPORTION = 9;
    private final int HEIGHT_PROPORTION = 7;
    
    /**
     * Constructor which is gets, cleans web data
     * @param links
     * @param outputFolder
     * @throws IOException 
     */
    public ProccessingData(List<String> links, File outputFolder) throws IOException {
        
        System.out.print("Scaping starting");
        
        final int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        int i = 0;
        Map<String, Integer> map = new HashMap<>();
        
        File articles = new File(outputFolder + File.separator + "articles");
        if (!articles.exists()) {
            if (!articles.mkdir()) {
                System.err.println("Enable to create articles folder");
            }
        }
        
        File imagesFolder = new File(outputFolder + File.separator + "images");
        if (!imagesFolder.exists()) {
            if (!imagesFolder.mkdir()) {
                System.err.println("Enable to create images folder");
            }
        }
        
        File articleBox = new File(outputFolder + File.separator + "articleBox");
        if (!articleBox.exists()) {
            if (!articleBox.mkdir()) {
                System.err.println("Enable to create articleBox folder");
            }
        }


        for (String link : links) {
            
            i++;
            String articleName = "article" + i + "Title";
            String contentName = "article" + i;
            String articleMetaTags = "article" + i + "MetaTag" + TMPL_EXT;
            String articleHeader = "<h2 class=\"featurette-heading\">$!{file_articles_article" + i + "Title}</h2>";
            String structure = "$!{file_structure_top}\n" +
                                "  $!{file_articles_article" + i + "MetaTag}\n" +
                                "   $!{file_structure_titleOpen}\n" +
                                "       $!{file_articles_article" + i + "Title}\n" +
                                "   $!{file_structure_titleClose}\n" + 
                                "  $!{file_structure_fulltop}\n" +
                                "    $!{file_articles_article" + i + "}\n" +
                                "  $!{file_pager_article" + i + "}\n" +
                                "$!{file_structure_bottom}";
            String articleBoxFile = "article" + i + "Box";
            String articleImage = "articleImage" + i;
            File destination = new File(imagesFolder + File.separator + articleImage);
            
            executor.execute(() -> {
                System.out.print("...");
                try {
                    Document doc = Jsoup.connect(link).timeout(5000).get();
                    String title = doc.getElementsByTag("title").text().replaceAll(" - Лео творит!", "");
                    Elements metaTags = doc.getElementsByAttributeValue("property", "article:tag");
                    Element content = doc.getElementsByClass("entry-content").get(0);
                    
                    for(Element el: metaTags) {
                        String teg = el.attr("content");
                        if (map.containsKey(teg)) {
                            map.replace(teg, map.get(teg)+1);
                        } else {
                            map.put(teg, 1);
                        }
                    }
                    
                    content.getElementsByTag("div").remove();
                    content.getElementsByAttributeValue("name", "cutid1").remove();
                    content.getElementsByAttributeValue("name", "cutid1-end").remove();
                    content.getElementsByAttributeValue("class", "i-ljuser-userhead").remove();
                    content.getElementsByTag("img").attr("class", "img-responsive");
                    content.select("img + br").remove();
                    content.child(0).lastElementSibling().remove();
                    
                    String articleStructureName = translit(title) + PAGE_EXT;
                    String articleBoxContent = "<a href=\"$!{root}/articles/"+ translit(title) +".html\">"
                            + " <img class=\"img-responsive\" src=\"$!{root}/images/"+articleImage+"\"/></a>\n" +
                            "<h2 class=\"box-title\"><a href=\"$!{root}/articles/"+translit(title)+".html\""
                            + " rel=\"bookmark\">$!{file_articles_"+articleName+"}</a></h2>";
                    Element keywords = new Element(Tag.valueOf("meta"), "").attr("name", "keywords")
                            .attr("content", getEnglishWords(content.text()));
                    URL imageUrl = new URL(content.getElementsByTag("img").get(0).attr("abs:src"));
                    proccessImage(imageUrl, destination);  
                    
                    try (PrintWriter out = new PrintWriter(articles + File.separator + articleMetaTags)) {
                        out.println(metaTags);
                        out.println(keywords);
                    }
                    try (PrintWriter out = new PrintWriter(articles + File.separator + articleName + TMPL_EXT)) {
                        out.println(title);
                    }
                    try (PrintWriter out = new PrintWriter(articles + File.separator + contentName + TMPL_EXT)) {
                        out.println(articleHeader);
                        out.println(content.html());
                    }
                    try (PrintWriter out = new PrintWriter(articles + File.separator + articleStructureName)) {
                        out.println(structure);
                    }
                    try (PrintWriter out = new PrintWriter(articleBox + File.separator + articleBoxFile + TMPL_EXT)) {
                        out.println(articleBoxContent);
                    }

                } catch (IOException ex) {
                    System.err.println("Error: " + ex);    
                }
            });   
        }
        shutdown();    
        File out = new File(outputFolder + File.separator + "teg.txt");
        for (Map.Entry pair : map.entrySet()) {
            FileUtils.writeStringToFile(out , pair.getKey() + " = " + pair.getValue() +"\n" , true);
        }
    }
    
    /**
     * Method for finding image proportion value
     * @param width
     * @param height
     * @return proportion
     */
    private int getProportion(int width, int height) {
        
        int a = 0;
        int b = 0;
        
        try {
            if (width < 0 || height < 0) {
                throw new IllegalArgumentException("Image width and height cannot be negative.");
            }
            a = width / WIDTH_PROPORTION;
            b = height / HEIGHT_PROPORTION;    
        } catch (ArithmeticException | IllegalArgumentException e) {
            System.err.println("Error: " + e);
        }
        if (a < b) {
            return a;
        }
        return b;
    }
    
    /**
     * Method for cutting image with proportion
     * @param imageUrl
     * @param destination 
     */
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
    
    /**
     * ExecutorService shutdown
     */       
    private final void shutdown() {
        executor.shutdown(); 
        try {
            executor.shutdown();
            executor.awaitTermination(55, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted" + e);
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdownNow();
            System.out.println("\nScraping finished");
        }
    }
    
    /**
     * Getting english words in one example(distinct) from text data 
     * @param text
     * @return string with words
     */
    private String getEnglishWords(final String text) {
        final String pattern = "(\\w+-\\w+-\\w+)|([\\w&&[^0-9]]+-\\w+)|([\\w&&[^0-9]]+)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        Map<String, Integer> countWords = new HashMap<>();
        StringBuilder s = new StringBuilder();
        
        while (m.find()) {
            final String word = m.group();
            if (countWords.containsKey(word)) {
                countWords.replace(word, countWords.get(word) + 1);
            } else {
                countWords.put(word, 1);
            }
        }
        
        for (String key: countWords.keySet()) {
            if (key.length() >= 3) {
                s.append(key);
                s.append(", ");
            }
        }
        
        if (s.length() > 0) {
            s.delete(s.length() - 2, s.length() );
        }
        
        return s.toString();
    }
    
    /**
     * Translit method for url
     * @param title
     * @return translit string
     */
    private String translit(final String title) {
        final String[] russian = {"а","б","в","г","д","е","ё","ж", "з","и","й","к","л","м",
            "н","о","п","р","с","т","у","ф","х", "ц", "ч", "ш", "щ",
            "ъ","ы","ь","э","ю", "я","і","ї","є",
             "А","Б","В","Г","Д","Е","Ё","Ж", "З","И","Й","К","Л","М","Н","О","П",
             "Р","С","Т","У","Ф","Х", "Ц", "Ч", "Ш", "Щ","Ъ","Ы","Ь","Э","Ю", "Я","І","Ї","Є"," "};
        final String[] translit = {"a","b","v","g","d","e","e","zh","z","i","y","k","l",
            "m","n","o","p","r","s","t","u","f","kh","tc","ch","sh","shch","", "y",
            "","e","iu","ya","i","i","e","A","B","V","G","D","E","E","Zh","Z","I",
            "Y","K","L","M","N","O","P","R","S","T","U","F","Kh","Tc","Ch","Sh","Shch",
            "", "Y", "","E","Iu","ya","I","I","E","-"};
        final String[] unsupported = {".",",","!","?",":",";","\"","'"};
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < russian.length; i++) {
            map.put(russian[i], translit[i]);
        }
        
        StringBuilder s = new StringBuilder();
        final String[] arr = title.split("");
        for (int i = 0; i < arr.length; i++) {
            String tmp = map.get(arr[i]);
            if (tmp == null) {
                if (!isUnSupported(arr[i], unsupported)) {
                    s.append(arr[i]);
                }
            } 
            else  {
                s.append(tmp); 
            } 
        }
        while (s.charAt(s.length()-1) == '-') {
            s.setLength(s.length()-1);
        }
        return s.toString();
    }
    
    /**
     * Check for unsupported symbols
     * @param t
     * @param un
     * @return bool value
     */
    private boolean isUnSupported(final String t,final String[] un) {  
        for(int i = 0; i < un.length; i ++) {
            if (t.equals(un[i])) { 
                return true;
            }
        }
        return false;
    }
}
