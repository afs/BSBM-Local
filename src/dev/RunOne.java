/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformOrderByDistinctApplication ;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp ;
import com.hp.hpl.jena.tdb.TDBFactory ;

public class RunOne
{

    public static void main(String[] args) {
        // Query 5  7.2 [139.66ms]
//        LogCtl.enable(ARQ.getExecLogger());
//        ARQ.setExecutionLogging(InfoLevel.ALL);
        
        String DIR= "TDB-DB/DB-25m/Store" ;
        String queryFile = "Q5.rq" ;

        if ( false ) {
            Query query = QueryFactory.read("Q.rq") ;
            // Why not TopN? (slice _ 5 (order (distinct () ))) is safe? Distinct is done by TopN?
            Op op = Algebra.compile(query) ;
            
            op = Transformer.transform(new TransformOrderByDistinctApplication(), op) ;
            
            Op op1 = Algebra.optimize(op) ;
            System.out.println(query) ;
            WriterOp.output(System.out, op, query.getPrefixMapping());
            System.out.println() ;
            WriterOp.output(System.out, op1, query.getPrefixMapping());
            System.exit(0) ;
        }
        
        Query query = QueryFactory.read(queryFile) ;
        if ( false ) System.out.println(query) ;
        
        Dataset ds = TDBFactory.createDataset(DIR) ;
        int N = 1 ;
        int rows = -1 ;
        for ( int i = 0 ; i < 500 ; i++ ) {
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            int rows2 = ResultSetFormatter.consume(qExec.execSelect()) ;
            if ( rows >= 0 && rows2 != rows )
                System.err.println("Mismatch") ;
            qExec.close(); 
        }
        System.out.println("DONE") ;
    }
}
