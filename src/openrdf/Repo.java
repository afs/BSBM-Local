/*
 * (c) Copyright 2009 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package openrdf;

import java.io.File ;
import java.io.IOException ;
import java.io.InputStream ;

import org.openrdf.repository.RepositoryConnection ;
import org.openrdf.repository.RepositoryException ;
import org.openrdf.repository.sail.SailRepository ;
import org.openrdf.rio.RDFFormat ;
import org.openrdf.rio.RDFParseException ;
import org.openrdf.sail.nativerdf.NativeStore ;

public class Repo
{
 
    private RepositoryConnection conn = null ;
    private SailRepository repo ;

    public Repo(String directory, String indexes)
    {
        // Indexes.
        try {
            File d = new File(directory) ;
            repo = new SailRepository( new NativeStore(d,indexes) );
            repo.initialize();
            conn = repo.getConnection();
            
        } catch (Exception ex)
        {
            System.err.println("Failed to open Sesame repo: "+directory) ;
            System.exit(1) ;
        }  
    }

    
    public void load(InputStream input)
    {
        try {
            long start ;
            long finish ;
            
            // Transaction?
            
            //try {
            start = System.currentTimeMillis() ;
            conn.add(input, "http://base/", RDFFormat.NTRIPLES) ;
            finish = System.currentTimeMillis() ;
            //} finally { }

            conn.close() ;
                

        } catch (RepositoryException ex)
        {
            ex.printStackTrace();
        } catch (RDFParseException ex)
        {
            ex.printStackTrace();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }   
    }


    public void close()
    {
        try {
            conn.close() ;
            repo.shutDown() ;
        } catch (RepositoryException ex)
        {
            ex.printStackTrace();
        }
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