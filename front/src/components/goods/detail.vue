<template>
  <el-dialog
    :model-value="visible"
    :title="paymentVisible ? 'Payment' : 'Product Detail'"
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
        <el-form-item label="description">
          {{ productInfo.description }}
        </el-form-item>
        <el-form-item label="price"> {{ productInfo.price }}￥ </el-form-item>
        <el-form-item label="category">{{ productInfo.cat_name }}</el-form-item>
        <el-form-item label="merchant">{{ productInfo.merName }}</el-form-item>
        <el-form-item label="state">
          <span v-if="productInfo.state === -1">Not Passed</span>
          <span v-else-if="productInfo.state === 0">Pending review</span>
          <span v-else-if="productInfo.state === 1">Passed</span>
        </el-form-item>
        <el-form-item
          v-if="curStatus === 'customer'"
          label="receive_addr"
          prop="receiveAddr"
          required
          ><el-select
            v-model="orderForm.receiveAddr"
            placeholder="select your receive address"
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
          label="number"
          prop="prod_num"
          required
        >
          <el-input-number
            v-model="orderForm.prod_num"
            min="1"
          ></el-input-number>
        </el-form-item>
        <el-form-item v-if="curStatus === 'customer'" label="remark">
          <el-input
            v-model="orderForm.remark"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="special requests, taste preference, no spicy..."
          />
        </el-form-item>
        <el-form-item v-if="curStatus === 'customer'" label="delivery_time">
          <el-date-picker
            v-model="orderForm.expected_delivery_time"
            type="datetime"
            placeholder="select expected delivery time"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <div class="footer">
        <el-button
          v-if="curStatus === 'customer'"
          @click="addToCart(productInfo.id, orderForm.prod_num)"
          >Add to cart
        </el-button>
        <el-button
          v-if="curStatus === 'customer'"
          type="primary"
          @click="buyProduct"
          >Buy it</el-button
        >

        <!-- 审核通过按钮，为管理员且商品状态为待审核（state===0）时才显示 -->
        <template v-if="curStatus === 'admin' && productInfo.state === 0">
          <el-button type="success" @click="passProduct">Passed</el-button>
          <el-button type="warning" @click="rejectProduct">Reject</el-button>
        </template>

        <!-- 管理员或者商品所属商户才能修改, 此处的mer后端接口实际上已经改成了name而不是id -->
        <template v-if="curStatus === 'admin'">
          <el-button type="danger" @click="deleteProduct">Delete</el-button>
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
          <el-button type="primary" @click="openEditModal">Edit</el-button>
          <el-button type="danger" @click="deleteProduct">Delete</el-button>
        </template>
      </div>
    </div>

    <div v-else>
      <div class="qr-code">
        <span>Scan to Pay</span>
        <el-radio-group v-model="payWay">
          <el-radio value="wechat" size="large">Wechat</el-radio>
          <el-radio value="alipay" size="large">AliPay</el-radio>
          <el-radio value="paypal" size="large">PayPal</el-radio>
        </el-radio-group>
        <img
          :src="payQRCodeUrl"
          @click="payBill"
          style="width: 200px; height: 200px"
        />
      </div>
      <div class="footer">
        <el-button
          v-if="!orderId"
          type="primary"
          @click="() => (paymentVisible = false)"
          >Back to detail</el-button
        >
      </div>
    </div>
  </el-dialog>
</template>
<script setup>
import { ref, reactive, computed } from "vue";
import { userInfo } from "@/store";
import fetch from "@/api/fetch";
import { Order, Product } from "@/api/apis";
import { ElMessage } from "element-plus";
import { curStatus } from "../../store";

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
const payWay = ref("wechat"); // "wechat" | "alibaba" | "paypal"
const payQRCodeUrlMap = {
  wechat: "/img/wechat_QR.png",
  alipay: "/img/alipay_QR.png",
  paypal: "/img/paypal_QR.png",
};
const payQRCodeUrl = computed(() => payQRCodeUrlMap[payWay.value]);

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
    ElMessage.success("Create Order Successfully");
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
      ElMessage.error("please finish the fields");
    }
  });
};
const payBill = () => {
  // if (!toPay.value) return;
  console.log("payBill cur userInfo", userInfo);
  console.log("###Order.payOrder", Order.payOrder);

  // 发送请求支付成功
  fetch(Order.payOrder2, {
    orderIdList: [props.orderId || curOrderInfo.value.id],
  }).then(() => {
    ElMessage.success("Pay the Bill Successfully");
    toPay.value = false;
    closeDetail();
  });
};

/* 管理员或商家删除商品 */
const deleteProduct = () => {
  console.log("delete", props.productInfo);

  fetch(Product.deleteProduct, { id: props.productInfo.id }).then(() => {
    ElMessage.success("Delete the product successfully");
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
    ElMessage.success("Pass the product successfully");
    closeDetail();
  });
};

const rejectProduct = () => {
  console.log("拒绝审核", props.productInfo);
  fetch(Product.checkProduct, {
    id: props.productInfo.id,
    state: -1,
  }).then(() => {
    ElMessage.success("Reject the product successfully");
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
      ElMessage.error("fail to pay the bill");
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
