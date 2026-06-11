<template>
  <div class="list">
    <div class="header">
      <span
        >state:
        <el-select
          v-model="orderCondition.state"
          clearable
          style="width: 120px"
        >
          <el-option
            v-for="state in stateOptions"
            :label="state.label"
            :value="state.value"
          ></el-option> </el-select
      ></span>
      <span>
        timeOrder:
        <el-button @click="changeTimeOrder">
          {{ orderCondition.timeOrder == 0 ? "ascend" : "descend" }}
        </el-button>
      </span>
      <span>
        <el-button type="primary" @click="getOrderList"
          >Refresh Order List</el-button
        >
      </span>
    </div>

    <div v-for="item in orderList" class="item">
      <div class="content">
        <div class="title">
          <span>Order-{{ item.orderInfo.id }}</span>
          <el-tag :type="stateType[item.orderInfo.state]"
            >state: {{ stateLabel[item.orderInfo.state] }}</el-tag
          >
        </div>
        <div class="info">
          <img class="product-img" :src="item.imgList?.[0]" />
          <el-descriptions class="detail" :column="2" :border="true">
            <el-descriptions-item label-align="center" :span="2">
              <template #label>
                <div>Customer</div>
              </template>                  
              {{ item.cusName }}
            </el-descriptions-item>
            <el-descriptions-item label-align="center" :span="2">
              <template #label>
                <div>Merchant</div>
              </template>
              {{ item.merName }}
            </el-descriptions-item>
            <el-descriptions-item label-align="center">
              <template #label>
                <div>Receive_Addr</div>
              </template>
              {{ item.receive }}
            </el-descriptions-item>
            <el-descriptions-item label-align="center">
              <template #label>
                <div>Deliver_Addr</div>
              </template>
              {{ item.delivery }}
            </el-descriptions-item>

            <el-descriptions-item label-align="center">
              <template #label>
                <div>UpdateTime</div>
              </template>
              {{ item.orderInfo.time }}
            </el-descriptions-item>
            <el-descriptions-item label-align="center">
              <template #label>
                <div>CurrentPlace</div>
              </template>
              {{ genPlace(item) }}
            </el-descriptions-item>
            <el-descriptions-item label-align="center">
              <template #label>
                <div>ProductName</div>
              </template>
              {{ item.productList[0].name }}
            </el-descriptions-item>
            <el-descriptions-item label-align="center">
              <template #label>
                <div>Bill</div>
              </template>
              <span class="price">{{ item.orderInfo.account }}￥</span>
              <span>for {{ item.productList.reduce((sum, product) => sum + Number(product.prodNum || 0), 0) }} item(s)</span>
            </el-descriptions-item>
            <el-descriptions-item label-align="center">
              <template #label>
                <div>Remark</div>
              </template>
              {{ item.orderInfo.remark || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label-align="center">
              <template #label>
                <div>Expected Delivery</div>
              </template>
              {{ item.orderInfo.expectedDeliveryTime || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label-align="center">
              <template #label>
                <div>Refund Reason</div>
              </template>
              {{ item.orderInfo.refundReason || "-" }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div class="footer">
        <el-button
          type="primary"
          v-if="
            item.orderInfo.state == stateEnum.toPay && userInfo.type === 'cus'
          "
          @click="updateOrder(item, 0)"
          >Pay</el-button
        >
        <el-button
          type="primary"
          v-if="
            item.orderInfo.state == stateEnum.toDeliver &&
            userInfo.type === 'mer'
          "
          @click="updateOrder(item, 4)"
          >Accept Order</el-button
        >
        <el-button
          type="danger"
          v-if="
            item.orderInfo.state == stateEnum.toDeliver &&
            userInfo.type === 'mer'
          "
          @click="rejectPaidOrder(item)"
          >Reject Order</el-button
        >
        <!--木吱吱-->
        <el-button
          type="warning"
          v-if="
            item.orderInfo.state == stateEnum.preparing &&
            userInfo.type === 'mer'
          "
          @click="updateOrder(item, 3)"
          >Ready for Driver</el-button
        >
        <el-button
          type="primary"
          v-if="
            item.orderInfo.state == stateEnum.missOrder &&
            userInfo.type === 'driver'
          "
          @click="updateOrder(item, 1)"
          >Take-Order</el-button
        >
        <el-button
          v-if="
            item.orderInfo.state == stateEnum.delivering &&
            userInfo.type === 'cus'
          "
          @click="updateOrder(item, 2)"
          >Receive</el-button
        >
        <el-button
          type="danger"
          v-if="
            item.orderInfo.state == stateEnum.delivering ||
            (item.orderInfo.state == stateEnum.received &&
              userInfo.type === 'cus')
          "
          @click="updateOrder(item, -2)"
          >Ask For Returning</el-button
        >
        <el-button
          v-if="
            item.orderInfo.state == stateEnum.returning &&
            userInfo.type === 'mer'
          "
          @click="updateOrder(item, -3)"
          >Returned</el-button
        >
      </div>
    </div>

    <Detail
      :visible="detailVisible"
      :orderId="curOrder.id"
      :curStatus="'customer'"
      @close="closeDetail"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Order } from "@/api/apis.js";
import fetch from "@/api/fetch.js";
import $store, { userInfo } from "@/store";
import { ElMessage, ElMessageBox } from "element-plus";
import Detail from "../goods/detail.vue";

const orderList = ref([]);
const curOrder = ref({});
const randomPlaces = [
  "Beijing",
  "Shanghai",
  "Guangzhou",
  "Shenzhen",
  "Hangzhou",
  "Chengdu",
];
const orderCondition = reactive({
  state: null,
  timeOrder: 0, // 0正序，1逆序
});

const stateEnum = {
  returned: -3,
  returning: -2,
  toPay: -1,
  toDeliver: 0,
  preparing: 4,
  delivering: 1,
  received: 2,
  missOrder:3,
};
const stateOptions = [
  {
    label: "returned",
    value: -3,
  },
  {
    label: "returning",
    value: -2,
  },
  {
    label: "toPay",
    value: -1,
  },
  {
    label: "toDeliver",
    value: 0,
  },
  {
    label: "preparing",
    value: 4,
  },
  {
    label: "waiting driver",
    value: 3,
  },
  {
    label: "delivering",
    value: 1,
  },
  {
    label: "received",
    value: 2,
  },
];

const stateType = {
  [stateEnum.returned]: "info",
  [stateEnum.returning]: "danger",
  [stateEnum.toPay]: "primary",
  [stateEnum.toDeliver]: "primary",
  [stateEnum.preparing]: "warning",
  [stateEnum.missOrder]: "warning",
  [stateEnum.delivering]: "primary",
  [stateEnum.received]: "success",
};

const stateLabel = Object.keys(stateEnum).reduce((cur, text) => {
  const key = stateEnum[text];
  return { ...cur, [key]: text };
}, {});

console.log("###stateOptions", stateOptions);

const genPlace = (item) => {
  if (item.orderInfo.state === stateEnum.received) {
    return item.receive;
  } else if (item.orderInfo.state === stateEnum.returned) {
    return item.delivery;
  }
  // 随机返回一个地址
  const idx = Math.floor(Math.random() * randomPlaces.length);
  return randomPlaces[idx];
};

const getOrderList = () => {
  fetch(Order.getOrderList, {
    usrId: userInfo.value.id,
    state: orderCondition.state,
    timeOrder: orderCondition.timeOrder,
  }).then((data) => {
    let key = userInfo.value.type;
    if (userInfo.value.type === "admin") {
      key = "allOrder";
    } else if (userInfo.value.type === "driver") {
      key = "driver";
    }
    orderList.value = data[key + "List"];
    console.log(`orderList ${key}List`, data[key + "List"]);
  });
};

const updateOrder = (order, wantedState, extraPayload = {}) => {
  curOrder.value = order.orderInfo;
  console.log("curOrder", curOrder.value, "wantedState", wantedState);
  console.log(
    "@@@curOrder.value.state",
    curOrder.value.state,
    "stateEnum.toPay",
    stateEnum.toPay
  );
  if (curOrder.value.state === stateEnum.toPay) {
    detailVisible.value = true;
    return;
  }

  fetch(Order.updateOrder, {
    id: curOrder.value.id,
    targetState: wantedState,
    ...extraPayload,
  }).then((data) => {
    console.log("after update order", data.order_info);
    getOrderList();
    ElMessage.success("Update Order Successfully");
  });
};

const rejectPaidOrder = async (order) => {
  try {
    const { value } = await ElMessageBox.prompt(
      "Please enter a reject reason",
      "Reject Order",
      {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
        inputPlaceholder: "sold out / store closed / unable to deliver...",
        inputValidator: (input) =>
          input && input.trim() ? true : "Reject reason is required",
      }
    );
    updateOrder(order, -3, {
      refundReason: value.trim(),
    });
  } catch (error) {
    if (error !== "cancel") {
      console.error("rejectPaidOrder error", error);
    }
  }
};

const changeTimeOrder = () => {
  const map = {
    0: 1,
    1: 0,
  };
  orderCondition.timeOrder = map[orderCondition.timeOrder];
};

const detailVisible = ref(false);
const closeDetail = () => {
  detailVisible.value = false;
  getOrderList();
};

const initOrderData = () => {
  getOrderList();
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Order",
    fn: initOrderData,
  });
  initOrderData();
});
</script>

<style lang="less" scoped>
@import "../../style/theme.less";
.header {
  display: flex;
  justify-content: space-between;
}

.list {
  .item {
    margin-top: 20px;
    padding: 10px;
    border: 1px #ccc solid;
    border-radius: 8px;

    .product-img {
      margin: 20px;
    }

    .info {
      display: flex;

      & > img {
        width: 200px;
        height: 200px;
      }

      & > div {
        flex: 1;
      }
    }

    .title {
      display: flex;
      align-items: center;
      margin-top: 10px;
      font-size: 20px;
      font-weight: 700;

      & > span {
        margin-left: 10px;
      }
    }

    .detail {
      margin: 20px;
      .price {
        color: @price;
      }
    }

    .footer {
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
