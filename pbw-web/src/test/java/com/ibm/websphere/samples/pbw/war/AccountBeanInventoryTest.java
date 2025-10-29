// ABOUTME: Integration tests for AccountBean.performCompleteCheckout inventory management logic
// ABOUTME: Tests inventory decrements, back order creation/updates for various scenarios
package com.ibm.websphere.samples.pbw.war;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.pbw.ejb.ShoppingCartBean;
import com.ibm.websphere.samples.pbw.jpa.BackOrder;
import com.ibm.websphere.samples.pbw.jpa.Customer;
import com.ibm.websphere.samples.pbw.jpa.Inventory;
import com.ibm.websphere.samples.pbw.jpa.Order;

@DisplayName("AccountBean.performCompleteCheckout - Inventory Management")
class AccountBeanInventoryTest {

	private TestEntityManager testEntityManager;
	private TestMailerBean mailer;
	private ShoppingCartBean shoppingCart;

	private AccountBean accountBean;
	private Customer testCustomer;
	private OrderInfo testOrderInfo;
	private Order testOrder;

	@BeforeEach
	void setUp() {
		// Set up test EntityManager
		testEntityManager = new TestEntityManager();

		// Set up collaborators
		mailer = new TestMailerBean();
		shoppingCart = new ShoppingCartBean();

		// Inject test EntityManager into ShoppingCartBean using reflection
		try {
			java.lang.reflect.Field emField = ShoppingCartBean.class.getDeclaredField("em");
			emField.setAccessible(true);
			emField.set(shoppingCart, testEntityManager);
		} catch (Exception e) {
			throw new RuntimeException("Failed to inject EntityManager", e);
		}

		accountBean = new AccountBean(null, mailer, shoppingCart);

		// Set up test customer
		testCustomer = new Customer(
			"customer123",
			"password",
			"John",
			"Doe",
			"123 Main St",
			"Apt 4",
			"Springfield",
			"IL",
			"62701",
			"555-123-4567"
		);

		// Set up test order info
		testOrderInfo = new OrderInfo(
			"John Doe",           // billName
			"123 Main St",        // billAddr1
			"Apt 4",              // billAddr2
			"Springfield",        // billCity
			"IL",                 // billState
			"62701",              // billZip
			"555-123-4567",       // billPhone
			"Jane Doe",           // shipName
			"456 Oak Ave",        // shipAddr1
			"",                   // shipAddr2
			"Chicago",            // shipCity
			"IL",                 // shipState
			"60601",              // shipZip
			"555-987-6543",       // shipPhone
			1,                    // shippingMethod
			"ORD-001"             // orderID
		);
		testOrderInfo.setCardName("Visa");
		testOrderInfo.setCardNum("4111 1111 1111 1111");
		testOrderInfo.setCardExpMonth("12");
		testOrderInfo.setCardExpYear("2025");
		testOrderInfo.setCardholderName("John Doe");

		// Set up test order
		testOrder = new Order();
		testOrder.setOrderID("ORD-12345");

		// Add test customer to EntityManager
		testEntityManager.addCustomer(testCustomer);

		// Set AccountBean state
		accountBean.setCustomer(testCustomer);
		accountBean.setOrderInfo(testOrderInfo);
	}

	private InventoryBuilder inventory(String id) {
		return new InventoryBuilder(id);
	}

	private class InventoryBuilder {
		private String id;
		private int quantity;
		private int minThreshold = 50; // default
		private float price = 10.0f;    // default

		public InventoryBuilder(String id) {
			this.id = id;
		}

		public InventoryBuilder withQuantity(int quantity) {
			this.quantity = quantity;
			return this;
		}

		public InventoryBuilder withMinThreshold(int minThreshold) {
			this.minThreshold = minThreshold;
			return this;
		}

		public InventoryBuilder withPrice(float price) {
			this.price = price;
			return this;
		}

		public Inventory create() {
			Inventory inv = new Inventory(
				id,
				"Test Item " + id,
				"Heading",
				"Description",
				"Package Info",
				"image.jpg",
				price,
				price * 0.5f,
				quantity,
				1,
				"Notes",
				true
			);
			inv.setMinThreshold(minThreshold);
			testEntityManager.addInventory(inv);
			return inv;
		}
	}

