package fr.liglab.adele.interop.services.dataManip;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

@ContextEntity(coreServices = {pidService.class, ServiceLayer.class})
public class pidServiceImpl implements pidService,ServiceLayer {

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.NAME)
            public String name;
    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS,value="100",directAccess = true)
            private int SrvQoS;

    MiniPID miniPID;

    @Override
    public int getMinQos() {
        return 100;
    }

    @Override
    public int getServiceQoS() {
        SrvQoS=100;
        return SrvQoS;
    }

    @Override
    public String getServiceName() {
        return name;
    }

    @Override
    public double getControlVariableValue(double P, double I, double D, double objective, double currentValue){
        miniPID= new MiniPID(P,I,D);
        miniPID.setOutputLimits(0,1);
        miniPID.setSetpointRange(4);

        miniPID.setSetpoint(objective);
        double controlSignal = miniPID.getOutput(currentValue,objective);

        //System.err.printf("Target\tActual\tOutput\tError\n");
        //System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f\n",objective,currentValue,controlSignal,(objective-currentValue));
        return controlSignal;
    }
}
