package benchmark.tools;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.util.*;

public class ResultTransformOriginalStyle {
	static StringBuffer sb;
	static FileWriter query_size_of_stores;
	static FileWriter store_size_of_queries;
	static FileWriter size_store_of_queries;
	static FileWriter store_query_of_size;
	static FileWriter overview;
	static String[][][] storesQueriesSizes;
	static String[][] storesSizes;
	static String[][] dimensionArrays;
	
	private final static String[] queries = {   "Query 1", "Query 2", "Query 3", "Query 4",
		 			                            "Query 5", "Query 6", "Query 7", "Query 8",
		 								        "Query 9", "Query 10", "Query 11", "Query 12"};
	private static final String[] sizes = { "50k", "250k", "1m", "5m", "25m", "100m", "200m"} ;
	private static final String queryLink = "http://www4.wiwiss.fu-berlin.de/bizer/BerlinSPARQLBenchmark/spec/index.html#queryTripleQ";
	
	private static HashMap<String, Integer> sizeMap = new HashMap<String, Integer>();

	static String queryParameter = "aqet";
	static String querymixParameter = "qmph";
	
	// AFS Not retrived currently.
    static String queryPerSecondParameter = "qps";

    static boolean american = true;//switch . and ,
	
	public static void main(String argv[]) {
		String[] storeNames = new String[argv.length];
		File[] storeDirs = new File[argv.length];
		for(int i=0; i<sizes.length;i++)
			sizeMap.put(sizes[i], i);
		
		init();
		
		for(int i=0;i<argv.length;i++) {
			try {
			(storeDirs[i] = new File(argv[i])).createNewFile();
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			storeNames[i] = storeDirs[i].getName();
		}
		
		createHtmlFile(query_size_of_stores);
		createHtmlFile(store_size_of_queries);
		createHtmlFile(size_store_of_queries);
		createHtmlFile(store_query_of_size);
		createHtmlFile(overview);

		storesQueriesSizes = new String[storeNames.length][sizes.length][queries.length];
		storesSizes = new String[storeNames.length][sizes.length];
		dimensionArrays = new String[3][];
		dimensionArrays[0] = storeNames;
		dimensionArrays[1] = sizes;
		dimensionArrays[2] = queries;
		
	 try{
		    SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		    ResultHandler1 handler = new ResultHandler1();
		for(int x=0; x<storeNames.length;x++) {
			File[] files = storeDirs[x].listFiles(new FileFilter1());
			if(files==null) {
				System.err.println("Can't read files from directory: " + storeDirs[x].getAbsolutePath());
				System.exit(-1);
			}
			for(int i=0;i<files.length;i++) {
				File file = files[i];
				int sizeIndex = checkSizeIndex(file.getName());
				if(sizeIndex==-1) {
					System.err.println("Error: XML result file names must contain size metrics e.g. store_50k.xml for " + file.getName());
					
//					System.exit(-1);
				}
				else
				{
					InputStream is = new FileInputStream(file);
					String[] queries = storesQueriesSizes[x][sizeIndex];
	
					handler.setArray(queries);
					saxParser.parse(is, handler);
					storesSizes[x][sizeIndex] = handler.getQmValue();
				}
			}
		}

	  } catch(Exception e) {
			System.err.println("SAX Error");
			e.printStackTrace();
	  }
	  
	  try{
	  	create_query_size_of_stores_table(storeNames);
	  	create_store_query_of_sizes_table(sizes);
	  	create_store_size_of_query_table(queries);
	  	create_size_store_of_query_table(queries);
	  	create_overview_table();
	  } catch(IOException e) {
		  e.printStackTrace();
		  System.exit(-1);
	  }

	  	closeHtmlFile(query_size_of_stores);
	  	closeHtmlFile(store_size_of_queries);
	  	closeHtmlFile(size_store_of_queries);
		closeHtmlFile(store_query_of_size);
		closeHtmlFile(overview);
		
		System.out.println("done");
	}
	
	//find out the size this files is about
	private static int checkSizeIndex(String name) {
		String[] sortedSizes = sizes.clone();
		Arrays.sort(sortedSizes, new StringLengthComparator1());
		for(int i=0; i<sortedSizes.length; i++) {
			if((name.toLowerCase()).contains(sortedSizes[i].toLowerCase())) return sizeMap.get(sortedSizes[i]);
		}
		return -1;
	}
	
	//Create HTML file
	private static void createHtmlFile(FileWriter fw) {
		try {
			fw.append("<html>\n<head>\n  <title>Benchmark Results</title>\n</head>\n");
			fw.append("<body>\n");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//Close HTML file
	private static void closeHtmlFile(FileWriter fw) {
		try {
			fw.append("</body></html>");
			fw.flush();
			fw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * Generate an HTML table. dimX and dimY are different numbers from the set {0,1,2},
	 * representing 0:stores, 1:sizes, 2:queries to define the dimensions of the table.
	 */
	private static String createHtmlTable(String name, int dimX, int dimY, int fixedDim, int fixedValue, boolean overall) {
		StringBuffer sb = new StringBuffer();
		String[] xDimArray = dimensionArrays[dimX];
		String[] yDimArray = dimensionArrays[dimY];
		
		IntReference1 x = null;
		IntReference1 y = null;
		
		IntReference1 storeInd = new IntReference1(0);
		IntReference1 queryInd = new IntReference1(0);
		IntReference1 sizeInd = new IntReference1(0);
		
		//define x-axis
		if(dimX==0)
			x = storeInd;
		else if(dimX==1)
			x = sizeInd;
		else if(dimX==2)
			x = queryInd;
		
		
		//define y-axis
		if(dimY==0)
			y = storeInd;
		else if(dimY==1)
			y = sizeInd;
		else if(dimY==2)
			y = queryInd;
				
		//fixed dimension
		if(fixedDim==0)
			storeInd = new IntReference1(fixedValue);
		else if(fixedDim==1)
			sizeInd = new IntReference1(fixedValue);
		else if(fixedDim==2)
			queryInd = new IntReference1(fixedValue);
		
		
		if(x==null || y==null) {
			System.err.println("Wrong dimensions");
			System.exit(-1);
		}
		
		//Append table head
		sb.append("<h4>" + name + "</h4>\n");
		sb.append("<table style=\"text-align: center; width: 80%;\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\">");
		sb.append("<b><tr><th>&nbsp;</th>");
		for(int i=0; i < xDimArray.length;i++)
			sb.append("<th>"+xDimArray[i]+"</th>");
		sb.append("</tr>\n");
		
		for(y.setValue(0); y.getValue() < yDimArray.length;y.inc()) {
			sb.append("<tr><td><b>"+yDimArray[y.getValue()]+"</b></td>");
			for(x.setValue(0); x.getValue() < xDimArray.length;x.inc()) {
				String val;
				if(overall)
					val = storesSizes[storeInd.getValue()][sizeInd.getValue()];
				else
					val = storesQueriesSizes[storeInd.getValue()][sizeInd.getValue()][queryInd.getValue()];
				if(val!=null) {
					if(val.equals("0.0"))
						sb.append("<td>not executed</td>");
					else {
						if(american)
							sb.append("<td>"+val+"</td>");
						else
							sb.append("<td>"+val.replace('.', ',')+"</td>");
					}
				}
				else
					sb.append("<td>no value</td>");
			}
			sb.append("</tr>\n");
		}
		
		sb.append("</table>\n");
		return sb.toString();
	}
	
	private static void create_query_size_of_stores_table(String[] stores) throws IOException{
		for(int i=0;i<stores.length;i++) {
			query_size_of_stores.append(createHtmlTable(stores[i], 1, 2, 0, i, false));
		}
	}
	
	private static void create_store_size_of_query_table(String[] queries) throws IOException{
		for(int i=0;i<queries.length;i++) {
			store_size_of_queries.append(createHtmlTable(queries[i], 1, 0, 2, i, false));
		}
	}
	
	private static void create_overview_table() throws IOException{
			overview.append(createHtmlTable(querymixParameter, 1, 0, 2, 0, true));
			overview.append(createHtmlTable(querymixParameter, 0, 1, 2, 0, true));
	}
	
	private static void create_size_store_of_query_table(String[] queries) throws IOException{
		for(int i=0;i<queries.length;i++) {
			size_store_of_queries.append(createHtmlTable(queries[i], 0, 1, 2, i, false));
		}
	}
	
	private static void create_store_query_of_sizes_table(String[] sizes) throws IOException{
		for(int i=0;i<sizes.length;i++) {
			store_query_of_size.append(createHtmlTable(sizes[i], 0, 2, 1, i, false));
		}
	}
	
	//Init Output Files
	private static void init() {
	    try {
	        query_size_of_stores = new FileWriter("query_and_size_tables_of_stores.html");
	        store_size_of_queries = new FileWriter("store_and_size_tables_of_queries.html");
	        size_store_of_queries = new FileWriter("size_and_store_tables_of_queries.html");
	        store_query_of_size = new FileWriter("store_and_query_tables_of_sizes.html");
	        overview = new FileWriter("overview.html");
	    } catch(IOException e) {
	        System.err.println("Could not open Input directories!");
	        System.exit(-1);
	    }
	}

	private static class FileFilter1 implements FilenameFilter {
	    public boolean accept(File dir, String name) {
	        return name.endsWith(".xml");
	    }
	}

	private static class IntReference1 {
	    int value;
	    IntReference1(int i) {
	        value = i;
	    }

	    public int getValue() {
	        return value;
	    }

	    public void setValue(int v) {
	        value = v;
	    }

	    public void inc() {
	        value++;
	    }

	    @Override
	    public String toString() {
	        return (new Integer(value)).toString();
	    }
	}

	private static class ResultHandler1 extends DefaultHandler {
	    boolean inQueryAttr;
	    boolean inQMAttr;
	    String[] resultArray;
	    int index;
	    String qmValue;
	    String queryAttr = ResultTransformOriginalStyle.queryParameter;
	    String qmAttr = ResultTransformOriginalStyle.querymixParameter;

	    ResultHandler1() {
	    }

	    @Override
	    public void startElement( String namespaceURI,
	                              String localName,   // local name
	                              String qName,       // qualified name
	                              Attributes attrs ) {
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
	            String t = "";
	            for ( int i = start; i < (start + length); i++ )
	                t += ch[i];

	            resultArray[index++] = t;
	        }
	        else if(inQMAttr) {
	            String t = "";
	            for ( int i = start; i < (start + length); i++ )
	                t += ch[i];

	            qmValue = t;
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

	private static class StringLengthComparator1 implements Comparator<String> {
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