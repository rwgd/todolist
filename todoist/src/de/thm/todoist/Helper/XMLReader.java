package de.thm.todoist.Helper;

/**
 * Created by Benedikt on 23.02.14.
 */

import android.app.Activity;
import android.util.Log;
import com.devspark.appmsg.AppMsg;
import de.thm.todoist.Model.Task;
import de.thm.todoist.R;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class XMLReader implements Constants {

    private Activity ctxt;

    public XMLReader(Activity ctxt) {
        super();
        this.ctxt = ctxt;
    }

    private TimeZone mTZ = TimeZone.getTimeZone("Europe/Berlin");
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss.SSS");

    public ArrayList<Task> loadXML(boolean silently, String filePath) {
        df.setTimeZone(mTZ);
        ArrayList<Task> result = new ArrayList<Task>();
        File f = new File(filePath);
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = null;
            document = builder.parse(f);

            Element root = document.getDocumentElement();
            NodeList nlroot = root.getChildNodes();

            String title = "", description = "";
            GregorianCalendar enddate = new GregorianCalendar();
            boolean done = false;
            boolean hasEndDate = false;
            int priority = 0;
            String id = "";
            boolean isDeleted = false;
            GregorianCalendar last_updated = new GregorianCalendar();
            int syncMode = 0;

            for (int k = 0; k < nlroot.getLength(); k++) {
                Node nvc = nlroot.item(k);
                if (nvc.getNodeName().equals("task")) {
                    if (nvc.getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) nvc;
                        String tag = e.getTagName();
                        if (e.hasAttribute("id")) {
                            id = e.getAttribute("id");
                        }
                        if (e.hasAttribute("isDeleted")) {
                            isDeleted = Boolean.parseBoolean(e.getAttribute("isDeleted"));
                        }
                        if (nvc.hasChildNodes()) {
                            NodeList childNodes = nvc.getChildNodes();
                            for (int i = 0; i < childNodes.getLength(); i++) {
                                Node child = childNodes.item(i);
                                if (child.getNodeType() == Node.ELEMENT_NODE) {
                                    Element childElement = (Element) child;
                                    String childTag = childElement.getTagName();
                                    if (childTag.equals("title")) {
                                        title = childElement.getTextContent();
                                    }
                                    if (childTag.equals("description")) {
                                        description = childElement.getTextContent();
                                    }
                                    if (childTag.equals("priority")) {
                                        priority = Integer.parseInt(childElement.getTextContent());
                                    }

                                    if (childTag.equals("done")) {
                                        done = Boolean.parseBoolean(childElement.getTextContent());
                                    }
                                    if (childTag.equals("enddate")) {
                                        enddate = readDate(childElement);
                                        if (childElement.hasAttribute("endEnabled")) {
                                            hasEndDate = Boolean.parseBoolean(childElement.getAttribute("endEnabled"));
                                        }
                                    }
                                    if (childTag.equals("last_updated")) {
                                        last_updated = readDate(childElement);
                                    }
                                    if (childTag.equals("state")) {
                                        syncMode = Integer.parseInt(childElement.getTextContent());
                                    }
                                }
                            }
                        }
                        Task curTask = new Task(id, title, description, enddate, done, priority, hasEndDate, last_updated, isDeleted, syncMode);
                        result.add(curTask);
                    }
                }
            }
            if (!silently) AppMsg.makeText(ctxt, "File loaded from: " + filePath, AppMsg.STYLE_INFO).show();
        } catch (ParserConfigurationException e) {
            Log.e("Fehler beim importieren", "Error:", e);
            if (!silently) AppMsg.makeText(ctxt, R.string.import_fail, AppMsg.STYLE_ALERT).show();
        } catch (SAXException e) {
            Log.e("Fehler beim importieren", "Error:", e);
            if (!silently) AppMsg.makeText(ctxt, R.string.import_fail, AppMsg.STYLE_ALERT).show();
        } catch (IOException e) {
            Log.e("Fehler beim importieren", "Error:", e);
            if (!silently) AppMsg.makeText(ctxt, R.string.import_fail, AppMsg.STYLE_ALERT).show();
        }
        return result;
    }

    private GregorianCalendar readDate(Element dateElement) {
        GregorianCalendar date = new GregorianCalendar();
        String timezone = "UTC";
        int month = 0;
        int year = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        if (dateElement.hasAttribute("Timezone")) timezone = dateElement.getAttribute("Timezone");
        NodeList childNodes = dateElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {


                Element childElement = (Element) child;
                String childTag = childElement.getTagName();
                if (childTag.equals("month")) {
                    month = Integer.valueOf(childElement.getTextContent());
                }
                if (childTag.equals("year")) {
                    year = Integer.valueOf(childElement.getTextContent());
                }
                if (childTag.equals("day")) {
                    day = Integer.valueOf(childElement.getTextContent());
                }
                if (childTag.equals("hour")) {
                    hour = Integer.valueOf(childElement.getTextContent());
                }
                if (childTag.equals("minutes")) {
                    minute = Integer.valueOf(childElement.getTextContent());
                }
            }
            date.set(year, month, day, hour, minute);
            date.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        return date;
    }
}

