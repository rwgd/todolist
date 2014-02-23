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
            try {
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("todoist");
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
                    enddate.appendChild(doc.createTextNode(aTaskList.getEndDateString()));
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
                    lastUpdate.appendChild(doc.createTextNode(aTaskList.getLastUpdatedString()));
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

}
