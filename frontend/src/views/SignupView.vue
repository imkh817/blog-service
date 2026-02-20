<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const email      = ref('')
const password   = ref('')
const pwConfirm  = ref('')
const nickname   = ref('')
const error      = ref('')
const loading    = ref(false)
const mounted    = ref(false)
const showPw     = ref(false)
const showPwc    = ref(false)

const previewCards = [
  {
    tag: '개발', emoji: '💻',
    title: 'React로 나만의 투두앱 만들기',
    excerpt: 'useState와 useEffect를 활용해서 간단하게...',
    author: '김코딩', likes: 128, comments: 24,
    rotate: '-4deg', z: 0,
  },
  {
    tag: '여행', emoji: '✈️',
    title: '교토 2박 3일 완벽 여행 가이드',
    excerpt: '벚꽃 시즌 교토는 정말 너무 예뻤어요. 아라시...',
    author: '박여행', likes: 215, comments: 47,
    rotate: '-1.5deg', z: 1,
  },
  {
    tag: '일상', emoji: '☕',
    title: '오늘 발견한 성수동 감성 카페',
    excerpt: '우연히 발견한 골목길 카페, 분위기가 너무...',
    author: '이감성', likes: 84, comments: 19,
    rotate: '2deg', z: 2,
  },
]

onMounted(() => setTimeout(() => { mounted.value = true }, 60))

/* password strength */
const pwStrength = computed(() => {
  const pw = password.value
  if (!pw) return 0
  let s = 0
  if (pw.length >= 8) s++
  if (pw.length >= 12) s++
  if (/[A-Z]/.test(pw)) s++
  if (/[0-9]/.test(pw)) s++
  if (/[^A-Za-z0-9]/.test(pw)) s++
  return Math.min(s, 4)
})

const pwInfo = computed(() => [
  { label: '',       color: 'transparent',  bg: '#E2E8F0' },
  { label: '약해요',  color: '#EF4444',      bg: '#FEE2E2' },
  { label: '보통이에요', color: '#F59E0B',   bg: '#FEF3C7' },
  { label: '강해요',  color: '#10B981',      bg: '#D1FAE5' },
  { label: '완벽해요!', color: '#059669',    bg: '#A7F3D0' },
][pwStrength.value])

const matchState = computed(() => {
  if (!pwConfirm.value) return 'idle'
  return password.value === pwConfirm.value ? 'ok' : 'bad'
})

const nicknameLen = computed(() => nickname.value.length)

