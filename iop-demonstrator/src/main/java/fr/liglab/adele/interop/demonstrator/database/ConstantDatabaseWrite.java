package fr.liglab.adele.interop.demonstrator.database;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

@ContextService
public interface ConstantDatabaseWrite {
    @State
    String APPLICATION_STATE ="application.state";
}
