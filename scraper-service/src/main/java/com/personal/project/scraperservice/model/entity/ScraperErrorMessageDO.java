package com.personal.project.scraperservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.personal.project.scraperservice.constant.StatusEnum;
import com.personal.project.scraperservice.typehandler.StatusEnumTypeHandler;
import lombok.Data;
import lombok.ToString;

import java.util.Optional;

@ToString
@Data
@TableName(value = "scraper_error_message", autoResultMap = true)
public class ScraperErrorMessageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String date;

    private String scraperName;

    private String errorMessage;

    //例外名稱
    private String exception;

    //錯誤原因訊息
    private String exceptionMessage;

    //額外資訊
    private String extra;

    @TableField(typeHandler = StatusEnumTypeHandler.class)
    private Integer status = StatusEnum.UNHANDLED.getCode();

    public void setStatus(StatusEnum status) {
        this.status = status.getCode();
    }

    public Optional<StatusEnum> getStatus() {

        return StatusEnum.of(this.status);
    }
}
