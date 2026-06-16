/**
 * 订单状态统一映射工具
 * 所有页面应使用此工具获取状态文案、徽章样式和操作按钮，避免重复硬编码
 */

/** 订单状态枚举 */
export const ORDER_STATE = {
  REFUNDED: -3,
  REFUNDING: -2,
  UNPAID: -1,
  PAID: 0,
  DELIVERING: 1,
  COMPLETED: 2,
  WAITING_RIDER: 3,
  PREPARING: 4,
};

/** 状态 → 中文名 */
const STATE_LABEL = {
  [-3]: '已退款',
  [-2]: '退款中',
  [-1]: '待支付',
  [0]: '待接单',
  [1]: '配送中',
  [2]: '已完成',
  [3]: '待骑手接单',
  [4]: '备餐中',
};

/** 状态 → 徽章样式类名（与 badge.less 中的变体对应） */
const STATE_BADGE = {
  [-3]: 'badge--danger',
  [-2]: 'badge--warning',
  [-1]: 'badge--info',
  [0]: 'badge--warning',
  [1]: 'badge--primary',
  [2]: 'badge--success',
  [3]: 'badge--info',
  [4]: 'badge--primary',
};

/** 状态 → Element Plus tag 类型（用于 el-tag） */
const STATE_TAG_TYPE = {
  [-3]: 'danger',
  [-2]: 'warning',
  [-1]: 'info',
  [0]: 'warning',
  [1]: '',
  [2]: 'success',
  [3]: 'info',
  [4]: '',
};

/** 顾客端：各状态可见操作 */
const CUSTOMER_ACTIONS = {
  [-3]: ['viewRefund'],
  [-2]: [],
  [-1]: ['pay', 'cancel'],
  [0]: [],
  [1]: ['confirmReceive', 'applyRefund'],
  [2]: ['review', 'applyAfterSale'],
  [3]: [],
  [4]: ['applyRefund'],
};

/** 商家端：各状态可见操作 */
const MERCHANT_ACTIONS = {
  [-3]: ['viewRefund'],
  [-2]: ['handleRefund'],
  [-1]: [],
  [0]: ['accept', 'reject'],
  [1]: [],
  [2]: ['viewReview'],
  [3]: [],
  [4]: ['callRider'],
};

/** 骑手端：各状态可见操作 */
const DRIVER_ACTIONS = {
  [-3]: [],
  [-2]: [],
  [-1]: [],
  [0]: [],
  [1]: ['completeDelivery', 'returnToPool'],
  [2]: ['viewHistory'],
  [3]: ['acceptOrder'],
  [4]: [],
};

/** 订单状态时间线步骤定义 */
const TIMELINE_STEPS = [
  { state: -1, label: '订单提交', icon: 'fas fa-file-alt' },
  { state: 0, label: '商家接单', icon: 'fas fa-store' },
  { state: 4, label: '备餐完成', icon: 'fas fa-fire' },
  { state: 3, label: '骑手接单', icon: 'fas fa-motorcycle' },
  { state: 1, label: '配送中', icon: 'fas fa-shipping-fast' },
  { state: 2, label: '已送达', icon: 'fas fa-flag-checkered' },
];

// ---------- 公共方法 ----------

/**
 * 获取状态中文名
 */
export function getStateLabel(state) {
  return STATE_LABEL[state] ?? '未知状态';
}

/**
 * 获取状态对应的徽章类名
 */
export function getStateBadgeClass(state) {
  return STATE_BADGE[state] ?? 'badge--info';
}

/**
 * 获取状态对应的 el-tag type
 */
export function getStateTagType(state) {
  return STATE_TAG_TYPE[state] ?? 'info';
}

/**
 * 获取指定角色在某状态下的可操作列表
 * @param {'customer'|'merchant'|'driver'} role
 * @param {number} state
 * @returns {string[]}
 */
export function getActionsForState(role, state) {
  const map = {
    customer: CUSTOMER_ACTIONS,
    merchant: MERCHANT_ACTIONS,
    driver: DRIVER_ACTIONS,
  };
  return (map[role]?.[state]) ?? [];
}

/**
 * 判断某角色是否可以在某状态下执行某操作
 */
export function canAction(role, state, action) {
  return getActionsForState(role, state).includes(action);
}

/**
 * 获取时间线步骤（用于订单追踪组件）
 * @param {number} currentState 当前订单状态
 * @returns {Array<{state, label, icon, status: 'done'|'current'|'pending'}>}
 */
export function getTimelineSteps(currentState) {
  let foundCurrent = false;
  return TIMELINE_STEPS.map((step) => {
    if (currentState < 0) {
      // 退款状态，全部标为 done（已取消/已退款的场景）
      return { ...step, status: 'done' };
    }
    if (currentState === step.state) {
      foundCurrent = true;
      return { ...step, status: 'current' };
    }
    if (!foundCurrent && currentState > step.state) {
      return { ...step, status: 'done' };
    }
    return { ...step, status: 'pending' };
  });
}

/**
 * 订单状态转颜色（用于统计图表）
 */
export function getStateColor(state) {
  const map = {
    [-3]: '#DC2626',
    [-2]: '#F59E0B',
    [-1]: '#3B82F6',
    [0]: '#F59E0B',
    [1]: '#EF4444',
    [2]: '#10B981',
    [3]: '#3B82F6',
    [4]: '#E8652B',
  };
  return map[state] ?? '#9CA3AF';
}
