/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
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
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import org.apache.commons.io.FileUtils;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final File article;
    private final File imagesFolder;
    private final File articleBox;
    private Map<String, Integer> tegCount = new HashMap<>();
    private final List<String> links;
    private final File outputFolder;
    private final boolean tegEnable;
    private final Logger logger = LoggerFactory.getLogger(ProccessingData.class);
    /**
     * Constructor which is gets, cleans web data
     * @param links
     * @param outputFolder
     * @param tegEnable
     * @throws IOException 
     */
    public ProccessingData(List<String> links, File outputFolder, boolean tegEnable) throws IOException {
        this.links = links;
        this.outputFolder = outputFolder;
        this.article = createFolder("articles");
        this.imagesFolder = createFolder("images");
        this.articleBox = createFolder("articleBox");
        this.tegEnable = tegEnable;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());   
    }
    
    /**
     * Iterating links
     * @throws IOException 
     */
    public void proccessLinks() throws IOException {
        System.out.print("Scrapping started");
        int id = 1;
        for(String link: this.links) {
            proccessWebPage(link, Integer.toString(id));
            id++;
        }
        
        shutdown();
        
        if (this.tegEnable) {
            tegCount();
        }
    }
    
    /**
     * Creating teg file with tegs count
     */
    private void tegCount() {
        try {
            File out = new File(outputFolder + File.separator + "teg.txt");
            for (Map.Entry pair : tegCount.entrySet()) {
                FileUtils.writeStringToFile(out , pair.getKey() + " = " + pair.getValue() +"\n" , true);
            }
        } catch (IOException e) {
            logger.error("method: tegCount()\n"
                    + "Unable to create teg.txt file: " + e);
        }
    }
    
    /**
     * Proccessing web page
     * @param link
     * @param id 
     */
    public void proccessWebPage(String link, String id) {
        executor.execute(() -> {    
            String articleName = "article" + id + "Title";
            String contentName = "article" + id;
            String articleMetaTags = "article" + id + "MetaTag" + TMPL_EXT;
            String articleHeader = "<h2 class=\"featurette-heading\">$!{file_articles_article" + id + "Title}</h2>";
            String structure = "$!{file_structure_top}\n" +
                                "  $!{file_articles_article" + id + "MetaTag}\n" +
                                "   $!{file_structure_titleOpen}\n" +
                                "       $!{file_articles_article" + id + "Title}\n" +
                                "   $!{file_structure_titleClose}\n" + 
                                "  $!{file_structure_fulltop}\n" +
                                "    $!{file_articles_article" + id + "}\n" +
                                "  $!{file_pager_article" + id + "}\n" +
                                "$!{file_structure_bottom}";
            String articleBoxFile = "article" + id + "Box";
            String articleImage = "articleImage" + id;

            File destination = new File(imagesFolder + File.separator + articleImage);


            System.out.print("...");
            try {
                Document doc = Jsoup.connect(link).timeout(5000).get();
                String title = doc.getElementsByTag("title").text().replaceAll(" - Лео творит!", "");
                Elements metaTags = doc.getElementsByAttributeValue("property", "article:tag");
                Element content = doc.getElementsByClass("entry-content").get(0);
                
                if (this.tegEnable) {
                    for(Element el: metaTags) {
                        String teg = el.attr("content");
                        if (tegCount.containsKey(teg)) {
                            tegCount.replace(teg, tegCount.get(teg)+1);
                        } else {
                            tegCount.put(teg, 1);
                        }
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


                writeFile(this.article + File.separator + articleMetaTags, metaTags + "\n" + keywords);
                writeFile(this.article + File.separator + articleName + TMPL_EXT, title);
                writeFile(this.article + File.separator + contentName + TMPL_EXT, articleHeader + "\n" + content.html());
                writeFile(this.article + File.separator + articleStructureName, structure);
                writeFile(this.articleBox + File.separator + articleBoxFile + TMPL_EXT, articleBoxContent);
            } catch (IOException e) {
                logger.error("method: proccessWebPage(String, String)\n"
                    + "Error during connection or FileNotFoundException: " + e);
            }  
        });
    }
    
    /**
     * Write file
     * @param directory
     * @param content
     * @throws FileNotFoundException 
     */
    public void writeFile(String directory, String content) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(directory)) {
                    out.println(content);
        } 
    }
    
    /**
     * Create folder
     * @param folderName
     * @return 
     */
    public final File createFolder(String folderName) {
        
        File folder = new File(outputFolder + File.separator + folderName);
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                System.err.println("Enable to create " + folderName + " folder");
                logger.error("method: createFolder(String)\n"
                    + "Enable to create " + folderName + " folder");
            }
        }
        return folder;
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
            logger.error("method: getProportion(int, int)\n"
                    + "ArithmeticException or IllegalArgumentException: " + e);
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
            logger.error("method: proccessImage(Url, File)\n"
                    + "Error during image read: " + e);
        }
        int p = getProportion(img.getWidth(), img.getHeight());
       
        img = img.getSubimage(0, 0, p * WIDTH_PROPORTION, p * HEIGHT_PROPORTION);
        try {
            Iterator<ImageWriter> i = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter jpegWriter = i.next();
            ImageWriteParam param = jpegWriter.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1.0f);
            FileImageOutputStream out = new FileImageOutputStream(destination);
            jpegWriter.setOutput(out);
            jpegWriter.write(null, new IIOImage(img, null, null), param);
            jpegWriter.dispose();
            out.close();
//            ImageIO.write(img, "png", destination);
        } catch (IOException e) {
             System.err.println("Error write image: " + e);
             logger.error("method: proccessImage(Url, File)\n"
                    + "Error during image proccess: " + e);
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
