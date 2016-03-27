package dooglz;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static java.lang.Thread.currentThread;

public class workOrder {
    public long dispatchID;
    public GenAlgParams params;

    public static synchronized workOrder GetFromDispatch() {
        workOrder wo = null;
        boolean done = false;
        while (!done) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpGet httpGet = new HttpGet(Main.ip + "/req");
                CloseableHttpResponse response1 = httpclient.execute(httpGet);
                done = true;
                try {
                    System.out.println(currentThread().getId() + " Asked dispatch for job: " + response1.getStatusLine());
                    if (response1.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity1 = response1.getEntity();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(entity1.getContent()));
                        String line;
                        ArrayList<String> st = new ArrayList<>();
                        while ((line = reader.readLine()) != null) {
                            if (line != null && !line.isEmpty()) {
                                st.add(line);
                            }
                        }
                        reader.close();
                        Gson gson = new Gson();
                        wo = gson.fromJson(st.get(0), workOrder.class);
                        EntityUtils.consume(entity1);
                    }
                } finally {
                    response1.close();
                }
            } catch (IOException e) {
                System.out.println(currentThread().getId() + " Error requesting from dispatch, " + e.getMessage());
                try {
                    Thread.sleep(1000 + (int) (Math.random() * 2000));
                } catch (InterruptedException ie) {
                }
            } finally {
                try {
                    httpclient.close();
                } catch (IOException ioe) {
                }
            }
        }
        return wo;
    }
}