	private void addItemToCart(String inventoryId, int orderQuantity) {
		Inventory cartItem = new Inventory();
		cartItem.setID(inventoryId);
		cartItem.setQuantity(orderQuantity);
		shoppingCart.addItem(cartItem);
	}

	// ========== BASIC INVENTORY SCENARIOS ==========

	@Test
	@DisplayName("should decrease inventory when sufficient stock is available")
	void testSufficientInventory() throws Exception {
		// Given - plenty of stock, well above minThreshold
		inventory("ITEM-001")
			.withQuantity(100)
			.withMinThreshold(50)
			.create();
		addItemToCart("ITEM-001", 10);

		// When
		accountBean.performCompleteCheckout();

		// Then - inventory decreased correctly
		Inventory inv = testEntityManager.find(Inventory.class, "ITEM-001");
		assertThat(inv.getQuantity()).isEqualTo(90);

		// No backorder created (still above minThreshold)
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-001");
		assertThat(backOrder).isNull();
	}

	@Test
	@DisplayName("should handle exact inventory match (order quantity = stock)")
	void testExactInventoryMatch() throws Exception {
		// Given - exact quantity available
		inventory("ITEM-002")
			.withQuantity(25)
			.withMinThreshold(50)
			.create();
		addItemToCart("ITEM-002", 25);

		// When
		accountBean.performCompleteCheckout();

		// Then - inventory goes to zero
		Inventory inv = testEntityManager.find(Inventory.class, "ITEM-002");
		assertThat(inv.getQuantity()).isEqualTo(0);

		// BackOrder created (went below minThreshold of 50) but quantityNotFilled = 0
		// since all items were fulfilled from inventory
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-002");
		assertThat(backOrder).isNotNull();
		assertThat(backOrder.getQuantity()).isEqualTo(0); // no unfilled orders
	}

	@Test
	@DisplayName("should handle insufficient inventory (partial stock)")
	void testInsufficientInventory() throws Exception {
		// Given - only 5 items in stock, order 20
		inventory("ITEM-003")
			.withQuantity(5)
			.withMinThreshold(50)
			.create();
		addItemToCart("ITEM-003", 20);

		// When
		accountBean.performCompleteCheckout();

		// Then - inventory goes negative
		Inventory inv = testEntityManager.find(Inventory.class, "ITEM-003");
		assertThat(inv.getQuantity()).isEqualTo(-15);

		// BackOrder created for the unfilled portion
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-003");
		assertThat(backOrder).isNotNull();
		assertThat(backOrder.getQuantity()).isEqualTo(15);
		assertThat(backOrder.getStatus()).isEqualTo("Order Stock");
	}

	@Test
	@DisplayName("should handle zero inventory")
	void testZeroInventory() throws Exception {
		// Given - no stock available
		inventory("ITEM-004")
			.withQuantity(0)
			.withMinThreshold(50)
			.create();
		addItemToCart("ITEM-004", 10);

		// When
		accountBean.performCompleteCheckout();

		// Then - inventory goes negative by full order amount
		Inventory inv = testEntityManager.find(Inventory.class, "ITEM-004");
		assertThat(inv.getQuantity()).isEqualTo(-10);

		// BackOrder created for full order quantity
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-004");
		assertThat(backOrder).isNotNull();
		assertThat(backOrder.getQuantity()).isEqualTo(10);
	}

	// ========== THRESHOLD SCENARIOS ==========

	@Test
	@DisplayName("should create backorder when inventory drops below minThreshold")
	void testDropsBelowMinThreshold() throws Exception {
		// Given - 60 in stock, minThreshold=50, order 15
		inventory("ITEM-005")
			.withQuantity(60)
			.withMinThreshold(50)
			.create();
		addItemToCart("ITEM-005", 15);

		// When
		accountBean.performCompleteCheckout();

		// Then - inventory at 45 (below threshold)
		Inventory inv = testEntityManager.find(Inventory.class,"ITEM-005");
		assertThat(inv.getQuantity()).isEqualTo(45);

		// BackOrder created for shortfall
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-005");
		assertThat(backOrder).isNotNull();
		assertThat(backOrder.getQuantity()).isEqualTo(0); // no unfilled, but below threshold
	}

