package io.choerodon.base.infra.dto.asgard;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.asgard.property.PropertyJobParam;

public class ScheduleMethodDTO {

    @ApiModelProperty(value = "方法id")
    private Long id;

    @ApiModelProperty(value = "方法名")
    private String method;

    @ApiModelProperty(value = "方法编码")
    private String code;

    @ApiModelProperty(value = "方法描述")
    private String description;

    private List<PropertyJobParam> paramList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PropertyJobParam> getParamList() {
        return paramList;
    }

    public void setParamList(List<PropertyJobParam> paramList) {
        this.paramList = paramList;
    }

    public ScheduleMethodDTO() {
    }
}
