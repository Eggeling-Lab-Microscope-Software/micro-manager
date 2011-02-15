package org.micromanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import mmcorej.CMMCore;
import org.micromanager.utils.HttpUtils;
import org.micromanager.utils.ReportingUtils;
import sun.misc.UUEncoder;

class ProblemReportSender extends Thread {

    private String status_;
    private String currentCfgPath_;
    private CMMCore core_;
    String prepreamble_;

    public ProblemReportSender(String prepreamble, CMMCore c, String config) {
        super("sender");
        status_ = "";
        currentCfgPath_ = config;
        core_ = c;
        prepreamble_ = prepreamble;
    }

    public String Status() {
        return status_;
    }

    public void run() {
        status_ = "";
        String cfgFile = currentCfgPath_;
        // is there a public way to get these keys??
        //mainPrefs_.get("sysconfig_file", cfgFile);
        String preamble = prepreamble_;
        if(0< preamble.length())
            preamble += "\n";
        preamble += "#";
        try {
            preamble += "Host: " + InetAddress.getLocalHost().getHostName() + " ";
        } catch (IOException e) {
        }
        preamble += ("User: " + core_.getUserId() + " configuration file: " + cfgFile + "\n");
        try {
            Reader in = new BufferedReader(new FileReader(cfgFile));
            StringBuilder sb = new StringBuilder();
            char[] tmpBuffer = new char[8192];
            int length;

            while ((length = in.read(tmpBuffer)) > 0) {
                sb.append(tmpBuffer, 0, length);
            }
            preamble += sb.toString();
            preamble += "\n";
        } catch (IOException e) {
        }
        String archPath = core_.saveLogArchiveWithPreamble(preamble, preamble.length());
        //String archPath = core_.saveLogArchive();
        try {
            HttpUtils httpu = new HttpUtils();
            List<File> list = new ArrayList<File>();
            File archiveFile = new File(archPath);
            // contruct a filename for the archive which is extremely
            // likely to be unique as follows:
            // yyyyMMddHHmmss + timezone + ip address
            String qualifiedArchiveFileName = "";
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                qualifiedArchiveFileName += df.format(new Date());
                String shortTZName = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
                qualifiedArchiveFileName += shortTZName;
                qualifiedArchiveFileName += "_";
                try {
                    qualifiedArchiveFileName += InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e2) {
                }
            } catch (Throwable t) {
            }
            // try ensure valid and convenient UNIX file name
            qualifiedArchiveFileName.replace(' ', '_');
            qualifiedArchiveFileName.replace('*', '_');
            qualifiedArchiveFileName.replace('|', '_');
            qualifiedArchiveFileName.replace('>', '_');
            qualifiedArchiveFileName.replace('<', '_');
            qualifiedArchiveFileName.replace('(', '_');
            qualifiedArchiveFileName.replace(')', '_');
            qualifiedArchiveFileName.replace(':', '_');
            qualifiedArchiveFileName.replace(';', '_');                //File fileToSend = new File(qualifiedArchiveFileName);
            qualifiedArchiveFileName += ".log";
            UUEncoder uuec = new UUEncoder();
            InputStream reader = new FileInputStream(archiveFile);
            OutputStream writer = new FileOutputStream(qualifiedArchiveFileName);
            uuec.encodeBuffer(reader, writer);
            reader.close();
            writer.close();
            File fileToSend = new File(qualifiedArchiveFileName);
            try {
                URL url = new URL("http://valelab.ucsf.edu/~MM/upload_corelog.php");
                List flist = new ArrayList<File>();
                flist.add(fileToSend);
                // for each of a colleciton of files to send...
                for (Object o0 : flist) {
                    File f0 = (File) o0;
                    try {
                        httpu.upload(url, f0);
                    } catch (java.net.UnknownHostException e2) {
                        status_ = e2.toString();//, " log archive upload");
                    } catch (IOException e2) {
                        status_ = e2.toString();
                    } catch (SecurityException e2) {
                        status_ = e2.toString();
                    } catch (Exception e2) {
                        status_ = e2.toString();
                    }
                }
            } catch (MalformedURLException e2) {
                status_ = e2.toString();
            }
            if( !fileToSend.delete())
                ReportingUtils.logMessage("Couldn't delete temporary file " + qualifiedArchiveFileName );
            if(!archiveFile.delete())
                ReportingUtils.logMessage("Couldn't delete archive file " + archPath);
        } catch (IOException e2) {
            status_ = e2.toString();
        }
    }
    public String Send(){
       start();
       try {
            join();
       } catch (InterruptedException ex) {
           status_ = ex.toString();

       }
       return Status();
    }
}


