package fr.liglab.adele.interop.demonstrator.test;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.interop.services.simulations.ExternalTemperatureSimulator;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

@ContextEntity(coreServices = {ApplicationLayer.class})
@Provides(specifications ={testApp.class})
        public class testApp implements ApplicationLayer{

   /* @Creator.Field Creator.Entity<ExternalTemperatureSimulator> extTemperatureSim;
    @Validate
    public void start(){
        extTemperatureSim.create("ExtTempSim");
    }*/
}
