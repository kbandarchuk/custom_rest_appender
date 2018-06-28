package vrp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import vrp.dto.ModuleEventLogDTO;
import vrp.exceptions.CreateInvalidObjectException;
import vrp.exceptions.UnexpectedResponseStatusCode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
            final var client = HttpClients.createDefault();
            final var response = client.execute(getHttpPostRequest(loggingEvent));
            checkResponseStatus(response);
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
    }

    public boolean requiresLayout() {
        return false;
    }

    protected HttpPost getHttpPostRequest(final LoggingEvent loggingEvent) {
        validateCredBasicAuth();

        final var httpPost = new HttpPost(restURL);
        httpPost.setEntity(getJsonEntity(loggingEvent));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        final var base64Credentials = new String(Base64.encodeBase64(credBasicAuth.getBytes()));
        httpPost.setHeader("Authorization", "Basic " + base64Credentials);
        return httpPost;
    }

    protected StringEntity getJsonEntity(final LoggingEvent loggingEvent) {
        try {
            return new StringEntity(new ObjectMapper().writeValueAsString(new ModuleEventLogDTO( projectName
                                                                                               , moduleName
                                                                                               , layout.format(loggingEvent))));
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected void checkResponseStatus(final CloseableHttpResponse response) {
        final var responseStatusCode = response.getStatusLine().getStatusCode();
        if (responseStatusCode == 404) {
            throw new UnexpectedResponseStatusCode("Status 404. Page not found. Check parameter of log appender: RestURL");
        }
        if (responseStatusCode == 401) {
            throw new UnexpectedResponseStatusCode("Status 401. Unauthorized. Check parameter of log appender: CredBasicAuth ");
        }
        if (responseStatusCode == 412) {
            throw new UnexpectedResponseStatusCode("Status 412. Precondition Failed. Check parameters of log appender: ProjectName, ModuleName and log layout");
        }
        if (responseStatusCode != 200) {
            throw new UnexpectedResponseStatusCode("UnexpectedResponseStatusCode. Check parameters of log appender");
        }
    }


    //////////////////////////////////
    // Validate invariants fields
    //

    protected void validateRestURL() {
        if (StringUtils.isEmpty(restURL)) {
            throw new CreateInvalidObjectException("URL can't be null. Check appender properties");
        }
    }

    protected void validateCredBasicAuth() {
        if (StringUtils.isEmpty(credBasicAuth)) {
            throw new CreateInvalidObjectException("Credentials for basic authentication can't be null. Check appender properties");
        }
    }

    protected void validateProjectName() {
        if (StringUtils.isEmpty(projectName)) {
            throw new CreateInvalidObjectException("Project name can't be null. Check appender properties");
        }
    }

    protected void validateModuleName() {
        if (StringUtils.isEmpty(moduleName)) {
            throw new CreateInvalidObjectException("Module name can't be null. Check appender properties");
        }
    }
}
