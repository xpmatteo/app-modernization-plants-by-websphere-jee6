// ABOUTME: Unit tests for AccountBean.performCompleteCheckout method
// ABOUTME: Tests order creation, inventory checking, email sending, and state cleanup
package com.ibm.websphere.samples.pbw.war;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.pbw.jpa.Customer;
import com.ibm.websphere.samples.pbw.jpa.Inventory;
import com.ibm.websphere.samples.pbw.jpa.Order;

@DisplayName("AccountBean.performCompleteCheckout")
class AccountBeanTest {

	private TestMailerBean mailer;
	private TestShoppingCartBean shoppingCart;
	private Order testOrder;

	private AccountBean accountBean;
	private Customer testCustomer;
	private OrderInfo testOrderInfo;
	private ArrayList<Inventory> testCartItems;

	@BeforeEach
	void setUp() {
		mailer = new TestMailerBean();
		shoppingCart = new TestShoppingCartBean();

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

		// Set up test cart items
		testCartItems = new ArrayList<>();
		Inventory item1 = new Inventory();
		item1.setID("ITEM-001");
		item1.setQuantity(2);
		testCartItems.add(item1);

		Inventory item2 = new Inventory();
		item2.setID("ITEM-002");
		item2.setQuantity(1);
		testCartItems.add(item2);

		// Set AccountBean state
		accountBean.setCustomer(testCustomer);
		accountBean.setOrderInfo(testOrderInfo);

		// Set up test order
		testOrder = new Order();
		testOrder.setOrderID("ORD-12345");

		// Configure shopping cart behavior
		shoppingCart.setItems(testCartItems);
		shoppingCart.setOrderToReturn(testOrder);
	}

	@Test
	@DisplayName("should create order with correct customer ID and order details")
	void testPerformCompleteCheckout_CreatesOrderSuccessfully() throws Exception {
		// When
		accountBean.performCompleteCheckout();

		// Then - order was created (verify by checking that we got the order ID back)
		assertThat(accountBean.getLastOrderNum()).isEqualTo("ORD-12345");
	}

	@Test
	@DisplayName("should store last order number after order creation")
	void testPerformCompleteCheckout_StoresLastOrderNumber() throws Exception {
		// When
		accountBean.performCompleteCheckout();

		// Then
		assertThat(accountBean.getLastOrderNum()).isEqualTo("ORD-12345");
	}

	@Test
	@DisplayName("should send confirmation email with correct customer and order ID")
	void testPerformCompleteCheckout_SendsConfirmationEmail() throws Exception {
		// When
		accountBean.performCompleteCheckout();

		// Then
		assertThat(mailer.getCallCount()).isEqualTo(1);
		assertThat(mailer.getLastCustomer()).isEqualTo(testCustomer);
		assertThat(mailer.getLastOrderId()).isEqualTo("ORD-12345");
	}

	@Test
	@DisplayName("should clear order info after completion")
	void testPerformCompleteCheckout_ClearsOrderInfo() throws Exception {
		// When
		accountBean.performCompleteCheckout();

		// Then
		assertThat(accountBean.getOrderInfo()).isNull();
	}

	@Test
	@DisplayName("should empty shopping cart after completion")
	void testPerformCompleteCheckout_EmptiesShoppingCart() throws Exception {
		// When
		accountBean.performCompleteCheckout();

		// Then
		assertThat(shoppingCart.getRemoveAllItemsCallCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("should return 'orderdone' action")
	void testPerformCompleteCheckout_ReturnsCorrectAction() throws Exception {
		// When
		String result = accountBean.performCompleteCheckout();

		// Then
		assertThat(result).isEqualTo("orderdone");
	}

	@Test
	@DisplayName("should continue when mailer throws MailerAppException")
	void testPerformCompleteCheckout_ContinuesWhenMailerThrowsMailerAppException() throws Exception {
		// Given
		mailer.setShouldThrowMailerException(true);

		// When
		String result = accountBean.performCompleteCheckout();

		// Then - order still completes successfully
		assertThat(result).isEqualTo("orderdone");
		assertThat(shoppingCart.getRemoveAllItemsCallCount()).isEqualTo(1);
		assertThat(accountBean.getOrderInfo()).isNull();
	}

	@Test
	@DisplayName("should continue when mailer throws generic Exception")
	void testPerformCompleteCheckout_ContinuesWhenMailerThrowsGenericException() throws Exception {
		// Given
		mailer.setShouldThrowGenericException(true);

		// When
		String result = accountBean.performCompleteCheckout();

		// Then - order still completes successfully
		assertThat(result).isEqualTo("orderdone");
		assertThat(shoppingCart.getRemoveAllItemsCallCount()).isEqualTo(1);
		assertThat(accountBean.getOrderInfo()).isNull();
	}

	@Test
	@DisplayName("should skip inventory check when cart items collection is empty")
	void testPerformCompleteCheckout_SkipsInventoryCheckWhenCartIsEmpty() throws Exception {
		// Given - empty collection, not null
		ArrayList<Inventory> emptyList = new ArrayList<>();
		shoppingCart.setItems(emptyList);

		// When
		accountBean.performCompleteCheckout();

		// Then - checkInventory should never be called for empty list
		assertThat(shoppingCart.getTotalCheckInventoryCallCount()).isEqualTo(0);
		// But other operations should still complete
		assertThat(mailer.getCallCount()).isEqualTo(1);
		assertThat(shoppingCart.getRemoveAllItemsCallCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("should handle empty cart gracefully")
	void testPerformCompleteCheckout_HandlesEmptyCart() throws Exception {
		// Given
		ArrayList<Inventory> emptyCart = new ArrayList<>();
		shoppingCart.setItems(emptyCart);

		// When
		String result = accountBean.performCompleteCheckout();

		// Then - no inventory checks should occur, but order should complete
		assertThat(shoppingCart.getTotalCheckInventoryCallCount()).isEqualTo(0);
		assertThat(result).isEqualTo("orderdone");
		assertThat(shoppingCart.getRemoveAllItemsCallCount()).isEqualTo(1);
		assertThat(accountBean.getOrderInfo()).isNull();
	}
}
