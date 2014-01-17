package de.thm.todoist.Helper;

import android.content.Context;
import android.widget.Toast;
import de.thm.todoist.Model.Task;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;

public class XMLBuilder implements Constants {

    private ArrayList<Task> taskList;
    private Context ctxt;

    public XMLBuilder(ArrayList<Task> taskList, Context ctxt) {
        super();
        this.taskList = taskList;
        this.ctxt = ctxt;
    }

    public boolean generateXML() throws Exception {

        boolean result = false;
        if (taskList != null) {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("todoist");
            doc.appendChild(rootElement);

            for (int i = 0; i < taskList.size(); i++) {

                Element task = doc.createElement("task");
                rootElement.appendChild(task);

                Attr attr = doc.createAttribute("id");
                attr.setValue(taskList.get(i).getId());
                task.setAttributeNode(attr);

                Element title = doc.createElement("title");
                title.appendChild(doc.createTextNode(taskList.get(i).getTitle()));
                task.appendChild(title);

                Element description = doc.createElement("description");
                description.appendChild(doc.createTextNode(taskList.get(i).getDescription()));
                task.appendChild(description);

                Element enddate = doc.createElement("enddate");
                enddate.appendChild(doc.createTextNode(taskList.get(i).getEnddate()));
                task.appendChild(enddate);

                Element done = doc.createElement("done");
                done.appendChild(doc.createTextNode(String.valueOf(taskList.get(i).isDone())));
                task.appendChild(done);

                Element priority = doc.createElement("priority");
                priority.appendChild(doc.createTextNode(String.valueOf(taskList.get(i).getPriority())));
                task.appendChild(priority);

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult resultStr = new StreamResult(new File(SAVE_DIR_XML));
            transformer.transform(source, resultStr);

            Toast.makeText(ctxt, "File saved to: " + SAVE_DIR_XML, Toast.LENGTH_LONG).show();

			/*	 Transformer transformer = TransformerFactory.newInstance().newTransformer();
             StreamResult resultStr = new StreamResult(new StringWriter());
			 DOMSource source = new DOMSource(doc);
			 transformer.transform(source, resultStr);
			 String test =  resultStr.getWriter().toString();
			 
			 Log.e("testXML", test);*/


        }

        return result;

    }

}
