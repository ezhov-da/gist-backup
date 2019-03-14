package ru.ezhov.gist.backup.processing;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfWriter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

//https://www.ibm.com/developerworks/ru/library/os-javapdf/index.html
public class PdfCreator {
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, DocumentException {

        String text = "привет";
        String[] arr = text.split("");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            stringBuilder.append("\\u" + (int) arr[i].charAt(0));
        }
        System.out.println(stringBuilder.toString());
        System.out.println(new String("привет".getBytes("cp866")));

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("./ITextTest.pdf"));
        document.open();
        Paragraph title1 = new Paragraph(stringBuilder.toString(),
                FontFactory.getFont("/cour.ttf", "Cp1251",
                        14, Font.NORMAL, CMYKColor.BLACK));
        Chapter chapter1 = new Chapter(title1, 1);
        chapter1.setNumberDepth(0);
        Paragraph someSectionText = new Paragraph(stringBuilder.toString(),
                FontFactory.getFont("/cour.ttf", "Cp1251",
                        10, Font.NORMAL, CMYKColor.BLACK));
        chapter1.add(someSectionText);
        document.add(chapter1);
        document.close();

//        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
//        try {
//            org.w3c.dom.Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("D:/programmer/gist_ezhov-da_20190313222654.bkp.xml"));
//            NodeList nodeList = xmlDoc.getElementsByTagName("gist");
//            int len = nodeList.getLength();
//
//            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("./ITextTest.pdf"));
//            document.open();
//
//            for (int i = 0; i < len; i++) {
//                Node node = nodeList.item(i);
//                Node nameNode = node.getFirstChild();
//                Node contentNode = node.getLastChild();
//
//                String name = nameNode.getTextContent();
//                String content = contentNode.getTextContent();
//
//                Paragraph title1 = new Paragraph(name,
//                        FontFactory.getFont("/cour.ttf", "Cp1251",
//                                14, Font.NORMAL, CMYKColor.BLACK));
//                Chapter chapter1 = new Chapter(title1, 1);
//                chapter1.setNumberDepth(0);
//                Paragraph someSectionText = new Paragraph(content,
//                        FontFactory.getFont("/cour.ttf", "Cp1251",
//                                10, Font.NORMAL, CMYKColor.BLACK));
//                chapter1.add(someSectionText);
//                document.add(chapter1);
//
//            }
//            document.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
