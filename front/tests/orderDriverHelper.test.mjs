import test from "node:test";
import assert from "node:assert/strict";

import {
  filterDriverVisibleOrders,
  getOrderDriverId,
  normalizeOrderItems,
} from "../src/lib/orderDriverHelper.js";

const buildOrderItem = ({ id, state, driver_id = null, delivery = "西电南校区" }) => ({
  orderInfo: {
    id,
    state,
    driver_id,
  },
  delivery,
  receive: "顾客宿舍",
  cusName: "顾客",
  merName: "商家",
});

test("normalizeOrderItems 能兼容后端 snake_case driver_id", () => {
  const [item] = normalizeOrderItems([
    buildOrderItem({ id: "order-1", state: 1, driver_id: "4" }),
  ]);

  assert.equal(item.orderInfo.driverId, "4");
  assert.equal(getOrderDriverId(item), "4");
});

test("filterDriverVisibleOrders 会保留当前骑手的配送中订单", () => {
  const list = [
    buildOrderItem({ id: "order-1", state: 1, driver_id: "4" }),
    buildOrderItem({ id: "order-2", state: 3, driver_id: null }),
  ];

  const visible = filterDriverVisibleOrders(list, "4", () => true, 3);

  assert.deepEqual(
    visible.map((item) => item.orderInfo.id),
    ["order-1", "order-2"]
  );
});

test("filterDriverVisibleOrders 仅展示服务区域匹配的新订单", () => {
  const list = [
    buildOrderItem({ id: "order-1", state: 3, delivery: "竹园餐厅" }),
    buildOrderItem({ id: "order-2", state: 3, delivery: "海棠餐厅" }),
  ];

  const visible = filterDriverVisibleOrders(
    list,
    "4",
    (item) => item.delivery.includes("竹园"),
    3
  );

  assert.deepEqual(
    visible.map((item) => item.orderInfo.id),
    ["order-1"]
  );
});
