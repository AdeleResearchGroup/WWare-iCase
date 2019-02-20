package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.interop.services.database.InfluxService;
import javafx.application.Application;
import org.apache.felix.ipojo.annotations.Requires;
import org.influxdb.dto.QueryResult;

import java.util.List;

@ContextEntity(coreServices = {LearnedHeaterBehavior.class,ServiceLayer.class})
public class LearnedHeaterBehaviorImpl implements LearnedHeaterBehavior, ServiceLayer{

    //SERVICE's STATES

    @ContextEntity.State.Field(service = LearnedHeaterBehavior.class, state = SERVICE_STATUS, value="0")
    private String srvState;

    @ContextEntity.State.Field(service = LearnedHeaterBehavior.class, state = SERVICE_CHANGE, value="0.0")
    private Double srvChange;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS, value = "0",directAccess = true)
    private int SrvQoS;

    //REQUIREMENTS
    @Requires(id="database", optional = false,specification = InfluxService.class)
    private InfluxService DB;

    //STATES CHANGE
    @ContextEntity.State.Push(service = LearnedHeaterBehavior.class, state = RoomThermometerService.SERVICE_STATUS)
    public Double pushService(Double serviceState){
        return serviceState;
    }

    @ContextEntity.State.Push(service = LearnedHeaterBehavior.class, state = RoomThermometerService.SERVICE_CHANGE)
    public Boolean pushChange(Boolean serviceChange){return serviceChange;}


    @Override
    public String getServiceStatus() {
        return srvState;
    }

    @Override
    public Double getHeaterPorcentage(Double ReferenceTemperature, String zone) {
        srvState="2.0";
        System.out.println("++++++");
        System.out.println(DB.manualQuery("SELECT MAX(value) FROM temperature WHERE type = 'ThermometerExt'","test").get(0).getSeries());
        System.out.println("++++++");
        System.out.println(DB.manualQuery("SELECT MAX(value) FROM temperature WHERE type = 'ThermometerExt'","test").get(0).getSeries().get(0).getValues().get(0));
        System.out.println("++++++");
        //List [time, temperature]  i.e: [2019-02-15T14:21:35.447Z, 284.97499999999997]
        List<Object> maxTemp = DB.manualQuery("SELECT MAX(value) FROM temperature WHERE type = 'ThermometerExt'","test").get(0).getSeries().get(0).getValues().get(0);
        List<Object>  minTemp = DB.manualQuery("SELECT MIN(value) FROM temperature WHERE type = 'ThermometerExt'","test").get(0).getSeries().get(0).getValues().get(0);

        Double delta = 0.0;
        Double DeltaIncrement = 0.1;
        int maxIterations=5;
        int iteration =0;
        System.err.printf("Reference\tMin\tMax\n");
        System.err.printf("%3.2f\t%3.2f\t%3.2f\n", ReferenceTemperature, minTemp.get(1), maxTemp.get(1));
        //ToDO make verification that at the time given from max and min Temp, the Heater did exist
        //checking if ReferenceTemperature is in the range of the saved temperatures...
        if(ReferenceTemperature>(Double)maxTemp.get(1)){
            //return heater % of max temp recorded
            System.out.println("ref to high");
            return (Double)DB.manualQuery(" SELECT value FROM powerLvl WHERE zone = '"+zone+"' AND type = 'Heater' AND time = '"+maxTemp.get(0)+"'","test").get(0).getSeries().get(0).getValues().get(0).get(1);

        }else if(ReferenceTemperature<(Double)minTemp.get(1)){
            //return heater% of min temp recorded
            System.out.println("ref to low");
            return (Double)DB.manualQuery(" SELECT value FROM powerLvl WHERE zone = '"+zone+"' AND type = 'Heater' AND time = '"+minTemp.get(0)+"'","test").get(0).getSeries().get(0).getValues().get(0).get(1);
        }else{
            while(iteration<maxIterations){
            List<QueryResult.Series> result = DB.manualQuery("SELECT LAST(value) FROM temperature WHERE type = 'ThermometerExt' AND value >= "+(ReferenceTemperature-delta)+" AND value <= "+(ReferenceTemperature+delta),"test").get(0).getSeries();
            if(result==null){
                delta+=DeltaIncrement;
                iteration+=1;
                System.out.println("expanding delta...+"+delta+".");
            }else{
                iteration=maxIterations;
                String time = (String) result.get(0).getValues().get(0).get(0);
                System.out.println("*****");
                System.out.println(time);
                System.out.println(result.get(0).getValues().get(0).get(1));
                System.out.println("------");
                return (Double)DB.manualQuery(" SELECT value FROM powerLvl WHERE zone = '"+zone+"' AND type = 'Heater' AND time = '"+time+"'","test").get(0).getSeries().get(0).getValues().get(0).get(1);
            }
            }

           String a= "SELECT LAST(value) FROM temperature WHERE type = 'ThermometerExt' AND value >= "+(ReferenceTemperature-delta)+" AND value <= "+(ReferenceTemperature+delta);
        }

        //"SELECT LAST(value) FROM temperature WHERE type = 'ThermometerExt' AND value >273.5 AND value<=273.7";
        //"SELECT value FROM temperature WHERE type = 'ThermometerExt' AND time = '2019-02-15T14:21:35.447Z'";
        System.out.println(DB.manualQuery("SELECT MAX(value) FROM temperature WHERE type = 'ThermometerExt'","test").get(0).getSeries().get(0).getValues().get(0));
        System.out.println(DB.manualQuery("SELECT MAX(value) FROM temperature WHERE type = 'ThermometerExt'","test").get(0).getSeries().get(0).getValues().get(0).get(1));
        System.out.println(DB.manualQuery("SELECT MIN(value) FROM temperature WHERE type = 'ThermometerExt'","test").get(0).getSeries().get(0).getValues().get(0));

        return null;
    }

    @Override
    public int getMinQos() {
        return 100;
    }

    @Override
    public int getServiceQoS() {
        return SrvQoS;
    }

    @Override
    public String getServiceName() {
        return name;
    }

}
