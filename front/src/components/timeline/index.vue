<!--
  OrderTimeline 订单状态时间线组件
  使用统一的 orderState 工具生成时间线步骤
-->
<template>
  <div class="order-timeline">
    <div
      v-for="(step, idx) in steps"
      :key="idx"
      class="tl-item"
    >
      <div
        class="tl-dot"
        :class="step.status"
      >
        <i :class="step.icon"></i>
      </div>
      <div class="tl-content" v-if="step.status !== 'pending' || idx === activeIndex">
        <div class="tl-title" :class="{ pending: step.status === 'pending' }">
          {{ step.label }}
        </div>
        <div v-if="step.time" class="tl-time">{{ step.time }}</div>
      </div>
      <div class="tl-content" v-else>
        <div class="tl-title pending">{{ step.label }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { getTimelineSteps } from '@/utils/orderState';

const props = defineProps({
  /** 当前订单状态值 */
  currentState: { type: Number, required: true },
  /** 各步骤的时间戳，可选，格式: { '-1': '23:16', '0': '23:17', ... } */
  times: { type: Object, default: () => ({}) },
});

const steps = computed(() => {
  const raw = getTimelineSteps(props.currentState);
  return raw.map((s) => ({
    ...s,
    time: props.times[s.state] || '',
  }));
});

const activeIndex = computed(() => {
  return steps.value.findIndex((s) => s.status === 'current');
});
</script>

<style lang="less" scoped>
.order-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.tl-item {
  display: flex;
  gap: 14px;
  padding-bottom: 20px;
  position: relative;

  &:not(:last-child)::before {
    content: '';
    position: absolute;
    left: 15px;
    top: 32px;
    bottom: 0;
    width: 2px;
    background: var(--border);
  }

  &:last-child {
    padding-bottom: 0;
  }
}

.tl-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
  z-index: 1;

  &.done {
    background: var(--success-light, #ECFDF5);
    color: var(--success, #10B981);
  }

  &.current {
    background: var(--primary-light, #FFF3ED);
    color: var(--primary, #E8652B);
    box-shadow: 0 0 0 4px rgba(232, 101, 43, 0.1);
  }

  &.pending {
    background: var(--bg, #F5F6FA);
    color: var(--text-muted, #9CA3AF);
  }
}

.tl-content {
  padding-top: 5px;
}

.tl-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);

  &.pending {
    color: var(--text-muted, #9CA3AF);
  }
}

.tl-time {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
  margin-top: 2px;
}
</style>
