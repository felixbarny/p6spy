/*
 * #%L
 * P6Spy
 * %%
 * Copyright (C) 2002 - 2016 P6Spy
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.p6spy.engine.wrapper;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.p6spy.engine.common.CallableStatementInformation;
import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.proxy.P6Proxy;

/**
 * Provides a convenient implementation of the Connection interface
 * that can be subclassed by developers wishing to adapt implementation.
 * <p>
 * This class implements the Wrapper or Decorator pattern. Methods default
 * to calling through to the wrapped request object.
 *
 * @see Connection
 */
public class ConnectionWrapper implements Connection, P6Proxy {

  private final Connection delegate;
  private final JdbcEventListener eventListener;
  private final ConnectionInformation connectionInformation;

  public ConnectionWrapper(Connection delegate, JdbcEventListener eventListener) {
    this.delegate = delegate;
    this.eventListener = eventListener;
    connectionInformation = new ConnectionInformation();
  }

  @Override
  public Statement createStatement() throws SQLException {
    return new StatementWrapper(delegate.createStatement(), new StatementInformation(connectionInformation), eventListener);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    return new StatementWrapper(delegate.createStatement(resultSetType, resultSetConcurrency), new StatementInformation(connectionInformation), eventListener);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return new StatementWrapper(delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), new StatementInformation(connectionInformation), eventListener);
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return new PreparedStatementWrapper(delegate.prepareStatement(sql), new PreparedStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    return new PreparedStatementWrapper(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency), new PreparedStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return new PreparedStatementWrapper(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), new PreparedStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return new PreparedStatementWrapper(delegate.prepareStatement(sql, autoGeneratedKeys), new PreparedStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return new PreparedStatementWrapper(delegate.prepareStatement(sql, columnIndexes), new PreparedStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return new PreparedStatementWrapper(delegate.prepareStatement(sql, columnNames), new PreparedStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return new CallableStatementWrapper(delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), new CallableStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    return new CallableStatementWrapper(delegate.prepareCall(sql), new CallableStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    return new CallableStatementWrapper(delegate.prepareCall(sql, resultSetType, resultSetConcurrency), new CallableStatementInformation(connectionInformation, sql), eventListener);
  }

  @Override
  public void commit() throws SQLException {
    long start = System.nanoTime();
    try {
      delegate.commit();
    } finally {
      eventListener.onCommit(connectionInformation, System.nanoTime() - start);
    }
  }

  @Override
  public void rollback() throws SQLException {
    long start = System.nanoTime();
    try {
      delegate.rollback();
    } finally {
      eventListener.onRollback(connectionInformation, System.nanoTime() - start);
    }
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    long start = System.nanoTime();
    try {
      delegate.rollback(savepoint);
    } finally {
      eventListener.onRollback(connectionInformation, System.nanoTime() - start);
    }
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    return delegate.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    delegate.setAutoCommit(autoCommit);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return delegate.getAutoCommit();
  }

  @Override
  public void close() throws SQLException {
    try {
      delegate.close();
    } finally {
      eventListener.onConnectionClose(connectionInformation);
    }
  }

  @Override
  public boolean isClosed() throws SQLException {
    return delegate.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return delegate.getMetaData();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    delegate.setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return delegate.isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    delegate.setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    return delegate.getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    delegate.setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return delegate.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return delegate.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    delegate.clearWarnings();
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return delegate.getTypeMap();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    delegate.setTypeMap(map);
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    delegate.setHoldability(holdability);
  }

  @Override
  public int getHoldability() throws SQLException {
    return delegate.getHoldability();
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    return delegate.setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    return delegate.setSavepoint(name);
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    delegate.releaseSavepoint(savepoint);
  }

  @Override
  public Clob createClob() throws SQLException {
    return delegate.createClob();
  }

  @Override
  public Blob createBlob() throws SQLException {
    return delegate.createBlob();
  }

  @Override
  public NClob createNClob() throws SQLException {
    return delegate.createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    return delegate.createSQLXML();
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    return delegate.isValid(timeout);
  }

  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    delegate.setClientInfo(name, value);
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    delegate.setClientInfo(properties);
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    return delegate.getClientInfo(name);
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    return delegate.getClientInfo();
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return delegate.createArrayOf(typeName, elements);
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return delegate.createStruct(typeName, attributes);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return delegate.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return delegate.isWrapperFor(iface);
  }

  public void setSchema(String schema) throws SQLException {
    delegate.setSchema(schema);
  }

  public String getSchema() throws SQLException {
    return delegate.getSchema();
  }

  public void abort(Executor executor) throws SQLException {
    delegate.abort(executor);
  }

  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    delegate.setNetworkTimeout(executor, milliseconds);
  }

  public int getNetworkTimeout() throws SQLException {
    return delegate.getNetworkTimeout();
  }

  @Override
  public Object unwrapP6SpyProxy() {
    return delegate;
  }
}