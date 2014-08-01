package ninja.swagger;

import ninja.lifecycle.LifecycleService;

public interface SwaggerService extends LifecycleService {

    /**
     * Returns the start time of the service.
     * 
     * @return start time
     */
    long getStartTime();

    /**
     * Returns the requested Swagger document, if it exists. Otherwise returns
     * null.
     * 
     * @param name
     * @return the document or null
     */
    String getDocument(String name);

}