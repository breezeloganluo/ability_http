package com.breezefw.ability.http;

import com.breeze.base.log.Level;
import com.breeze.base.log.Logger;
import com.breeze.support.tools.FileTools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HTTP
{
  private static HTTP inc = new HTTP();

  Logger log = Logger.getLogger("com.weiguang.ability.http.HTTP");

  public static HTTP getInc()
  {
    return inc;
  }

  public String sendHttpGet(String url, String param)
  {
    if (this.log.isLoggable(Level.FINE)) {
      this.log.fine("sendHttpGet(" + url + ',' + param + ')');
    }
    String result = "";
    try
    {
      URL url2 = new URL(url);
      InputStream in = url2.openStream();
      BufferedReader bin = new BufferedReader(new InputStreamReader(in, 
        "UTF-8"));
      String line = null;
      while ((line = bin.readLine()) != null) {
        result = result + line + "\r\n"; 
      }
      bin.close();
    } catch (Exception e) {
      this.log.severe("发送GET请求出现异常！", e);
    }

    return result;
  }

  public String sendHttpPost(String url, String param, Map<String, String> header)
  {
    if (this.log.isLoggable(Level.FINE)) {
      this.log.fine("sendHttpPost(" + url + ',' + param + ',' + 
        header + ')');
    }
    OutputStream out = null;
    BufferedReader in = null;
    String result = "";
    try {
      URL realUrl = new URL(url);

      URLConnection conn = realUrl.openConnection();

      conn.setRequestProperty("accept", "*/*");
      conn.setRequestProperty("connection", "Keep-Alive");
      conn.setRequestProperty("user-agent", 
        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

      if ((header != null) && 
        (header.size() > 0)) {
        for (String key : header.keySet()) {
          conn.setRequestProperty(key, (String)header.get(key));
        }

      }

      conn.setDoOutput(true);
      conn.setDoInput(true);

      out = conn.getOutputStream();

      out.write(param.getBytes("UTF-8"));

      out.flush();

      in = new BufferedReader(
        new InputStreamReader(conn.getInputStream(),"UTF-8"));
      String line;
      while ((line = in.readLine()) != null)
      {
        result = result + line +"\r\n";
      }
    } catch (Exception e) {
      this.log.severe("发送POST请求出现异常！", e);
      try
      {
        if (out != null) {
          out.close();
        }
        if (in != null)
          in.close();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    finally
    {
      try
      {
        if (out != null) {
          out.close();
        }
        if (in != null)
          in.close();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return result;
  }

  public String sendHttpPost(String url, String param) {
    return sendHttpPost(url, param, null);
  }

  public String sendHttpsPost(String Url, String data)
    throws Exception
  {
    URL url = new URL(Url);

    trustAllHttpsCertificates();
    HostnameVerifier hv = new HostnameVerifier() {
      public boolean verify(String urlHostName, SSLSession session) {
        return true;
      }

      public boolean verify(String arg0, String arg1)
      {
        return true;
      }
    };
    HttpsURLConnection.setDefaultHostnameVerifier(hv);
    HttpsURLConnection https_url_connection = (HttpsURLConnection)url
      .openConnection();

    https_url_connection.setRequestMethod("POST");
    https_url_connection.setDoOutput(true);

    OutputStream out = https_url_connection.getOutputStream();
    out.write(data.getBytes("UTF-8"));
    
    //临时代码，将发送日志打印出来
    this.log.severe("sent https:" + Url);
    this.log.severe(data);

    InputStream in = https_url_connection.getInputStream();
    String result = FileTools.readFile(in, "UTF-8");
    this.log.severe("result from http is:");
    this.log.severe(result);
    return result;
  }

  public void trustAllHttpsCertificates() throws Exception
  {
    TrustManager[] tm_array = new TrustManager[1];
    TrustManager tm = new myTrustManager();
    tm_array[0] = tm;
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, tm_array, null);
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
  }

  public static class myTrustManager implements TrustManager, X509TrustManager
  {
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    public boolean isServerTrusted(X509Certificate[] certs) {
      return true;
    }

    public boolean isClientTrusted(X509Certificate[] certs) {
      return true;
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType)
      throws CertificateException
    {
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType)
      throws CertificateException
    {
    }
  }
}