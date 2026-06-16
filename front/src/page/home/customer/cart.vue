<template>
  <div class="customer-cart">
    <!-- 页头 -->
    <div class="cart-header">
      <h3>购物车</h3>
      <el-button text type="danger" size="small" @click="clearCart" v-if="cartItems.length">
        清空购物车
      </el-button>
    </div>

    <!-- 购物车列表 -->
    <div class="cart-items" v-if="cartItems.length">
      <div class="cart-item" v-for="item in cartItems" :key="item.id || item.cartId">
        <div class="cart-item-img">
          <img v-if="item.image" :src="item.image" alt="" />
          <div v-else class="img-placeholder">🍽️</div>
        </div>
        <div class="cart-item-info">
          <div class="cart-item-name">{{ item.name || item.goodsName }}</div>
          <div class="cart-item-price">¥{{ item.price || item.unitPrice }}</div>
        </div>
        <div class="cart-item-qty">
          <button class="qty-btn minus" @click="changeQty(item, -1)">−</button>
          <span class="qty-num">{{ item.num || item.quantity || 1 }}</span>
          <button class="qty-btn plus" @click="changeQty(item, 1)">+</button>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-else>
      <i class="fas fa-shopping-cart"></i>
      <span>购物车空空如也</span>
      <el-button type="primary" round @click="$router.push('/home/customer/restaurants')">去点餐</el-button>
    </div>

    <!-- 结算汇总 -->
    <div class="cart-summary" v-if="cartItems.length">
      <div class="summary-row">
        <span>菜品小计</span>
        <span>¥{{ subtotal.toFixed(2) }}</span>
      </div>
      <div class="summary-row">
        <span>配送费</span>
        <span>¥{{ deliveryFee.toFixed(2) }}</span>
      </div>
      <div class="summary-row total">
        <span>合计</span>
        <span class="price">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 提交订单 -->
    <div class="checkout-area" v-if="cartItems.length">
      <el-button
        class="checkout-btn"
        type="primary"
        round
        :loading="submitting"
        @click="submitOrder"
      >
        提交订单 · ¥{{ totalAmount.toFixed(2) }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userInfo } from '@/store';
import { Cart as CartApi, Order as OrderApi } from '@/api/apis';
import fetch from '@/api/fetch';

const router = useRouter();
const cartItems = ref([]);
const submitting = ref(false);
const deliveryFee = 5;

const subtotal = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    const price = Number(item.price || item.unitPrice || 0);
    const qty = Number(item.num || item.quantity || 1);
    return sum + price * qty;
  }, 0);
});

const totalAmount = computed(() => subtotal.value + deliveryFee);

const loadCart = () => {
  if (!userInfo.value?.id) return;
  fetch(CartApi.getCart, { usrId: userInfo.value.id }).then((data) => {
    cartItems.value = data?.data || data?.items || (Array.isArray(data) ? data : []);
  }).catch(() => {
    cartItems.value = [];
  });
};

const changeQty = (item, delta) => {
  const currentQty = Number(item.num || item.quantity || 1);
  const newQty = currentQty + delta;
  if (newQty <= 0) {
    removeItem(item);
    return;
  }
  // 乐观更新
  if (item.num !== undefined) item.num = newQty;
  else item.quantity = newQty;

  fetch(CartApi.updateCart, {
    usrId: userInfo.value.id,
    goodsId: item.id || item.goodsId,
    num: newQty,
  }).catch(() => {
    // 回滚
    if (item.num !== undefined) item.num = currentQty;
    else item.quantity = currentQty;
  });
};

const removeItem = (item) => {
  ElMessageBox.confirm('确定移除该商品吗？', '提示', { type: 'warning' })
    .then(() => {
      fetch(CartApi.updateCart, {
        usrId: userInfo.value.id,
        goodsId: item.id || item.goodsId,
        num: 0,
      }).then(() => {
        cartItems.value = cartItems.value.filter(
          (i) => (i.id || i.goodsId) !== (item.id || item.goodsId)
        );
      });
    })
    .catch(() => {});
};

const clearCart = () => {
  ElMessageBox.confirm('确定清空购物车吗？', '提示', { type: 'warning' })
    .then(() => {
      // 逐项删除
      const promises = cartItems.value.map((item) =>
        fetch(CartApi.updateCart, {
          usrId: userInfo.value.id,
          goodsId: item.id || item.goodsId,
          num: 0,
        })
      );
      Promise.all(promises).then(() => {
        cartItems.value = [];
        ElMessage.success('购物车已清空');
      });
    })
    .catch(() => {});
};

const submitOrder = () => {
  submitting.value = true;
  fetch(CartApi.submitOrderList, {
    usrId: userInfo.value.id,
  })
    .then((data) => {
      ElMessage.success('下单成功！');
      cartItems.value = [];
      router.push('/home/customer/orders');
    })
    .catch((err) => {
      ElMessage.error(err?.message || '下单失败，请重试');
    })
    .finally(() => {
      submitting.value = false;
    });
};

onMounted(() => {
  loadCart();
});
</script>

<style lang="less" scoped>
.customer-cart {
  padding: 0 0 24px;
}

.cart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0 20px;

  h3 {
    margin: 0;
    font-size: 22px;
    font-family: "Georgia", "Times New Roman", serif;
    color: var(--text-primary, #1F2937);
  }
}

// ============ 购物车列表 ============
.cart-items {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: var(--card, #fff);
  border-radius: 14px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  }
}

.cart-item-img {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  font-size: 22px;
  opacity: 0.6;
}

.cart-item-info {
  flex: 1;
  min-width: 0;
}

.cart-item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1F2937);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-item-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary, #EF4444);
  margin-top: 4px;
}

.cart-item-qty {
  display: flex;
  align-items: center;
  gap: 10px;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1.5px solid var(--border, #F0F0F0);
  background: var(--card, #fff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  color: var(--text-secondary, #6B7280);
  font-weight: 600;
  transition: all 0.15s ease;

  &.plus {
    background: var(--primary, #EF4444);
    border-color: var(--primary, #EF4444);
    color: white;
  }

  &:hover {
    transform: scale(1.1);
  }
}

.qty-num {
  font-size: 16px;
  font-weight: 700;
  min-width: 20px;
  text-align: center;
}

// ============ 汇总 ============
.cart-summary {
  background: var(--card, #fff);
  border-radius: 14px;
  padding: 18px 20px;
  border: 1px solid var(--border, #F0F0F0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  padding: 4px 0;

  &.total {
    font-size: 16px;
    font-weight: 800;
    color: var(--text-primary, #1F2937);
    padding-top: 12px;
    margin-top: 10px;
    border-top: 1px dashed var(--border, #F0F0F0);

    .price {
      color: var(--primary, #EF4444);
    }
  }
}

// ============ 结算按钮 ============
.checkout-area {
  text-align: center;
  padding: 8px 0;
}

.checkout-btn {
  width: 100%;
  max-width: 400px;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary, #EF4444), var(--accent, #F97316)) !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(239, 68, 68, 0.3);
}

// ============ 空态 ============
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 0;
  color: var(--text-muted, #9CA3AF);
  font-size: 14px;

  i {
    font-size: 48px;
    opacity: 0.3;
  }
}
</style>