	@Test
	@DisplayName("should NOT create backorder when inventory stays above minThreshold")
	void testStaysAboveMinThreshold() throws Exception {
		// Given - 100 in stock, minThreshold=50, order 30
		inventory("ITEM-006")
			.withQuantity(100)
			.withMinThreshold(50)
			.create();
		addItemToCart("ITEM-006", 30);

		// When
		accountBean.performCompleteCheckout();

		// Then - inventory at 70 (still above threshold)
		Inventory inv = testEntityManager.find(Inventory.class,"ITEM-006");
		assertThat(inv.getQuantity()).isEqualTo(70);

		// No backorder created
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-006");
		assertThat(backOrder).isNull();
	}

	@Test
	@DisplayName("should create backorder when already below threshold and ordered")
	void testAlreadyBelowThreshold() throws Exception {
		// Given - 30 in stock (already below minThreshold=50), order 10
		inventory("ITEM-007")
			.withQuantity(30)
			.withMinThreshold(50)
			.create();
		addItemToCart("ITEM-007", 10);

		// When
		accountBean.performCompleteCheckout();

		// Then - inventory at 20
		Inventory inv = testEntityManager.find(Inventory.class,"ITEM-007");
		assertThat(inv.getQuantity()).isEqualTo(20);

		// BackOrder created
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-007");
		assertThat(backOrder).isNotNull();
		assertThat(backOrder.getQuantity()).isEqualTo(0); // no unfilled orders
	}

	// ========== BACKORDER UPDATE SCENARIOS ==========

	@Test
	@DisplayName("should update existing backorder quantity")
	void testExistingBackOrderUpdated() throws Exception {
		// Given - existing backorder and insufficient stock (5 in stock, order 15)
		Inventory inv = inventory("ITEM-008")
			.withQuantity(5)
			.withMinThreshold(50)
			.create();
		BackOrder existingBackOrder = new BackOrder(inv, 20);
		existingBackOrder.setBackOrderID("BO-EXISTING");
		testEntityManager.persist(existingBackOrder);

		addItemToCart("ITEM-008", 15);

		// When
		accountBean.performCompleteCheckout();

		// Then - inventory decreased (5 - 15 = -10)
		Inventory updatedInv = testEntityManager.find(Inventory.class, "ITEM-008");
		assertThat(updatedInv.getQuantity()).isEqualTo(-10);

		// Existing backorder quantity increased by quantityNotFilled (15 - 5 = 10)
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-008");
		assertThat(backOrder).isSameAs(existingBackOrder);
		assertThat(backOrder.getQuantity()).isEqualTo(30); // 20 + 10 (new unfilled)
	}

	@Test
	@DisplayName("should create new backorder when none exists")
	void testNewBackOrderCreated() throws Exception {
		// Given - no existing backorder
		inventory("ITEM-009")
			.withQuantity(3)
			.withMinThreshold(50)
			.create();
		addItemToCart("ITEM-009", 10);

		// When
		accountBean.performCompleteCheckout();

		// Then - new backorder created
		BackOrder backOrder = testEntityManager.getBackOrder("ITEM-009");
		assertThat(backOrder).isNotNull();
		assertThat(backOrder.getBackOrderID()).isNotNull();
		assertThat(backOrder.getQuantity()).isEqualTo(7); // unfilled portion
		assertThat(backOrder.getStatus()).isEqualTo("Order Stock");
		assertThat(backOrder.getLowDate()).isGreaterThan(0);
	}

	// ========== MULTIPLE ITEMS SCENARIOS ==========

