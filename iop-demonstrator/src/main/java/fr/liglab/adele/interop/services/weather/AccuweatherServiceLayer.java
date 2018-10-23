package fr.liglab.adele.interop.services.weather;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;




@ContextEntity(coreServices = {AccuweatherService.class, ServiceLayer.class})

public class AccuweatherServiceLayer implements AccuweatherService, ServiceLayer {

    private int weatherValue = 0;
    private double curCityTemperature=0;
    private String curWeatherTxt = "";

    
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS, directAccess = true, value="0")
    public int AppQos;

    @ContextEntity.State.Field(service=ServiceLayer.class,state = ServiceLayer.NAME,directAccess = true)
    public String name;

    public String[] getHTML(String cityId, String apiKey){
        String url ="[{\"LocalObservationDateTime\":\"2018-02-22T16:45:00+01:00\",\"EpochTime\":1519314300,\"WeatherText\":\"Cloudy\",\"WeatherIcon\":7,\"IsDayTime\":true,\"Temperature\":{\"Metric\":{\"Value\":4.0,\"Unit\":\"C\",\"UnitType\":17},\"Imperial\":{\"Value\":39.0,\"Unit\":\"F\",\"UnitType\":18}},\"MobileLink\":\"http://m.accuweather.com/en/fr/grenoble/136555/current-weather/136555?lang=en-us\",\"Link\":\"http://www.accuweather.com/en/fr/grenoble/136555/current-weather/136555?lang=en-us\"}]";
        url=url.replaceAll("[\\[\\](){}]","").replaceAll("\"","");
        String[] temp=url.split(",");

        setCurWeatherTxt(temp[2].split(":")[1]);
        setWeatherValue(Integer.parseInt(temp[3].split(":")[1]));
        setCurCityTemperature(Double.parseDouble(temp[5].split(":")[3]));
        System.out.println(temp[0].split(":"));
        //temp[0]= dummyResponse;
        return temp;
    }



    public static String getHTML1(String cityId, String apiKey) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL("http://dataservice.accuweather.com/currentconditions/v1/"+cityId+"?apikey="+apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        String[] parts = result.toString().split(":");
        return result.toString();
    }

    @Override
    public int getWeatherValue() {
        return weatherValue;
    }

    public void setWeatherValue(int weatherValue) {
        this.weatherValue = weatherValue;
    }

    public double getCurCityTemperature() {
        return curCityTemperature;
    }

    public void setCurCityTemperature(double curCityTemperature) {
        this.curCityTemperature = curCityTemperature;
    }

    public String getCurWeatherTxt() {
        return curWeatherTxt;
    }

    public void setCurWeatherTxt(String curWeatherTxt) {
        this.curWeatherTxt = curWeatherTxt;
    }

    @Override
    public int getMinQos() {
        return 80;
    }

    @Override
    public String getServiceName() {
        return name;
    }

    @Override
    public int getServiceQoS() {
        return 0;
    }
}
