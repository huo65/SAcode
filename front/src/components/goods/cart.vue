<template>
  <el-drawer
    :model-value="visible"
    title="购物车"
    :before-close="close"
    style="min-width: 400px"
  >
    <div class="wrap">
      <div class="header">
        <el-select
          v-model="receiveAddr"
          placeholder="请选择收货地址"
        >
          <el-option
            v-for="item in userInfo.addr_list"
            :key="item.addrId"
            :label="item.location"
            :value="item.addrId"
          ></el-option>
        </el-select>
        <el-date-picker
          v-model="expectedDeliveryTime"
          type="datetime"
          placeholder="请选择期望送达时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
        <el-input
          v-model="remark"
          type="textarea"
          :rows="2"
          maxlength="200"
          show-word-limit
          placeholder="填写本次结算订单的统一备注"
        />
      </div>
      <div class="content">
        <div v-for="item in cartList" class="product">
          <div class="img-box">
            <img :src="item.firstImage" :alt="item.name" />
          </div>
          <div class="info-box">
            <p class="title">{{ item.name }}</p>
            <p class="mer">来自 {{ item.mer_name }}</p>
            <p class="price">{{ item.price }}￥</p>
            <div class="number-box">
              <el-checkbox v-model="item.checked">已选中</el-checkbox>
              <el-button type="danger" @click="changeItemNum(item, 0)"
                ><el-icon><Delete /></el-icon
              ></el-button>
              <el-button @click="changeItemNum(item, -1)"
                ><el-icon><Minus /></el-icon
              ></el-button>
              <span>{{ item.numberInCart }}</span>
              <el-button @click="changeItemNum(item, 1)"
                ><el-icon><Plus /></el-icon
              ></el-button>
            </div>
          </div>
        </div>
      </div>
      <div class="footer">
        <div>
          合计金额：<span class="price">{{ totalAccount }} ￥</span>
        </div>
        <el-button type="primary" @click="generateOrders"
          >生成订单</el-button
        >
        <el-button @click="toggleSelectAll">全选</el-button>
        <el-button type="danger" @click="clearOut">清空购物车</el-button>
      </div>
    </div>
  </el-drawer>
  <el-dialog v-model="paymentVisible" :before-close="cancelPay">
    <Pay @pay="finishPay" />
  </el-dialog>
</template>

<script setup>
import $store, { userInfo, cartList } from "@/store/index.js";
import { Minus, Plus, Delete } from "@element-plus/icons-vue";
import { ref, computed } from "vue";
import fetch from "@/api/fetch";
import { Alipay, Cart, Order } from "@/api/apis";
import Pay from "./pay.vue";
import { ElMessage } from "element-plus";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(["close", "change"]);
const paymentVisible = ref(false);
const remark = ref("");
const expectedDeliveryTime = ref("");

const close = () => {
  emit("close");
};

const clearOut = () => {
  cartList.value.splice(0, cartList.value.length);
  console.log("clearOut", cartList.value);
  emit("change");
};

let payResolver = null,
  payRejecter = null;

const currentOrderIdList = ref([]);

const finishPay = async (payWay) => {
  if (!payResolver) return;
  if (payWay === "alipay") {
    await startAlipayPayment();
    return;
  }
  fetch(Order.payOrder2, {
    orderIdList: currentOrderIdList.value,
  }).then(() => {
    ElMessage.success("支付成功");
    window.dispatchEvent(new CustomEvent("navigate-orders"));
    payResolver();
    payResolver = null;
    payRejecter = null;
    paymentVisible.value = false;
  });
};

const cancelPay = () => {
  if (!payRejecter) return;
  ElMessage.error("已取消支付");
  payRejecter();
  payRejecter = null;
  payResolver = null;
  paymentVisible.value = false;
};

const getOrderIdsParam = () =>
  [...new Set(currentOrderIdList.value.filter(Boolean))].join(",");

