<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route  = useRoute()
const auth   = useAuthStore()

const email    = ref('')
const password = ref('')
const error    = ref('')
const loading  = ref(false)
const showPw   = ref(false)
const mounted  = ref(false)

onMounted(() => setTimeout(() => { mounted.value = true }, 60))

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    error.value = e.response?.data?.message || '이메일 또는 비밀번호를 확인해주세요.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page" :class="{ ready: mounted }">

    <!-- ══════ LEFT PANEL ══════ -->
    <div class="left">
      <div class="orb orb1" aria-hidden="true"></div>
      <div class="orb orb2" aria-hidden="true"></div>

      <!-- top bar -->
      <div class="left-top">
        <router-link to="/" class="home-btn">
          <svg viewBox="0 0 20 20" fill="currentColor"><path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z"/></svg>
          홈으로
        </router-link>
        <div class="brand">
          <span class="brand-dot"></span>
          Blog
        </div>
      </div>

      <!-- welcome message -->
      <div class="welcome">
        <p class="eyebrow">✦ 다시 돌아오셨군요</p>
        <h1>
          안녕하세요,<br>
          <span class="grad-text">반갑습니다 👋</span>
        </h1>
        <p class="sub">오늘도 좋은 글로 독자들과 소통해보세요.</p>
      </div>

      <!-- stats cards -->
      <div class="stat-cards">
        <div class="stat-card">
          <span class="stat-emoji">✍️</span>
          <p class="stat-label">글 쓰는 작가</p>
          <p class="stat-val">매일 성장 중</p>
        </div>
        <div class="stat-card">
          <span class="stat-emoji">❤️</span>
          <p class="stat-label">독자의 반응</p>
          <p class="stat-val">실시간 소통</p>
        </div>
        <div class="stat-card">
          <span class="stat-emoji">📝</span>
          <p class="stat-label">나의 기록</p>
          <p class="stat-val">언제 어디서나</p>
        </div>
      </div>
    </div>

    <!-- ══════ RIGHT PANEL ══════ -->
    <div class="right">
      <div class="form-box">

        <!-- success banner -->
        <div v-if="route.query.registered" class="success-banner">
          <svg viewBox="0 0 20 20" fill="currentColor" class="s-icon">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
          </svg>
          회원가입이 완료됐어요! 로그인해주세요.
        </div>

        <div class="fh">
          <h2 class="fh-title">로그인</h2>
          <p class="fh-sub">
            계정이 없으신가요?&nbsp;
            <router-link to="/signup" class="signup-link">회원가입</router-link>
          </p>
        </div>

        <!-- error -->
        <div v-if="error" class="err-box" role="alert">
          <svg viewBox="0 0 20 20" fill="currentColor" class="err-icon">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
          {{ error }}
        </div>

        <form @submit.prevent="submit" novalidate class="form">

          <!-- email -->
          <div class="field">
            <label for="l-email">이메일</label>
            <input
              id="l-email" v-model="email"
              type="email" required
              placeholder="hello@example.com"
              class="inp"
            />
          </div>

          <!-- password -->
          <div class="field">
            <label for="l-pw">비밀번호</label>
            <div class="inp-wrap">
              <input
                id="l-pw" v-model="password"
                :type="showPw ? 'text' : 'password'"
                required
                placeholder="비밀번호를 입력하세요"
                class="inp"
              />
              <button type="button" class="eye-btn" @click="showPw = !showPw" tabindex="-1">
                <svg v-if="showPw" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/></svg>
              </button>
            </div>
          </div>

          <button type="submit" :disabled="loading" class="submit">
            <span v-if="loading" class="spin" aria-hidden="true"></span>
            <span>{{ loading ? '로그인 중...' : '로그인' }}</span>
            <svg v-if="!loading" viewBox="0 0 20 20" fill="currentColor" class="arrow-icon">
              <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
          </button>

        </form>
      </div>
    </div>

  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Noto+Sans+KR:wght@300;400;500;600;700&display=swap');

.page {
  display: flex;
  min-height: 100vh;
  font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', sans-serif;
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.4s ease, transform 0.4s ease;
}
.page.ready { opacity: 1; transform: none; }

/* ── LEFT ── */
.left {
  position: relative;
  width: 48%;
  background: #F8F9FF;
  display: flex;
  flex-direction: column;
  padding: 1.75rem 2.5rem 2.5rem;
  overflow: hidden;
  border-right: 1px solid #E8EDF8;
}

.orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(60px);
}
.orb1 {
  width: 320px; height: 320px;
  background: radial-gradient(circle, rgba(99,102,241,0.15), transparent 70%);
  top: -80px; right: -40px;
}
.orb2 {
  width: 260px; height: 260px;
  background: radial-gradient(circle, rgba(16,185,129,0.12), transparent 70%);
  bottom: 60px; left: -40px;
  animation: orbFloat 5s ease-in-out infinite;
}
@keyframes orbFloat {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-16px); }
}

