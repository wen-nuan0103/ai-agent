<template>
  <div class="chat-page manus-theme">
    <!-- Header -->
    <header class="chat-header">
      <router-link to="/" class="back-btn" id="manus-back-btn">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </router-link>
      <div class="header-info">
        <h1 class="header-title">🧠 AI 超级智能体</h1>
        <span class="header-status">
          <span class="status-dot"></span>
          在线
        </span>
      </div>
      <div class="header-actions">
        <button class="action-btn" @click="clearChat" id="manus-clear-btn" title="清空对话">
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
        <div class="welcome-icon">🧠</div>
        <h2>你好，我是 AI 超级智能体</h2>
        <p>我可以帮你解决各种复杂问题，来试试吧！</p>
      </div>

      <template v-for="(msg, index) in messages" :key="index">
        <!-- Thinking block for AI messages -->
        <ThinkingBlock
          v-if="msg.role === 'assistant' && msg.thinkingSteps && msg.thinkingSteps.length > 0"
          :steps="msg.thinkingSteps"
        />
        <ChatMessage 
          :role="msg.role" 
          :content="msg.content" 
          :allowDownload="!(isStreaming && index === messages.length - 1)"
        />
      </template>

      <!-- Typing / thinking indicator -->
      <div v-if="isStreaming && messages.length > 0 && !messages[messages.length - 1].content" class="message-wrapper message-ai">
        <div class="avatar avatar-ai"><span>🤖</span></div>
        <div class="bubble bubble-ai">
          <div class="streaming-status">
            <template v-if="isThinking">
              <span class="thinking-label">🔧 正在思考...</span>
            </template>
            <template v-else>
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- Input -->
    <ChatInput
      :disabled="isStreaming"
      placeholder="描述你想解决的问题..."
      @send="handleSend"
    />
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import ThinkingBlock from '../components/ThinkingBlock.vue'
import { connectManusChatSSE } from '../api/index.js'

const messages = ref([])
const isStreaming = ref(false)
const isThinking = ref(false)
const messagesRef = ref(null)

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
  isThinking.value = true
  const aiIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '', thinkingSteps: [] })

  const eventSource = connectManusChatSSE(
    text,
    (data) => {
      try {
        const event = JSON.parse(data)

        if (event.type === 'thinking' || event.type === 'tool') {
          // Add to thinking steps
          isThinking.value = true
          messages.value[aiIndex].thinkingSteps.push({
            type: event.type,
            step: event.step,
            content: event.content
          })
        } else if (event.type === 'result') {
          // Set final answer
          isThinking.value = false
          messages.value[aiIndex].content = event.content || ''
        } else if (event.type === 'error') {
          isThinking.value = false
          messages.value[aiIndex].content = event.content || '发生错误'
        }
      } catch (e) {
        // Fallback: treat as plain text (backward compatibility)
        messages.value[aiIndex].content += data
      }
      scrollToBottom()
    },
    (error) => {
      console.error('SSE error:', error)
      if (!messages.value[aiIndex].content && messages.value[aiIndex].thinkingSteps.length === 0) {
        messages.value[aiIndex].content = '抱歉，连接出现问题，请稍后重试。'
      }
      isStreaming.value = false
      isThinking.value = false
      scrollToBottom()
    },
    () => {
      isStreaming.value = false
      isThinking.value = false
      scrollToBottom()
    }
  )
}

function clearChat() {
  messages.value = []
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
  background: var(--manus-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-message p {
  font-size: 15px;
  color: var(--text-secondary);
}

/* ===== Typing / thinking indicator ===== */
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

.streaming-status {
  display: flex;
  align-items: center;
}

.thinking-label {
  font-size: 13px;
  color: #a78bfa;
  animation: pulse 1.5s infinite ease-in-out;
}
</style>
