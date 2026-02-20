<script setup>
import { ref } from 'vue'

const props = defineProps({
  parentId:    { type: Number, default: null },
  placeholder: { type: String, default: '댓글을 작성하세요...' },
})
const emit = defineEmits(['submit', 'cancel'])
const content = ref('')

function handleSubmit() {
  const trimmed = content.value.trim()
  if (!trimmed) return
  emit('submit', trimmed)
  content.value = ''
}
</script>

<template>
  <div class="comment-form">
    <textarea
      v-model="content"
      :placeholder="placeholder"
      rows="3"
      class="comment-textarea"
    />
    <div class="form-actions">
      <button v-if="parentId" @click="emit('cancel')" class="cancel-btn">취소</button>
      <button @click="handleSubmit" :disabled="!content.trim()" class="submit-btn">
        <svg viewBox="0 0 20 20" fill="currentColor"><path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"/></svg>
        등록
      </button>
    </div>
  </div>
</template>

<style scoped>
.comment-form { display: flex; flex-direction: column; gap: 0.6rem; }

.comment-textarea {
  width: 100%;
  padding: 0.85rem 1rem;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  font-size: 0.875rem;
  color: #0F172A;
  font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', sans-serif;
  resize: none;
  outline: none;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
  line-height: 1.7;
}
.comment-textarea::placeholder { color: #CBD5E1; }
.comment-textarea:focus {
  border-color: #6366F1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 0.5rem;
}

.cancel-btn {
  padding: 0.5rem 1rem;
  background: none;
  border: 1.5px solid #E2E8F0;
  border-radius: 8px;
  font-size: 0.8rem;
  color: #64748B;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: all 0.18s;
}
.cancel-btn:hover { background: #F1F5F9; color: #374151; }

.submit-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1.1rem;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(71,118,230,0.25);
}
.submit-btn svg { width: 13px; height: 13px; }
.submit-btn:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
.submit-btn:disabled { opacity: 0.45; cursor: not-allowed; }
</style>
