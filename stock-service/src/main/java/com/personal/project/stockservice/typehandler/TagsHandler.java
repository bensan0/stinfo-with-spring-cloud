package com.personal.project.stockservice.typehandler;

import cn.hutool.json.JSONUtil;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes({DailyStockInfoDetailDO.Tags.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class TagsHandler extends BaseTypeHandler<DailyStockInfoDetailDO.Tags> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, DailyStockInfoDetailDO.Tags parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSONUtil.toJsonStr(parameter));
    }

    @Override
    public DailyStockInfoDetailDO.Tags getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return JSONUtil.toBean(rs.getString(columnName), DailyStockInfoDetailDO.Tags.class);
    }

    @Override
    public DailyStockInfoDetailDO.Tags getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return JSONUtil.toBean(rs.getString(columnIndex), DailyStockInfoDetailDO.Tags.class);
    }

    @Override
    public DailyStockInfoDetailDO.Tags getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return JSONUtil.toBean(cs.getString(columnIndex), DailyStockInfoDetailDO.Tags.class);
    }
}
