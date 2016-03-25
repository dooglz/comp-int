package dooglz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.org.glassfish.external.probe.provider.annotations.ProbeListener;
import com.sun.tools.internal.ws.wsdl.document.jaxws.Exception;
import modelP.JSSP;
import modelP.Problem;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ProblemRun {
    public long disaptchID;
    public long disaptchTime;
    public long returnTime;
    public GenAlgResult result;
    public GenAlgParams params;
}

class problemStat {
    public int id;
    public int lb;
    public int bestScore;
    public int bestGen;
    public int completeRuns;
    public int stalledRuns;
    public ArrayList<ProblemRun> runs;
}

class runData {
    public long lastUpdateTime;
    public long version;
    public problemStat[] problems;
}


public class DispatchServer {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8080;
    private static final int BACKLOG = 1;
    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int STATUS_OK = 200;
    private static final int STATUS_METHOD_NOT_ALLOWED = 405;
    private static final int NO_RESPONSE_LENGTH = -1;
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String ALLOWED_METHODS = METHOD_GET + "," + METHOD_OPTIONS;
    private HttpServer server;
    private runData rd;

    public void Start() {
        server.start();
    }

    public void Stop() {
        server.stop(0);
    }

    private synchronized GenAlgParams getNewRunFromProblemStat(problemStat ps) {
        GenAlgParams p = new GenAlgParams();
        p.goal = ps.lb;
        p.problemID = ps.id;
        if (ps.runs.size() == 0) {
            p.maxGen = 200;
            p.tournamentNewChilderenCount = 4;
            p.tournamentSampleSize = 32;
            p.crossovermode = 5;
            p.maxTime = 400000;
            p.popsize = 128;
            p.seedRange = 32;
            p.resetTrigger = 300;
            p.tournamentMutateRange = 128;
            return p;
        } else {
            p.maxGen = 200 + (ps.runs.size() * 50);
            p.tournamentNewChilderenCount = 4;
            p.tournamentSampleSize = 32;
            p.crossovermode = 5;
            p.maxTime = 400000;
            p.popsize = 128 + (ps.runs.size() * 50);
            p.seedRange = 32;
            p.resetTrigger = 300;
            p.tournamentMutateRange = 128;
            return p;
        }
    }

    private synchronized workOrder getNextJob() {
        workOrder wo = new workOrder();
        ProblemRun pr = new ProblemRun();
        wo.dispatchID = (long) (Math.random() * ((double) Long.MAX_VALUE));
        //search for any Problem stat with 0 runs.
        for (problemStat ps : rd.problems) {
            if (ps.runs.size() == 0) {
                wo.params = getNewRunFromProblemStat(ps);
                pr.params = wo.params;
                pr.disaptchID = wo.dispatchID;
                ps.runs.add(pr);
                return wo;
            }
        }
        wo.params = getNewRunFromProblemStat(rd.problems[0]);
        pr.params = wo.params;
        pr.disaptchID = wo.dispatchID;
        pr.disaptchTime = System.currentTimeMillis();
        rd.problems[0].runs.add(pr);
        //update file
        rd.lastUpdateTime = System.currentTimeMillis();
        rd.version++;
        saveTofile(rd);
        return wo;
    }

    private synchronized void parseResponce(workResponce wr) {
        //System.out.println("parsing new WR " + wr.dispatchID);
        for (problemStat ps : rd.problems) {
            for (ProblemRun pr : ps.runs) {
                if (pr.disaptchID == wr.dispatchID) {
                    pr.returnTime = System.currentTimeMillis();
                    pr.result = wr.result;
                    if (wr.result.result == "done") {
                        ps.completeRuns++;
                    } else {
                        ps.stalledRuns++;
                    }
                    ps.bestGen = Math.min(ps.bestGen, wr.result.generation);
                    ps.bestScore = Math.min(ps.bestScore, wr.result.bestScore);
                    System.out.println("WR " + wr.dispatchID + " returned, " + wr.result.result + " score: " + wr.result.bestScore + " gen:" + wr.result.generation);

                    //
                    //update file
                    rd.lastUpdateTime = System.currentTimeMillis();
                    rd.version++;
                    saveTofile(rd);
                    return;
                }
            }
        }
        System.out.println("Missing job returned, " + wr.dispatchID);

    }

