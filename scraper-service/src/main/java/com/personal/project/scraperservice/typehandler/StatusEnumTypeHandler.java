package com.personal.project.scraperservice.typehandler;

import com.personal.project.scraperservice.constant.StatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes({StatusEnum.class})
@MappedJdbcTypes(JdbcType.INTEGER)
public class StatusEnumTypeHandler extends BaseTypeHandler<StatusEnum> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, StatusEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public StatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return StatusEnum.of(rs.getInt(columnName)).orElse(null);
    }

    @Override
    public StatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return StatusEnum.of(rs.getInt(columnIndex)).orElse(null);
    }

    @Override
    public StatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return StatusEnum.of(cs.getInt(columnIndex)).orElse(null);
    }
}
