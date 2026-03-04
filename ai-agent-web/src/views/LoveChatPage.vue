<template>
  <div class="chat-page love-theme">
    <!-- Header -->
    <header class="chat-header">
      <router-link to="/" class="back-btn" id="love-back-btn">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </router-link>
      <div class="header-info">
        <h1 class="header-title">💕 AI 恋爱大师</h1>
        <span class="header-status">
          <span class="status-dot"></span>
          在线
        </span>
      </div>
      <div class="header-actions">
        <button class="action-btn" @click="clearChat" id="love-clear-btn" title="清空对话">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
        </button>
      </div>
    </header>

    <!-- Messages -->
    <div class="chat-messages" ref="messagesRef">
      <!-- Welcome message -->
      <div class="welcome-message" v-if="messages.length === 0">
        <div class="welcome-icon">💕</div>
        <h2>你好，我是 AI 恋爱大师</h2>
        <p>有什么感情上的困惑，都可以告诉我哦~</p>
      </div>

      <ChatMessage
        v-for="(msg, index) in messages"
        :key="index"
        :role="msg.role"
        :content="msg.content"
        :allowDownload="!(isStreaming && index === messages.length - 1)"
      />

      <!-- Typing indicator (only when AI hasn't started responding yet) -->
      <div v-if="isStreaming && messages.length > 0 && !messages[messages.length - 1].content" class="message-wrapper message-ai">
        <div class="avatar avatar-ai"><span>🤖</span></div>
        <div class="bubble bubble-ai">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Input -->
    <ChatInput
      :disabled="isStreaming"
      placeholder="聊聊你的感情问题..."
      @send="handleSend"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import { connectLoveChatSSE } from '../api/index.js'

const messages = ref([])
const isStreaming = ref(false)
const messagesRef = ref(null)
const chatId = ref('')

onMounted(() => {
  chatId.value = uuidv4()
})

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function handleSend(text) {
  // Add user message
  messages.value.push({ role: 'user', content: text })
  scrollToBottom()

  // Start streaming
  isStreaming.value = true
  let aiContent = ''
  const aiIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })

  const eventSource = connectLoveChatSSE(
    text,
    chatId.value,
    (data) => {
      // onMessage
      aiContent += data
      messages.value[aiIndex].content = aiContent
      scrollToBottom()
    },
    (error) => {
      // onError
      console.error('SSE error:', error)
      if (!aiContent) {
        messages.value[aiIndex].content = '抱歉，连接出现问题，请稍后重试。'
      }
      isStreaming.value = false
      scrollToBottom()
    },
    () => {
      // onComplete
      isStreaming.value = false
      scrollToBottom()
    }
  )
}

function clearChat() {
  messages.value = []
  chatId.value = uuidv4()
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-primary);
  position: relative;
}

/* ===== Header ===== */
.chat-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid var(--border-subtle);
  backdrop-filter: blur(12px);
  z-index: 10;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  background: rgba(255, 255, 255, 0.05);
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.header-info {
  flex: 1;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
}

.header-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.status-dot {
  width: 7px;
  height: 7px;
  background: #22c55e;
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(34, 197, 94, 0.5);
}

.action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: var(--text-muted);
  background: rgba(255, 255, 255, 0.05);
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

/* ===== Messages ===== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* ===== Welcome ===== */
.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  flex: 1;
  gap: 12px;
  opacity: 0.7;
}

.welcome-icon {
  font-size: 56px;
  margin-bottom: 4px;
}

.welcome-message h2 {
  font-size: 22px;
  font-weight: 600;
  background: var(--love-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-message p {
  font-size: 15px;
  color: var(--text-secondary);
}

/* ===== Typing indicator inline ===== */
.message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 6px 0;
  animation: messageSlideIn 0.35s ease forwards;
}

.message-ai {
  flex-direction: row;
}

.avatar {
  width: 40px;
  height: 40px;
  min-width: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.avatar-ai {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid var(--border-subtle);
}

.bubble {
  max-width: 70%;
  padding: 12px 18px;
  line-height: 1.65;
  font-size: 14.5px;
}

.bubble-ai {
  background: var(--ai-bubble);
  color: var(--ai-bubble-text);
  border-radius: 4px var(--radius-md) var(--radius-md) var(--radius-md);
  border: 1px solid var(--border-subtle);
}
</style>
