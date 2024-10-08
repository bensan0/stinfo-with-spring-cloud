package com.personal.project.stockservice.typehandler;

import cn.hutool.json.JSONUtil;
import com.personal.project.stockservice.model.entity.Tags;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Tags.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class TagsHandler extends BaseTypeHandler<Tags> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Tags parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSONUtil.toJsonStr(parameter));
    }

    @Override
    public Tags getNullableResult(ResultSet rs, String columnName) throws SQLException {

        return JSONUtil.toBean(rs.getString(columnName), Tags.class);
    }

    @Override
    public Tags getNullableResult(ResultSet rs, int columnIndex) throws SQLException {

        return JSONUtil.toBean(rs.getString(columnIndex), Tags.class);
    }

    @Override
    public Tags getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {

        return JSONUtil.toBean(cs.getString(columnIndex), Tags.class);
    }
}
