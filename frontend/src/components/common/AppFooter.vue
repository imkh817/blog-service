<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <span class="footer-dot"></span>
        <span>Blog</span>
      </div>
      <p class="footer-copy">© {{ new Date().getFullYear() }} Blog. 당신의 이야기를 세상에.</p>
    </div>
  </footer>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@700&display=swap');

.site-footer {
  border-top: 1px solid #E8EDF8;
  background: white;
  padding: 1.5rem 0;
  margin-top: auto;
}
.footer-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 0 1.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.footer-brand {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 0.9rem;
  font-weight: 800;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.footer-dot {
  width: 7px; height: 7px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border-radius: 50%;
}
.footer-copy {
  font-size: 0.78rem;
  color: #94A3B8;
  margin: 0;
}
</style>