package fr.liglab.adele.interop.demonstrator.home.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.services.temperature.*;
import org.apache.felix.ipojo.annotations.*;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ContextEntity(coreServices = {ApplicationLayer.class})
@Provides(specifications = RoomTemperatureControlApp.class)

public class RoomTemperatureControlApp implements ApplicationLayer{

    double OutsideTemperature = 0d;

    //SERVICE's STATES

    //IMPLEMENTATION's FUNCTIONS
    public RoomTemperatureControlApp(){

    }

    //REQUIREMENTS
    @Requires(id="heaterSrv",specification = HeatersService.class,optional = true)
    @ContextRequirement(spec = {ZoneService.class})
    private List<HeatersService> heaterSrvices;

    @Requires(id="extThermometer",specification = ExternalThermometerService.class,optional = true)
    private ExternalThermometerService externalThermometerService;

    @Requires(id="balconyThermometerService",specification = BalconyThermometerService.class,optional = true)
    private BalconyThermometerService balconyThermometerService;


    //CREATORS
    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;
    private @Creator.Field Creator.Entity<ExternalThermometerServiceImpl> externalThermometerServiceCreator;

    //@Creator.Field Creator.Entity<ExternalTemperatureSimulator> extTemperatureSim;
    private @Creator.Field Creator.Entity<HeatersServiceImpl> roomHeaterServices;

    private @Creator.Field Creator.Entity<BalconyThermometerServiceImpl> balconyThermometers;



    //ACTIONS
    @Validate
    public void start(){
        System.out.println("APP STARTED ROOM TEMPERATURE CONTROL");
        externalThermometerServiceCreator.create("extThermIOPServ");
        balconyThermometers.create("balconyThermometers");
        //extTempSimulatorHelper.create("extTemp");
        //extTemperatureSim.create("ExtTempSim");

    }
    @Bind(id="extThermometer")
    public void bindservice(ExternalThermometerService ther){
        System.out.println("APP(heat) External Thermometer initilized");

       // ther.setConnection(new String[] {Thermometer.class.getCanonicalName()});
    }
    @Modified(id="extThermometer")
    public void modifiedservice(ExternalThermometerService ther){
        System.out.println("APP(heat) External Thermometer Mod");
        //ther.getCurrentTemperature(new String[] {Thermometer.class.getCanonicalName()});
    }

    @Modified(id="balconyThermometerService")
    public void modifiedBalconyTher(BalconyThermometerService balThr){
       // balconyThermometers.getInstance("balconyThermometers").getExternalZoneSensor();
    }

    @Bind(id="balconyThermometerService")
    public void bindbalconyTh(BalconyThermometerService balThr){
        System.out.println("balcony SRV binded...");
        // externalThermometerService.setConnection(new String[] {Thermometer.class.getCanonicalName()});
    }

    @Unbind(id="balconyThermometerService")
    public void unbindbalconyTh(BalconyThermometerService balThr){
        System.out.println("local Thermometer not available... requesting to Base...");
       // externalThermometerService.setConnection(new String[] {Thermometer.class.getCanonicalName()});
    }

    @Bind(id="zones", specification = Zone.class, aggregate = true, optional = true)
    public void bindZone(Zone zone){

        System.out.println("APP(heat) Zone to create heaters...");

       // externalThermometerService.getCurrentTemperature(new String[] {Thermometer.class.getCanonicalName()});
        String instance = zone.getZoneName()+".heaters";

        Map<String,Object> properties = new HashMap<>();
        properties.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME), instance);

        roomHeaterServices.create(instance,properties);
        attacher.link(instance,zone);
    }

    @Unbind(id="zones")
    public void unbindzone(Zone zone){
        String name = zone.getZoneName()+".heaters";

        roomHeaterServices.delete(name);
        attacher.unlink(name,zone);
    }

    @Modified(id="heaterSrv")
    public void modifiedheaters(HeatersService srv){
        System.out.println("APP(heat) Heater modified: "+srv.getServiceName());
        balconyThermometerService.getExternalZoneSensor(((ZoneService)srv).getZone());
        String tmp = (srv.getServiceName().split("\\."))[0];
        setTemperature(280d,tmp);
    }
    @Bind(id="heaterSrv")
    public void bindedHeaters(HeatersService srv){
        System.out.println("APP(heat) Heater srv binded: "+srv.getServiceName());
        Double temp= (Double) balconyThermometerService.getExternalZoneSensor(((ZoneService)srv).getZone()).getValue();
        String zone =srv.getServiceName();

        System.out.println("temp got:"+temp);


        if(temp==null){
            setTemperature(200,zone);
        }else{
            setTemperature(temp,zone);
        }

    }


    //FUNCTIONS
    public void setTemperature(double outsideTemperature, String zone){
        System.out.println("Trying to set Temperature...");
        for(HeatersService htrSrv:heaterSrvices){
            String tmp=(htrSrv.getServiceName().split("\\."))[0];
            System.out.println("zone: "+zone);
            System.out.println("srv zone:"+tmp);
            if(tmp.equals(zone)){
                System.out.println("equal by LOCATED SPLIT");

                if(outsideTemperature>290d){
                    htrSrv.setPowerLevel(0d);
                    System.out.println("highTemp");
                }else if(outsideTemperature>273.15d){//0°C
                    htrSrv.setPowerLevel(0.9d);
                    System.out.println("midTemp");
                }else if(outsideTemperature>256.2d){//-16.95
                    htrSrv.setPowerLevel(1d);
                    System.out.println("lowTemp");
                }
            }
        }

    }


}
