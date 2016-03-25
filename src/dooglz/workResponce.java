package dooglz;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

public class workResponce {
    public long dispatchID;
    public GenAlgResult result;

    public synchronized void sendToServer()throws IOException{
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(Main.ip+"/submit");
            Gson g = new Gson();
            StringEntity se = new StringEntity("data="+g.toJson(this));
            httpPost.setEntity(se);
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                System.out.println("Server's Responce: "+response2.getStatusLine());
                HttpEntity entity2 = response2.getEntity();

                EntityUtils.consume(entity2);
            } finally {
                response2.close();
            }
        } finally {
            httpclient.close();
        }
    }
}
