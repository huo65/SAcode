<template>
  <div class="goods">
    <!-- 搜索条件 -->
    <div class="search">
      <el-form :inline="true">
        <!-- 基于商品分类查询 -->
        <el-form-item :label="t('goods.goodsCategory')">
          <el-select
            v-model="searchCondition.category"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="category in productCategories"
              :label="category.label"
              :value="category.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <!-- 基于商品名查询 -->
        <el-form-item :label="t('goods.goodsName')">
          <el-input
            v-model="searchCondition.name"
            clearable
            style="width: 220px"
          ></el-input>
        </el-form-item>
        <el-form-item :label="t('goods.minPrice')">
          <el-input
            v-model="searchCondition.min_price"
            clearable
            style="width: 120px"
          ></el-input>
        </el-form-item>
        <el-form-item :label="t('goods.maxPrice')">
          <el-input
            v-model="searchCondition.max_price"
            clearable
            style="width: 120px"
          ></el-input>
        </el-form-item>
        <!-- 基于商品状态查询 -->
        <el-form-item :label="t('goods.goodsState')" v-if="curStatus === 'admin'">
          <el-select
            v-model="searchCondition.state"
            clearable
            style="width: 120px"
          >
            <el-option :label="t('goods.notPassed')" :value="-1"></el-option>
            <el-option :label="t('goods.pendingReview')" :value="0"></el-option>
            <el-option :label="t('goods.passed')" :value="1"></el-option> </el-select
        ></el-form-item>

        <!-- 基于商品状态查询 -->
        <el-form-item :label="t('goods.goodsState')" v-if="curStatus === 'admin'">
          <el-select
            v-model="searchCondition.state"
            clearable
            style="width: 120px"
          >
            <el-option :label="t('goods.notPassed')" :value="-1"></el-option>
            <el-option :label="t('goods.pendingReview')" :value="0"></el-option>
            <el-option :label="t('goods.passed')" :value="1"></el-option> </el-select
        ></el-form-item>

        <el-form-item>
          <el-button
            v-if="searchCondition.priceOrder == 0"
            @click="order(1)"
            type="primary"
            >{{ t('goods.lowToHigh') }}</el-button
          >
          <el-button
            v-if="searchCondition.priceOrder == 1"
            @click="order(0)"
            type="primary"
            >{{ t('goods.highToLow') }}</el-button
          >
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="clickSearchGoods"
            ><el-icon><Search /></el-icon>{{ t('goods.search') }}</el-button
          >
        </el-form-item>
        <el-form-item v-if="curStatus === 'merchant'">
          <el-button type="primary" @click="openEditModal"
            ><el-icon><Plus /></el-icon>{{ t('goods.add') }}</el-button
          >
        </el-form-item>
        <el-form-item v-if="curStatus === 'customer'">
          <el-button type="primary" @click="toggleCart"
            ><el-icon><ShoppingCart /></el-icon>{{ t('goods.cart') }}</el-button
          >
        </el-form-item>
      </el-form>
    </div>

    <div class="list">
      <div
        v-for="item in goodsList"
        class="good-wrap"
        @click="openDetail(item)"
      >
        <div class="good-item">
          <img :src="item.image_list[0]" class="image" />
          <!-- <img :src="imageUrlMap[item.image_list[0]]" class="image" /> -->
        </div>
        <div class="info">
          <h3>{{ item.name }}</h3>
          <p>{{ item.description }}</p>
        </div>
        <div class="price">
          <div class="btn-box">
            <!-- <el-button
            v-if="curStatus === 'merchant'"
            type="primary"
            size="small"
            @click.stop="openEditModal(item)"
            >Edit</el-button> -->
            <!-- <el-button size="small" @click="openDetail(item)">Detail</el-button> -->
          </div>
          <!-- <el-button
            v-if="curStatus === 'admin' || curStatus === 'merchant'"
            type="danger"
            size="small"
            style="margin-right: 10px"
            @click.stop=""
            >Delete</el-button
          > -->
          <span>{{ item.price }}￥</span>
        </div>
        <!-- <div class="btn-box">
          <el-button type="primary" @click.stop="">Buy</el-button>
          <el-button>Detail</el-button>
        </div> -->
      </div>
    </div>
    <Detail
      :visible="detailVisible"
      :productInfo="curProduct"
      :curStatus="curStatus"
      @close="closeDetail"
      @add="addToCart"
      @openEdit="openEditModal"
    />

    <Edit
      v-if="editVisible"
      :visible="editVisible"
      :product-info="curEditItem"
      @close="closeEditModal"
    />
    <CartDrawer
      :visible="cartVisible"
      @open="getCartInfo"
      @close="closeCart"
      @change="saveCart"
    />

    <CartDrawer
      :visible="cartVisible"
      @open="getCartInfo"
      @close="closeCart"
      @change="saveCart"
    />
  </div>
