package com.ibm.parsertools.bttsxml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


public class Main
{
	static String USAGE = "This application is used for copy UserAction Element \n" +
			              "from BT.xml to TS.xml according to the Event ID from TS.xml\n\n" +
			              "This is only from IBM use.\nCroypright@IBM 2012\n\n" +
			              "USAGE: Command <get/count> <BT.xml> <TS.xml>";
	
	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DocumentException, IOException
	{
		if (args.length != 3)
		{
			System.out.println(USAGE);
			return;
		}
		
		boolean isGet = true;
		String changeW = "ff";
		String changeWT = "xx";
		String bt_filePath = null;
        String ts_filePath = null;
		if(args[0].equals("get"))
		{
			bt_filePath = args[1];
	        ts_filePath = args[2];
		}
		else if(args[0].equals("count"))
		{
			bt_filePath = args[2];
	        ts_filePath = args[1];
	        isGet = false;
	        changeW = "xx";
	        changeWT = "ff";
		}
		else
		{
			System.out.println(USAGE);
			return;
		}
		
		XMLWriter writer = null;// 声明写XML的对象
        SAXReader bt_reader = new SAXReader();
        SAXReader ts_reader = new SAXReader();
        
        
//        String bt_filePath = "D:/xml/platform-msg.xml";
//        String ts_filePath = "D:/xml/blacktip-manual-x-edit012312.xml";
        
        Document bt_document = bt_reader.read(bt_filePath);// 读取XML文件
        Document ts_document = ts_reader.read(ts_filePath);// 读取XML文件
        
        //OutputFormat format = OutputFormat.createPrettyPrint();
        //format.setEncoding("UTF-8");// 设置XML文件的编码格式
        
        
        Element bt_root = bt_document.getRootElement();// 得到根节点
        Element ts_root = ts_document.getRootElement();// 得到根节点
        
        System.out.println("-------- Start --------");
        for (Iterator bt_i = bt_root.elementIterator(); bt_i.hasNext();)
        {
        	Element bt_e = (Element) bt_i.next(); 
        	
        	String eventID = bt_e.attributeValue("ID");
        	Element ts_e = ts_document.elementByID(eventID);
        	
        	if (ts_e == null && -1 != eventID.indexOf(changeW) )
        	{
        		//System.out.println("-------- " + eventID + " --------");
        		eventID = eventID.replaceAll(changeW, changeWT);
        		ts_e = ts_document.elementByID(eventID);
        		//System.out.println("???????? " + eventID + " --------");
           	}
        	if (ts_e != null)
    		{
        		if(isGet)
        		{
        			//System.out.println("======== " + eventID + " ========");
            		Element bt_user_action = bt_e.element("UserAction");
                	Element ts_user_action = ts_e.element("UserAction");
                	Element bt_Message = bt_e.element("MessageString");
                	Element ts_Message = ts_e.element("MessageString");
                	Element bt_MessageDS = bt_e.element("MessageDescription");
                	Element ts_MessageDs = ts_e.element("MessageDescription");
                	String ts_Message_String = ts_user_action.getTextTrim();
                	ts_Message_String = ts_Message_String + bt_Message.getTextTrim();
                	
                	ts_Message.setText(ts_Message_String);
                	ts_user_action.setContent(bt_user_action.content());
                	ts_MessageDs.setContent(bt_MessageDS.content());
                	
                	writer = new XMLWriter(new FileWriter(ts_filePath) );
                	writer.write(ts_document);
                	writer.close();
        		}
    		}
        	else
        	{
        		if(!isGet)
        		{
        			eventID = eventID.replaceAll(changeWT, changeW);
        			System.out.println("Event ID=\"" + eventID + "\"");
        		}
        	}
        	        	
        }
        System.out.println("-------- End --------");
	}

}
