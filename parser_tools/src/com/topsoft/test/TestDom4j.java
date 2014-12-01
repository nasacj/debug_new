package com.topsoft.test; 

import org.dom4j.io.SAXReader; 
import org.dom4j.Document; 
import org.dom4j.DocumentException; 
import org.dom4j.Element; 
import org.dom4j.Node; 

import java.util.Iterator; 
import java.util.List; 
import java.io.File;
import java.io.InputStream; 

/** 
* Created by IntelliJ IDEA.<br> 
* <b>User</b>: leizhimin<br> 
* <b>Date</b>: 2008-3-26 15:53:51<br> 
* <b>Note</b>: Dom4j遍历解析XML测试 
*/ 
public class TestDom4j { 
    /** 
     * 获取指定xml文档的Document对象,xml文件必须在classpath中可以找到 
     * 
     * @param xmlFilePath xml文件路径 
     * @return Document对象 
     */ 
    public static Document parse2Document(String xmlFilePath) { 
        SAXReader reader = new SAXReader(); 
        Document document = null; 
        try { 
            InputStream in = TestDom4j.class.getResourceAsStream(xmlFilePath); 
            document = reader.read(in); 
        } catch (DocumentException e) { 
            System.out.println(e.getMessage()); 
            System.out.println("读取classpath下xmlFileName文件发生异常，请检查CLASSPATH和文件名是否存在！"); 
            e.printStackTrace(); 
        } 
        return document; 
    } 

    public static void testParseXMLData(String xmlFileName) throws DocumentException { 
        //产生一个解析器对象 
        SAXReader reader = new SAXReader(); 
        //将xml文档转换为Document的对象 
        //Document document = parse2Document(xmlFileName); 
        Document document = reader.read(new File(xmlFileName));
        //获取文档的根元素 
        Element root = document.getRootElement(); 
        //定义个保存输出xml数据的缓冲字符串对象 
        StringBuffer sb = new StringBuffer(); 
        sb.append("通过Dom4j解析XML,并输出数据:\n"); 
        sb.append(xmlFileName + "\n"); 
        sb.append("----------------遍历start----------------\n"); 
        //遍历当前元素(在此是根元素)的子元素 
        for (Iterator i_pe = root.elementIterator(); i_pe.hasNext();) { 
            Element e_pe = (Element) i_pe.next(); 
            //获取当前元素的名字 
            String person = e_pe.getName(); 
            //获取当前元素的id和sex属性的值并分别赋给id,sex变量 
            String id = e_pe.attributeValue("id"); 
            String sex = e_pe.attributeValue("sex"); 
            String name = e_pe.element("name").getText(); 
            String age = e_pe.element("age").getText(); 
            //将数据存放到缓冲区字符串对象中 
            sb.append(person + ":\n"); 
            sb.append("\tid=" + id + " sex=" + sex + "\n"); 
            sb.append("\t" + "name=" + name + " age=" + age + "\n"); 

            //获取当前元素e_pe(在此是person元素)下的子元素adds 
            Element e_adds = e_pe.element("adds"); 
            sb.append("\t" + e_adds.getName() + "\n"); 

            //遍历当前元素e_adds(在此是adds元素)的子元素 
            for (Iterator i_adds = e_adds.elementIterator(); i_adds.hasNext();) { 
                Element e_add = (Element) i_adds.next(); 
                String code = e_add.attributeValue("code"); 
                String add = e_add.getTextTrim(); 
                sb.append("\t\t" + e_add.getName() + ":" + " code=" + code + " value=\"" + add + "\"\n"); 
            } 
            sb.append("\n"); 
        } 
        sb.append("-----------------遍历end-----------------\n"); 
        System.out.println(sb.toString()); 


//        System.out.println("---------通过XPath获取一个元素----------"); 
//        Node node1 = document.selectSingleNode("/doc/person"); 
//        System.out.println("输出节点:" + 
//                "\t"+node1.asXML()); 
//
//        Node node2 = document.selectSingleNode("/doc/person/@sex"); 
//        System.out.println("输出节点:" + 
//                "\t"+node2.asXML()); 
//
//        Node node3 = document.selectSingleNode("/doc/person[name=\"zhangsan\"]/age"); 
//        System.out.println("输出节点:" + 
//                "\t"+node3.asXML()); 
//
//        System.out.println("\n---------XPath获取List节点测试------------"); 
//        List list = document.selectNodes("/doc/person[name=\"zhangsan\"]/adds/add"); 
//        for(Iterator it=list.iterator();it.hasNext();){ 
//            Node nodex=(Node)it.next(); 
//            System.out.println(nodex.asXML()); 
//        } 

        System.out.println("\n---------通过ID获取元素的测试----------"); 
        System.out.println("陷阱：通过ID获取，元素ID属性名必须为“大写ID”，小写的“id”会认为是普通属性！"); 
        String id22 = document.elementByID("22").asXML(); 
        String id23 = document.elementByID("23").asXML(); 
        String id24 = null; 
        if (document.elementByID("24") != null) { 
            id24 = document.elementByID("24").asXML(); 
        } else { 
            id24 = "null"; 
        } 

        System.out.println("id22=  " + id22); 
        System.out.println("id23=  " + id23); 
        System.out.println("id24=  " + id24); 
    } 


    public static void main(String args[]) throws DocumentException { 
        testParseXMLData("D:/person.xml"); 
    } 
}