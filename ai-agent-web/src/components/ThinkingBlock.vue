<template>
  <div class="thinking-block" v-if="steps && steps.length > 0">
    <div class="thinking-header" @click="expanded = !expanded">
      <span class="thinking-icon">🔧</span>
      <span class="thinking-title">思考过程 ({{ steps.length }} 步)</span>
      <svg
        class="thinking-arrow"
        :class="{ 'arrow-expanded': expanded }"
        width="16" height="16" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" stroke-width="2"
        stroke-linecap="round" stroke-linejoin="round"
      >
        <polyline points="9 18 15 12 9 6"></polyline>
      </svg>
    </div>
    <transition name="expand">
      <div class="thinking-body" v-show="expanded">
        <div
          v-for="(step, index) in steps"
          :key="index"
          class="thinking-step"
        >
          <div class="step-header">
            <span class="step-badge">Step {{ step.step }}</span>
            <span class="step-type" :class="step.type === 'thinking' ? 'type-think' : 'type-tool'">
              {{ step.type === 'thinking' ? '💭 思考' : '🔧 工具调用' }}
            </span>
          </div>
          <div class="step-content">{{ step.content }}</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  steps: {
    type: Array,
    default: () => []
  }
})

const expanded = ref(false)
</script>

<style scoped>
.thinking-block {
  margin-bottom: 8px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(139, 92, 246, 0.2);
  background: rgba(139, 92, 246, 0.06);
  overflow: hidden;
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  user-select: none;
  transition: background var(--transition-fast);
}

.thinking-header:hover {
  background: rgba(139, 92, 246, 0.1);
}

.thinking-icon {
  font-size: 14px;
}

.thinking-title {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.thinking-arrow {
  color: var(--text-muted);
  transition: transform var(--transition-fast);
}

.arrow-expanded {
  transform: rotate(90deg);
}

/* Expand transition */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.thinking-body {
  border-top: 1px solid rgba(139, 92, 246, 0.12);
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
}

.thinking-step {
  padding: 8px 10px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: var(--radius-sm);
  border-left: 3px solid rgba(139, 92, 246, 0.4);
}

.step-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.step-badge {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  background: rgba(255, 255, 255, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
}

.step-type {
  font-size: 12px;
  font-weight: 500;
}

.type-think {
  color: #a78bfa;
}

.type-tool {
  color: #67e8f9;
}

.step-content {
  font-size: 12.5px;
  color: var(--text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 150px;
  overflow-y: auto;
}
</style>
