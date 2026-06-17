<template>
  <div class="order-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">订单管理</h2>
        <p class="page-subtitle">实时跟踪订单状态，快速处理接单、备餐与配送</p>
      </div>
      <div class="page-header-actions">
        <div class="header-search">
          <i class="fas fa-search"></i>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索订单号、顾客..."
            @keyup.enter="getOrderList"
          />
        </div>
        <button class="btn" :class="orderCondition.timeOrder === 0 ? 'btn-primary' : 'btn-outline'" @click="changeTimeOrder">
          <i :class="orderCondition.timeOrder === 0 ? 'fas fa-sort-amount-up' : 'fas fa-sort-amount-down'"></i>
          {{ orderCondition.timeOrder === 0 ? '时间升序' : '时间降序' }}
        </button>
        <button class="btn btn-primary" @click="getOrderList">
          <i class="fas fa-sync-alt"></i> 刷新
        </button>
      </div>
    </div>

    <!-- 状态筛选栏 -->
    <div class="filter-bar card">
      <div class="filter-row">
        <div class="filter-item">
          <label class="filter-label">订单状态</label>
          <select v-model="orderCondition.state" class="filter-select" @change="getOrderList">
            <option :value="null">全部状态</option>
            <option v-for="s in stateOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 订单列表 -->
    <div v-if="orderList.length > 0" class="order-list">
      <div
        v-for="item in displayedOrderList"
        :key="item.orderInfo.id"
        class="order-card"
        :class="{ 'order-timeout': isDispatchTimedOut(item) }"
      >
        <!-- 订单头部 -->
        <div class="order-card-header">
          <div class="order-card-id">
            <i class="fas fa-receipt"></i>
            <span>订单号 {{ item.orderInfo.id }}</span>
          </div>
          <div class="order-card-badges">
            <span class="badge" :class="getStateBadgeClass(item.orderInfo.state)">
              {{ getStateLabel(item.orderInfo.state) }}
            </span>
            <span v-if="isDispatchTimedOut(item)" class="badge badge-danger">派单超时</span>
          </div>
        </div>

        <!-- 订单详情 -->
        <div class="order-card-body">
          <div class="order-card-img">
            <img
              v-if="item.imgList?.[0]"
              :src="resolveImageUrl(item.imgList[0])"
              :alt="item.productList?.[0]?.name || '商品'"
            />
            <div v-else class="order-card-img-placeholder">
              <i class="fas fa-utensils"></i>
            </div>
          </div>
          <div class="order-card-info">
            <div class="info-row">
              <span class="info-label">顾客</span>
              <span class="info-value">{{ item.cusName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">商家</span>
              <span class="info-value">{{ item.merName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">收货地址</span>
              <span class="info-value">{{ item.receive || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">出餐地址</span>
              <span class="info-value">{{ item.delivery || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">更新时间</span>
              <span class="info-value">{{ item.orderInfo.time || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">当前节点</span>
              <span class="info-value">{{ getRouteCurrentPlace(item) }}</span>
            </div>
            <div v-if="item.orderInfo.state === stateEnum.missOrder" class="info-row">
              <span class="info-label">派单时效</span>
              <span class="info-value info-warning">{{ getDispatchStatusText(item) }}</span>
            </div>
            <div v-if="userInfo.type === 'driver'" class="info-row">
              <span class="info-label">配送阶段</span>
              <span class="info-value">{{ getRouteStageText(item) }}</span>
            </div>
            <div v-if="userInfo.type === 'driver'" class="info-row">
              <span class="info-label">预计 / 距离</span>
              <span class="info-value">{{ getEtaText(item) }} / {{ getDistanceText(item) }} km</span>
            </div>
            <div v-if="userInfo.type === 'driver' && isDispatchTimedOut(item)" class="info-row">
              <span class="info-label">重派轮次</span>
              <span class="info-value">第 {{ getRedispatchRound(item) }} 轮优先派发</span>
            </div>
            <div class="info-row">
              <span class="info-label">商品名称</span>
              <span class="info-value">{{ item.productList?.[0]?.name || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">订单金额</span>
              <span class="info-value info-price">¥ {{ item.orderInfo.account }} <span class="info-muted">共 {{ getProductCount(item) }} 件</span></span>
            </div>
            <div v-if="item.orderInfo.remark" class="info-row">
              <span class="info-label">订单备注</span>
              <span class="info-value">{{ item.orderInfo.remark }}</span>
            </div>
            <div v-if="item.orderInfo.expectedDeliveryTime" class="info-row">
              <span class="info-label">期望送达</span>
              <span class="info-value">{{ item.orderInfo.expectedDeliveryTime }}</span>
            </div>
            <div v-if="item.orderInfo.refundReason" class="info-row">
              <span class="info-label">退款原因</span>
              <span class="info-value info-danger">{{ item.orderInfo.refundReason }}</span>
            </div>

            <!-- 售后工单 -->
            <div v-if="getAfterSaleTicket(item)" class="info-row info-block">
              <span class="info-label">售后工单</span>
              <div class="info-value">
                <strong>{{ getAfterSaleTicket(item).type }}</strong>
                <span class="badge" :class="getAfterSaleStatusClass(getAfterSaleTicket(item).status)">{{ getAfterSaleTicket(item).status }}</span>
                <div class="info-remark">{{ getAfterSaleTicket(item).content }}</div>
                <div v-if="getAfterSaleTicket(item).handlerNote" class="info-reply">处理备注：{{ getAfterSaleTicket(item).handlerNote }}</div>
              </div>
            </div>

            <!-- 顾客评价 -->
            <div v-if="item.reviewed" class="info-row info-block">
              <span class="info-label">顾客评价</span>
              <div class="info-value">
                <strong>{{ item.review?.score || 0 }}/5</strong>
                <span>{{ item.review?.content || '-' }}</span>
                <div v-if="item.review?.replyContent" class="info-reply">商家回复：{{ item.review.replyContent }}</div>
              </div>
            </div>

            <!-- 骑手反馈 -->
            <div v-if="userInfo.type === 'driver' && isDriverOwnedOrder(item, userInfo.id)" class="info-row info-block">
              <span class="info-label">配送反馈</span>
              <div class="info-value">
                <strong>{{ getDriverFeedbackText(item) }}</strong>
                <div v-if="getIssueReport(item)" class="info-remark">{{ getIssueReport(item).type }} / {{ getIssueReport(item).status }}<span v-if="getIssueReport(item).note"> - {{ getIssueReport(item).note }}</span></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单操作 -->
        <div class="order-card-actions">
          <!-- 顾客：去支付 -->
          <button
            v-if="item.orderInfo.state == stateEnum.toPay && userInfo.type === 'cus'"
            class="btn btn-primary"
            @click="updateOrder(item, 0)"
          >去支付</button>

          <!-- 商家：开始备餐 / 拒绝接单 -->
          <button
            v-if="item.orderInfo.state == stateEnum.toDeliver && userInfo.type === 'mer'"
            class="btn btn-primary"
            @click="updateOrder(item, 4)"
          >开始备餐</button>
          <button
            v-if="item.orderInfo.state == stateEnum.toDeliver && userInfo.type === 'mer'"
            class="btn btn-danger"
            @click="rejectPaidOrder(item)"
          >拒绝接单</button>

          <!-- 商家：呼叫骑手 / 取消订单 -->
          <button
            v-if="item.orderInfo.state == stateEnum.preparing && userInfo.type === 'mer'"
            class="btn btn-warning"
            @click="updateOrder(item, 3)"
          >呼叫骑手</button>
          <button
            v-if="item.orderInfo.state == stateEnum.preparing && userInfo.type === 'mer'"
            class="btn btn-danger"
            @click="cancelPreparingOrder(item)"
          >取消订单</button>

          <!-- 骑手：立即接单 -->
          <button
            v-if="item.orderInfo.state == stateEnum.missOrder && userInfo.type === 'driver' && isDriverOnline && !isDriverBusy"
            class="btn"
            :class="isDispatchTimedOut(item) ? 'btn-danger' : 'btn-primary'"
            @click="updateOrder(item, 1)"
          >立即接单</button>

          <!-- 骑手：退回待接单 / 上报异常 -->
          <button
            v-if="item.orderInfo.state == stateEnum.delivering && userInfo.type === 'driver' && isDriverOwnedOrder(item, userInfo.id)"
            class="btn btn-danger"
            @click="rejectDriverOrder(item)"
          >退回待接单</button>
          <button
            v-if="item.orderInfo.state == stateEnum.delivering && userInfo.type === 'driver' && isDriverOwnedOrder(item, userInfo.id)"
            class="btn btn-warning"
            @click="openIssueDialog(item)"
          >上报异常</button>

          <!-- 顾客：确认收货 -->
          <button
            v-if="item.orderInfo.state == stateEnum.delivering && userInfo.type === 'cus'"
            class="btn btn-primary"
            @click="updateOrder(item, 2)"
          >确认收货</button>

          <!-- 顾客：评价订单 -->
          <button
            v-if="item.orderInfo.state == stateEnum.received && userInfo.type === 'cus' && !item.reviewed"
            class="btn btn-success"
            @click="openReviewDialog(item)"
          >评价订单</button>

          <!-- 商家：回复评价 -->
          <button
            v-if="item.orderInfo.state == stateEnum.received && userInfo.type === 'mer' && item.reviewed && !item.review?.replyContent"
            class="btn btn-primary"
            @click="openReplyDialog(item)"
          >回复评价</button>

          <!-- 顾客：申请退款 -->
          <button
            v-if="(item.orderInfo.state == stateEnum.delivering || item.orderInfo.state == stateEnum.received) && userInfo.type === 'cus'"
            class="btn btn-danger"
            @click="updateOrder(item, -2)"
          >申请退款</button>

          <!-- 顾客：申请售后 -->
          <button
            v-if="canOpenAfterSale(item)"
            class="btn btn-warning"
            @click="openAfterSaleDialog(item)"
          >申请售后</button>

          <!-- 商家：确认退款 -->
          <button
            v-if="item.orderInfo.state == stateEnum.returning && userInfo.type === 'mer'"
            class="btn btn-danger"
            @click="updateOrder(item, -3)"
          >确认退款</button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon"><i class="fas fa-inbox"></i></div>
      <p class="empty-text">暂无订单数据</p>
      <p class="empty-hint">尝试调整筛选条件</p>
    </div>

    <!-- 详情弹窗 -->
    <Detail
      :visible="detailVisible"
      :orderId="curOrder.id"
      :pay-amount="Number(curOrder.account || 0)"
      :curStatus="'customer'"
      @close="closeDetail"
    />

    <!-- 评价弹窗 -->
    <div v-if="reviewVisible" class="modal-overlay show" @click.self="closeReviewDialog">
      <div class="modal">
        <div class="modal-header">
          <h3>评价订单</h3>
          <button class="modal-close" @click="closeReviewDialog"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">订单号</label>
            <div class="form-value">{{ reviewForm.orderId || '-' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">评分 <span class="required">*</span></label>
            <div class="star-rating">
              <i
                v-for="n in 5"
                :key="n"
                class="fas"
                :class="n <= reviewForm.score ? 'fa-star' : 'fa-star-o'"
                :style="n <= reviewForm.score ? 'color:#F59E0B' : 'color:#D1D5DB'"
                @click="reviewForm.score = n"
              ></i>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">评价内容 <span class="required">*</span></label>
            <textarea
              v-model="reviewForm.content"
              class="form-textarea"
              rows="4"
              maxlength="300"
              placeholder="请输入本次用餐体验"
            ></textarea>
            <div class="form-counter">{{ reviewForm.content.length }}/300</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeReviewDialog">取消</button>
          <button class="btn btn-primary" @click="submitReview">提交评价</button>
        </div>
      </div>
    </div>

    <!-- 回复弹窗 -->
    <div v-if="replyVisible" class="modal-overlay show" @click.self="closeReplyDialog">
      <div class="modal">
        <div class="modal-header">
          <h3>回复评价</h3>
          <button class="modal-close" @click="closeReplyDialog"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">订单号</label>
            <div class="form-value">{{ replyForm.orderId || '-' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">顾客评价</label>
            <div class="form-value">{{ replyForm.reviewContent || '-' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">回复内容 <span class="required">*</span></label>
            <textarea
              v-model="replyForm.replyContent"
              class="form-textarea"
              rows="4"
              maxlength="300"
              placeholder="请输入对顾客评价的回复"
            ></textarea>
            <div class="form-counter">{{ replyForm.replyContent.length }}/300</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeReplyDialog">取消</button>
          <button class="btn btn-primary" @click="submitReply">提交回复</button>
        </div>
      </div>
    </div>

    <!-- 异常上报弹窗 -->
    <div v-if="issueVisible" class="modal-overlay show" @click.self="closeIssueDialog">
      <div class="modal">
        <div class="modal-header">
          <h3>配送异常上报</h3>
          <button class="modal-close" @click="closeIssueDialog"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">订单号</label>
            <div class="form-value">{{ issueForm.orderId || '-' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">异常类型 <span class="required">*</span></label>
            <select v-model="issueForm.type" class="form-select">
              <option value="联系不上顾客">联系不上顾客</option>
              <option value="商家出餐延迟">商家出餐延迟</option>
              <option value="地址定位困难">地址定位困难</option>
              <option value="交通拥堵">交通拥堵</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">异常说明</label>
            <textarea
              v-model="issueForm.note"
              class="form-textarea"
              rows="4"
              maxlength="200"
              placeholder="记录当前配送异常，便于后续跟进处理"
            ></textarea>
            <div class="form-counter">{{ issueForm.note.length }}/200</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeIssueDialog">取消</button>
          <button class="btn btn-primary" @click="submitIssue">提交上报</button>
        </div>
      </div>
    </div>

    <!-- 售后工单弹窗 -->
    <div v-if="afterSaleVisible" class="modal-overlay show" @click.self="closeAfterSaleDialog">
      <div class="modal">
        <div class="modal-header">
          <h3>申请售后工单</h3>
          <button class="modal-close" @click="closeAfterSaleDialog"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">订单号</label>
            <div class="form-value">{{ afterSaleForm.orderId || '-' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">问题类型 <span class="required">*</span></label>
            <select v-model="afterSaleForm.type" class="form-select">
              <option value="投诉反馈">投诉反馈</option>
              <option value="退款问题">退款问题</option>
              <option value="配送问题">配送问题</option>
              <option value="商品问题">商品问题</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">问题描述 <span class="required">*</span></label>
            <textarea
              v-model="afterSaleForm.content"
              class="form-textarea"
              rows="4"
              maxlength="300"
              placeholder="请描述问题现象、期望处理方式和关键细节"
            ></textarea>
            <div class="form-counter">{{ afterSaleForm.content.length }}/300</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeAfterSaleDialog">取消</button>
          <button class="btn btn-primary" @click="submitAfterSale">提交工单</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted, onUnmounted } from "vue";
import { AfterSale, Order, Review } from "@/api/apis.js";
import fetch from "@/api/fetch.js";
import $store, { userInfo } from "@/store";
import { ElMessage, ElMessageBox } from "element-plus";
import Detail from "../goods/detail.vue";
import {
  filterDriverVisibleOrders,
  isDriverOwnedOrder,
  normalizeOrderItems,
} from "@/lib/orderDriverHelper";
import { resolveImageUrl } from "@/lib/imageHelper";

const orderList = ref([]);
const afterSaleMap = ref({});
const curOrder = ref({});
const searchKeyword = ref("");
const orderCondition = reactive({
  state: null,
  timeOrder: 0,
});

const stateEnum = {
  returned: -3,
  returning: -2,
  toPay: -1,
  toDeliver: 0,
  preparing: 4,
  delivering: 1,
  received: 2,
  missOrder: 3,
};
const stateOptions = [
  { label: "已退款", value: -3 },
  { label: "退款中", value: -2 },
  { label: "待支付", value: -1 },
  { label: "待商家处理", value: 0 },
  { label: "备餐中", value: 4 },
  { label: "待骑手接单", value: 3 },
  { label: "配送中", value: 1 },
  { label: "已完成", value: 2 },
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

const stateLabel = {
  [stateEnum.returned]: "已退款",
  [stateEnum.returning]: "退款中",
  [stateEnum.toPay]: "待支付",
  [stateEnum.toDeliver]: "待商家处理",
  [stateEnum.preparing]: "备餐中",
  [stateEnum.missOrder]: "待骑手接单",
  [stateEnum.delivering]: "配送中",
  [stateEnum.received]: "已完成",
};

const isDriverOnline = computed(() => userInfo.value.driverWorkStatus !== "rest");
const isDriverBusy = computed(() =>
  orderList.value.some(
    (item) =>
      item?.orderInfo?.state === stateEnum.delivering &&
      isDriverOwnedOrder(item, userInfo.value.id)
  )
);
const driverServiceArea = computed(() =>
  (userInfo.value.driverServiceArea || "").trim().toLowerCase()
);
const driverIssueReports = computed(() => userInfo.value.driverIssueReports || {});
const afterSaleStatusType = {
  待处理: "danger",
  处理中: "warning",
  已解决: "success",
  已关闭: "info",
};
const DISPATCH_TIMEOUT_MINUTES = 10;

const parseOrderTime = (timeText) => {
  if (!timeText) return null;
  const normalized = String(timeText).replace(" ", "T");
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date;
};

const getDispatchWaitMinutes = (item) => {
  if (item?.orderInfo?.state !== stateEnum.missOrder) return 0;
  const date = parseOrderTime(item?.orderInfo?.time);
  if (!date) return 0;
  return Math.max(0, Math.floor((Date.now() - date.getTime()) / 60000));
};

const isDispatchTimedOut = (item) =>
  getDispatchWaitMinutes(item) >= DISPATCH_TIMEOUT_MINUTES;

const getDispatchStatusText = (item) => {
  const waitMinutes = getDispatchWaitMinutes(item);
  if (waitMinutes >= DISPATCH_TIMEOUT_MINUTES) {
    return `已超时 ${waitMinutes - DISPATCH_TIMEOUT_MINUTES} 分钟`;
  }
  return `剩余 ${DISPATCH_TIMEOUT_MINUTES - waitMinutes} 分钟`;
};

const getRedispatchRound = (item) =>
  Math.max(1, Math.floor(getDispatchWaitMinutes(item) / DISPATCH_TIMEOUT_MINUTES));

const calcDistanceValue = (item) => {
  const source = `${item?.delivery || ""}${item?.receive || ""}${item?.orderInfo?.id || ""}`;
  const total = source.split("").reduce((sum, ch) => sum + ch.charCodeAt(0), 0);
  return (1.2 + (total % 48) / 10).toFixed(1);
};

const getDistanceText = (item) => calcDistanceValue(item);

const getRouteStageText = (item) => {
  const state = item?.orderInfo?.state;
  if (state === stateEnum.missOrder) return "待接单，系统正在派发给合适骑手";
  if (state === stateEnum.delivering) return "已取餐，正在前往顾客地址";
  if (state === stateEnum.received) return "已送达并完成签收";
  if (state === stateEnum.preparing) return "商家备餐中，等待取餐";
  if (state === stateEnum.returned) return "退款完成，配送任务结束";
  return "待处理";
};

const getRouteCurrentPlace = (item) => {
  const state = item?.orderInfo?.state;
  if (state === stateEnum.delivering) {
    return `${item?.delivery || "商家"} → ${item?.receive || "顾客地址"}`;
  }
  if (state === stateEnum.received) {
    return item?.receive || "顾客已签收";
  }
  if (state === stateEnum.missOrder || state === stateEnum.preparing) {
    return item?.delivery || "商家待出餐点";
  }
  if (state === stateEnum.returned) {
    return item?.delivery || "商家侧退款结束";
  }
  return item?.receive || item?.delivery || "-";
};

const getEtaText = (item) => {
  const state = item?.orderInfo?.state;
  if (state === stateEnum.received) return "已送达";
  if (state === stateEnum.delivering) return `约 ${Math.max(6, Math.round(Number(calcDistanceValue(item)) * 4))} 分钟`;
  if (state === stateEnum.missOrder) {
    return isDispatchTimedOut(item) ? "系统优先重派中" : "待骑手接单";
  }
  if (state === stateEnum.preparing) return "待商家出餐";
  return "-";
};

const matchesDriverServiceArea = (item) => {
  if (!driverServiceArea.value) return true;
  if (isDriverOwnedOrder(item, userInfo.value.id)) return true;
  const haystack = [item?.delivery, item?.receive, item?.cusName, item?.merName]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
  return haystack.includes(driverServiceArea.value);
};

const displayedOrderList = computed(() => {
  if (userInfo.value.type !== "driver") return orderList.value;
  return [...filterDriverVisibleOrders(orderList.value, userInfo.value.id, matchesDriverServiceArea, stateEnum.missOrder)]
    .sort((a, b) => {
      const timeoutDiff = Number(isDispatchTimedOut(b)) - Number(isDispatchTimedOut(a));
      if (timeoutDiff !== 0) return timeoutDiff;
      return new Date(b?.orderInfo?.time || 0).getTime() - new Date(a?.orderInfo?.time || 0).getTime();
    });
});

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
    orderList.value = normalizeOrderItems(data[key + "List"] || []);
  });
};

const updateOrder = (order, wantedState, extraPayload = {}) => {
  curOrder.value = order.orderInfo;
  if (curOrder.value.state === stateEnum.toPay) {
    detailVisible.value = true;
    return;
  }
  if (
    userInfo.value.type === "driver" &&
    wantedState === stateEnum.delivering &&
    !isDriverOnline.value
  ) {
    ElMessage.warning("当前为休息状态，请先切换为在线接单。");
    return;
  }
  if (
    userInfo.value.type === "driver" &&
    wantedState === stateEnum.delivering &&
    isDriverBusy.value
  ) {
    ElMessage.warning("当前还有配送中的订单，请先完成后再接新单。");
    return;
  }
  fetch(Order.updateOrder, {
    id: curOrder.value.id,
    targetState: wantedState,
    ...extraPayload,
  }).then(() => {
    getOrderList();
    ElMessage.success("订单状态更新成功");
  });
};

const rejectPaidOrder = async (order) => {
  try {
    const { value } = await ElMessageBox.prompt(
      "请填写拒绝接单原因",
      "拒绝接单",
      {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        inputPlaceholder: "例如：商品售罄、门店暂停营业、无法配送等",
        inputValidator: (input) =>
          input && input.trim() ? true : "请填写拒绝原因",
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

const cancelPreparingOrder = async (order) => {
  try {
    const { value } = await ElMessageBox.prompt(
      "请填写取消订单原因",
      "取消订单",
      {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        inputPlaceholder: "例如：原料不足、后厨异常、门店临时关闭等",
        inputValidator: (input) =>
          input && input.trim() ? true : "请填写取消原因",
      }
    );
    updateOrder(order, -3, {
      refundReason: value.trim(),
    });
  } catch (error) {
    if (error !== "cancel") {
      console.error("cancelPreparingOrder error", error);
    }
  }
};

const rejectDriverOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      "确认将该订单退回待骑手接单池吗？",
      "退回待接单",
      {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        type: "warning",
      }
    );
    updateOrder(order, 3);
  } catch (error) {
    if (error !== "cancel") {
      console.error("rejectDriverOrder error", error);
    }
  }
};

const getIssueReport = (item) =>
  driverIssueReports.value?.[item?.orderInfo?.id] || null;

const getDriverFeedbackText = (item) => {
  const score = Number(item?.review?.score || 0);
  if (score > 0) {
    return `顾客评分 ${score}/5${item?.review?.content ? `，${item.review.content}` : ""}`;
  }
  if (getIssueReport(item)) {
    return "已记录配送异常，待人工跟进";
  }
  return "当前无差评或异常记录";
};

const changeTimeOrder = () => {
  const map = { 0: 1, 1: 0 };
  orderCondition.timeOrder = map[orderCondition.timeOrder];
  getOrderList();
};

const getStateBadgeClass = (state) => {
  const map = {
    [stateEnum.returned]: "badge-info",
    [stateEnum.returning]: "badge-danger",
    [stateEnum.toPay]: "badge-primary",
    [stateEnum.toDeliver]: "badge-primary",
    [stateEnum.preparing]: "badge-warning",
    [stateEnum.missOrder]: "badge-warning",
    [stateEnum.delivering]: "badge-primary",
    [stateEnum.received]: "badge-success",
  };
  return map[state] || "badge-info";
};

const getStateLabel = (state) => stateLabel[state] || "未知";

const getProductCount = (item) =>
  (item.productList || []).reduce((sum, p) => sum + Number(p.prodNum || 0), 0);

const detailVisible = ref(false);
const closeDetail = () => {
  detailVisible.value = false;
  getOrderList();
};

const reviewVisible = ref(false);
const reviewForm = reactive({
  orderId: "",
  score: 5,
  content: "",
});

const openReviewDialog = (item) => {
  reviewForm.orderId = item.orderInfo.id;
  reviewForm.score = 5;
  reviewForm.content = "";
  reviewVisible.value = true;
};

const closeReviewDialog = () => {
  reviewVisible.value = false;
};

const submitReview = () => {
  if (!reviewForm.orderId) {
    ElMessage.error("缺少订单号");
    return;
  }
  if (!reviewForm.score) {
    ElMessage.error("请选择评分");
    return;
  }
  if (!reviewForm.content.trim()) {
    ElMessage.error("请填写评价内容");
    return;
  }
  fetch(Review.add, {
    orderId: reviewForm.orderId,
    score: reviewForm.score,
    content: reviewForm.content.trim(),
  }).then(() => {
    ElMessage.success("评价提交成功");
    closeReviewDialog();
    getOrderList();
  });
};

const loadAfterSaleTickets = () => {
  if (!userInfo.value?.type || userInfo.value.type === "driver") {
    afterSaleMap.value = {};
    return;
  }
  const scopeMap = {
    cus: "customer",
    mer: "merchant",
    admin: "admin",
  };
  fetch(AfterSale.list, {
    scope: scopeMap[userInfo.value.type],
  }).then((data) => {
    const nextMap = {};
    (data?.ticketList || []).forEach((ticket) => {
      nextMap[ticket.orderId] = ticket;
    });
    afterSaleMap.value = nextMap;
  });
};

const getAfterSaleTicket = (item) => afterSaleMap.value?.[item?.orderInfo?.id] || null;

const canOpenAfterSale = (item) => {
  if (getAfterSaleTicket(item)) return false;
  const state = item?.orderInfo?.state;
  return [stateEnum.delivering, stateEnum.received, stateEnum.returning, stateEnum.returned].includes(state);
};

const getAfterSaleStatusClass = (status) => {
  const map = {
    待处理: "badge-danger",
    处理中: "badge-warning",
    已解决: "badge-success",
    已关闭: "badge-info",
  };
  return map[status] || "badge-info";
};

const replyVisible = ref(false);
const replyForm = reactive({
  orderId: "",
  reviewContent: "",
  replyContent: "",
});

const openReplyDialog = (item) => {
  replyForm.orderId = item.orderInfo.id;
  replyForm.reviewContent = item.review?.content || "";
  replyForm.replyContent = "";
  replyVisible.value = true;
};

const closeReplyDialog = () => {
  replyVisible.value = false;
};

const submitReply = () => {
  if (!replyForm.orderId) {
    ElMessage.error("缺少订单号");
    return;
  }
  if (!replyForm.replyContent.trim()) {
    ElMessage.error("请填写回复内容");
    return;
  }
  fetch(Review.reply, {
    orderId: replyForm.orderId,
    replyContent: replyForm.replyContent.trim(),
  }).then(() => {
    ElMessage.success("回复提交成功");
    closeReplyDialog();
    getOrderList();
  });
};

const issueVisible = ref(false);
const issueForm = reactive({
  orderId: "",
  type: "联系不上顾客",
  note: "",
});

const openIssueDialog = (item) => {
  issueForm.orderId = item?.orderInfo?.id || "";
  issueForm.type = "联系不上顾客";
  issueForm.note = "";
  issueVisible.value = true;
};

const closeIssueDialog = () => {
  issueVisible.value = false;
};

const submitIssue = () => {
  if (!issueForm.orderId) {
    ElMessage.error("缺少订单号");
    return;
  }
  const nextReports = {
    ...driverIssueReports.value,
    [issueForm.orderId]: {
      type: issueForm.type,
      note: issueForm.note.trim(),
      status: "已上报",
      time: new Date().toISOString(),
    },
  };
  $store.commit("patchUserInfo", {
    driverIssueReports: nextReports,
  });
  ElMessage.success("配送异常已记录");
  closeIssueDialog();
};

const afterSaleVisible = ref(false);
const afterSaleForm = reactive({
  orderId: "",
  type: "投诉反馈",
  content: "",
});

const openAfterSaleDialog = (item) => {
  afterSaleForm.orderId = item?.orderInfo?.id || "";
  afterSaleForm.type = "投诉反馈";
  afterSaleForm.content = "";
  afterSaleVisible.value = true;
};

const closeAfterSaleDialog = () => {
  afterSaleVisible.value = false;
};

const submitAfterSale = () => {
  if (!afterSaleForm.orderId) {
    ElMessage.error("缺少订单号");
    return;
  }
  if (!afterSaleForm.content.trim()) {
    ElMessage.error("请填写问题描述");
    return;
  }
  fetch(AfterSale.create, {
    orderId: afterSaleForm.orderId,
    type: afterSaleForm.type,
    content: afterSaleForm.content.trim(),
  }).then(() => {
    ElMessage.success("售后工单已提交");
    closeAfterSaleDialog();
    loadAfterSaleTickets();
    getOrderList();
  });
};

const initOrderData = () => {
  getOrderList();
  loadAfterSaleTickets();
};

let driverOrderTimer = null;

const startDriverAutoRefresh = () => {
  if (userInfo.value.type !== "driver" || driverOrderTimer) return;
  driverOrderTimer = window.setInterval(() => {
    if (document.hidden) return;
    getOrderList();
  }, 10000);
};

const stopDriverAutoRefresh = () => {
  if (!driverOrderTimer) return;
  window.clearInterval(driverOrderTimer);
  driverOrderTimer = null;
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Order",
    fn: initOrderData,
  });
  initOrderData();
  startDriverAutoRefresh();
});

onUnmounted(() => {
  stopDriverAutoRefresh();
});
</script>

<style lang="less" scoped>
@import "@/style/theme.less";

.order-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== 页面标题区 ===== */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}
.page-header-left {
  flex: 1;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}
.page-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}
.page-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.header-search {
  position: relative;
  i {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-muted);
    font-size: 13px;
  }
  input {
    width: 220px;
    height: 38px;
    border: 1px solid var(--border);
    border-radius: 20px;
    padding: 0 14px 0 36px;
    font-size: 13px;
    background: var(--bg);
    color: var(--text-primary);
    outline: none;
    transition: all 0.2s ease;
    &:focus {
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.1);
    }
  }
}

/* ===== 通用按钮 ===== */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  outline: none;
  white-space: nowrap;
  background: var(--card);
  color: var(--text-secondary);
  &:hover {
    border-color: var(--primary);
    color: var(--primary);
  }
}
.btn-primary {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
  &:hover {
    background: var(--primary-hover);
    color: white;
    box-shadow: 0 4px 12px rgba(232, 101, 43, 0.3);
  }
}
.btn-outline {
  background: transparent;
  color: var(--text-secondary);
  border-color: var(--border);
  &:hover {
    border-color: var(--primary);
    color: var(--primary);
  }
}
.btn-success {
  background: var(--success);
  color: white;
  border-color: var(--success);
  &:hover {
    background: darken(#00B894, 8%);
    color: white;
  }
}
.btn-warning {
  background: var(--warning);
  color: #92600A;
  border-color: var(--warning);
  &:hover {
    background: darken(#FDCB6E, 8%);
    color: #92600A;
  }
}
.btn-danger {
  background: var(--danger);
  color: white;
  border-color: var(--danger);
  &:hover {
    background: darken(#E17055, 8%);
    color: white;
  }
}

/* ===== 筛选栏 ===== */
.filter-bar {
  background: var(--card);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 18px 22px;
}
.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}
.filter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
}
.filter-select {
  height: 38px;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0 12px;
  font-size: 13px;
  background: var(--bg);
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s ease;
  min-width: 180px;
  &:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.08);
  }
}

/* ===== 订单列表 ===== */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.order-card {
  background: var(--card);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  overflow: hidden;
  transition: all 0.2s ease;
  &:hover {
    box-shadow: var(--shadow-md);
    border-color: var(--primary-light);
  }
}
.order-timeout {
  border-color: fade(#E17055, 36%);
  box-shadow: 0 0 0 1px fade(#E17055, 12%), var(--shadow-soft);
}
.order-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: var(--bg);
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
  gap: 8px;
}
.order-card-id {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  i {
    color: var(--primary);
    font-size: 15px;
  }
}
.order-card-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}
.order-card-body {
  display: flex;
  gap: 20px;
  padding: 18px 20px;
  flex-wrap: wrap;
}
.order-card-img {
  width: 140px;
  height: 140px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, #ffecd2, #fcb69f);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
.order-card-img-placeholder {
  font-size: 32px;
  color: var(--primary);
  opacity: 0.5;
}
.order-card-info {
  flex: 1;
  min-width: 240px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.info-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  font-size: 13px;
  line-height: 1.5;
}
.info-label {
  color: var(--text-muted);
  font-weight: 500;
  min-width: 80px;
  flex-shrink: 0;
}
.info-value {
  color: var(--text-primary);
  font-weight: 500;
}
.info-price {
  color: var(--price, var(--primary));
  font-size: 15px;
  font-weight: 700;
}
.info-muted {
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 400;
}
.info-warning {
  color: var(--warning-dark, #92600A);
}
.info-danger {
  color: var(--danger);
}
.info-block {
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
  background: var(--bg);
  border-radius: 8px;
  .info-label {
    min-width: auto;
  }
}
.info-remark {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  line-height: 1.5;
}
.info-reply {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
  padding: 6px 10px;
  background: var(--card);
  border-radius: 6px;
  line-height: 1.5;
}

/* ===== 徽标 ===== */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.badge-primary {
  background: var(--primary-light);
  color: var(--primary);
}
.badge-success {
  background: var(--success-light);
  color: #065F46;
}
.badge-warning {
  background: var(--warning-light);
  color: #92600A;
}
.badge-danger {
  background: var(--danger-light);
  color: #991B1B;
}
.badge-info {
  background: var(--info-light);
  color: #075CAA;
}

/* ===== 订单操作区 ===== */
.order-card-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid var(--border);
  flex-wrap: wrap;
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-muted);
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.4;
  }
  .empty-text {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-secondary);
    margin: 0 0 4px;
  }
  .empty-hint {
    font-size: 13px;
    margin: 0;
  }
}

/* ===== 弹窗 ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
}
.modal-overlay.show {
  display: flex;
}
.modal {
  background: var(--card);
  border-radius: 16px;
  width: 520px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
}
.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  h3 {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0;
  }
}
.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: var(--bg);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: all 0.2s ease;
  &:hover {
    background: var(--danger-light);
    color: var(--danger);
  }
}
.modal-body {
  padding: 24px;
}
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

/* ===== 表单 ===== */
.form-group {
  margin-bottom: 18px;
}
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.required {
  color: var(--danger);
}
.form-value {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 6px 0;
}
.form-select {
  width: 100%;
  height: 38px;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0 12px;
  font-size: 13px;
  background: var(--bg);
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s ease;
  &:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.08);
  }
}
.form-textarea {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--bg);
  outline: none;
  resize: vertical;
  transition: all 0.2s ease;
  box-sizing: border-box;
  &:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(232, 101, 43, 0.08);
  }
}
.form-counter {
  text-align: right;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}
.star-rating {
  display: flex;
  gap: 6px;
  font-size: 22px;
  cursor: pointer;
  i {
    transition: all 0.15s ease;
    &:hover {
      transform: scale(1.15);
    }
  }
}

/* ===== 响应式 ===== */
@media (max-width: 960px) {
  .page-header {
    flex-direction: column;
  }
  .page-header-actions {
    width: 100%;
    .header-search input {
      width: 100%;
    }
  }
  .order-card-body {
    flex-direction: column;
  }
  .order-card-img {
    width: 100%;
    height: 180px;
  }
}
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
  }
  .filter-item {
    width: 100%;
  }
  .filter-select {
    width: 100%;
  }
  .order-card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .modal {
    width: calc(100vw - 32px);
    margin: 16px;
  }
}
</style>
