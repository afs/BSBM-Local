/*
 * (c) Copyright 2009 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package openrdf;

public class LoadData
{
    // Split into create repo, load repo.
    // Don't class all in load.
    public static void main(String... argv)
    {
        if ( argv.length != 1 )
        {
            System.err.println("Usage: "+Repo.class.getSimpleName()+" repoDir < datafile.nt") ;
            System.exit(1) ;
        }
        String repoName = argv[0] ;
        String indexes = "spoc,posc,opsc" ;
        Repo repo = new Repo(repoName, indexes) ;
        
        long start = System.currentTimeMillis() ; 
        repo.load(System.in) ;
        
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