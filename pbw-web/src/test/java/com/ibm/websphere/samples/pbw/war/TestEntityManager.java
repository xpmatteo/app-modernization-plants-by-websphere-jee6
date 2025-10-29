// ABOUTME: Test double for EntityManager that tracks entities in memory
// ABOUTME: Used for testing business logic without a real database
package com.ibm.websphere.samples.pbw.war;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;

import com.ibm.websphere.samples.pbw.jpa.BackOrder;
import com.ibm.websphere.samples.pbw.jpa.Customer;
import com.ibm.websphere.samples.pbw.jpa.Inventory;

/**
 * Test double for EntityManager that stores entities in memory maps.
 * Only implements methods needed for inventory management tests.
 */
public class TestEntityManager implements EntityManager {

	private Map<String, Inventory> inventoryStore = new HashMap<>();
	private Map<String, BackOrder> backOrderStore = new HashMap<>();
	private Map<String, Customer> customerStore = new HashMap<>();
	private int backOrderIdCounter = 1;

	// ========== IMPLEMENTED METHODS ==========

	@Override
	@SuppressWarnings("unchecked")
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		if (entityClass == Inventory.class) {
			return (T) inventoryStore.get(primaryKey.toString());
		} else if (entityClass == BackOrder.class) {
			// BackOrder uses inventory ID as key in our test
			return (T) backOrderStore.get(primaryKey.toString());
		} else if (entityClass == Customer.class) {
			return (T) customerStore.get(primaryKey.toString());
		}
		return null;
	}

	@Override
	public void persist(Object entity) {
		if (entity instanceof BackOrder) {
			BackOrder backOrder = (BackOrder) entity;
			// Simulate auto-generated ID
			if (backOrder.getBackOrderID() == null) {
				backOrder.setBackOrderID("BO-" + backOrderIdCounter++);
			}
			backOrderStore.put(backOrder.getInventory().getInventoryId(), backOrder);
		} else if (entity instanceof Inventory) {
			Inventory inv = (Inventory) entity;
			inventoryStore.put(inv.getInventoryId(), inv);
		} else if (entity instanceof Customer) {
			Customer customer = (Customer) entity;
			customerStore.put(customer.getCustomerID(), customer);
		}
		// Orders are persisted but we don't track them in these tests
	}

	@Override
	public void flush() {
		// No-op for in-memory store
	}

	@Override
	public void lock(Object entity, LockModeType lockMode) {
		// No-op for in-memory store
	}

	@Override
	public void refresh(Object entity) {
		// No-op for in-memory store
	}

	@Override
	public Query createNamedQuery(String name) {
		// Return a test query that can be configured per test
		return new TestQuery(backOrderStore);
	}

	// Helper method to add inventory to the store
	public void addInventory(Inventory inv) {
		inventoryStore.put(inv.getInventoryId(), inv);
	}

	// Helper method to add customer to the store
	public void addCustomer(Customer customer) {
		customerStore.put(customer.getCustomerID(), customer);
	}

	// Helper method to get back orders (for test assertions)
	public BackOrder getBackOrder(String inventoryId) {
		return backOrderStore.get(inventoryId);
	}

	// ========== UNIMPLEMENTED METHODS (throw UnsupportedOperationException) ==========

	@Override
	public <T> T merge(T entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(Object entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
			Map<String, Object> properties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FlushModeType getFlushMode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void detach(Object entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LockModeType getLockMode(Object entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProperty(String propertyName, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query createQuery(CriteriaUpdate updateQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query createQuery(CriteriaDelete deleteQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query createQuery(String qlString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query createNativeQuery(String sqlString, Class resultClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void joinTransaction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isJoinedToTransaction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getDelegate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public EntityTransaction getTransaction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Metamodel getMetamodel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityGraph<?> createEntityGraph(String graphName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityGraph<?> getEntityGraph(String graphName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Inner class to implement Query interface for named queries.
	 */
	private static class TestQuery implements Query {
		private Map<String, BackOrder> backOrderStore;
		private BackOrder resultToReturn;

		public TestQuery(Map<String, BackOrder> backOrderStore) {
			this.backOrderStore = backOrderStore;
		}

		public void setResultToReturn(BackOrder result) {
			this.resultToReturn = result;
		}

		@Override
		public Query setParameter(String name, Object value) {
			// Capture inventory ID parameter for findByInventoryID query
			// Return existing BackOrder from store if it exists
			if ("id".equals(name)) {
				resultToReturn = backOrderStore.get(value.toString());
			}
			return this;
		}

		@Override
		public Object getSingleResult() {
			return resultToReturn;
		}

		// Unimplemented methods
		@Override public List getResultList() { throw new UnsupportedOperationException(); }
		@Override public int executeUpdate() { throw new UnsupportedOperationException(); }
		@Override public Query setMaxResults(int maxResult) { throw new UnsupportedOperationException(); }
		@Override public int getMaxResults() { throw new UnsupportedOperationException(); }
		@Override public Query setFirstResult(int startPosition) { throw new UnsupportedOperationException(); }
		@Override public int getFirstResult() { throw new UnsupportedOperationException(); }
		@Override public Query setHint(String hintName, Object value) { throw new UnsupportedOperationException(); }
		@Override public Map<String, Object> getHints() { throw new UnsupportedOperationException(); }
		@Override public <T> Query setParameter(jakarta.persistence.Parameter<T> param, T value) { throw new UnsupportedOperationException(); }
		@Override public Query setParameter(jakarta.persistence.Parameter<java.util.Calendar> param, java.util.Calendar value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException(); }
		@Override public Query setParameter(jakarta.persistence.Parameter<java.util.Date> param, java.util.Date value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException(); }
		@Override public Query setParameter(String name, java.util.Calendar value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException(); }
		@Override public Query setParameter(String name, java.util.Date value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException(); }
		@Override public Query setParameter(int position, Object value) { throw new UnsupportedOperationException(); }
		@Override public Query setParameter(int position, java.util.Calendar value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException(); }
		@Override public Query setParameter(int position, java.util.Date value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException(); }
		@Override public java.util.Set<jakarta.persistence.Parameter<?>> getParameters() { throw new UnsupportedOperationException(); }
		@Override public jakarta.persistence.Parameter<?> getParameter(String name) { throw new UnsupportedOperationException(); }
		@Override public <T> jakarta.persistence.Parameter<T> getParameter(String name, Class<T> type) { throw new UnsupportedOperationException(); }
		@Override public jakarta.persistence.Parameter<?> getParameter(int position) { throw new UnsupportedOperationException(); }
		@Override public <T> jakarta.persistence.Parameter<T> getParameter(int position, Class<T> type) { throw new UnsupportedOperationException(); }
		@Override public boolean isBound(jakarta.persistence.Parameter<?> param) { throw new UnsupportedOperationException(); }
		@Override public <T> T getParameterValue(jakarta.persistence.Parameter<T> param) { throw new UnsupportedOperationException(); }
		@Override public Object getParameterValue(String name) { throw new UnsupportedOperationException(); }
		@Override public Object getParameterValue(int position) { throw new UnsupportedOperationException(); }
		@Override public Query setFlushMode(FlushModeType flushMode) { throw new UnsupportedOperationException(); }
		@Override public FlushModeType getFlushMode() { throw new UnsupportedOperationException(); }
		@Override public Query setLockMode(LockModeType lockMode) { throw new UnsupportedOperationException(); }
		@Override public LockModeType getLockMode() { throw new UnsupportedOperationException(); }
		@Override public <T> T unwrap(Class<T> cls) { throw new UnsupportedOperationException(); }
	}
}
