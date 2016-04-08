package dooglz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import modelP.JSSP;
import modelP.Problem;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
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

class Sentinel extends Thread {
    private DispatchServer ds;

    public Sentinel(DispatchServer ds) {
        this.ds = ds;
    }


    public String Status(boolean print) {
        String ss = "";
        synchronized (ds.rundDataLock) {
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            ss+="##" + timeStamp + " -- " + ds.rd.version;
            if(print){System.out.println("##" + timeStamp + " -- " + ds.rd.version);}
            int inflight = 0;
            int finished = 0;
            for (int i = 0; i < ds.rd.problems.length; i++) {
                problemStat ps = ds.rd.problems[i];
                ss+= "<br>Problem: " + ps.id + "\t LB:" + ps.lb + "\t Best Score:" + ps.bestScore + "\t Best Gen:" + ps.bestGen + "\t CR:" + ps.completeRuns + "\t SR:" + ps.stalledRuns + "\t rs:" + ps.runs.size();
                if(print){System.out.println("Problem: " + ps.id + "\t LB:" + ps.lb + "\t Best Score:" + ps.bestScore + "\t Best Gen:" + ps.bestGen + "\t CR:" + ps.completeRuns + "\t SR:" + ps.stalledRuns + "\t rs:" + ps.runs.size());}
                for (int j = 0; j < ps.runs.size(); j++) {
                    ProblemRun pr = ps.runs.get(j);
                    if (pr.result == null) {
                        inflight++;
                    } else {
                        finished++;
                    }
                }
            }
            ss+="<br>Jobs in-flight: " + inflight + "\t Completed jobs: " + finished;
            if(print){System.out.println("Jobs in-flight: " + inflight + "\t Completed jobs: " + finished);}
        }
        return ss;
    }

    public void go() {
        synchronized (ds.rundDataLock) {
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            System.out.println("##" + timeStamp + " -- " + ds.rd.version);
            int inflight = 0;
            int finished = 0;
            for (int i = 0; i < ds.rd.problems.length; i++) {
                problemStat ps = ds.rd.problems[i];
                if (ps.bestScore < 0) {
                    ps.bestScore = Integer.MAX_VALUE;
                }
                if (ps.bestGen < 0) {
                    ps.bestGen = Integer.MAX_VALUE;
                }
                //look for missplaced prs.
                for (int j = 0; j < ps.runs.size(); j++) {
                    ProblemRun pr = ps.runs.get(j);
                    if (pr.params.problemID != ps.id) {
                        System.out.println("PR " + pr.params.problemID + " " + pr.disaptchID + " saved to wrong PS " + ps.id);
                        for (int k = 0; k < ds.rd.problems.length; k++) {
                            problemStat ps2 = ds.rd.problems[k];
                            if (pr.params.problemID == ps2.id) {
                                System.out.println("...Moved to PS " + ps2.id);
                                ps2.runs.add(pr);
                                ps.runs.remove(pr);
                                j--;
                                break;
                            }
                        }
                    }
                }
                for (int j = 0; j < ps.runs.size(); j++) {
                    ProblemRun pr = ps.runs.get(j);
                    if (pr.result == null) {
                        inflight++;
                    } else {
                        finished++;
                    }
                    if (pr.disaptchTime == 0) {
                        pr.disaptchTime = System.currentTimeMillis() - 100;
                    }
                   /* if (pr.returnTime == 0 && System.currentTimeMillis() - pr.disaptchTime > 5200000) { //25 mins
                        System.out.println("Job " + pr.disaptchID + " Pid:"+ps.id+" Took too long to return, resettting");
                        ps.runs.remove(pr);
                        j--;
                    }*/
                }
            }
            System.out.println("Jobs in-flight: " + inflight + ", completed jobs: " + finished);
            ds.rd.lastUpdateTime = System.currentTimeMillis();
            ds.rd.version++;
            ds.saveTofile(ds.rd);
        }
    }

    public void Purge() {
        synchronized (ds.rundDataLock) {
            System.out.println(" Purging");
            int p = 0;
            for (int i = 0; i < ds.rd.problems.length; i++) {
                problemStat ps = ds.rd.problems[i];
                for (int j = 0; j < ps.runs.size(); j++) {
                    ProblemRun pr = ps.runs.get(j);
                    if (pr.result == null) {
                        ps.runs.remove(pr);
                        p++;
                        j--;
                    }
                }
            }
            ds.rd.lastUpdateTime = System.currentTimeMillis();
            ds.rd.version++;
            ds.saveTofile(ds.rd);
            System.out.println(" Purged:" + p);
        }
    }

