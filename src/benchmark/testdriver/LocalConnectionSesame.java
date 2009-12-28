/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package benchmark.testdriver;

import java.io.File ;
import java.io.IOException ;
import java.io.InputStream ;
import java.net.SocketTimeoutException ;

import javax.xml.parsers.SAXParser ;
import javax.xml.parsers.SAXParserFactory ;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.openrdf.OpenRDFException ;
import org.openrdf.model.Resource ;
import org.openrdf.model.Statement ;
import org.openrdf.model.URI ;
import org.openrdf.model.Value ;
import org.openrdf.query.BindingSet ;
import org.openrdf.query.BooleanQuery ;
import org.openrdf.query.GraphQuery ;
import org.openrdf.query.GraphQueryResult ;
import org.openrdf.query.QueryLanguage ;
import org.openrdf.query.TupleQuery ;
import org.openrdf.query.TupleQueryResult ;
import org.openrdf.repository.RepositoryConnection ;
import org.openrdf.repository.sail.SailRepository ;
import org.openrdf.sail.nativerdf.NativeStore ;
import org.xml.sax.Attributes ;
import org.xml.sax.helpers.DefaultHandler ;
import benchmark.qualification.QueryResult ;

import com.hp.hpl.jena.sparql.util.Timer ;

public class LocalConnectionSesame implements ServerConnection
{
    private String serviceURL;
    private String defaultGraph;
    private static Logger logger = Logger.getLogger( LocalConnectionSesame.class );
    private int timeout;
    private RepositoryConnection conn = null ;
    private SailRepository repo ;
    
    
    public LocalConnectionSesame(String serviceURL, String defaultGraph, int timeout)
    {
        this.serviceURL = serviceURL ;
        String directory = serviceURL.substring("sesame:".length()) ;
        this.defaultGraph = defaultGraph ;
        this.timeout = timeout ;

        try {
            // ---- Neo4j
            /*
            NeoService neo = new EmbeddedNeo(directory);
            IndexService indexService = new LuceneIndexService( neo );
            RdfStore rdfStore = new VerboseQuadStore( neo, indexService );
            Sail sail = new NeoSail( neo, rdfStore );
            repo = new SailRepository( sail );
            */
            // ---- Neo4j
            
            if ( repo == null )
                repo = new SailRepository( new NativeStore(new File(directory)) );
            repo.initialize();
            conn = repo.getConnection();
        } catch (Exception ex)
        {
            System.err.println("Failed to open Sesame repo: "+directory) ;
            ex.printStackTrace(System.err) ;
            System.exit(1) ;
        }
    }
    
    
    
    @Override
    public void close()
    {
        try {

            if ( conn != null )
                conn.close() ;

            if ( repo != null )
                repo.shutDown() ;

        } catch (OpenRDFException ex)
        {
            ex.printStackTrace();
        }
    }

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
            //resultCount = executeQuery1(queryString, dataset) ;
            resultCount = executeQuery1(queryString) ;
        } catch (Throwable th)
        {
            System.err.println("Throwable: "+th.getMessage()) ;
            th.printStackTrace(System.err) ;
            long timeMilli = timer.readTimer() ;
            System.out.println("Query " + queryNr + ": " + (timeMilli/1000.0) + " seconds timeout!");
            queryMix.reportTimeOut();//inc. timeout counter
            queryMix.setCurrent(0, 1.0*timeMilli);

        }
        long timeMilli = timer.endTimer() ;
        
        timeInSeconds = timeMilli/1000.0 ; // qe.getExecutionTimeInSeconds();

        if(logger.isEnabledFor( Level.ALL ) && queryMixRun > 0)
            logResultInfo(queryNr, queryMixRun, timeInSeconds,
                       queryString, queryType,
                       resultCount);
        
        queryMix.setCurrent(resultCount, timeInSeconds);
        //qe.close();
    }

    private int executeQuery1(String queryString)
    {
        // Need to determine the query type:
        // Crude: string.contains

        if ( queryString.contains("SELECT") )
        {
            doSelectQuery(conn, queryString) ;
        }
        else if ( queryString.contains("CONSTRUCT") || queryString.contains("DESCRIBE") )
        {
            doGraphQuery(conn, queryString) ;
        }
        else if ( queryString.contains("ASK") )
        {
            doBooleanQuery(conn, queryString) ;
        }
        else
        {
            System.err.println("Unrecognized: "+queryString) ;
            throw new RuntimeException("Unrecognized: "+queryString) ;
        }

        return -1 ;
    }
    
    private static void doSelectQuery(RepositoryConnection conn, String queryString)
    {
        try {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = tupleQuery.evaluate();
            // Consume results.
            for ( ; result.hasNext() ; )
            {
                BindingSet bindingSet = result.next() ;

                for ( String vn : result.getBindingNames() )
                {
                    Value v = bindingSet.getValue(vn) ;
                    String x = null ;
                    if ( v != null )
                        x = v.stringValue() ;
                }
            }
            result.close();
        } catch (OpenRDFException ex)
        {
            ex.printStackTrace();
        } finally { }

    }

    private static void doGraphQuery(RepositoryConnection conn, String queryString)
    {
        try {
            GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
            GraphQueryResult results = graphQuery.evaluate() ;
            for ( ; results.hasNext() ; )
            {
                Statement stmt = results.next() ;
                Resource resource = stmt.getSubject() ;
                URI uri = stmt.getPredicate() ;
                Value v = stmt.getObject() ;
            }
            results.close() ;
        } catch (OpenRDFException ex)
        {
            ex.printStackTrace();
        } finally { }
    }

    private void doBooleanQuery(RepositoryConnection conn2, String queryString)
    {
       try {
           BooleanQuery booleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
           booleanQuery.evaluate() ;
       } catch (OpenRDFException ex)
       {
           ex.printStackTrace();
       } finally { }
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