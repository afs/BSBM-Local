/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package benchmark.testdriver;

import java.io.IOException ;
import java.io.InputStream ;
import java.net.SocketTimeoutException ;

import javax.xml.parsers.SAXParser ;
import javax.xml.parsers.SAXParserFactory ;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.xml.sax.Attributes ;
import org.xml.sax.helpers.DefaultHandler ;
import benchmark.qualification.QueryResult ;

import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;

public class LocalConnectionJena implements ServerConnection
{
    private String queryService;
    private String updateService;
    private String defaultGraph;
    private static Logger logger = Logger.getLogger( LocalConnectionJena.class );
    private int timeout;
    private Dataset dataset ;
    
    public LocalConnectionJena(String queryService, String updateService, String defaultGraph, int timeout)
    {
        this.queryService = queryService ;
        this.updateService = updateService ;
        String location = queryService.substring("jena:".length()) ;
        this.defaultGraph = defaultGraph ;
        this.timeout = timeout ;
        
        String assemblerFile = location ;
        System.out.println("Assemble dataset ...") ;
        this.dataset = 
            (Dataset)AssemblerUtils.build(assemblerFile, JA.getURI()+"RDFDataset")  ; 
        System.out.println("Assemble dataset ... finished") ;
        if ( dataset == null )
        {
            System.out.println("Assemble dataset ... no dataset") ;
            throw new JenaException("No valid assembler description of a dataset") ;
        }
        //this.dataset = TDBFactory.createDataset(location) ;
    }
    
    @Override
    public void close()
    {}

    @Override
    public void executeQuery(Query query, byte queryType)
    {
        executeQuery(query.getQueryString(), queryType, query.getNr(), query.getQueryMix()) ;
    }

    @Override
    public void executeQuery(CompiledQuery query, CompiledQueryMix queryMix)
    {
        System.err.println("executeQuery on CompiledQuery") ;
    }

    // Worker.
    private void executeQuery(String queryString, byte queryType, int queryNr, QueryMix queryMix)
    {
        double timeInSeconds = 0.0 ;
        //NetQuery qe = new NetQuery(serverURL, queryString, queryType, defaultGraph, timeout);
        //NetQuery qe = null ;
        int queryMixRun = queryMix.getRun() + 1;

        int resultCount = 0;
        Timer timer = new Timer() ;
        timer.startTimer() ;
        try {
            resultCount = executeQuery1(queryString, dataset) ;
        } catch (Throwable th)
        {
            System.err.println("Throwable: "+th.getMessage()) ;
            long timeMilli = timer.readTimer() ;
            System.out.println("Query " + queryNr + ": " + (timeMilli/1000.0) + " seconds timeout!");
            queryMix.reportTimeOut();//inc. timeout counter
            queryMix.setCurrent(0, 1.0*timeMilli);

        }
        long timeMilli = timer.endTimer() ;
        
//        InputStream is = qe.exec();
//        if(is==null) {
//            double t = this.timeout/1000.0;
//            System.out.println("Query " + queryNr + ": " + t + " seconds timeout!");
//            queryMix.reportTimeOut();//inc. timeout counter
//            queryMix.setCurrent(0, t);
//            qe.close();
//            return;
//        }
//        int resultCount = 0;
//        //Write XML result into result
//        try {
//            if(queryType==Query.SELECT_TYPE)
//                resultCount = countResults(is);
//            else
//                resultCount = countBytes(is);
//        } catch(SocketTimeoutException e) {
//            double t = this.timeout/1000.0;
//            System.out.println("Query " + queryNr + ": " + t + " seconds timeout!");
//            queryMix.reportTimeOut();//inc. timeout counter
//            queryMix.setCurrent(0, t);
//            qe.close();
//            return;
//        }
        
        timeInSeconds = timeMilli/1000.0 ; // qe.getExecutionTimeInSeconds();

        if(logger.isEnabledFor( Level.ALL ) && queryMixRun > 0)
            logResultInfo(queryNr, queryMixRun, timeInSeconds,
                       queryString, queryType,
                       resultCount);
        
        queryMix.setCurrent(resultCount, timeInSeconds);
        //qe.close();
    }