    private boolean run;

    @Override
    public void run() {
        run = true;
        go();
        while (run) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                Thread.sleep(180000);//3 mins
            } catch (InterruptedException e) {
            }
            go();
        }
    }

    public void Stop() {
        run = false;
    }
}


public class DispatchServer {


    public GenAlgParams getBestforPid(int pid){
        synchronized (rundDataLock) {
            for (int kk = 0; kk < rd.problems.length; kk++) {
                if(rd.problems[kk].id == pid){
                    problemStat ps = rd.problems[kk];
                    ProblemRun best = ps.runs.get(0);
                    for (int i = 0; i < ps.runs.size(); i++) {
                        ProblemRun prb = ps.runs.get(i);
                        if(prb.returnTime == 0 || prb.params.problemID != pid){
                            continue;
                        }
                        if (prb.result.bestScore < best.result.bestScore ||
                                (prb.result.bestScore == best.result.bestScore
                                        && prb.result.generation < best.result.generation)) {
                            best = prb;
                        }
                    }
                    System.out.println("The best params for PID: "+pid+" is:" + best.disaptchID + " -- " + best.result.result);
                    System.out.println("Mg: "+best.params.maxGen+" Ps: "+best.params.popsize+" TSS: "+best.params.tournamentSampleSize+ " SR: "+best.params.seedRange);
                    System.out.println("It achieved "+best.result.bestScore+" at gen "+best.result.generation + " In: "+ util.msToString(best.result.runtime));
                    return best.params;
                }
            }
        }
        System.out.println("Cuoldn't find best params for pid: "+pid);
        return null;
    }


    public static final Object rundDataLock = new Object();
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
    public runData rd;
    private Sentinel sn;

    public void HandleCmd(String cmd) {
        switch (cmd) {
            case "status":
                sn.Status(true);
                break;
            case "clean":
                sn.go();
                break;
            case "purge":
                sn.Purge();
                break;
        }
    }

