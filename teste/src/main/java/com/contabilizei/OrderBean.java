package com.contabilizei;

import java.util.List;

import org.json.JSONArray;

import com.github.javafaker.Faker;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

/**
* This is a request model to interpret and receive mapped attributes in the API
*
* @author  Thiago Mello
* @see     Api
* @since   1.0
*/

public class OrderBean {
	private long customer, key;
	private JSONArray products;

	public long getKey() {
		return key;
	}

	public void setKey(long key) {
		this.key = key;
	}

	public long getCustomer() {
		return customer;
	}

	public void setCustomer(long customer) {
		this.customer = customer;
	}

	public JSONArray getProducts() {
		return products;
	}

	public void setProducts(JSONArray products) {
		this.products = products;
	}

}
