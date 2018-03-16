
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class Scraper
{

    public static void main(String[] args) throws IOException
    {
        ignoreTrust();


        Document doc = Jsoup.connect("https://www.finn.no/job/fulltime/search.html?occupation=0.23&rows=10000").get();
        System.out.println(doc.title());
        Elements addElements = doc.select("a[href*=/fulltime/ad.html]");
        ArrayList<Document> jobDocs = new ArrayList<>(addElements.size());
        int jobnum = 0;
        for (Element add : addElements)
        {
            System.out.println("Downloading: " + jobnum++ + "/" + addElements.size() + ".");
            System.out.println(add.attr("title") + " - " + add.absUrl("href"));
            Document jobAdd = Jsoup.connect(add.absUrl("href")).get();
            jobDocs.add(jobAdd);
            printJobDescription(jobAdd);
            //  if (jobnum==2) break; //for debugging!!
        }

    }

    private static void printJobDescription(Document jobAdd) throws IOException
    {
        PrintWriter writer
                = new PrintWriter(new BufferedWriter(new FileWriter("addtexts.txt",true)));
        Elements descriptionElements = jobAdd.select("div[class*=object-description]");
        try
        {
            for (Element descriptionElement : descriptionElements)
            {
                System.out.println("----------------------------------");
                String addText = Jsoup.parse(descriptionElement.toString()).text();
                writer.append(addText + "\n");
                System.out.println(addText);
            }
        }finally
        {
          writer.close();
        }
    }

    private static void ignoreTrust()
    {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager()
        {
            public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType)
            {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType)
            {
            }
        }};

        // Install the all-trusting trust manager
        try
        {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
