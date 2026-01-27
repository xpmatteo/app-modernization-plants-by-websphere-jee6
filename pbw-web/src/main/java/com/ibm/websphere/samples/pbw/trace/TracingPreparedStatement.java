// ABOUTME: PreparedStatement wrapper that captures SQL parameters and traces execution.
// ABOUTME: Records bound parameters and writes trace events on executeQuery/executeUpdate.

package com.ibm.websphere.samples.pbw.trace;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PreparedStatement wrapper that captures bound parameters and traces SQL execution.
 */
public class TracingPreparedStatement extends TracingStatement implements PreparedStatement {

    private final PreparedStatement preparedDelegate;
    private final String sql;
    private final Map<Integer, Object> parameters = new HashMap<>();

    public TracingPreparedStatement(PreparedStatement delegate, String sql) {
        super(delegate);
        this.preparedDelegate = delegate;
        this.sql = sql;
    }

    private List<Object> getParameterList() {
        List<Object> params = new ArrayList<>();
        if (parameters.isEmpty()) {
            return params;
        }
        int maxIndex = parameters.keySet().stream().max(Integer::compareTo).orElse(0);
        for (int i = 1; i <= maxIndex; i++) {
            params.add(parameters.get(i));
        }
        return params;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        ResultSet rs = preparedDelegate.executeQuery();
        if (TraceContext.isActive()) {
            return new TracingResultSet(rs, sql, getParameterList());
        }
        return rs;
    }

    @Override
    public int executeUpdate() throws SQLException {
        int rows = preparedDelegate.executeUpdate();
        if (TraceContext.isActive()) {
            TraceWriter.writeJdbcUpdate(sql, getParameterList(), rows);
        }
        return rows;
    }

    @Override
    public boolean execute() throws SQLException {
        boolean result = preparedDelegate.execute();
        if (TraceContext.isActive()) {
            if (!result) {
                int updateCount = preparedDelegate.getUpdateCount();
                TraceWriter.writeJdbcUpdate(sql, getParameterList(), updateCount);
            }
        }
        return result;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        parameters.put(parameterIndex, null);
        preparedDelegate.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setByte(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setShort(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        parameters.clear();
        preparedDelegate.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setObject(parameterIndex, x);
    }

    @Override
    public void addBatch() throws SQLException {
        preparedDelegate.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        parameters.put(parameterIndex, "<blob>");
        preparedDelegate.setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        parameters.put(parameterIndex, "<clob>");
        preparedDelegate.setClob(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setArray(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedDelegate.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        parameters.put(parameterIndex, null);
        preparedDelegate.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedDelegate.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        parameters.put(parameterIndex, value);
        preparedDelegate.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        parameters.put(parameterIndex, "<nclob>");
        preparedDelegate.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        parameters.put(parameterIndex, "<sqlxml>");
        preparedDelegate.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
            throws SQLException {
        parameters.put(parameterIndex, x);
        preparedDelegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        parameters.put(parameterIndex, "<stream>");
        preparedDelegate.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        parameters.put(parameterIndex, "<reader>");
        preparedDelegate.setNClob(parameterIndex, reader);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        long rows = preparedDelegate.executeLargeUpdate();
        if (TraceContext.isActive()) {
            TraceWriter.writeJdbcUpdate(sql, getParameterList(), (int) rows);
        }
        return rows;
    }
}
