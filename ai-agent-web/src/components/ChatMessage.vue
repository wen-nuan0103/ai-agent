<template>
  <div class="message-wrapper" :class="[role === 'user' ? 'message-user' : 'message-ai']">
    <div class="avatar" :class="role === 'user' ? 'avatar-user' : 'avatar-ai'">
      <span v-if="role === 'user'">🧑</span>
      <span v-else>🤖</span>
    </div>
    <div class="bubble" :class="role === 'user' ? 'bubble-user' : 'bubble-ai'">
      <!-- Dynamic content render to support custom components like download buttons -->
      <component :is="renderContent" />
    </div>
  </div>
</template>

<script setup>
import { computed, h } from 'vue'
import { downloadFile } from '../api/index'

const props = defineProps({
  role: {
    type: String,
    required: true,
    validator: (v) => ['user', 'assistant'].includes(v)
  },
  content: {
    type: String,
    default: ''
  },
  allowDownload: {
    type: Boolean,
    default: true
  }
})

// Simple parser to extract filenames like xxx.pdf or xxx.txt
const renderContent = computed(() => {
  const text = props.content || ''
  
  // Look for any word that ends in .pdf or .txt (basic regex)
  // We split the string by files and render text and files alternately
  const fileRegex = /([a-zA-Z0-9_-]+\.(?:pdf|txt))/gi
  
  const tokens = text.split(fileRegex)
  
  const children = tokens.map(token => {
    // If it's a file name like ending with .pdf/.txt
    if (fileRegex.test(token)) {
      // Return a download link component
      return h('span', { 
        class: ['file-download-badge', { 'badge-disabled': !props.allowDownload }],
        onClick: () => props.allowDownload && handleDownload(token),
        title: props.allowDownload ? '点击下载文件' : '文件生成中，暂不可下载'
      }, [
        h('span', { class: 'file-icon' }, '📄'),
        h('span', { class: 'file-name' }, token),
        h('svg', { 
          class: 'download-icon',
          width: '14', height: '14', viewBox: '0 0 24 24',
          fill: 'none', stroke: 'currentColor', 'stroke-width': '2',
          'stroke-linecap': 'round', 'stroke-linejoin': 'round'
        }, [
          h('path', { d: 'M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4' }),
          h('polyline', { points: '7 10 12 15 17 10' }),
          h('line', { x1: '12', y1: '15', x2: '12', y2: '3' })
        ])
      ])
    } else {
      // Just text with line breaks
      const parts = token.split('\n')
      const textNodes = []
      for (let i = 0; i < parts.length; i++) {
        textNodes.push(parts[i])
        if (i < parts.length - 1) textNodes.push(h('br'))
      }
      return h('span', { class: 'message-content' }, textNodes)
    }
  })
  
  return h('div', { class: 'message-container' }, children)
})

async function handleDownload(fileName) {
  try {
    await downloadFile(fileName)
  } catch (err) {
    alert('下载失败，请检查文件是否存在。')
  }
}
</script>

<style scoped>
.message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 6px 0;
  animation: messageSlideIn 0.35s ease forwards;
}

.message-user {
  flex-direction: row-reverse;
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
  flex-shrink: 0;
}

.avatar-user {
  background: var(--user-bubble);
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
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
  word-break: break-word;
}

.bubble-user {
  background: var(--user-bubble);
  color: var(--user-bubble-text);
  border-radius: var(--radius-md) 4px var(--radius-md) var(--radius-md);
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.2);
}

.bubble-ai {
  background: var(--ai-bubble);
  color: var(--ai-bubble-text);
  border-radius: 4px var(--radius-md) var(--radius-md) var(--radius-md);
  border: 1px solid var(--border-subtle);
}

.message-content {
  white-space: pre-wrap;
}

/* File Download Badge */
.file-download-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(139, 92, 246, 0.15);
  border: 1px solid rgba(139, 92, 246, 0.3);
  padding: 4px 10px;
  border-radius: 6px;
  margin: 2px 4px;
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-primary);
  font-weight: 500;
}

.file-download-badge:hover {
  background: rgba(139, 92, 246, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.15);
}

.file-download-badge:active {
  transform: translateY(0);
}

.badge-disabled {
  opacity: 0.6;
  cursor: not-allowed;
  filter: grayscale(100%);
}
.badge-disabled:hover {
  transform: none;
  box-shadow: none;
}

.file-icon {
  font-size: 14px;
}

.file-name {
  font-size: 13.5px;
}

.download-icon {
  margin-left: 2px;
  color: var(--text-muted);
}

.file-download-badge:hover .download-icon {
  color: #a78bfa;
}
</style>
