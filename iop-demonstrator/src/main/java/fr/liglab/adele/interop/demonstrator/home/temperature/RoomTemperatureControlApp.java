package fr.liglab.adele.interop.demonstrator.home.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.services.temperature.*;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ContextEntity(coreServices = {ApplicationLayer.class})
@Provides(specifications = RoomTemperatureControlApp.class)

public class RoomTemperatureControlApp implements ApplicationLayer{

    private static final Logger LOG = LoggerFactory.getLogger(RoomTemperatureControlApp.class);
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
    private @Creator.Field Creator.Entity<HeatersServiceImpl> roomHeaterServices;
    private @Creator.Field Creator.Entity<BalconyThermometerServiceImpl> balconyThermometers;



    //ACTIONS
    @Validate
    public void start(){
        localAM();
        LOG.info("Temperature control App Started");
        externalThermometerServiceCreator.create("extThermIOPServ");
        balconyThermometers.create("balconyThermometers");
        localAM();
    }
    @Bind(id="zones", specification = Zone.class, aggregate = true, optional = true)
    public void bindZone(Zone zone){
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

    @Bind(id="extThermometer")
    public void bindservice(ExternalThermometerService srv){
        localAM();
    }
    @Modified(id="extThermometer")
    public void modifiedservice(ExternalThermometerService srv){
        if(srv.isThermometerPresent()>0){
            setTemperature(zoneTmp);
        }
        localAM();

    }


    @Bind(id="balconyThermometerService")
    public void bindbalconyTh(BalconyThermometerService srv){
        localAM();

    }
    @Unbind(id="balconyThermometerService")
    public void unbindbalconyTh(BalconyThermometerService srv){
        LOG.warn("Local thermometer not found, asking Base...",srv);
        localAM();
        externalThermometerService.setConnection(new String[] {Thermometer.class.getCanonicalName()});
    }
    @Modified(id="balconyThermometerService")
    public void modifiedBalconyTher(BalconyThermometerService srv){
        localAM();
    }


    @Bind(id="heaterSrv")
    public void bindedHeaters(HeatersService srv){
        localAM();
        String zone =((ZoneService)srv).getZone();
        zoneTmp=zone;
            setTemperature(zone);
    }
    @Unbind(id="heaterSrv")
    public void unbindedHeaters(HeatersService srv){
        String zone =((ZoneService)srv).getZone();
        localAM();
        outsideThermos.remove(zone);
    }

    @Modified(id="heaterSrv")
    public void modifiedheaters(HeatersService srv){
        localAM();
        String tmp = (srv.getServiceName().split("\\."))[0];
        setTemperature(tmp);
    }


    //FUNCTIONS

    /**
     * Sets the power level of the heaters in a zone, depending on the localAm level
     * @param zone Zone for which the temperature will be attempted to be set
     */
    public void setTemperature(String zone){
        switch (localAM()){
            case 1:
            case 2:
            case 4:
            case 6:
                LOG.warn("can't set Temperature, insufficient resources",zone);
                break;
            case 3:
                //iop present
                double farTemp = (Double) externalThermometerService.getCurrentTemperature().getValue();
                setHeatersPower(farTemp,zone);
                break;
            case 5:
                //balcony present
            case 7:
                //balcony+iop present
                String nearestThermo = balconyThermometerService.getExternalZoneSensor(zone);
                LOG.info("geting temp from local thermometer service",balconyThermometerService);
                double nearTemp = (double)balconyThermometerService.getCurrentTemperature(nearestThermo).getValue();
                if(nearTemp!=-2.0d){
                    setHeatersPower(nearTemp,zone);
                }
                break;
        }








    }

    /**
     * Sets a power level distributed among all the heaters in a zone
     * @param referenceTemperature outside temperature for which the heaters will be set for.
     * @param zone zone for which the power will be set
     */
    private void setHeatersPower(double referenceTemperature, String zone){
        for(HeatersService htrSrv:heaterSrvices){
            String tmp=(htrSrv.getServiceName().split("\\."))[0];
            if(tmp.equals(zone)){
                if(referenceTemperature>290d){//16.85
                    htrSrv.setPowerLevel(0d);
                }else if(referenceTemperature>288.15d){//15°C
                    htrSrv.setPowerLevel(0.2d);
                }else if(referenceTemperature>283.15d){//10°C
                    htrSrv.setPowerLevel(0.4d);
                }else if(referenceTemperature>278.15d){//5°C
                    htrSrv.setPowerLevel(0.6d);
                }else if(referenceTemperature>273.15d){//0°C
                    htrSrv.setPowerLevel(0.8d);
                }else if(referenceTemperature<273.15d){//256.2=-16.95
                    htrSrv.setPowerLevel(1d);
                }
            }
        }
    }

    /**
     *
     * @return
     */
    private byte localAM(){
        int mainSrv = heaterSrvices.size();
        int balconSrv = balconyThermometerService.getServiceQoS();
        int extThSrv = externalThermometerService.getServiceQoS();

        byte level=0b0;
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
        LOG.info("localAM level: "+level);
        return level;
    }


}
