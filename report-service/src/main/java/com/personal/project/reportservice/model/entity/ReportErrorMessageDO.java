package com.personal.project.reportservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.personal.project.reportservice.constant.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

@ToString
@Setter
@TableName("report_error_message")
public class ReportErrorMessageDO {

    @Setter
    @Getter
    @TableId(type = IdType.AUTO)
    private Long id;

    @Setter
    @Getter
    private String date;

    @Setter
    @Getter
    private String reportName;

    @Setter
    @Getter
    private String errorMessage;

    //例外名稱
    @Setter
    @Getter
    private String exception;

    //錯誤原因訊息
    @Setter
    @Getter
    private String exceptionMessage;

    //額外資訊
    @Setter
    @Getter
    private String extra;

    private Integer status = StatusEnum.UNHANDLED.getCode();

    public void setStatus(StatusEnum status) {
        this.status = status.getCode();
    }

    public Optional<StatusEnum> getStatus() {

        return StatusEnum.of(this.status);
    }
}
