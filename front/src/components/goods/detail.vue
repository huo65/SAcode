<template>
  <el-dialog
    :model-value="visible"
    :title="paymentVisible ? '支付订单' : '商品详情'"
    :before-close="closeDetail"
    width="500"
  >
    <div v-if="!orderId && !paymentVisible && productInfo">
      <h3>{{ productInfo.name }}</h3>
      <el-carousel :interval="4000">
        <el-carousel-item v-for="img in productInfo.image_list">
          <img
            :src="img"
            :alt="img"
            class="image"
            style="height: 80%; margin: 0 auto"
          />
        </el-carousel-item>
      </el-carousel>
      <el-form label-width="auto" ref="orderFormRef" :model="orderForm">
        <el-form-item label="商品描述">
          {{ productInfo.description }}
        </el-form-item>
        <el-form-item label="价格"> {{ productInfo.price }}￥ </el-form-item>
        <el-form-item label="分类">{{ productInfo.cat_name }}</el-form-item>
        <el-form-item label="商家">{{ productInfo.merName }}</el-form-item>
        <el-form-item label="审核状态">
          <span v-if="productInfo.state === -1">未通过</span>
          <span v-else-if="productInfo.state === 0">待审核</span>
          <span v-else-if="productInfo.state === 1">已通过</span>
        </el-form-item>
        <el-form-item
          v-if="curStatus === 'customer'"
          label="收货地址"
          prop="receiveAddr"
          required
          ><el-select
            v-model="orderForm.receiveAddr"
            placeholder="请选择收货地址"
          >
            <el-option
              v-for="item in userInfo.addr_list"
              :key="item.addrId"
              :label="item.location"
              :value="item.addrId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="curStatus === 'customer'"
          label="购买数量"
          prop="prod_num"
          required
        >
          <el-input-number
            v-model="orderForm.prod_num"
            min="1"
          ></el-input-number>
        </el-form-item>
        <el-form-item v-if="curStatus === 'customer'" label="备注">
          <el-input
            v-model="orderForm.remark"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="例如：少辣、不要香菜、提前联系等"
          />
        </el-form-item>
        <el-form-item v-if="curStatus === 'customer'" label="期望送达">
          <el-date-picker
            v-model="orderForm.expected_delivery_time"
            type="datetime"
            placeholder="请选择期望送达时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <div class="footer">
        <el-button
          v-if="curStatus === 'customer'"
          @click="addToCart(productInfo.id, orderForm.prod_num)"
          >加入购物车
        </el-button>
        <el-button
          v-if="curStatus === 'customer'"
          type="primary"
          @click="buyProduct"
          >立即购买</el-button
        >

        <!-- 审核通过按钮，为管理员且商品状态为待审核（state===0）时才显示 -->
        <template v-if="curStatus === 'admin' && productInfo.state === 0">
          <el-button type="success" @click="passProduct">审核通过</el-button>
          <el-button type="warning" @click="rejectProduct">驳回商品</el-button>
        </template>

        <!-- 管理员或者商品所属商户才能修改, 此处的mer后端接口实际上已经改成了name而不是id -->
        <template v-if="curStatus === 'admin'">
          <el-button type="danger" @click="deleteProduct">删除商品</el-button>
        </template>
        <!-- 商品(拥有商品)修改或删除商品 -->
        <!-- <template
          v-if="
            (curStatus === 'merchant' && userInfo.value.id === productInfo.mer)
          "
        >
         <el-button type="primary" @click="openEditModal">Edit</el-button> 
          <el-button type="danger" @click="deleteProduct">Delete</el-button>
        </template> -->
        <template v-if="curStatus === 'merchant'">
          <el-button type="primary" @click="openEditModal">编辑商品</el-button>
          <el-button type="danger" @click="deleteProduct">删除商品</el-button>
        </template>
      </div>
    </div>

    <div v-else>
      <Pay @pay="payBill" />
      <div class="footer">
        <el-button
          v-if="!orderId"
          type="primary"
          @click="() => (paymentVisible = false)"
          >返回详情</el-button
        >
      </div>
    </div>
  </el-dialog>
</template>
<script setup>
import { ref, reactive } from "vue";
import { userInfo } from "@/store";
import fetch from "@/api/fetch";
import { Alipay, Order, Product } from "@/api/apis";
import { ElMessage } from "element-plus";
import { curStatus } from "../../store";
import Pay from "./pay.vue";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  productInfo: {
    type: Object,
    default: {},
  },
  curStatus: {
    type: String,
    default: false,
  },
  orderId: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["close", "add", "openEdit"]);