</template>

<script setup>
import { goodsCategories, prod_list } from "@/mock";
import { ref, reactive, onMounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { imageUrlMap } from "@/mock";
import { ElMessage } from "element-plus";
import { curStatus } from "@/store";
import Edit from "./edit.vue";
import Detail from "./detail.vue";
import CartDrawer from "./cart.vue";
import $store, { userInfo, productCategories, cartList } from "@/store";
import fetch from "@/api/fetch.js";
import { User, Product, Cart } from "@/api/apis.js";
import { Search, ShoppingCart, Plus } from "@element-plus/icons-vue";

const { t } = useI18n();

/* 查询列表 */
const searchCondition = reactive({
  category: null,
  name: "",
  state: null,
  priceOrder: 0,
});
let goodsList = ref([]);
function order(v) {
  searchCondition.priceOrder = v;
  clickSearchGoods();
}

const clickSearchGoods = () => {
  // TODO: 发送请求
  console.log("搜索条件：", searchCondition);

  fetch(Product.getProductList, {
    id: userInfo.value.id,
    name: searchCondition.name,
    category: searchCondition.category,
    state: searchCondition.state,
    priceOrder: searchCondition.priceOrder,
    min_price: searchCondition.min_price,
    max_price: searchCondition.max_price,
  }).then((data) => {
    console.log(curStatus.value, "##");
    // if(curStatus.value==='customer'||curStatus.value==='guest'){
    //   goodsList.value = data.prod_list.filter(i=>{
    //     return i.state == 1
    //   })
    // }else if(curStatus.value==='merchant'){
    //   goodsList.value = data.prod_list.filter(i=>{
    //     return i.mer == userInfo.value.name || i.state == 1;
    //   })

    // }else{
    //    goodsList.value = data.prod_list
    // }
    goodsList.value = data.prod_list;
  });
};

/* 商品详情 */
const detailVisible = ref(false);
const curProduct = ref(null);
const openDetail = (item) => {
  curProduct.value = item;
  // paymentVisible.value = false;
  detailVisible.value = true;

  console.log("detail", item);
};

const closeDetail = () => {
  detailVisible.value = false;
  clickSearchGoods();
};
/* 商品新增 */
const editVisible = ref(false);
const curEditItem = ref({});
const openEditModal = (item) => {
  curEditItem.value = item;
  detailVisible.value = false;
  editVisible.value = true;
  console.log(curEditItem.value);
};
const closeEditModal = () => {
  editVisible.value = false;
  curEditItem.value = null;
  clickSearchGoods();
};

/* 获取分类信息 */
const getCategories = () => {
  fetch(Product.getCategories).then((data) => {
    console.log("setCategoryList", data);
    const list = data.map((item) => ({
      label: item.name,
      value: item.name,
    }));
    $store.commit("setCategoryList", list);
  });
};

/* 获取当前用户的地址，并添加到个人信息中 */
const getAddrList = () => {
  if (!userInfo.value.id) return;
  fetch(User.getAddress, { id: userInfo.value.id }).then((data) => {
    console.log("获取到的addr_list", data);
    $store.commit("setUserInfo", { ...userInfo.value, addr_list: data }); // 把地址添加到其中
  });
};

/* 购物车 */
const cartVisible = ref(false);

const toggleCart = () => {
  console.log("toggleCart", !cartVisible.value);
  cartVisible.value = !cartVisible.value;
  getCartInfo();
};

const closeCart = () => {
  cartVisible.value = false;
};

const getCartInfo = () => {
  if (!userInfo.value.id) return;
  fetch(Cart.getCart, { id: userInfo.value.id }).then((data) => {
    console.log("获取到的cart_list", data.cartList);

    $store.commit(
      "updateCartList",
      data.cartList.map((item) => ({
        ...item,
        checked:
          cartList.value?.find((_item) => _item.id === item.id)?.checked ??
          false,
      })) || []
    );
  });
};

const saveCart = () => {
  console.log("saveCart");
  const cartListFormated = cartList.value.map((item) => ({
    cus: userInfo.value.id,
    prod: item.id,
    number: item.numberInCart,
  }));
  fetch(Cart.updateCart, {
    cus: userInfo.value.id,
    cartList: cartListFormated,
  }).then(() => {
    getCartInfo();
  });
};

const addToCart = (productId, number) => {
  const cartListFormated = cartList.value.map((item) => ({
    cus: userInfo.value.id,
    prod: item.id,
    number: item.numberInCart,
  }));

  const exsitedItem = cartListFormated.find((item) => item.id === productId);
  if (exsitedItem) {
    exsitedItem.numberInCart += number;
  } else {
    cartListFormated.push({
      cus: userInfo.value.id,
      prod: productId,
      number,
    });
  }

  console.log("addToCart", cartListFormated);
  fetch(Cart.updateCart, {
    cus: userInfo.value.id,
    cartList: cartListFormated,
  }).then(() => {
    getCartInfo();
    ElMessage.success(t('goods.addToCartSuccess'));
  });
};

const initGoodsData = () => {
  getCategories();
  getAddrList();
  clickSearchGoods();
  getCartInfo();
};

/* 钩子 */
onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Goods",
    fn: initGoodsData,
  });
  initGoodsData();
});
</script>

