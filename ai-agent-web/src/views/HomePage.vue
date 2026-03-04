<template>
  <div class="home-page">
    <!-- Background orbs -->
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>

    <div class="home-content">
      <header class="home-header">
        <h1 class="title">
          <span class="title-icon">✨</span>
          AI 智能助手
        </h1>
        <p class="subtitle">选择一个应用开始体验 AI 的魅力</p>
      </header>

      <div class="app-cards">
        <!-- Love Master Card -->
        <router-link to="/love-chat" class="app-card love-card" id="love-card">
          <div class="card-glow love-glow"></div>
          <div class="card-icon">💕</div>
          <h2 class="card-title">AI 恋爱大师</h2>
          <p class="card-desc">专业的恋爱咨询助手，为你的感情保驾护航</p>
          <div class="card-arrow">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="5" y1="12" x2="19" y2="12"></line>
              <polyline points="12 5 19 12 12 19"></polyline>
            </svg>
          </div>
        </router-link>

        <!-- Manus Agent Card -->
        <router-link to="/manus-chat" class="app-card manus-card" id="manus-card">
          <div class="card-glow manus-glow"></div>
          <div class="card-icon">🧠</div>
          <h2 class="card-title">AI 超级智能体</h2>
          <p class="card-desc">强大的 AI 智能体，帮你解决复杂问题</p>
          <div class="card-arrow">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="5" y1="12" x2="19" y2="12"></line>
              <polyline points="12 5 19 12 12 19"></polyline>
            </svg>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-page {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  min-height: 100vh;
}

/* ===== Background Orbs ===== */
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.12;
  pointer-events: none;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: #f43f5e;
  top: -150px;
  left: -100px;
  animation: float 8s ease-in-out infinite;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: #3b82f6;
  bottom: -100px;
  right: -80px;
  animation: float 10s ease-in-out infinite reverse;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: #8b5cf6;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float 12s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

/* ===== Content ===== */
.home-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px 20px;
}

.home-header {
  margin-bottom: 50px;
}

.title {
  font-size: 42px;
  font-weight: 700;
  letter-spacing: -0.5px;
  margin-bottom: 14px;
  background: linear-gradient(135deg, #f1f5f9 0%, #94a3b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  -webkit-text-fill-color: initial;
  margin-right: 6px;
}

.subtitle {
  font-size: 17px;
  color: var(--text-secondary);
  font-weight: 400;
}

/* ===== Cards ===== */
.app-cards {
  display: flex;
  gap: 28px;
  justify-content: center;
  flex-wrap: wrap;
}

.app-card {
  position: relative;
  width: 300px;
  padding: 40px 30px 36px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  backdrop-filter: blur(20px);
  overflow: hidden;
  transition: all var(--transition-smooth);
  cursor: pointer;
  text-align: center;
}

.app-card:hover {
  background: var(--bg-card-hover);
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  border-radius: 50%;
  opacity: 0;
  transition: opacity var(--transition-smooth);
  pointer-events: none;
}

.app-card:hover .card-glow {
  opacity: 1;
}

.love-glow {
  background: radial-gradient(circle, var(--love-glow) 0%, transparent 60%);
}

.manus-glow {
  background: radial-gradient(circle, var(--manus-glow) 0%, transparent 60%);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 18px;
  display: inline-block;
  transition: transform var(--transition-smooth);
}

.app-card:hover .card-icon {
  transform: scale(1.15);
}

.card-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--text-primary);
}

.love-card:hover .card-title {
  background: var(--love-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.manus-card:hover .card-title {
  background: var(--manus-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20px;
}

.card-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-muted);
  transition: all var(--transition-fast);
}

.love-card:hover .card-arrow {
  background: var(--love-primary);
  color: white;
  box-shadow: 0 4px 15px var(--love-glow);
}

.manus-card:hover .card-arrow {
  background: var(--manus-primary);
  color: white;
  box-shadow: 0 4px 15px var(--manus-glow);
}
</style>
