package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrderRepository {
    HashMap<String, Order> orderDB;
    HashMap<String, DeliveryPartner> deliveryPartnerDB;
    HashMap<String, Set<String>> partnerOrderPairDB; // key: partnerID, values: set of orderIDs
    HashMap<String, String> orderPartnerPairDB; // key: orderID, values: partnerID

    public OrderRepository() {
        this.orderDB = new HashMap<>();
        this.deliveryPartnerDB = new HashMap<>();
        this.partnerOrderPairDB = new HashMap<>();
        this.orderPartnerPairDB = new HashMap<>();
    }

    public void addOrder(Order order) {
        String orderId = order.getId();
        if(orderId != null) orderDB.put(order.getId(), order);
    }

    public void addPartner(String partnerId){
        deliveryPartnerDB.put(partnerId, new DeliveryPartner(partnerId));
    }

    public void addOrderPartnerPair(String orderId, String partnerId){
        if(orderDB.containsKey(orderId) && deliveryPartnerDB.containsKey(partnerId)) {
            Set<String> orders;
            if(partnerOrderPairDB.containsKey(partnerId)) {
                orders = partnerOrderPairDB.get(partnerId);
            } else {
                orders = new HashSet<>();
            }
            orders.add(orderId);
            partnerOrderPairDB.put(partnerId, orders);
            deliveryPartnerDB.get(partnerId).setNumberOfOrders(orders.size());
            orderPartnerPairDB.put(orderId, partnerId);
        }
    }

    public Order getOrderById(String orderId) {
        return orderDB.get(orderId);
    }

    public DeliveryPartner getPartnerById(String partnerId){
        return deliveryPartnerDB.get(partnerId);
    }

    public int getOrderCountByPartnerId(String partnerId){
        if(partnerOrderPairDB.containsKey(partnerId)) {
            return partnerOrderPairDB.get(partnerId).size();
        }
        return 0;
//        return deliveryPartnerDB.get(partnerId).getNumberOfOrders();
    }

    public List<String> getOrdersByPartnerId(String partnerId){
        return new ArrayList<>(partnerOrderPairDB.get(partnerId));
    }

    public List<String> getAllOrders(){
        return new ArrayList<>(orderDB.keySet());
    }

    public Integer getCountOfUnassignedOrders(){
        Integer cnt = 0;
        for(String orderId : orderDB.keySet()) {
            if(!orderPartnerPairDB.containsKey(orderId)) cnt++;
        }
        return cnt;
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId){
        int timeInt = Integer.parseInt(time.substring(0,2))*60 + Integer.parseInt(time.substring(3));
        Integer leftOrderCnt = 0;
        Set<String> orders = partnerOrderPairDB.get(partnerId);
        for(String orderID : orders) {
            if(orderDB.get(orderID).getDeliveryTime() > timeInt) leftOrderCnt++;
        }
        return leftOrderCnt;
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId){
        StringBuilder time = new StringBuilder();
        int maxTime = 0;
        if(partnerOrderPairDB.containsKey(partnerId)) {
            Set<String> orders = partnerOrderPairDB.get(partnerId);
            for(String orderID : orders) {
                maxTime = Math.max(maxTime, orderDB.get(orderID).getDeliveryTime());
            }
        }

        int hours = maxTime / 60;
        if(hours < 10) time.append('0').append(hours);
        else time.append(hours);

        time.append(':');

        int minutes = maxTime % 60;
        if(minutes < 10) {
            time.append('0').append(minutes);
        } else {
            time.append(minutes);
        }

        return time.toString();
    }

    public void deletePartnerById(String partnerId){
        partnerOrderPairDB.remove(partnerId);

        for(String orderID : orderPartnerPairDB.keySet()) {
            if(orderPartnerPairDB.get(orderID).equals(partnerId)) {
                orderPartnerPairDB.remove(orderID);
            }
        }

        deliveryPartnerDB.remove(partnerId);
    }

    public void deleteOrderById(String orderId){
        orderDB.remove(orderId);
        String partnerID = orderPartnerPairDB.get(orderId);
        orderPartnerPairDB.remove(orderId);
        Set<String> orderIDs = partnerOrderPairDB.get(partnerID);
        orderIDs.remove(orderId);
        partnerOrderPairDB.put(partnerID, orderIDs);
        DeliveryPartner deliveryPartner = deliveryPartnerDB.get(partnerID);
        deliveryPartner.setNumberOfOrders(orderIDs.size());
    }
}
