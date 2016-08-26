/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
/**
 *
 * @author maryan
 */
public class ProccessingDataTest {
    
 @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Test of shutdown method, of class ProccessingData.
     * @throws org.apache.commons.cli.ParseException
     * @throws java.io.IOException
     */
    @Test
    public void mainTest() throws ParseException, IOException, org.apache.commons.cli.ParseException {
        System.out.println("Testing scraping-aplication");

        final File linkFile = new File("link.txt");
        final File resultFolder = new File("result");
        
        String data = "http://leo-tvorit.livejournal.com/92155.html";
        FileUtils.writeStringToFile(linkFile, data);
        
        String[] myArgs = {"-f","result", "-i", "link.txt"};       
        CLParser parser = new CLParser(myArgs);
        ProccessingData obj = new ProccessingData(parser.getLinks(), parser.getOutputFolder(), false);
        obj.proccessLinks();
        
        File articleImage1 = new File("result/images/articleImage1.jpeg");
        File article1Box = new File("result/articleBox/article1Box.tmpl");
        File article1 = new File("result/articles/article1.tmpl");
        File page = new File("result/articles/Khud-shopping-bumaga-dlya-akvareli.page");
        File article1Title = new File("result/articles/article1Title.tmpl");
        File article1MetaTag = new File("result/articles/article1MetaTag.tmpl");
//        File tag = new File("result/tag.txt");
        File nomirror = new File("result/articleBox/.nomirror");
        File indexBlocknot1 = new File("result/index-Bloknoty1.page");
        File indexBumaga1 = new File("result/index-Bumaga1.page");
        File indexKhudShoping = new File("result/index-Khud-shopping1.page");
        File index = new File("result/index-Vsi-mitki1.page");
        
        assertThat(FileUtils.listFiles(resultFolder, null, true), hasItems(articleImage1,
                article1Box, article1, page, article1Title, article1MetaTag, 
                nomirror, indexBlocknot1, indexBumaga1, indexKhudShoping, index));
        
        BufferedImage img = ImageIO.read(articleImage1);
        
        assertEquals("failure - image hasn't proportion", img.getWidth() / 9, img.getHeight() / 7);
        
        String expected_article1Box = "<article id=\"post-83\" class=\"col-md-4"
                + " col-sm-4 pbox post-83 post type-post status-publish format-standard"
                + " has-post-thumbnail hentry category-featured category-tutorials\">"
                + "<a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\"> "
                + "<img class=\"img-responsive\" src=\"$!{root}/images/articleImage1.jpeg\"/></a>\n" +
        "<h2 class=\"box-title\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\""
                + " rel=\"bookmark\">$!{file_articles_article1Title}</a></h2>    </article>\n";
        assertEquals("failure - article1Box and expected result are not equal", expected_article1Box, FileUtils.readFileToString(article1Box));
        
        String expected_article1 = "<h2 class=\"featurette-heading\">$!{file_articles_article1Title}</h2>\n" +
                "Это был такой себе тестовый забег за блокнотом и несколькими листами бумаги. Планирую еще один где-то позже.\n" +
                "<br>\n" +
                "<br>Для чего? Меня тут после обзорных постов акварельных карандашей уже несколько раз спрашивали, а на честной ли акварельной бумаге Лео карандаши тестирует? И Лео задумался: \"А в самом деле, на честно ли?\". После чего пошел смотреть на характеристики бумаги в склейке, откуда выдираются листы на тесты акварельных карандашей: 160 г/м.кв. Оно, конечно, типа как бы и для мокрых техник, но очень спорная такая плотность бумаги, если закрашивать весь лист. Для тестов мне удобна эта бумага тем, что она достаточно гладкая, как для карандашей и достаточно плотная, чтобы выдержать то минимальное количество воды, которое необходимо для размывания пигмента. Конечно, на характеристики тестируемых карандашей это никак не влияет, но если подходить к вопросу принципиально, то ок, возьмем честную бумагу для акварели!\n" +
                "<br>Только выберем сперва...\n" +
                "<br>\n" +
                "<br>Поход в разведку принес склейку и немного бумаги поштучно\n" +
                "<br>\n" +
                "<img src=\"http://aterleo.info/lj2/2016/01/13/01.jpg\" class=\"img-responsive\">\n" +
                "<br>Небольшая склейка \"Canson\" на 10 листов, плотностью 250 г/м.кв. Должна бы подойти, но фактура крупновата... \n" +
                "<br>\n" +
                "<br>\n" +
                "<img src=\"http://aterleo.info/lj2/2016/01/13/02.jpg\" class=\"img-responsive\">\n" +
                "<br>Несколько листов бумаги поштучно. Она немного тонированная и с разной фактурой. Плотность у всех акварельная, перечислять не буду, но оно даже по фото видно.\n" +
                "<br>Коричневая бумага в непонятный хаотичный рисунок - это не для акварели, я подозреваю. Она слишком гладкая и тоньше. Взял ее за необычный вид - прыгадыцца...\n" +
                "<br>\n" +
                "<img src=\"http://aterleo.info/lj2/2016/01/13/03.jpg\" class=\"img-responsive\">\n" +
                "<br>Чуть позже схожу еще раз, чтоб посмотреть детальнее, а то не было времени и похватал, фактически, что под руку попало.\n" +
                "<br>А что за карандаши тестировать буду? О-о... это просто песня, а не карандаши, но я пока загадочно промолчу =) \n" +
                "<br>\n";
        assertEquals("failure - article1 and expected result are not equal", expected_article1, FileUtils.readFileToString(article1));
        
        String expected_page = "$!{file_structure_top}\n" +
            "  $!{file_articles_article1MetaTag}\n" +
            "   $!{file_structure_titleOpen}\n" +
            "       $!{file_articles_article1Title}\n" +
            "   $!{file_structure_titleClose}\n" +
            "  $!{file_structure_fulltop}\n" +
            "    $!{file_articles_article1}\n" +
            "  $!{file_pager_article1}\n" +
            "$!{file_structure_bottom}\n";
        assertEquals("failure - page and expected result are not equal", expected_page, FileUtils.readFileToString(page));
        
        String expected_article1Title = "Худ. шоппинг: бумага для акварели\n";
        assertEquals("failure - article1Title and expected reult are not equal", expected_article1Title,
                FileUtils.readFileToString(article1Title));
        
        String expected_article1MetaTag = "<meta property=\"article:tag\" content=\"Худ. шоппинг\">\n" +
            "<meta property=\"article:tag\" content=\"Бумага\">\n" +
            "<meta property=\"article:tag\" content=\"Блокноты\">\n" +
            "<meta name=\"keywords\" content=\"Canson\">\n";
        assertEquals("fialure - article1MetaTag and expected result are not equal", expected_article1MetaTag,
                FileUtils.readFileToString(article1MetaTag));
        
//        String expected_teg = "Блокноты = 1\n" +
//        "Бумага = 1\n" +
//        "Худ. шоппинг = 1\n" +
//        "Всі мітки = 1\n";
//        assertEquals("failure - teg and expected result are not equal", expected_teg, FileUtils.readFileToString(teg));
        
        
        String expectedIndexBloknoty1 = "<!DOCTYPE html>\n" +
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
            "					<div class=\"row\">\n" +
            "<div class=\"row\"><article id=\"post-83\" class=\"col-md-4 col-sm-4 pbox post-83 post type-post status-publish format-standard has-post-thumbnail hentry category-featured category-tutorials\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\"> <img class=\"img-responsive\" src=\"$!{root}/images/articleImage1.jpeg\"/></a>\n" +
            "<h2 class=\"box-title\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\" rel=\"bookmark\">$!{file_articles_article1Title}</a></h2>    </article>\n" +
            "</div>\n" +
            "\n" +
            "  </div>\n" +
            "</div>\n" +
            "<div class=\"col-xs-10 col-sm-3 sidebar-offcanvas\" id=\"sidebar\">\n" +
            "	<ul class=\"nav nav-pills nav-stacked\">\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Vsi-mitki1.html\" rel=\"category tag\">Всі мітки<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "  <span  class=\"dis\">\n" +
            "  <a href=\"/index-Bloknoty1.html\" rel=\"category tag\">Блокноты<span class=\"badge\">1</span></a>\n" +
            "  </span>\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Bumaga1.html\" rel=\"category tag\">Бумага<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Khud-shopping1.html\" rel=\"category tag\">Худ. шоппинг<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "</ul>\n" +
            "</div>\n" +
            "\n" +
            "</div>\n" +
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
            "</body>\n" +
            "</html>\n";
        
        assertEquals("failure - index-Bloknoty1 and expected result are not equal", expectedIndexBloknoty1, FileUtils.readFileToString(indexBlocknot1));
        
        
        String expectedIndexBumaga1 = "<!DOCTYPE html>\n" +
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
            "					<div class=\"row\">\n" +
            "<div class=\"row\"><article id=\"post-83\" class=\"col-md-4 col-sm-4 pbox post-83 post type-post status-publish format-standard has-post-thumbnail hentry category-featured category-tutorials\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\"> <img class=\"img-responsive\" src=\"$!{root}/images/articleImage1.jpeg\"/></a>\n" +
            "<h2 class=\"box-title\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\" rel=\"bookmark\">$!{file_articles_article1Title}</a></h2>    </article>\n" +
            "</div>\n" +
            "\n" +
            "  </div>\n" +
            "</div>\n" +
            "<div class=\"col-xs-10 col-sm-3 sidebar-offcanvas\" id=\"sidebar\">\n" +
            "	<ul class=\"nav nav-pills nav-stacked\">\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Vsi-mitki1.html\" rel=\"category tag\">Всі мітки<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Bloknoty1.html\" rel=\"category tag\">Блокноты<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "  <span  class=\"dis\">\n" +
            "  <a href=\"/index-Bumaga1.html\" rel=\"category tag\">Бумага<span class=\"badge\">1</span></a>\n" +
            "  </span>\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Khud-shopping1.html\" rel=\"category tag\">Худ. шоппинг<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "</ul>\n" +
            "</div>\n" +
            "\n" +
            "</div>\n" +
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
            "</body>\n" +
            "</html>\n";
        
        assertEquals("failure - index-Bumaga1 and expected result are not equal", expectedIndexBumaga1, FileUtils.readFileToString(indexBumaga1));
        
        String indexKhudShopping1 = "<!DOCTYPE html>\n" +
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
        "					<div class=\"row\">\n" +
        "<div class=\"row\"><article id=\"post-83\" class=\"col-md-4 col-sm-4 pbox post-83 post type-post status-publish format-standard has-post-thumbnail hentry category-featured category-tutorials\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\"> <img class=\"img-responsive\" src=\"$!{root}/images/articleImage1.jpeg\"/></a>\n" +
        "<h2 class=\"box-title\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\" rel=\"bookmark\">$!{file_articles_article1Title}</a></h2>    </article>\n" +
        "</div>\n" +
        "\n" +
        "  </div>\n" +
        "</div>\n" +
        "<div class=\"col-xs-10 col-sm-3 sidebar-offcanvas\" id=\"sidebar\">\n" +
        "	<ul class=\"nav nav-pills nav-stacked\">\n" +
        "  <li role=\"presentation\">\n" +
        "  <a href=\"/index-Vsi-mitki1.html\" rel=\"category tag\">Всі мітки<span class=\"badge\">1</span></a>\n" +
        "  </li>\n" +
        "  <li role=\"presentation\">\n" +
        "  <a href=\"/index-Bloknoty1.html\" rel=\"category tag\">Блокноты<span class=\"badge\">1</span></a>\n" +
        "  </li>\n" +
        "  <li role=\"presentation\">\n" +
        "  <a href=\"/index-Bumaga1.html\" rel=\"category tag\">Бумага<span class=\"badge\">1</span></a>\n" +
        "  </li>\n" +
        "  <span  class=\"dis\">\n" +
        "  <a href=\"/index-Khud-shopping1.html\" rel=\"category tag\">Худ. шоппинг<span class=\"badge\">1</span></a>\n" +
        "  </span>\n" +
        "</ul>\n" +
        "</div>\n" +
        "\n" +
        "</div>\n" +
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
        "</body>\n" +
        "</html>\n";
        
        assertEquals("failure - index-Khud-Shopping1 and expected result are not equal", indexKhudShopping1, FileUtils.readFileToString(indexKhudShoping));
        
        String indexVsiMitky = "<!DOCTYPE html>\n" +
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
            "					<div class=\"row\">\n" +
            "<div class=\"row\"><article id=\"post-83\" class=\"col-md-4 col-sm-4 pbox post-83 post type-post status-publish format-standard has-post-thumbnail hentry category-featured category-tutorials\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\"> <img class=\"img-responsive\" src=\"$!{root}/images/articleImage1.jpeg\"/></a>\n" +
            "<h2 class=\"box-title\"><a href=\"$!{root}/articles/Khud-shopping-bumaga-dlya-akvareli.html\" rel=\"bookmark\">$!{file_articles_article1Title}</a></h2>    </article>\n" +
            "</div>\n" +
            "\n" +
            "  </div>\n" +
            "</div>\n" +
            "<div class=\"col-xs-10 col-sm-3 sidebar-offcanvas\" id=\"sidebar\">\n" +
            "	<ul class=\"nav nav-pills nav-stacked\">\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Vsi-mitki1.html\" rel=\"category tag\">Всі мітки<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Bloknoty1.html\" rel=\"category tag\">Блокноты<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Bumaga1.html\" rel=\"category tag\">Бумага<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "  <li role=\"presentation\">\n" +
            "  <a href=\"/index-Khud-shopping1.html\" rel=\"category tag\">Худ. шоппинг<span class=\"badge\">1</span></a>\n" +
            "  </li>\n" +
            "</ul>\n" +
            "</div>\n" +
            "\n" +
            "</div>\n" +
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
            "</body>\n" +
            "</html>\n";
        
        assertEquals("failure - index-Vsi-Mitky1 and expected result are not equal", indexVsiMitky, FileUtils.readFileToString(index));
        
        
        String expectedNomirror = "";
        assertEquals("failure - .nomirror and expected result are not equal", expectedNomirror, FileUtils.readFileToString(nomirror));
        FileUtils.deleteQuietly(linkFile);
        FileUtils.deleteDirectory(resultFolder);
    }
    
}