    public DispatchServer() throws IOException {
        sn = null;
        synchronized (rundDataLock) {
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
                        ps.bestGen = Integer.MAX_VALUE;
                        ps.bestScore = Integer.MAX_VALUE;
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
        }
        server = HttpServer.create(new InetSocketAddress(Main.ip, PORT), BACKLOG);
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
                        System.out.println("Dispatching Job:\t" + wo.dispatchID + "\t " + wo.params.problemID + "\t "+ wo.params.popsize + "\t " + he.getRemoteAddress());
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
        server.createContext("/", he -> {
            try {
                final Headers headers = he.getResponseHeaders();
                final String requestMethod = he.getRequestMethod().toUpperCase();
                switch (requestMethod) {
                    case METHOD_GET:

                        final String responseBody = sn.Status(false);
                        headers.set(HEADER_CONTENT_TYPE, String.format("text/html; charset=%s", CHARSET));
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
                        System.out.println("Data Received from:\t" + he.getRemoteAddress());

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

    public static void saveTofile(runData d) {
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
                } else if (st.get(i).split("=").length == 2) {
                    String[] s = st.get(i).split("=");
                    //params.putIfAbsent(s[0].substring(1,s[0].length()-1), s[1].substring(1,s[1].length()-1));
                    params.putIfAbsent(s[0], s[1]);
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

    public void Start() {
        sn = new Sentinel(this);
        sn.start();
        server.start();
    }

    public void Stop() {
        server.stop(0);
        sn.Stop();
    }

    private synchronized GenAlgParams getNewRunFromProblemStat(problemStat ps) {
        synchronized (rundDataLock) {
            GenAlgParams p = new GenAlgParams();
            p.goal = ps.lb;
            p.problemID = ps.id;
            if (ps.runs.size() == 0) {
                p.maxGen = 100;
                p.tournamentNewChilderenCount = 4;
                p.tournamentSampleSize = 32;
                p.crossovermode = 5;
                p.maxTime = 200000; // 2 mins for first run
                p.popsize = 128;
                p.seedRange = 128;
                p.resetTrigger = 300;
                p.tournamentMutateRange = 128;
                return p;
            } else if (ps.runs.get(0).returnTime != 0 && (ps.completeRuns > 0 || ps.stalledRuns > 20)) {
                //Local searchtime
                //find the bestrun
                ProblemRun best = ps.runs.get(0);
                for (int i = 0; i < ps.runs.size(); i++) {
                    ProblemRun prb = ps.runs.get(i);
                    if(prb.returnTime == 0){
                        continue;
                    }
                    if (prb.result.bestScore < best.result.bestScore ||
                            (prb.result.bestScore == best.result.bestScore
                                    && prb.result.generation < best.result.generation)) {
                        best = prb;
                    }
                }
                //Mutate best
                p.maxGen = best.params.maxGen + (int) (Math.random() * 20) - 10;
                p.tournamentNewChilderenCount = best.params.tournamentNewChilderenCount + (int) (Math.random() * 4) - 2;
                p.tournamentSampleSize = best.params.tournamentSampleSize + (int) (Math.random() * 8) - 6;
                p.crossovermode = best.params.crossovermode + (int) (Math.random() * 4) - 2;
                p.maxTime = best.params.maxTime;
                p.popsize = best.params.popsize + (int) (Math.random() * 40) - 20;
                p.seedRange = best.params.seedRange + (int) (Math.random() * 40) - 20;
                p.resetTrigger = best.params.resetTrigger + (int) (Math.random() * 40) - 20;
                p.tournamentMutateRange = best.params.tournamentMutateRange + (int) (Math.random() * 40) - 20;
                return p;
            } else {
                p.maxGen = 100 + (ps.runs.size() * 50);
                p.tournamentNewChilderenCount = 4;
                p.tournamentSampleSize = 32;
                p.crossovermode = 5;
                p.maxTime = 1200000;
                p.popsize = 128 + (ps.runs.size() * 50);
                p.seedRange = 128;
                p.resetTrigger = 300;
                p.tournamentMutateRange = 128;
                return p;
            }
        }
    }

    private synchronized workOrder getNextJob() {
        synchronized (rundDataLock) {
            workOrder wo = new workOrder();
            ProblemRun pr = new ProblemRun();
            wo.dispatchID = (long) (Math.random() * ((double) Long.MAX_VALUE));
            problemStat ps;
            if (Math.random() < 0.1) {//small chance of just picking *any* job
                ps = rd.problems[(int) Math.floor(Math.random() * ((double) rd.problems.length))];
            } else {
                ps = rd.problems[0];
                for (problemStat pst : rd.problems) {
                    if (pst.runs.size() < ps.runs.size()) {
                        ps = pst;
                    }
                }
            }
            wo.params = getNewRunFromProblemStat(ps);
            pr.params = wo.params;
            pr.disaptchID = wo.dispatchID;
            pr.disaptchTime = System.currentTimeMillis();
            pr.result = null;
            ps.runs.add(pr);
            //update file
            rd.lastUpdateTime = System.currentTimeMillis();
            rd.version++;
            saveTofile(rd);
            return wo;
        }
    }

    private synchronized void parseResponce(workResponce wr) {
        synchronized (rundDataLock) {
            //System.out.println("parsing new WR " + wr.dispatchID);
            for (problemStat ps : rd.problems) {
                for (ProblemRun pr : ps.runs) {
                    if (pr.returnTime == 0 && pr.disaptchID == wr.dispatchID) {
                        pr.returnTime = System.currentTimeMillis();
                        pr.result = wr.result;
                        if (wr.result.result.equals("done")) {
                            ps.completeRuns++;
                        } else {
                            ps.stalledRuns++;
                        }
                        if(wr.result.bestScore < ps.bestScore){
                            ps.bestScore = wr.result.bestScore;
                            ps.bestGen = wr.result.generation;
                            System.out.println("\n-- New Best for Pid:"+ps.id+"\t Score:"+ps.bestScore+" (lb:"+ps.lb+")\t Gen:"+ps.bestGen);
                        }
                        System.out.println("WR " + wr.dispatchID + " returned, #" + wr.result.result + "# score: " + wr.result.bestScore + " (" + ps.lb + ") gen:" + wr.result.generation + " (" + ps.bestGen + ") Time:" + (pr.returnTime - pr.disaptchTime) + " pid:" + pr.params.problemID + " " + pr.params.popsize);

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
    }
}