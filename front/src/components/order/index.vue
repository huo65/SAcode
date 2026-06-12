<template>
  <div class="list">
    <section class="header glass-panel">
      <div class="section-heading">
        <div>
          <span class="micro-tag">Order Timeline</span>
          <h3>订单状态、流转节点与异常反馈</h3>
          <p>以卡片化订单时间轴替代生硬表单组合，强化状态标签、配送节点与售后信息的可扫描性。</p>
        </div>
        <div class="header-actions">
          <el-button @click="changeTimeOrder">
            {{ orderCondition.timeOrder == 0 ? "按时间升序" : "按时间降序" }}
          </el-button>
          <el-button type="primary" @click="getOrderList">刷新订单列表</el-button>
        </div>
      </div>

      <div class="header-filter">
        <span>状态筛选</span>
        <el-select v-model="orderCondition.state" clearable style="width: 180px">
          <el-option
            v-for="state in stateOptions"
            :key="state.value"
            :label="state.label"
            :value="state.value"
          ></el-option>
        </el-select>
      </div>
    </section>

    <div
      v-for="item in displayedOrderList"
      :key="item.orderInfo.id"
      class="item"
      :class="{ 'item-timeout': isDispatchTimedOut(item) }"
    >
      <div class="content">
        <div class="title">
          <span>Order-{{ item.orderInfo.id }}</span>
          <el-tag :type="stateType[item.orderInfo.state]"
            >state: {{ stateLabel[item.orderInfo.state] }}</el-tag
          >
          <el-tag
            v-if="isDispatchTimedOut(item)"
            type="danger"
          >
            dispatch timeout
          </el-tag>
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
              {{ getRouteCurrentPlace(item) }}
            </el-descriptions-item>
            <el-descriptions-item
              v-if="item.orderInfo.state === stateEnum.missOrder"
              label-align="center"
            >
              <template #label>
                <div>Dispatch SLA</div>
              </template>
              {{ getDispatchStatusText(item) }}
            </el-descriptions-item>
            <el-descriptions-item
              v-if="userInfo.type === 'driver'"
              label-align="center"
            >
              <template #label>
                <div>Route Stage</div>
              </template>
              {{ getRouteStageText(item) }}
            </el-descriptions-item>
            <el-descriptions-item
              v-if="userInfo.type === 'driver'"
              label-align="center"
            >
              <template #label>
                <div>ETA / Distance</div>
              </template>
              {{ getEtaText(item) }} / {{ getDistanceText(item) }}
            </el-descriptions-item>
            <el-descriptions-item
              v-if="userInfo.type === 'driver' && isDispatchTimedOut(item)"
              label-align="center"
            >
              <template #label>
                <div>Redispatch</div>
              </template>
              第 {{ getRedispatchRound(item) }} 轮优先派发
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
            <el-descriptions-item
              v-if="getAfterSaleTicket(item)"
              label-align="center"
              :span="2"
            >
              <template #label>
                <div>After-Sale</div>
              </template>
              <div class="review-block">
                <div>
                  <strong>{{ getAfterSaleTicket(item).type }}</strong>
                  <el-tag
                    size="small"
                    :type="afterSaleStatusType[getAfterSaleTicket(item).status] || 'info'"
                    style="margin-left: 8px"
                  >
                    {{ getAfterSaleTicket(item).status }}
                  </el-tag>
                </div>
                <div class="reply-text">
                  {{ getAfterSaleTicket(item).content }}
                </div>
                <div v-if="getAfterSaleTicket(item).handlerNote" class="reply-text">
                  处理备注: {{ getAfterSaleTicket(item).handlerNote }}
                </div>
              </div>
            </el-descriptions-item>
            <el-descriptions-item
              v-if="item.reviewed"
              label-align="center"
              :span="2"
            >
              <template #label>
                <div>Review</div>
              </template>
              <div class="review-block">
                <div>
                  <strong>{{ item.review?.score || 0 }}/5</strong>
                  <span>{{ item.review?.content || "-" }}</span>
                </div>
                <div v-if="item.review?.replyContent" class="reply-text">
                  Reply: {{ item.review.replyContent }}
                </div>
              </div>
            </el-descriptions-item>
            <el-descriptions-item
              v-if="userInfo.type === 'driver' && item.orderInfo.driverId === userInfo.id"
              label-align="center"
              :span="2"
            >
              <template #label>
                <div>Delivery Feedback</div>
              </template>
              <div class="review-block">
                <div>
                  <strong>{{ getDriverFeedbackText(item) }}</strong>
                </div>
                <div v-if="getIssueReport(item)" class="reply-text">
                  异常上报: {{ getIssueReport(item).type }} / {{ getIssueReport(item).status }}
                  <span v-if="getIssueReport(item).note"> - {{ getIssueReport(item).note }}</span>
                </div>
              </div>
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
          type="danger"
          v-if="
            item.orderInfo.state == stateEnum.preparing &&
            userInfo.type === 'mer'
          "
          @click="cancelPreparingOrder(item)"
          >Cancel Order</el-button
        >
        <el-button
          :type="isDispatchTimedOut(item) ? 'danger' : 'primary'"
          v-if="
            item.orderInfo.state == stateEnum.missOrder &&
            userInfo.type === 'driver' &&
            isDriverOnline &&
            !isDriverBusy
          "
          @click="updateOrder(item, 1)"
          >Take-Order</el-button
        >
        <el-button
          type="danger"
          v-if="
            item.orderInfo.state == stateEnum.delivering &&
            userInfo.type === 'driver' &&
            item.orderInfo.driverId === userInfo.id
          "
          @click="rejectDriverOrder(item)"
          >Reject Order</el-button
        >
        <el-button
          type="warning"
          v-if="
            item.orderInfo.state == stateEnum.delivering &&
            userInfo.type === 'driver' &&
            item.orderInfo.driverId === userInfo.id
          "
          @click="openIssueDialog(item)"
          >Report Issue</el-button
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
          type="success"
          v-if="
            item.orderInfo.state == stateEnum.received &&
            userInfo.type === 'cus' &&
            !item.reviewed
          "
          @click="openReviewDialog(item)"
          >Review</el-button
        >
        <el-button
          type="primary"
          v-if="
            item.orderInfo.state == stateEnum.received &&
            userInfo.type === 'mer' &&
            item.reviewed &&
            !item.review?.replyContent
          "
          @click="openReplyDialog(item)"
          >Reply Review</el-button
        >
        <el-button
          type="danger"
          v-if="
            (item.orderInfo.state == stateEnum.delivering ||
              item.orderInfo.state == stateEnum.received) &&
            userInfo.type === 'cus'
          "
          @click="updateOrder(item, -2)"
          >Ask For Returning</el-button
        >
        <el-button
          type="warning"
          v-if="
            userInfo.type === 'cus' &&
            canOpenAfterSale(item)
          "
          @click="openAfterSaleDialog(item)"
          >After-Sale</el-button
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

    <el-dialog
      :model-value="reviewVisible"
      title="Review Order"
      width="520px"
      @close="closeReviewDialog"
    >
      <el-form label-width="110px">
        <el-form-item label="Order">
          <span>{{ reviewForm.orderId || "-" }}</span>
        </el-form-item>
        <el-form-item label="Score" required>
          <el-rate v-model="reviewForm.score" />
        </el-form-item>
        <el-form-item label="Content" required>
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="share your dining experience"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeReviewDialog">Cancel</el-button>
        <el-button type="primary" @click="submitReview">Submit Review</el-button>
      </template>
    </el-dialog>

    <el-dialog
      :model-value="replyVisible"
      title="Reply Review"
      width="520px"
      @close="closeReplyDialog"
    >
      <el-form label-width="110px">
        <el-form-item label="Order">
          <span>{{ replyForm.orderId || "-" }}</span>
        </el-form-item>
        <el-form-item label="Review">
          <span>{{ replyForm.reviewContent || "-" }}</span>
        </el-form-item>
        <el-form-item label="Reply" required>
          <el-input
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="reply to customer feedback"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeReplyDialog">Cancel</el-button>
        <el-button type="primary" @click="submitReply">Submit Reply</el-button>
      </template>
    </el-dialog>

    <el-dialog
      :model-value="issueVisible"
      title="配送异常上报"
      width="520px"
      @close="closeIssueDialog"
    >
      <el-form label-width="110px">
        <el-form-item label="Order">
          <span>{{ issueForm.orderId || "-" }}</span>
        </el-form-item>
        <el-form-item label="Type" required>
          <el-select v-model="issueForm.type" style="width: 100%">
            <el-option label="联系不上顾客" value="联系不上顾客" />
            <el-option label="商家出餐延迟" value="商家出餐延迟" />
            <el-option label="地址定位困难" value="地址定位困难" />
            <el-option label="交通拥堵" value="交通拥堵" />
          </el-select>
        </el-form-item>
        <el-form-item label="Note">
          <el-input
            v-model="issueForm.note"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="记录当前配送异常，课堂展示版会保存在本地会话中"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeIssueDialog">Cancel</el-button>
        <el-button type="primary" @click="submitIssue">Submit</el-button>
      </template>
    </el-dialog>

    <el-dialog
      :model-value="afterSaleVisible"
      title="申请售后工单"
      width="540px"
      @close="closeAfterSaleDialog"
    >
      <el-form label-width="110px">
        <el-form-item label="Order">
          <span>{{ afterSaleForm.orderId || "-" }}</span>
        </el-form-item>
        <el-form-item label="Type" required>
          <el-select v-model="afterSaleForm.type" style="width: 100%">
            <el-option label="投诉反馈" value="投诉反馈" />
            <el-option label="退款问题" value="退款问题" />
            <el-option label="配送问题" value="配送问题" />
            <el-option label="商品问题" value="商品问题" />
          </el-select>
        </el-form-item>
        <el-form-item label="Description" required>
          <el-input
            v-model="afterSaleForm.content"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="请描述问题现象、期望处理方式和关键细节"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeAfterSaleDialog">Cancel</el-button>
        <el-button type="primary" @click="submitAfterSale">Submit Ticket</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from "vue";
