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
    String zoneTmp="";

    //SERVICE's STATES

    private Map<String,String> outsideThermos = new HashMap<String, String>();

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
        levelOfApplication();
        System.out.println("APP STARTED ROOM TEMPERATURE CONTROL");
        externalThermometerServiceCreator.create("extThermIOPServ");
        balconyThermometers.create("balconyThermometers");
        //extTempSimulatorHelper.create("extTemp");
        //extTemperatureSim.create("ExtTempSim");
        levelOfApplication();
    }
    @Bind(id="extThermometer")
    public void bindservice(ExternalThermometerService ther){
        System.out.println("APP(heat) External Thermometer initilized");
        levelOfApplication();
       // ther.setConnection(new String[] {Thermometer.class.getCanonicalName()});
    }
    @Modified(id="extThermometer")
    public void modifiedservice(ExternalThermometerService ther){
        System.out.println("APP(heat) External Thermometer Mod");

        if(ther.isThermometerPresent()>0){
            setTemperature((double)ther.getCurrentTemperature().getValue(),zoneTmp);
        }

        //setTemperature(balconyThermometerService.getCurrentTemperature(),zone);
        levelOfApplication();
        //ther.getCurrentTemperature(new String[] {Thermometer.class.getCanonicalName()});
    }


    @Bind(id="balconyThermometerService")
    public void bindbalconyTh(BalconyThermometerService balThr){
        System.out.println("balcony SRV binded...");
        levelOfApplication();
        // externalThermometerService.setConnection(new String[] {Thermometer.class.getCanonicalName()});
    }
    @Unbind(id="balconyThermometerService")
    public void unbindbalconyTh(BalconyThermometerService balThr){
        System.out.println("local Thermometer not available... requesting to Base...");
        levelOfApplication();
        externalThermometerService.setConnection(new String[] {Thermometer.class.getCanonicalName()});
    }
    @Modified(id="balconyThermometerService")
    public void modifiedBalconyTher(BalconyThermometerService balThr){
        levelOfApplication();
        // balconyThermometers.getInstance("balconyThermometers").getExternalZoneSensor();
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


    @Bind(id="heaterSrv")
    public void bindedHeaters(HeatersService srv){
        System.out.println("APP(heat) Heater srv binded: "+srv.getServiceName());
        levelOfApplication();
        String zone =((ZoneService)srv).getZone();
        String thermo = balconyThermometerService.getExternalZoneSensor(zone);
        double temp = (double) balconyThermometerService.getCurrentTemperature(thermo).getValue();
        outsideThermos.put(zone,thermo);
        zoneTmp=zone;

        if(thermo==null){
            setTemperature(200,zone);
        }else{
            setTemperature(temp,zone);
        }

    }
    @Unbind(id="heaterSrv")
    public void unbindedHeaters(HeatersService srv){
        String zone =((ZoneService)srv).getZone();
        levelOfApplication();
        outsideThermos.remove(zone);
    }

    @Modified(id="heaterSrv")
    public void modifiedheaters(HeatersService srv){
        System.out.println("APP(heat) Heater modified: "+srv.getServiceName());
        levelOfApplication();
        balconyThermometerService.getExternalZoneSensor(((ZoneService)srv).getZone());
        String tmp = (srv.getServiceName().split("\\."))[0];
        setTemperature(280d,tmp);
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

    private byte levelOfApplication(){
        System.out.println("**********************************");
        int mainSrv = heaterSrvices.size();
        int balconSrv = balconyThermometerService.getServiceQoS();
        int extThSrv = externalThermometerService.getServiceQoS();

        System.out.println("-----------LevelOfAPP--- "+mainSrv+" - "+balconSrv+" - "+extThSrv+" ------------------");
        byte level=0b0;
        //System.out.println(heaterSrvices.size());
        if(mainSrv<1){
            return 0b0;
        }else{
            level+=(byte)0b1;
        }
        if(extThSrv==100){
            level+=(byte)0b10;
        }
        if(balconSrv==100){
            level+=(byte)0b100;
        }
        System.out.println("App level:");
        System.out.println(level);
        return level;
    }


}
