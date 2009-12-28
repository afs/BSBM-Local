/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import atlas.lib.StrUtils ;

import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.solver.Explain ;

public class RunBSBM
{
    static public void main(String ... args)
    {
        TDB.init();
        //Log.enable("com.hp.hpl.jena.tdb.exec") ;
        TDB.setExecutionLogging(Explain.InfoLevel.ALL) ;
        
        if ( args.length == 0 )
        {
            // Default settings.
            String SET = "5m" ;
            String DIR = "Data/data-"+SET ;
            String[] a =
              { "-runs", "2",
                "-w",  "0",
                "-idir", DIR,
                "-o", "Results/res-"+SET+".xml",
                "sesame:Sesame-DB/Repo-50k" } ;
                //"local:assembler.ttl"} ;
                //"local:"DIR+"/assembler.ttl" } ;
            args = a ;
        }
        
        System.out.println(StrUtils.join(" ", args)) ;
        
        benchmark.testdriver.TestDriver.main(args) ;
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