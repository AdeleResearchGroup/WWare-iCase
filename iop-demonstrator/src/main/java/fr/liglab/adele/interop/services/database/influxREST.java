package fr.liglab.adele.interop.services.database;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
@Component(immediate = true)
@Instantiate
public class influxREST extends DefaultController {
    //@Requires


    @Route(method = HttpMethod.GET, uri = "/icasa/database/{query}")
    public Result senQuery(){
        return ok();
    }

    private String getTest(){
        JSONArray simpleGet = new JSONArray();
        return "e";
    }
}

