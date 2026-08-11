package com.example.petnow.mapper.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import com.example.petnow.entity.ReservationType;

@MappedTypes(ReservationType.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ReservationTypeHandler extends BaseTypeHandler<ReservationType> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ReservationType parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.getLabel());
	}

	@Override
	public ReservationType getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String label = rs.getString(columnName);
		return toEnum(label);
	}

	@Override
	public ReservationType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String label = rs.getString(columnIndex);
		return toEnum(label);
	}

	@Override
	public ReservationType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String label = cs.getString(columnIndex);
		return toEnum(label);
	}

	private ReservationType toEnum(String label) {
		if (label == null) {
			return null;
		}
		return ReservationType.fromLabel(label);
	}
}
