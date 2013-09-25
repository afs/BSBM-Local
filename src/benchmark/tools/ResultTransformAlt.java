package benchmark.tools;

import java.io.FileInputStream ;
import java.io.InputStream ;
import java.util.* ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import javax.xml.parsers.SAXParser ;
import javax.xml.parsers.SAXParserFactory ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Lib ;
import org.xml.sax.Attributes ;
import org.xml.sax.helpers.DefaultHandler ;

public class ResultTransformAlt {
    static class QueryResult {
        final String storeSize ;
        final int nr ;
        final String queryName ;
        final double qps ;
        public QueryResult(String storeSize, int nr, String queryName, double qps) {
            this.storeSize = storeSize ;
            this.nr = nr ;
            this.queryName = queryName ;
            this.qps = qps ;
        }
        @Override
        public String toString() { 
            return String.format("[%s:%d] %s %f", storeSize, nr, queryName, qps);
        }
    }
    
    private final static String[] queries = { "Query 1", "Query 2", "Query 3", "Query 4",
                                              "Query 5", "Unused", "Query 7", "Query 8",
                                              "Query 9", "Query 10", "Query 11", "Query 12"};
    
    // Store name (from file) ->  query results.
    
    
    
    public static void main(String ... argv) {
        
        //argv = new String[] {"Results/res-1m.xml", "Results/res-10m.xml", } ;
        
        Set<QueryResult> results = new HashSet<>() ;
        
        for ( String fn : argv ) {
            parse(fn, results) ;
        }
        
        List<String> stores = stores(results) ;
        String x = generateHtml(stores, results) ;
        System.out.println(x) ;
//        try {
//            FileWriter fw = new FileWriter("results.html") ;
//            fw.write(x); 
//            fw.close() ;
//        } catch (IOException ex) {
//            IO.exception(ex); 
//        }
//        System.out.println("Results writtern") ;
    }
    
    private static List<String> stores(Set<QueryResult> results) {
        Set<String> x = new HashSet<>() ;
        for ( QueryResult qr : results )
            x.add(qr.storeSize) ;
        List<String> storesBySizeList = Iter.toList(x.iterator()) ;
        Collections.sort(storesBySizeList, new StringLengthComparator());
        return storesBySizeList ;
    }

    private static String generateHtml(List<String> storesBySizeList , Set<QueryResult> results) {
        StringBuilder sbuff = new StringBuilder() ;
        sbuff.append("<h4>Queries per second</h4>") ;
        
        sbuff.append("<table style=\"text-align: center; width: 60%;\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\">") ;
        sbuff.append("<tr>\n") ;
        sbuff.append("   <th>&nbsp;</th>\n") ;
        for ( String s : storesBySizeList )
            sbuff.append("   <th>"+s+"</th>\n") ;
        sbuff.append("</tr>\n") ;
        
        for ( String qn : queries ) {
            sbuff.append("<tr>\n") ;
            sbuff.append("   <td>"+qn+"</td>\n") ;
            for ( String s : storesBySizeList ) {
                QueryResult qr = getQueryResult(results, qn, s) ;
                
                if ( qr == null )
                    sbuff.append("   <td> ---- </td>\n") ;
                else
                    sbuff.append(String.format("   <td>%.1f [%.2fms]</td>\n", qr.qps, (1000/qr.qps))) ;
            }
            sbuff.append("</tr>\n") ;
        }
        sbuff.append("</table>") ;
        return sbuff.toString() ; 
    }

    private static QueryResult getQueryResult(Set<QueryResult> results, String qn, String store) {
        for ( QueryResult qr : results ) {
            if ( Lib.equal(qr.queryName, qn) && Lib.equal(qr.storeSize, store) )
                return qr ;
        }
        return null ;
    }

    private static void parse(String fn, Set<QueryResult> results) {
        Pattern p = Pattern.compile("(\\d+(k|m))", Pattern.CASE_INSENSITIVE) ;
        Matcher matcher = p.matcher(fn) ;
        if ( ! matcher.find() )
            throw new IllegalArgumentException() ;
        String storeSize = matcher.group(1) ;
        try{
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            ResultHandler handler = new ResultHandler(storeSize, results);
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
        
        int index = 0 ;
        int nr = -1  ;
        double queriesPerSecond ;
        String queryName ; 
        String storeSize ;
        
        String qmValue;
        String queryAttr = ResultTransform.queryParameter;  // "qps";
        String qmAttr = ResultTransform.querymixParameter;  // "qmph"
        private Set<QueryResult> results ;

        ResultHandler(String storeSize, Set<QueryResult> results ) {
            this.storeSize = storeSize ;
            this.results = results ;
        }

        @Override
        public void startElement( String namespaceURI,
                                  String localName,   // local name
                                  String qName,       // qualified name
                                  Attributes attrs ) {

            if ( qName.equals("query") ) {
                String x = attrs.getValue("nr") ;
                try {
                    nr = Integer.parseInt(x) ;
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
            else if ( qName.equals("query") ) {
                if ( queriesPerSecond != -1 ) {
                    queryName = queries[nr-1] ; 
                    QueryResult queryResult = new QueryResult(storeSize, nr, queryName, queriesPerSecond) ; 
                    results.add(queryResult) ;
                    init() ;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if(inQueryAttr) {
                StringBuilder t = new StringBuilder();
                for ( int i = start; i < (start + length); i++ )
                    t.append(ch[i]);

                queriesPerSecond = Double.parseDouble(t.toString().trim()) ;
                index++ ;
            }
            else if(inQMAttr) {
                StringBuilder t = new StringBuilder();
                for ( int i = start; i < (start + length); i++ )
                    t.append(ch[i]);

                qmValue = t.toString();
            }
        }

        private void init() {
            inQueryAttr = false;
            index = 0;
            nr = -1 ;
            queryName = null ;
            queriesPerSecond = -1 ;
        }
    }

    static class StringLengthComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return Integer.compare(size(s1), size(s2)) ;
        }
        
        int size(String str) {
            Pattern p = Pattern.compile("(\\d+)(k|m)", Pattern.CASE_INSENSITIVE) ;
            Matcher matcher1 = p.matcher(str) ;
            matcher1.find() ;
            int s = Integer.parseInt(matcher1.group(1)) ;
            String x = matcher1.group(2) ;
            
            if ( x.equalsIgnoreCase("k") )
                s = s*1000 ;
            else if ( x.equalsIgnoreCase("m") )
                s = s*1000*1000 ;
            return s ;
        }
    }
}
