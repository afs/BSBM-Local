/*
 * (c) Copyright 2009 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package openrdf;

import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.zip.GZIPInputStream ;

import atlas.lib.FileOps ;
import atlas.lib.StrUtils ;

public class test
{
    static { System.setProperty("log4j.configuration", "file:log4j.properties") ; }
    
    public static void main(String... argv) throws IOException
    {
        query(argv) ;
    }
    
    public static void query(String... argv) throws IOException
    {
        if ( argv.length == 0 )
        {
            // Default settings.
            String SET = "50k" ;
            String DIR = "Data/data-"+SET ;
            String repo = "sesame:Sesame-DB/Repo-"+SET ;
            
            String[] a =
              { "-runs", "2",
                "-w",  "0",
                "-idir", DIR,
                "-o", "Results/res-"+SET+".xml",
                repo } ;
            argv = a ;
        }
        
        System.out.println(StrUtils.join(" ", argv)) ;
        
        benchmark.testdriver.TestDriver.main(argv) ;
    }

    
    public static void load(String... argv) throws IOException
    {
        String repoName = "Sesame-DB/Repo-250k" ;
        String indexes = "spoc,posc,opsc" ;
        String filename = "Data/data-250k/data.nt.gz" ;
        
        FileOps.clearDirectory(repoName) ;
        
        InputStream input = new FileInputStream(filename) ;
        if ( filename.endsWith(".gz") )
            input = new GZIPInputStream(input) ;
        
        Repo repo = new Repo(repoName, indexes) ;
        
        System.out.println("Start load: "+filename) ;
        long start = System.currentTimeMillis() ; 
        repo.load(input) ;
        
        long finish = System.currentTimeMillis() ;
        
        double time = (finish-start)/1000.0 ; 
        System.out.printf("Load time = %.2fs\n", time) ;
        
        repo.close() ;
        finish = System.currentTimeMillis() ;
        time = (finish-start)/1000.0 ; 
        System.out.printf("After close = %.2fs\n", time) ;

    }
    
}

/*
 * (c) Copyright 2009 Talis Information Ltd.
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