    private static int executeQuery1(String queryString, Dataset dataset)
    {
        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString) ;
        QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset) ;
        if ( query.isSelectType() )
            return doSelectQuery(query, queryExecution) ;
        if ( query.isDescribeType() )
            doDescribeQuery(query, queryExecution) ;
        else if ( query.isConstructType() )
            doConstructQuery(query, queryExecution) ;
//        else if ( query.isAskType() )
//            doAskQuery(query, queryExecution) ;
        else
            System.err.println("Unsupported query type: "+query.getQueryType()) ;
        queryExecution.close() ;
        return -1 ;
    }
    
    private static int doSelectQuery(com.hp.hpl.jena.query.Query query, QueryExecution queryExecution)
    {
        try {
            return ResultSetFormatter.consume(queryExecution.execSelect()) ;
        } finally { queryExecution.close(); }
    }

    private static int doConstructQuery(com.hp.hpl.jena.query.Query query, QueryExecution queryExecution)
    {
        try {
            Model m = queryExecution.execConstruct() ;
            return 1913 ; // ???
        } finally { queryExecution.close(); }
    }

    private static int doDescribeQuery(com.hp.hpl.jena.query.Query query, QueryExecution queryExecution)
    {
        try {
            Model m = queryExecution.execDescribe() ;
            return 1816 ; // ???
        } finally { queryExecution.close(); }
    }

    @Override
    public QueryResult executeValidation(Query query, byte queryType)
    {
        return null ;
    }

    
    // ----- Copied/hacked from SPARQLConnection
    
    private class ResultHandler extends DefaultHandler {
        private int count;
        
        ResultHandler() {
            count = 0;
        }
        
        @Override
        public void startElement( String namespaceURI,
                String localName,   // local name
                String qName,       // qualified name
                Attributes attrs ) {
            if(qName.equals("result"))
                count++;
        }

        public int getCount() {
            return count;
        }
    }
    
    
    private int countBytes(InputStream is) {
        int nrBytes=0;
        byte[] buf = new byte[10000];
        int len=0;
        //  StringBuffer sb = new StringBuffer(1000);
        try {
            while((len=is.read(buf))!=-1) {
                nrBytes += len;//resultCount counts the returned bytes
                //          String temp = new String(buf,0,len);
                //          temp = "\n\n" + temp + "\n\n";
                //          logger.log(Level.ALL, temp);
                //          sb.append(temp);
            }
        } catch(IOException e) {
            System.err.println("Could not read result from input stream");
        }
        //  System.out.println(sb.toString());
        return nrBytes;
    }

    private void logResultInfo(int queryNr, int queryMixRun, double timeInSeconds,
                               String queryString, byte queryType,
                               int resultCount) {
        StringBuffer sb = new StringBuffer(1000);
        sb.append("\n\n\tQuery " + queryNr + " of run " + queryMixRun + " has been executed ");
        sb.append("in " + String.format("%.6f",timeInSeconds) + " seconds.\n" );
        sb.append("\n\tQuery string:\n\n");
        sb.append(queryString);
        sb.append("\n\n");
    
        //Log results
        if(queryType==Query.DESCRIBE_TYPE)
            sb.append("\tQuery(Describe) result (" + resultCount + " Bytes): \n\n");
        else if(queryType==Query.CONSTRUCT_TYPE)
            sb.append("\tQuery(Construct) result (" + resultCount + " Bytes): \n\n");
        else
            sb.append("\tQuery results (" + resultCount + " results): \n\n");
        

        sb.append("\n__________________________________________________________________________________\n");
        logger.log(Level.ALL, sb.toString());
    }
    
    private int countResults(InputStream s) throws SocketTimeoutException {
        ResultHandler handler = new ResultHandler();
        int count=0;
        try {
          SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
//        ByteArrayInputStream bis = new ByteArrayInputStream(s.getBytes("UTF-8"));
          saxParser.parse( s, handler );
          count = handler.getCount();
        } catch(SocketTimeoutException e) { throw new SocketTimeoutException(); }
          catch(Exception e) {
            System.err.println("SAX Error");
            e.printStackTrace();
            return -1;
        }
        return count;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */