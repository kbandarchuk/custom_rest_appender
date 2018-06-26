package vrp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import vrp.dto.ModuleEventLogDTO;
import vrp.exceptions.CreateInvalidObjectException;

public class CustomRestAppender extends AppenderSkeleton {

    //////////////////////////////////
    // Calculated fields
    //

    private String restURL;
    private String credBasicAuth;
    private String projectName;
    private String moduleName;


    //////////////////////////////////
    // Constructors
    //

    public CustomRestAppender() {
    }


    //////////////////////////////////
    // Accessors
    //

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    public void setRestURL(final String restURL) {
        this.restURL = restURL;
    }

    public void setCredBasicAuth(final String credBasicAuth) {
        this.credBasicAuth = credBasicAuth;
    }


    //////////////////////////////////
    // Business Logic
    //

    protected void append(final LoggingEvent loggingEvent) {
        validateRestURL();
        validateProjectName();
        validateModuleName();

        try {
           final var restTemplate = new RestTemplate();
           final var json = new ObjectMapper().writeValueAsString(new ModuleEventLogDTO( projectName
                                                                                       , moduleName
                                                                                       , layout.format(loggingEvent)));
           final var requestBody = new HttpEntity<>(json, getHttpHeaders());
           restTemplate.exchange( restURL
                                , HttpMethod.POST
                                , requestBody
                                , String.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Exception in JSON processing", e);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Can't send log, check request data(project name, module name and log format)", e);
        }
    }

    public void close() {
    }

    public boolean requiresLayout() {
        return false;
    }

    protected HttpHeaders getHttpHeaders() {
        validateCredBasicAuth();

        final var headers = new HttpHeaders();
        final var base64Credentials = new String(Base64.encodeBase64(credBasicAuth.getBytes()));
        headers.add("Authorization", "Basic " + base64Credentials);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    //////////////////////////////////
    // Validate invariants fields
    //

    protected void validateRestURL(){
        if(StringUtils.isEmpty(restURL)){
            throw new CreateInvalidObjectException("URL can't be null. Check appender properties");
        }
    }

    protected void validateCredBasicAuth(){
        if(StringUtils.isEmpty(credBasicAuth)){
            throw new CreateInvalidObjectException("Credentials for basic authentication can't be null. Check appender properties");
        }
    }

    protected void validateProjectName(){
        if(StringUtils.isEmpty(projectName)){
            throw new CreateInvalidObjectException("Project name can't be null. Check appender properties");
        }
    }
    protected void validateModuleName(){
        if(StringUtils.isEmpty(moduleName)){
            throw new CreateInvalidObjectException("Module name can't be null. Check appender properties");
        }
    }
}
