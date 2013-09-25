package benchmark.tools;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.InputStream ;
import java.io.Serializable ;
import java.util.Comparator ;
import java.util.HashMap ;

import javax.xml.parsers.SAXParser ;
import javax.xml.parsers.SAXParserFactory ;

import benchmark.tools.ResultTransform.FileFilter ;
import benchmark.tools.ResultTransform.ResultHandler ;
import org.apache.jena.atlas.lib.MultiMap ;
import org.xml.sax.Attributes ;
import org.xml.sax.helpers.DefaultHandler ;

public class ResultTransformAlt {
    static class QueryResult {
        int nr ;
        String query ;
        double qps ;
    }
    
    private final static String[] queries = { "Query 1", "Query 2", "Query 3", "Query 4",
                                              "Query 5", "Unused", "Query 7", "Query 8",
                                              "Query 9", "Query 10", "Query 11", "Query 12"};
    
    // Store name (from file) ->  query results.
    MultiMap<String, QueryResult> results = MultiMap.createMapList() ;
    
    
    public static void main(String ... argv) {
        for ( String fn : argv ) {
            parse(fn) ;
        }
        
        
    }
    
    
    
    
    private static void parse(String fn) {
        try{
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            ResultHandler handler = new ResultHandler();
            // Name to size.
            InputStream is = new FileInputStream(fn);
            //handler.setArray(queries);
            saxParser.parse(is, handler);
        } catch(Exception e) {
            System.err.println("SAX Error");
            e.printStackTrace();
        }
    }


    static class ResultHandler extends DefaultHandler {
        boolean inQueryAttr;
        boolean inQMAttr;
        String[] resultArray;
        int index;
        int nr = 0 ;
        String qmValue;
        String queryAttr = ResultTransform.queryParameter;
        String qmAttr = ResultTransform.querymixParameter;

        ResultHandler() {
        }

        @Override
        public void startElement( String namespaceURI,
                                  String localName,   // local name
                                  String qName,       // qualified name
                                  Attributes attrs ) {

            if ( qName.equals("query") ) {
                String x = attrs.getValue("nr") ;
                try {
                    nr=Integer.parseInt(x) ;
                    nr = nr - 1 ;
                } catch (NumberFormatException ex) {
                    System.err.println("Bad number: "+x) ;
                }
            } else


                if(qName.equals("bsbm"))
                    init();
                else if(qName.equals(queryAttr))
                    inQueryAttr = true;
                else if(qName.equals(qmAttr))
                    inQMAttr = true;
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if(qName.equals(queryAttr))
                inQueryAttr = false;
            else if(qName.equals(qmAttr))
                inQMAttr = false;
        }

        @Override
        public void characters(char[] ch,
                               int start,
                               int length) {
            if(inQueryAttr) {
                StringBuilder t = new StringBuilder();
                for ( int i = start; i < (start + length); i++ )
                    t.append(ch[i]);

                //resultArray[index] = t.toString();
                index++ ;
                resultArray[nr] = t.toString();
            }
            else if(inQMAttr) {
                StringBuilder t = new StringBuilder();
                for ( int i = start; i < (start + length); i++ )
                    t.append(ch[i]);

                qmValue = t.toString();
            }
        }

        public void setArray(String[] a) {
            resultArray = a;
            index = 0;
        }

        private void init() {
            inQueryAttr = false;
            index = 0;
        }

        public String getQmValue() {
            return qmValue;
        }
    }

    class StringLengthComparator implements Comparator<String>, Serializable{
        private static final long serialVersionUID = -5232659752583741930L;

        @Override
        public int compare(String s1, String s2) {
            if(s1.length() == s2.length())
                return 0;
            else if(s1.length()>s2.length())
                return -1;
            else
                return 1;
        }
    }

    
}