<style lang="less" scoped>
@import "../../style/theme.less";
.search {
  i {
    margin-right: 6px;
  }
}

.list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;

  .good-wrap {
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    margin: 10px;
    padding: 10px 5px;
    width: 30%;
    height: 280px;
    // background-color: #bfa;
    border: 1px #ccc solid;
    border-radius: 6px;
    cursor: pointer;

    &:hover {
      border: 1px @main-yellow solid;
    }

    .good-item {
      display: flex;
      align-items: center;
      .image {
        width: 150px;
        height: 150px;
        margin: 0 auto;
      }
    }

    .info {
      display: flex;
      flex-direction: column;
      flex: 1;
      padding: 0 8px;
      h3 {
        word-break: break-all;
        display: -webkit-box;
        /** 对象作为伸缩盒子模型显示 **/
        -webkit-box-orient: vertical;
        /** 设置或检索伸缩盒对象的子元素的排列方式 **/
        -webkit-line-clamp: 2;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      p {
        word-break: break-all;
        text-overflow: ellipsis;
        display: -webkit-box;
        /** 对象作为伸缩盒子模型显示 **/
        -webkit-box-orient: vertical;
        /** 设置或检索伸缩盒对象的子元素的排列方式 **/
        -webkit-line-clamp: 2;
        /** 显示的行数 **/
        overflow: hidden;
        /** 隐藏超出的内容 **/
        word-wrap: break-word;
        /*英文强制换行*/
      }
    }

    .price {
      margin-left: auto;
      color: @price;
      font-size: 18px;
    }
    .btn-box {
      display: flex;
      justify-content: space-around;
    }
  }
}
</style>
