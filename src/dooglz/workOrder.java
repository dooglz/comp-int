package dooglz;

import com.google.gson.Gson;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class workOrder {
    public long dispatchID;
    public GenAlgParams params;

    public static synchronized workOrder GetFromDispatch() throws IOException {
        workOrder wo = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet("http://localhost:8080/req");
            CloseableHttpResponse response1 = httpclient.execute(httpGet);
            try {
                System.out.println("Asked dispatch for job: " + response1.getStatusLine());
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
                    wo = gson.fromJson(st.get(0),workOrder.class);
                    EntityUtils.consume(entity1);
                }
            } finally {
                response1.close();
            }

        } finally {
            httpclient.close();
        }


        return wo;
    }
}
