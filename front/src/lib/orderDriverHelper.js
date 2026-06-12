const normalizeOrderInfo = (orderInfo = {}) => ({
  ...orderInfo,
  prodNum: orderInfo.prodNum ?? orderInfo.prod_num,
  deliAddr: orderInfo.deliAddr ?? orderInfo.deli_addr,
  recAddr: orderInfo.recAddr ?? orderInfo.rec_addr,
  driverId: orderInfo.driverId ?? orderInfo.driver_id ?? "",
  expectedDeliveryTime:
    orderInfo.expectedDeliveryTime ?? orderInfo.expected_delivery_time,
  payTime: orderInfo.payTime ?? orderInfo.pay_time,
});

export const normalizeOrderItem = (item = {}) => ({
  ...item,
  orderInfo: normalizeOrderInfo(item.orderInfo || {}),
});

export const normalizeOrderItems = (list = []) =>
  Array.isArray(list) ? list.map(normalizeOrderItem) : [];

export const getOrderDriverId = (item) =>
  item?.orderInfo?.driverId ?? item?.orderInfo?.driver_id ?? "";

export const isDriverOwnedOrder = (item, driverId) =>
  Boolean(driverId) && getOrderDriverId(item) === driverId;

export const filterDriverVisibleOrders = (
  list = [],
  driverId,
  matchesDriverServiceArea = () => true,
  waitingState = 3
) =>
  normalizeOrderItems(list).filter((item) => {
    if (isDriverOwnedOrder(item, driverId)) return true;
    if (item?.orderInfo?.state === waitingState) {
      return matchesDriverServiceArea(item);
    }
    return false;
  });
