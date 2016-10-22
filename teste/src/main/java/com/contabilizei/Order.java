package com.contabilizei;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.javafaker.Faker;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;

/**
* This class consists of  creating static functions for the Order/OrderItem resources in which the OrderAPI class
* can use without any prejudice and making the code isolated and cleaner. 
* This class has a DatastoreSerivce statically declared to be used whenever it is needed.
*
*
* @author  Thiago Mello
* @since   1.0
*/

public class Order {

	/**
	 * Retrieve current Datastoreservice
	 */
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	/**
	 * Static method to find a unique Order resource by it's code
	 * @param code
	 * @return Entity
	 */
	public static Entity findByCode(String code) {
		Query q = new Query("Order").setFilter(new FilterPredicate("code", FilterOperator.EQUAL, code));
		PreparedQuery pq = datastore.prepare(q);
		return pq.asSingleEntity();
	}
	
	/**
	 * Static method to retrieve all the OrderItems contained within a code specified Order
	 * @param code
	 * @return Entity
	 */
	public static List<Entity> findOrderItemsByCode(String code) {
		Query q = new Query("OrderItem").setFilter(new FilterPredicate("order", FilterOperator.EQUAL, code));
		PreparedQuery pq = datastore.prepare(q);
		return pq.asList(FetchOptions.Builder.withDefaults());
	}

	/**
	 * Static method to retrieve all the Orders within a given limit and page. If not specified, it will
	 * retrieve all the resources entities.
	 * @param limit
	 * @param page
	 * @return List<Entity>
	 */
	public static List<Entity> list(int limit, int page) {
		try {
			Query q;
			List<Entity> result;
			q = new Query("Order").addSort("emitted_at", Query.SortDirection.ASCENDING);
			if (limit <= 0) {
				result = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
			} else {
				result = datastore.prepare(q).asList(FetchOptions.Builder.withOffset(page * limit).limit(limit));
			}
			JSONArray order_items;
			for (Entity entity : result) {
				entity.setProperty("customer", new JSONObject(datastore.get(entity.getParent())).toString());
				Query q_product = new Query("OrderItem");
				q_product.setAncestor(entity.getKey());
				JSONObject temp;
				order_items = new JSONArray();
				List<Entity> res = datastore.prepare(q_product).asList(FetchOptions.Builder.withDefaults());
				for (Entity product : res) {
					temp = new JSONObject();
					temp.append("key", product.getKey().getId());
					temp.append("qty", product.getProperty("qty"));
					temp.append("unit_value", product.getProperty("unit_value"));
					temp.append("product",
							new JSONObject(datastore.get((Key) product.getProperty("product"))).toString());
					order_items.put(temp);
				}
				entity.setProperty("order_items", order_items.toString());
			}
			return result;
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	/**
	 * Static method to insert a new Entity into the Orders resource, a automatic generated code is received
	 * with the current customer which is ordering and also a JSONArray that describe all the cart items to be
	 * inserted (as OrderItems). 
	 * @param code
	 * @param customer
	 * @param cart
	 * @return Entity
	 */
	public static Entity create(String code, Entity customer, JSONArray cart) {
		Entity order = new Entity("Order", code, customer.getKey());
		order.setProperty("code", code);
		order.setProperty("emitted_at", new Date());
		order.setProperty("value", 0);
		datastore.put(order);
		double total_cart = 0;
		double price = 0;
		int qty = 0;
		ArrayList<Key> db_cart = new ArrayList<Key>();
		for (int i = 0; i < cart.length(); i++) {
			JSONObject cart_item = cart.getJSONObject(i);
			JSONObject product = cart_item.getJSONObject("product").getJSONObject("properties");
			Long product_key = cart_item.getJSONObject("product").getJSONObject("key").getLong("id");
			qty = cart_item.getInt("qty");
			if (qty > 0) {
				price = Double.parseDouble(product.getString("unit_value"));
				Entity product_item = new Entity("OrderItem", order.getKey());
				product_item.setIndexedProperty("product", KeyFactory.createKey("Product", product_key));
				product_item.setProperty("order", code);
				product_item.setProperty("unit_value", price);
				product_item.setProperty("qty", qty);
				product_item.setProperty("subtotal", price * qty);
				total_cart += price * qty;
				db_cart.add(datastore.put(product_item));
			}
		}
		order.setIndexedProperty("order_items", db_cart);
		order.setProperty("value", total_cart);
		datastore.put(order);
		return order;

	}
	/**
	 * Static method to update the OrderItems and the Order itself, a JSONArray is received with 
	 * all the cart items(OrderItems) to be updated. A boolean is returned with the result of the update
	 * @param order
	 * @param cart (items)
	 * @return boolean
	 */
	public static boolean update(Entity order, JSONArray cart) {
		try {
			order.setProperty("updated_at", new Date());
			double total_cart = 0;
			double price = 0;
			int qty = 0;
			List<Entity> current_items = findOrderItemsByCode(order.getProperty("code").toString());
			ArrayList<Key> db_cart = new ArrayList<Key>();
			for (Entity entity : current_items) {
				for (int i = 0; i < cart.length(); i++) {
					JSONObject cart_item = cart.getJSONObject(i);
					if (cart_item.getLong("key") == entity.getKey().getId()) {
						price = cart_item.getInt("unit_value");
						qty = cart_item.getInt("qty");
						entity.setProperty("qty", qty);
						entity.setProperty("subtotal", price * qty);
						total_cart += price * qty;
						db_cart.add(datastore.put(entity));
					}
				}
			}
			order.setIndexedProperty("order_items", db_cart);
			order.setProperty("value", total_cart);
			datastore.put(order);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Static method to delete the OrderItems and the Order itself by processing 
	 * the order Key and the customer_key. A boolean is returned with the result of the update
	 * @param order_key
	 * @param customer_key
	 * @see Key
	 * @return boolean
	 */
	public static boolean delete(Key order_key, Key customer_key) {
		try {
			// Delete order
			datastore.delete(order_key);
			// Delete order items
			Query q_product = new Query("OrderItem");
			q_product.setAncestor(order_key);
			List<Entity> res = datastore.prepare(q_product).asList(FetchOptions.Builder.withDefaults());
			for (Entity product : res) {
				datastore.delete(product.getKey());
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
