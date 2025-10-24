// ABOUTME: Test stub for ShoppingCartBean used in unit tests
// ABOUTME: Provides a simple implementation with controllable behavior for testing
package com.ibm.websphere.samples.pbw.war;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ibm.websphere.samples.pbw.ejb.ShoppingCartBean;
import com.ibm.websphere.samples.pbw.jpa.Inventory;
import com.ibm.websphere.samples.pbw.jpa.Order;

/**
 * Test stub for ShoppingCartBean that tracks method calls and allows controlled behavior.
 */
public class TestShoppingCartBean extends ShoppingCartBean {

	private ArrayList<Inventory> items = new ArrayList<>();
	private Order orderToReturn = null;
	private int removeAllItemsCallCount = 0;
	private Map<Inventory, Integer> checkInventoryCallCount = new HashMap<>();

	public void setItems(ArrayList<Inventory> items) {
		this.items = items;
	}

	@Override
	public ArrayList<Inventory> getItems() {
		return items;
	}

	public void setOrderToReturn(Order order) {
		this.orderToReturn = order;
	}

	@Override
	public Order createOrder(
			String customerID,
			String billName,
			String billAddr1,
			String billAddr2,
			String billCity,
			String billState,
			String billZip,
			String billPhone,
			String shipName,
			String shipAddr1,
			String shipAddr2,
			String shipCity,
			String shipState,
			String shipZip,
			String shipPhone,
			String creditCard,
			String ccNum,
			String ccExpireMonth,
			String ccExpireYear,
			String cardHolder,
			int shippingMethod,
			Collection<Inventory> items) {
		return orderToReturn;
	}

	@Override
	public void checkInventory(Inventory item) {
		checkInventoryCallCount.put(item, checkInventoryCallCount.getOrDefault(item, 0) + 1);
	}

	@Override
	public void removeAllItems() {
		removeAllItemsCallCount++;
		items.clear();
	}

	public int getRemoveAllItemsCallCount() {
		return removeAllItemsCallCount;
	}

	public int getCheckInventoryCallCount(Inventory item) {
		return checkInventoryCallCount.getOrDefault(item, 0);
	}

	public int getTotalCheckInventoryCallCount() {
		return checkInventoryCallCount.values().stream().mapToInt(Integer::intValue).sum();
	}

	public void reset() {
		items = new ArrayList<>();
		orderToReturn = null;
		removeAllItemsCallCount = 0;
		checkInventoryCallCount.clear();
	}
}