.left-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 3rem;
}

.home-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.45rem 0.9rem;
  border-radius: 100px;
  background: white;
  border: 1px solid #E2E8F0;
  color: #475569;
  font-size: 0.8rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.18s ease;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.home-btn svg { width: 14px; height: 14px; }
.home-btn:hover { background: #F1F5F9; color: #1E293B; transform: translateX(-2px); }

.brand {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 0.95rem;
  font-weight: 800;
  color: #4F46E5;
}
.brand-dot {
  width: 8px; height: 8px;
  background: linear-gradient(135deg, #6366F1, #8B5CF6);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(99,102,241,0.5);
}

.welcome {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-bottom: 1rem;
}
.eyebrow {
  font-size: 0.72rem;
  color: #6366F1;
  font-weight: 600;
  letter-spacing: 0.05em;
  margin-bottom: 0.75rem;
}
.welcome h1 {
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: clamp(1.75rem, 3vw, 2.4rem);
  font-weight: 800;
  color: #0F172A;
  line-height: 1.15;
  margin: 0 0 0.85rem;
  letter-spacing: -0.02em;
}
.grad-text {
  background: linear-gradient(135deg, #4776E6 0%, #8E54E9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.sub {
  font-size: 0.875rem;
  color: #64748B;
  line-height: 1.65;
}

/* stat cards */
.stat-cards {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.65rem;
}
.stat-card {
  background: white;
  border: 1px solid #E8EDF8;
  border-radius: 14px;
  padding: 0.9rem 0.85rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(15,23,42,0.04);
}
.stat-emoji { font-size: 1.4rem; display: block; margin-bottom: 0.4rem; }
.stat-label {
  font-size: 0.65rem;
  color: #94A3B8;
  font-weight: 500;
  margin: 0 0 0.15rem;
}
.stat-val {
  font-size: 0.72rem;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
}

/* ── RIGHT ── */
.right {
  width: 52%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 2rem;
}
.form-box {
  width: 100%;
  max-width: 380px;
}

.success-banner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #F0FDF4;
  border: 1px solid #BBF7D0;
  border-radius: 10px;
  color: #15803D;
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
}
.s-icon { width: 15px; height: 15px; flex-shrink: 0; }

.fh { margin-bottom: 2rem; }
.fh-title {
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 1.75rem;
  font-weight: 800;
  color: #0F172A;
  letter-spacing: -0.02em;
  margin: 0 0 0.4rem;
}
.fh-sub { font-size: 0.85rem; color: #94A3B8; }
.signup-link {
  color: #4F46E5;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.15s;
}
.signup-link:hover { color: #4338CA; text-decoration: underline; }

.err-box {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 10px;
  color: #DC2626;
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 1.25rem;
}
.err-icon { width: 15px; height: 15px; flex-shrink: 0; }

.form { display: flex; flex-direction: column; gap: 1.25rem; }
.field { display: flex; flex-direction: column; gap: 0.4rem; }
.field label {
  font-size: 0.78rem;
  font-weight: 600;
  color: #374151;
}
.inp-wrap { position: relative; }
.inp {
  width: 100%;
  padding: 0.8rem 1rem;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 0.875rem;
  color: #0F172A;
  font-family: 'Noto Sans KR', sans-serif;
  outline: none;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}
.inp::placeholder { color: #CBD5E1; }
.inp:focus {
  border-color: #6366F1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
}
.inp-wrap .inp { padding-right: 2.75rem; }

.eye-btn {
  position: absolute;
  right: 0.85rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  color: #94A3B8;
  display: flex;
  align-items: center;
  transition: color 0.15s;
}
.eye-btn:hover { color: #4F46E5; }
.eye-btn svg { width: 18px; height: 18px; }

.submit {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.9rem 1.5rem;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 0.925rem;
  font-weight: 700;
  font-family: 'Noto Sans KR', sans-serif;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.18s, box-shadow 0.2s;
  box-shadow: 0 4px 16px rgba(71,118,230,0.35);
  margin-top: 0.25rem;
}
.submit:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(71,118,230,0.45);
}
.submit:active:not(:disabled) { transform: translateY(0); }
.submit:disabled { opacity: 0.55; cursor: not-allowed; }
.arrow-icon { width: 18px; height: 18px; transition: transform 0.2s; }
.submit:hover:not(:disabled) .arrow-icon { transform: translateX(3px); }
.spin {
  width: 16px; height: 16px;
  border: 2.5px solid rgba(255,255,255,0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 820px) {
  .page { flex-direction: column; }
  .left { width: 100%; min-height: auto; padding: 1.5rem; }
  .right { width: 100%; padding: 2rem 1.25rem; }
  .form-box { max-width: 100%; }
  .welcome { flex: none; padding-bottom: 1.5rem; }
  .welcome h1 { font-size: 1.75rem; }
}
</style>