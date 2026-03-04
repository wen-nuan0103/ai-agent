<template>
  <div class="chat-input-bar">
    <div class="input-wrapper">
      <textarea
        ref="inputRef"
        v-model="message"
        :placeholder="placeholder"
        :disabled="disabled"
        rows="1"
        @keydown.enter.exact.prevent="handleSend"
        @input="autoResize"
      />
      <button
        class="send-btn"
        :class="{ active: canSend }"
        :disabled="!canSend"
        @click="handleSend"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="22" y1="2" x2="11" y2="13"></line>
          <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '输入你的消息...'
  }
})

const emit = defineEmits(['send'])

const message = ref('')
const inputRef = ref(null)

const canSend = computed(() => message.value.trim().length > 0 && !props.disabled)

function handleSend() {
  if (!canSend.value) return
  emit('send', message.value.trim())
  message.value = ''
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
}

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}
</script>

<style scoped>
.chat-input-bar {
  padding: 16px 20px 20px;
  background: linear-gradient(to top, var(--bg-primary) 60%, transparent);
  backdrop-filter: blur(12px);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: var(--bg-input);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 10px 10px 10px 18px;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.input-wrapper:focus-within {
  border-color: rgba(139, 92, 246, 0.5);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.input-wrapper textarea {
  flex: 1;
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.5;
  resize: none;
  max-height: 120px;
  min-height: 24px;
}

.input-wrapper textarea::placeholder {
  color: var(--text-muted);
}

.send-btn {
  width: 40px;
  height: 40px;
  min-width: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-muted);
  transition: all var(--transition-fast);
}

.send-btn.active {
  background: var(--user-bubble);
  color: white;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.35);
}

.send-btn.active:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.45);
}

.send-btn:disabled {
  cursor: not-allowed;
}
</style>
