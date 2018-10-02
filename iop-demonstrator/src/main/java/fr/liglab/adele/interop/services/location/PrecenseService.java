package fr.liglab.adele.interop.services.location;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

import java.util.List;

@ContextService
public interface PrecenseService extends ServiceLayer {
    @State
    String CHANGES = "Changes";
    public String getchange(String state);
    String getChanges();
}