async function submit() {
  error.value = ''
  if (password.value !== pwConfirm.value) { error.value = '비밀번호가 일치하지 않아요.'; return }
  if (password.value.length < 8)           { error.value = '비밀번호는 8자 이상이어야 해요.'; return }
  loading.value = true
  try {
    await auth.signup(email.value, password.value, nickname.value)
    router.push({ name: 'Login', query: { registered: 'true' } })
  } catch (e) {
    error.value = e.response?.data?.message || '회원가입에 실패했어요.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page" :class="{ ready: mounted }">

    <!-- ══════ LEFT PANEL ══════ -->
    <div class="left">
      <!-- gradient orbs -->
      <div class="orb orb1" aria-hidden="true"></div>
      <div class="orb orb2" aria-hidden="true"></div>
      <div class="orb orb3" aria-hidden="true"></div>

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

      <!-- headline -->
      <div class="headline">
        <p class="eyebrow">✦ 지금 바로 시작해보세요</p>
        <h1>
          당신의 이야기를<br>
          <span class="grad-text">세상에 전하세요</span>
        </h1>
        <p class="sub">글쓰기부터 독자와의 소통까지, 블로그 하나로 모두 가능해요.</p>
      </div>

      <!-- feature pills -->
      <div class="pills">
        <span class="pill">✍️ 마크다운 글쓰기</span>
        <span class="pill">🏷️ 스마트 태그</span>
        <span class="pill">💬 독자와 소통</span>
        <span class="pill">❤️ 좋아요 & 댓글</span>
      </div>

      <!-- preview card stack -->
      <div class="card-stack">
        <div
          v-for="(c, i) in previewCards"
          :key="i"
          class="preview-card"
          :class="`card-${i}`"
          :style="{ '--rot': c.rotate, zIndex: c.z }"
        >
          <div class="pc-tag">{{ c.emoji }} {{ c.tag }}</div>
          <p class="pc-title">{{ c.title }}</p>
          <p class="pc-excerpt">{{ c.excerpt }}</p>
          <div class="pc-footer">
            <span class="pc-author">{{ c.author }}</span>
            <span class="pc-stats">❤️ {{ c.likes }} · 💬 {{ c.comments }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ══════ RIGHT PANEL ══════ -->
    <div class="right">
      <div class="form-box">

        <!-- header -->
        <div class="fh">
          <h2 class="fh-title">회원가입</h2>
          <p class="fh-sub">
            이미 계정이 있으신가요?&nbsp;
            <router-link to="/login" class="login-link">로그인</router-link>
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
            <label for="s-email">이메일</label>
            <input
              id="s-email" v-model="email"
              type="email" required
              placeholder="hello@example.com"
              class="inp"
            />
          </div>

          <!-- nickname -->
          <div class="field">
            <div class="label-row">
              <label for="s-nick">닉네임</label>
              <span class="char-count" :class="{ warn: nicknameLen >= 18 }">
                {{ nicknameLen }}/20
              </span>
            </div>
            <input
              id="s-nick" v-model="nickname"
              type="text" required minlength="2" maxlength="20"
              placeholder="블로그에서 사용할 이름"
              class="inp"
            />
          </div>

          <!-- password -->
          <div class="field">
            <label for="s-pw">비밀번호</label>
            <div class="inp-wrap">
              <input
                id="s-pw" v-model="password"
                :type="showPw ? 'text' : 'password'"
                required minlength="8"
                placeholder="8자 이상"
                class="inp"
              />
              <button type="button" class="eye-btn" @click="showPw = !showPw" tabindex="-1">
                <!-- eye-open -->
                <svg v-if="showPw" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                <!-- eye-off -->
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/></svg>
              </button>
            </div>
            <!-- strength meter -->
            <div v-if="password" class="strength">
              <div class="bars">
                <span
                  v-for="n in 4" :key="n"
                  class="bar"
                  :style="n <= pwStrength
                    ? { background: pwInfo.color, transform: 'scaleY(1.4)' }
                    : {}"
                ></span>
              </div>
              <span
                class="str-label"
                :style="{ color: pwInfo.color, background: pwInfo.bg }"
              >{{ pwInfo.label }}</span>
            </div>
          </div>

          <!-- password confirm -->
          <div class="field">
            <label for="s-pwc">비밀번호 확인</label>
            <div class="inp-wrap">
              <input
                id="s-pwc" v-model="pwConfirm"
                :type="showPwc ? 'text' : 'password'"
                required
                placeholder="비밀번호를 다시 입력하세요"
                class="inp"
                :class="{
                  'inp-ok':  matchState === 'ok',
                  'inp-bad': matchState === 'bad',
                }"
              />
              <button type="button" class="eye-btn" @click="showPwc = !showPwc" tabindex="-1">
                <svg v-if="showPwc" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/></svg>
              </button>
            </div>
            <p v-if="matchState === 'ok'" class="hint-ok">✓ 비밀번호가 일치해요!</p>
            <p v-if="matchState === 'bad'" class="hint-bad">✗ 비밀번호가 일치하지 않아요</p>
          </div>

          <!-- submit -->
          <button type="submit" :disabled="loading" class="submit">
            <span v-if="loading" class="spin" aria-hidden="true"></span>
            <span>{{ loading ? '가입하는 중...' : '블로그 시작하기' }}</span>
            <svg v-if="!loading" viewBox="0 0 20 20" fill="currentColor" class="arrow-icon"><path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
          </button>

          <!-- divider -->
          <div class="divider"><span>가입하면 서비스 이용약관에 동의하는 것으로 간주합니다</span></div>

        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ─── FONTS ─── */
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Noto+Sans+KR:wght@300;400;500;600;700&display=swap');

/* ─── PAGE ─── */
.page {
  display: flex;
  min-height: 100vh;
  font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', sans-serif;
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.4s ease, transform 0.4s ease;
}
.page.ready { opacity: 1; transform: none; }

/* ══════════════════════════════
   LEFT PANEL
══════════════════════════════ */
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

/* gradient orbs */
.orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(60px);
}
.orb1 {
  width: 340px; height: 340px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.18), transparent 70%);
  top: -80px; left: -80px;
}
.orb2 {
  width: 280px; height: 280px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.14), transparent 70%);
  bottom: 80px; left: 40%;
  animation: orbFloat 6s ease-in-out infinite;
}
.orb3 {
  width: 200px; height: 200px;
  background: radial-gradient(circle, rgba(6, 182, 212, 0.12), transparent 70%);
  bottom: -40px; right: -40px;
}