    public DispatchServer() throws IOException {
        rd = loadFromFile();
        if (rd == null) {
            System.out.println("Sttarting new runData");
            rd = new runData();
            ArrayList<problemStat> psa = new ArrayList<>();
            for (int i = 0; i < 150; i++) {
                Problem p = JSSP.getProblem(i);
                if (p != null) {
                    problemStat ps = new problemStat();
                    ps.id = i;
                    ps.lb = p.getLowerBound();
                    ps.bestGen = -1;
                    ps.bestScore = -1;
                    ps.completeRuns = 0;
                    ps.stalledRuns = 0;
                    ps.runs = new ArrayList<>();
                    psa.add(ps);
                }
            }
            rd.problems = psa.toArray(new problemStat[psa.size()]);
            rd.version = 0;
            rd.lastUpdateTime = System.currentTimeMillis();
        } else {
            System.out.println("loaded Rd file: " + rd.version);
        }
        //update file
        rd.lastUpdateTime = System.currentTimeMillis();
        rd.version++;
        saveTofile(rd);

        server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
        server.createContext("/req", he -> {
            try {
                final Headers headers = he.getResponseHeaders();
                final String requestMethod = he.getRequestMethod().toUpperCase();
                switch (requestMethod) {
                    case METHOD_GET:
                        final Map<String, List<String>> requestParameters = getRequestParameters(he.getRequestURI());
                        // do something with the request parameters
                        //final String responseBody = "['hello world!',"+requestParameters+"]";

                        Gson gson = new Gson();
                        workOrder wo = getNextJob();
                        System.out.println("Dispatching Job: " + wo.dispatchID + " _ " + wo.params.problemID);
                        final String responseBody = gson.toJson(wo);
                        headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                        final byte[] rawResponseBody = responseBody.getBytes(CHARSET);
                        he.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
                        he.getResponseBody().write(rawResponseBody);
                        break;
                    case METHOD_OPTIONS:
                        headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                        he.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                        break;
                    default:
                        headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                        he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                        break;
                }
            } finally {
                he.close();
            }
        });
        server.createContext("/submit", he -> {
            boolean good = false;
            workResponce wr = null;
            try {
                final Headers headers = he.getResponseHeaders();
                final String requestMethod = he.getRequestMethod().toUpperCase();
                switch (requestMethod) {
                    case METHOD_POST:
                        final Map<String, String> postData = getPostData(he.getRequestBody());
                        System.out.println("data Received from: " + he.getRemoteAddress());

                        if (postData.get("data") != null) {
                            Gson gson = new Gson();
                            try {
                                wr = gson.fromJson(postData.get("data"), workResponce.class);
                                good = true;
                            } catch (JsonSyntaxException e) {
                                System.out.println(e.getMessage() + " gson parse error");
                                good = false;
                            }
                        }

                        if (good) {
                            final String response = "cheers bro";
                            final byte[] rawResponseBody = response.getBytes(CHARSET);
                            headers.set(HEADER_CONTENT_TYPE, String.format("text/html;  charset=%s", CHARSET));
                            he.sendResponseHeaders(200, rawResponseBody.length);
                            he.getResponseBody().write(rawResponseBody);
                        } else {
                            final String response = "I can't Parse this!";
                            final byte[] rawResponseBody = response.getBytes(CHARSET);
                            headers.set(HEADER_CONTENT_TYPE, String.format("text/html; charset=%s", CHARSET));
                            he.sendResponseHeaders(400, rawResponseBody.length);
                            he.getResponseBody().write(rawResponseBody);
                        }
                        break;
                    case METHOD_OPTIONS:
                        headers.set(HEADER_ALLOW, METHOD_POST + "," + METHOD_OPTIONS);
                        he.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                        break;
                    default:
                        headers.set(HEADER_ALLOW, METHOD_POST + "," + METHOD_OPTIONS);
                        he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                        break;
                }
            } finally {
                he.close();
                if (good) {
                    parseResponce(wr);
                }
            }

        });
    }

    private static runData loadFromFile() {
        try {
            File myFile = new File("myjsonstuff.json");
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = myReader.readLine()) != null) {
                sb.append(line);
            }
            myReader.close();
            String json = sb.toString();
            Gson gson = new Gson();
            return gson.fromJson(json, runData.class);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void saveTofile(runData d) {
        try {
            Writer writer = new FileWriter("myjsonstuff.json");
            Gson gson = new GsonBuilder().create();
            gson.toJson(d, writer);
            writer.close();
        } catch (IOException e) {
            System.out.print("save error: " + e.getMessage());
        }
    }

    private static Map<String, String> getPostData(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            //StringBuilder out = new StringBuilder();
            String line;
            ArrayList<String> st = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line != null && !line.isEmpty()) {
                    st.add(line);
                }
            }
            reader.close();
            Map<String, String> params = new LinkedHashMap<String, String>();
            for (int i = 0; i < st.size(); i++) {
                if (st.get(i).startsWith("Content-Disposition: form-data; name=")) {
                    params.putIfAbsent(st.get(i).substring(38, st.get(i).length() - 1), st.get(i + 1));
                }else if(st.get(i).split("=").length == 2){
                    String[] s = st.get(i).split("=");
                    //params.putIfAbsent(s[0].substring(1,s[0].length()-1), s[1].substring(1,s[1].length()-1));
                    params.putIfAbsent(s[0],s[1]);
                }
            }
            return params;
        } catch (IOException e) {

        }
        return new LinkedHashMap<String, String>();
    }


    private static Map<String, List<String>> getRequestParameters(final URI requestUri) {
        final Map<String, List<String>> requestParameters = new LinkedHashMap<>();
        final String requestQuery = requestUri.getRawQuery();
        if (requestQuery != null) {
            final String[] rawRequestParameters = requestQuery.split("[&;]", -1);
            for (final String rawRequestParameter : rawRequestParameters) {
                final String[] requestParameter = rawRequestParameter.split("=", 2);
                final String requestParameterName = decodeUrlComponent(requestParameter[0]);
                requestParameters.putIfAbsent(requestParameterName, new ArrayList<>());
                final String requestParameterValue = requestParameter.length > 1 ? decodeUrlComponent(requestParameter[1]) : null;
                requestParameters.get(requestParameterName).add(requestParameterValue);
            }
        }
        return requestParameters;
    }

    private static String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (final UnsupportedEncodingException ex) {
            throw new InternalError(ex);
        }
    }
}