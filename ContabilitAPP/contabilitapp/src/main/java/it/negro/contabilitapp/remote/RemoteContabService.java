package it.negro.contabilitapp.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import it.negro.contabilitapp.entity.MovimentoContabile;
import it.negro.contabilitapp.json.HierarchicalJsonDeserializer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class RemoteContabService {

    private static final String ROOT_URL = "http://192.168.0.4/contab-war/disp/rest/";

    private Gson gson;
    private HierarchicalJsonDeserializer jsonDeserializer;

    public RemoteContabService (){
        this.jsonDeserializer = new HierarchicalJsonDeserializer();
        this.gson = new GsonBuilder()
                .serializeNulls()
                .setDateFormat("dd/MM/yyyy")
                .registerTypeHierarchyAdapter(Object.class, this.jsonDeserializer)
                .create();
    }

    public List<MovimentoContabile> getMovimenti(Integer pageN, Integer elemsN, Date al){
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String a = format.format(al);
        String url = "getPaged/" + String.valueOf(pageN) + "/" + String.valueOf(elemsN) + "/" + a;
        return (List<MovimentoContabile>)get(url, MovimentoContabile.class, true);
    }

    public MovimentoContabile getMovimento(Integer id){
        return (MovimentoContabile) get("getMovimento/" + String.valueOf(id), MovimentoContabile.class, false);
    }

    private Object get(String url, Class<?> clazz, boolean array){
        Object result = null;
        try {
            HttpResponse response = get(url);
            String jsonResult = toJson(response);
            if (array)
                result = fromJsonArray(jsonResult, clazz);
            else
                result = fromJson(jsonResult, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private HttpResponse get(String url) throws IOException{
        HttpClient client = new DefaultHttpClient();
        HttpContext context = new BasicHttpContext();
        HttpGet get = new HttpGet(ROOT_URL + url);
        HttpResponse response = client.execute(get, context);
        return response;
    }

    private Object fromJson (String json, Class<?> clazz){
        return this.gson.fromJson(json, clazz);
    }

    private Object fromJsonArray (String json, Class<?> componentClass){
        return jsonDeserializer.deserializeToList((JsonArray) new JsonParser().parse(json), componentClass);
    }

    private String toJson(HttpResponse response)throws IOException{
        HttpEntity entity = response.getEntity();
        InputStream in = entity.getContent();
        StringBuffer out = new StringBuffer();
        int n = 1;
        while (n > 0) {
            byte[] b = new byte[4096];
            n = in.read(b);
            if (n > 0)
                out.append(new String(b, 0, n));
        }
        
        return out.toString();
    }

}
