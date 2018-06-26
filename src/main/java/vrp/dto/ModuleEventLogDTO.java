package vrp.dto;

import org.apache.commons.lang3.StringUtils;
import vrp.exceptions.CreateInvalidObjectException;

public class ModuleEventLogDTO {

    //////////////////////////////////
    // Calculated fields
    //

    private final String projectName;
    private final String moduleName;
    private final String textLog;


    //////////////////////////////////
    // Constructors
    //

    public ModuleEventLogDTO(String projectName, String moduleName, String textLog) {
        this.projectName = projectName;
        this.moduleName = moduleName;
        this.textLog = textLog;
        validateCreateObject();
    }


    //////////////////////////////////
    // Accessors
    //

    public String getProjectName() {
        return projectName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getTextLog() {
        return textLog;
    }


    //////////////////////////////////
    // Validate invariants fields
    //

    protected void validateCreateObject(){
        validateTextLog();
        validateProjectName();
        validateModuleName();
    }

    protected void validateTextLog(){
        if(StringUtils.isEmpty(textLog)){
            throw new CreateInvalidObjectException("Text log can not be empty");
        }
    }

    protected void validateProjectName(){
        if(StringUtils.isEmpty(projectName)){
            throw new CreateInvalidObjectException("Project name can not be empty");
        }
    }

    protected void validateModuleName(){
        if(StringUtils.isEmpty(moduleName)){
            throw new CreateInvalidObjectException("Module name can not be empty");
        }
    }
}