import { AfterSale, Order, Review } from "@/api/apis.js";
import fetch from "@/api/fetch.js";
import $store, { userInfo } from "@/store";
import { ElMessage, ElMessageBox } from "element-plus";
import Detail from "../goods/detail.vue";

const orderList = ref([]);
const afterSaleMap = ref({});
const curOrder = ref({});
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
const isDriverOnline = computed(
  () => userInfo.value.driverWorkStatus !== "rest"
);
const isDriverBusy = computed(() =>
  orderList.value.some(
    (item) =>
      item?.orderInfo?.state === stateEnum.delivering &&
      item?.orderInfo?.driverId === userInfo.value.id
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
    return `timeout ${waitMinutes - DISPATCH_TIMEOUT_MINUTES} min`;
  }
  return `${DISPATCH_TIMEOUT_MINUTES - waitMinutes} min left`;
};

const getRedispatchRound = (item) =>
  Math.max(1, Math.floor(getDispatchWaitMinutes(item) / DISPATCH_TIMEOUT_MINUTES));

const calcDistanceValue = (item) => {
  const source = `${item?.delivery || ""}${item?.receive || ""}${item?.orderInfo?.id || ""}`;
  const total = source
    .split("")
    .reduce((sum, ch) => sum + ch.charCodeAt(0), 0);
  return (1.2 + (total % 48) / 10).toFixed(1);
};

