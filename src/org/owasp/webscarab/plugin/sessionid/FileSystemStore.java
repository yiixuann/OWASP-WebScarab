/*
 * FileSystemStore.java
 *
 * Created on September 14, 2004, 4:09 PM
 */

package org.owasp.webscarab.plugin.sessionid;

import org.owasp.webscarab.model.StoreException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.logging.Logger;

/**
 *
 * @author  knoppix
 */
public class FileSystemStore implements SessionIDStore {
    
    private SortedMap _sessionIDs = new TreeMap();
    
    private File _dir;
    
    private Logger _logger = Logger.getLogger(getClass().getName());
    
    /** Creates a new instance of FileSystemStore */
    public FileSystemStore(File dir) throws StoreException {
        _dir = dir;
        File f = new File(_dir, "sessionids");
        if (f.exists()) {
            load();
        } else {
            create();
        }
    }
    
    private void create() throws StoreException {
        // we create our files when we save
    }
    
    private void load() throws StoreException {
        File f = new File(_dir, "sessionids");
        if (!f.isFile()) {
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            for (String key = br.readLine(); key != null; key = br.readLine()) {
                List list = new ArrayList();
                _sessionIDs.put(key, list);
                for (String line = br.readLine(); line != null && ! line.equals(""); line=br.readLine()) {
                    SessionID id = new SessionID(line);
                    list.add(id);
                }
            }
        } catch (IOException ioe) {
            throw new StoreException("Error reading sessionids: " + ioe);
        }
    }
    
    public int addSessionID(String key, SessionID id) {
        List list = (List) _sessionIDs.get(key);
        if (list == null) {
            list = new ArrayList();
            _sessionIDs.put(key, list);
        }
        int insert = Collections.binarySearch(list, id);
        if (insert<0) insert = -insert-1;
        list.add(insert, id);
        return insert;
    }
    
    public int getSessionIDNameCount() {
        return _sessionIDs.size();
    }
    
    public String getSessionIDName(int index) {
        Iterator it = _sessionIDs.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (index==0) {
                return key;
            } else {
                index--;
            }
        }
        return null;
    }
    
    public int getSessionIDCount(String key) {
        List list = (List) _sessionIDs.get(key);
        if (list == null) return 0;
        return list.size();
    }
    
    public SessionID getSessionIDAt(String key, int index) {
        List list = (List) _sessionIDs.get(key);
        if (list == null) return null;
        return (SessionID) list.get(index);
    }
    
    public void flush() throws StoreException {
        File f = new File(_dir, "sessionids");
        if (f.exists() && !f.isFile()) {
            throw new StoreException("Couldn't create output file " + f);
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            Iterator it = _sessionIDs.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                bw.write(key + "\r\n");
                List list = (List) _sessionIDs.get(key);
                Iterator it2 = list.iterator();
                while (it2.hasNext()) {
                    SessionID id = (SessionID) it2.next();
                    bw.write(id.toString() + "\r\n");
                }
                bw.write("\r\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new StoreException("IOException: " + ioe);
        }
    }
    
}