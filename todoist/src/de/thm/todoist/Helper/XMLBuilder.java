package de.thm.todoist.Helper;

import android.app.Activity;
import com.devspark.appmsg.AppMsg;
import de.thm.todoist.Model.Task;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class XMLBuilder implements Constants {

    private Activity ctxt;

    public XMLBuilder(Activity ctxt) {
        super();
        this.ctxt = ctxt;
    }

    public boolean generateXML(ArrayList<Task> taskList, boolean silently, String filePath) {

        boolean result = false;
        if (taskList != null) {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            // Specify our own schema - this overrides the schemaLocation in the xml file
            //factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", "file:./test.xsd");
            try {
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();

                String NS_URL = "namespaceURL";
                Element rootElement = doc.createElement("todoist");
                rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                        "xsi:noNamespaceSchemaLocation", "http://kadrei.caelum.uberspace.de/todoist.xsd");
                doc.appendChild(rootElement);

                for (Task aTaskList : taskList) {

                    Element task = doc.createElement("task");
                    rootElement.appendChild(task);

                    Attr id = doc.createAttribute("id");
                    id.setValue(aTaskList.getId());
                    task.setAttributeNode(id);

                    Attr isDeleted = doc.createAttribute("isDeleted");
                    isDeleted.setValue(String.valueOf(aTaskList.isDeleted()));
                    task.setAttributeNode(isDeleted);

                    Element title = doc.createElement("title");
                    title.appendChild(doc.createTextNode(aTaskList.getTitle()));
                    task.appendChild(title);

                    Element description = doc.createElement("description");
                    description.appendChild(doc.createTextNode(aTaskList.getDescription()));
                    task.appendChild(description);

                    Element enddate = doc.createElement("enddate");
                    addDateToElement(doc, enddate, aTaskList.getEnddate());
                    task.appendChild(enddate);

                    Attr hasEndDate = doc.createAttribute("endEnabled");
                    hasEndDate.setValue(Boolean.toString(aTaskList.hasEndDate()));
                    enddate.setAttributeNode(hasEndDate);

                    Element done = doc.createElement("done");
                    done.appendChild(doc.createTextNode(String.valueOf(aTaskList.isDone())));
                    task.appendChild(done);

                    Element priority = doc.createElement("priority");
                    priority.appendChild(doc.createTextNode(String.valueOf(aTaskList.getPriority())));
                    task.appendChild(priority);

                    Element state = doc.createElement("state");
                    state.appendChild(doc.createTextNode(String.valueOf(aTaskList.getMode())));
                    task.appendChild(state);

                    Element lastUpdate = doc.createElement("last_updated");
                    addDateToElement(doc, lastUpdate, aTaskList.getLastUpdated());
                    task.appendChild(lastUpdate);

                    result = true;

                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult resultStr = new StreamResult(new File(filePath));
                transformer.transform(source, resultStr);

                if (!silently) AppMsg.makeText(ctxt, "File saved to: " + filePath, AppMsg.STYLE_INFO).show();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }

        return result;

    }

    private void addDateToElement(Document doc, Element addTo, GregorianCalendar calDate) {
        int minutes = calDate.get(GregorianCalendar.MINUTE);
        Element minutesEl = doc.createElement("minutes");
        minutesEl.appendChild(doc.createTextNode(String.valueOf(minutes)));
        addTo.appendChild(minutesEl);

        int hour = calDate.get(GregorianCalendar.HOUR_OF_DAY);
        Element hourEl = doc.createElement("hour");
        hourEl.appendChild(doc.createTextNode(String.valueOf(hour)));
        addTo.appendChild(hourEl);

        int day = calDate.get(GregorianCalendar.DAY_OF_MONTH);
        Element dayEl = doc.createElement("day");
        dayEl.appendChild(doc.createTextNode(String.valueOf(day)));
        addTo.appendChild(dayEl);

        int month = calDate.get(GregorianCalendar.MONTH);
        Element monthEl = doc.createElement("month");
        monthEl.appendChild(doc.createTextNode(String.valueOf(month)));
        addTo.appendChild(monthEl);

        int year = calDate.get(GregorianCalendar.YEAR);
        Element yearEl = doc.createElement("year");
        yearEl.appendChild(doc.createTextNode(String.valueOf(year)));
        addTo.appendChild(yearEl);

        String timezone = calDate.getTimeZone().getID();
        Attr timezoneAttr = doc.createAttribute("Timezone");
        timezoneAttr.setValue(String.valueOf(timezone));
        addTo.setAttributeNode(timezoneAttr);

    }

}
