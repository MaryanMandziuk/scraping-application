/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Map<String, Integer> tagCount = new HashMap<>();
    private final List<String> links;
    private final File outputFolder;
//    private final boolean tagEnable;
    private final Logger logger = LoggerFactory.getLogger(ProccessingData.class);
    private Map<String, String> articleTag = new HashMap<>();
    private Map<Integer, String> articlePage = new HashMap<>();
    private final String indexName = "Всі мітки";
    /**
     * Constructor which is gets, cleans web data
     * @param links
     * @param outputFolder
     * @param tagEnable
     * @param linkGeneration
     * @throws IOException 
     */
    public ProccessingData(List<String> links, File outputFolder, boolean linkGeneration) throws IOException {
        this.outputFolder = outputFolder;
        this.article = createFolder("articles");
        this.imagesFolder = createFolder("images");
        this.articleBox = createFolder("articleBox");
//        this.tagEnable = tagEnable;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); 
        this.tagCount.put(indexName, 0);
        if (linkGeneration) {
            this.links = this.generateLinks();
        } else {
            this.links = links;
        }
    }
    
    /**
     * Scraping links
     * @return
     * @throws IOException 
     */
    public List<String> generateLinks() throws IOException {
        List<String> links = new ArrayList<>();
        
        Document doc = Jsoup.connect("http://leo-tvorit.livejournal.com/").timeout(5000).get();
        List<String> blackList = Arrays.asList("Про Лео");
        while (true) {
            Elements subjLink = doc.getElementsByClass("subj-link");

            Elements ljtags = doc.getElementsByClass("ljtags");

            for (int i = 0; i < ljtags.size(); i++) {
                Elements tags = ljtags.get(i).getElementsByTag("a");
                boolean check = false;

                for (Element e : tags) {
                    if (blackList.contains(e.text())) {
                        check = true;
                    } else {
                        check = false;
                        break;
                    }
                }

                if (!check) {
                    links.add(subjLink.get(i).attr("href"));
                }

            }

            Element nextPage = doc.getElementsByClass("prev").select("a").first();
            try {
                doc = Jsoup.connect(nextPage.attr("href")).timeout(5000).get();
            } catch (Exception e) {
                System.out.println("Finish!");
                break; 
            }
        }
        
        return links;
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
        
//        if (this.tagEnable) {
            tagCount();
//        }
        generateIndexPages();
        if (articleTag.size() > 1) {
            generatePager();
        }
        createNomirrors(this.articleBox);
    }
    
    /**
     * Creating tag file with tags count
     */
    private void tagCount() {
        try {
            File out = new File(outputFolder + File.separator + "tag.txt");
            for (Map.Entry pair : tagCount.entrySet()) {
                FileUtils.writeStringToFile(out , pair.getKey() + " = " + pair.getValue() +"\n" , true);
            }
        } catch (IOException e) {
            logger.error("method: tagCount()\n"
                    + "Unable to create tag.txt file: " + e);
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
            String articleImage = "articleImage" + id + ".jpeg";

            File destination = new File(imagesFolder + File.separator + articleImage);


            System.out.print("...");
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
                content.select("img + br").remove();
                content.child(0).lastElementSibling().remove();
                
                try {
                    URL imageUrl = new URL(content.getElementsByTag("img").get(0).attr("abs:src"));
                    proccessImage(imageUrl, destination); 
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                String articleStructureName = translit(title) + PAGE_EXT;

                String articleBoxContent = "<article id=\"post-83\" class=\"col-md-4 col-sm-4 pbox post-83 post type-post status-publish format-standard has-post-thumbnail hentry category-featured category-tutorials\">"
                        + "<a href=\"$!{root}/articles/" + translit(title) + ".html\">"
                        + " <img class=\"img-responsive\" src=\"$!{root}/images/" + articleImage + "\"/></a>\n"
                        + "<h2 class=\"box-title\"><a href=\"$!{root}/articles/" + translit(title) + ".html\""
                        + " rel=\"bookmark\">$!{file_articles_" + articleName + "}</a></h2>"
                        + "    </article>";
                
                
                StringBuilder b = new StringBuilder();
                for (Element el : metaTags) {
                    String tag = el.attr("content");
                    b.append(tag + " ");
                    if (tagCount.containsKey(tag)) {
                        tagCount.replace(tag, tagCount.get(tag) + 1);
                    } else if (tag != "") {
                        tagCount.put(tag, 1);
                    }
                }
                tagCount.replace(indexName, tagCount.get(indexName) + 1);
                articleTag.put(articleBoxContent, b.toString());
                articlePage.put(Integer.parseInt(id), translit(title));
                
                Element keywords = new Element(Tag.valueOf("meta"), "").attr("name", "keywords")
                        .attr("content", getEnglishWords(content.text()));

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
            FileImageOutputStream out = new FileImageOutputStream(destination );
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
            executor.awaitTermination(6000, TimeUnit.SECONDS);
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
    
    /**
     * 
     * @param tag
     * @param numberRows
     * @param numberBoxInRow
     * @return 
     */
    public String[] generateBoxes(String tag, int numberRows, int numberBoxInRow) {
        StringBuilder tmp = new StringBuilder("<div class=\"row\">");
        int len=0;
        if (tagCount.get(tag) < (numberRows * numberBoxInRow)) {
            len = 1;
        } else {
            int c  = tagCount.get(tag) / (numberRows * numberBoxInRow);
            if ((numberRows * numberBoxInRow) * c < tagCount.get(tag)) {
                len = c+1 ;
            } else {
                len = c;
            }
        }
        String[] result = new String[len];
        int countBoxRow = 0;
        int countBoxPage = 0;
        int i = 0;
        int j = 0;
        if (indexName.equals(tag)) {
            for (String box: this.articleTag.keySet()) {
                tmp.append(box + "\n");
                countBoxRow++;
                countBoxPage++;
                if (countBoxPage == numberRows * numberBoxInRow || tagCount.get(tag) == j+1) {
                    tmp.append("</div>\n");
                    result[i] = tmp.toString();
                    i++; j++;
                    tmp = new StringBuilder("<div class=\"row\">");
                    countBoxRow = 0;
                    continue;
                }
                if (countBoxRow == numberBoxInRow) {
                    tmp.append("</div>\n" 
                    + "<div class =\"row\">\n");
                    countBoxRow = 0;
                }
                j++;
            }
            
        } else {
            j=0;
            for (String box: this.articleTag.keySet()) {
                if (articleTag.get(box).contains(tag)) {
                    tmp.append(box + "\n");
                    countBoxRow++;
                    countBoxPage++;
                    if (countBoxPage == numberRows * numberBoxInRow || tagCount.get(tag) == j+1) {
                        tmp.append("</div>\n");
                        result[i] = tmp.toString();
                        i++; j++;
                        tmp = new StringBuilder("<div class=\"row\">");
                        countBoxRow = 0;
                        continue;
                    }
                    if (countBoxRow == numberBoxInRow) {
                        tmp.append("</div>\n" 
                        + "<div class =\"row\">\n");
                        countBoxRow = 0;
                    }
                    j++;
                }
            }
        }
        
        return result;
    }
    
    /**
     * 
     * @param tag
     * @return 
     */
    public String generateLinksWithTags(String tag) {
        StringBuilder tmp = new StringBuilder("<div class=\"col-xs-10 col-sm-3 sidebar-offcanvas\" id=\"sidebar\">\n" +
                        "	<ul class=\"nav nav-pills nav-stacked\">\n");
        String nameIndex ="";
        tmp.append("  <li role=\"presentation\">\n" +
                "  <a href=\"/index-" + translit(indexName) + "1.html\""
                + " rel=\"category tag\">" + indexName + "<span class=\"badge\">"
                + tagCount.get(indexName)+"</span></a>\n" +
                "  </li>\n");
        for (String t : tagCount.keySet()) {
            if (t == indexName) {
                continue;
            }
            nameIndex = translit(t);
            if (t != tag) {
                tmp.append("  <li role=\"presentation\">\n" +
                "  <a href=\"/index-" + nameIndex + "1.html\""
                + " rel=\"category tag\">" + t + "<span class=\"badge\">"
                + tagCount.get(t)+"</span></a>\n" +
                "  </li>\n");
            } else {
                tmp.append("  <span  class=\"dis\">\n" +
                "  <a href=\"/index-" + nameIndex + "1.html\""
                + " rel=\"category tag\">" + t + "<span class=\"badge\">"
                + tagCount.get(t)+"</span></a>\n" +
                "  </span>\n");
            }            
        }
        tmp.append("</ul>\n</div>\n");    
        return tmp.toString();
    }
    
    /**
     * 
     * @param len
     * @param current
     * @param indexName
     * @return 
     */
    public String generatePagination(int len, int current, String indexName) {
        StringBuilder tmp = new StringBuilder(" <div class=\"clearfix\"></div>\n" +
            "				<div class=\"col-md-12\">\n" +
            "				<div class='fab-paginate paginate'>\n" +
            "					<ul class='pagination'>\n" +
            "\n");
        for (int i = 1; i <= len; i ++) {
            if (i == current) {
                tmp.append("<li class='active'><span class='current'>" + current + "</span></li>\n");
            } else {
                tmp.append("<li><a href='/index-" + indexName + i + ".html' class='inactive' >"+ i +"</a></li>\n");
            }
        }
        tmp.append(
            "      </ul>\n" +
            "  </div>\n" +
            "</div>");
        return tmp.toString();
    }
    
    /**
     * 
     * @throws FileNotFoundException 
     */
    public void generateIndexPages() throws FileNotFoundException {
        String top = "<!DOCTYPE html>\n" +
                "<html lang=\"en-US\">\n" +
                "<head>\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/bootstrap.min.css\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/topography.css\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\" media=\"all\">\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width\">\n" +
                "<title>Articles</title>\n" +
                "\n" +
                "		</head>\n" +
                "\n" +
                "<body class=\"home blog\">\n" +
                "\n" +
                "    <div class=\"site-overlay\"></div>\n" +
                "<div id=\"page\" class=\"hfeed site  \">\n" +
                "	<div class=\"container\">\n" +
                "	<div id=\"content\" class=\"site-content row\">\n" +
                "	<div class=\"col-md-12 intro-me clearfix topography\">\n" +
                "		<h1>Articles</h1>\n" +
                "		<p>Articles Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit </p>\n" +
                "	</div>\n" +
                "\n" +
                "\n" +
                "	<div id=\"primary\" class=\"content-area \">\n" +
                "		<main id=\"main\" class=\"site-main\" role=\"main\">\n" +
                "\n" +
                "			<div class=\"row\">\n" +
                "				<div class=\"col-xs-12 col-sm-9\">\n" +
                "					<div class=\"row\">";
        String mid = "  </div>\n" +
                "</div>";
        String bottom = "</div>\n" +
                "</main><!-- #main -->\n" +
                "</div><!-- #primary -->\n" +
                "\n" +
                "\n" +
                "\n" +
                "</div><!-- #content -->\n" +
                "\n" +
                "<div class=\"row\">\n" +
                "<footer id=\"colophon\" class=\"site-footer col-md-12\" role=\"contentinfo\">\n" +
                "  <div class=\"site-info\">\n" +
                "    <div class=\"fcred col-12\">\n" +
                "      Copyright &copy; 2016 <a href=\"http://demo.fabthemes.com/wembley\">Articles</a> - Just another demo Sites site.<br />\n" +
                "    </div>\n" +
                "  </div><!-- .site-info -->\n" +
                "</footer><!-- #colophon -->\n" +
                "</div>\n" +
                "</div>\n" +
                "</div><!-- #page -->\n" +
                "\n" +
                "<style type=\"text/css\">\n" +
                "\n" +
                "  .pushy,.menu-btn{ background: ; }\n" +
                "  a,a:visited{ color:;}\n" +
                "  a:hover,a:focus,a:active { color:; }\n" +
                "\n" +
                "</style>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        final int numberRows = 4;
        final int numberBoxInRow = 3;
        String nameIndex ="";
        for (String tag: tagCount.keySet()) {
            String[] boxBody = generateBoxes(tag, numberRows, numberBoxInRow);
            for (int i = 0; i < boxBody.length; i++) {
                StringBuilder page = new StringBuilder();
                nameIndex = translit(tag);
                page.append(top +"\n");
                page.append(boxBody[i]+"\n");
                if (boxBody.length > 1) {
                    
                    page.append(generatePagination(boxBody.length, i + 1, nameIndex));
                }
                page.append(mid+"\n");
                page.append(generateLinksWithTags(tag) + "\n");
                page.append(bottom);
                int tmp =  i + 1;
                writeFile(this.outputFolder + File.separator + "index-" + nameIndex + tmp + this.PAGE_EXT, page.toString());
            }
        } 
    }
    
    /**
     * 
     * @throws FileNotFoundException 
     */
    public void generatePager() throws FileNotFoundException, IOException {
        String tmpl = "";
        File folderPager = createFolder("pager");
        new File(folderPager + File.separator + ".nomirror").createNewFile();
        for(Integer i : articlePage.keySet()) {
            if (i == 1) {
                tmpl = "<nav>\n" +
                "  <ul class=\"pager\">\n" +
                "    <li class=\"previous disabled\"><a href=\"#\"><span aria-hidden=\"true\">&larr;</span> Previous</a></li>\n" +
                "    <li class=\"next\"><a href=\"$!{root}/articles/" + articlePage.get(i+1) +  ".html\">Next <span aria-hidden=\"true\">&rarr;</span></a></li>\n" +
                "  </ul>\n" +
                "</nav>";
            } else if (i == articlePage.size()) {
                tmpl = "<nav>\n" +
                    "  <ul class=\"pager\">\n" +
                    "    <li class=\"previous\"><a href=\"$!{root}/articles/" + articlePage.get(i-1) + ".html\"><span aria-hidden=\"true\">&larr;</span> Previous</a></li>\n" +
                    "    <li class=\"next disabled\"><a href=\"#\">Next <span aria-hidden=\"true\">&rarr;</span></a></li>\n" +
                    "  </ul>\n" +
                    "</nav>";
            } else {
                tmpl = "<nav>\n" +
                "  <ul class=\"pager\">\n" +
                "    <li class=\"previous\"><a href=\"$!{root}/articles/" + articlePage.get(i-1) + ".html\"><span aria-hidden=\"true\">&larr;</span> Previous</a></li>\n" +
                "    <li class=\"next\"><a href=\"$!{root}/articles/" + articlePage.get(i+1) + ".html\">Next <span aria-hidden=\"true\">&rarr;</span></a></li>\n" +
                "  </ul>\n" +
                "</nav>";
            }
            writeFile(folderPager + File.separator + "article" + (i) + TMPL_EXT, tmpl);
        }
        
    }
    
    /**
     * Create nomirror file 
     * @param destenation
     * @throws IOException 
     */
    public void createNomirrors(File destenation) throws IOException {
        new File(destenation + File.separator + ".nomirror").createNewFile();
    }
    
}
