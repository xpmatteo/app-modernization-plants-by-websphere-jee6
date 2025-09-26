//
// COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, 
// modify, and distribute these sample programs in any form without payment to IBM for the purposes of 
// developing, using, marketing or distributing application programs conforming to the application 
// programming interface for the operating platform for which the sample code is written. 
// Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS 
// AND IBM DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED 
// WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A PARTICULAR PURPOSE, 
// TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT, 
// INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE 
// SAMPLE SOURCE CODE. IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS 
// OR MODIFICATIONS TO THE SAMPLE SOURCE CODE.  
//
// (C) COPYRIGHT International Business Machines Corp., 2001,2011
// All Rights Reserved * Licensed Materials - Property of IBM
//
package com.ibm.websphere.samples.pbw.war;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.ibm.websphere.samples.pbw.ejb.CatalogMgr;
import com.ibm.websphere.samples.pbw.ejb.ShoppingCartBean;
//import com.ibm.websphere.samples.pbw.ejb.ShoppingCartBean;
import com.ibm.websphere.samples.pbw.jpa.Inventory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;

/**
 * A combination JSF action bean and backing bean for the shopping web
 * page.
 *
 */
@Named("shopping")
@SessionScoped
public class ShoppingBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String ACTION_CART = "cart";
	private static final String ACTION_PRODUCT = "product";
	private static final String ACTION_SHOPPING = "shopping";

	private static final Logger logger = Logger.getLogger(ShoppingBean.class.getName());

	public ShoppingBean() {
		logger.info("[ShoppingBean] Constructor - new session started");
	}

	@PostConstruct
	public void init() {
		logger.info("[ShoppingBean] PostConstruct - bean initialized and ready for use");
	}

	// keep an independent list of items so we can add pricing methods
	private ArrayList<ShoppingItem> cartItems;

	@EJB
	private CatalogMgr catalog;

	private ProductBean product;
	private LinkedList<ProductBean> products;
	private float shippingCost;

	@Inject
	private ShoppingCartBean shoppingCart;

	public String performAddToCart () {
		logger.info("[ShoppingBean] performAddToCart() - Adding item: " + this.product.getInventory().getName() +
				   " (quantity: " + this.product.getQuantity() + ")");

		Inventory item = new Inventory(this.product.getInventory());

		item.setQuantity (this.product.getQuantity());

		shoppingCart.addItem(item);

		String outcome = performCart();
		logger.info("[ShoppingBean] performAddToCart() -> returning: '" + outcome + "' (resolves to: " + outcome + ".xhtml)");
		return outcome;
	}

	public String performCart () {
		cartItems = wrapInventoryItems(shoppingCart.getItems());

		return ShoppingBean.ACTION_CART;
	}

	public String performProductDetail () {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		Map<String, String> requestParams =
			externalContext.getRequestParameterMap();

		String itemID = requestParams.get("itemID");
		logger.info("[ShoppingBean] performProductDetail() - itemID: " + itemID);

		this.product = new ProductBean (this.catalog.getItemInventory
				(requestParams.get ("itemID")));

		String outcome = ShoppingBean.ACTION_PRODUCT;
		logger.info("[ShoppingBean] performProductDetail() -> returning: '" + outcome + "' (resolves to: " + outcome + ".xhtml)");
		logger.info("[ShoppingBean] Product data: " + this.product.toString());

		return outcome;
	}

	public String performRecalculate () {
		
		shoppingCart.removeZeroQuantityItems();

		this.cartItems = wrapInventoryItems(shoppingCart.getItems());

		return performCart();
	}

	public String performShopping () {
		int category = 0;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		Vector<Inventory> inventories;
		Map<String, String> requestParams =
			externalContext.getRequestParameterMap();

		try {
			category = Integer.parseInt (requestParams.get
					("category"));
		}

		catch (Throwable e) {
			if (this.products != null) {
				// No category specified, so just use the last one.

				return ShoppingBean.ACTION_SHOPPING;
			}
		}

		inventories = this.catalog.getItemsByCategory (category);

		this.products = new LinkedList<ProductBean>();

		// Have to convert all the inventory objects into product beans.

		for (Object obj : inventories) {
			Inventory inventory = (Inventory) obj;

			if (inventory.isPublic()) {
				this.products.add (new ProductBean (inventory));
			}
		}

		return ShoppingBean.ACTION_SHOPPING;
	}

	public Collection<ShoppingItem> getCartItems () {
		return this.cartItems;
	}

	public ProductBean getProduct () {
		return this.product;
	}

	public Collection<ProductBean> getProducts () {
		return this.products;
	}

	public String getShippingCostString () {
		return NumberFormat.getCurrencyInstance (Locale.US).format
		(this.shippingCost);
	}

	/**
	 * @return the shippingCost
	 */
	 public float getShippingCost() {
		 return shippingCost;
	 }

	 public void setShippingCost (float shippingCost) {
		 this.shippingCost = shippingCost;
	 }

	 public float getTotalCost () {
		 return shoppingCart.getSubtotalCost() + this.shippingCost;
	 }

	 public String getTotalCostString () {
		 return NumberFormat.getCurrencyInstance (Locale.US).format
		 (getTotalCost());
	 }

	 public ShoppingCartBean getCart() {
		 return shoppingCart;
	 }
	 
	 private ArrayList<ShoppingItem> wrapInventoryItems(Collection<Inventory> invItems) {
		 ArrayList<ShoppingItem> shoppingList = new ArrayList<ShoppingItem>();
		 for (Inventory i : invItems) {
			 shoppingList.add(new ShoppingItem(i));
		 }
		 return shoppingList;
	 }
}