	@Test
	@DisplayName("should handle multiple items with mixed inventory scenarios")
	void testMultipleItemsMixedScenarios() throws Exception {
		// Given - three items with different stock situations
		inventory("ITEM-010").withQuantity(100).withMinThreshold(50).create(); // sufficient, above threshold
		inventory("ITEM-011").withQuantity(40).withMinThreshold(50).create();  // below threshold
		inventory("ITEM-012").withQuantity(5).withMinThreshold(50).create();   // insufficient stock

		addItemToCart("ITEM-010", 20); // 100 -> 80, still above threshold
		addItemToCart("ITEM-011", 10); // 40 -> 30, still below threshold
		addItemToCart("ITEM-012", 15); // 5 -> -10, unfilled

		// When
		accountBean.performCompleteCheckout();

		// Then - verify each item
		Inventory inv1 = testEntityManager.find(Inventory.class,"ITEM-010");
		assertThat(inv1.getQuantity()).isEqualTo(80);
		assertThat(testEntityManager.getBackOrder("ITEM-010")).isNull(); // no backorder

		Inventory inv2 = testEntityManager.find(Inventory.class,"ITEM-011");
		assertThat(inv2.getQuantity()).isEqualTo(30);
		BackOrder backOrder2 = testEntityManager.getBackOrder("ITEM-011");
		assertThat(backOrder2).isNotNull();
		assertThat(backOrder2.getQuantity()).isEqualTo(0); // below threshold but no unfilled

		Inventory inv3 = testEntityManager.find(Inventory.class,"ITEM-012");
		assertThat(inv3.getQuantity()).isEqualTo(-10);
		BackOrder backOrder3 = testEntityManager.getBackOrder("ITEM-012");
		assertThat(backOrder3).isNotNull();
		assertThat(backOrder3.getQuantity()).isEqualTo(10); // unfilled portion
	}

	@Test
	@DisplayName("should handle multiple items all with sufficient inventory")
	void testMultipleItemsAllSufficient() throws Exception {
		// Given - all items have plenty of stock
		inventory("ITEM-013").withQuantity(200).withMinThreshold(50).create();
		inventory("ITEM-014").withQuantity(150).withMinThreshold(50).create();
		inventory("ITEM-015").withQuantity(100).withMinThreshold(50).create();

		addItemToCart("ITEM-013", 10);
		addItemToCart("ITEM-014", 15);
		addItemToCart("ITEM-015", 20);

		// When
		accountBean.performCompleteCheckout();

		// Then - all inventories decreased, no backorders
		assertThat(testEntityManager.find(Inventory.class,"ITEM-013").getQuantity()).isEqualTo(190);
		assertThat(testEntityManager.find(Inventory.class,"ITEM-014").getQuantity()).isEqualTo(135);
		assertThat(testEntityManager.find(Inventory.class,"ITEM-015").getQuantity()).isEqualTo(80);

		assertThat(testEntityManager.getBackOrder("ITEM-013")).isNull();
		assertThat(testEntityManager.getBackOrder("ITEM-014")).isNull();
		assertThat(testEntityManager.getBackOrder("ITEM-015")).isNull();
	}

	@Test
	@DisplayName("should handle multiple items all requiring backorders")
	void testMultipleItemsAllRequireBackOrders() throws Exception {
		// Given - all items have insufficient stock
		inventory("ITEM-016").withQuantity(2).withMinThreshold(50).create();
		inventory("ITEM-017").withQuantity(3).withMinThreshold(50).create();
		inventory("ITEM-018").withQuantity(0).withMinThreshold(50).create();

		addItemToCart("ITEM-016", 10);
		addItemToCart("ITEM-017", 15);
		addItemToCart("ITEM-018", 20);

		// When
		accountBean.performCompleteCheckout();

		// Then - all inventories negative, all have backorders
		Inventory inv1 = testEntityManager.find(Inventory.class,"ITEM-016");
		assertThat(inv1.getQuantity()).isEqualTo(-8);
		assertThat(testEntityManager.getBackOrder("ITEM-016").getQuantity()).isEqualTo(8);

		Inventory inv2 = testEntityManager.find(Inventory.class,"ITEM-017");
		assertThat(inv2.getQuantity()).isEqualTo(-12);
		assertThat(testEntityManager.getBackOrder("ITEM-017").getQuantity()).isEqualTo(12);

		Inventory inv3 = testEntityManager.find(Inventory.class,"ITEM-018");
		assertThat(inv3.getQuantity()).isEqualTo(-20);
		assertThat(testEntityManager.getBackOrder("ITEM-018").getQuantity()).isEqualTo(20);
	}
}