const startAlipayPayment = async () => {
  const orderIds = getOrderIdsParam();
  if (!orderIds) {
    ElMessage.error("没有可支付的订单");
    return;
  }

  const popup = window.open(
    `${Alipay.pay.url}?orderIds=${encodeURIComponent(orderIds)}`,
    "_blank",
    "width=1200,height=800"
  );
  if (!popup) {
    ElMessage.error("请允许弹出支付窗口后重试");
    return;
  }

  const maxAttempts = 120;
  let attempts = 0;
  const timer = window.setInterval(() => {
    attempts += 1;
    fetch(Alipay.check, { orderIds })
      .then((data) => {
        if (data?.paid) {
          window.clearInterval(timer);
          if (!popup.closed) {
            popup.close();
          }
          ElMessage.success("支付成功");
          payResolver?.();
          payResolver = null;
          payRejecter = null;
          paymentVisible.value = false;
        }
      })
      .catch(() => {})
      .finally(() => {
        if (attempts >= maxAttempts) {
          window.clearInterval(timer);
        }
      });
  }, 1500);
};

const generateOrders = () => {
  console.log("generateOrders");
  if (!receiveAddr.value) {
    ElMessage.error("请选择收货地址");
    return;
  }

  const selectedList = [],
    unSelectedList = [];
  cartList.value.forEach((item) => {
    (item.checked ? selectedList : unSelectedList).push({ ...item }); // 浅拷贝
  });
  if (!selectedList.length) {
    ElMessage.error("请选择要结算的商品");
    return;
  }

  const orderList = selectedList.map((item) => ({
    cus: userInfo.value.id,
    mer: item.mer,
    prod: item.id,
    prod_num: item.numberInCart,
    rec_addr: receiveAddr.value,
    state: -1,
    remark: remark.value,
    expected_delivery_time: expectedDeliveryTime.value,
  }));

  fetch(Cart.submitOrderList, { orderList }).then((data) => {
    ElMessage.success("订单已生成");
    currentOrderIdList.value = data || [];
    paymentVisible.value = true;
    new Promise((resolve, reject) => {
      payResolver = resolve;
      payRejecter = reject;
    }).finally(() => {
      $store.commit("updateCartList", unSelectedList);
      remark.value = "";
      expectedDeliveryTime.value = "";
      receiveAddr.value = "";
      emit("change");
    });
  });
};

const changeItemNum = (item, flag) => {
  if (!item) return;
  console.log("@@@", item, flag);
  switch (flag) {
    case 0:
      item.numberInCart = 0;
      console.log(`flag ${flag}`);
      break;
    case -1:
    case 1:
      item.numberInCart += flag;
      console.log(`flag ${flag}`);
      break;

    default:
      console.error("changeItemNum flag error");
      break;
  }

  if (item.numberInCart <= 0) {
    const idx = cartList.value.findIndex((_item) => _item.id === item.id);
    cartList.value.splice(idx, 1);
  }

  emit("change");
};

let receiveAddr = ref("");
let totalAccount = computed(() => {
  return cartList.value.reduce(
    (acc, cur) => acc + (cur.checked ? cur.price * cur.numberInCart : 0),
    0
  );
});

const toggleSelectAll = () => {
  if (cartList.value.some((item) => !item.checked)) {
    cartList.value.forEach((item) => (item.checked = true));
  } else {
    cartList.value.forEach((item) => (item.checked = false));
  }
};
</script>

<style lang="less" scoped>
@import "../../style/theme.less";
.price {
  color: @price;
}

.wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;

  .header {
    margin-bottom: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .content {
    flex: 1;
    overflow: auto;
  }

  .footer {
    div {
      margin: 4px 0;
    }
    background-color: #fff;
  }
}

.product {
  height: 100px;
  margin-bottom: 16px;

  .img-box {
    width: 100px;
    height: 100px;
    float: left;

    img {
      width: 100px;
      height: 100px;
    }
  }

  .info-box {
    position: relative;
    margin-left: 100px;
    padding: 0 16px;
    height: 100%;

    .title {
      font-size: 18px;
    }

    .mer {
      font-size: 14px;
    }

    .number-box {
      position: absolute;
      bottom: 0px;
      right: 0px;
      display: flex;
      align-items: center;
      justify-content: space-around;
      padding: 0 8px;
      width: 100%;
      .el-checkbox {
        float: left;
      }
      button {
        margin: 0;
        padding: 2px;
        width: 20px;
        height: 20px;
      }
    }
  }
}
</style>

<style>
.el-drawer__header {
  margin-bottom: 0;
}
</style>
