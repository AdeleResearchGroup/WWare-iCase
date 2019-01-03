package fr.liglab.adele.interop.services.database;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import org.influxdb.InfluxDB;

@ContextService
public interface influxService extends ServiceLayer {
    @State
    String SERVICE_STATUS = "service.status";

    boolean isInfluxRunning();
    void singleDBwrite(String varName, String varValue, String Parameters);
    void writeSensorsState(int time);

}