const getDistanceText = (item) => `${calcDistanceValue(item)} km`;

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
    return `从 ${item?.delivery || "商家"} 前往 ${item?.receive || "顾客地址"}`;
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
  if (item?.orderInfo?.driverId === userInfo.value.id) return true;
  const haystack = [item?.delivery, item?.receive, item?.cusName, item?.merName]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
  return haystack.includes(driverServiceArea.value);
};

const displayedOrderList = computed(() => {
  if (userInfo.value.type !== "driver") return orderList.value;
  return [...orderList.value]
    .filter((item) => {
    if (item?.orderInfo?.driverId === userInfo.value.id) return true;
    if (item?.orderInfo?.state === stateEnum.missOrder) {
      return matchesDriverServiceArea(item);
    }
    return false;
    })
    .sort((a, b) => {
      const timeoutDiff = Number(isDispatchTimedOut(b)) - Number(isDispatchTimedOut(a));
      if (timeoutDiff !== 0) return timeoutDiff;
      return new Date(b?.orderInfo?.time || 0).getTime() - new Date(a?.orderInfo?.time || 0).getTime();
    });
});

console.log("###stateOptions", stateOptions);

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
  if (
    userInfo.value.type === "driver" &&
    wantedState === stateEnum.delivering &&
    !isDriverOnline.value
  ) {
    ElMessage.warning("Driver is resting now. Switch to online before taking orders.");
    return;
  }
  if (
    userInfo.value.type === "driver" &&
    wantedState === stateEnum.delivering &&
    isDriverBusy.value
  ) {
    ElMessage.warning("Driver is busy delivering another order.");
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

const cancelPreparingOrder = async (order) => {
  try {
    const { value } = await ElMessageBox.prompt(
      "Please enter a cancel reason",
      "Cancel Preparing Order",
      {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
        inputPlaceholder: "ingredients unavailable / kitchen issue / store closed...",
        inputValidator: (input) =>
          input && input.trim() ? true : "Cancel reason is required",
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
      "Reject this delivery and return it to the waiting driver pool?",
      "Reject Delivery",
      {
        confirmButtonText: "Confirm",
        cancelButtonText: "Cancel",
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
    return `顾客评分 ${score}/5${item?.review?.content ? `，评价：${item.review.content}` : ""}`;
  }
  if (getIssueReport(item)) {
    return "已记录配送异常，待人工跟进";
  }
  return "当前无差评或异常记录";
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
    ElMessage.error("Order is required");
    return;
  }
  if (!reviewForm.score) {
    ElMessage.error("Score is required");
    return;
  }
  if (!reviewForm.content.trim()) {
    ElMessage.error("Review content is required");
    return;
  }
  fetch(Review.add, {
    orderId: reviewForm.orderId,
    score: reviewForm.score,
    content: reviewForm.content.trim(),
  }).then(() => {
    ElMessage.success("Review submitted successfully");
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
    ElMessage.error("Order is required");
    return;
  }
  if (!replyForm.replyContent.trim()) {
    ElMessage.error("Reply content is required");
    return;
  }
  fetch(Review.reply, {
    orderId: replyForm.orderId,
    replyContent: replyForm.replyContent.trim(),
  }).then(() => {
    ElMessage.success("Reply submitted successfully");
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
    ElMessage.error("Order is required");
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
    ElMessage.error("Order is required");
    return;
  }
  if (!afterSaleForm.content.trim()) {
    ElMessage.error("Description is required");
    return;
  }
  fetch(AfterSale.create, {
    orderId: afterSaleForm.orderId,
    type: afterSaleForm.type,
    content: afterSaleForm.content.trim(),
  }).then(() => {
    ElMessage.success("After-sale ticket submitted");
    closeAfterSaleDialog();
    loadAfterSaleTickets();
    getOrderList();
  });
};

const initOrderData = () => {
  getOrderList();
  loadAfterSaleTickets();
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
  padding: 24px;
  margin-bottom: 18px;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.header-filter {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  color: var(--text-soft);
  font-weight: 600;
}

.list {
  .item {
    margin-top: 18px;
    padding: 18px;
    border: 1px solid rgba(92, 46, 20, 0.08);
    border-radius: 26px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.86), rgba(255, 249, 243, 0.8));
    box-shadow: var(--shadow-soft);
    transition:
      transform var(--transition-base),
      box-shadow var(--transition-base),
      border-color var(--transition-base);

    &:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-card);
    }

    .product-img {
      margin: 20px;
      width: 200px;
      height: 200px;
      border-radius: 20px;
      object-fit: cover;
      background: rgba(255, 241, 225, 0.7);
    }

    .info {
      display: flex;
      gap: 10px;

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
      flex-wrap: wrap;
      align-items: center;
      margin-top: 6px;
      gap: 12px;

      & > span {
        color: var(--text-strong);
        font-size: 30px;
        font-weight: 700;
        font-family: var(--font-display);
      }
    }

    .detail {
      margin: 20px;
      .price {
        color: @price;
        font-weight: 700;
      }
      .review-block {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      .reply-text {
        color: #606266;
      }
    }

    .footer {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-end;
      gap: 10px;
      padding-top: 14px;
      border-top: 1px solid rgba(92, 46, 20, 0.08);
    }
  }

  .item-timeout {
    border-color: rgba(213, 95, 80, 0.36);
    box-shadow:
      0 0 0 1px rgba(245, 108, 108, 0.12),
      var(--shadow-soft);
  }
}

@media (max-width: 960px) {
  .header {
    padding: 18px;
  }

  .header-filter,
  .section-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    justify-content: flex-start;
  }

  .list .item .info {
    flex-direction: column;
  }

  .list .item .product-img,
  .list .item .info > img {
    width: 100%;
    height: 220px;
    margin: 0 0 16px;
  }
}
</style>