@keyframes orbFloat {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-20px); }
}

/* ─── top bar ─── */
.left-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2.5rem;
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
.home-btn:hover {
  background: #F1F5F9;
  color: #1E293B;
  border-color: #CBD5E1;
  transform: translateX(-2px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 0.95rem;
  font-weight: 700;
  color: #4F46E5;
  letter-spacing: -0.01em;
}
.brand-dot {
  width: 8px; height: 8px;
  background: linear-gradient(135deg, #6366F1, #8B5CF6);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(99,102,241,0.5);
}

/* ─── headline ─── */
.headline {
  position: relative;
  z-index: 1;
  margin-bottom: 1.5rem;
}
.eyebrow {
  font-size: 0.72rem;
  color: #6366F1;
  font-weight: 600;
  letter-spacing: 0.05em;
  margin-bottom: 0.75rem;
}
.headline h1 {
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: clamp(1.75rem, 3vw, 2.25rem);
  font-weight: 800;
  color: #0F172A;
  line-height: 1.15;
  margin: 0 0 0.75rem;
  letter-spacing: -0.02em;
}
.grad-text {
  background: linear-gradient(135deg, #4776E6 0%, #8E54E9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.sub {
  font-size: 0.85rem;
  color: #64748B;
  line-height: 1.6;
  font-weight: 400;
}

/* ─── feature pills ─── */
.pills {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-bottom: 2rem;
}
.pill {
  padding: 0.35rem 0.8rem;
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 100px;
  font-size: 0.75rem;
  color: #475569;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}

/* ─── preview card stack ─── */
.card-stack {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.preview-card {
  position: absolute;
  width: min(88%, 320px);
  background: white;
  border-radius: 16px;
  padding: 1.1rem 1.25rem;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.08), 0 1px 4px rgba(15,23,42,0.04);
  border: 1px solid rgba(226, 232, 240, 0.7);
  transform: rotate(var(--rot, 0deg));
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card-stack:hover .card-0 { transform: rotate(-8deg) translateY(-4px); }
.card-stack:hover .card-1 { transform: rotate(-3deg) translateY(-2px); }
.card-stack:hover .card-2 { transform: rotate(4deg) translateY(4px); }

.card-0 { bottom: 100px; }
.card-1 { bottom: 55px; }
.card-2 { bottom: 10px; }

.pc-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.2rem 0.6rem;
  background: #EEF2FF;
  color: #4F46E5;
  border-radius: 100px;
  font-size: 0.68rem;
  font-weight: 600;
  margin-bottom: 0.55rem;
}
.pc-title {
  font-size: 0.85rem;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 0.3rem;
  line-height: 1.3;
}
.pc-excerpt {
  font-size: 0.75rem;
  color: #94A3B8;
  line-height: 1.5;
  margin: 0 0 0.7rem;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.pc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.72rem;
}
.pc-author { color: #64748B; font-weight: 500; }
.pc-stats  { color: #94A3B8; }

/* ══════════════════════════════
   RIGHT PANEL
══════════════════════════════ */
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
  max-width: 400px;
}

/* ─── form header ─── */
.fh { margin-bottom: 2rem; }
.fh-title {
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 1.75rem;
  font-weight: 800;
  color: #0F172A;
  letter-spacing: -0.02em;
  margin: 0 0 0.4rem;
}
.fh-sub {
  font-size: 0.85rem;
  color: #94A3B8;
  font-weight: 400;
}
.login-link {
  color: #4F46E5;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.15s;
}
.login-link:hover { color: #4338CA; text-decoration: underline; }

/* ─── error ─── */
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

/* ─── form ─── */
.form { display: flex; flex-direction: column; gap: 1.25rem; }

.field { display: flex; flex-direction: column; gap: 0.4rem; }

.field label,
.label-row {
  font-size: 0.78rem;
  font-weight: 600;
  color: #374151;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 0.72rem;
  font-weight: 400;
  color: #9CA3AF;
  transition: color 0.2s;
}
.char-count.warn { color: #F59E0B; }

/* ─── inputs ─── */
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
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

/* inside inp-wrap, give room for eye button */
.inp-wrap .inp { padding-right: 2.75rem; }

.inp-ok  { border-color: #10B981 !important; }
.inp-bad { border-color: #EF4444 !important; }
.inp-ok:focus  { box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1) !important; }
.inp-bad:focus { box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1)  !important; }

/* ─── eye button ─── */
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

/* ─── password strength ─── */
.strength {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.25rem;
}
.bars { display: flex; gap: 4px; }
.bar {
  width: 36px; height: 4px;
  background: #E2E8F0;
  border-radius: 2px;
  transition: background 0.3s ease, transform 0.2s ease;
}
.str-label {
  font-size: 0.68rem;
  font-weight: 700;
  padding: 0.15rem 0.5rem;
  border-radius: 100px;
  transition: color 0.3s, background 0.3s;
}

/* ─── match hints ─── */
.hint-ok  { font-size: 0.72rem; color: #10B981; font-weight: 600; margin: 0; }
.hint-bad { font-size: 0.72rem; color: #EF4444; font-weight: 600; margin: 0; }

/* ─── submit button ─── */
.submit {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.9rem 1.5rem;
  background: linear-gradient(135deg, #4776E6 0%, #8E54E9 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 0.925rem;
  font-weight: 700;
  font-family: 'Noto Sans KR', sans-serif;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.18s, box-shadow 0.2s;
  box-shadow: 0 4px 16px rgba(71, 118, 230, 0.35);
  margin-top: 0.25rem;
}
.submit:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(71, 118, 230, 0.45);
}
.submit:active:not(:disabled) { transform: translateY(0); box-shadow: none; }
.submit:disabled { opacity: 0.55; cursor: not-allowed; }

.arrow-icon { width: 18px; height: 18px; transition: transform 0.2s; }
.submit:hover:not(:disabled) .arrow-icon { transform: translateX(3px); }

.spin {
  width: 16px; height: 16px;
  border: 2.5px solid rgba(255,255,255,0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ─── divider ─── */
.divider {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #CBD5E1;
  font-size: 0.68rem;
  text-align: center;
}
.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #F1F5F9;
}
.divider span { color: #94A3B8; white-space: nowrap; }

/* ─── responsive ─── */
@media (max-width: 820px) {
  .page { flex-direction: column; }
  .left { width: 100%; padding: 1.5rem; min-height: auto; }
  .card-stack { min-height: 240px; }
  .right { width: 100%; padding: 2rem 1.25rem; }
  .form-box { max-width: 100%; }
  .headline h1 { font-size: 1.6rem; }
}
</style>