/* 购买商品，弹出支付页面 */
const paymentVisible = ref(Boolean(props.orderId));
const toPay = ref(Boolean(props.orderId));

/* 顾客下单 */
const curOrderInfo = ref({});
const orderFormRef = ref(null);
const orderForm = reactive({
  prod_num: 1,
  receiveAddr: "",
  remark: "",
  expected_delivery_time: "",
});
const openEditModal = () => {
  let info = props.productInfo;
  console.log(info);
  emit("openEdit", info);
};
const createOrder = async () => {
  console.log("createOrder cur user", userInfo.value, props.productInfo);

  const params = {
    cus: userInfo.value.id,
    mer: props.productInfo.mer,
    prod: props.productInfo.id,
    prod_num: orderForm.prod_num, // 暂时先买一件
    rec_addr: orderForm.receiveAddr,
    state: -1,
    remark: orderForm.remark,
    expected_delivery_time: orderForm.expected_delivery_time,
  };

  fetch(Order.createOrder, params).then((data) => {
    ElMessage.success("订单创建成功");
    console.log("创建订单成功", data.order_info);
    curOrderInfo.value = { ...curOrderInfo.value, ...data.order_info };
  });
};
const buyProduct = async () => {
  console.log("buy", props.productInfo);
  orderFormRef.value.validate((valid) => {
    if (valid) {
      console.log("@@@valid");
      paymentVisible.value = true;
      // 发送请求生成订单
      if (!toPay.value) {
        createOrder();
        toPay.value = true;
      }
    } else {
      ElMessage.error("请完善订单信息");
    }
  });
};

const getCurrentOrderIds = () =>
  [props.orderId || curOrderInfo.value.id].filter(Boolean).join(",");

const startAlipayPayment = async () => {
  const orderIds = getCurrentOrderIds();
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
          toPay.value = false;
          closeDetail();
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

const payBill = (payWay) => {
  if (payWay === "alipay") {
    startAlipayPayment();
    return;
  }
  fetch(Order.payOrder2, {
    orderIdList: [props.orderId || curOrderInfo.value.id],
  }).then(() => {
    ElMessage.success("支付成功");
    window.dispatchEvent(new CustomEvent("navigate-orders"));
    toPay.value = false;
    closeDetail();
  });
};

/* 管理员或商家删除商品 */
const deleteProduct = () => {
  console.log("delete", props.productInfo);

  fetch(Product.deleteProduct, { id: props.productInfo.id }).then(() => {
    ElMessage.success("商品删除成功");
    closeDetail();
  });
};

// 管理员审核商品
const passProduct = () => {
  console.log("通过审核", props.productInfo);
  fetch(Product.checkProduct, {
    id: props.productInfo.id,
    state: 1,
  }).then(() => {
    ElMessage.success("商品已通过审核");
    closeDetail();
  });
};

const rejectProduct = () => {
  console.log("拒绝审核", props.productInfo);
  fetch(Product.checkProduct, {
    id: props.productInfo.id,
    state: -1,
  }).then(() => {
    ElMessage.success("商品已驳回");
    closeDetail();
  });
};

const closeDetail = () => {
  emit("close");
  // 延迟一会，避免闪屏
  setTimeout(() => {
    curOrderInfo.value = {};
    paymentVisible.value = false;
    if (toPay.value && !props.orderId) {
      ElMessage.error("支付未完成");
      toPay.value = false;
    }
    orderForm.prod_num = 1;
    orderForm.receiveAddr = "";
    orderForm.remark = "";
    orderForm.expected_delivery_time = "";
  }, 300);
};

// 添加到购物车
const addToCart = (id, number) => {
  emit("add", id, number);
};
</script>

<style lang="less">
@import "../../style/theme.less";
.el-dialog {
  border-radius: 8px;

  .qr-code {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  .el-carousel__arrow {
    background-color: @main-yellow;
    &:hover {
      background-color: @light-yellow;
    }
  }

  .el-carousel__item {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .el-carousel__indicator > button {
    background-color: @main-yellow;
  }

  .el-carousel__indicator.is-active > button {
    background-color: @light-yellow;
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
