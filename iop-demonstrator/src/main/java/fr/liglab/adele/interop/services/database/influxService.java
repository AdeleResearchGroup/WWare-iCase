package fr.liglab.adele.interop.services.database;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import org.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;

import java.util.List;

@ContextService
public interface InfluxService extends ServiceLayer {
    @State
    String SERVICE_STATUS = "service.status";

    boolean isInfluxRunning();
    void writeAllSensorsState(long time);
    List<QueryResult.Result> QueryDB(SensorType sensorType, String timeStart, String timeDuration, DBfunction function, int limit);
    List<QueryResult.Result>  aiQuery(String timeStart,String lowerLimit, String upperLimit,DBfunction function, int limit);
    List<QueryResult.Result> manualQuery(String Query, String DataBaseName);
    public void eraseDB();

}
