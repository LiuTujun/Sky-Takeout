package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("员工DTO类")
public class EmployeeDTO implements Serializable {

    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("用户姓名")
    private String name;

    @ApiModelProperty("用户电话")
    private String phone;

    @ApiModelProperty("用户性别")
    private String sex;

    @ApiModelProperty("用户身份证号")
    private String idNumber;

}
